package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import net.minecraft.item.Items;

public class AntiBlind extends Module {
    public AntiBlind() {
        super("AntiBlind", "Xóa bỏ sương mù và hiệu ứng mù lòa (Warden)", Items.ENDER_EYE.getDefaultStack());
    }
}