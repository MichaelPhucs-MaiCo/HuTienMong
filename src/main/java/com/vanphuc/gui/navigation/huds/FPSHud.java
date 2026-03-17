package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.navigation.HudWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FPSHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public FPSHud(int x, int y) {
        // Trả về size 60x20
        super("FPSHud", x, y, 60, 20);
        this.minWidth = 50f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks);

        if (isVisible()) {
            Rectangle pos = position;
            int fps = MC.getCurrentFps();
            String fpsText = "FPS: " + fps;

            int drawX = (int) pos.getX() + 6;
            int drawY = (int) pos.getY() + 6; // Luôn chốt ở y + 6

            drawTextWithShadow(context, fpsText, drawX, drawY, 0xFF0F4C81);
        }
    }

    private void drawTextWithShadow(DrawContext context, String text, int x, int y, int color) {
        context.drawText(MC.textRenderer, text, x + 1, y + 1, 0x3F000000, false);
        context.drawText(MC.textRenderer, text, x, y, color, false);
    }
}