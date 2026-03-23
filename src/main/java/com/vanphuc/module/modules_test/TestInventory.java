package com.vanphuc.module.modules_test;

import com.vanphuc.module.Module;
import com.vanphuc.utils.InventoryUtils;
import net.minecraft.item.Items;

public class TestInventory extends Module {
    public TestInventory() {
        super("TestInventory", "Bật phát là bốc Kim Cương ném lên Hotbar.", Items.DIAMOND.getDefaultStack());
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null) return;

        // Tìm Kim Cương trong túi
        int slot = InventoryUtils.findItem(Items.DIAMOND);

        if (slot != -1) {
            InventoryUtils.moveToHotbar(slot);
            info("Đã lôi Kim Cương từ slot " + slot + " lên Hotbar thành công 💎");
        } else {
            info("Lục tung túi mà không thấy viên Kim Cương nào ráo 😭");
        }

        // Làm xong việc thì tự tắt Module luôn
        this.toggle();
    }
}