package com.vanphuc.gui.window;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Size;
import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.BooleanComponent;
import com.vanphuc.gui.components.ButtonComponent;
import com.vanphuc.gui.components.KeybindComponent;
import com.vanphuc.gui.components.SliderComponent;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.*;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public class SettingsWindow extends Window {
    private final Module module;

    // ĐÃ SỬA: Tăng chiều cao mỗi setting lên 24f để lấy không gian thở
    private final float settingHeight = 24f;
    private final float horizontalPadding = 6f;

    public SettingsWindow(Module module, Rectangle position) {
        // ĐÃ SỬA: Thêm "§l" (mã in đậm của Minecraft) vào trước Title
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
            else if (s instanceof ActionSetting as) addChild(new ButtonComponent(as));// Thêm dòng này
        }
        super.initialize();
        arrange(position);
    }

    @Override
    public void arrange(Rectangle finalSize) {
        this.position = finalSize;
        float currentY = finalSize.getY() + titleHeight + 6f;

        for (UIElement child : children) {
            float childHeight = settingHeight;
            child.measure(new Size(finalSize.getWidth() - (horizontalPadding * 2), childHeight));
            child.arrange(new Rectangle(finalSize.getX() + horizontalPadding, currentY,
                    finalSize.getWidth() - (horizontalPadding * 2), childHeight));
            currentY += childHeight; // Không cần cộng thêm khoảng cách vì height đã đủ to
        }

        this.position.setHeight(Math.max(titleHeight + 10f, currentY - finalSize.getY() + 4f));
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        arrange(position);
        super.draw(context, partialTicks);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        com.vanphuc.utils.render.Render2D.drawBox(matrix,
                position.getX() + 2, position.getY() + titleHeight,
                position.getWidth() - 4, 1f,
                new com.vanphuc.gui.colors.Color(0xFF3B82F6));
    }

}