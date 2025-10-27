package com.rain.client.gui.tab;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;

/**
 * 标签页面板上下文
 * <p>
 * 提供标签页与主Screen交互的接口，避免直接依赖Screen类的protected方法。
 * </p>
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class TabPanelContext {

    private final int width;
    private final int height;
    private final TextRenderer textRenderer;
    private final WidgetAdder widgetAdder;
    private final Runnable reinitializer;

    /**
     * 构造标签页上下文
     *
     * @param width         屏幕宽度
     * @param height        屏幕高度
     * @param textRenderer  文本渲染器
     * @param widgetAdder   控件添加器
     * @param reinitializer 重新初始化回调
     */
    public TabPanelContext(int width, int height, TextRenderer textRenderer,
                          WidgetAdder widgetAdder, Runnable reinitializer) {
        this.width = width;
        this.height = height;
        this.textRenderer = textRenderer;
        this.widgetAdder = widgetAdder;
        this.reinitializer = reinitializer;
    }

    /**
     * 获取屏幕宽度
     *
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @return 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * 获取文本渲染器
     *
     * @return 文本渲染器
     */
    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    /**
     * 添加可绘制控件
     *
     * @param drawable 控件
     * @param <T>      控件类型
     * @return 添加的控件
     */
    public <T extends Element & Drawable & Selectable> T addWidget(T drawable) {
        return widgetAdder.addDrawableChild(drawable);
    }

    /**
     * 请求重新初始化界面
     */
    public void requestReinit() {
        reinitializer.run();
    }

    /**
     * 控件添加器接口
     */
    @FunctionalInterface
    public interface WidgetAdder {
        /**
         * 添加可绘制控件
         *
         * @param drawable 控件
         * @param <T>      控件类型
         * @return 添加的控件
         */
        <T extends Element & Drawable & Selectable> T addDrawableChild(T drawable);
    }
}
