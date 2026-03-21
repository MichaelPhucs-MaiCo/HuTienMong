package com.vanphuc.gui.navigation;

import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.UIElement;
import com.vanphuc.gui.Window;
import com.vanphuc.gui.components.BooleanComponent;
import com.vanphuc.gui.components.KeybindComponent;
import com.vanphuc.gui.components.SliderComponent;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.KeybindSetting;
import com.vanphuc.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class ConfigPage extends Page {

    public static final BooleanSetting snappingSetting = new BooleanSetting("Bám Lưới (Snapping)", com.vanphuc.utils.ClientConfig.snapping) {
        @Override
        public void setValue(Boolean value) {
            super.setValue(value);
            com.vanphuc.utils.ClientConfig.snapping = value;
        }
    };
    public static final BooleanSetting showGridSetting = new BooleanSetting("Hiển thị Lưới", com.vanphuc.utils.ClientConfig.showGrid) {
        @Override
        public void setValue(Boolean value) {
            super.setValue(value);
            com.vanphuc.utils.ClientConfig.showGrid = value;
        }
    };
    public static final NumberSetting gridSizeSetting = new NumberSetting("Kích thước Lưới", (double) com.vanphuc.utils.ClientConfig.gridSize, 5.0, 30.0) {
        @Override
        public void setValue(Double value) {
            super.setValue(value);
            com.vanphuc.utils.ClientConfig.gridSize = value.intValue();
        }
    };

    // TẠO SETTING KEYBIND CHO CLICKGUI
    public static final KeybindSetting guiKeybindSetting = new KeybindSetting("Phím mở Menu", com.vanphuc.utils.ClientConfig.guiKey, null) {
        @Override
        public void setKey(int key, int modifiers) {
            super.setKey(key, modifiers);
            com.vanphuc.utils.ClientConfig.guiKey = key;
            com.vanphuc.utils.ClientConfig.guiMods = modifiers;
        }

        @Override
        public com.vanphuc.module.Module getModule() {
            // Trả về module giả để tránh NullPointerException khi KeybindComponent gọi getModule().name
            return new com.vanphuc.module.Module("Hệ Thống", "") {};
        }
    };

    public ConfigPage() {
        super("Config");
        initWindows();
    }

    private void initWindows() {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        float windowWidth = 140f;
        float windowHeight = 120f; // Tăng chiều cao để đủ chỗ cho keybind
        float startX = (screenWidth - windowWidth) / 2f;
        float startY = (screenHeight - windowHeight) / 2f;

        // DÙNG CLASS MỚI TỚ VIẾT BÊN DƯỚI
        ConfigWindow configWindow = new ConfigWindow("Client Settings", new Rectangle(startX, startY, windowWidth, windowHeight));

        configWindow.addChild(new BooleanComponent(snappingSetting));
        configWindow.addChild(new BooleanComponent(showGridSetting));
        configWindow.addChild(new SliderComponent(gridSizeSetting));

        // THÊM COMPONENT KEYBIND VÀO CỬA SỔ
        configWindow.addChild(new KeybindComponent(guiKeybindSetting));

        // QUAN TRỌNG: Gọi initialize để nó hiện nội dung lên (Fix ảnh 7a8688)
        configWindow.initialize();
        // Gọi arrange lần đầu để nó xếp hàng ngay ngắn
        configWindow.arrange(configWindow.getPosition());

        this.windows.add(configWindow);
    }

    // CLASS CON ĐỂ FIX LỖI "MỘT CHÙM" (Fix ảnh 7a83a3)
    private static class ConfigWindow extends Window {
        public ConfigWindow(String title, Rectangle position) {
            super(title, position);
        }

        @Override
        public void arrange(Rectangle finalSize) {
            this.position = finalSize;
            float currentY = finalSize.getY() + titleHeight + 6f; // Bắt đầu dưới Title
            float padding = 6f;
            float settingHeight = 20f;

            for (UIElement child : children) {
                // Xếp mỗi child cách nhau settingHeight (20px)
                child.arrange(new Rectangle(
                        finalSize.getX() + padding,
                        currentY,
                        finalSize.getWidth() - padding * 2,
                        settingHeight
                ));
                currentY += settingHeight + 2f; // Khoảng cách giữa các dòng
            }
        }
    }
}