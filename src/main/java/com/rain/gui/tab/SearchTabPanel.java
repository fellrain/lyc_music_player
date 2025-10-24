package com.rain.gui.tab;

import com.rain.MusicPlayerMod;
import com.rain.audio.AudioManager;
import com.rain.gui.constants.UIConstants;
import com.rain.gui.util.RenderHelper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Integer, MusicTrack> searchResults = new HashMap<>();
    private TabPanelContext context;
    private TextRenderer textRenderer;
    private TextFieldWidget searchField;
    private int searchScrollOffset = 0;

    /**
     * 构造搜索标签页
     */
    public SearchTabPanel(AudioManager audioManager, MusicManager musicManager, MusicAPIClient apiClient) {
        this.audioManager = audioManager;
        this.musicManager = musicManager;
        this.apiClient = apiClient;
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
        renderActionButtons(drawContext, itemY, mouseX, isHovered);
    }

    /**
     * 渲染操作按钮（播放、添加）
     */
    private void renderActionButtons(DrawContext drawContext, int itemY, int mouseX, boolean isHovered) {
        String playText = "播放";
        int playButtonWidth = textRenderer.getWidth(playText) + 10,
                playButtonX = context.getWidth() - UIConstants.PADDING - playButtonWidth - 80,
                playColor = (isHovered && mouseX >= playButtonX && mouseX <= playButtonX + playButtonWidth)
                        ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                playText,
                playButtonX, itemY + 10,
                playColor
        );
        String addText = "添加";
        int addButtonX = context.getWidth() - UIConstants.PADDING - textRenderer.getWidth(addText) - 5,
                addColor = (isHovered && mouseX >= addButtonX)
                        ? UIConstants.COLOR_PRIMARY : UIConstants.COLOR_WARNING;
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                addText,
                addButtonX, itemY + 10,
                addColor
        );
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY) {
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
     * 处理列表项点击
     */
    private boolean handleItemClick(MusicTrack track, double mouseX) {
        String playText = "播放";
        int playButtonWidth = textRenderer.getWidth(playText) + 10,
                playButtonX = context.getWidth() - UIConstants.PADDING - playButtonWidth - 80;
        // 点击播放按钮
        if (mouseX >= playButtonX && mouseX <= playButtonX + playButtonWidth) {
            audioManager.playTrack(track);
            return true;
        }
        // 点击添加按钮
        String addText = "添加";
        int addButtonX = context.getWidth() - UIConstants.PADDING - textRenderer.getWidth(addText) - 5;
        if (mouseX >= addButtonX) {
            musicManager.addToPlaylist(track);
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
