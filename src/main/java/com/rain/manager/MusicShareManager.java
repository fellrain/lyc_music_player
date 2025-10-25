package com.rain.manager;

import com.rain.MusicPlayerMod;
import com.rain.model.MusicTrack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 音乐分享管理器（纯客户端实现）
 * 通过聊天消息传递分享信息
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public class MusicShareManager {

    /**
     * 待处理的分享请求：发送者名 -> 歌曲名
     */
    private final Map<String, String> pendingShares = new ConcurrentHashMap<>();

    /**
     * 分享音乐给指定玩家
     *
     * @param targetPlayerName 目标玩家名（为空时广播给所有人）
     * @param track            要分享的歌曲
     */
    public void shareMusic(String targetPlayerName, MusicTrack track) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (Objects.isNull(player)) {
            MusicPlayerMod.LOGGER.warn("无法分享音乐：玩家为空");
            return;
        }
        if (Objects.isNull(track)) {
            player.sendMessage(Text.literal("§c无法分享：歌曲信息为空"), false);
            return;
        }
        MusicPlayerMod.LOGGER.info("分享音乐: {}", track.getTitle());
        player.networkHandler.sendChatMessage("§f分享了歌曲: §e《" + track.getTitle() + "》");
    }

    /**
     * 接受音乐分享
     *
     * @param senderName 发送者名称
     */
    public void acceptShare(String senderName) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (Objects.isNull(player)) {
            return;
        }
        String songName = pendingShares.get(senderName);
        if (Objects.isNull(songName)) {
            player.sendMessage(Text.literal("§c未找到来自 " + senderName + " 的分享"), false);
            return;
        }
        player.sendMessage(
                Text.literal("§a已接受 §e" + senderName + " §a的分享，正在搜索: §f《" + songName + "》"),
                false
        );
        // 搜索并播放歌曲
        searchAndPlay(songName);
        MusicPlayerMod.LOGGER.info("接受音乐分享: {}", songName);
    }

    /**
     * 拒绝音乐分享
     *
     * @param senderName 发送者名称
     */
    public void rejectShare(String senderName) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (Objects.isNull(player)) {
            return;
        }
        String songName = pendingShares.get(senderName);
        if (Objects.isNull(songName)) {
            player.sendMessage(Text.literal("§c未找到来自 " + senderName + " 的分享"), false);
            return;
        }
        player.sendMessage(
                Text.literal("§7已拒绝 §e" + senderName + " §7的分享"),
                false
        );
        MusicPlayerMod.LOGGER.info("拒绝音乐分享来自: {}", senderName);
    }

    /**
     * 保存待处理的分享请求
     *
     * @param senderName 发送者名称
     * @param songName   歌曲名
     */
    public void savePendingShare(String senderName, String songName) {
        pendingShares.put(senderName, songName);
    }

    /**
     * 搜索并播放歌曲
     *
     * @param songName 歌曲名
     */
    private void searchAndPlay(String songName) {
        MusicPlayerMod mod = MusicPlayerMod.getInstance();

        mod.getApiClient().searchMusic(songName).thenAccept(result -> {
            MinecraftClient.getInstance().execute(() -> {
                if (result.isEmpty() || result.tracks().isEmpty()) {
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("§c未找到歌曲: 《" + songName + "》"),
                            false
                    );
                    return;
                }

                // 播放第一首搜索结果
                MusicTrack track = result.tracks().get(0);
                mod.getAudioManager().playTrack(track);

                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§a开始播放: §f" + track.getTitle() + " - " + track.getArtist()),
                        false
                );
            });
        }).exceptionally(throwable -> {
            MusicPlayerMod.LOGGER.error("搜索歌曲失败", throwable);
            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§c搜索失败: " + throwable.getMessage()),
                        false
                );
            });
            return null;
        });
    }
}
