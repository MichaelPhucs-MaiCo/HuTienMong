package com.vanphuc.module.modules_test;

import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.utils.render.RenderWorldUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class TestRender extends Module {
    public final NumberSetting radius = new NumberSetting("Bán kính", 5.0, 1.0, 20.0);
    private Vec3d anchorPos = null;

    public TestRender() {
        super("TestRender", "Kiểm tra chốt tâm và vẽ vòng tròn 3D.", Items.ENDER_EYE.getDefaultStack());
        addSetting(radius);

        // Đăng ký sự kiện vẽ 3D ngay trong module cho nó độc lập
        WorldRenderEvents.LAST.register(context -> {
            if (isActive() && anchorPos != null) {
                // Vẽ màu Sleek Carbon: Xanh Blue nhấn #0F4C81
                RenderWorldUtils.drawCircle(context.matrixStack(), anchorPos, radius.getValue(), new Color(0xFF0F4C81));
            }
        });
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player != null) {
            // Chốt tâm đúng 1 lần khi Bật
            anchorPos = mc.player.getPos();
            info("Đã chốt tâm vẽ vòng tròn tại: " + Math.round(anchorPos.x) + ", " + Math.round(anchorPos.z));
        }
    }

    @Override
    public void onDeactivate() {
        // Tắt đi thì xóa tâm
        anchorPos = null;
        super.onDeactivate();
    }
}