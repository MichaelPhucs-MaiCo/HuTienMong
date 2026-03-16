package com.vanphuc.gui.navigation;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class NavigationBar {
    public Rectangle position;
    private final float tabWidth = 100f; // Bằng đúng width của module

    public NavigationBar() {
        // Tọa độ y = 10. Width = 200 (cho 2 tab, mỗi tab 100). Height = 20 (bằng height module)
        this.position = new Rectangle(0, 10, 200, 20);
    }

    public void draw(DrawContext context) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        position.setX((screenWidth - position.getWidth()) / 2f); // Luôn căn giữa màn hình

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Background chính Sleek Carbon #121212
        Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 4f, new Color(0xE6121212));
        // Surface/Card #1E1E1E (viền mỏng hơn cho gọn)
        Render2D.drawSmoothRoundedBox(matrix, position.getX() + 1, position.getY() + 1, position.getWidth() - 2, position.getHeight() - 2, 3f, new Color(0xE61E1E1E));

        float currentX = position.getX();
        for (Page page : GuiManager.getInstance().pages) {
            boolean isActive = (GuiManager.getInstance().activePage == page);
            // Màu nhấn Xanh Blue #0F4C81, chữ xám #888888
            Color textColor = isActive ? new Color(0xFF0F4C81) : new Color(0xFF888888);

            // Căn giữa text trong cái tab 100px
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(page.name);
            float textX = currentX + (tabWidth - textWidth) / 2f;
            float textY = position.getY() + 6; // Hạ text xuống một chút cho vừa với height 20

            Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, page.name, textX, textY, textColor);
            currentX += tabWidth;
        }
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + position.getHeight()) {

            float currentX = position.getX();
            for (Page page : GuiManager.getInstance().pages) {
                if (mouseX >= currentX && mouseX <= currentX + tabWidth) {
                    GuiManager.getInstance().activePage = page;
                    return true;
                }
                currentX += tabWidth;
            }
        }
        return false;
    }
}