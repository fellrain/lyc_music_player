package com.rain.util;

/**
 * 时间工具类
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class TimeUtils {

    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long SECONDS_PER_MINUTE = 60;

    private TimeUtils() {
    }

    /**
     * 将毫秒数格式化为 MM:SS 格式
     *
     * @param milliseconds 毫秒数
     * @return 格式化的时间字符串 (分:秒)
     */
    public static String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / MILLISECONDS_PER_SECOND,
                minutes = totalSeconds / SECONDS_PER_MINUTE,
                seconds = totalSeconds % SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, seconds);
    }
}