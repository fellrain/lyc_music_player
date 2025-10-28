package com.rain.client.network.handler;

import com.rain.client.MusicPlayerClientMod;
import com.rain.common.network.MusicShareNotificationPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * 客户端网络处理器
 * 统一管理客户端网络通信
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class ClientNetworkHandler {

    /**
     * 注册客户端网络接收器
     */
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                MusicShareNotificationPayload.ID,
                (payload, context) -> context.client()
                        .execute(() -> MusicPlayerClientMod.getInstance()
                                .getShareManager()
                                .handleShareNotification(payload))
        );
    }
}