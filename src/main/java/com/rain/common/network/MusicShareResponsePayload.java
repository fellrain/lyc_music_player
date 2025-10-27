package com.rain.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 音乐分享响应数据包（客户端→服务端）
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public record MusicShareResponsePayload(String shareId, boolean accepted) implements CustomPayload {

    public static final Id<MusicShareResponsePayload> ID = new Id<>(Identifier.of("lyc_music_player", "share_response"));

    public static final PacketCodec<RegistryByteBuf, MusicShareResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MusicShareResponsePayload::shareId,
            PacketCodecs.BOOLEAN, MusicShareResponsePayload::accepted,
            MusicShareResponsePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
