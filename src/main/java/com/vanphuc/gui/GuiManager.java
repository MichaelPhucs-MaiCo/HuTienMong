package com.vanphuc.gui;

import com.vanphuc.gui.navigation.*;
import com.vanphuc.gui.navigation.friends.FriendWindow;
import com.vanphuc.gui.navigation.huds.*;
import com.vanphuc.gui.window.ModuleWindow;
import com.vanphuc.gui.window.SettingsWindow;
import com.vanphuc.module.Module;
import com.vanphuc.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.vanphuc.gui.navigation.ConfigPage.guiKeybindSetting;

public class GuiManager {
    private static GuiManager INSTANCE;

    private boolean isOpen = false;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public List<Page> pages = new ArrayList<>();
    public Page activePage;
    public NavigationBar navigationBar;

    public static GuiManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GuiManager();
        return INSTANCE;
    }

    public void initialize() {
        pages.clear();
        navigationBar = new NavigationBar();

        // 2. Setup Page Modules
        Page modulesPage = new Page("Modules");

        List<ModuleWindow> moduleWindows = new ArrayList<>();
        int defaultX = 200;
        int defaultY = 50;

        // Khởi tạo các ModuleWindow (Mặc định sẽ ẩn)
        for (Module module : Modules.get().getAll()) {
            ModuleWindow window = new ModuleWindow(module, new Rectangle(defaultX, defaultY, 120, 20));
            window.initialize();
            moduleWindows.add(window);
        }

        // Tạo bảng quản lý Modules (Lấy tọa độ từ biến static đã được ConfigManager nạp)
        ToggleModulesList modulesListWindow = new ToggleModulesList("Modules Manager", ToggleModulesList.savedX, ToggleModulesList.savedY, 150, moduleWindows);
        modulesPage.addWindow(modulesListWindow);

        // Bắt buộc add các ModuleWindow vào page để nó nhận event (tàng hình hay không do nó tự quyết định)
        for (ModuleWindow mw : moduleWindows) {
            modulesPage.addWindow(mw);
        }

        // 3. Setup Page Hud
        Page hudPage = new Page("Hud");

        // Khai báo danh sách các HUD hiện có trong Client
        List<HudWindow> addHuds = new ArrayList<>();
        addHuds.add(new FPSHud(200, 100));
//        addHuds.add(new HudTestTimerHud(200, 130));// Cậu có thể add thêm CoordsHud, ArmorHud... vào đây
        addHuds.add(new AutoSavePaperHud(200, 160));
        addHuds.add(new AutoSwitchHotbarHud(200, 190));
        addHuds.add(new AutoQuestHud(200, 220));
        addHuds.add(new LogConsoleHud(10, 150));

        // Khởi tạo Bảng quản lý HUD
        ToggleHudsList hudListWindow = new ToggleHudsList("HUDs Manager", 420, 150, 120, addHuds);
        hudPage.addWindow(hudListWindow); // Nhét cái bảng vào trang Hud

        // BẮT BUỘC: Add cả các HudWindow vào trang Hud để nó nhận Event chuột và render khi đang mở ClickGUI
        for (HudWindow hud : addHuds) {
            hudPage.addWindow(hud);
        }

        // 4. Setup Page Friend
        Page friendPage = new Page("Friend");
        int friendWidth = 250;
        int friendHeight = 200;
        float friendX = (mc.getWindow().getScaledWidth() - friendWidth) / 2f;
        float friendY = mc.getWindow().getScaledHeight() / 4f;

        FriendWindow friendWindow = new FriendWindow(new Rectangle(friendX, friendY, friendWidth, friendHeight));
        friendPage.addWindow(friendWindow);

        // 5. Setup Page Config (MỚI THÊM)
        Page configPage = new ConfigPage(); // Khởi tạo trang ConfigPage cậu vừa làm

        // Add tất cả Pages vào list quản lý
        pages.add(modulesPage);
        pages.add(hudPage);
        pages.add(friendPage);
        pages.add(configPage); // Bắt buộc add vào list để NavigationBar nó đếm được

        activePage = modulesPage; // Mặc định mở lên sẽ ở tab Modules

        // --- QUAN TRỌNG: Nạp config ngay sau khi setup xong GUI ---
        // Nạp lại tọa độ và trạng thái (Bật/Tắt) của Modules và HUDs từ file JSON
        com.vanphuc.utils.ConfigManager.load();
    }

    public void render(DrawContext context, float partialTicks) {
        // Khi ClickGUI tắt -> Cập nhật đoạn này để nó CHỈ vẽ các HUD đang được BẬT
        if (!isOpen) {
            if (pages.size() > 1) { // Đảm bảo pages đã được khởi tạo
                for (Window w : pages.get(1).windows) { // Lấy hudPage (index 1)
                    // Kiểm tra nếu là HudWindow và có enabled = true thì mới cho xuất hiện
                    if (w instanceof HudWindow && ((HudWindow) w).enabled) {
                        ((HudWindow) w).draw(context, partialTicks);
                    }
                }
            }
            return;
        }

        // Khi ClickGUI mở
        com.vanphuc.utils.render.Render2D.drawOverlay(context, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 500f);

        // --- BƯỚC QUAN TRỌNG: GỌI HÀM VẼ LƯỚI Ở ĐÂY ---
        // Vẽ lưới nền trước khi vẽ các Window đè lên
        drawGrid(context);

        // Vẽ thanh điều hướng
        if (navigationBar != null) {
            navigationBar.draw(context);
        }

        // Vẽ Page hiện tại (nếu đang ở Modules thì vẽ Modules, ở Hud thì vẽ danh sách Hud)
        if (activePage != null) {
            activePage.draw(context, partialTicks);
        }

        context.getMatrices().pop();
    }

    public void drawGrid(DrawContext context) {
        // Sử dụng logic mới
        if (com.vanphuc.utils.ClientConfig.showGrid && com.vanphuc.utils.ClientConfig.isAnyWindowMoving) {
            int gridSize = com.vanphuc.utils.ClientConfig.gridSize;
            int w = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int h = MinecraftClient.getInstance().getWindow().getScaledHeight();

            // Xài mã màu chuẩn int của Java (Alpha 15, Xám) cho dễ render
            int color = 0x15708090;

            // Vẽ dọc
            for (int x = 0; x <= w; x += gridSize) {
                context.fill(x, 0, x + 1, h, color);
            }
            // Vẽ ngang
            for (int y = 0; y <= h; y += gridSize) {
                context.fill(0, y, w, y + 1, color);
            }
        }
    }

    public void addWindow(Window window) {
        if (activePage != null) {
            activePage.addWindow(window);
        }
    }

    public void removeWindow(Window window) {
        if (activePage != null) {
            activePage.windows.remove(window);
        }
    }

    public void toggle() {
        if (pages.isEmpty()) {
            initialize();
        }
        this.isOpen = !isOpen;

        if (isOpen) {
            mc.mouse.unlockCursor();
            // Reset về Page đầu tiên (Modules) mỗi khi ClickGUI được mở lên
            if (!pages.isEmpty()) {
                activePage = pages.get(0);
            }
        } else {
            mc.mouse.lockCursor();
            com.vanphuc.utils.ConfigManager.save();
        }
    }

    public boolean onKey(int key, int action, int mods) {
        if (action != GLFW.GLFW_PRESS) return false;

        // Nếu GUI đang mở
        if (isOpen) {
            if (activePage != null) {
                // Truyền mods xuống Window để KeybindComponent có thể nhận diện tổ hợp phím
                for (int i = activePage.windows.size() - 1; i >= 0; i--) {
                    if (activePage.windows.get(i).onKey(key, action, mods)) return true;
                }

                // Xử lý ESC để đóng Settings hoặc đóng GUI
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    for (int i = activePage.windows.size() - 1; i >= 0; i--) {
                        Window window = activePage.windows.get(i);
                        if (window instanceof SettingsWindow sw) {
                            for (Window w : activePage.windows) {
                                if (w instanceof ModuleWindow mw && mw.getModule().name.equals(sw.getTitle().replace("§l", "").replace(" Settings", ""))) {
                                    mw.closeSettings();
                                    com.vanphuc.utils.ConfigManager.save();
                                    return true;
                                }
                            }
                            activePage.windows.remove(window);
                            com.vanphuc.utils.ConfigManager.save();
                            return true;
                        }
                    }
                    toggle();
                    return true;
                }
            }
        }
        // Nếu GUI ĐANG ĐÓNG
        else {
          // Kiểm tra phím thông qua setting keybind
            if (guiKeybindSetting.matches(key, mods)) {
                toggle();
                return true;
            }

            // XỬ LÝ BẬT TẮT MODULE BẰNG PHÍM TẮT
            if (com.vanphuc.utils.ClientConfig.blockKeybindInGui && mc.currentScreen != null) {
                return false; // Có màn hình nào đó đang mở (Chat, Inventory, Meteor...) -> chặn bật module bằng keybind
            }
            for (Module module : Modules.get().getAll()) {
                if (module.keybind.matches(key, mods)) {
                    module.toggle();
                }
            }
        }
        return false;
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button, boolean pressed) {
        if (!isOpen) return false;

        // Check click vào Topbar trước
        if (pressed && navigationBar != null && navigationBar.onMouseClick(mouseX, mouseY, button)) {
            return true;
        }

        // Pass sự kiện xuống Page hiện hành
        if (activePage != null) {
            return activePage.onMouseClick(mouseX, mouseY, button, pressed);
        }
        return false;
    }

    public void onMouseMove(double mouseX, double mouseY) {
        if (!isOpen) return;
        if (activePage != null) {
            activePage.onMouseMove(mouseX, mouseY);
        }
    }

    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (!isOpen) return;
        if (activePage != null) {
            activePage.onMouseRelease(mouseX, mouseY, button);
        }
    }

    public boolean onChar(int codePoint, int modifiers) {
        // Nếu GUI đang tắt thì không nhận chữ gõ
        if (!isOpen) return false;

        // Truyền sự kiện gõ chữ xuống cho các Window đang hiển thị trong Page hiện hành
        if (activePage != null) {
            for (int i = activePage.windows.size() - 1; i >= 0; i--) {
                if (activePage.windows.get(i).onChar(codePoint, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void closeSettingsWindows() {
        if (activePage != null) {
            // Tạo một list copy để duyệt, tránh lỗi Crash (ConcurrentModificationException) khi xóa phần tử
            List<Window> copyList = new ArrayList<>(activePage.windows);
            for (Window w : copyList) {
                if (w instanceof ModuleWindow mw) {
                    mw.closeSettings(); // Bắt ModuleWindow tự dọn dẹp SettingsWindow của nó
                }
            }
            // Dự phòng dọn rác nốt nếu còn kẹt
            activePage.windows.removeIf(w -> w instanceof SettingsWindow);
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}