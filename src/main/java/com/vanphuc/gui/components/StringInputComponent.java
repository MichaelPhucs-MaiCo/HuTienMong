package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.StringSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class StringInputComponent extends UIElement {
    private final StringSetting setting;
    private boolean isEditing = false;
    private String editText = "";

    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f);
    private final Color boxBgColor = new Color(0f, 0f, 0f, 0.4f);
    private final Color textColor = new Color(0xFFFFFFFF);
    private final Color editColor = new Color(0xFFF1C40F);

    public StringInputComponent(StringSetting setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        if (!visible) return;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float padding = 8f;

        if (isHovered) {
            Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(),
                    position.getHeight(), 3f, hoverColor);
        }

        String nameText = setting.getName();
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                nameText, position.getX() + padding, position.getY() + 4f, textColor);

        String displayText = isEditing ? editText + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "") : setting.getValue();
        Color currentColor = isEditing ? editColor : textColor;

        float textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        float boxW = Math.max(50f, textWidth + 12f);
        float boxH = 14f;
        float boxX = position.getX() + position.getWidth() - boxW - padding;
        float boxY = position.getY() + 2f;

        Render2D.drawSmoothRoundedBox(matrix, boxX, boxY, boxW, boxH, 2f, boxBgColor);
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                displayText, boxX + 6f, boxY + 3f, currentColor);

        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        float padding = 8f;
        String currentText = isEditing ? editText : setting.getValue();
        float textWidth = MinecraftClient.getInstance().textRenderer.getWidth(currentText);

        float boxW = Math.max(50f, textWidth + 12f);
        float boxX = position.getX() + position.getWidth() - boxW - padding;
        float boxY = position.getY() + 2f;
        float boxH = 14f;

        boolean clickOnBox = mouseX >= boxX && mouseX <= boxX + boxW &&
                mouseY >= boxY && mouseY <= boxY + boxH;

        if (button == 0 && pressed) {
            if (clickOnBox) {
                if (!isEditing) {
                    isEditing = true;
                    editText = setting.getValue();
                }
                return true;
            } else {
                if (isEditing) {
                    applyEdit();
                }
            }
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    @Override
    public boolean onKey(int key, int action, int mods) {
        if (isEditing) {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
                    applyEdit();
                } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!editText.isEmpty()) {
                        editText = editText.substring(0, editText.length() - 1);
                    }
                }
            }
            return true;
        }
        return super.onKey(key, action, mods);
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        if (isEditing) {
            if (codePoint >= 32 && codePoint != 127) {
                editText += (char) codePoint;
            }
            return true;
        }
        return super.onChar(codePoint, modifiers);
    }

    private void applyEdit() {
        isEditing = false;
        setting.setValue(editText);
    }

    @Override
    public void measure(com.vanphuc.gui.Size availableSize) {
        this.position.setWidth(availableSize.getWidth());
        this.position.setHeight(availableSize.getHeight());
        super.measure(availableSize);
    }
}