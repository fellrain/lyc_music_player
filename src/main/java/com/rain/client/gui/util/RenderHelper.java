package com.rain.client.gui.util;

import com.rain.client.gui.constants.UIConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * 渲染工具类
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public final class RenderHelper {

    private RenderHelper() {
    }

    /**
     * 绘制主要文本（白色）
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param x        X坐标
     * @param y        Y坐标
     */
    public static void drawPrimaryText(DrawContext context, TextRenderer renderer, String text, int x, int y) {
        context.drawTextWithShadow(renderer, text, x, y, UIConstants.COLOR_TEXT_PRIMARY);
    }

    /**
     * 绘制次要文本（灰色）
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param x        X坐标
     * @param y        Y坐标
     */
    public static void drawSecondaryText(DrawContext context, TextRenderer renderer, String text, int x, int y) {
        context.drawTextWithShadow(renderer, text, x, y, UIConstants.COLOR_TEXT_SECONDARY);
    }

    /**
     * 绘制居中的主要文本
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param centerX  中心X坐标
     * @param y        Y坐标
     */
    public static void drawCenteredPrimaryText(DrawContext context, TextRenderer renderer, String text, int centerX, int y) {
        context.drawCenteredTextWithShadow(renderer, text, centerX, y, UIConstants.COLOR_TEXT_PRIMARY);
    }

    /**
     * 绘制居中的次要文本
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param centerX  中心X坐标
     * @param y        Y坐标
     */
    public static void drawCenteredSecondaryText(DrawContext context, TextRenderer renderer, String text, int centerX, int y) {
        context.drawCenteredTextWithShadow(renderer, text, centerX, y, UIConstants.COLOR_TEXT_SECONDARY);
    }

    /**
     * 绘制带颜色的文本
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param x        X坐标
     * @param y        Y坐标
     * @param color    颜色值
     */
    public static void drawColoredText(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
        context.drawTextWithShadow(renderer, text, x, y, color);
    }

    /**
     * 绘制居中的带颜色文本
     *
     * @param context  绘制上下文
     * @param renderer 文本渲染器
     * @param text     文本内容
     * @param centerX  中心X坐标
     * @param y        Y坐标
     * @param color    颜色值
     */
    public static void drawCenteredColoredText(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color) {
        context.drawCenteredTextWithShadow(renderer, text, centerX, y, color);
    }

    /**
     * 绘制列表项背景
     *
     * @param context   绘制上下文
     * @param x1        左上角X坐标
     * @param y1        左上角Y坐标
     * @param x2        右下角X坐标
     * @param y2        右下角Y坐标
     * @param isHovered 是否悬停
     * @param isSelected 是否选中
     */
    public static void drawListItemBackground(DrawContext context, int x1, int y1, int x2, int y2, 
                                              boolean isHovered, boolean isSelected) {
        int backgroundColor;
        if (isSelected) {
            backgroundColor = UIConstants.COLOR_ITEM_BACKGROUND_SELECTED;
        } else if (isHovered) {
            backgroundColor = UIConstants.COLOR_ITEM_BACKGROUND_HOVER;
        } else {
            backgroundColor = UIConstants.COLOR_ITEM_BACKGROUND;
        }
        context.fill(x1, y1, x2, y2, backgroundColor);
    }

    /**
     * 计算滚动偏移量
     *
     * @param currentOffset   当前偏移量
     * @param totalItems      总项目数
     * @param itemHeight      单项高度
     * @param containerHeight 容器高度
     * @param scrollDelta     滚动增量
     * @return 新的滚动偏移量
     */
    public static int calculateScrollOffset(int currentOffset, int totalItems, int itemHeight, 
                                           int containerHeight, double scrollDelta) {
        int maxVisible = containerHeight / itemHeight;
        int maxScroll = Math.max(0, totalItems - maxVisible);
        return Math.max(0, Math.min(maxScroll, currentOffset - (int) scrollDelta));
    }

    /**
     * 检查鼠标是否在矩形区域内
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param x1     矩形左上角X坐标
     * @param y1     矩形左上角Y坐标
     * @param x2     矩形右下角X坐标
     * @param y2     矩形右下角Y坐标
     * @return 是否在区域内
     */
    public static boolean isMouseOver(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }
}
