package com.vanphuc.mixin;

import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AntiBlind;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    // Trả lại bầu trời thay vì để nó đen kịt
    @Inject(at = @At("HEAD"), method = "hasBlindnessOrDarkness", cancellable = true)
    private void onHasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> ci) {
        AntiBlind module = Modules.get().get(AntiBlind.class);
        if (module != null && module.isActive()) {
            ci.setReturnValue(false);
        }
    }
}