package com.rain.server.network.handler;

import com.rain.common.network.MusicShareRequestPayload;
import com.rain.common.network.MusicShareResponsePayload;
import com.rain.server.MusicPlayerServerMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * 服务端网络处理器
 * 统一管理服务端网络通信
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class ServerNetworkHandler {

    /**
     * 注册服务端网络接收器
     */
    public static void registerReceivers() {
        // 处理分享请求
        ServerPlayNetworking.registerGlobalReceiver(
                MusicShareRequestPayload.ID,
                (payload, context) -> context.server().execute(() ->
                        MusicPlayerServerMod.getShareManager()
                                .handleShareRequest(context.player(), payload)
                )
        );

        // 处理分享响应
        ServerPlayNetworking.registerGlobalReceiver(
                MusicShareResponsePayload.ID,
                (payload, context) -> context.server().execute(() ->
                        MusicPlayerServerMod.getShareManager()
                                .handleShareResponse(context.player(), payload)
                )
        );
    }
}
