package com.vanphuc.mixin;

import com.vanphuc.module.modules.NoWeather;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "getRainGradient(F)F", at = @At("HEAD"), cancellable = true)
    private void onGetRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (NoWeather.getInstance() != null && NoWeather.getInstance().isRainDisabled()) {
            cir.setReturnValue(0F);
        }
    }

    @Inject(method = "getTimeOfDay()J", at = @At("HEAD"), cancellable = true)
    private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (NoWeather.getInstance() != null && NoWeather.getInstance().isTimeChanged()) {
            cir.setReturnValue(NoWeather.getInstance().getChangedTime());
        }
    }
}