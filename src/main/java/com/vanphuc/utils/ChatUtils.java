package com.vanphuc.utils;

import com.vanphuc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

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
        logToChat(PREFIX + message);
    }

    // Hàm bonus: In đậm toàn bộ nội dung tin nhắn luôn 🔥
    public static void addModMessageBold(String message) {
        logToChat(PREFIX + "§l" + message);
    }

    public static void error(String message) {
        logToChat(ERROR_PREFIX + message);
    }

    public static void debug(String message) {
        logToChat(DEBUG_PREFIX + message);
    }

    // ============================================================
    // DẠNG 2: LOG THEO MODULE (Sử dụng module.name của Hư Tiên Mộng)
    // ============================================================
    public static void info(Module module, String message) {
        // Tên module sẽ được in đậm cực cháy
        String modulePrefix = "§7[§b§l" + module.name + "§r§7] §f";
        logToChat(modulePrefix + message);
    }

    public static void error(Module module, String message) {
        String modulePrefix = "§7[§c§l" + module.name + " ❌§r§7] §f";
        logToChat(modulePrefix + message);
    }

    // ============================================================
    // DẠNG 3: DÀNH CHO MIXIN (Prefix tùy biến)
    // ============================================================
    public static void info(String prefixName, String message) {
        String fullPrefix = "§7[§d§l" + prefixName + "§r§7] §f";
        logToChat(fullPrefix + message);
    }

    // Hàm phụ trợ gửi tin nhắn vào khung chat Minecraft
    private static void logToChat(String fullMsg) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of(fullMsg), false);
        }
    }
}