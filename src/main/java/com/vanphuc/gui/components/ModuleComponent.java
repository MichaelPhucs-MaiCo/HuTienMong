package com.vanphuc.gui.components;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Size;
import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.*;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class ModuleComponent extends UIElement {
    private final Module module;
    private final float baseHeight = 14f; // Chiều cao mặc định của module (Title bar)
    private final float settingHeight = 16f; // Chiều cao mỗi dòng setting component
    private boolean extended = false; // Trạng thái đóng/mở danh sách setting

    // Palette màu Sleek Carbon
    private final Color activeColor = new Color(0xFF3B82F6); // Blue nhấn #3B82F6
    private final Color inactiveColor = new Color(0xFFFFFFFF); // Trắng
    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f); // Hover mờ

    public ModuleComponent(Module module) {
        this.module = module;
        initializeSettings();
    }

    private void initializeSettings() {
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BooleanSetting booleanSetting) {
                addChild(new BooleanComponent(booleanSetting));
            } else if (s instanceof NumberSetting numberSetting) {
                addChild(new SliderComponent(numberSetting));
            }
            else if (s instanceof StringSetting ss)
                addChild(new StringInputComponent(ss));
        }
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // 1. Vẽ nền khi di chuột vào (Chỉ vẽ phần header)
        if (isHovered) {
            Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(), baseHeight, 2f,
                    hoverColor);
        }

        // 2. Vẽ tên Module (Thêm icon mũi tên cho sang chảnh)
        String prefix = extended ? "▼ " : "► ";
        Color textColor = module.isActive() ? activeColor : inactiveColor;
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                prefix + module.name, position.getX() + 4, position.getY() + 3, textColor);

        // 3. Vẽ icon vật phẩm của module
        if (module.icon != null) {
            Render2D.drawItemIcon(context, module.icon, (int) (position.getX() + position.getWidth() - 18),
                    (int) (position.getY() - 1));
        }

        // 4. Vẽ danh sách Settings nếu đang mở rộng
        if (extended) {
            for (UIElement child : children) {
                child.draw(context, partialTicks);
            }
        }
    }

    @Override
    public void measure(Size availableSize) {
        float totalHeight = baseHeight;

        if (extended) {
            for (UIElement child : children) {
                child.measure(new Size(availableSize.getWidth() - 8, settingHeight));
                totalHeight += child.getPosition().getHeight();
            }
        }

        this.position.setWidth(availableSize.getWidth());
        this.position.setHeight(totalHeight);
    }

    @Override
    public void arrange(Rectangle finalSize) {
        this.position = finalSize;
        float currentY = finalSize.getY() + baseHeight;

        if (extended) {
            for (UIElement child : children) {
                float childHeight = settingHeight;
                child.arrange(new Rectangle(finalSize.getX() + 4, currentY, finalSize.getWidth() - 8, childHeight));
                currentY += childHeight;
            }
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        // Chỉ xử lý click nếu trỏ chuột nằm trong phần tiêu đề (baseHeight)
        boolean inHeader = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + baseHeight;

        if (inHeader && pressed) {
            if (button == 0) { // Chuột trái: Bật/Tắt module
                module.toggle();
            } else if (button == 1) { // Chuột phải: Đóng/Mở Setting
                extended = !extended;
                if (parent != null) {
                    parent.arrange(parent.getPosition());
                }
            }
            return true;
        }

        if (extended) {
            return super.onMouseClick(mouseX, mouseY, button, pressed);
        }

        return false;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        // Cập nhật trạng thái hover cho chính mình
        this.isHovered = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + baseHeight;

        if (extended) {
            for (UIElement child : children) {
                child.onMouseMove(mouseX, mouseY);
            }
        }
    }
}