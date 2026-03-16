package com.vanphuc.gui.navigation;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import net.minecraft.client.gui.DrawContext;

public class HudWindow extends Window {
    public enum ResizeMode { None, WidthAndHeight }
    public ResizeMode resizeMode = ResizeMode.None;

    public float minWidth;
    public float minHeight;
    public float maxHeight;

    // Thêm công tắc trạng thái (Mặc định là false - Tắt)
    public boolean enabled = false;

    public HudWindow(String title, float x, float y, float width, float height) {
        super(title, new Rectangle(x, y, width, height));
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        // CÚ CHỐT: Nếu HUD đang tắt thì dẹp, không vẽ gì sất!
        if (!enabled) return;

        // Nếu ClickGUI mở và HUD đang BẬT, vẽ viền kéo thả
        if (GuiManager.getInstance().isOpen()) {
            super.draw(context, partialTicks);
        }
    }

    public boolean isVisible() {
        return enabled;
    }

    // Nếu HUD tắt thì khóa luôn tính năng kéo thả
    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!enabled) return false;
        boolean handled = super.onMouseClick(mouseX, mouseY, button, pressed);

        // Nếu nhả chuột trái sau khi kéo (pressed == false)
        if (!pressed && button == 0 && isMoving) {
            com.vanphuc.utils.ConfigManager.save(); // Gọi hàm lưu chung của client
        }
        return handled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        com.vanphuc.utils.ConfigManager.save(); // Lưu ngay khi bật/tắt
    }
}