package com.vanphuc.gui;

import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {
    private static GuiManager INSTANCE;

    private boolean isOpen = false;
    private final List<Window> windows = new ArrayList<>();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public static GuiManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GuiManager();
        return INSTANCE;
    }

    public void initialize() {
        windows.clear();
        int x = 20;
        int y = 20;
        int windowWidth = 100;
        int windowHeight = 20;
        int spacing = 10;

        int screenWidth = mc.getWindow().getScaledWidth();

        for (Module module : Modules.get().getAll()) {
            ModuleWindow window = new ModuleWindow(module, new Rectangle(x, y, windowWidth, windowHeight));
            window.initialize();
            windows.add(window);

            x += windowWidth + spacing;

            if (x + windowWidth > screenWidth - 20) {
                x = 20;
                y += windowHeight + spacing + 10;
            }
        }
    }

    public void render(DrawContext context, float partialTicks) {
        if (!isOpen)
            return;

        com.vanphuc.utils.render.Render2D.drawOverlay(context, mc.getWindow().getScaledWidth(),
                mc.getWindow().getScaledHeight());

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 500f);

        for (Window window : windows) {
            window.draw(context, partialTicks);
        }

        context.getMatrices().pop();
    }

    public void addWindow(Window window) {
        if (windows.contains(window)) {
            windows.remove(window);
        }
        windows.add(window);
    }

    public void removeWindow(Window window) {
        windows.remove(window);
    }

    public void toggle() {
        if (windows.isEmpty()) {
            initialize();
        }
        this.isOpen = !isOpen;

        if (isOpen) {
            mc.mouse.unlockCursor();
        } else {
            mc.mouse.lockCursor();
            com.vanphuc.utils.ConfigManager.save();
        }
    }

    // ĐÃ CẬP NHẬT: Thêm tham số mods và logic match keybind
    public boolean onKey(int key, int action, int mods) {
        if (action != GLFW.GLFW_PRESS) return false;

        // Nếu GUI đang mở
        if (isOpen) {
            // Truyền mods xuống Window để KeybindComponent có thể nhận diện tổ hợp phím
            for (int i = windows.size() - 1; i >= 0; i--) {
                if (windows.get(i).onKey(key, action, mods)) return true;
            }

            // Xử lý ESC để đóng Settings hoặc đóng GUI
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                for (int i = windows.size() - 1; i >= 0; i--) {
                    Window window = windows.get(i);
                    if (window instanceof SettingsWindow sw) {
                        for (Window w : windows) {
                            if (w instanceof ModuleWindow mw && mw.getModule().name.equals(sw.getTitle().replace("§l", "").replace(" Settings", ""))) {
                                mw.closeSettings();
                                // LƯU KHI ĐÓNG CỬA SỔ SETTING BẰNG ESC 👇
                                com.vanphuc.utils.ConfigManager.save();
                                return true;
                            }
                        }
                        removeWindow(window);
                        // LƯU KHI ĐÓNG WINDOW BẤT KỲ BẰNG ESC 👇
                        com.vanphuc.utils.ConfigManager.save();
                        return true;
                    }
                }
                toggle(); // Hàm toggle() bên trên đã có save() rồi nên không cần gọi lại ở đây
                return true;
            }
        }
        // Nếu GUI ĐANG ĐÓNG
        else {
            // Mở GUI bằng phím ` (Grave)
            if (key == GLFW.GLFW_KEY_GRAVE_ACCENT) {
                toggle();
                return true;
            }

            // XỬ LÝ BẬT TẮT MODULE BẰNG PHÍM TẮT (Sử dụng hàm matches mới)
            for (Module module : Modules.get().getAll()) {
                if (module.keybind.matches(key, mods)) {
                    module.toggle();
                }
            }
        }
        return false;
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!isOpen)
            return false;

        List<Window> copy = new ArrayList<>(windows);
        for (int i = copy.size() - 1; i >= 0; i--) {
            Window window = copy.get(i);
            if (window.onMouseClick(mouseX, mouseY, button, pressed)) {
                if (windows.contains(window)) {
                    windows.remove(window);
                    windows.add(window);
                }
                return true;
            }
        }
        return false;
    }

    public void onMouseMove(double mouseX, double mouseY) {
        if (!isOpen)
            return;

        for (Window window : windows) {
            window.onMouseMove(mouseX, mouseY);
        }
    }

    public boolean onChar(int codePoint, int modifiers) {
        // Nếu GUI đang tắt thì không nhận chữ gõ
        if (!isOpen) return false;

        // Truyền sự kiện gõ chữ xuống cho các Window đang hiển thị
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).onChar(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }
    // Riêng trong GuiManager.java, thêm hàm này để đóng Settings:
    public void closeSettingsWindows() {
        windows.removeIf(w -> w instanceof SettingsWindow);
    }

    public boolean isOpen() {
        return isOpen;
    }
}