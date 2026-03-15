package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class SliderComponent extends UIElement {
    private final NumberSetting setting;
    private boolean dragging = false;

    private final Color bgColor = new Color(0.117f, 0.117f, 0.117f, 0.5f);
    private final Color accentColor = new Color(0xFF3B82F6);
    private final Color textColor = new Color(0xFFFFFFFF);
    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f);

    public SliderComponent(NumberSetting setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float padding = 8f;

        if (isHovered || dragging) {
            Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(),
                    position.getHeight(), 3f, hoverColor);
        }

        String nameText = setting.getName();
        String valueText = String.format("%.1f", setting.getValue());

        // Vẽ Text ở phía trên (Y + 4)
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                nameText, position.getX() + padding, position.getY() + 4f, textColor);

        float valWidth = MinecraftClient.getInstance().textRenderer.getWidth(valueText);
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                valueText, position.getX() + position.getWidth() - valWidth - padding, position.getY() + 4f,
                accentColor);

        float barX = position.getX() + padding;
        // ĐÃ SỬA: Đẩy thanh kéo tụt xuống dưới (Y + 16), cách xa chữ
        float barY = position.getY() + 16f;
        float barW = position.getWidth() - (padding * 2);
        float barH = 4f;

        Render2D.drawSmoothRoundedBox(matrix, barX, barY, barW, barH, 2f, bgColor);

        double range = setting.getMax() - setting.getMin();
        double current = setting.getValue() - setting.getMin();
        float percent = (float) (current / range);

        if (percent > 0.01f) {
            Render2D.drawSmoothRoundedBox(matrix, barX, barY, barW * percent, barH, 2f, accentColor);
        }

        float handleX = barX + (barW * percent) - 2f;
        handleX = Math.max(barX - 1f, handleX);
        Render2D.drawSmoothRoundedBox(matrix, handleX, barY - 1, 4f, 6f, 2f, textColor);

        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (button == 0) {
            if (pressed && isHovered) {
                dragging = true;
                updateValue(mouseX);
                return true;
            } else if (!pressed && dragging) {
                dragging = false;
                return true;
            }
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        super.onMouseMove(mouseX, mouseY);
        if (dragging) {
            updateValue(mouseX);
        }
    }

    private void updateValue(double mouseX) {
        float padding = 8f;
        float barX = position.getX() + padding;
        float barW = position.getWidth() - (padding * 2);
        double percent = (mouseX - barX) / barW;
        percent = Math.clamp(percent, 0.0, 1.0);
        double range = setting.getMax() - setting.getMin();
        double newValue = setting.getMin() + (percent * range);
        setting.setValue(newValue);
    }

    @Override
    public void measure(com.vanphuc.gui.Size availableSize) {
        this.position.setWidth(availableSize.getWidth());
        this.position.setHeight(availableSize.getHeight());
        super.measure(availableSize);
    }
}