package com.vanphuc.gui.window;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.Module;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ModuleWindow extends Window {
    private final Module module;
    private SettingsWindow settingsWindow;
    public boolean showInGui = false; // Thêm dòng này

    public ModuleWindow(Module module, Rectangle position) {
        super(module.name, position);
        this.module = module;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        if (!showInGui) return; // Nếu không add ra màn hình thì tàng hình

        if (module.isActive()) {
            this.currentBorderColor = new Color(0xFF2ECC71);
        } else {
            this.currentBorderColor = new Color(0xFF3B82F6);
        }

        super.draw(context, partialTicks);

        if (module.icon != null) {
            Render2D.drawItemIcon(context, module.icon,
                    (int) (position.getX() + position.getWidth() - 20),
                    (int) (position.getY() + 2));
        }
        if (module.isActive()) {
            Render2D.drawString(context, MinecraftClient.getInstance().textRenderer, "*",
                    position.getX() + position.getWidth() - 28, position.getY() + 5,
                    this.currentBorderColor);
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!showInGui) return false; // Không nhận click nếu đang ẩn

        boolean inHeader = mouseX >= position.getX() && mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + titleHeight;

        if (button == 1) {
            if (inHeader && pressed) {
                toggleSettings();
                return true;
            }
        } else if (button == 0) {
            if (pressed) {
                if (inHeader) {
                    boolean inIcon = mouseX >= position.getX() + position.getWidth() - 24;
                    if (inIcon) {
                        // --- ĐÃ FIX Ở ĐÂY ---
                        isMoving = true;
                        com.vanphuc.utils.ClientConfig.isAnyWindowMoving = true; // Bật lưới khi kéo

                        // Chốt tọa độ thực tế để tránh lỗi nhảy góc
                        exactX = position.getX();
                        exactY = position.getY();

                        lastMouseX = mouseX;
                        lastMouseY = mouseY;
                    } else {
                        module.toggle();
                    }
                    return true;
                }
            } else {
                if (isMoving) {
                    // --- ĐÃ FIX Ở ĐÂY ---
                    isMoving = false;
                    com.vanphuc.utils.ClientConfig.isAnyWindowMoving = false; // Tắt lưới khi thả tay
                    com.vanphuc.utils.ConfigManager.save(); // Lưu lại tọa độ khi thả chuột
                    return true;
                }
            }
        }
        if (inHeader) return true;
        return super.onMouseClick(mouseX, mouseY, button, pressed);
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        if (!showInGui) return;
        super.onMouseMove(mouseX, mouseY);
    }

    // Các hàm còn lại (toggleSettings, closeSettings, getModule) giữ nguyên như cũ...
    private void toggleSettings() {
        if (settingsWindow == null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            float width = 250f;
            float x = (sw - width) / 2f;
            float y = sh / 3f;
            Rectangle settingsPos = new Rectangle(x, y, width, 20);
            settingsWindow = new SettingsWindow(module, settingsPos);
            settingsWindow.initialize();
            GuiManager.getInstance().addWindow(settingsWindow);
        } else {
            closeSettings();
        }
    }

    public void closeSettings() {
        if (settingsWindow != null) {
            GuiManager.getInstance().removeWindow(settingsWindow);
            settingsWindow = null;
        }
    }

    public Module getModule() { return module; }
}