package com.vanphuc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AntiBlind;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @WrapOperation(
            at = @At(value = "NEW", target = "net/minecraft/client/render/Fog"),
            method = "applyFog"
    )
    private static Fog createTransparentFog(
            float start, float end, FogShape shape,
            float red, float green, float blue, float alpha,
            Operation<Fog> original,
            Camera camera, BackgroundRenderer.FogType fogType, Vector4f color,
            float viewDistance, boolean thickenFog, float tickDelta)
    {
        AntiBlind module = Modules.get().get(AntiBlind.class);

        // Tắt module thì trả về sương mù gốc
        if(module == null || !module.isActive() || fogType != BackgroundRenderer.FogType.FOG_TERRAIN) {
            return original.call(start, end, shape, red, green, blue, alpha);
        }

        // Xuống nước / lava thì vẫn giữ hiệu ứng của nước
        net.minecraft.block.enums.CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        if(cameraSubmersionType != net.minecraft.block.enums.CameraSubmersionType.NONE) {
            return original.call(start, end, shape, red, green, blue, alpha);
        }

        // Xóa bỏ đoạn gọi getFogModifier private rườm rà.
        // Ép sương mù tàng hình luôn (alpha = 0, RGB = 0)
        return original.call(start, end, shape, 0F, 0F, 0F, 0F);
    }

    // Chặn hiệu ứng mù lòa (Blindness / Darkness Warden)
    @Inject(at = @At("HEAD"), method = "getFogModifier", cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> ci) {
        AntiBlind module = Modules.get().get(AntiBlind.class);
        if (module != null && module.isActive()) {
            ci.setReturnValue(null);
        }
    }
}