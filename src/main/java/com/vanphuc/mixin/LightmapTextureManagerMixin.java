package com.vanphuc.mixin;

import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AntiBlind;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    // Chặn màn hình bị tối đen nhấp nháy của Warden
    @Inject(at = @At("HEAD"), method = "getDarknessFactor(F)F", cancellable = true)
    private void onGetDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> cir) {
        AntiBlind module = Modules.get().get(AntiBlind.class);
        if (module != null && module.isActive()) {
            cir.setReturnValue(0F);
        }
    }
}