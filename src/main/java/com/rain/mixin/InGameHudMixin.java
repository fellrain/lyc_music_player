package com.rain.mixin;

import com.rain.MusicPlayerMod;
import com.rain.gui.MusicHudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * InGameHud Mixin - 注入HUD渲染逻辑
 *
 * @author 落雨川
 * @version 1.0
 * @since 1.0
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * 在HUD渲染结束时注入音乐播放器HUD
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MusicPlayerMod mod = MusicPlayerMod.getInstance();
        if (!Objects.isNull(mod)) {
            MusicHudRenderer hudRenderer = mod.getHudRenderer();
            if (!Objects.isNull(hudRenderer) && mod.getAudioManager().isPlaying()) {
                hudRenderer.render(context, tickCounter.getDynamicDeltaTicks());
            }
        }
    }
}
