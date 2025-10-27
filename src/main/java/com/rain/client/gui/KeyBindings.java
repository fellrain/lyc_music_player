package com.rain.client.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * 键盘绑定管理
 *
 * @author 落雨川
 * @version 1.5
 * @since 1.5
 */
public class KeyBindings {

    private static KeyBinding openGuiKey;

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
        Identifier.of("lyc_music_player", "music_player")
    );

    /**
     * 注册所有键盘绑定
     */
    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lycMusicPlayer.openGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                client.setScreen(new MusicPlayerScreen());
            }
        });
    }
}
