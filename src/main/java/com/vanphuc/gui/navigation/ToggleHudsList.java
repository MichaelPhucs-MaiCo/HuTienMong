package com.vanphuc.gui.navigation;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.HudComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ToggleHudsList extends Window {
    private List<HudComponent> components = new ArrayList<>();

    public ToggleHudsList(String title, float x, float y, float width, List<HudWindow> huds) {
        super(title, new Rectangle(x, y, width, 0)); // Chiều cao = 0, tý tính lại

        float currentY = position.getY() + titleHeight + 4;
        for (HudWindow hud : huds) {
            // Mỗi component cao 16, cách nhau 2px
            components.add(new HudComponent(hud, position.getX() + 4, currentY, width - 8, 16));
            currentY += 18;
        }

        // Cập nhật lại chiều cao của Window cho bọc vừa đủ các component
        this.position.setHeight(currentY - position.getY() + 2);
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        super.draw(context, partialTicks); // Vẽ background và title của Window

        // Lấy tọa độ chuột thật để làm hiệu ứng Hover
        MinecraftClient mc = MinecraftClient.getInstance();
        int mouseX = (int)(mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth());
        int mouseY = (int)(mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());

        // Vẽ các dòng HUD Component
        for (HudComponent comp : components) {
            comp.draw(context, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        // Cho phép click vào Title để kéo thả Window này
        if (super.onMouseClick(mouseX, mouseY, button, pressed)) {
            return true;
        }

        // Truyền event click xuống cho các dòng Component bên trong
        if (pressed) {
            for (HudComponent comp : components) {
                if (comp.onMouseClick(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        super.onMouseMove(mouseX, mouseY); // Cập nhật vị trí kéo thả của Window

        // Nếu kéo thả Window đi chỗ khác, phải update tọa độ của mấy cái Component chạy theo
        float currentY = position.getY() + titleHeight + 4;
        for (HudComponent comp : components) {
            comp.position.setX(position.getX() + 4);
            comp.position.setY(currentY);
            currentY += 18;
        }
    }
}