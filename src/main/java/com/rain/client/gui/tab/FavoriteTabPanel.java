package com.rain.client.gui.tab;

import com.rain.client.MusicPlayerMod;
import com.rain.client.audio.AudioManager;
import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.FavoriteManager;
import com.rain.client.manager.MusicManager;
import com.rain.client.model.MusicTrack;
import com.rain.client.network.MusicAPIClient;
import com.rain.common.util.CollUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.*;

/**
 * 收藏标签页面板
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class FavoriteTabPanel implements TabPanel {

    private final AudioManager audioManager;
    private final MusicManager musicManager;
    private final FavoriteManager favoriteManager;
    private final MusicAPIClient apiClient;
    private TabPanelContext context;
    private TextRenderer textRenderer;
    private final List<MusicTrack> favoriteTracks = new ArrayList<>();
    private int scrollOffset = 0;

    public FavoriteTabPanel(AudioManager audioManager, MusicManager musicManager, MusicAPIClient apiClient) {
        this.audioManager = audioManager;
        this.musicManager = musicManager;
        this.apiClient = apiClient;
        this.favoriteManager = MusicPlayerMod.getInstance().getFavoriteManager();
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
        loadFavorites();
    }

    /**
     * 加载收藏的歌曲
     */
    private void loadFavorites() {
        favoriteTracks.clear();
        // 直接从 FavoriteManager 获取完整的收藏歌曲列表
        List<MusicTrack> tracks = favoriteManager.getFavoriteTracks();
        favoriteTracks.addAll(tracks);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // 重新加载收藏列表（可能有变化）
        loadFavorites();
        
        if (CollUtil.isEmpty(favoriteTracks)) {
            renderEmptyState(drawContext);
            return;
        }
        
        renderHeader(drawContext);
        renderFavoriteItems(drawContext, mouseX, mouseY);
    }

    /**
     * 渲染空状态
     */
    private void renderEmptyState(DrawContext drawContext) {
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "还没有收藏的歌曲",
                context.getWidth() / 2,
                60
        );
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "在搜索或播放列表中点击 ♡ 收藏歌曲",
                context.getWidth() / 2,
                80
        );
    }

    /**
     * 渲染头部
     */
    private void renderHeader(DrawContext drawContext) {
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                "我的收藏 (" + favoriteTracks.size() + " 首)",
                UIConstants.PADDING, 40,
                UIConstants.COLOR_PRIMARY
        );
    }

    /**
     * 渲染收藏项目
     */
    private void renderFavoriteItems(DrawContext drawContext, int mouseX, int mouseY) {
        int listY = 60,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        for (int i = scrollOffset; i < favoriteTracks.size() && i < scrollOffset + maxVisible; i++) {
            int itemY = listY + (i - scrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            MusicTrack track = favoriteTracks.get(i);
            renderFavoriteItem(drawContext, track, itemY, mouseX, mouseY, i);
        }
    }

    /**
     * 渲染单个收藏项
     */
    private void renderFavoriteItem(DrawContext drawContext, MusicTrack track, int itemY,
                                   int mouseX, int mouseY, int index) {
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
                isHovered, false
        );
        
        // 绘制歌曲信息
        String displayText = "♥ " + track.getTitle() + " - " + track.getArtist();
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                displayText,
                UIConstants.PADDING + 5, itemY + 10,
                UIConstants.COLOR_TEXT_PRIMARY
        );
        
        // 绘制操作按钮
        if (isHovered) {
            // 播放按钮
            String playText = "播放";
            int playButtonX = context.getWidth() - UIConstants.PADDING - 100;
            int playColor = (mouseX >= playButtonX && mouseX <= playButtonX + 40) 
                    ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    playText,
                    playButtonX, itemY + 10,
                    playColor
            );
            
            // 添加到播放列表按钮
            String addText = "添加";
            int addButtonX = context.getWidth() - UIConstants.PADDING - 50;
            int addColor = (mouseX >= addButtonX && mouseX <= addButtonX + 40)
                    ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    addText,
                    addButtonX, itemY + 10,
                    addColor
            );
            
            // 取消收藏按钮
            int removeX = context.getWidth() - UIConstants.PADDING - 15;
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    UIConstants.ICON_DELETE,
                    removeX, itemY + 10,
                    UIConstants.COLOR_DANGER
            );
        }
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
        if (CollUtil.isEmpty(favoriteTracks)) return false;
        
        int listY = 60,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        for (int i = scrollOffset; i < favoriteTracks.size() && i < scrollOffset + maxVisible; i++) {
            int itemY = listY + (i - scrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            
            if (mouseY >= itemY && mouseY <= itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2) {
                MusicTrack track = favoriteTracks.get(i);
                
                // 点击取消收藏
                int removeX = context.getWidth() - UIConstants.PADDING - 20;
                if (mouseX >= removeX) {
                    favoriteManager.removeFavorite(track);
                    loadFavorites();
                    return true;
                }
                
                // 点击添加到播放列表
                int addButtonX = context.getWidth() - UIConstants.PADDING - 50;
                if (mouseX >= addButtonX && mouseX <= addButtonX + 40) {
                    musicManager.addToPlaylist(track);
                    return true;
                }
                
                // 点击播放
                int playButtonX = context.getWidth() - UIConstants.PADDING - 100;
                if (mouseX >= playButtonX && mouseX <= playButtonX + 40) {
                    audioManager.playTrack(track);
                    return true;
                }
                
                // 点击其他区域，播放该歌曲
                audioManager.playTrack(track);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        if (!CollUtil.isEmpty(favoriteTracks)) {
            int listY = 60,
                    listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT;
            scrollOffset = RenderHelper.calculateScrollOffset(
                    scrollOffset,
                    favoriteTracks.size(),
                    UIConstants.PLAYLIST_ITEM_HEIGHT,
                    listHeight,
                    verticalAmount
            );
            return true;
        }
        return false;
    }
}
