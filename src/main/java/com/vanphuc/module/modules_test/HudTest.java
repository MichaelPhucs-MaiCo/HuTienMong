package com.vanphuc.module.modules_test;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import net.minecraft.item.Items;

public class HudTest extends Module {
    // Setting thời gian đếm ngược, mặc định 120s
    public final NumberSetting timeSetting = new NumberSetting("Thời gian (s)", 120.0, 1.0, 3600.0);
    private long endTime = 0;

    public HudTest() {
        super("HudTest", "Đếm ngược thời gian, hết giờ tự cúp cầu dao ⏳", Items.CLOCK.getDefaultStack());
        addSetting(timeSetting);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        // Lấy thời gian hiện tại cộng thêm số giây cài đặt (đổi ra mili-giây)
        endTime = System.currentTimeMillis() + (long)(timeSetting.getValue() * 1000);
        info("Bắt đầu đếm ngược " + timeSetting.getValue().intValue() + "s!");
    }

    @Override
    public void onUpdate() {
        if (!isActive()) return;

        // Kiểm tra xem đã hết giờ chưa
        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            info("Hết giờ rồi ông cháu ơi! Tự tắt module đây ⏰");
            toggle(); // Tự động cúp cầu dao
        }
    }

    // Hàm public này để cho cái HUD kia gọi sang lấy số giây còn lại
    public long getRemainingSeconds() {
        if (!isActive()) return 0;
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
    }
}