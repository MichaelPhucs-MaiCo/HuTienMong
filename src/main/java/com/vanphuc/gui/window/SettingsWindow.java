package com.vanphuc.gui.window;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Size;
import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.*;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.*;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class SettingsWindow extends Window {
    private final Module module;
    private final float settingHeight = 24f; // Chiều cao cơ bản
    private final float horizontalPadding = 6f;

    public SettingsWindow(Module module, Rectangle position) {
        super("§l" + module.name + " Settings", position);
        this.module = module;
    }

    @Override
    public void initialize() {
        children.clear();
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BooleanSetting bs) addChild(new BooleanComponent(bs));
            else if (s instanceof NumberSetting ns) addChild(new SliderComponent(ns));
            else if (s instanceof KeybindSetting ks) addChild(new KeybindComponent(ks));
            else if (s instanceof ActionSetting as) addChild(new ButtonComponent(as));
            else if (s instanceof StringSetting ss) addChild(new StringInputComponent(ss));
            else if (s instanceof EnumSetting es) addChild(new DropdownComponent(es)); // Setting mới của Khầy nè
        }
        super.initialize();
        // Sau khi add con xong, gọi measure và arrange ngay lập tức để ẩn những thứ cần ẩn
        this.measure(new Size(position.getWidth(), position.getHeight()));
        this.arrange(position);
    }

    @Override
    public void arrange(Rectangle finalSize) {
        this.position = finalSize;
        float currentY = finalSize.getY() + titleHeight + 6f;

        java.util.List<com.vanphuc.module.settings.Setting<?>> settings = module.getSettings();

        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i);
            com.vanphuc.module.settings.Setting<?> s = settings.get(i);

            // Cập nhật trạng thái visible từ Setting sang UIElement
            child.visible = s.isVisible();

            if (!child.visible) {
                // Nếu ẩn, triệt tiêu kích thước để không chiếm chỗ và không render
                child.getPosition().setHeight(0);
                child.getPosition().setX(-9999); // Đẩy nó ra khỏi màn hình cho chắc cú
                continue;
            }

            // Nếu hiện, mới tính toán vị trí
            child.measure(new Size(finalSize.getWidth() - (horizontalPadding * 2), settingHeight));
            float actualHeight = child.getPosition().getHeight();

            child.arrange(new Rectangle(
                    finalSize.getX() + horizontalPadding,
                    currentY,
                    finalSize.getWidth() - (horizontalPadding * 2),
                    actualHeight
            ));
            currentY += actualHeight + 2f;
        }
        // Cập nhật lại tổng chiều cao cửa sổ
        this.position.setHeight(Math.max(titleHeight + 10f, currentY - finalSize.getY() + 4f));
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        // Luôn arrange lại trước khi vẽ để cập nhật vị trí tức thời khi Dropdown đóng/mở
        arrange(position);
        super.draw(context, partialTicks);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        // Đường kẻ phân cách Xanh Blue Sleek Carbon
        com.vanphuc.utils.render.Render2D.drawBox(matrix,
                position.getX() + 2, position.getY() + titleHeight,
                position.getWidth() - 4, 1f,
                new com.vanphuc.gui.colors.Color(0xFF0F4C81));
    }
}