package com.rain.client.gui.tab;

import com.rain.client.MusicPlayerClientMod;
import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.CookieManager;
import com.rain.client.network.MusicAPIClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * 设置标签页面板
 * <p>
 * 提供Cookie管理功能和API策略选择功能，
 * 用户可以保存和清除API请求所需的Cookie，
 * 以及选择不同的音乐平台API。
 * </p>
 *
 * @author 落雨川
 * @version 1.6
 * @since 1.5
 */
public class SettingsTabPanel implements TabPanel {

    private final CookieManager cookieManager;

    private final MusicAPIClient apiClient;

    private TabPanelContext context;

    private TextRenderer textRenderer;

    private TextFieldWidget cookieField;

    private final String[] availableStrategies;

    private int currentStrategyIndex = 0;

    /**
     * 构造设置标签页
     */
    public SettingsTabPanel() {
        this.cookieManager = CookieManager.getInstance();
        this.apiClient = MusicPlayerClientMod.getInstance().getApiClient();
        this.availableStrategies = apiClient.getAvailableStrategies();
        // 查找当前策略在数组中的索引
        String currentStrategyName = apiClient.getCurrentStrategyEnum();
        for (int i = 0; i < availableStrategies.length; i++) {
            if (availableStrategies[i].equals(currentStrategyName)) {
                currentStrategyIndex = i;
                break;
            }
        }
    }

    @Override
    public void init(TabPanelContext context) {
        this.context = context;
        this.textRenderer = context.getTextRenderer();
        initSettingsControls();
    }

