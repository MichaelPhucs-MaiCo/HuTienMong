package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.KeybindSetting;
import net.minecraft.item.Items;

public class CopyDataComp extends Module {
    // Công tắc mặc định bật: Cuộn chuột = Click trái
    public final BooleanSetting useScrollToClick = new BooleanSetting("Cuộn chuột = L-Click", true);

    // Nút dự phòng nếu không thích cuộn chuột
    public final KeybindSetting altClickKey = new KeybindSetting("Phím L-Click phụ", -1, this);

    public CopyDataComp() {
        super("CopyDataComp", "Copy Data Item. Cuộn chuột hoặc dùng phím phụ thay thế cho chuột trái 📋", Items.PAPER.getDefaultStack());
        addSetting(useScrollToClick);
        addSetting(altClickKey);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        info("§a§lĐÃ BẬT! §fChuột trái để Copy. Cuộn chuột/Phím phụ để lụm/vứt đồ bình thường.");
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        info("§c§lĐÃ TẮT!.");
    }
}