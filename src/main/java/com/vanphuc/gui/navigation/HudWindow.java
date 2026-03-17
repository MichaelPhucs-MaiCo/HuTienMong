package com.vanphuc.gui.navigation;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class HudWindow extends Window {
    public enum ResizeMode { None, WidthAndHeight }
    public ResizeMode resizeMode = ResizeMode.None;
    public float minWidth;
    public float minHeight;
    public float maxHeight;
    public boolean enabled = false;

    public HudWindow(String title, float x, float y, float width, float height) {
        super(title, new Rectangle(x, y, width, height));
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        if (!enabled) return;

        // Khi mở ClickGUI -> Chỉ vẽ Nền và Viền, KHÔNG gọi super.draw() để triệt tiêu Title
        if (GuiManager.getInstance().isOpen()) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            com.vanphuc.utils.render.Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 4f, bgColor);
            com.vanphuc.utils.render.Render2D.drawRoundedOutline(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 4f, 1f, currentBorderColor);
        }
    }

    public boolean isVisible() {
        return enabled;
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!enabled) return false;
        boolean handled = super.onMouseClick(mouseX, mouseY, button, pressed);
        if (!pressed && button == 0 && isMoving) {
            com.vanphuc.utils.ConfigManager.save();
        }
        return handled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        com.vanphuc.utils.ConfigManager.save();
    }
}