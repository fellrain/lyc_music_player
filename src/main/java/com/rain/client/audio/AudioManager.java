package com.rain.client.audio;

import com.rain.client.MusicPlayerMod;
import com.rain.common.config.ModConfig;
import com.rain.client.model.MusicTrack;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音频管理器
 * <p>
 * 基于JLayer库实现MP3音频播放功能。
 * </p>
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public final class AudioManager {

    private static final String USER_AGENT = "Mozilla/5.0";

    private static final String THREAD_NAME = "MusicPlayer-Audio";

    private AdvancedPlayer player;

    private final ExecutorService audioThread;

    private Future<?> playbackTask;

    private final AtomicBoolean isPlaying = new AtomicBoolean(false);

    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private MusicTrack currentTrack;

    private Runnable onTrackEndCallback;

    private InputStream currentStream;

    private long playStartTime;

    private long pausedPosition;

    private Runnable onTrackStartCallback;

    public AudioManager() {
        this.audioThread = Executors.newSingleThreadExecutor(r -> new Thread(r, THREAD_NAME));
        MusicPlayerMod.LOGGER.info("音频管理器已使用JLayer初始化");
    }

    public void playTrack(MusicTrack track) {
        stop();
        this.currentTrack = track;
        this.pausedPosition = 0;
        playbackTask = audioThread.submit(() -> {
            try {
                URL url = new URL(track.getUrl());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(ModConfig.CONNECT_TIMEOUT);
                connection.setReadTimeout(ModConfig.READ_TIMEOUT);
                connection.setRequestProperty("User-Agent", USER_AGENT);
                currentStream = new BufferedInputStream(connection.getInputStream());
                player = new AdvancedPlayer(currentStream);
                player.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        isPlaying.set(false);
                        MinecraftClient.getInstance().execute(() -> {
                            if (!Objects.isNull(onTrackEndCallback)) {
                                onTrackEndCallback.run();
                            }
                        });
                    }
                });
                isPlaying.set(true);
                playStartTime = System.currentTimeMillis();
                MinecraftClient.getInstance().execute(() -> {
                    if (!Objects.isNull(MinecraftClient.getInstance().player)) {
                        MinecraftClient.getInstance().player.sendMessage(
                                net.minecraft.text.Text.literal("§a正在播放: §f" + track.getTitle() + " - " + track.getArtist()), true
                        );
                    }
                    // 触发播放开始回调
                    if (!Objects.isNull(onTrackStartCallback)) {
                        onTrackStartCallback.run();
                    }
                });
                player.play();
            } catch (Exception e) {
                MusicPlayerMod.LOGGER.error("播放结束", e);
                isPlaying.set(false);
                MinecraftClient.getInstance().execute(() -> {
                    if (!Objects.isNull(MinecraftClient.getInstance().player)) {
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§c播放结束..."), false);
                    }
                });
            }
        });
    }

    public void pause() {
        stop();
    }

    public void resume() {
        // 缺省的方法
    }

    public void stop() {
        isPlaying.set(false);
        isPaused.set(false);
        pausedPosition = 0;
        playStartTime = 0;
        if (!Objects.isNull(player)) {
            try {
                player.close();
            } catch (Exception e) {
                MusicPlayerMod.LOGGER.error("关闭播放器时出错", e);
            }
            player = null;
        }
        if (!Objects.isNull(currentStream)) {
            try {
                currentStream.close();
            } catch (Exception e) {
                MusicPlayerMod.LOGGER.error("关闭音频流时出错", e);
            }
            currentStream = null;
        }
        if (!Objects.isNull(playbackTask) && !playbackTask.isDone()) {
            playbackTask.cancel(true);
        }
        currentTrack = null;
        MusicPlayerMod.LOGGER.info("播放已停止");
    }

    public boolean isPlaying() {
        return isPlaying.get();
    }

    public boolean isPaused() {
        return isPaused.get();
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public long getPosition() {
        if (isPlaying.get() && playStartTime > 0) {
            return System.currentTimeMillis() - playStartTime + pausedPosition;
        }
        return pausedPosition;
    }

    public long getDuration() {
        return !Objects.isNull(currentTrack) ? currentTrack.getDuration() : 0;
    }

    public void shutdown() {
        MusicPlayerMod.LOGGER.info("正在关闭音频管理器");
        stop();
        audioThread.shutdown();
    }

    public void setOnTrackEndCallback(Runnable callback) {
        this.onTrackEndCallback = callback;
    }

    public void setOnTrackStartCallback(Runnable callback) {
        this.onTrackStartCallback = callback;
    }

    public void onTrackEnded() {
        if (!Objects.isNull(onTrackEndCallback)) {
            onTrackEndCallback.run();
        }
    }
}