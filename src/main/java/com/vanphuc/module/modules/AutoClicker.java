package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;


public class AutoClicker extends Module {
    private long lastSwingTime = 0L;

    // Setting giống hệt code mẫu ông gửi
    public final NumberSetting speedMs = new NumberSetting("Tốc độ (ms)", 310.0, 1.0, 1000.0);

    public AutoClicker() {
        super("AutoClicker", "Tự động vung tay và chém quái.", Items.GOLDEN_SWORD.getDefaultStack());
        addSetting(speedMs);
    }

    @Override
    public void onDeactivate() {
        // Khi tắt module phải nhả chuột ra, không là nó cứ chém mãi
        mc.options.attackKey.setPressed(false);
        super.onDeactivate();
    }

    public void onUpdate() {
        if (!isActive() || mc.player == null || mc.currentScreen != null) {
            if (isActive()) mc.options.attackKey.setPressed(false);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwingTime >= speedMs.getValue().longValue()) {
            lastSwingTime = currentTime;

            // 1. Vung tay (Giống code mẫu của ông)
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            // Nhả chuột ra để chuẩn bị cho lần click sau
            mc.options.attackKey.setPressed(false);
        }
    }
}