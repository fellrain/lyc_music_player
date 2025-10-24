package com.rain.config;

/**
 * 音乐配置类
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class ModConfig {

    private ModConfig() {
    }

    // ========== API配置 ==========
    
    /**
     * API URL
     */
    public static final String SEARCH_API_URL = "";

    /**
     * API读取超时时间（毫秒）
     */
    public static final int READ_TIMEOUT = 10000;
    
    /**
     * API连接超时时间（毫秒）
     */
    public static final int CONNECT_TIMEOUT = 100000;

    // ========== 播放列表配置 ==========
    
    /**
     * 播放列表最大歌曲数量限制
     */
    public static final int MAX_PLAYLIST_SIZE = 100;
    
    /**
     * 搜索结果返回的最大数量
     */
    public static final int SEARCH_RESULTS_LIMIT = 10;

    // ========== 音频配置 ==========
    
    /**
     * 音质等级-exhigh
     */
    public static final String AUDIO_QUALITY = "exhigh";
}