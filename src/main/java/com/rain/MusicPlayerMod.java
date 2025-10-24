package com.rain;

import com.rain.audio.AudioManager;
import com.rain.command.MusicCommands;
import com.rain.gui.KeyBindings;
import com.rain.manager.MusicManager;
import com.rain.network.MusicAPIClient;
import net.fabricmc.api.ClientModInitializer;
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
public final class MusicPlayerMod implements ClientModInitializer {

    public static final String MOD_ID = "lycMusicPlayer";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MusicPlayerMod instance;

    private AudioManager audioManager;

    private MusicAPIClient apiClient;

    private MusicManager musicManager;

    /**
     * 客户端初始化方法
     */
    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("初始化小落音乐播放器");
        try {
            audioManager = new AudioManager();
            musicManager = new MusicManager(audioManager);
            audioManager.setOnTrackEndCallback(() -> {
                if (!musicManager.isPlaylistEmpty()) {
                    musicManager.playNext();
                }
            });
            apiClient = new MusicAPIClient();
            ClientCommandRegistrationCallback.EVENT
                    .register((dispatcher, registryAccess)
                            -> MusicCommands.register(dispatcher));
            // 注册键盘绑定
            KeyBindings.register();
        } catch (Exception e) {
            LOGGER.error("初始化小落音乐播放器失败", e);
        }
    }

    /**
     * 获取音乐模组实例
     */
    public static MusicPlayerMod getInstance() {
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
     * 释放所有资源
     */
    public void shutdown() {
        if (!Objects.isNull(audioManager)) {
            audioManager.shutdown();
        }
    }
}
