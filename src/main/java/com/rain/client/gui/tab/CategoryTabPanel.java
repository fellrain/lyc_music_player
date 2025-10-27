package com.rain.client.gui.tab;

import com.rain.client.MusicPlayerMod;
import com.rain.client.audio.AudioManager;
import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.CategoryManager;
import com.rain.client.manager.MusicManager;
import com.rain.client.model.MusicTrack;
import com.rain.util.CollUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

/**
 * 分类管理标签页面板
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class CategoryTabPanel implements TabPanel {

    private final AudioManager audioManager;
    private final MusicManager musicManager;
    private final CategoryManager categoryManager;
    private TabPanelContext context;
    private TextRenderer textRenderer;
    private TextFieldWidget categoryNameField;
    
    private String selectedCategory = null;
    private int categoryScrollOffset = 0;
    private int trackScrollOffset = 0;
    private boolean showingCategoryList = true;

    public CategoryTabPanel(AudioManager audioManager, MusicManager musicManager) {
        this.audioManager = audioManager;
        this.musicManager = musicManager;
        this.categoryManager = MusicPlayerMod.getInstance().getCategoryManager();
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
        initControls();
    }

    /**
     * 初始化控件
     */
    private void initControls() {
        // 创建分类输入框
        categoryNameField = new TextFieldWidget(
                textRenderer,
                UIConstants.PADDING,
                40,
                150,
                UIConstants.SEARCH_FIELD_HEIGHT,
                Text.literal("分类名称")
        );
        categoryNameField.setMaxLength(20);
        categoryNameField.setPlaceholder(Text.literal("输入分类名称..."));
        context.addWidget(categoryNameField);
        
        // 创建分类按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("创建分类"),
                button -> createCategory()
        ).dimensions(UIConstants.PADDING + 160, 40, 70, UIConstants.SEARCH_FIELD_HEIGHT).build());
        
        // 返回按钮（只在显示分类内容时显示）
        if (!showingCategoryList) {
            context.addWidget(ButtonWidget.builder(
                    Text.literal("返回"),
                    button -> {
                        showingCategoryList = true;
                        selectedCategory = null;
                        context.requestReinit();
                    }
            ).dimensions(UIConstants.PADDING + 240, 40, 60, UIConstants.SEARCH_FIELD_HEIGHT).build());
        }
    }

    /**
     * 创建分类
     */
    private void createCategory() {
        String categoryName = categoryNameField.getText().trim();
        if (categoryName.isEmpty()) {
            return;
        }
        
        // 检查是否已达到数量上限
        if (categoryManager.isAtMaxCapacity()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c分类数量已达到上限（" + categoryManager.getMaxCategories() + "个）"), 
                    false
                );
            }
            return;
        }
        
        if (categoryManager.createCategory(categoryName)) {
            categoryNameField.setText("");
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a已创建分类: " + categoryName), false);
            }
        } else {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c分类已存在或名称无效"), false);
            }
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (showingCategoryList) {
            renderCategoryList(drawContext, mouseX, mouseY);
        } else {
            renderCategoryContent(drawContext, mouseX, mouseY);
        }
    }

    /**
     * 渲染分类列表
     */
    private void renderCategoryList(DrawContext drawContext, int mouseX, int mouseY) {
        Set<String> categories = categoryManager.getCategoryNames();
        
        if (CollUtil.isEmpty(categories)) {
            renderEmptyState(drawContext);
            return;
        }
        
        // 显示分类数量和限制
        String headerText = "我的分类 (" + categories.size() + "/" + categoryManager.getMaxCategories() + ")";
        int headerColor = categoryManager.isAtMaxCapacity() ? UIConstants.COLOR_WARNING : UIConstants.COLOR_PRIMARY;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                headerText,
                UIConstants.PADDING, 70,
                headerColor
        );
        
        // 如果已达上限，显示提示
        if (categoryManager.isAtMaxCapacity()) {
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    "§e已达到最大分类数量，请删除后再创建",
                    context.getWidth() - UIConstants.PADDING - 220, 70,
                    UIConstants.COLOR_WARNING
            );
        }
        
        int listY = 90,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        List<String> categoryList = new ArrayList<>(categories);
        for (int i = categoryScrollOffset; i < categoryList.size() && i < categoryScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - categoryScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            String category = categoryList.get(i);
            renderCategoryItem(drawContext, category, itemY, mouseX, mouseY);
        }
    }

    /**
     * 渲染单个分类项
     */
    private void renderCategoryItem(DrawContext drawContext, String category, int itemY,
                                   int mouseX, int mouseY) {
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
        
        // 绘制分类名称和歌曲数量
        int trackCount = categoryManager.getTrackCountInCategory(category);
        String displayText = "📁 " + category + " (" + trackCount + " 首)";
        RenderHelper.drawPrimaryText(
                drawContext, textRenderer,
                displayText,
                UIConstants.PADDING + 5, itemY + 10
        );
        
        // 绘制删除按钮
        if (isHovered) {
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    UIConstants.ICON_DELETE,
                    context.getWidth() - UIConstants.PADDING - 15, itemY + 10,
                    UIConstants.COLOR_DANGER
            );
        }
    }

    /**
     * 渲染分类内容（分类中的歌曲列表）
     */
    private void renderCategoryContent(DrawContext drawContext, int mouseX, int mouseY) {
        if (selectedCategory == null) {
            showingCategoryList = true;
            return;
        }
        
        List<MusicTrack> tracks = categoryManager.getTracksInCategory(selectedCategory);
        
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                selectedCategory + " (" + tracks.size() + " 首)",
                UIConstants.PADDING, 70,
                UIConstants.COLOR_PRIMARY
        );
        
        if (CollUtil.isEmpty(tracks)) {
            RenderHelper.drawCenteredSecondaryText(
                    drawContext, textRenderer,
                    "该分类还没有歌曲",
                    context.getWidth() / 2,
                    120
            );
            return;
        }
        
        int listY = 90,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        for (int i = trackScrollOffset; i < tracks.size() && i < trackScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - trackScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            MusicTrack track = tracks.get(i);
            renderTrackItem(drawContext, track, itemY, mouseX, mouseY, i);
        }
    }

    /**
     * 渲染歌曲项
     */
    private void renderTrackItem(DrawContext drawContext, MusicTrack track, int itemY,
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
        String displayText = (index + 1) + ". " + track.getTitle() + " - " + track.getArtist();
        RenderHelper.drawPrimaryText(
                drawContext, textRenderer,
                displayText,
                UIConstants.PADDING + 5, itemY + 10
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
            
            // 移除按钮
            int removeX = context.getWidth() - UIConstants.PADDING - 15;
            RenderHelper.drawColoredText(
                    drawContext, textRenderer,
                    UIConstants.ICON_DELETE,
                    removeX, itemY + 10,
                    UIConstants.COLOR_DANGER
            );
        }
    }

    /**
     * 渲染空状态
     */
    private void renderEmptyState(DrawContext drawContext) {
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "还没有创建分类",
                context.getWidth() / 2,
                100
        );
        RenderHelper.drawCenteredSecondaryText(
                drawContext, textRenderer,
                "在上方输入分类名称并点击\"\u521b建分类\"",
                context.getWidth() / 2,
                120
        );
        RenderHelper.drawCenteredColoredText(
                drawContext, textRenderer,
                "§e最多可创建 " + categoryManager.getMaxCategories() + " 个分类",
                context.getWidth() / 2,
                140,
                UIConstants.COLOR_WARNING
        );
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
        if (showingCategoryList) {
            return handleCategoryListClick(mouseX, mouseY);
        } else {
            return handleCategoryContentClick(mouseX, mouseY);
        }
    }

    /**
     * 处理分类列表点击
     */
    private boolean handleCategoryListClick(double mouseX, double mouseY) {
        Set<String> categories = categoryManager.getCategoryNames();
        if (CollUtil.isEmpty(categories)) return false;
        
        int listY = 90,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        List<String> categoryList = new ArrayList<>(categories);
        for (int i = categoryScrollOffset; i < categoryList.size() && i < categoryScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - categoryScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            
            if (mouseY >= itemY && mouseY <= itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2) {
                String category = categoryList.get(i);
                
                // 点击删除按钮
                if (mouseX >= context.getWidth() - UIConstants.PADDING - 20) {
                    categoryManager.deleteCategory(category);
                    return true;
                }
                
                // 点击分类，查看分类内容
                selectedCategory = category;
                showingCategoryList = false;
                trackScrollOffset = 0;
                context.requestReinit();
                return true;
            }
        }
        return false;
    }

    /**
     * 处理分类内容点击
     */
    private boolean handleCategoryContentClick(double mouseX, double mouseY) {
        if (selectedCategory == null) return false;
        
        List<MusicTrack> tracks = categoryManager.getTracksInCategory(selectedCategory);
        if (CollUtil.isEmpty(tracks)) return false;
        
        int listY = 90,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT,
                maxVisible = listHeight / UIConstants.PLAYLIST_ITEM_HEIGHT;
        
        for (int i = trackScrollOffset; i < tracks.size() && i < trackScrollOffset + maxVisible; i++) {
            int itemY = listY + (i - trackScrollOffset) * UIConstants.PLAYLIST_ITEM_HEIGHT;
            
            if (mouseY >= itemY && mouseY <= itemY + UIConstants.PLAYLIST_ITEM_HEIGHT - 2) {
                MusicTrack track = tracks.get(i);
                
                // 点击移除按钮
                int removeX = context.getWidth() - UIConstants.PADDING - 20;
                if (mouseX >= removeX) {
                    categoryManager.removeTrackFromCategory(selectedCategory, i);
                    return true;
                }
                
                // 点击播放按钮
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
        int listY = showingCategoryList ? 90 : 90,
                listHeight = context.getHeight() - listY - UIConstants.CONTROL_AREA_HEIGHT;
        
        if (showingCategoryList) {
            Set<String> categories = categoryManager.getCategoryNames();
            if (!CollUtil.isEmpty(categories)) {
                categoryScrollOffset = RenderHelper.calculateScrollOffset(
                        categoryScrollOffset,
                        categories.size(),
                        UIConstants.PLAYLIST_ITEM_HEIGHT,
                        listHeight,
                        verticalAmount
                );
                return true;
            }
        } else if (selectedCategory != null) {
            List<MusicTrack> tracks = categoryManager.getTracksInCategory(selectedCategory);
            if (!CollUtil.isEmpty(tracks)) {
                trackScrollOffset = RenderHelper.calculateScrollOffset(
                        trackScrollOffset,
                        tracks.size(),
                        UIConstants.PLAYLIST_ITEM_HEIGHT,
                        listHeight,
                        verticalAmount
                );
                return true;
            }
        }
        return false;
    }
}
