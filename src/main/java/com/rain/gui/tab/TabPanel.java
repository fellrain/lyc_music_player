package com.rain.gui.tab;

import net.minecraft.client.gui.DrawContext;

/**
 * 标签页面板接口
 * <p>
 * 定义标签页的通用行为，使用策略模式将不同标签页的逻辑分离。
 * 每个标签页实现类负责自己的初始化、渲染和事件处理。
 * </p>
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
public interface TabPanel {

    /**
     * 初始化标签页
     * <p>
     * 在标签页被激活时调用，用于添加控件、重置状态等。
     * </p>
     *
     * @param context 标签页上下文，提供与主Screen交互的方法
     */
    void init(TabPanelContext context);

    /**
     * 渲染标签页内容
     * <p>
     * 每帧调用，用于绘制标签页的UI元素。
     * </p>
     *
     * @param drawContext 绘制上下文
     * @param mouseX      鼠标X坐标
     * @param mouseY      鼠标Y坐标
     * @param delta       帧间隔时间
     */
    void render(DrawContext drawContext, int mouseX, int mouseY, float delta);

    /**
     * 处理鼠标点击事件
     * <p>
     * 当用户点击鼠标时调用，用于处理自定义的点击逻辑。
     * </p>
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 如果事件被处理返回true，否则返回false
     */
    boolean handleClick(double mouseX, double mouseY);

    /**
     * 处理鼠标滚轮事件
     * <p>
     * 当用户滚动鼠标滚轮时调用，用于处理滚动逻辑。
     * </p>
     *
     * @param mouseX         鼠标X坐标
     * @param mouseY         鼠标Y坐标
     * @param verticalAmount 垂直滚动量（正值向上，负值向下）
     * @return 如果事件被处理返回true，否则返回false
     */
    boolean handleScroll(double mouseX, double mouseY, double verticalAmount);

    /**
     * 渲染覆盖层（模态弹窗等）
     * <p>
     * 在所有其他内容渲染后调用，确保模态弹窗显示在最上层。
     * </p>
     *
     * @param drawContext 绘制上下文
     * @param mouseX      鼠标X坐标
     * @param mouseY      鼠标Y坐标
     * @param delta       帧间隔时间
     */
    default void renderOverlay(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // 默认实现：不渲染任何覆盖层
    }
}
