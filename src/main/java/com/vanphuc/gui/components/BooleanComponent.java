package com.vanphuc.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import net.minecraft.client.render.RenderLayer;

public class BooleanComponent extends UIElement {
    private final BooleanSetting setting;

    // Định nghĩa đường dẫn đến file ảnh dấu tích
    // namespace: "hutienmong", path: "textures/check.png"
    // Lưu ý: Với MC 1.21+, dùng Identifier.of thay vì new Identifier
    private static final Identifier CHECK_TEXTURE = Identifier.of("hutienmong", "textures/check.png");

    private final Color bgColor = new Color(0.117f, 0.117f, 0.117f, 0.5f);
    // private final Color accentColor = new Color(0xFF3B82F6); // Không cần dùng màu xanh này nữa
    private final Color textColor = new Color(0xFFFFFFFF);
    private final Color hoverColor = new Color(1f, 1f, 1f, 0.05f);

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float padding = 8f;

        // 1. Vẽ nền hover
        if (isHovered) {
            Render2D.drawSmoothRoundedBox(matrix, position.getX(), position.getY(), position.getWidth(),
                    position.getHeight(), 3f, hoverColor);
        }

        // 2. Vẽ tên setting
        Render2D.drawString(context, MinecraftClient.getInstance().textRenderer,
                setting.getName(), position.getX() + padding, position.getY() + (position.getHeight() - 8) / 2f,
                textColor);

        // 3. Vẽ checkbox
        float boxSize = 12f;
        float boxX = position.getX() + position.getWidth() - boxSize - padding;
        float boxY = position.getY() + (position.getHeight() - boxSize) / 2f;

        // Vẽ cái hộp nền mờ
        Render2D.drawSmoothRoundedBox(matrix, boxX, boxY, boxSize, boxSize, 3f, bgColor);

        // Nếu đang bật -> Vẽ Texture dấu tích
        if (setting.isEnabled()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Sửa lại dòng này: Thêm RenderLayer::getGuiTextured vào đầu
            context.drawTexture(
                    RenderLayer::getGuiTextured,
                    CHECK_TEXTURE,
                    (int)boxX + 1, (int)boxY + 1,
                    0.0F, 0.0F,
                    (int)boxSize - 2, (int)boxSize - 2,
                    (int)boxSize - 2, (int)boxSize - 2
            );
        } else {
            // Nếu tắt -> Vẽ viền xám mờ
            Render2D.drawRoundedOutline(matrix, boxX, boxY, boxSize, boxSize, 3f, 1f, new Color(0xFF444444));
        }
        super.draw(context, partialTicks);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (isHovered && pressed && button == 0) {
            setting.toggle();
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