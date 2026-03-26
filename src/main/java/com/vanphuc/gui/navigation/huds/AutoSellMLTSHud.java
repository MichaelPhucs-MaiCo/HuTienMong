package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AutoSellMLTS;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class AutoSellMLTSHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public AutoSellMLTSHud(int x, int y) {
        super("AutoSellMLTS HUD", x, y, 140, 20);
        this.minWidth = 80f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks);

        if (!isVisible()) return;

        AutoSellMLTS module = Modules.get().get(AutoSellMLTS.class);

        String valueText;
        int valueColor;

        if (module != null && module.isActive()) {
            long secs = module.getRemainingSeconds();

            // Nếu secs == 0 tức là đang trong quá trình thực hiện các Phase click
            if (secs <= 0 && secs != -1) {
                // Kiểm tra xem có đang thực hiện Phase bán không
                valueText = "§lRUNNING...";
                valueColor = 0xFF0F4C81; // Màu xanh nhấn (Blue)
            } else if (secs == -1) {
                valueText = "§l--";
                valueColor = 0xFF888888;
            } else {
                long mm = secs / 60;
                long ss = secs % 60;
                valueText = String.format("§l%02d:%02d", mm, ss);

                // Đổi màu đỏ nếu còn dưới 10 giây đúng ý Khầy
                valueColor = (secs <= 10) ? 0xFFFF5555 : 0xFF0F4C81;
            }
        } else {
            valueText = "§lOFF";
            valueColor = 0xFF888888;
        }

        int renderX = (int) position.getX() + 6;
        int renderY = (int) position.getY() + 6;

        // Vẽ Label chính
        context.drawTextWithShadow(MC.textRenderer, "§lAutoSell MLTS: ", renderX, renderY, 0xFFCCCCCC);

        // Vẽ giá trị thời gian đằng sau Label
        int labelWidth = MC.textRenderer.getWidth("§lAutoSell MLTS: ");
        context.drawTextWithShadow(MC.textRenderer, valueText, renderX + labelWidth, renderY, valueColor);
    }
}