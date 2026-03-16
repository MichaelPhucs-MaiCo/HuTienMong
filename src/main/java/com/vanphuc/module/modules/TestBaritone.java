package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.utils.BaritoneHelper;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class TestBaritone extends Module {
    public final BooleanSetting goBack = new BooleanSetting("Chạy về tâm", false);
    private Vec3d anchorPos = null;

    public TestBaritone() {
        super("TestBaritone", "Test khả năng vác dép chạy về của Baritone.", Items.IRON_BOOTS.getDefaultStack());
        addSetting(goBack);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player != null) {
            // Lưu lại vị trí lúc vừa bật
            anchorPos = mc.player.getPos();
            info("Đã lưu tọa độ. Hãy đi xa rồi gạt nút 'Chạy về tâm' nhé 🏃💨");
        }
    }

    @Override
    public void onDeactivate() {
        BaritoneHelper.stop();
        anchorPos = null;
        goBack.setValue(false); // Reset lại nút
        super.onDeactivate();
    }

    // Hàm onUpdate này cậu gọi từ HuTienMongClient giống thằng AutoClicker nhé
    public void onUpdate() {
        if (!isActive() || mc.player == null || anchorPos == null) return;

        // Nếu cậu gạt cái Switch "Chạy về tâm" trong Menu
        if (goBack.getValue()) {
            info("Baritone đã nhận lệnh, đang phi về tâm...");

            // Gọi lệnh di chuyển
            BaritoneHelper.goTo((int) anchorPos.x, (int) anchorPos.y, (int) anchorPos.z);

            // QUAN TRỌNG: Tắt ngay cái công tắc đi để Baritone không bị kẹt vì nhận lệnh liên tục
            goBack.setValue(false);
        }
    }
}