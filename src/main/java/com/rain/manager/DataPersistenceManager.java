package com.rain.manager;

import com.rain.MusicPlayerMod;
import com.rain.model.MusicTrack;
import com.rain.util.CollUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * 数据持久化管理器
 * <p>
 * 负责所有本地数据的保存和加载
 * </p>
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class DataPersistenceManager {

    private static final String DATA_DIR = "lyc_music_player";
    private static final String COOKIE_FILE = "cookie.json";
    private static final String PLAYLIST_FILE = "playlist.json";
    private static final String FAVORITES_FILE = "favorites.json";
    private static final String CATEGORIES_FILE = "categories.json";

    private static final long COOKIE_EXPIRY_DAYS = 3;
    private static final long COOKIE_EXPIRY_MILLIS = COOKIE_EXPIRY_DAYS * 24 * 60 * 60 * 1000;

    private final Path dataDirectory;

    public DataPersistenceManager() {
        // 获取游戏配置目录
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.dataDirectory = configDir.resolve(DATA_DIR);
        // 创建数据目录
        try {
            Files.createDirectories(dataDirectory);
            MusicPlayerMod.LOGGER.info("数据目录已初始化: {}", dataDirectory);
        } catch (IOException e) {
            MusicPlayerMod.LOGGER.error("创建数据目录失败", e);
        }
    }

    // ==================== Cookie持久化 ====================

    /**
     * 保存Cookie
     */
    public void saveCookie(String cookie) {
        try {
            JSONObject data = new JSONObject();
            data.put("cookie", cookie);
            data.put("timestamp", System.currentTimeMillis());
            data.put("expiry", System.currentTimeMillis() + COOKIE_EXPIRY_MILLIS);
            writeJsonFile(COOKIE_FILE, data);
            MusicPlayerMod.LOGGER.info("Cookie已保存，有效期至: {}",
                    new Date(data.getLong("expiry")));
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("保存Cookie失败", e);
        }
    }

    /**
     * 加载Cookie
     *
     * @return Cookie字符串，如果不存在或已过期返回null
     */
    public String loadCookie() {
        try {
            JSONObject data = readJsonFile(COOKIE_FILE);
            if (Objects.isNull(data)) {
                return null;
            }
            long expiry = data.optLong("expiry", 0),
                    now = System.currentTimeMillis();
            // 检查是否过期
            if (expiry > 0 && now > expiry) {
                MusicPlayerMod.LOGGER.info("Cookie已过期，已自动清理");
                deleteCookie();
                return null;
            }
            String cookie = data.optString("cookie", null);
            if (cookie != null && !cookie.isEmpty()) {
                MusicPlayerMod.LOGGER.info("Cookie已加载，剩余有效时间: {} 小时",
                        (expiry - now) / (1000 * 60 * 60));
            }
            return cookie;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("加载Cookie失败", e);
            return null;
        }
    }

    /**
     * 删除Cookie
     */
    public void deleteCookie() {
        deleteFile(COOKIE_FILE);
        MusicPlayerMod.LOGGER.info("Cookie已删除");
    }

    // ==================== 播放列表持久化 ====================

    /**
     * 保存播放列表
     */
    public void savePlaylist(List<MusicTrack> playlist, int currentIndex) {
        try {
            JSONObject data = new JSONObject();
            data.put("currentIndex", currentIndex);
            data.put("timestamp", System.currentTimeMillis());
            JSONArray tracksArray = new JSONArray();
            for (MusicTrack track : playlist) {
                tracksArray.put(trackToJson(track));
            }
            data.put("tracks", tracksArray);
            writeJsonFile(PLAYLIST_FILE, data);
            MusicPlayerMod.LOGGER.info("播放列表已保存，共 {} 首歌曲", playlist.size());
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("保存播放列表失败", e);
        }
    }

    /**
     * 加载播放列表
     *
     * @return Map包含 "tracks" 和 "currentIndex"
     */
    public Map<String, Object> loadPlaylist() {
        try {
            JSONObject data = readJsonFile(PLAYLIST_FILE);
            if (Objects.isNull(data)) {
                return null;
            }
            JSONArray tracksArray = data.optJSONArray("tracks");
            if (CollUtil.isEmpty(tracksArray)) {
                return null;
            }
            List<MusicTrack> tracks = new ArrayList<>();
            for (int i = 0; i < tracksArray.length(); i++) {
                MusicTrack track = jsonToTrack(tracksArray.getJSONObject(i));
                if (!Objects.isNull(track)) {
                    tracks.add(track);
                }
            }
            Map<String, Object> result = new HashMap<>(3);
            result.put("tracks", tracks);
            result.put("currentIndex", data.optInt("currentIndex", -1));
            MusicPlayerMod.LOGGER.info("播放列表已加载，共 {} 首歌曲", tracks.size());
            return result;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("加载播放列表失败", e);
            return null;
        }
    }

    // ==================== 收藏列表持久化 ====================

    /**
     * 保存收藏列表
     */
    public void saveFavorites(Set<String> favoriteIds) {
        try {
            JSONObject data = new JSONObject();
            data.put("timestamp", System.currentTimeMillis());
            data.put("favorites", new JSONArray(favoriteIds));
            writeJsonFile(FAVORITES_FILE, data);
            MusicPlayerMod.LOGGER.info("收藏列表已保存，共 {} 首", favoriteIds.size());
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("保存收藏列表失败", e);
        }
    }

    /**
     * 加载收藏列表
     */
    public Set<String> loadFavorites() {
        try {
            JSONObject data = readJsonFile(FAVORITES_FILE);
            if (Objects.isNull(data)) {
                return new HashSet<>();
            }
            JSONArray favArray = data.optJSONArray("favorites");
            if (CollUtil.isEmpty(favArray)) {
                return new HashSet<>();
            }
            Set<String> favorites = new HashSet<>();
            for (int i = 0; i < favArray.length(); i++) {
                favorites.add(favArray.getString(i));
            }
            MusicPlayerMod.LOGGER.info("收藏列表已加载，共 {} 首", favorites.size());
            return favorites;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("加载收藏列表失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 保存收藏歌曲列表（包含完整信息）
     */
    public void saveFavoriteTracks(List<MusicTrack> favoriteTracks) {
        try {
            JSONObject data = new JSONObject();
            data.put("timestamp", System.currentTimeMillis());
            JSONArray tracksArray = new JSONArray();
            for (MusicTrack track : favoriteTracks) {
                tracksArray.put(trackToJson(track));
            }
            data.put("tracks", tracksArray);
            writeJsonFile(FAVORITES_FILE, data);
            MusicPlayerMod.LOGGER.info("收藏歌曲列表已保存，共 {} 首", favoriteTracks.size());
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("保存收藏歌曲列表失败", e);
        }
    }

    /**
     * 加载收藏歌曲列表（包含完整信息）
     */
    public List<MusicTrack> loadFavoriteTracks() {
        try {
            JSONObject data = readJsonFile(FAVORITES_FILE);
            if (Objects.isNull(data)) {
                return new ArrayList<>();
            }
            JSONArray tracksArray = data.optJSONArray("tracks");
            if (CollUtil.isEmpty(tracksArray)) {
                return new ArrayList<>();
            }
            List<MusicTrack> tracks = new ArrayList<>();
            for (int i = 0; i < tracksArray.length(); i++) {
                MusicTrack track = jsonToTrack(tracksArray.getJSONObject(i));
                if (!Objects.isNull(track)) {
                    tracks.add(track);
                }
            }
            MusicPlayerMod.LOGGER.info("收藏歌曲列表已加载，共 {} 首", tracks.size());
            return tracks;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("加载收藏歌曲列表失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 自定义分类持久化 ====================

    /**
     * 保存所有分类
     */
    public void saveCategories(Map<String, List<MusicTrack>> categories) {
        try {
            JSONObject data = new JSONObject();
            data.put("timestamp", System.currentTimeMillis());
            JSONObject categoriesObj = new JSONObject();
            for (Map.Entry<String, List<MusicTrack>> entry : categories.entrySet()) {
                JSONArray tracksArray = new JSONArray();
                for (MusicTrack track : entry.getValue()) {
                    tracksArray.put(trackToJson(track));
                }
                categoriesObj.put(entry.getKey(), tracksArray);
            }
            data.put("categories", categoriesObj);
            writeJsonFile(CATEGORIES_FILE, data);
            MusicPlayerMod.LOGGER.info("分类已保存，共 {} 个分类", categories.size());
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("保存分类失败", e);
        }
    }

    /**
     * 加载所有分类
     */
    public Map<String, List<MusicTrack>> loadCategories() {
        try {
            JSONObject data = readJsonFile(CATEGORIES_FILE);
            if (Objects.isNull(data)) {
                return new LinkedHashMap<>();
            }
            JSONObject categoriesObj = data.optJSONObject("categories");
            if (Objects.isNull(categoriesObj)) {
                return new LinkedHashMap<>();
            }
            Map<String, List<MusicTrack>> categories = new LinkedHashMap<>();
            for (String categoryName : categoriesObj.keySet()) {
                JSONArray tracksArray = categoriesObj.getJSONArray(categoryName);
                List<MusicTrack> tracks = new ArrayList<>(tracksArray.length());
                for (int i = 0; i < tracksArray.length(); i++) {
                    MusicTrack track = jsonToTrack(tracksArray.getJSONObject(i));
                    if (track != null) {
                        tracks.add(track);
                    }
                }
                categories.put(categoryName, tracks);
            }
            MusicPlayerMod.LOGGER.info("分类已加载，共 {} 个分类", categories.size());
            return categories;
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("加载分类失败", e);
            return new LinkedHashMap<>();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * MusicTrack转JSON
     */
    private JSONObject trackToJson(MusicTrack track) {
        JSONObject json = new JSONObject();
        json.put("id", track.getId());
        json.put("title", track.getTitle());
        json.put("artist", track.getArtist());
        json.put("album", track.getAlbum());
        json.put("url", track.getUrl());
        json.put("duration", track.getDuration());
        return json;
    }

    /**
     * JSON转MusicTrack
     */
    private MusicTrack jsonToTrack(JSONObject json) {
        try {
            return new MusicTrack(
                    json.getString("id"),
                    json.getString("title"),
                    json.getString("artist"),
                    json.getString("album"),
                    json.getString("url"),
                    json.getLong("duration")
            );
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("解析音轨JSON失败", e);
            return null;
        }
    }

    /**
     * 写入JSON文件
     */
    private void writeJsonFile(String filename, JSONObject data) throws IOException {
        Path filePath = dataDirectory.resolve(filename);
        // 首行2缩进
        String jsonString = data.toString(2);
        Files.writeString(filePath, jsonString, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * 读取JSON文件
     */
    private JSONObject readJsonFile(String filename) {
        try {
            Path filePath = dataDirectory.resolve(filename);
            if (!Files.exists(filePath)) {
                return null;
            }
            String content = Files.readString(filePath);
            return new JSONObject(content);
        } catch (Exception e) {
            MusicPlayerMod.LOGGER.error("读取文件失败: {}", filename, e);
            return null;
        }
    }

    /**
     * 删除文件
     */
    private void deleteFile(String filename) {
        try {
            Path filePath = dataDirectory.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            MusicPlayerMod.LOGGER.error("删除文件失败: {}", filename, e);
        }
    }

    /**
     * 获取数据目录路径
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }
}
