package com.vanphuc.gui.components;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class HudComponent {
    public HudWindow hud;
    public Rectangle position;

    public HudComponent(HudWindow hud, float x, float y, float width, float height) {
        this.hud = hud;
        this.position = new Rectangle(x, y, width, height);
    }

    public void draw(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Hiệu ứng Hover: Nếu rê chuột vào thì màu nền sáng lên một xíu
        boolean hovered = isHovered(mouseX, mouseY);
        Color bgColor = hovered ? new Color(0xE62A2A2A) : new Color(0xE61E1E1E);

        Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 3f, bgColor);

        // Vẽ tên HUD (Màu trắng)
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, hud.getTitle(),
                position.getX() + 6, position.getY() + 4, new Color(0xFFFFFFFF));

        // Vẽ trạng thái (+ hoặc -)
        String statusBtn = hud.enabled ? "-" : "+";
        // Màu Đỏ trầm khi Bật, Xanh lá trầm khi Tắt (Không dùng Neon chói mắt)
        Color statusColor = hud.enabled ? new Color(0xFFE03131) : new Color(0xFF0CA678);

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, statusBtn,
                position.getX() + position.getWidth() - 12, position.getY() + 4, statusColor);
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        // Chuột trái vào component thì bật/tắt HUD đó
        if (button == 0 && isHovered(mouseX, mouseY)) {
            hud.toggle();
            return true;
        }
        return false;
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + position.getHeight();
    }
}