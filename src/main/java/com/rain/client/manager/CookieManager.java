package com.rain.client.manager;

import com.rain.client.MusicPlayerMod;

import java.util.Objects;

/**
 * Cookie管理器 - 用于管理API请求所需的Cookie
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class CookieManager {
    private static volatile CookieManager instance;
    private String cookie = "";
    private DataPersistenceManager persistenceManager;

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
     * 初始化持久化管理器
     */
    public void initialize(DataPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        loadCookie();
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
        // 持久化保存
        if (!Objects.isNull(persistenceManager)) {
            if (this.cookie.isEmpty()) {
                persistenceManager.deleteCookie();
            } else {
                persistenceManager.saveCookie(this.cookie);
            }
        }
        MusicPlayerMod.LOGGER.info("Cookie已更新，长度: {}", this.cookie.length());
    }

    /**
     * 清除Cookie
     */
    public void clearCookie() {
        this.cookie = "";
        if (!Objects.isNull(persistenceManager)) {
            persistenceManager.deleteCookie();
        }
        MusicPlayerMod.LOGGER.info("Cookie已清除");
    }

    /**
     * 检查是否有Cookie
     */
    public boolean hasCookie() {
        return cookie != null && !cookie.isEmpty();
    }

    /**
     * 从本地加载Cookie
     */
    private void loadCookie() {
        if (Objects.isNull(persistenceManager)) {
            return;
        }
        String loaded = persistenceManager.loadCookie();
        if (!Objects.isNull(loaded) && !loaded.isEmpty()) {
            this.cookie = loaded;
            MusicPlayerMod.LOGGER.info("Cookie已从本地加载");
        }
    }
}
