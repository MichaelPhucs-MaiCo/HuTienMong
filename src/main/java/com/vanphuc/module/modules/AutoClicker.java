package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;

public class AutoClicker extends Module {
    // Biến lưu thời gian riêng biệt cho 2 hành động
    private long lastLeftSwingTime = 0L;
    private long lastRightSwingTime = 0L;

    // --- CẤU HÌNH CHUỘT TRÁI (Vung ảo lấy sát thương) ---
    public final BooleanSetting enableLeft = new BooleanSetting("Bật Chuột Trái", true);
    public final NumberSetting speedLeftMs = new NumberSetting("Delay Trái (ms)", 310.0, 1.0, 1000.0);

    // --- CẤU HÌNH CHUỘT PHẢI (Click thật tung skill) ---
    public final BooleanSetting enableRight = new BooleanSetting("Bật Chuột Phải", false);
    // Skill cooldown lâu nên tớ để max là 60000ms (60s), mặc định 30000ms cho skill Thiên Thủ luôn
    public final NumberSetting speedRightMs = new NumberSetting("Delay Phải (ms)", 30000.0, 1.0, 60000.0);

    public AutoClicker() {
        super("AutoClicker", "Autoclick chuột trái phải", Items.GOLDEN_SWORD.getDefaultStack());

        addSetting(enableLeft);
        addSetting(speedLeftMs);
        addSetting(enableRight);
        addSetting(speedRightMs);
    }

    @Override
    public void onDeactivate() {
        // Trả lại sự trong sạch cho con chuột khi tắt
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
        super.onDeactivate();
    }

    @Override
    public void onUpdate() {
        // CHỈ cần check module có đang bật hay không và người chơi có tồn tại không
        if (!isActive() || mc.player == null) return;

        long currentTime = System.currentTimeMillis();

        // ================== LOGIC CHUỘT TRÁI (VUNG ẢO MAIN_HAND) ==================
        if (enableLeft.isEnabled()) {
            if (currentTime - lastLeftSwingTime >= speedLeftMs.getValue().longValue()) {
                lastLeftSwingTime = currentTime;
                // Giữ nguyên mc.player.swingHand để chỉ vung tay ảo, không gây damage thật
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }

        // ================== LOGIC CHUỘT PHẢI (CLICK THẬT MAIN_HAND) ==================
        if (enableRight.isEnabled()) {
            if (currentTime - lastRightSwingTime >= speedRightMs.getValue().longValue()) {
                lastRightSwingTime = currentTime;

                // Nhấp phải "real" 100% bằng InteractionManager để kích hoạt skill vũ khí
                if (mc.interactionManager != null) {
                    ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

                    // Nếu xài skill thành công thì game sẽ tự động vung tay cho hợp logic,
                    // nhưng nếu muốn chắc kèo có animation thì bật dòng dưới lên
                    if (result.isAccepted()) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        }
    }
}