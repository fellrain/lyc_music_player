package com.rain.util;

import java.util.UUID;

/**
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class UUIDUtil {

    /**
     * 生成唯一ID
     */
    public static String generateShareId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
