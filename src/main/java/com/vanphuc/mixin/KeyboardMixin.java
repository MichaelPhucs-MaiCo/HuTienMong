package com.vanphuc.mixin;

import com.vanphuc.gui.GuiManager;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
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

        // KHOÁ MỌI PHÍM CỦA MINECRAFT KHI MENU ĐANG MỞ
        if (GuiManager.getInstance().isOpen()) {
            // Danh sách phím ĐƯỢC PHÉP hoạt động (W, A, S, D, Space, Shift, Ctrl)
            boolean isMovementKey = (
                    key == GLFW.GLFW_KEY_W ||
                            key == GLFW.GLFW_KEY_A ||
                            key == GLFW.GLFW_KEY_S ||
                            key == GLFW.GLFW_KEY_D ||
                            key == GLFW.GLFW_KEY_SPACE ||
                            key == GLFW.GLFW_KEY_LEFT_SHIFT ||
                            key == GLFW.GLFW_KEY_LEFT_CONTROL
            );

            // Nếu KHÔNG PHẢI phím di chuyển -> Huỷ luôn lệnh, tránh việc bấm 'E' mở túi đồ hay 'L' mở thành tựu
            if (!isMovementKey) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (GuiManager.getInstance().onChar(codePoint, modifiers)) {
            ci.cancel();
        }
    }
}