    /**
     * 初始化设置控件
     */
    private void initSettingsControls() {
        int startY = 50,
                fieldWidth = context.getWidth() - 2 * UIConstants.PADDING - 100;
        // Cookie输入框
        cookieField = new TextFieldWidget(
                textRenderer,
                UIConstants.PADDING,
                startY + 20,
                fieldWidth,
                UIConstants.BUTTON_HEIGHT,
                Text.literal("Cookie")
        );
        cookieField.setMaxLength(2000);
        cookieField.setPlaceholder(Text.literal("输入您的Cookie..."));
        if (cookieManager.hasCookie()) {
            cookieField.setText(cookieManager.getCookie());
        }
        context.addWidget(cookieField);
        // 保存按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("保存"),
                button -> saveCookie()
        ).dimensions(UIConstants.PADDING + fieldWidth + 5, startY + 20, 90, UIConstants.BUTTON_HEIGHT).build());
        // 清除按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("清除"),
                button -> clearCookie()
        ).dimensions(UIConstants.PADDING + fieldWidth + 5, startY + 45, 90, UIConstants.BUTTON_HEIGHT).build());
        // API策略选择控件
        initApiStrategyControls(startY + 100);
    }

    /**
     * 初始化API策略选择控件
     */
    private void initApiStrategyControls(int startY) {
        int fieldWidth = context.getWidth() - 2 * UIConstants.PADDING - 100;
        // 上一个策略按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("<"),
                button -> switchToPreviousStrategy()
        ).dimensions(UIConstants.PADDING, startY + 20, 30, UIConstants.BUTTON_HEIGHT).build());
        // 下一个策略按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal(">"),
                button -> switchToNextStrategy()
        ).dimensions(UIConstants.PADDING + fieldWidth + 5 + 60, startY + 20, 30, UIConstants.BUTTON_HEIGHT).build());
        // 应用策略按钮
        context.addWidget(ButtonWidget.builder(
                Text.literal("应用"),
                button -> applyCurrentStrategy()
        ).dimensions(UIConstants.PADDING + fieldWidth + 5, startY + 45, 90, UIConstants.BUTTON_HEIGHT).build());
    }

    /**
     * 切换到上一个策略
     */
    private void switchToPreviousStrategy() {
        if (availableStrategies.length > 1) {
            currentStrategyIndex = (currentStrategyIndex - 1 + availableStrategies.length) % availableStrategies.length;
            context.requestReinit();
        }
    }

    /**
     * 切换到下一个策略
     */
    private void switchToNextStrategy() {
        if (availableStrategies.length > 1) {
            currentStrategyIndex = (currentStrategyIndex + 1) % availableStrategies.length;
            context.requestReinit();
        }
    }

    /**
     * 应用当前选择的策略
     */
    private void applyCurrentStrategy() {
        if (currentStrategyIndex >= 0 && currentStrategyIndex < availableStrategies.length) {
            String strategyName = availableStrategies[currentStrategyIndex];
            boolean success = apiClient.setStrategy(strategyName);
            MinecraftClient client = MinecraftClient.getInstance();
            if (!Objects.isNull(client.player)) {
                if (success) {
                    client.player.sendMessage(Text.literal("§a已切换到 " + strategyName + " API策略"), false);
                } else {
                    client.player.sendMessage(Text.literal("§c切换API策略失败"), false);
                }
            }
        }
    }

    /**
     * 保存Cookie
     */
    private void saveCookie() {
        String cookie = cookieField.getText();
        cookieManager.setCookie(cookie);
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Objects.isNull(client.player)) {
            client.player.sendMessage(Text.literal("§aCookie已保存"), false);
        }
    }

    /**
     * 清除Cookie
     */
    private void clearCookie() {
        cookieManager.clearCookie();
        cookieField.setText("");
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Objects.isNull(client.player)) {
            client.player.sendMessage(Text.literal("§6Cookie已清除"), false);
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int startY = 50;
        // 绘制标题
        RenderHelper.drawCenteredColoredText(drawContext, textRenderer, "设置", context.getWidth() / 2, startY - 8, UIConstants.COLOR_PRIMARY);
        // 绘制Cookie标签
        RenderHelper.drawPrimaryText(drawContext, textRenderer, "Cookie:", UIConstants.PADDING, startY + 10);
        // 绘制Cookie状态
        renderCookieStatus(drawContext, startY);
        // 绘制API策略选择区域
        renderApiStrategySection(drawContext, startY + 100);
        // 绘制说明信息
        renderInstructions(drawContext, startY + 160);
    }

    /**
     * 渲染Cookie状态信息
     */
    private void renderCookieStatus(DrawContext drawContext, int startY) {
        String statusText;
        int statusColor;
        if (cookieManager.hasCookie()) {
            statusText = "§a已设置 (" + cookieManager.getCookie().length() + " 个字符)";
            statusColor = UIConstants.COLOR_PRIMARY;
        } else {
            statusText = "§c未设置";
            statusColor = UIConstants.COLOR_DANGER;
        }
        RenderHelper.drawColoredText(drawContext, textRenderer, statusText, UIConstants.PADDING, startY + 70, statusColor);
    }

    /**
     * 渲染API策略选择区域
     */
    private void renderApiStrategySection(DrawContext drawContext, int startY) {
        // 绘制API策略标签
        RenderHelper.drawPrimaryText(drawContext, textRenderer, "API策略:", UIConstants.PADDING, startY);
        // 绘制当前策略名称
        if (availableStrategies.length > 0 && currentStrategyIndex < availableStrategies.length) {
            // 策略 (1/1)
            String currentStrategy = availableStrategies[currentStrategyIndex],
                    currentStrategyDisplay = currentStrategy + " (" + (currentStrategyIndex + 1) + "/" + availableStrategies.length + ")";
            RenderHelper.drawCenteredColoredText(drawContext, textRenderer, currentStrategyDisplay, context.getWidth() / 2, startY + 25, UIConstants.COLOR_WARNING);
        } else {
            RenderHelper.drawCenteredSecondaryText(drawContext, textRenderer, "无可用策略", context.getWidth() / 2, startY + 25);
        }
    }

    /**
     * 渲染说明信息
     */
    private void renderInstructions(DrawContext drawContext, int startY) {
        RenderHelper.drawSecondaryText(drawContext, textRenderer, "§7说明: 输入Cookie后，所有API请求都会带上此Cookie", UIConstants.PADDING, startY);
        RenderHelper.drawSecondaryText(drawContext, textRenderer, "§7您可以随时更新或清除Cookie", UIConstants.PADDING, startY + 15);
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