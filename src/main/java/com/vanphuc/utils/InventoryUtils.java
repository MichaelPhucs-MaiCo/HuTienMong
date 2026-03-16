package com.vanphuc.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        // Quét toàn bộ từ Hotbar (0-8) đến Inventory chính (9-35)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int findEmptyHotbarSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void moveToHotbar(int invSlot) {
        if (mc.player == null || mc.interactionManager == null) return;

        // 1. Nếu item đã nằm ở Hotbar (slot 0-8) thì chỉ cần cầm lên
        if (invSlot < 9) {
            mc.player.getInventory().selectedSlot = invSlot;
            return;
        }

        // 2. Tìm slot trống ở Hotbar, nếu đầy thì đè luôn vào slot 0
        int emptyHotbar = findEmptyHotbarSlot();
        int targetHotbar = (emptyHotbar != -1) ? emptyHotbar : 0;

        // 3. TUYỆT CHIÊU CỦA METEOR CLIENT: Dùng SWAP
        // Action SWAP sẽ dùng biến thứ 3 (button) làm vị trí hotbar (0-8)
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                invSlot,        // Slot đang chứa Kim Cương (9-35)
                targetHotbar,   // Ném vào Hotbar thứ mấy (0-8)
                SlotActionType.SWAP,
                mc.player
        );

        // 4. Ép người chơi cầm luôn cái item vừa swap xuống hotbar
        mc.player.getInventory().selectedSlot = targetHotbar;
    }
}