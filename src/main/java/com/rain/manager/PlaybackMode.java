package com.rain.manager;

/**
 * 播放模式枚举
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public enum PlaybackMode {
    SEQUENTIAL("顺序播放", "§7按顺序播放音轨"),
    SHUFFLE("随机播放", "§7随机播放音轨"),
    REPEAT_ONE("单曲循环", "§7重复播放当前音轨"),
    REPEAT_ALL("列表循环", "§7循环播放整个播放列表");

    private final String displayName;
    private final String description;

    /**
     * 构造播放模式
     */
    PlaybackMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取模式描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取模式图标
     */
    public String getIcon() {
        return switch (this) {
            case SEQUENTIAL -> "→";
            case SHUFFLE -> "⤨";
            case REPEAT_ONE -> "↻1";
            case REPEAT_ALL -> "↻";
        };
    }
}