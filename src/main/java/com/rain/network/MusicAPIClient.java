package com.rain.network;

import com.rain.MusicPlayerMod;
import com.rain.config.ModConfig;
import com.rain.manager.CookieManager;
import com.rain.model.MusicTrack;
import com.rain.model.SearchResult;
import com.rain.util.CollUtil;
import com.rain.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 音乐API客户端
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class MusicAPIClient {

    private static final int API_SUCCESS_CODE = 200;
    private static final String UNKNOWN_SONG_TITLE = "未知歌曲";
    private static final String UNKNOWN_ARTIST = "未知艺术家";
    private static final String ARTIST_SEPARATOR = ", ";
    private final String apiBaseUrl;

    public MusicAPIClient() {
        this.apiBaseUrl = ModConfig.SEARCH_API_URL;
        MusicPlayerMod.LOGGER.info("MusicAPIClient 初始化 - API基础URL: {}", apiBaseUrl);
    }

    /**
     * 搜索音乐
     */
    public CompletableFuture<SearchResult> searchMusic(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = apiBaseUrl + "/search?keywords=" +
                        HttpUtil.encodeParam(query) + "&limit=" +
                        ModConfig.SEARCH_RESULTS_LIMIT;
                Map<String, String> headers = buildHeaders();
                String responseBody = HttpUtil.get(url, headers);
                MusicPlayerMod.LOGGER.info("搜索音乐: {}", query);
                MusicPlayerMod.LOGGER.debug("搜索响应: {}", responseBody);
                return parseSearchResponse(query, responseBody);
            } catch (Exception e) {
                MusicPlayerMod.LOGGER.error("搜索音乐失败", e);
                return new SearchResult(query, new ArrayList<>(), 0);
            }
        });
    }

    /**
     * 获取音乐播放URL
     */
    public String getUrl(String trackId) {
        Map<String, String> resMap;
        return CollUtil.isEmpty(resMap = getUrls(List.of(trackId))) ? null : resMap.get(trackId);
    }

    /**
     * 获取音乐播放URL
     */
    public Map<String, String> getUrls(List<String> trackIds) {
        try {
            if (CollUtil.isEmpty(trackIds)) return null;
            String ids = String.join(",", trackIds);
            String url = apiBaseUrl + "/song/url/v1?id=" +
                    HttpUtil.encodeParam(ids) + "&level=" +
                    ModConfig.AUDIO_QUALITY;
            Map<String, String> headers = buildHeaders();
            MusicPlayerMod.LOGGER.info("获取歌曲URL: {} (音质: {}", ids, ModConfig.AUDIO_QUALITY);
            String responseBody = HttpUtil.get(url, headers);
            return parseTrackDetailResponse(responseBody);
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("获取歌曲URL失败", e);
            return null;
        }
    }

    /**
     * 构建请求头，包含Cookie（如果有）
     */
    private Map<String, String> buildHeaders() {
        Map<String, String> headers = HttpUtil.getRequestHeader();
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager.hasCookie())
            headers.put("Cookie", cookieManager.getCookie());
        return headers;
    }

    /**
     * 解析搜索响应
     */
    private SearchResult parseSearchResponse(String query, String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!(json.optInt("code") == API_SUCCESS_CODE)) {
                MusicPlayerMod.LOGGER.warn("搜索API返回异常响应");
                return new SearchResult(query, new ArrayList<>(), 0);
            }
            JSONObject result = json.optJSONObject("result");
            if (Objects.isNull(result))
                return new SearchResult(query, new ArrayList<>(), 0);
            List<MusicTrack> tracks = parseTracksFromResult(result);
            int totalResults = result.optInt("songCount", tracks.size());
            MusicPlayerMod.LOGGER.info("找到 {} 首歌曲，关键词: {}", tracks.size(), query);
            return new SearchResult(query, tracks, totalResults);
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("解析搜索响应失败", e);
            return new SearchResult(query, new ArrayList<>(), 0);
        }
    }

    /**
     * 从搜索结果中解析音轨列表
     */
    private List<MusicTrack> parseTracksFromResult(JSONObject result) {
        List<MusicTrack> tracks = new ArrayList<>();
        JSONArray songs = result.optJSONArray("songs");
        if (CollUtil.isEmpty(songs)) {
            return tracks;
        }
        int limit = Math.min(songs.length(), ModConfig.SEARCH_RESULTS_LIMIT);
        List<String> trackIds = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            JSONObject songJson = songs.getJSONObject(i);
            MusicTrack track = parseTrack(songJson);
            if (!Objects.isNull(track)) {
                tracks.add(track);
                trackIds.add(track.getId());
            }
        }
        Map<String, String> urls = getUrls(trackIds);
        if (!CollUtil.isEmpty(urls)) {
            tracks.forEach(i
                    -> i.setUrl(urls.get(i.getId())));
        }
        return tracks;
    }

    /**
     * 解析单个音轨信息
     */
    private MusicTrack parseTrack(JSONObject songJson) {
        try {
            String id = String.valueOf(songJson.optLong("id")),
                    name = songJson.optString("name", UNKNOWN_SONG_TITLE),
                    artist = parseArtists(songJson.optJSONArray("artists")),
                    album = parseAlbum(songJson.optJSONObject("album"));
            long duration = songJson.optLong("duration", 0);
            return new MusicTrack(id, name, artist, album, "", duration);
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("解析歌曲信息失败", e);
            return null;
        }
    }

    /**
     * 解析艺术家信息
     */
    private String parseArtists(JSONArray artists) {
        if (CollUtil.isEmpty(artists)) {
            return UNKNOWN_ARTIST;
        }
        StringBuilder artistNames = new StringBuilder();
        for (int i = 0; i < artists.length(); i++) {
            JSONObject artistObj = artists.getJSONObject(i);
            String name = artistObj.optString("name");
            if (!name.isEmpty()) {
                if (i > 0) {
                    artistNames.append(ARTIST_SEPARATOR);
                }
                artistNames.append(name);
            }
        }
        return !artistNames.isEmpty() ? artistNames.toString() : UNKNOWN_ARTIST;
    }

    /**
     * 解析专辑信息
     */
    private String parseAlbum(JSONObject albumObj) {
        return Objects.isNull(albumObj) ? "" : albumObj.optString("name", "");
    }

    /**
     * 解析音轨详情响应
     */
    private Map<String, String> parseTrackDetailResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!(json.optInt("code") == API_SUCCESS_CODE)) {
                return null;
            }
            JSONArray data = json.optJSONArray("data");
            if (CollUtil.isEmpty(data)) {
                return null;
            }
            HashMap<String, String> resMap = new HashMap<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                String url = obj.optString("url", null)
                        , id = obj.optString("id", null);
                resMap.put(id, url);
            }
            return resMap;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("解析歌曲详情响应失败", e);
            return null;
        }
    }
}