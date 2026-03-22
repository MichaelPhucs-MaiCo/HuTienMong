package com.vanphuc.gui.navigation;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.ModuleToggleComponent;
import com.vanphuc.gui.window.ModuleWindow;
import com.vanphuc.utils.ConfigManager; // Nhớ import con hàng này nhé Khầy
import com.vanphuc.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ToggleModulesList extends Window {
    private List<ModuleToggleComponent> components = new ArrayList<>();

    // Khầy cho mấy thằng này thành public static để GuiManager dễ truy cập và lưu config nhé
    public static boolean isExpanded = true;
    public static float savedX = 100; // Vị trí mặc định ban đầu
    public static float savedY = 100;

    private final ItemStack icon = Items.COMMAND_BLOCK.getDefaultStack();

    public ToggleModulesList(String title, float x, float y, float width, List<ModuleWindow> moduleWindows) {
        // Sử dụng giá trị x, y truyền vào (đã được nạp từ Config ở GuiManager)
        super(title, new Rectangle(x, y, width, 0));

        float currentY = position.getY() + titleHeight + 4;
        for (ModuleWindow mw : moduleWindows) {
            components.add(new ModuleToggleComponent(mw, position.getX() + 4, currentY, width - 8, 16));
            currentY += 18;
        }
        updateHeight();
    }

    private void updateHeight() {
        if (isExpanded) {
            this.position.setHeight(titleHeight + 4 + (components.size() * 18));
        } else {
            this.position.setHeight(titleHeight + 2);
        }
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        updateHeight();
        super.draw(context, partialTicks);

        Render2D.drawItemIcon(context, icon,
                (int) (position.getX() + position.getWidth() - 20),
                (int) (position.getY() + 1));

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
        boolean inIcon = mouseX >= position.getX() + position.getWidth() - 24 &&
                mouseX <= position.getX() + position.getWidth() &&
                mouseY >= position.getY() && mouseY <= position.getY() + titleHeight;

        if (pressed && inIcon && (button == 0 || button == 1)) {
            isExpanded = !isExpanded;
            updateHeight();
            // Mỗi lần gập/mở là phải lưu lại ngay cho nóng!
            ConfigManager.save();
            return true;
        }

        if (super.onMouseClick(mouseX, mouseY, button, pressed)) {
            // Nếu nhả chuột sau khi kéo thả (pressed == false), thì lưu vị trí mới
            if (!pressed) {
                savedX = position.getX();
                savedY = position.getY();
                ConfigManager.save();
            }
            return true;
        }

        if (!isExpanded) return false;

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

        float currentY = position.getY() + titleHeight + 4;
        for (ModuleToggleComponent comp : components) {
            comp.position.setX(position.getX() + 4);
            comp.position.setY(currentY);
            currentY += 18;
        }

        // Cập nhật tọa độ tĩnh để ConfigManager bốc dữ liệu cho chuẩn
        savedX = position.getX();
        savedY = position.getY();
    }
}