package com.vanphuc.gui.components;

import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class SliderComponent extends UIElement {
    private final NumberSetting setting;
    private boolean dragging = false;

    // Thêm các biến để quản lý việc gõ số
    private boolean isEditing = false;
    private String editText = "";

    // Bảng màu chuẩn Sleek Carbon cực mượt
    private final Color bgColor = new Color(0.117f, 0.117f, 0.117f, 0.5f);
    private final Color accentColor = new Color(0xFF0F4C81); // Màu nhấn Xanh Blue
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

        // --- LOGIC HIỂN THỊ CHỮ SỐ ---
        String valueText;
        Color displayColor;

        // Chuẩn bị format dựa trên precision
        String formatString = "%." + setting.getPrecision() + "f";

        if (isEditing) {
            // Khi đang gõ: Thêm hiệu ứng con trỏ nhấp nháy mỗi nửa giây, đổi màu chữ sang Vàng (#F1C40F)
            valueText = editText + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
            displayColor = new Color(0xFFF1C40F);
        } else {
            // Trạng thái bình thường hiển thị số linh hoạt theo precision
            valueText = String.format(formatString, setting.getValue()).replace(",", ".");
            displayColor = accentColor;
        }

        // Vẽ Tên Setting
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                nameText, position.getX() + padding, position.getY() + 4f, textColor);

        // Vẽ Giá Trị
        float valWidth = MinecraftClient.getInstance().textRenderer.getWidth(valueText);
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                valueText, position.getX() + position.getWidth() - valWidth - padding, position.getY() + 4f,
                displayColor);

        // Vẽ Thanh Trượt
        float barX = position.getX() + padding;
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
        float padding = 8f;

        // Tính toán độ rộng của text linh hoạt theo precision
        String formatString = "%." + setting.getPrecision() + "f";
        String currentValueText = String.format(formatString, setting.getValue()).replace(",", ".");
        float valWidth = MinecraftClient.getInstance().textRenderer.getWidth(currentValueText);
        float valX = position.getX() + position.getWidth() - valWidth - padding;
        float valY = position.getY() + 4f;

        // Tính toán hitbox của cái số để nhận diện cú click
        boolean clickOnText = mouseX >= valX - 4 && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= valY - 2 && mouseY <= valY + 12;

        if (button == 0) {
            if (pressed) {
                if (clickOnText) {
                    // Nếu click vào số -> Mở chế độ edit
                    isEditing = true;
                    // Lấy số hiện tại đưa vào khung sửa (thay phẩy thành chấm để parse không bị lỗi)
                    editText = currentValueText;
                    return true;
                } else {
                    // Nếu click ra ngoài vùng số
                    if (isEditing) {
                        applyEdit(); // Tự động lưu số đang gõ dở
                    }
                    if (isHovered) {
                        dragging = true;
                        updateValue(mouseX);
                        return true;
                    }
                }
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
        // Không cho thanh trượt chạy theo chuột nếu đang ở chế độ gõ số
        if (dragging && !isEditing) {
            updateValue(mouseX);
        }
    }

    // --- LOGIC NHẬP PHÍM BÀN PHÍM ---
    @Override
    public boolean onKey(int key, int action, int mods) {
        if (isEditing) {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                // Giờ thì Enter hay ESC cũng đều gọi applyEdit để lưu hết nhé!
                if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
                    applyEdit();
                } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!editText.isEmpty()) {
                        editText = editText.substring(0, editText.length() - 1);
                    }
                }
            }
            return true; // Chặn phím không cho lọt xuống Game
        }
        return super.onKey(key, action, mods);
    }

    @Override
    public boolean onChar(int codePoint, int modifiers) {
        if (isEditing) {
            char c = (char) codePoint;
            // Chỉ cho phép gõ số, dấu trừ và dấu chấm thập phân
            if (Character.isDigit(c) || c == '.' || c == '-') {
                editText += c;
            }
            return true;
        }
        return super.onChar(codePoint, modifiers);
    }

    private void applyEdit() {
        isEditing = false;
        try {
            // Thay thế dấu phẩy nhỡ người dùng gõ nhầm trên bàn phím numpad
            double newValue = Double.parseDouble(editText.replace(",", "."));
            setting.setValue(newValue);
        } catch (NumberFormatException e) {
            // Nếu người dùng gõ linh tinh (ví dụ "....") thì vứt xó, giữ nguyên giá trị cũ
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