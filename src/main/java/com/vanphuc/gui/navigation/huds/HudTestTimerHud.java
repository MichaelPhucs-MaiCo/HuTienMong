package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.HudTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudTestTimerHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public HudTestTimerHud(int x, int y) {
        // Chiều cao 20, rộng 70 là vừa zin
        super("Timer HUD", x, y, 70, 20);
        this.minWidth = 50f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        // Gọi lên HudWindow để nó vẽ cái khung mờ (không có Title)
        super.draw(context, partialTicks);

        if (isVisible()) {
            Rectangle pos = position;
            HudTest module = Modules.get().get(HudTest.class);

            int labelColor = 0xFFCCCCCC; // Trắng xám
            int valueColor;
            String valueText;

            if (module != null && module.isActive()) {
                long secs = module.getRemainingSeconds();
                valueText = "§l" + secs + "s";
                // Dưới 10s đỏ, bình thường Xanh Blue Sleek Carbon
                valueColor = (secs <= 10) ? 0xFFFF5555 : 0xFF0F4C81;
            } else {
                valueText = "§l--";
                valueColor = 0xFF888888;
            }

            int renderX = (int) pos.getX() + 6;
            int renderY = (int) pos.getY() + 6; // Giờ thì cứ chốt thẳng ở y + 6 thôi

            context.drawTextWithShadow(MC.textRenderer, "§lTime: ", renderX, renderY, labelColor);
            int labelWidth = MC.textRenderer.getWidth("§lTime: ");
            context.drawTextWithShadow(MC.textRenderer, valueText, renderX + labelWidth, renderY, valueColor);
        }
    }
}