package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class ButtonComponent extends UIElement {
    private final ActionSetting setting;
    private final Color bgColor = new Color(0xFF1E1E1E); // Card/Surface Sleek Carbon
    private final Color hoverColor = new Color(0xFF0F4C81); // Màu nhấn Xanh Blue
    private final Color textColor = new Color(0xFFFFFFFF);

    public ButtonComponent(ActionSetting setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        Color currentColor = isHovered ? hoverColor : bgColor;

        // Vẽ nút bo góc mượt mà
        Render2D.drawSmoothRoundedBox(matrix, position.getX() + 4, position.getY() + 2, position.getWidth() - 8, position.getHeight() - 4, 3f, currentColor);

        // Vẽ Text căn giữa
        float textWidth = MinecraftClient.getInstance().textRenderer.getWidth(setting.getName());
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, setting.getName(),
                position.getX() + (position.getWidth() - textWidth) / 2f,
                position.getY() + (position.getHeight() - 8) / 2f, textColor);

        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (isHovered && pressed && button == 0) {
            setting.execute(); // Chạy hàm khi Click
            return true;
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    @Override
    public void measure(com.vanphuc.gui.Size availableSize) {
        this.position.setWidth(availableSize.getWidth());
        this.position.setHeight(availableSize.getHeight());
        super.measure(availableSize);
    }
}