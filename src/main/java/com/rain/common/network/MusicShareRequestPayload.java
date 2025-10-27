package com.rain.common.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 音乐分享请求数据包（客户端→服务端）
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public record MusicShareRequestPayload(String musicId, String musicTitle, String musicArtist) implements CustomPayload {

    public static final Id<MusicShareRequestPayload> ID = new Id<>(Identifier.of("lyc_music_player", "share_request"));

    public static final PacketCodec<RegistryByteBuf, MusicShareRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MusicShareRequestPayload::musicId,
            PacketCodecs.STRING, MusicShareRequestPayload::musicTitle,
            PacketCodecs.STRING, MusicShareRequestPayload::musicArtist,
            MusicShareRequestPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
