package com.rain.gui;

import com.rain.audio.AudioManager;
import com.rain.gui.constants.UIConstants;
import com.rain.manager.LyricManager;
import com.rain.model.Lyric;
import com.rain.model.MusicTrack;
import com.rain.util.TimeUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Objects;

/**
 * 音乐HUD渲染器
 * <p>
 * 在游戏界面物品栏上方渲染歌词、播放时间和进度条
 * </p>
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public class MusicHudRenderer {

    private static final int HUD_OFFSET_FROM_HOTBAR = 45;
    private static final int LYRIC_LINE_SPACING = 12;
    private static final int PROGRESS_BAR_WIDTH = 182;
    private static final int PROGRESS_BAR_HEIGHT = 3;
    private static final int PROGRESS_BAR_Y_OFFSET = 25;
    private static final int TIME_TEXT_Y_OFFSET = 30;

    private static final int COLOR_LYRIC_PRIMARY = 0xFFFFFFFF;
    private static final int COLOR_LYRIC_TRANSLATION = 0xFFAAAAAA;
    private static final int COLOR_PROGRESS_BACKGROUND = 0x80000000;
    private static final int COLOR_PROGRESS_BAR = 0xFF00FF00;
    private static final int COLOR_PROGRESS_HOVER = 0xFF00FFFF;

    private final AudioManager audioManager;
    private final LyricManager lyricManager;

    private int progressBarX;
    private int progressBarY;
    private boolean isHoveringProgressBar;

    public MusicHudRenderer(AudioManager audioManager, LyricManager lyricManager) {
        this.audioManager = audioManager;
        this.lyricManager = lyricManager;
    }

    /**
     * 渲染HUD
     *
     * @param context   绘制上下文
     * @param tickDelta 帧间隔
     */
    public void render(DrawContext context, float tickDelta) {
        MusicTrack currentTrack = audioManager.getCurrentTrack();
        if (Objects.isNull(currentTrack)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth(),
                screenHeight = client.getWindow().getScaledHeight(),
                // 计算HUD基础Y坐标（物品栏上方）
                hotbarY = screenHeight - 22,
                hudBaseY = hotbarY - HUD_OFFSET_FROM_HOTBAR;
        // 渲染歌词
        renderLyrics(context, client.textRenderer, screenWidth, hudBaseY);
        // 渲染进度条
        renderProgressBar(context, screenWidth, hudBaseY);
        // 渲染播放时间
        renderPlaybackTime(context, client.textRenderer, screenWidth, hudBaseY);
    }

    /**
     * 渲染歌词
     */
    private void renderLyrics(DrawContext context, TextRenderer textRenderer, int screenWidth, int baseY) {
        Lyric lyric = lyricManager.getCurrentLyric();
        if (Objects.isNull(lyric) || !lyric.hasLyric()) {
            String noLyricText = "♬ 暂无歌词 ♬";
            int textWidth = textRenderer.getWidth(noLyricText);
            context.drawTextWithShadow(
                    textRenderer,
                    noLyricText,
                    (screenWidth - textWidth) / 2,
                    baseY,
                    COLOR_LYRIC_TRANSLATION
            );
            return;
        }
        long currentPosition = audioManager.getPosition();
        // 获取当前歌词行
        Lyric.LyricLine currentLine = lyric.getCurrentLine(currentPosition);
        if (!Objects.isNull(currentLine)) {
            String lyricText = currentLine.getText();
            int textWidth = textRenderer.getWidth(lyricText);
            context.drawTextWithShadow(
                    textRenderer,
                    lyricText,
                    (screenWidth - textWidth) / 2,
                    baseY,
                    COLOR_LYRIC_PRIMARY
            );
        }
        // 获取翻译歌词
        Lyric.LyricLine translationLine = lyric.getCurrentTranslation(currentPosition);
        if (!Objects.isNull(translationLine)) {
            String translationText = translationLine.getText();
            int textWidth = textRenderer.getWidth(translationText);
            context.drawTextWithShadow(
                    textRenderer,
                    translationText,
                    (screenWidth - textWidth) / 2,
                    baseY + LYRIC_LINE_SPACING,
                    COLOR_LYRIC_TRANSLATION
            );
        }
    }

    /**
     * 渲染进度条
     */
    private void renderProgressBar(DrawContext context, int screenWidth, int baseY) {
        progressBarX = (screenWidth - PROGRESS_BAR_WIDTH) / 2;
        progressBarY = baseY + PROGRESS_BAR_Y_OFFSET;

        // 绘制背景
        context.fill(
                progressBarX,
                progressBarY,
                progressBarX + PROGRESS_BAR_WIDTH,
                progressBarY + PROGRESS_BAR_HEIGHT,
                COLOR_PROGRESS_BACKGROUND
        );

        // 计算进度
        long currentPosition = audioManager.getPosition();
        long duration = audioManager.getDuration();

        if (duration > 0) {
            float progress = Math.min(1.0f, (float) currentPosition / duration);
            int progressWidth = (int) (PROGRESS_BAR_WIDTH * progress);

            // 绘制进度条
            int barColor = isHoveringProgressBar ? COLOR_PROGRESS_HOVER : COLOR_PROGRESS_BAR;
            context.fill(
                    progressBarX,
                    progressBarY,
                    progressBarX + progressWidth,
                    progressBarY + PROGRESS_BAR_HEIGHT,
                    barColor
            );
        }
    }

    /**
     * 渲染播放时间
     */
    private void renderPlaybackTime(DrawContext context, TextRenderer textRenderer, int screenWidth, int baseY) {
        long currentPosition = audioManager.getPosition();
        long duration = audioManager.getDuration();

        String currentTimeStr = TimeUtils.formatTime(currentPosition);
        String durationStr = TimeUtils.formatTime(duration);
        String timeText = currentTimeStr + " / " + durationStr;

        int textWidth = textRenderer.getWidth(timeText);
        context.drawTextWithShadow(
                textRenderer,
                timeText,
                (screenWidth - textWidth) / 2,
                baseY + TIME_TEXT_Y_OFFSET,
                UIConstants.COLOR_TEXT_SECONDARY
        );
    }

    /**
     * 处理鼠标点击
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 是否处理了点击事件
     */
    public boolean handleClick(double mouseX, double mouseY) {
        // 检查是否点击了进度条
        if (isMouseOverProgressBar(mouseX, mouseY)) {
            // 计算点击位置对应的播放时间
            double relativeX = mouseX - progressBarX;
            float progress = (float) (relativeX / PROGRESS_BAR_WIDTH);
            progress = Math.max(0, Math.min(1, progress));

            long duration = audioManager.getDuration();
            long targetPosition = (long) (duration * progress);

            // TODO: 实现seek功能（JLayer限制，需要重新实现）
            // audioManager.seek(targetPosition);

            return true;
        }
        return false;
    }

    /**
     * 更新鼠标悬停状态
     */
    public void updateHoverState(double mouseX, double mouseY) {
        isHoveringProgressBar = isMouseOverProgressBar(mouseX, mouseY);
    }

    /**
     * 检查鼠标是否在进度条上
     */
    private boolean isMouseOverProgressBar(double mouseX, double mouseY) {
        return mouseX >= progressBarX &&
                mouseX <= progressBarX + PROGRESS_BAR_WIDTH &&
                mouseY >= progressBarY &&
                mouseY <= progressBarY + PROGRESS_BAR_HEIGHT + 5; // 增加5像素点击容差
    }
}
