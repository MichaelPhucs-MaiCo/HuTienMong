package com.vanphuc.module.modules_test;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.EnumSetting;
import net.minecraft.item.Items;

public class TestDropdown extends Module {

    public enum Mode {
        CheDo1, CheDo2, CheDo3
    }

    public final EnumSetting<Mode> modeSetting = new EnumSetting<>("Chế độ", Mode.CheDo1);

    public TestDropdown() {
        super("DropdownTest", "Test setting kiểu dropdown thả xuống 📦", Items.CHEST.getDefaultStack());
        addSetting(modeSetting);
    }

    @Override
    public void onUpdate() {
        if (!isActive()) return;
        // Thực hiện logic dựa trên chế độ đã chọn
        switch (modeSetting.getValue()) {
            case CheDo1 -> { /* Logic 1 */ }
            case CheDo2 -> { /* Logic 2 */ }
            case CheDo3 -> { /* Logic 3 */ }
        }
    }
}