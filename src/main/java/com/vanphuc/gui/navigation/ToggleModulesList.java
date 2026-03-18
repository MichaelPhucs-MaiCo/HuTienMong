package com.vanphuc.gui.navigation;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.ModuleToggleComponent;
import com.vanphuc.gui.window.ModuleWindow;
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ToggleModulesList extends Window {
    private List<ModuleToggleComponent> components = new ArrayList<>();
    private boolean isExpanded = true; // Trạng thái đóng/mở
    private final ItemStack icon = Items.COMMAND_BLOCK.getDefaultStack(); // Lấy icon Command Block cho ngầu

    public ToggleModulesList(String title, float x, float y, float width, List<ModuleWindow> moduleWindows) {
        super(title, new Rectangle(x, y, width, 0));

        float currentY = position.getY() + titleHeight + 4;
        for (ModuleWindow mw : moduleWindows) {
            components.add(new ModuleToggleComponent(mw, position.getX() + 4, currentY, width - 8, 16));
            currentY += 18;
        }
        updateHeight();
    }

    // Hàm tự động co giãn chiều cao của Window
    private void updateHeight() {
        if (isExpanded) {
            this.position.setHeight(titleHeight + 4 + (components.size() * 18));
        } else {
            this.position.setHeight(titleHeight + 2); // Chỉ chừa lại mỗi thanh Title
        }
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        updateHeight(); // Liên tục cập nhật chiều cao để animation gập/mở mượt mà
        super.draw(context, partialTicks);

        // Vẽ Item Icon ở góc phải thanh Title (giống hệt ModuleWindow)
        Render2D.drawItemIcon(context, icon,
                (int) (position.getX() + position.getWidth() - 20),
                (int) (position.getY() + 1));

        // Nếu đang thu gọn thì return luôn, không vẽ đống component bên dưới nữa
        if (!isExpanded) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int mouseX = (int)(mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth());
        int mouseY = (int)(mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());

        for (ModuleToggleComponent comp : components) {
            comp.draw(context, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        // Xác định vùng hitbox của cái Icon
        boolean inIcon = mouseX >= position.getX() + position.getWidth() - 24 &&
                mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + titleHeight;

        // Nếu click trái (0) hoặc phải (1) trúng cái Icon thì đóng/mở
        if (pressed && inIcon && (button == 0 || button == 1)) {
            isExpanded = !isExpanded;
            updateHeight();
            return true;
        }

        // Cho phép click vào Title để kéo thả Window này (Logic kế thừa từ class Window)
        if (super.onMouseClick(mouseX, mouseY, button, pressed)) {
            return true;
        }

        // Nếu đang gập thì không truyền event click xuống mấy cái component tàng hình bên dưới
        if (!isExpanded) return false;

        // Truyền event click xuống cho các dòng Component bên trong
        if (pressed) {
            for (ModuleToggleComponent comp : components) {
                if (comp.onMouseClick(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        super.onMouseMove(mouseX, mouseY);

        // Cập nhật vị trí kéo thả của Window cho các component con trôi theo
        float currentY = position.getY() + titleHeight + 4;
        for (ModuleToggleComponent comp : components) {
            comp.position.setX(position.getX() + 4);
            comp.position.setY(currentY);
            currentY += 18;
        }
    }
}