package com.rain;

import com.rain.common.network.MusicShareNotificationPayload;
import com.rain.common.network.MusicShareRequestPayload;
import com.rain.common.network.MusicShareResponsePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用模组初始化类
 * 用于注册客户端和服务端都需要的内容
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class CommonModInitializer implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lycMusicPlayer_Common");

    @Override
    public void onInitialize() {
        LOGGER.info("注册小落音乐播放器通用网络包");
        // 注册客户端到服务端的数据包
        PayloadTypeRegistry.playC2S().register(MusicShareRequestPayload.ID, MusicShareRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MusicShareResponsePayload.ID, MusicShareResponsePayload.CODEC);
        // 注册服务端到客户端的数据包
        PayloadTypeRegistry.playS2C().register(MusicShareNotificationPayload.ID, MusicShareNotificationPayload.CODEC);
        LOGGER.info("小落音乐播放器通用网络包注册完成");
    }
}
