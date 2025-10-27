package com.rain.server;

import com.rain.config.ModConfig;
import com.rain.server.manager.ServerMusicShareManager;
import com.rain.server.network.handler.ServerNetworkHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 音乐播放器服务端模组入口
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class MusicPlayerServerMod implements DedicatedServerModInitializer {


    public static final Logger LOGGER = LoggerFactory.getLogger(ModConfig.MOD_ID + "_Server");

    /**
     * 服务端实例
     */
    private static MinecraftServer serverInstance;

    /**
     * 分享管理器
     */
    private static ServerMusicShareManager shareManager;

    @Override
    public void onInitializeServer() {
        LOGGER.info("初始化小落音乐播放器服务端");
        // 注册网络通信
        ServerNetworkHandler.registerReceivers();

        // 监听服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            shareManager = new ServerMusicShareManager(server);
            LOGGER.info("小落音乐播放器服务端启动完成");
        });

        LOGGER.info("小落音乐播放器服务端初始化完成");
    }

    /**
     * 获取服务器实例
     */
    public static MinecraftServer getServerInstance() {
        return serverInstance;
    }

    /**
     * 获取分享管理器
     */
    public static ServerMusicShareManager getShareManager() {
        return shareManager;
    }
}
