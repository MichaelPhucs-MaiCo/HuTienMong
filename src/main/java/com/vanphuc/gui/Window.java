package com.vanphuc.gui;

import com.vanphuc.gui.colors.Color;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class Window extends UIElement {
    private String title;
    protected boolean isMoving = false;
    protected double lastMouseX, lastMouseY;

    protected final float titleHeight = 18f;

    // Biến lưu tọa độ thực tế (không làm tròn) để tránh trôi cửa sổ
    protected double exactX, exactY;

    protected final Color bgColor = new Color(0.07f, 0.07f, 0.07f, 0.6f);
    protected Color currentBorderColor = new Color(0xFF3B82F6);

    public Window(String title, Rectangle position) {
        this.title = title;
        this.position = position;
        // FIX LỖI NHẢY CỬA SỔ: Khởi tạo tọa độ exactX và exactY ngay từ đầu
        this.exactX = position.getX();
        this.exactY = position.getY();
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(),
                position.getHeight(), 4f, bgColor);

        Render2D.drawRoundedOutline(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(),
                4f, 1f, currentBorderColor);

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, title, position.getX() + 6,
                position.getY() + 5, currentBorderColor);

        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        boolean inTitle = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + titleHeight;

        if (button == 0 && pressed && inTitle) {
            isMoving = true;
            com.vanphuc.utils.ClientConfig.isAnyWindowMoving = true;

            // FIX: Cập nhật lại vị trí thực tế trước khi bắt đầu kéo để tránh giật lag
            exactX = position.getX();
            exactY = position.getY();

            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        } else if (button == 0 && !pressed) {
            isMoving = false;
            com.vanphuc.utils.ClientConfig.isAnyWindowMoving = false;
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed) || inTitle;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        if (isMoving) {
            // FIX LỖI KÉO CHUỘT: Tính toán độ dời (delta)
            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            exactX += deltaX;
            exactY += deltaY;

            double targetX = exactX;
            double targetY = exactY;

            if (com.vanphuc.utils.ClientConfig.snapping) {
                int size = com.vanphuc.utils.ClientConfig.gridSize;
                targetX = Math.round(exactX / size) * size;
                targetY = Math.round(exactY / size) * size;
            }

            position.setX((float) targetX);
            position.setY((float) targetY);

            this.arrange(this.position);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        super.onMouseMove(mouseX, mouseY);
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        return super.onChar(codePoint, modifiers);
    }

    @Override
    public boolean onKey(int key, int action, int mods) {
        return super.onKey(key, action, mods);
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}