package com.rain.client.manager;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.model.Lyric;
import com.rain.client.model.MusicTrack;
import com.rain.client.network.MusicAPIClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 歌词管理器
 * 充血模型
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class LyricManager {

    private final Map<String, Lyric> lyricCache;
    private final MusicAPIClient apiClient;
    private Lyric currentLyric;

    public LyricManager(MusicAPIClient apiClient) {
        this.apiClient = apiClient;
        this.lyricCache = new HashMap<>();
        MusicPlayerClientMod.LOGGER.info("歌词管理器已初始化");
    }

    /**
     * 加载歌词
     *
     * @param track 音乐轨道
     */
    public void loadLyric(MusicTrack track) {
        if (Objects.isNull(track)) {
            currentLyric = null;
            return;
        }
        String trackId = track.getId();
        // 检查缓存
        if (lyricCache.containsKey(trackId)) {
            currentLyric = lyricCache.get(trackId);
            MusicPlayerClientMod.LOGGER.info("从缓存加载歌词: {}", track.getTitle());
            return;
        }
        // 异步获取歌词
        apiClient.getLyric(trackId).thenAccept(lyric -> {
            if (!Objects.isNull(lyric)) {
                lyricCache.put(trackId, lyric);
                // 如果当前还在播放这首歌，更新歌词
                if (!Objects.isNull(track) && track.getId().equals(trackId)) {
                    currentLyric = lyric;
                    MusicPlayerClientMod.LOGGER.info("歌词加载成功: {}", track.getTitle());
                }
            } else {
                MusicPlayerClientMod.LOGGER.warn("无法获取歌词: {}", track.getTitle());
                currentLyric = null;
            }
        });
    }

    /**
     * 获取当前歌词
     */
    public Lyric getCurrentLyric() {
        return currentLyric;
    }

    /**
     * 清除当前歌词
     */
    public void clearCurrentLyric() {
        this.currentLyric = null;
    }

    /**
     * 清除歌词缓存
     */
    public void clearCache() {
        lyricCache.clear();
        MusicPlayerClientMod.LOGGER.info("歌词缓存已清空");
    }
}
