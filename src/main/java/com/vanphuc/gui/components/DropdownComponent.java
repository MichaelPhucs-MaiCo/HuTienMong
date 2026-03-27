package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.EnumSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class DropdownComponent<T extends Enum<T>> extends UIElement {
    private final EnumSetting<T> setting;
    private boolean expanded = false;

    private final Color bgColor = new Color(0xCC121212);
    private final Color surfaceColor = new Color(0xFF1E1E1E);
    private final Color accentColor = new Color(0xFF0F4C81);
    private final Color textColor = new Color(0xFFFFFFFF);
    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f);

    public DropdownComponent(EnumSetting<T> setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        if (!visible) return;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float padding = 8f;

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                setting.getName(), position.getX() + padding, position.getY() + 6, textColor);

        float boxW = 80f;
        float boxX = position.getX() + position.getWidth() - boxW - padding;
        float boxY = position.getY() + 3;

        Render2D.drawSmoothRoundedBox(matrix, boxX, boxY, boxW, 14, 3f, surfaceColor);
        String currentText = setting.getValue().name();
        float textW = MinecraftClient.getInstance().textRenderer.getWidth(currentText);
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                currentText, boxX + (boxW - textW) / 2f, boxY + 3, accentColor);

        if (expanded) {
            T[] values = setting.getValues();
            float optionY = boxY + 16;
            Render2D.drawSmoothRoundedBox(matrix, boxX, optionY, boxW, values.length * 14f, 3f, bgColor);

            for (int i = 0; i < values.length; i++) {
                T val = values[i];
                float currentOptY = optionY + (i * 14);
                if (isMouseOver(boxX, currentOptY, boxW, 14)) {
                    Render2D.drawBox(matrix, boxX, currentOptY, boxW, 14, hoverColor);
                }
                String valName = val.name();
                float valW = MinecraftClient.getInstance().textRenderer.getWidth(valName);
                Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                        valName, boxX + (boxW - valW) / 2f, currentOptY + 3,
                        val == setting.getValue() ? accentColor : textColor);
            }
        }
        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        // KIỂM TRA CHUỘT CÓ NẰM TRONG VÙNG CỦA COMPONENT KHÔNG
        boolean hovered = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + position.getHeight();

        if (pressed && button == 0) {
            float boxW = 80f;
            float boxX = position.getX() + position.getWidth() - 8f - boxW;
            float boxY = position.getY() + 3;

            // Click vào box chính
            if (isMouseOver(boxX, boxY, boxW, 14)) {
                expanded = !expanded;
                if (parent != null) parent.arrange(parent.getPosition());
                return true; // Khóa click
            }

            // Click vào option
            if (expanded) {
                T[] values = setting.getValues();
                for (int i = 0; i < values.length; i++) {
                    if (isMouseOver(boxX, boxY + 16 + (i * 14), boxW, 14)) {
                        setting.setValue(values[i]);
                        expanded = false;
                        if (parent != null) parent.arrange(parent.getPosition());
                        return true; // Khóa click
                    }
                }
            }

            // FIX QUAN TRỌNG: Nếu đang mở rộng và click vào vùng linh kiện,
            // dù không trúng nút cũng phải "hút" click để không bị bấm xuyên thấu xuống dưới.
            if (hovered) return true;
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    private boolean isMouseOver(float x, float y, float w, float h) {
        double mx = com.vanphuc.utils.render.RenderUtils.getMouseX();
        double my = com.vanphuc.utils.render.RenderUtils.getMouseY();
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void measure(com.vanphuc.gui.Size availableSize) {
        this.position.setWidth(availableSize.getWidth());
        float h = expanded ? 24f + (setting.getValues().length * 14f) : 24f;
        this.position.setHeight(h);
    }
}