package com.vanphuc.gui.components;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.gui.window.ModuleWindow;
import com.vanphuc.utils.ConfigManager;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class ModuleToggleComponent {
    public ModuleWindow moduleWindow;
    public Rectangle position;

    public ModuleToggleComponent(ModuleWindow moduleWindow, float x, float y, float width, float height) {
        this.moduleWindow = moduleWindow;
        this.position = new Rectangle(x, y, width, height);
    }

    public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        boolean hovered = isHovered(mouseX, mouseY);
        Color bgColor = hovered ? new Color(0xE62A2A2A) : new Color(0xE61E1E1E);

        Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 3f, bgColor);

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, moduleWindow.getTitle(),
                position.getX() + 6, position.getY() + 4, new Color(0xFFFFFFFF));

        String statusBtn = moduleWindow.showInGui ? "-" : "+";
        Color statusColor = moduleWindow.showInGui ? new Color(0xFFE03131) : new Color(0xFF0F4C81);

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, statusBtn,
                position.getX() + position.getWidth() - 12, position.getY() + 4, statusColor);
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            moduleWindow.showInGui = !moduleWindow.showInGui;
            ConfigManager.save();
            return true;
        }
        return false;
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + position.getHeight();
    }
}