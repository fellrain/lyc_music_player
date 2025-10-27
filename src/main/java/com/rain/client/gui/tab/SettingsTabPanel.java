package com.rain.client.gui.tab;

import com.rain.client.gui.constants.UIConstants;
import com.rain.client.gui.util.RenderHelper;
import com.rain.client.manager.CookieManager;
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
 * 提供Cookie管理功能，用户可以保存和清除API请求所需的Cookie。
 * </p>
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class SettingsTabPanel implements TabPanel {

    private final CookieManager cookieManager;
    private TabPanelContext context;
    private TextRenderer textRenderer;
    private TextFieldWidget cookieField;

    /**
     * 构造设置标签页
     */
    public SettingsTabPanel() {
        this.cookieManager = CookieManager.getInstance();
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
        RenderHelper.drawCenteredColoredText(
                drawContext, textRenderer,
                "设置",
                context.getWidth() / 2, startY - 8,
                UIConstants.COLOR_PRIMARY
        );
        // 绘制Cookie标签
        RenderHelper.drawPrimaryText(
                drawContext, textRenderer,
                "Cookie:",
                UIConstants.PADDING, startY + 10
        );
        // 绘制Cookie状态
        renderCookieStatus(drawContext, startY);
        // 绘制说明信息
        renderInstructions(drawContext, startY);
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
        RenderHelper.drawColoredText(
                drawContext, textRenderer,
                statusText,
                UIConstants.PADDING, startY + 70,
                statusColor
        );
    }

    /**
     * 渲染说明信息
     */
    private void renderInstructions(DrawContext drawContext, int startY) {
        RenderHelper.drawSecondaryText(
                drawContext, textRenderer,
                "§7说明: 输入Cookie后，所有API请求都会带上此Cookie",
                UIConstants.PADDING, startY + 100
        );
        RenderHelper.drawSecondaryText(
                drawContext, textRenderer,
                "§7您可以随时更新或清除Cookie",
                UIConstants.PADDING, startY + 115
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
