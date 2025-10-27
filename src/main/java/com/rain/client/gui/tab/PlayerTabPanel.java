package com.rain.client.gui.tab;

import com.rain.client.audio.AudioManager;
import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.MusicManager;
import com.rain.client.manager.ClientMusicShareManager;
import com.rain.client.manager.PlaybackMode;
import com.rain.client.model.MusicTrack;
import com.rain.client.MusicPlayerMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * 播放器标签页面板
 * <p>
 * 显示当前播放的音乐信息和播放控制按钮。
 * </p>
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class PlayerTabPanel implements TabPanel {

    private final AudioManager audioManager;
    private final MusicManager musicManager;
    private final ClientMusicShareManager shareManager;
    private TabPanelContext context;
    private TextRenderer textRenderer;

    /**
     * 构造播放器标签页
     */
    public PlayerTabPanel(AudioManager audioManager, MusicManager musicManager) {
        this.audioManager = audioManager;
        this.musicManager = musicManager;
        this.shareManager = MusicPlayerMod.getInstance().getShareManager();
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
        initPlayerControls();
    }

    /**
     * 初始化播放控制按钮
     */
    private void initPlayerControls() {
        int controlsY = context.getHeight() - 40,
                centerX = context.getWidth() / 2;
        // 上一首按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("上一首"),
                button -> musicManager.playPrevious()
        ).dimensions(centerX - 120, controlsY, 50, UIConstants.BUTTON_HEIGHT).build());
        // 播放/暂停按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal(audioManager.isPlaying() ? "暂停" : "播放"),
                button -> togglePlayPause()
        ).dimensions(centerX - 60, controlsY, 50, UIConstants.BUTTON_HEIGHT).build());
        // 下一首按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("下一首"),
                button -> musicManager.playNext()
        ).dimensions(centerX, controlsY, 50, UIConstants.BUTTON_HEIGHT).build());
        // 停止按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("停止"),
                button -> audioManager.stop()
        ).dimensions(centerX + 60, controlsY, 50, UIConstants.BUTTON_HEIGHT).build());
        // 播放模式按钮
        PlaybackMode mode = musicManager.getPlaybackMode();
        context.addWidget(ButtonWidget.builder(
                Text.literal(mode.getDisplayName()),
                button -> {
                    musicManager.cyclePlaybackMode();
                    context.requestReinit();
                }
        ).dimensions(centerX - 60, controlsY + 25, 120, UIConstants.BUTTON_HEIGHT).build());
        // 分享按钮（在播放模式按钮下方）
        context.addWidget(ButtonWidget.builder(
                Text.literal("分享给所有人"),
                button -> shareCurrentMusic()
        ).dimensions(centerX - 60, controlsY + 50, 120, UIConstants.BUTTON_HEIGHT).build());
    }

    /**
     * 切换播放/暂停状态
     */
    private void togglePlayPause() {
        if (audioManager.isPlaying()) {
            audioManager.pause();
        } else if (audioManager.isPaused()) {
            audioManager.resume();
        } else {
            if (!musicManager.isPlaylistEmpty()) {
                musicManager.playTrackAt(0);
            }
        }
        context.requestReinit();
    }

    /**
     * 分享当前播放的音乐
     */
    private void shareCurrentMusic() {
        MusicTrack currentTrack = audioManager.getCurrentTrack();
        if (Objects.isNull(currentTrack)) {
            // 没有正在播放的音乐，不做任何事
            return;
        }
        shareManager.shareMusic(currentTrack);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int centerX = context.getWidth() / 2, startY = 60;
        // 绘制标题
        RenderHelper.drawCenteredColoredText(
                drawContext, textRenderer,
                "小落音乐播放器",
                centerX, startY,
                UIConstants.COLOR_PRIMARY
        );
        MusicTrack currentTrack = audioManager.getCurrentTrack();
        if (!Objects.isNull(currentTrack)) {
            renderCurrentTrackInfo(drawContext, centerX, startY, currentTrack);
            renderPlaybackStatus(drawContext, centerX, startY);
        } else {
            renderNoMusicPlaying(drawContext, centerX, startY);
        }
        renderPlaybackMode(drawContext, centerX);
    }

    /**
     * 渲染当前音轨信息
     */
    private void renderCurrentTrackInfo(DrawContext drawContext, int centerX, int startY, MusicTrack track) {
        RenderHelper.drawCenteredColoredText(
                drawContext, textRenderer,
                "正在播放",
                centerX, startY + 30,
                UIConstants.COLOR_WARNING
        );
        RenderHelper.drawCenteredPrimaryText(
                drawContext, textRenderer,
                track.getTitle(),
                centerX, startY + 50
        );
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                track.getArtist(),
                centerX, startY + 65
        );
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "专辑: " + track.getAlbum(),
                centerX, startY + 80
        );
    }

    /**
     * 渲染播放状态
     */
    private void renderPlaybackStatus(DrawContext drawContext, int centerX, int startY) {
        String status;
        int statusColor;
        if (audioManager.isPlaying()) {
            status = "播放中";
            statusColor = UIConstants.COLOR_PRIMARY;
        } else if (audioManager.isPaused()) {
            status = "已暂停";
            statusColor = UIConstants.COLOR_WARNING;
        } else {
            status = "已停止";
            statusColor = UIConstants.COLOR_DANGER;
        }
        RenderHelper.drawCenteredColoredText(
                drawContext, textRenderer,
                status,
                centerX, startY + 123,
                statusColor
        );
    }

    /**
     * 渲染无音乐播放提示
     */
    private void renderNoMusicPlaying(DrawContext drawContext, int centerX, int startY) {
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "没有正在播放的音乐",
                centerX, startY + 50
        );
    }

    /**
     * 渲染播放模式信息
     */
    private void renderPlaybackMode(DrawContext drawContext, int centerX) {
        PlaybackMode mode = musicManager.getPlaybackMode();
        RenderHelper.drawCenteredPrimaryText(
                drawContext, textRenderer,
                "模式: " + mode.getDisplayName(),
                centerX, context.getHeight() - 75
        );
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        return false;
    }
}
