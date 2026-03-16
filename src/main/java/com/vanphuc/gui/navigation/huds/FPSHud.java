package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FPSHud extends HudWindow {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public FPSHud(int x, int y) {
        super("FPSHud", x, y, 50, 24);
        this.minWidth = 50f;
        this.minHeight = 20f;
        this.maxHeight = 20f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks); // Vẽ nền mờ kéo thả khi ở ClickGUI

        if (isVisible()) {
            Rectangle pos = position;
            int fps = MC.getCurrentFps();
            String fpsText = "FPS: " + fps;
            // Áp dụng màu Xanh Blue #0F4C81
            Render2D.drawString(context, MC.textRenderer, fpsText, pos.getX() + 6, pos.getY() + 6, new Color(0x0F4C81));
        }
    }
}