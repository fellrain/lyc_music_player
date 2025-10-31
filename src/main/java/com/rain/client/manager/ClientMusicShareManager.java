package com.rain.client.manager;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.model.MusicTrack;
import com.rain.common.network.MusicShareNotificationPayload;
import com.rain.common.network.MusicShareRequestPayload;
import com.rain.common.network.MusicShareResponsePayload;
import com.rain.common.util.StrUtil;
import com.rain.server.model.ClientCacheShareInfo;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 音乐分享管理器（客户端）
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class ClientMusicShareManager {

    private final Map<String, ClientCacheShareInfo> pendingShares = new ConcurrentHashMap<>(64);

    /**
     * 分享当前播放的音乐给所有玩家
     */
    public void shareMusic(MusicTrack track) {
        shareMusic(track, "");
    }

    public void shareMusic(MusicTrack track, String playerName) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || track == null) {
            return;
        }
        MusicPlayerClientMod.LOGGER.info("分享音乐: {}", track.getTitle());
        ClientPlayNetworking.send(new MusicShareRequestPayload(
                track.getId(),
                track.getTitle(),
                track.getArtist(),
                playerName
        ));
        player.sendMessage(Text.literal("§a正在分享音乐 §e《" + track.getTitle() + "》 §a给" + (StrUtil.isNotEmpty(playerName) ? playerName : "所有玩家") + "..."), false);
    }

    /**
     * 处理来自服务端的分享通知
     */
    public void handleShareNotification(MusicShareNotificationPayload payload) {
        pendingShares.put(payload.shareId(), new ClientCacheShareInfo(payload.musicId(), payload.musicTitle()));
        MusicPlayerClientMod.LOGGER.info("收到来自 {} 的分享: {}", payload.senderName(), payload.musicTitle());
    }

    /**
     * 接受分享
     */
    public void acceptShare(String shareId) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (Objects.isNull(player)) return;
        ClientCacheShareInfo info = pendingShares.remove(shareId);
        if (Objects.isNull(info)) {
            player.sendMessage(Text.literal("§c未找到该分享"), false);
            return;
        }
        ClientPlayNetworking.send(new MusicShareResponsePayload(shareId, true));
        player.sendMessage(Text.literal("§a已接受分享，正在搜索并播放音乐..."), false);
        searchAndPlay(info);
    }

    /**
     * 拒绝分享
     */
    public void rejectShare(String shareId) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (pendingShares.remove(shareId) == null) {
            player.sendMessage(Text.literal("§c未找到该分享"), false);
            return;
        }
        ClientPlayNetworking.send(new MusicShareResponsePayload(shareId, false));
        player.sendMessage(Text.literal("§7已拒绝分享"), false);
    }

    /**
     * 搜索并播放歌曲
     *
     * @param info 歌曲ID | 歌曲名称
     */
    private void searchAndPlay(ClientCacheShareInfo info) {
        MusicPlayerClientMod mod = MusicPlayerClientMod.getInstance();
        Consumer<String> cs = msg -> MinecraftClient.getInstance().player.sendMessage(Text.literal(msg), false);
        mod.getApiClient().searchMusic(info.musicTitle()).thenAccept(result ->
                MinecraftClient.getInstance().execute(() -> {
                    if (result.isEmpty()) {
                        cs.accept("§c未找到歌曲: 《" + info.musicTitle() + "》");
                        return;
                    }
                    MusicTrack track = result.tracks().stream()
                            .filter(t -> t.getId().equals(info.musicId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.isNull(track)) {
                        cs.accept("§c未找到歌曲: 《" + info.musicTitle() + "》");
                        return;
                    }
                    mod.getAudioManager().playTrack(track);
                    cs.accept("§a开始播放: §f" + track.getTitle() + " - " + track.getArtist());
                })).exceptionally(throwable -> {
            MusicPlayerClientMod.LOGGER.error("搜索歌曲失败", throwable);
            MinecraftClient.getInstance().execute(() -> cs.accept("§c搜索失败: " + throwable.getMessage()));
            return null;
        });
    }
}
