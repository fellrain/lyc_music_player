package com.rain.client.gui.tab;

import com.rain.client.MusicPlayerMod;
import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.FavoriteManager;
import com.rain.client.manager.MusicManager;
import com.rain.client.manager.ClientMusicShareManager;
import com.rain.client.model.MusicTrack;
import com.rain.util.CollUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * 播放列表标签页面板
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.6
 */
public class PlaylistTabPanel implements TabPanel {

    private final MusicManager musicManager;

    private final FavoriteManager favoriteManager;

    private final ClientMusicShareManager shareManager;

    private TabPanelContext context;

    private TextRenderer textRenderer;

    private int playlistScrollOffset = 0;

    /**
     * 构造播放列表标签页
     */
    public PlaylistTabPanel(MusicManager musicManager) {
        this.musicManager = musicManager;
        this.favoriteManager = MusicPlayerMod.getInstance().getFavoriteManager();
        this.shareManager = MusicPlayerMod.getInstance().getShareManager();
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        List<MusicTrack> playlist = musicManager.getPlaylist();
        if (CollUtil.isEmpty(playlist)) {
            renderEmptyState(drawContext);
            return;
        }
        renderPlaylistHeader(drawContext, playlist.size());
        renderPlaylistItems(drawContext, playlist, mouseX, mouseY);
    }

    /**
     * 渲染空状态提示
     */
    private void renderEmptyState(DrawContext drawContext) {
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "播放列表为空",
                context.getWidth() / 2,
                60
        );
    }

    /**
     * 渲染播放列表头部
     */
    private void renderPlaylistHeader(DrawContext drawContext, int playlistSize) {
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                "播放列表 (" + playlistSize + " 首)",
                UIConstants.PADDING, 40,
                UIConstants.COLOR_PRIMARY
        );
    }

    /**
     * 渲染播放列表项目
     */
    private void renderPlaylistItems(DrawContext drawContext, List<MusicTrack> playlist, int mouseX, int mouseY) {
        int listY = 60,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT,
                currentIndex = musicManager.getCurrentIndex();
        for (int i = playlistScrollOffset; i < playlist.size() && i < playlistScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - playlistScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            MusicTrack track = playlist.get(i);
            boolean isCurrent = i == currentIndex;
            renderPlaylistItem(drawContext, track, itemY, mouseX, mouseY, isCurrent, i);
        }
    }

    /**
     * 渲染单个播放列表项
     */
    private void renderPlaylistItem(DrawContext drawContext, MusicTrack track, int itemY,
                                    int mouseX, int mouseY, boolean isCurrent, int index) {
        boolean isHovered = RenderHelper.isMouseOver(
                mouseX, mouseY,
                UIConstants.PADDING, itemY,
                context.getWidth() - UIConstants.PADDING, itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2
        );
        // 绘制背景
        RenderHelper.drawListItemBackground(
                drawContext,
                UIConstants.PADDING, itemY,
                context.getWidth() - UIConstants.PADDING, itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2,
                isHovered, isCurrent
        );
        // 绘制歌曲信息
        String displayText;
        int textColor;
        if (isCurrent) {
            displayText = UIConstants.ICON_PLAYING + " " + track.getTitle() + " - " + track.getArtist();
            textColor = UIConstants.COLOR_WARNING;
        } else {
            displayText = (index + 1) + ". " + track.getTitle() + " - " + track.getArtist();
            textColor = UIConstants.COLOR_TEXT_PRIMARY;
        }
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                displayText,
                UIConstants.PADDING + 5, itemY + 10,
                textColor
        );
        if (isHovered) {
            // 分享按钮
            String shareText = "[分享]";
            int shareX = context.getWidth() - UIConstants.PADDING - 80,
                    shareColor = UIConstants.COLOR_TEXT_SECONDARY;
            if (mouseX >= shareX && mouseX <= shareX + textRenderer.getWidth(shareText)) {
                shareColor = UIConstants.COLOR_PRIMARY;
            }
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    shareText,
                    shareX, itemY + 10,
                    shareColor
            );

            // 收藏按钮
            boolean isFavorite = favoriteManager.isFavorite(track);
            String favoriteText = isFavorite ? "♥" : "♡";
            int favoriteColor = isFavorite ? UIConstants.COLOR_DANGER : UIConstants.COLOR_TEXT_SECONDARY;
            int favoriteX = context.getWidth() - UIConstants.PADDING - 35;
            if (mouseX >= favoriteX && mouseX <= favoriteX + 15) {
                favoriteColor = UIConstants.COLOR_PRIMARY;
            }
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    favoriteText,
                    favoriteX, itemY + 10,
                    favoriteColor
            );
            // 删除按钮
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    UIConstants.ICON_DELETE,
                    context.getWidth() - UIConstants.PADDING - 15, itemY + 10,
                    UIConstants.COLOR_DANGER
            );
        }
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
        List<MusicTrack> playlist = musicManager.getPlaylist();
        if (CollUtil.isEmpty(playlist)) return false;
        int listY = 60,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        for (int i = playlistScrollOffset; i < playlist.size() && i < playlistScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - playlistScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            if (mouseY >= itemY && mouseY <= itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2) {
                MusicTrack track = playlist.get(i);
                // 点击分享按钮
                String shareText = "[分享]";
                int shareX = context.getWidth() - UIConstants.PADDING - 80;
                if (mouseX >= shareX && mouseX <= shareX + textRenderer.getWidth(shareText)) {
                    shareManager.shareMusic(track);
                    return true;
                }
                // 点击收藏按钮
                int favoriteX = context.getWidth() - UIConstants.PADDING - 35;
                if (mouseX >= favoriteX && mouseX <= favoriteX + 15) {
                    favoriteManager.toggleFavorite(track);
                    return true;
                }
                // 点击删除按钮
                if (mouseX >= context.getWidth() - UIConstants.PADDING - 20) {
                    musicManager.removeFromPlaylist(i);
                    return true;
                }
                // 点击歌曲区域，播放该歌曲
                musicManager.playTrackAt(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        if (!musicManager.isPlaylistEmpty()) {
            int listY = 40,
                    listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT;
            playlistScrollOffset = RenderHelper.calculateScrollOffset(
                    playlistScrollOffset,
                    musicManager.getPlaylistSize(),
                    UIConstants.PLAYLIST_ITEM_HEIGHT,
                    listHeight,
                    verticalAmount
            );
            return true;
        }
        return false;
    }
}
