package com.vanphuc.mixin;

import com.vanphuc.gui.GuiManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        // Luôn thử pass key vào GuiManager trước
        if (GuiManager.getInstance().onKey(key, action, mods)) {
            ci.cancel(); return;
        }

        // Nếu GUI đang mở, hãy cancel phím ESC và GRAVE để tránh mở Menu Minecraft
        // hoặc bị conflict phím (đã được xử lý trong GuiManager nếu cần)
        if (GuiManager.getInstance().isOpen()) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT) {
                if (action == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    ci.cancel();
                }
            }
        }
    }
}
