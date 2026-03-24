package com.vanphuc.gui.navigation.huds;

import com.vanphuc.gui.navigation.HudWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class LogConsoleHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // Class phụ để lưu nội dung và thời gian tạo log
    private static class LogEntry {
        String text;
        long timestamp;

        LogEntry(String text) {
            this.text = text;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final List<LogEntry> logs = new ArrayList<>();
    private static final long EXPIRE_TIME = 5000; // 5 giây (5000ms) là tự "bay màu"

    public LogConsoleHud(int x, int y) {
        super("Client Console", x, y, 250, 100);
        this.minWidth = 150f;
        this.minHeight = 50f;
        this.resizeMode = ResizeMode.WidthAndHeight;
    }

    public static void addLog(String message) {
        // Mỗi khi thêm log mới là mình cấp luôn cái "giấy khai sinh" timestamp cho nó
        logs.add(new LogEntry(message));

        // Giới hạn số lượng để danh sách không quá dài
        if (logs.size() > 50) {
            logs.remove(0);
        }
    }

    @Override
    public void draw(DrawContext context, float partialTicks) {
        // Dọn dẹp rác: Xóa những thằng đã "hết hạn sử dụng" (> 5s) khỏi list
        long now = System.currentTimeMillis();
        logs.removeIf(entry -> (now - entry.timestamp) > EXPIRE_TIME);

        // Gọi super để vẽ khung Sleek Carbon [cite: 02-10]
        super.draw(context, partialTicks);

        // Nếu không có log nào hoặc HUD đang tắt thì nghỉ khỏe
        if (!isVisible() || logs.isEmpty()) return;

        int renderX = (int) position.getX() + 6;
        int renderY = (int) position.getY() + 6;
        int fontHeight = MC.textRenderer.fontHeight + 2;

        // Tính toán hiển thị dựa trên kích thước khung hiện tại
        int maxVisibleLines = (int) ((position.getHeight() - 12) / fontHeight);
        int startIndex = Math.max(0, logs.size() - maxVisibleLines);

        for (int i = startIndex; i < logs.size(); i++) {
            LogEntry entry = logs.get(i);

            // Vẽ log lên HUD
            context.drawTextWithShadow(MC.textRenderer, entry.text, renderX, renderY, 0xFFFFFFFF);
            renderY += fontHeight;
        }
    }
}