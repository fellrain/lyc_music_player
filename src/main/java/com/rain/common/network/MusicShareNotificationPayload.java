package com.rain.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 音乐分享通知数据包（服务端→客户端）
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public record MusicShareNotificationPayload(String shareId,String musicId, String senderName, String musicTitle, String musicArtist) implements CustomPayload {

    public static final Id<MusicShareNotificationPayload> ID = new Id<>(Identifier.of("lyc_music_player", "share_notification"));

    public static final PacketCodec<RegistryByteBuf, MusicShareNotificationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MusicShareNotificationPayload::shareId,
            PacketCodecs.STRING, MusicShareNotificationPayload::musicId,
            PacketCodecs.STRING, MusicShareNotificationPayload::senderName,
            PacketCodecs.STRING, MusicShareNotificationPayload::musicTitle,
            PacketCodecs.STRING, MusicShareNotificationPayload::musicArtist,
            MusicShareNotificationPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}