package com.rain.client;

import com.rain.client.audio.AudioManager;
import com.rain.client.command.MusicCommands;
import com.rain.client.gui.KeyBindings;
import com.rain.client.gui.MusicHudRenderer;
import com.rain.client.manager.*;
import com.rain.client.network.MusicAPIClient;
import com.rain.common.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import com.rain.client.network.handler.ClientNetworkHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 音乐播放器模组主类
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class MusicPlayerClientMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(ModConfig.MOD_ID);

    private static MusicPlayerClientMod instance;

    private AudioManager audioManager;

    private MusicAPIClient apiClient;

    private MusicManager musicManager;

    private LyricManager lyricManager;

    private MusicHudRenderer hudRenderer;

    private DataPersistenceManager persistenceManager;

    private FavoriteManager favoriteManager;

    private CategoryManager categoryManager;

    private ClientMusicShareManager shareManager;

    /**
     * 客户端初始化方法
     */
    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("初始化小落音乐播放器");
        try {
            // 注册网络通信
            ClientNetworkHandler.registerReceivers();
            // 初始化持久化管理器
            persistenceManager = new DataPersistenceManager();
            // 初始化核心组件
            audioManager = new AudioManager();
            // 创建音乐管理器（管理播放列表）
            musicManager = new MusicManager(audioManager);
            // 创建API客户端
            apiClient = new MusicAPIClient();
            // 创建歌词管理器（解析和管理歌词）
            lyricManager = new LyricManager(apiClient);
            // 创建HUD渲染器（在游戏界面显示歌词）
            hudRenderer = new MusicHudRenderer(audioManager, lyricManager);
            // 初始化功能管理器
            // 收藏管理器
            favoriteManager = new FavoriteManager(persistenceManager);
            // 分类管理器
            categoryManager = new CategoryManager(persistenceManager);
            // 音乐分享管理器
            shareManager = new ClientMusicShareManager();
            // 初始化Cookie管理器并加载数据
            CookieManager.getInstance().initialize(persistenceManager);
            // 初始化音乐管理器并加载播放列表
            musicManager.initialize(persistenceManager);
            // 设置回调
            audioManager.setOnTrackEndCallback(() -> {
                if (!musicManager.isPlaylistEmpty()) {
                    musicManager.playNext();
                }
            });
            // 设置播放开始回调，自动加载歌词
            audioManager.setOnTrackStartCallback(() -> {
                if (!Objects.isNull(audioManager.getCurrentTrack())) {
                    lyricManager.loadLyric(audioManager.getCurrentTrack());
                }
            });
            // 注册命令和键位
            ClientCommandRegistrationCallback.EVENT
                    .register((dispatcher, registryAccess)
                            -> MusicCommands.register(dispatcher));
            KeyBindings.register();
            LOGGER.info("小落音乐播放器初始化完成");
        } catch (Exception e) {
            LOGGER.error("初始化小落音乐播放器失败", e);
        }
    }

    /**
     * 获取音乐模组实例
     */
    public static MusicPlayerClientMod getInstance() {
        return instance;
    }

    /**
     * 获取音频管理器
     */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * 获取API客户端
     */
    public MusicAPIClient getApiClient() {
        return apiClient;
    }

    /**
     * 获取音乐管理器
     */
    public MusicManager getMusicManager() {
        return musicManager;
    }

    /**
     * 获取歌词管理器
     */
    public LyricManager getLyricManager() {
        return lyricManager;
    }

    /**
     * 获取HUD渲染器
     */
    public MusicHudRenderer getHudRenderer() {
        return hudRenderer;
    }

    /**
     * 获取持久化管理器
     */
    public DataPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * 获取收藏管理器
     */
    public FavoriteManager getFavoriteManager() {
        return favoriteManager;
    }

    /**
     * 获取分类管理器
     */
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    /**
     * 获取音乐分享管理器
     */
    public ClientMusicShareManager getShareManager() {
        return shareManager;
    }
    
    /**
     * 释放所有资源
     */
    public void shutdown() {
        if (!Objects.isNull(audioManager)) {
            audioManager.shutdown();
        }
    }
}
