package com.vanphuc.mixin;

import com.vanphuc.module.modules.NoWeather;
import net.minecraft.world.LunarWorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LunarWorldView.class)
public interface LunarWorldViewMixin {

    @Inject(method = "getMoonPhase()I", at = @At("HEAD"), cancellable = true)
    default void onGetMoonPhase(CallbackInfoReturnable<Integer> cir) {
        if (NoWeather.getInstance() != null && NoWeather.getInstance().isMoonPhaseChanged()) {
            cir.setReturnValue(NoWeather.getInstance().getChangedMoonPhase());
        }
    }
}