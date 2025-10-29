package com.rain.common.enums;

/**
 * @author 落雨川
 */
public enum MusicStrategyEnum {

    NETEASE("Netease", "WYY"),
    ;

    private final String platform;
    private final String description;

    /**
     * 构造播放模式
     */
    MusicStrategyEnum(String platform, String description) {
        this.platform = platform;
        this.description = description;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDescription() {
        return description;
    }
}
