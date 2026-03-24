package com.vanphuc.utils;

import com.vanphuc.module.Module;
import com.vanphuc.gui.navigation.huds.LogConsoleHud;
import net.minecraft.client.MinecraftClient;

/**
 * ChatUtils – Hệ thống thông báo độc quyền của Hư Tiên Mộng. 🚀
 */
public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Thêm §l để in đậm chữ, §r để reset lại trạng thái bình thường cho phần sau ngoặc
    private static final String PREFIX = "§7[§b§lHutienMong§r§7] §f";
    private static final String DEBUG_PREFIX = "§7[§a§lDebug ⚙️§r§7] §7";
    private static final String ERROR_PREFIX = "§7[§c§lBug ❌§r§7] §f";

    // --- HÀM GỬI LỆNH/CHAT ---
    public static void sendPlayerMsg(String message) {
        if (mc.player == null || message == null) return;

        if (message.startsWith("#") || message.startsWith("/")) {
            // Với MC 1.21.4, việc gửi command/chat cần qua networkHandler
            if (message.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(message.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(message);
            }
        } else {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    // ============================================================
    // DẠNG 1: LOG CHUNG
    // ============================================================
    public static void addModMessage(String message) {
        logToConsole(PREFIX + message);
    }

    // Hàm bonus: In đậm toàn bộ nội dung tin nhắn luôn 🔥
    public static void addModMessageBold(String message) {
        logToConsole(PREFIX + "§l" + message);
    }

    public static void error(String message) {
        logToConsole(ERROR_PREFIX + message);
    }

    public static void debug(String message) {
        logToConsole(DEBUG_PREFIX + message);
    }

    // ============================================================
    // DẠNG 2: LOG THEO MODULE (Sử dụng module.name của Hư Tiên Mộng)
    // ============================================================
    public static void info(Module module, String message) {
        // Tên module sẽ được in đậm cực cháy
        String modulePrefix = "§7[§b§l" + module.name + "§r§7] §f";
        logToConsole(modulePrefix + message);
    }

    public static void error(Module module, String message) {
        String modulePrefix = "§7[§c§l" + module.name + " ❌§r§7] §f";
        logToConsole(modulePrefix + message);
    }

    // ============================================================
    // DẠNG 3: DÀNH CHO MIXIN (Prefix tùy biến)
    // ============================================================
    public static void info(String prefixName, String message) {
        String fullPrefix = "§7[§d§l" + prefixName + "§r§7] §f";
        logToConsole(fullPrefix + message);
    }

    // ============================================================
    // HÀM XỬ LÝ LÕI MỚI - ĐẨY VÀO HUD THAY VÌ KÊNH CHAT
    // ============================================================
    private static void logToConsole(String fullMsg) {
        // Ném thẳng đoạn text đã có màu mè vào HUD
        LogConsoleHud.addLog(fullMsg);

        // (Tùy chọn) Nếu Khầy muốn log nó hiện cả ở màn hình Console của VSCode/IntelliJ để dễ soi lỗi code thì mở comment dòng dưới:
        // System.out.println(fullMsg.replaceAll("§[0-9a-fk-or]", ""));
    }
}