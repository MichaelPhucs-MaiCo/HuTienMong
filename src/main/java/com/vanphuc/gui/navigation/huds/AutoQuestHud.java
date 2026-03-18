package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AutoQuest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class AutoQuestHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public AutoQuestHud(int x, int y) {
        super("AutoQuest HUD", x, y, 220, 20);
        this.minWidth = 180f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks);

        if (!isVisible()) return;

        AutoQuest module = Modules.get().get(AutoQuest.class);
        String valueText = "§l--:--:--";
        int valueColor = 0xFF888888;

        if (module != null && module.isActive()) {
            long secs = module.getRemainingCooldown();

            if (secs > 0) {
                long h = secs / 3600;
                long m = (secs % 3600) / 60;
                long s = secs % 60;

                valueText = String.format("§l%02d:%02d:%02d", h, m, s);

                valueColor = (secs <= 10) ? 0xFFFF5555 : 0xFFFFFF55;
            } else {
                valueText = "§l--:--:--";
                valueColor = 0xFF00AA00;
            }
        }

        int renderX = (int) position.getX() + 6;
        int renderY = (int) position.getY() + 6;

        String prefix = "§lNhận lại nhiệm vụ sau: ";

        context.drawTextWithShadow(MC.textRenderer, prefix, renderX, renderY, 0xFF00AA00);

        int labelWidth = MC.textRenderer.getWidth(prefix);
        context.drawTextWithShadow(MC.textRenderer, valueText, renderX + labelWidth, renderY, valueColor);
    }
}