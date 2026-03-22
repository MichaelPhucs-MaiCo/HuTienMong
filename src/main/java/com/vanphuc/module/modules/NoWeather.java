package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.NumberSetting;
import net.minecraft.item.Items;

public class NoWeather extends Module {
    public final BooleanSetting disableRain = new BooleanSetting("Tắt mưa", true);
    public final BooleanSetting changeTime = new BooleanSetting("Thay đổi thời gian", false);
    public final NumberSetting time = new NumberSetting("Thời gian", 6000.0, 0.0, 23900.0);
    public final BooleanSetting changeMoonPhase = new BooleanSetting("Thay đổi pha mặt trăng", false);
    public final NumberSetting moonPhase = new NumberSetting("Pha mặt trăng", 0.0, 0.0, 7.0);

    // Dùng Singleton siêu mỏng nhẹ để Mixin gọi sang cho lẹ
    private static NoWeather instance;

    public NoWeather() {
        super("NoWeather", "Thay đổi thời tiết, thời gian và mặt trăng", Items.ENDER_EYE.getDefaultStack());
        addSetting(disableRain);
        addSetting(changeTime);
        addSetting(time);
        addSetting(changeMoonPhase);
        addSetting(moonPhase);
        instance = this;
    }

    public static NoWeather getInstance() {
        return instance;
    }

    public boolean isRainDisabled() {
        return isActive() && disableRain.getValue();
    }

    public boolean isTimeChanged() {
        return isActive() && changeTime.getValue();
    }

    public long getChangedTime() {
        return time.getValue().longValue();
    }

    public boolean isMoonPhaseChanged() {
        return isActive() && changeMoonPhase.getValue();
    }

    public int getChangedMoonPhase() {
        return moonPhase.getValue().intValue();
    }
}