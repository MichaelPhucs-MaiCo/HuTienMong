package com.vanphuc.module.modules;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.TrashNotepadWindow;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.StringListSetting;
import com.vanphuc.utils.ChatUtils;
import com.vanphuc.utils.InventoryUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.registry.Registries;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import java.util.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;

    public class AutoSavePaper extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Settings
    public final StringListSetting trashListSetting = new StringListSetting("TrashList", new ArrayList<>());
    public final NumberSetting saveTimeMinutes = new NumberSetting("SaveTimeMinutes", 5.0, 1.0, 120.0);
    public final ActionSetting openListSetting;
    public final ActionSetting clearTrashSetting;

    // State machine / runtime
    private long lastSaveTime = 0L;
    private long nextCycleTime = 0L;
    private CycleState cycleState = CycleState.IDLE;
    private long delayTimer = 0L;
    private final Set<String> visitedVaultItemKeys = new HashSet<>();

        public AutoSavePaper() {
            // Thêm Items.PAPER.getDefaultStack() vào đây để nó hiện icon Tờ Giấy nhé
            super("AutoSavePaper", "Auto deposit & auto trash helper (port from AutoSavePaperMod).", Items.PAPER.getDefaultStack());

            openListSetting = new ActionSetting("Trash List", () -> {
                GuiManager.getInstance().closeSettingsWindows();

                // XÓA CỬA SỔ CŨ ĐỂ KHÔNG BỊ SPAM NHIỀU CỬA SỔ CÙNG LÚC
                if (GuiManager.getInstance().activePage != null) {
                    GuiManager.getInstance().activePage.windows.removeIf(w -> w instanceof TrashNotepadWindow);
                }

                float x = (mc.getWindow().getScaledWidth() - 300) / 2f;
                float y = mc.getWindow().getScaledHeight() / 3f;
                GuiManager.getInstance().addWindow(new TrashNotepadWindow(this, new Rectangle(x, y, 300, 150)));
            });

        clearTrashSetting = new ActionSetting("Clear Trash", () -> {
            trashListSetting.getValue().clear();
            cycleState = CycleState.IDLE;
            ChatUtils.info(this, "Đã xóa toàn bộ danh sách rác. Hệ thống Auto-Trash đã TẮT hoàn toàn.");
            // Cập nhật lại UI nếu đang mở
            if (GuiManager.getInstance().activePage != null) {
                for (com.vanphuc.gui.Window w : GuiManager.getInstance().activePage.windows) {
                    if (w instanceof TrashNotepadWindow tnw) tnw.refresh();
                }
            }
        });

        addSetting(openListSetting);
        addSetting(trashListSetting);
        addSetting(clearTrashSetting);
        addSetting(saveTimeMinutes);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("mItem")
                    .then(ClientCommandManager.literal("trash")
                            .then(ClientCommandManager.literal("add").executes(ctx -> {
                                if (mc.player != null) {
                                    ItemStack handStack = mc.player.getMainHandStack();
                                    if (!handStack.isEmpty()) {
                                        String itemId = Registries.ITEM.getId(handStack.getItem()).toString();
                                        String itemKey = itemId + "|" + handStack.getName().getString();
                                        List<String> list = trashListSetting.getValue();
                                        if (!list.contains(itemKey)) {
                                            list.add(itemKey);
                                            trashListSetting.setValue(list);

                                            // LÀM MỚI GIAO DIỆN NGAY LẬP TỨC
                                            if (GuiManager.getInstance().activePage != null) {
                                                for (com.vanphuc.gui.Window w : GuiManager.getInstance().activePage.windows) {
                                                    if (w instanceof TrashNotepadWindow tnw) {
                                                        tnw.refresh();
                                                    }
                                                }
                                            }
                                        }
                                        if (trashListSetting.getValue().size() == 1) {
                                            nextCycleTime = System.currentTimeMillis() + (long)(saveTimeMinutes.getValue().doubleValue() * 60.0 * 1000.0);
                                            sendModuleMsg("Hệ thống Auto-Trash đã được BẬT. Bắt đầu đếm ngược!");
                                        }
                                        sendModuleMsg("Đã thêm vào danh sách rác: " + handStack.getName().getString());
                                    } else {
                                        sendModuleMsg("Bạn phải cầm một item trên tay!");
                                    }
                                }
                                return 1;
                            }))
                            .then(ClientCommandManager.literal("list").executes(ctx -> {
                                openListSetting.execute();
                                return 1;
                            }))
                    )
            );
        });
    }

    private void sendModuleMsg(String msg) {
        ChatUtils.info(this, msg);
    }

    // --- Public accessors used by TrashNotepadWindow/AutoSavePaperHud ---
    public List<String> getListContent() {
        return trashListSetting.getValue();
    }

    public void updateList(List<String> newContent) {
        trashListSetting.setValue(new ArrayList<>(newContent));
    }

    // For HUD
    public long getRemainingSeconds() {
        if (!isActive() || trashListSetting.getValue().isEmpty()) return -1;
        long rem = (nextCycleTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0L, rem);
    }

    // --- rest of logic (state machine) uses same code as previous version ---
    @Override
    public void onActivate() {
        super.onActivate();
        if (!trashListSetting.getValue().isEmpty()) {
            nextCycleTime = System.currentTimeMillis() + (long)(saveTimeMinutes.getValue().doubleValue() * 60.0 * 1000.0);
            sendModuleMsg("AutoSavePaper bật. Chu kỳ đặt: " + (int)saveTimeMinutes.getValue().doubleValue() + " phút.");
        } else {
            sendModuleMsg("AutoSavePaper bật nhưng danh sách rác đang trống.");
        }
        visitedVaultItemKeys.clear();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        cycleState = CycleState.IDLE;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();

        if (!trashListSetting.getValue().isEmpty()) {
            if (now >= nextCycleTime && cycleState == CycleState.IDLE) {
                if (mc.currentScreen != null) mc.player.closeHandledScreen();
                visitedVaultItemKeys.clear();
                cycleState = CycleState.START_SAVE_PHASE;
            }

            switch (cycleState) {
                case IDLE: break;
                case START_SAVE_PHASE: continueSavePhase(); break;
                case WAITING_VAULT_GUI:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Vault")) {
                            cycleState = CycleState.DELAY_BEFORE_VAULT_DEPOSIT;
                            delayTimer = now + 1000L;
                        }
                    } else if (now >= delayTimer) {
                        continueSavePhase();
                    }
                    break;
                case DELAY_BEFORE_VAULT_DEPOSIT:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Vault")) {
                            if (now >= delayTimer) {
                                autoDepositHotbar(container.getScreenHandler());
                                cycleState = CycleState.WAITING_VAULT_SERVER_RESPONSE;
                                delayTimer = now + 1000L;
                            }
                        }
                    } else {
                        continueSavePhase();
                    }
                    break;
                case WAITING_VAULT_SERVER_RESPONSE:
                    if (now >= delayTimer) {
                        if (mc.currentScreen instanceof GenericContainerScreen container) {
                            if (hasItemsToSave(container.getScreenHandler())) {
                                mc.player.closeHandledScreen();
                                continueSavePhase();
                            } else {
                                mc.player.closeHandledScreen();
                                cycleState = CycleState.START_TRASH_PHASE;
                            }
                        } else {
                            continueSavePhase();
                        }
                    }
                    break;
                case WAITING_EC_GUI:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Ender Chest")) {
                            cycleState = CycleState.DELAY_BEFORE_EC_DEPOSIT;
                            delayTimer = now + 1000L;
                        }
                    } else if (now >= delayTimer) {
                        cycleState = CycleState.START_TRASH_PHASE;
                    }
                    break;
                case DELAY_BEFORE_EC_DEPOSIT:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Ender Chest")) {
                            if (now >= delayTimer) {
                                autoDepositHotbar(container.getScreenHandler());
                                cycleState = CycleState.WAITING_EC_SERVER_RESPONSE;
                                delayTimer = now + 1000L;
                            }
                        }
                    } else {
                        cycleState = CycleState.START_TRASH_PHASE;
                    }
                    break;
                case WAITING_EC_SERVER_RESPONSE:
                    if (now >= delayTimer) {
                        if (mc.currentScreen != null) mc.player.closeHandledScreen();
                        cycleState = CycleState.START_TRASH_PHASE;
                    }
                    break;
                case START_TRASH_PHASE:
                    if (mc.getNetworkHandler() != null) {
                        ChatUtils.sendPlayerMsg("/trash");
                        cycleState = CycleState.WAITING_TRASH_GUI;
                        delayTimer = now + 3000L;
                    } else {
                        finishCycle(now);
                    }
                    break;
                case WAITING_TRASH_GUI:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Disposal")) {
                            cycleState = CycleState.DELAY_BEFORE_TRASH_DUMP;
                            delayTimer = now + 1000L;
                        }
                    } else if (now >= delayTimer) {
                        finishCycle(now);
                    }
                    break;
                case DELAY_BEFORE_TRASH_DUMP:
                    if (mc.currentScreen instanceof GenericContainerScreen container) {
                        if (container.getTitle().getString().contains("Disposal")) {
                            if (now >= delayTimer) {
                                dumpTrashToContainer(container.getScreenHandler());
                                cycleState = CycleState.WAITING_TRASH_SERVER_RESPONSE;
                                delayTimer = now + 1000L;
                            }
                        }
                    } else {
                        finishCycle(now);
                    }
                    break;
                case WAITING_TRASH_SERVER_RESPONSE:
                    if (now >= delayTimer) {
                        if (mc.currentScreen != null) mc.player.closeHandledScreen();
                        finishCycle(now);
                    }
                    break;
            }
        } else if (cycleState != CycleState.IDLE) {
            cycleState = CycleState.IDLE;
        }

        if (mc.currentScreen instanceof GenericContainerScreen container) {
            if (cycleState == CycleState.IDLE) {
                String title = container.getTitle().getString();
                if ((title.contains("Ender Chest") || title.contains("Vault")) && now - lastSaveTime >= 5000L) {
                    lastSaveTime = now;
                    autoDepositHotbar(container.getScreenHandler());
                }
            }
        }
    }

    // helpers (same as prior version)...
    private void continueSavePhase() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        int foundSlot = -1;
        String foundItemKey = null;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && isGioiChi(stack)) {
                String itemKey = makeItemKey(stack);
                if (!visitedVaultItemKeys.contains(itemKey)) {
                    foundSlot = i;
                    foundItemKey = itemKey;
                    break;
                }
            }
        }

        if (foundSlot == -1) {
            for (int i = 9; i < 36; ++i) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty() && isGioiChi(stack)) {
                    String itemKey = makeItemKey(stack);
                    if (!visitedVaultItemKeys.contains(itemKey)) {
                        foundSlot = i;
                        foundItemKey = itemKey;
                        break;
                    }
                }
            }
        }

        if (foundSlot != -1) {
            visitedVaultItemKeys.add(foundItemKey);
            openGioiChi(player, foundSlot);
            cycleState = CycleState.WAITING_VAULT_GUI;
            delayTimer = System.currentTimeMillis() + 3000L;
        } else if (mc.getNetworkHandler() != null) {
            ChatUtils.sendPlayerMsg("/ec");
            cycleState = CycleState.WAITING_EC_GUI;
            delayTimer = System.currentTimeMillis() + 2000L;
        }
    }

    private boolean isGioiChi(ItemStack stack) {
        return stack.getItem() == Items.RECOVERY_COMPASS && stack.getName().getString().contains("Giới Chỉ");
    }

    private void openGioiChi(ClientPlayerEntity player, int slotIndex) {
        if (mc.interactionManager == null) return;

        if (slotIndex < 9) {
            player.getInventory().selectedSlot = slotIndex;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotIndex));
            }
        } else {
            InventoryUtils.moveToHotbar(slotIndex);
            if (mc.getNetworkHandler() != null && mc.player != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
    }

    private void finishCycle(long now) {
        cycleState = CycleState.IDLE;
        nextCycleTime = now + (long)(saveTimeMinutes.getValue().doubleValue() * 60.0 * 1000.0);
        if (mc.player != null) {
            mc.player.getInventory().selectedSlot = 0;
            if (mc.getNetworkHandler() != null) mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(0));
        }
        sendModuleMsg("Đã hoàn tất chu trình cất đồ & dọn rác!");
    }

    private boolean hasItemsToSave(ScreenHandler handler) {
        int startIndex = Math.max(0, handler.slots.size() - 36);
        for (int i = startIndex; i < handler.slots.size(); ++i) {
            Slot slot = handler.getSlot(i);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                if (shouldSave(stack)) return true;
            }
        }
        return false;
    }

    private boolean shouldSave(ItemStack stack) {
        Item item = stack.getItem();
        String itemId = Registries.ITEM.getId(item).toString();
        String itemKey = itemId + "|" + stack.getName().getString();

        // Thay ToolItem bằng MiningToolItem cho MC 1.21.4
        boolean isProtected = item instanceof MiningToolItem || item instanceof ArmorItem ||
                item instanceof SwordItem || item instanceof BowItem ||
                item instanceof ShieldItem || item instanceof TridentItem ||
                item == Items.TOTEM_OF_UNDYING;

        return !isProtected && !trashListSetting.getValue().contains(itemKey);
    }

    private void autoDepositHotbar(ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null) return;

        int startIndex = Math.max(0, handler.slots.size() - 36);
        for (int i = startIndex; i < handler.slots.size(); ++i) {
            Slot slot = handler.getSlot(i);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                if (shouldSave(stack)) {
                    mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    private void dumpTrashToContainer(ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null) return;

        int startIndex = Math.max(0, handler.slots.size() - 36);
        for (int i = startIndex; i < handler.slots.size(); ++i) {
            Slot slot = handler.getSlot(i);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                String itemKey = Registries.ITEM.getId(stack.getItem()).toString() + "|" + stack.getName().getString();
                if (trashListSetting.getValue().contains(itemKey)) {
                    mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    private String makeItemKey(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).toString() + "|" + stack.getName().getString();
    }

    private enum CycleState {
        IDLE,
        START_SAVE_PHASE,
        WAITING_VAULT_GUI,
        DELAY_BEFORE_VAULT_DEPOSIT,
        WAITING_VAULT_SERVER_RESPONSE,
        WAITING_EC_GUI,
        DELAY_BEFORE_EC_DEPOSIT,
        WAITING_EC_SERVER_RESPONSE,
        START_TRASH_PHASE,
        WAITING_TRASH_GUI,
        DELAY_BEFORE_TRASH_DUMP,
        WAITING_TRASH_SERVER_RESPONSE
    }
}