package com.vanphuc.gui.navigation;

import com.vanphuc.gui.Window;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class Page {
    public String name;
    public List<Window> windows = new ArrayList<>();

    public Page(String name) {
        this.name = name;
    }

    public void addWindow(Window window) {
        windows.add(window);
    }

    public void draw(DrawContext context, float partialTicks) {
        for (Window window : windows) {
            window.draw(context, partialTicks);
        }
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        // Duyệt ngược để click vào window trên cùng trước (Z-Index)
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseClick(mouseX, mouseY, button, pressed)) {
                // Focus: Đưa window vừa click lên đầu danh sách render
                windows.remove(window);
                windows.add(window);
                return true;
            }
        }
        return false;
    }

    public void onMouseMove(double mouseX, double mouseY) {
        for (Window window : windows) {
            window.onMouseMove(mouseX, mouseY);
        }
    }

    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Window window : windows) {
            // Thay vì gọi onMouseRelease, ta gọi onMouseClick với pressed = false
            window.onMouseClick(mouseX, mouseY, button, false);
        }
    }
}