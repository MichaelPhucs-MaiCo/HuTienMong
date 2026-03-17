package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.navigation.HudWindow;
import com.vanphuc.module.Modules;
import com.vanphuc.module.modules.AutoSwitchHotbar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class AutoSwitchHotbarHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public AutoSwitchHotbarHud(int x, int y) {
        super("AutoSwitch HUD", x, y, 140, 20); // Width 140, Height sẽ tự scale
        this.minWidth = 140f;
        this.resizeMode = ResizeMode.None;
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        AutoSwitchHotbar module = Modules.get().get(AutoSwitchHotbar.class);

        // 1. Tự động co giãn chiều cao (Scale height) dựa theo số lượng slot
        int taskCount = (module != null && !module.getTasks().isEmpty()) ? module.getTasks().size() : 1;
        float newHeight = 12f + (taskCount * 12f); // Padding + số dòng * 12px
        this.position.setHeight(Math.max(20f, newHeight));
        this.maxHeight = newHeight;
        this.minHeight = newHeight;

        // Vẽ cái khung nền Sleek Carbon mờ mờ
        super.draw(context, partialTicks);
        if (!isVisible()) return;

        int renderX = (int) position.getX() + 6;
        int renderY = (int) position.getY() + 6;

        if (module != null && !module.getTasks().isEmpty()) {
            for (AutoSwitchHotbar.Task task : module.getTasks()) {
                String prefix = "§lChuyển skill " + task.slot + " sau: ";
                String timeText;
                int timeColor = 0xFFF1C40F; // Mặc định: Vàng tươi

                if (module.isActive()) {
                    if (module.isStartupPhase()) {
                        timeText = "§lSetup...";
                        timeColor = 0xFF3B82F6; // Xanh Blue lúc vừa bật (Phase 1)
                    } else {
                        // Tính toán thời gian còn lại (Phase 2)
                        long remainingMs = (task.lastExecutedTime + task.delayMs) - System.currentTimeMillis();
                        long remainingSec = Math.max(0, remainingMs / 1000);

                        timeText = "§l" + remainingSec + "s";

                        // Dưới 3s thì chuyển sang màu Đỏ báo động
                        if (remainingSec <= 3) {
                            timeColor = 0xFFFF5555;
                        }
                    }
                } else {
                    // Khi module tắt
                    timeText = "§l--";
                    timeColor = 0xFF888888; // Xám tro
                }

                // Vẽ text "Chuyển skill X sau: " (Màu Xanh Lá - 0xFF2ECC71)
                context.drawTextWithShadow(MC.textRenderer, prefix, renderX, renderY, 0xFF2ECC71);

                // Lấy độ dài của prefix để vẽ text thời gian nối tiếp vào
                int prefixWidth = MC.textRenderer.getWidth(prefix);

                // Vẽ thời gian (Vàng / Đỏ / Xám)
                context.drawTextWithShadow(MC.textRenderer, timeText, renderX + prefixWidth, renderY, timeColor);

                renderY += 12; // Nhích tọa độ Y xuống để vẽ dòng tiếp theo
            }
        } else {
            // Nếu danh sách trống chưa cấu hình
            context.drawTextWithShadow(MC.textRenderer, "Chưa cài list skill", renderX, renderY, 0xFF888888);
        }
    }
}