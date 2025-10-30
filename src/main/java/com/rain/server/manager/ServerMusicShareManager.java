package com.rain.server.manager;

import com.rain.common.network.MusicShareNotificationPayload;
import com.rain.common.network.MusicShareRequestPayload;
import com.rain.common.network.MusicShareResponsePayload;
import com.rain.common.util.UUIDUtil;
import com.rain.server.model.ShareInfo;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端音乐分享管理器
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class ServerMusicShareManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("ServerMusicShareManager");

    /**
     * 5分钟
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000;

    private final MinecraftServer server;

    private final Map<String, ShareInfo> shareCache = new ConcurrentHashMap<>();

    public ServerMusicShareManager(MinecraftServer server) {
        this.server = server;
    }

    /**
     * 处理分享请求
     */
    public void handleShareRequest(ServerPlayerEntity sender, MusicShareRequestPayload payload) {
        String senderName = sender.getName().getString(),
                shareId = UUIDUtil.generateShareId();
        LOGGER.info("收到来自 {} 的分享请求，歌曲: {}", senderName, payload.musicTitle());
        // 缓存分享信息
        ShareInfo shareInfo = new ShareInfo(shareId, sender.getUuidAsString(), senderName
                , payload.musicId(), payload.musicTitle(), payload.musicArtist(), System.currentTimeMillis());
        shareCache.put(shareId, shareInfo);
        // 广播分享通知给所有玩家（除了发送者）
        broadcastShareNotification(sender, shareInfo);
        // 给发送者反馈
        sender.sendMessage(Text.literal("§a已将音乐 §e《" + payload.musicTitle() + "》 §a分享给所有玩家"), false);
    }

    /**
     * 广播分享通知给所有玩家
     */
    private void broadcastShareNotification(ServerPlayerEntity sender, ShareInfo shareInfo) {
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // 不发送给自己
            if (player.getUuid().equals(sender.getUuid())) continue;
            // 发送网络数据包
            ServerPlayNetworking.send(player, new MusicShareNotificationPayload(shareInfo.getShareId(), shareInfo.getMusicId(), shareInfo.getSenderName(), shareInfo.getMusicTitle(), shareInfo.getMusicArtist()));
            // 发送可点击的聊天消息
            Text shareMessage = Text.literal("\n§e" + shareInfo.getSenderName() + " §f分享了一首歌曲 §e《" + shareInfo.getMusicTitle() + "》")
                    .append("\n§7艺术家: §f" + shareInfo.getMusicArtist())
                    .append("\n§7是否接受: ")
                    .append(Text.literal("§a[接受]").styled(style -> style
                            .withClickEvent(new ClickEvent.RunCommand(
                                    "/music share accept " + shareInfo.getShareId()))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Text.literal("§a点击接受分享")))))
                    .append(Text.literal(" "))
                    .append(Text.literal("§c[拒绝]").styled(style -> style
                            .withClickEvent(new ClickEvent.RunCommand(
                                    "/music share reject " + shareInfo.getShareId()))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("§c点击拒绝分享")))));
            player.sendMessage(shareMessage, false);
            count++;
        }
        LOGGER.info("已将分享 {} 广播给 {} 位玩家", shareInfo.getShareId(), count);
    }

    /**
     * 处理分享响应
     */
    public void handleShareResponse(ServerPlayerEntity responder, MusicShareResponsePayload payload) {
        String shareId = payload.shareId();
        boolean accepted = payload.accepted();
        // 懒清除：检查并清除过期缓存
        cleanExpiredCache();
        ShareInfo shareInfo = shareCache.get(shareId);
        if (shareInfo == null) {
            responder.sendMessage(Text.literal("§c该分享已过期或不存在"), false);
            return;
        }
        String responderName = responder.getName().getString();
        LOGGER.info("{} {} 分享 {}", responderName, accepted ? "接受了" : "拒绝了", shareId);
        if (accepted) {
            // 接受分享：发送网络包通知客户端播放音乐
            responder.sendMessage(Text.literal("§a已接受 §e" + shareInfo.getSenderName() + " §a的分享，正在加载音乐..."), false);
            // 通知分享者
            ServerPlayerEntity sender = server.getPlayerManager().getPlayer(UUID.fromString(shareInfo.getSenderUuid()));
            if (sender != null) {
                sender.sendMessage(Text.literal("§e" + responderName + " §a接受了你分享的音乐 §e《" + shareInfo.getMusicTitle() + "》"), false);
            }
        } else {
            // 拒绝分享
            responder.sendMessage(Text.literal("§7已拒绝 §e" + shareInfo.getSenderName() + " §7的分享"), false);
            // 通知分享者
            ServerPlayerEntity sender = server.getPlayerManager().getPlayer(UUID.fromString(shareInfo.getSenderUuid()));
            if (sender != null) {
                sender.sendMessage(Text.literal("§e" + responderName + " §7拒绝了你分享的音乐"), false);
            }
        }
        // 清除缓存
        shareCache.remove(shareId);
    }

    /**
     * 懒清除过期缓存
     */
    private void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        shareCache.entrySet().removeIf(entry -> {
            boolean expired = now - entry.getValue().getTimestamp() > CACHE_EXPIRE_TIME;
            if (expired) LOGGER.debug("清除过期分享缓存: {}", entry.getKey());
            return expired;
        });
    }
}