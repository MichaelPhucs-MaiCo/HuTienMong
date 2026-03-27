package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.KeybindSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class KeybindComponent extends UIElement {
    private final KeybindSetting setting;
    private boolean isBinding = false;

    private final Color bgColor = new Color(0.117f, 0.117f, 0.117f, 0.5f);
    private final Color textColor = new Color(0xFFFFFFFF);
    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f);
    private final Color bindColor = new Color(0xFF3B82F6); // Xanh lúc đang chờ phím

    public KeybindComponent(KeybindSetting setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        if (!visible) return;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float padding = 8f;

        if (isHovered) {
            Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), position.getHeight(), 3f, hoverColor);
        }

        // Vẽ tên Setting (VD: "Keybind")
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, setting.getName(), position.getX() + padding, position.getY() + (position.getHeight() - 8) / 2f, textColor);

        // LẤY CHUỖI FORMAT ĐÃ ĐƯỢC XỬ LÝ (VD: CTRL + R)
        String keyName = isBinding ? "..." : setting.getFormattedKey();
        float keyWidth = MinecraftClient.getInstance().textRenderer.getWidth("[" + keyName + "]");

        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, "[" + keyName + "]",
                position.getX() + position.getWidth() - keyWidth - padding,
                position.getY() + (position.getHeight() - 8) / 2f,
                isBinding ? bindColor : textColor);

        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (isHovered && pressed && button == 0) {
            isBinding = !isBinding;
            return true;
        }
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    @Override
    public boolean onKey(int key, int action, int mods) {
        if (isBinding && action == GLFW.GLFW_PRESS) {

            // BỎ QUA NẾU CHỈ LÀ PHÍM BỔ TRỢ (Tránh việc vừa bấm giữ LCTRL nó đã gán luôn LCTRL)
            if (key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL ||
                    key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT ||
                    key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT ||
                    key == GLFW.GLFW_KEY_LEFT_SUPER || key == GLFW.GLFW_KEY_RIGHT_SUPER) {
                return true; // Vẫn giữ trạng thái isBinding chờ phím tiếp theo
            }

            // Nhấn ESC hoặc DELETE/BACKSPACE để xoá phím
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                setting.setKey(-1, 0);
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.of("§7[§bHutienMong§7] §cĐã gỡ phím tắt cho §f" + setting.getModule().name), false);
                }
            } else {
                // LƯU CẢ PHÍM VÀ MODS
                setting.setKey(key, mods);
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.of("§7[§bHutienMong§7] §aĐã gán §e" + setting.getFormattedKey() + " §abật tắt §f" + setting.getModule().name), false);
                }
            }
            isBinding = false;
            return true;
        }
        return super.onKey(key, action, mods); // Propagate nếu có UIElement con
    }

    @Override
    public void measure(com.vanphuc.gui.Size availableSize) {
        this.position.setWidth(availableSize.getWidth());
        this.position.setHeight(availableSize.getHeight());
        super.measure(availableSize);
    }
}