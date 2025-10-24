package com.rain.manager;

import com.rain.MusicPlayerMod;

import java.util.Objects;

/**
 * Cookie管理器 - 用于管理API请求所需的Cookie
 *
 * @author 落雨川
 */
public class CookieManager {
    private static volatile CookieManager instance;
    private String cookie = "";

    private CookieManager() {
    }

    public static CookieManager getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (CookieManager.class) {
                if (Objects.isNull(instance)) {
                    instance = new CookieManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取当前Cookie
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * 设置Cookie
     */
    public void setCookie(String cookie) {
        if (Objects.isNull(cookie)) {
            this.cookie = "";
        } else {
            this.cookie = cookie.trim();
        }
        MusicPlayerMod.LOGGER.info("Cookie已更新，长度: {}", this.cookie.length());
    }

    /**
     * 清除Cookie
     */
    public void clearCookie() {
        this.cookie = "";
        MusicPlayerMod.LOGGER.info("Cookie已清除");
    }

    /**
     * 检查是否有Cookie
     */
    public boolean hasCookie() {
        return cookie != null && !cookie.isEmpty();
    }
}
