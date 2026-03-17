package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AutoSavePaper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class AutoSavePaperHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public AutoSavePaperHud(int x, int y) {
        super("AutoSavePaper HUD", x, y, 140, 20);
        this.minWidth = 80f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks);

        if (!isVisible()) return;

        AutoSavePaper module = Modules.get().get(AutoSavePaper.class);

        String valueText;
        int valueColor;

        if (module != null && module.isActive()) {
            long secs = module.getRemainingSeconds(); // <- method must be public in AutoSavePaper
            if (secs < 0) {
                valueText = "--";
                valueColor = 0xFF888888;
            } else {
                long mm = secs / 60;
                long ss = secs % 60;
                valueText = String.format("%02d:%02d", mm, ss);
                valueColor = (secs <= 10) ? 0xFFFF5555 : 0xFF0F4C81;
            }
        } else {
            valueText = "--";
            valueColor = 0xFF888888;
        }

        int renderX = (int) position.getX() + 6;
        int renderY = (int) position.getY() + 6;

        context.drawTextWithShadow(MC.textRenderer, "AutoSavePaper: ", renderX, renderY, 0xFFCCCCCC);
        int labelWidth = MC.textRenderer.getWidth("AutoSavePaper: ");
        context.drawTextWithShadow(MC.textRenderer, valueText, renderX + labelWidth, renderY, valueColor);
    }
}