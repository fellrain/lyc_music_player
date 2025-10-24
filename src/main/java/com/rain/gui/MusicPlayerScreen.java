package com.rain.gui;

import com.rain.MusicPlayerMod;
import com.rain.audio.AudioManager;
import com.rain.gui.constants.UIConstants;
import com.rain.gui.tab.*;
import com.rain.manager.MusicManager;
import com.rain.network.MusicAPIClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * 音乐播放器GUI主界面
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public class MusicPlayerScreen extends Screen {

    private final AudioManager audioManager;
    private final MusicManager musicManager;
    private final MusicAPIClient apiClient;
    private final Map<Tab, TabPanel> tabPanels = new EnumMap<>(Tab.class);
    private Tab currentTab = Tab.PLAYER;

    /**
     * 标签页枚举
     */
    private enum Tab {
        PLAYER("播放器"),
        SEARCH("搜索"),
        PLAYLIST("播放列表"),
        FAVORITE("收藏"),
        CATEGORY("分类"),
        SETTINGS("设置");

        final String name;

        Tab(String name) {
            this.name = name;
        }
    }

    /**
     * 构造音乐播放器Screen
     */
    public MusicPlayerScreen() {
        super(Text.literal("小落音乐播放器"));
        this.audioManager = MusicPlayerMod.getInstance().getAudioManager();
        this.musicManager = MusicPlayerMod.getInstance().getMusicManager();
        this.apiClient = MusicPlayerMod.getInstance().getApiClient();
        initializeTabPanels();
    }

    /**
     * 初始化所有标签页面板
     */
    private void initializeTabPanels() {
        tabPanels.put(Tab.PLAYER, new PlayerTabPanel(audioManager, musicManager));
        tabPanels.put(Tab.SEARCH, new SearchTabPanel(audioManager, musicManager, apiClient));
        tabPanels.put(Tab.PLAYLIST, new PlaylistTabPanel(musicManager));
        tabPanels.put(Tab.FAVORITE, new FavoriteTabPanel(audioManager, musicManager, apiClient));
        tabPanels.put(Tab.CATEGORY, new CategoryTabPanel(audioManager, musicManager));
        tabPanels.put(Tab.SETTINGS, new SettingsTabPanel());
    }

    @Override
    protected void init() {
        super.init();
        initTabButtons();
        initCurrentTab();
    }

    /**
     * 初始化标签页按钮
     */
    private void initTabButtons() {
        Tab[] tabs = Tab.values();
        int totalTabs = tabs.length;
        int totalWidth = this.width - 2 * UIConstants.PADDING;
        int tabWidth = (totalWidth - (totalTabs - 1) * UIConstants.TAB_SPACING) / totalTabs;
        
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            int tabX = UIConstants.PADDING + i * (tabWidth + UIConstants.TAB_SPACING);
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(tab.name),
                    button -> switchTab(tab)
            ).dimensions(tabX, UIConstants.PADDING, tabWidth, UIConstants.BUTTON_HEIGHT).build());
        }
    }

    /**
     * 初始化当前激活的标签页
     */
    private void initCurrentTab() {
        TabPanel currentPanel = tabPanels.get(currentTab);
        if (!Objects.isNull(currentPanel)) {
            TabPanelContext context = new TabPanelContext(
                    this.width,
                    this.height,
                    this.textRenderer,
                    this::addDrawableChild,
                    this::reinitialize
            );
            currentPanel.init(context);
        }
    }

    /**
     * 切换到指定标签页
     *
     * @param tab 目标标签页
     */
    private void switchTab(Tab tab) {
        this.currentTab = tab;
        reinitialize();
    }

    /**
     * 重新初始化界面
     */
    private void reinitialize() {
        clearChildren();
        init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制背景渐变
        context.fillGradient(0, 0, this.width, this.height,
                UIConstants.COLOR_BACKGROUND_START, UIConstants.COLOR_BACKGROUND_END);
        // 渲染当前标签页
        TabPanel currentPanel = tabPanels.get(currentTab);
        if (!Objects.isNull(currentPanel)) {
            currentPanel.render(context, mouseX, mouseY, delta);
        }
        // 渲染控件（按钮等）
        super.render(context, mouseX, mouseY, delta);
        
        if (!Objects.isNull(currentPanel)) {
            currentPanel.renderOverlay(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // 优先处理控件点击
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        // 分发点击事件给当前标签页
        TabPanel currentPanel = tabPanels.get(currentTab);
        if (!Objects.isNull(currentPanel)) {
            return currentPanel.handleClick(click.x(), click.y());
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 分发滚轮事件给当前标签页
        TabPanel currentPanel = tabPanels.get(currentTab);
        if (!Objects.isNull(currentPanel) && currentPanel.handleScroll(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
