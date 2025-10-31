package com.rain.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.rain.client.MusicPlayerClientMod;
import com.rain.client.audio.AudioManager;
import com.rain.client.manager.MusicManager;
import com.rain.client.manager.PlaybackMode;
import com.rain.client.model.MusicTrack;
import com.rain.client.network.MusicAPIClient;
import com.rain.common.util.CollUtil;
import com.rain.common.util.TimeUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * 音乐播放器命令注册器
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class MusicCommands {

    private static final int SEARCH_CACHE_CAPACITY = 256;
    private static final String COMMAND_SUCCESS_COLOR = "§a";
    private static final String COMMAND_ERROR_COLOR = "§c";
    private static final String COMMAND_INFO_COLOR = "§e";
    private static final String COMMAND_HIGHLIGHT_COLOR = "§f";
    private static final String COMMAND_MUTED_COLOR = "§7";

    private MusicCommands() {
    }

    private static final Map<Integer, MusicTrack> searchCache = new HashMap<>(SEARCH_CACHE_CAPACITY);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("music")
                .then(literal("search")
                        .then(argument("query", StringArgumentType.greedyString())
                                .executes(MusicCommands::searchMusic)))
                .then(literal("play")
                        .then(argument("id", StringArgumentType.string())
                                .executes(MusicCommands::playMusic)))
                .then(literal("stop")
                        .executes(MusicCommands::stopMusic))
                .then(literal("pause")
                        .executes(MusicCommands::pauseMusic))
                .then(literal("resume")
                        .executes(MusicCommands::resumeMusic))
                .then(literal("nowplaying")
                        .executes(MusicCommands::nowPlaying))
                .then(literal("list")
                        .executes(MusicCommands::listSearchResults))
                .then(literal("playlist")
                        .then(literal("add")
                                .then(argument("id", StringArgumentType.string())
                                        .executes(MusicCommands::addToPlaylist)))
                        .then(literal("remove")
                                .then(argument("index", IntegerArgumentType.integer(1))
                                        .executes(MusicCommands::removeFromPlaylist)))
                        .then(literal("clear")
                                .executes(MusicCommands::clearPlaylist))
                        .then(literal("show")
                                .executes(MusicCommands::showPlaylist))
                        .then(literal("shuffle")
                                .executes(MusicCommands::shufflePlaylist)))
                .then(literal("next")
                        .executes(MusicCommands::playNext))
                .then(literal("previous")
                        .executes(MusicCommands::playPrevious))
                .then(literal("mode")
                        .executes(MusicCommands::cyclePlaybackMode))
                .then(literal("share")
                        .then(literal("accept")
                                .then(argument("shareId", StringArgumentType.string())
                                        .executes(MusicCommands::acceptShare)))
                        .then(literal("reject")
                                .then(argument("shareId", StringArgumentType.string())
                                        .executes(MusicCommands::rejectShare)))
                        .then(literal("to")
                                .then(argument("playerName", StringArgumentType.string())
                                        .executes(MusicCommands::shareMusic))))
                .then(literal("help")
                        .executes(MusicCommands::showHelp))
        );
    }

    private static int shareMusic(CommandContext<FabricClientCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        FabricClientCommandSource source = context.getSource();
        MusicTrack currentTrack = audioManager.getCurrentTrack();
        if (Objects.isNull(currentTrack)) {
            sendError(source, "当前没有正在播放的音乐");
            return 0;
        }
        MusicPlayerClientMod.getInstance().getShareManager().shareMusic(currentTrack, playerName);
        return 1;
    }

    private static int searchMusic(CommandContext<FabricClientCommandSource> context) {
        String query = StringArgumentType.getString(context, "query");
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = MinecraftClient.getInstance();
        sendFeedback(source, COMMAND_INFO_COLOR + "搜索音乐: " + COMMAND_HIGHLIGHT_COLOR + query + COMMAND_INFO_COLOR + " 中...");
        MusicAPIClient apiClient = MusicPlayerClientMod.getInstance().getApiClient();
        apiClient.searchMusic(query).thenAccept(result ->
                client.execute(() -> {
                    if (result.isEmpty()) {
                        sendError(source, "未找到音乐: " + query);
                        return;
                    }
                    searchCache.clear();
                    sendFeedback(source, COMMAND_SUCCESS_COLOR + "§l=== 搜索结果: " + COMMAND_HIGHLIGHT_COLOR + query + " " + COMMAND_SUCCESS_COLOR + "§l===");
                    sendFeedback(source, COMMAND_INFO_COLOR + "找到 " + result.totalResults() + " 条结果");
                    sendFeedback(source, "");
                    List<MusicTrack> tracks = result.tracks();
                    for (int i = 0; i < tracks.size(); i++) {
                        MusicTrack track = tracks.get(i);
                        int index = i + 1;
                        searchCache.put(index, track);
                        Text trackText = Text.literal("§e" + index + ". §f" + track.getTitle())
                                .append(Text.literal(" §7by §f" + track.getArtist()))
                                .append(Text.literal(" §8[" + track.getFormattedDuration() + "]"))
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent.RunCommand("/music play " + index))
                                        .withHoverEvent(new HoverEvent.ShowText(
                                                Text.literal("§a点击播放\n§7专辑: §f" + track.getAlbum())
                                        )));
                        source.sendFeedback(trackText);
                    }
                    sendFeedback(source, "");
                    sendFeedback(source, COMMAND_MUTED_COLOR + "点击歌曲播放,或使用 " + COMMAND_HIGHLIGHT_COLOR + "/music play <编号>");
                })).exceptionally(throwable -> {
            MusicPlayerClientMod.LOGGER.info("报错了", throwable);
            client.execute(() -> source.sendError(Text.literal("§c搜索失败: " + throwable.getMessage())));
            return null;
        });
        return 1;
    }

    private static int playMusic(CommandContext<FabricClientCommandSource> context) {
        String id = StringArgumentType.getString(context, "id");
        FabricClientCommandSource source = context.getSource();
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        try {
            int index = Integer.parseInt(id);
            if (searchCache.containsKey(index)) {
                MusicTrack track = searchCache.get(index);
                sendFeedback(source, COMMAND_SUCCESS_COLOR + "加载中: " + COMMAND_INFO_COLOR + track.getTitle() + COMMAND_SUCCESS_COLOR + "...");
                audioManager.playTrack(track);
                return 1;
            } else {
                sendError(source, "无效的编号. 请先使用 /music search 搜索音乐");
                return 0;
            }
        } catch (NumberFormatException e) {
            sendError(source, "参数不正确!");
        }
        return 1;
    }

    private static int stopMusic(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        if (!audioManager.isPlaying() && !audioManager.isPaused()) {
            sendError(source, "当前没有正在播放的音乐");
            return 0;
        }
        audioManager.stop();
        sendFeedback(source, COMMAND_SUCCESS_COLOR + "播放已停止");
        return 1;
    }

    private static int pauseMusic(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        if (!audioManager.isPlaying()) {
            sendError(source, "当前没有正在播放的音乐");
            return 0;
        }
        audioManager.pause();
        sendFeedback(source, COMMAND_SUCCESS_COLOR + "播放已暂停");
        return 1;
    }

    private static int resumeMusic(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        if (!audioManager.isPaused()) {
            sendError(source, "当前没有暂停播放的音乐");
            return 0;
        }
        audioManager.resume();
        sendFeedback(source, COMMAND_SUCCESS_COLOR + "播放已恢复");
        return 1;
    }

    /**
     * 显示当前播放信息
     */
    private static int nowPlaying(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        AudioManager audioManager = MusicPlayerClientMod.getInstance().getAudioManager();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        MusicTrack currentTrack = audioManager.getCurrentTrack();
        if (Objects.isNull(currentTrack)) {
            sendError(source, "当前没有正在播放的音乐");
            return 0;
        }
        long position = audioManager.getPosition(),
                duration = audioManager.getDuration();
        String status = audioManager.isPlaying() ? COMMAND_SUCCESS_COLOR + "播放中" : COMMAND_INFO_COLOR + "已暂停";
        PlaybackMode mode = musicManager.getPlaybackMode();
        sendFeedback(source, COMMAND_SUCCESS_COLOR + "§l=== 当前播放 ===");
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "标题: " + COMMAND_INFO_COLOR + currentTrack.getTitle());
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "艺术家: " + COMMAND_INFO_COLOR + currentTrack.getArtist());
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "专辑: " + COMMAND_INFO_COLOR + currentTrack.getAlbum());
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "状态: " + status);
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "进度: " + COMMAND_INFO_COLOR +
                TimeUtils.formatTime(position) + " " + COMMAND_MUTED_COLOR + "/ " + COMMAND_INFO_COLOR + TimeUtils.formatTime(duration));
        sendFeedback(source, COMMAND_HIGHLIGHT_COLOR + "模式: " + COMMAND_INFO_COLOR + mode.getDisplayName());
        return 1;
    }

    private static int listSearchResults(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        if (CollUtil.isEmpty(searchCache)) {
            source.sendError(Text.literal("§c没有缓存的搜索结果. 请先使用 /music search 搜索音乐"));
            return 0;
        }
        source.sendFeedback(Text.literal("§a§l=== 缓存的搜索结果 ==="));
        searchCache.forEach((index, track) -> {
            Text trackText = Text.literal("§e" + index + ". §f" + track.getTitle())
                    .append(Text.literal(" §7by §f" + track.getArtist()))
                    .styled(style -> style
                            .withClickEvent(new ClickEvent.RunCommand(
                                    "/music play " + index))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Text.literal("§a点击播放"))));
            source.sendFeedback(trackText);
        });
        return 1;
    }

    private static int addToPlaylist(CommandContext<FabricClientCommandSource> context) {
        String id = StringArgumentType.getString(context, "id");
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        try {
            int index = Integer.parseInt(id);
            if (searchCache.containsKey(index)) {
                MusicTrack track = searchCache.get(index);
                if (musicManager.addToPlaylist(track)) {
                    source.sendFeedback(Text.literal("§a已添加到播放列表: §f" + track.getTitle()));
                } else {
                    source.sendError(Text.literal("§c播放列表已满"));
                }
                return 1;
            }
        } catch (NumberFormatException ignored) {
        }
        source.sendError(Text.literal("§c无效的音轨编号"));
        return 0;
    }

    private static int removeFromPlaylist(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index") - 1;
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        if (musicManager.removeFromPlaylist(index)) {
            source.sendFeedback(Text.literal("§a已从播放列表移除音轨"));
        } else {
            source.sendError(Text.literal("§c无效的播放列表索引"));
        }
        return 1;
    }

    private static int clearPlaylist(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        musicManager.clearPlaylist();
        source.sendFeedback(Text.literal("§a播放列表已清空"));
        return 1;
    }

    private static int showPlaylist(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        if (musicManager.isPlaylistEmpty()) {
            source.sendError(Text.literal("§c播放列表为空"));
            return 0;
        }
        List<MusicTrack> playlist = musicManager.getPlaylist();
        int currentIndex = musicManager.getCurrentIndex();
        source.sendFeedback(Text.literal("§a§l=== 播放列表 (" + playlist.size() + " 首歌曲) ==="));
        for (int i = 0; i < playlist.size(); i++) {
            MusicTrack track = playlist.get(i);
            String prefix = (i == currentIndex) ? "§e▶ " : "§7" + (i + 1) + ". ";
            Text trackText = Text.literal(prefix + "§f" + track.getTitle())
                    .append(Text.literal(" §7by §f" + track.getArtist()));
            source.sendFeedback(trackText);
        }
        return 1;
    }

    private static int shufflePlaylist(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        if (musicManager.isPlaylistEmpty()) {
            source.sendError(Text.literal("§c播放列表为空"));
            return 0;
        }
        musicManager.shuffle();
        source.sendFeedback(Text.literal("§a播放列表已随机排序"));
        return 1;
    }

    private static int playNext(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        if (musicManager.isPlaylistEmpty()) {
            source.sendError(Text.literal("§c播放列表为空"));
            return 0;
        }
        musicManager.playNext();
        source.sendFeedback(Text.literal("§a正在播放下一首歌曲"));
        return 1;
    }

    private static int playPrevious(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        if (musicManager.isPlaylistEmpty()) {
            source.sendError(Text.literal("§c播放列表为空"));
            return 0;
        }
        musicManager.playPrevious();
        source.sendFeedback(Text.literal("§a正在播放上一首歌曲"));
        return 1;
    }

    private static int cyclePlaybackMode(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MusicManager musicManager = MusicPlayerClientMod.getInstance().getMusicManager();
        PlaybackMode newMode = musicManager.cyclePlaybackMode();
        source.sendFeedback(Text.literal("§a播放模式: §f" + newMode.getDisplayName())
                .append(Text.literal(" " + newMode.getDescription())));
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        source.sendFeedback(Text.literal("§a§l=== 音乐播放器命令帮助 ==="));
        source.sendFeedback(Text.literal("§e/music search <关键词> §7- 搜索音乐"));
        source.sendFeedback(Text.literal("§e/music play <编号> §7- 播放指定歌曲"));
        source.sendFeedback(Text.literal("§e/music stop §7- 停止播放"));
        source.sendFeedback(Text.literal("§e/music pause §7- 暂停播放"));
        source.sendFeedback(Text.literal("§e/music resume §7- 恢复播放"));
        source.sendFeedback(Text.literal("§e/music next §7- 播放下一首"));
        source.sendFeedback(Text.literal("§e/music previous §7- 播放上一首"));
        source.sendFeedback(Text.literal("§e/music mode §7- 切换播放模式"));
        source.sendFeedback(Text.literal("§e/music nowplaying §7- 显示当前播放信息"));
        source.sendFeedback(Text.literal("§e/music list §7- 显示缓存的搜索结果"));
        source.sendFeedback(Text.literal(""));
        source.sendFeedback(Text.literal("§a§l播放列表命令:"));
        source.sendFeedback(Text.literal("§e/music playlist add <编号> §7- 添加到播放列表"));
        source.sendFeedback(Text.literal("§e/music playlist remove <索引> §7- 从播放列表移除"));
        source.sendFeedback(Text.literal("§e/music playlist clear §7- 清空播放列表"));
        source.sendFeedback(Text.literal("§e/music playlist show §7- 显示播放列表"));
        source.sendFeedback(Text.literal("§e/music playlist shuffle §7- 随机播放播放列表"));
        source.sendFeedback(Text.literal(""));
        source.sendFeedback(Text.literal("§a§l分享命令:"));
        source.sendFeedback(Text.literal("§e/music share to <玩家名> §7- 分享当前播放音乐给指定玩家"));
        return 1;
    }

    /**
     * 接受音乐分享
     */
    private static int acceptShare(CommandContext<FabricClientCommandSource> context) {
        String shareId = StringArgumentType.getString(context, "shareId");
        MusicPlayerClientMod.getInstance().getShareManager().acceptShare(shareId);
        return 1;
    }

    /**
     * 拒绝音乐分享
     */
    private static int rejectShare(CommandContext<FabricClientCommandSource> context) {
        String shareId = StringArgumentType.getString(context, "shareId");
        MusicPlayerClientMod.getInstance().getShareManager().rejectShare(shareId);
        return 1;
    }

    /**
     * 发送消息给玩家
     */
    private static void sendFeedback(FabricClientCommandSource source, String message) {
        MinecraftClient.getInstance().execute(() -> source.sendFeedback(Text.literal(message)));
    }

    /**
     * 发送错误消息给玩家
     */
    private static void sendError(FabricClientCommandSource source, String message) {
        MinecraftClient.getInstance().execute(() -> source.sendError(Text.literal(COMMAND_ERROR_COLOR + message)));
    }
}