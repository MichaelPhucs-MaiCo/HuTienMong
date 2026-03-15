package com.vanphuc.mixin;

import com.vanphuc.gui.GuiManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (GuiManager.getInstance().isOpen()) {
            double mouseX = com.vanphuc.utils.render.RenderUtils.getMouseX();
            double mouseY = com.vanphuc.utils.render.RenderUtils.getMouseY();
            GuiManager.getInstance().onMouseClick(mouseX, mouseY, button, action == 1);
            ci.cancel();
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (GuiManager.getInstance().isOpen()) {
            double mouseX = com.vanphuc.utils.render.RenderUtils.getMouseX();
            double mouseY = com.vanphuc.utils.render.RenderUtils.getMouseY();
            GuiManager.getInstance().onMouseMove(mouseX, mouseY);
        }
    }
}
