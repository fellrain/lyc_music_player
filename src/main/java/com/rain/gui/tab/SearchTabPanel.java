package com.rain.gui.tab;

import com.rain.MusicPlayerMod;
import com.rain.audio.AudioManager;
import com.rain.gui.constants.UIConstants;
import com.rain.gui.util.RenderHelper;
import com.rain.manager.CategoryManager;
import com.rain.manager.FavoriteManager;
import com.rain.manager.MusicManager;
import com.rain.model.MusicTrack;
import com.rain.network.MusicAPIClient;
import com.rain.util.CollUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

/**
 * 搜索标签页面板
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public class SearchTabPanel implements TabPanel {

    private final AudioManager audioManager;
    private final MusicManager musicManager;
    private final MusicAPIClient apiClient;
    private final FavoriteManager favoriteManager;
    private final CategoryManager categoryManager;
    private final Map<Integer, MusicTrack> searchResults = new HashMap<>();
    private TabPanelContext context;
    private TextRenderer textRenderer;
    private TextFieldWidget searchField;
    private int searchScrollOffset = 0;
    
    private boolean showingCategoryPopup = false;
    private MusicTrack pendingTrack = null;

    /**
     * 构造搜索标签页
     */
    public SearchTabPanel(AudioManager audioManager, MusicManager musicManager, MusicAPIClient apiClient) {
        this.audioManager = audioManager;
        this.musicManager = musicManager;
        this.apiClient = apiClient;
        this.favoriteManager = MusicPlayerMod.getInstance().getFavoriteManager();
        this.categoryManager = MusicPlayerMod.getInstance().getCategoryManager();
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
        initSearchControls();
    }

    /**
     * 初始化搜索控件
     */
    private void initSearchControls() {
        int searchY = 40,
                searchWidth = context.getWidth() - 2 * UIConstants.PADDING - 70;
        searchField = new TextFieldWidget(
                textRenderer,
                UIConstants.PADDING,
                searchY,
                searchWidth,
                UIConstants.SEARCH_FIELD_HEIGHT,
                Text.literal("搜索音乐")
        );
        searchField.setMaxLength(100);
        searchField.setPlaceholder(Text.literal("输入歌曲名或艺术家..."));
        context.addWidget(searchField);
        context.addWidget(ButtonWidget.builder(
                Text.literal("搜索"),
                button -> searchMusic()
        ).dimensions(UIConstants.PADDING + searchWidth + 5, searchY, 60, UIConstants.SEARCH_FIELD_HEIGHT).build());
    }

    /**
     * 执行音乐搜索
     */
    private void searchMusic() {
        String query = searchField.getText();
        if (query.isEmpty()) {
            return;
        }
        searchResults.clear();
        searchScrollOffset = 0;
        apiClient.searchMusic(query).thenAccept(result -> MinecraftClient.getInstance()
                .execute(() -> {
                    if (!result.isEmpty()) {
                        List<MusicTrack> tracks = result.tracks();
                        for (int i = 0; i < tracks.size(); i++) {
                            searchResults.put(i, tracks.get(i));
                        }
                        MusicPlayerMod.LOGGER.info("搜索完成，共 {} 首歌曲", searchResults.size());
                    }
                })).exceptionally(throwable -> {
            MusicPlayerMod.LOGGER.error("搜索失败", throwable);
            return null;
        });
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (CollUtil.isEmpty(searchResults)) {
            renderEmptyState(drawContext);
            return;
        }
        renderSearchResults(drawContext, mouseX, mouseY);
    }

    @Override
    public void renderOverlay(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // 在最上层渲染分类弹窗，确保不被其他元素遭挡
        if (showingCategoryPopup) {
            renderCategoryPopup(drawContext, mouseX, mouseY);
        }
    }

    /**
     * 渲染空状态提示
     */
    private void renderEmptyState(DrawContext drawContext) {
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "输入关键词搜索音乐",
                context.getWidth() / 2,
                UIConstants.LIST_TOP_OFFSET + 50
        );
    }

    /**
     * 渲染搜索结果列表
     */
    private void renderSearchResults(DrawContext drawContext, int mouseX, int mouseY) {
        int listY = UIConstants.LIST_TOP_OFFSET,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.SEARCH_ITEM_HEIGHT,
                displayIndex = 0;
        for (Map.Entry<Integer, MusicTrack> entry : searchResults.entrySet()) {
            if (displayIndex < searchScrollOffset) {
                displayIndex++;
                continue;
            }
            if (displayIndex >= searchScrollOffset + maxVisible) {
                break;
            }
            int itemY = listY + (displayIndex - searchScrollOffset) * UIConstants.SEARCH_ITEM_HEIGHT;
            renderSearchResultItem(drawContext, entry.getValue(), itemY, mouseX, mouseY);
            displayIndex++;
        }
    }

    /**
     * 渲染单个搜索结果项
     */
    private void renderSearchResultItem(DrawContext drawContext, MusicTrack track, int itemY, int mouseX, int mouseY) {
        boolean isHovered = RenderHelper.isMouseOver(
                mouseX, mouseY,
                UIConstants.PADDING, itemY,
                context.getWidth() - UIConstants.PADDING, itemY + UIConstants.SEARCH_ITEM_HEIGHT - 2
        );
        // 绘制背景
        RenderHelper.drawListItemBackground(
                drawContext,
                UIConstants.PADDING, itemY,
                context.getWidth() - UIConstants.PADDING, itemY + UIConstants.SEARCH_ITEM_HEIGHT - 2,
                isHovered, false
        );
        // 绘制歌曲信息
        RenderHelper.drawPrimaryText(
                drawContext, textRenderer,
                track.getTitle(),
                UIConstants.PADDING + 5, itemY + 5
        );
        RenderHelper.drawSecondaryText(
                drawContext, textRenderer,
                track.getArtist() + " - " + track.getAlbum(),
                UIConstants.PADDING + 5, itemY + 18
        );
        // 绘制操作按钮
        renderActionButtons(drawContext, itemY, mouseX, isHovered, track);
    }

    /**
     * 渲染操作按钮（收藏、播放、添加、分类）
     */
    private void renderActionButtons(DrawContext drawContext, int itemY, int mouseX, boolean isHovered, MusicTrack track) {
        int rightX = context.getWidth() - UIConstants.PADDING;
        
        // 添加到播放列表按钮
        String addText = "添加";
        int addButtonX = rightX - textRenderer.getWidth(addText) - 5;
        int addColor = (isHovered && mouseX >= addButtonX)
                ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                addText,
                addButtonX, itemY + 10,
                addColor
        );
        
        // 播放按钮
        String playText = "播放";
        int playButtonX = addButtonX - textRenderer.getWidth(playText) - 15;
        int playColor = (isHovered && mouseX >= playButtonX && mouseX <= playButtonX + textRenderer.getWidth(playText) + 5)
                ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                playText,
                playButtonX, itemY + 10,
                playColor
        );
        
        // 分类按钮
        String categoryText = "📁";  // 文件夹emoji
        int categoryButtonX = playButtonX - 25;
        int categoryColor = (isHovered && mouseX >= categoryButtonX && mouseX <= categoryButtonX + 20)
                ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_TEXT_SECONDARY;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                categoryText,
                categoryButtonX, itemY + 10,
                categoryColor
        );
        
        // 收藏按钮
        boolean isFavorite = favoriteManager.isFavorite(track);
        String favoriteText = isFavorite ? "♥" : "♡";
        int favoriteButtonX = categoryButtonX - 20;
        int favoriteColor = isFavorite ? UIConstants.COLOR_DANGER : UIConstants.COLOR_TEXT_SECONDARY;
        if (isHovered && mouseX >= favoriteButtonX && mouseX <= favoriteButtonX + 15) {
            favoriteColor = UIConstants.COLOR_PRIMARY;
        }
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                favoriteText,
                favoriteButtonX, itemY + 10,
                favoriteColor
        );
    }

    /**
     * 渲染分类弹窗
     */
    private void renderCategoryPopup(DrawContext drawContext, int mouseX, int mouseY) {
        // 优化后的尺寸：更小更紧凑
        int popupWidth = 160;
        int popupHeight = 180;
        int popupX = (context.getWidth() - popupWidth) / 2;
        int popupY = (context.getHeight() - popupHeight) / 2;
        
        // 绘制半透明背景遮罩（层级最低）
        drawContext.fill(0, 0, context.getWidth(), context.getHeight(), 0x80000000);
        
        // 绘制弹窗阴影（提升层次感）
        drawContext.fill(popupX + 2, popupY + 2, popupX + popupWidth + 2, popupY + popupHeight + 2, 0x80000000);
        
        // 绘制弹窗主体背景（深色）
        drawContext.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, 0xFF1a1a1a);
        
        // 绘制边框
        drawContext.fill(popupX, popupY, popupX + popupWidth, popupY + 1, 0xFF00FF00);
        drawContext.fill(popupX, popupY + popupHeight - 1, popupX + popupWidth, popupY + popupHeight, 0xFF00FF00);
        drawContext.fill(popupX, popupY, popupX + 1, popupY + popupHeight, 0xFF00FF00);
        drawContext.fill(popupX + popupWidth - 1, popupY, popupX + popupWidth, popupY + popupHeight, 0xFF00FF00);
        
        // 标题栏背景
        drawContext.fill(popupX + 1, popupY + 1, popupX + popupWidth - 1, popupY + 25, 0xFF2d2d2d);
        
        // 标题
        RenderHelper.drawCenteredPrimaryText(
                drawContext, textRenderer,
                "选择分类",
                popupX + popupWidth / 2,
                popupY + 8
        );
        
        // 关闭按钮
        String closeText = "×";
        int closeX = popupX + popupWidth - 18;
        int closeY = popupY + 6;
        boolean isCloseHovered = mouseX >= closeX && mouseX <= closeX + 12 
                && mouseY >= closeY && mouseY <= closeY + 12;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                closeText,
                closeX, closeY,
                isCloseHovered ? UIConstants.COLOR_DANGER : UIConstants.COLOR_TEXT_PRIMARY
        );
        
        // 分类列表
        Set<String> categories = categoryManager.getCategoryNames();
        if (CollUtil.isEmpty(categories)) {
            RenderHelper.drawCenteredSecondaryText(
                    drawContext, textRenderer,
                    "还没有分类",
                    popupX + popupWidth / 2,
                    popupY + 70
            );
            RenderHelper.drawCenteredSecondaryText(
                    drawContext, textRenderer,
                    "请在分类页面创建",
                    popupX + popupWidth / 2,
                    popupY + 90
            );
        } else {
            int itemY = popupY + 30;
            int itemHeight = 22;
            List<String> categoryList = new ArrayList<>(categories);
            int maxVisible = Math.min((popupHeight - 35) / itemHeight, categoryList.size());
            
            for (int i = 0; i < maxVisible; i++) {
                String category = categoryList.get(i);
                int currentY = itemY + i * itemHeight;
                boolean isHovered = mouseX >= popupX + 5 && mouseX <= popupX + popupWidth - 5
                        && mouseY >= currentY && mouseY <= currentY + itemHeight - 2;
                
                // 悬停高亮
                if (isHovered) {
                    drawContext.fill(popupX + 5, currentY, popupX + popupWidth - 5, 
                            currentY + itemHeight - 2, 0x8000FF00);
                }
                
                // 绘制分类名称
                String displayText = "📁 " + category;
                // 截断过长的名称
                int maxWidth = popupWidth - 25;
                if (textRenderer.getWidth(displayText) > maxWidth) {
                    while (textRenderer.getWidth(displayText + "...") > maxWidth && displayText.length() > 3) {
                        displayText = displayText.substring(0, displayText.length() - 1);
                    }
                    displayText += "...";
                }
                
                RenderHelper.drawPrimaryText(
                        drawContext, textRenderer,
                        displayText,
                        popupX + 10, currentY + 6
                );
            }
            
            // 如果分类太多，显示滚动提示
            if (categoryList.size() > maxVisible) {
                RenderHelper.drawCenteredSecondaryText(
                        drawContext, textRenderer,
                        "...",
                        popupX + popupWidth / 2,
                        popupY + popupHeight - 15
                );
            }
        }
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
        // 处理弹窗点击
        if (showingCategoryPopup) {
            return handleCategoryPopupClick(mouseX, mouseY);
        }
        
        int listY = UIConstants.LIST_TOP_OFFSET,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.SEARCH_ITEM_HEIGHT,
                displayIndex = 0;
        for (Map.Entry<Integer, MusicTrack> entry : searchResults.entrySet()) {
            if (displayIndex < searchScrollOffset) {
                displayIndex++;
                continue;
            }
            if (displayIndex >= searchScrollOffset + maxVisible) {
                break;
            }
            int itemY = listY + (displayIndex - searchScrollOffset) * UIConstants.SEARCH_ITEM_HEIGHT;
            if (mouseY >= itemY && mouseY <= itemY + UIConstants.SEARCH_ITEM_HEIGHT - 2) {
                return handleItemClick(entry.getValue(), mouseX);
            }
            displayIndex++;
        }
        return false;
    }

    /**
     * 处理分类弹窗点击
     */
    private boolean handleCategoryPopupClick(double mouseX, double mouseY) {
        int popupWidth = 160;
        int popupHeight = 180;
        int popupX = (context.getWidth() - popupWidth) / 2;
        int popupY = (context.getHeight() - popupHeight) / 2;
        
        // 点击关闭按钮
        int closeX = popupX + popupWidth - 18;
        int closeY = popupY + 6;
        if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12) {
            showingCategoryPopup = false;
            pendingTrack = null;
            return true;
        }
        
        // 点击弹窗外部关闭
        if (mouseX < popupX || mouseX > popupX + popupWidth 
                || mouseY < popupY || mouseY > popupY + popupHeight) {
            showingCategoryPopup = false;
            pendingTrack = null;
            return true;
        }
        
        // 点击分类
        Set<String> categories = categoryManager.getCategoryNames();
        if (!CollUtil.isEmpty(categories)) {
            int itemY = popupY + 30;
            int itemHeight = 22;
            List<String> categoryList = new ArrayList<>(categories);
            int maxVisible = Math.min((popupHeight - 35) / itemHeight, categoryList.size());
            
            for (int i = 0; i < maxVisible; i++) {
                String category = categoryList.get(i);
                int currentY = itemY + i * itemHeight;
                
                if (mouseX >= popupX + 5 && mouseX <= popupX + popupWidth - 5
                        && mouseY >= currentY && mouseY <= currentY + itemHeight - 2) {
                    if (pendingTrack != null) {
                        categoryManager.addTrackToCategory(category, pendingTrack);
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            client.player.sendMessage(
                                    Text.literal("§a已添加到分类: " + category), 
                                    false
                            );
                        }
                    }
                    showingCategoryPopup = false;
                    pendingTrack = null;
                    return true;
                }
            }
        }
        
        return true;
    }

    /**
     * 处理列表项点击
     */
    private boolean handleItemClick(MusicTrack track, double mouseX) {
        int rightX = context.getWidth() - UIConstants.PADDING;
        
        // 点击添加按钮
        String addText = "添加";
        int addButtonX = rightX - textRenderer.getWidth(addText) - 5;
        if (mouseX >= addButtonX) {
            boolean added = musicManager.addToPlaylist(track);
            if (!added) {
                // 给出提示：歌曲已在播放列表中
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("§6歌曲已在播放列表中"), 
                            false
                    );
                }
            }
            return true;
        }
        
        // 点击播放按钮
        String playText = "播放";
        int playButtonX = addButtonX - textRenderer.getWidth(playText) - 15;
        if (mouseX >= playButtonX && mouseX <= playButtonX + textRenderer.getWidth(playText) + 5) {
            audioManager.playTrack(track);
            return true;
        }
        
        // 点击分类按钮
        int categoryButtonX = playButtonX - 25;
        if (mouseX >= categoryButtonX && mouseX <= categoryButtonX + 20) {
            pendingTrack = track;
            showingCategoryPopup = true;
            return true;
        }
        
        // 点击收藏按钮
        int favoriteButtonX = categoryButtonX - 20;
        if (mouseX >= favoriteButtonX && mouseX <= favoriteButtonX + 15) {
            favoriteManager.toggleFavorite(track);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        if (!CollUtil.isEmpty(searchResults)) {
            int listY = UIConstants.LIST_TOP_OFFSET;
            int listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT;
            searchScrollOffset = RenderHelper.calculateScrollOffset(
                    searchScrollOffset,
                    searchResults.size(),
                    UIConstants.SEARCH_ITEM_HEIGHT,
                    listHeight,
                    verticalAmount
            );
            return true;
        }
        return false;
    }
}
