package com.rain.common.util;

/**
 * 字符串工具类
 *
 * @author 落雨川
 * @version 1.2
 * @since 1.0
 */
public class StrUtil {

    /**
     * 字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
