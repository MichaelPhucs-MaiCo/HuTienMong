package com.vanphuc.module.modules;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.TrashNotepadWindow;
import com.vanphuc.gui.window.windows.SaveNotepadWindow;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.*;
import com.vanphuc.utils.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import java.util.*;

public class AutoSavePaper extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public final ActionSetting openTrashBtn = new ActionSetting("Mở Danh Sách Trash", () -> openNotepad(true));
    public final ActionSetting openSaveBtn = new ActionSetting("Mở Danh Sách Save", () -> openNotepad(false));
    public final StringSetting checkSlotsSetting = new StringSetting("Slots Check", "7-8-9");
    public final NumberSetting saveTimeMinutes = new NumberSetting("Time Loop (min)", 5.0, 1.0, 120.0);
    public final NumberSetting actionDelayMs = new NumberSetting("Delay thao tác (ms)", 500.0, 100.0, 2000.0);
    public final BooleanSetting passiveEnabled = new BooleanSetting("Tự động cất đồ khi tự mở kho", true);
    public final NumberSetting passiveDelaySetting = new NumberSetting("Delay cất đồ khi tự mở kho (s)", 5.0, 1.0, 60.0);
    public final StringSetting resetSlotSetting = new StringSetting("Quay lại slot (1-9)", "1");
    public final StringListSetting trashListSetting = new StringListSetting("TrashList", new ArrayList<>());
    public final StringListSetting saveListSetting = new StringListSetting("SaveList", new ArrayList<>());

    private long nextCycleTime = 0L;
    private CycleState cycleState = CycleState.IDLE;
    private long delayTimer = 0L;
    private long passiveTimer = 0L;
    private String lastGuiTitle = "";

    // THÊM BIẾN NÀY ĐỂ CHỐNG TREO
    private long stateTimeoutTimer = 0L;

    public AutoSavePaper() {
        super("AutoSavePaper", "Tự động cất đồ & dọn dẹp rác 📦", Items.PAPER.getDefaultStack());
        setupSettings();
        setupCommands();
    }

    private void setupSettings() {
        addSetting(checkSlotsSetting);
        addSetting(saveTimeMinutes);
        addSetting(actionDelayMs);
        addSetting(passiveEnabled);
        addSetting(passiveDelaySetting);
        addSetting(resetSlotSetting);
        addSetting(openTrashBtn);
        addSetting(openSaveBtn);
    }

    private void setupCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("vpSave")
                    .then(ClientCommandManager.literal("trash").then(ClientCommandManager.literal("add").executes(ctx -> addItemToList(true))))
                    .then(ClientCommandManager.literal("save").then(ClientCommandManager.literal("add").executes(ctx -> addItemToList(false))))
            );
        });
    }

    public List<String> getListContent() {
        return trashListSetting.getValue();
    }

    public void updateList(List<String> newContent) {
        trashListSetting.setValue(new ArrayList<>(newContent));
    }

    public long getRemainingSeconds() {
        // Fix hiển thị: Chỉ hiện 0 khi thực sự đang làm việc, nếu kẹt thì vẫn tính toán tiếp
        if (cycleState != CycleState.IDLE) return 0;
        long diff = nextCycleTime - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        resetCycleState(System.currentTimeMillis()); // Reset sạch sẽ khi bật
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        cycleState = CycleState.IDLE;
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null) {
            // Nếu bị văng khỏi server, đưa về IDLE ngay lập tức
            if (cycleState != CycleState.IDLE) cycleState = CycleState.IDLE;
            return;
        }

        long now = System.currentTimeMillis();

        // CHỐT CHẶN TIMEOUT: Nếu kẹt ở một trạng thái quá 15 giây mà không xong thì reset
        if (cycleState != CycleState.IDLE && now > stateTimeoutTimer) {
            error("Module bị kẹt quá lâu! Đang tự động reset về IDLE... 🛠️");
            resetCycleState(now);
            return;
        }

        if (now >= nextCycleTime && cycleState == CycleState.IDLE) {
            startNewCycle(now);
        }

        handlePassiveLogic(now);

        if (now < delayTimer) return;
        handleStateMachine(now);
    }

    private void handleStateMachine(long now) {
        switch (cycleState) {
            case START_SAVE_PHASE -> {
                int gioiChiSlot = findGioiChiInHotbar();
                if (gioiChiSlot != -1) {
                    mc.player.getInventory().selectedSlot = gioiChiSlot;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    cycleState = CycleState.WAITING_SAVE_GUI;
                    delayTimer = now + 1000L;
                } else {
                    ChatUtils.sendPlayerMsg("/ec");
                    cycleState = CycleState.WAITING_SAVE_GUI;
                    delayTimer = now + 1000L;
                }
            }
            case WAITING_SAVE_GUI -> {
                if (mc.currentScreen instanceof GenericContainerScreen container) {
                    String title = container.getTitle().getString();
                    if (title.contains("Vault") || title.contains("Ender Chest") || title.contains("Kho")) {
                        mc.player.getInventory().selectedSlot = getResetSlot();
                        autoDepositItems(container, true);
                        delayTimer = now + actionDelayMs.getValue().longValue();
                        cycleState = CycleState.CLOSE_SAVE_GUI;
                        stateTimeoutTimer = now + 15000L; // Reset timeout cho bước tiếp theo
                    }
                }
            }
            case CLOSE_SAVE_GUI -> {
                mc.player.closeHandledScreen();
                cycleState = CycleState.START_TRASH_PHASE;
                delayTimer = now + actionDelayMs.getValue().longValue();
                stateTimeoutTimer = now + 15000L;
            }
            case START_TRASH_PHASE -> {
                ChatUtils.sendPlayerMsg("/trash");
                cycleState = CycleState.WAITING_TRASH_GUI;
                delayTimer = now + 1000L;
            }
            case WAITING_TRASH_GUI -> {
                if (mc.currentScreen instanceof GenericContainerScreen container &&
                        (container.getTitle().getString().contains("Disposal") || container.getTitle().getString().contains("Thùng rác"))) {
                    autoDepositItems(container, false);
                    delayTimer = now + actionDelayMs.getValue().longValue();
                    cycleState = CycleState.FINISH_CYCLE;
                    stateTimeoutTimer = now + 15000L;
                }
            }
            case FINISH_CYCLE -> {
                mc.player.closeHandledScreen();
                resetCycleState(now);
                info("Đã hoàn thành cất đồ & dọn rác! ✅");
            }
        }
    }

    // Hàm hỗ trợ reset trạng thái an toàn
    private void resetCycleState(long now) {
        cycleState = CycleState.IDLE;
        nextCycleTime = now + (long)(saveTimeMinutes.getValue() * 60000L);
        stateTimeoutTimer = 0;
    }

    private void startNewCycle(long now) {
        cycleState = CycleState.START_SAVE_PHASE;
        stateTimeoutTimer = now + 15000L; // Cho phép tối đa 15s để hoàn thành chu kỳ
        info("Bắt đầu chu kỳ dọn dẹp mới! 🚀");
    }

    private void handlePassiveLogic(long now) {
        if (!passiveEnabled.isEnabled() || cycleState != CycleState.IDLE) return;

        if (mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();

            if ((title.contains("Vault") || title.contains("Ender Chest")) && !lastGuiTitle.equals(title)) {

                if (passiveTimer == 0) {
                    passiveTimer = now + (long)(passiveDelaySetting.getValue() * 1000L);
                }

                if (now >= passiveTimer) {
                    autoDepositItems(container, true);
                    lastGuiTitle = title;
                    passiveTimer = 0;
                    info("Cơ chế Passive: Đã tự động cất đồ quý!");
                }
            }
        } else {
            passiveTimer = 0;
            lastGuiTitle = "";
        }
    }

    private void autoDepositItems(GenericContainerScreen container, boolean isSavePhase) {
        int startIndex = Math.max(0, container.getScreenHandler().slots.size() - 36);
        for (int i = startIndex; i < container.getScreenHandler().slots.size(); i++) {
            Slot slot = container.getScreenHandler().getSlot(i);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                if (isSavePhase ? shouldSave(stack) : shouldTrash(stack)) {
                    mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    private boolean shouldSave(ItemStack stack) {
        if (isProtectedItem(stack)) return false;

        String itemKey = makeItemKey(stack);
        if (trashListSetting.getValue().contains(itemKey)) return false;

        if (saveListSetting.getValue().isEmpty()) return true;
        return saveListSetting.getValue().contains(itemKey);
    }

    private boolean shouldTrash(ItemStack stack) {
        if (isProtectedItem(stack)) return false;

        return trashListSetting.getValue().contains(makeItemKey(stack));
    }

    private boolean isProtectedItem(ItemStack stack) {
        String name = stack.getName().getString();
        if (name.contains("Giới Chỉ") || name.contains("Menu")) return true;

        Item item = stack.getItem();
        return item instanceof MiningToolItem || item instanceof ArmorItem ||
                item instanceof SwordItem || item instanceof BowItem ||
                item instanceof ShieldItem || item instanceof TridentItem ||
                item == Items.TOTEM_OF_UNDYING;
    }

    private int findGioiChiInHotbar() {
        List<Integer> slots = getParsedSlots();
        for (int slotIdx : slots) {
            ItemStack stack = mc.player.getInventory().getStack(slotIdx);
            if (!stack.isEmpty() && stack.getName().getString().contains("Giới Chỉ")) {
                return slotIdx;
            }
        }
        return -1;
    }

    private List<Integer> getParsedSlots() {
        List<Integer> result = new ArrayList<>();
        String input = checkSlotsSetting.getValue().trim().isEmpty() ? "7-8-9" : checkSlotsSetting.getValue();
        try {
            for (String s : input.split("-")) {
                result.add(Integer.parseInt(s.trim()) - 1);
            }
        } catch (Exception e) { result.addAll(List.of(6, 7, 8)); }
        return result;
    }

    private void openNotepad(boolean isTrash) {
        GuiManager.getInstance().closeSettingsWindows();
        float x = (mc.getWindow().getScaledWidth() - 300) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;

        if (isTrash) {
            GuiManager.getInstance().addWindow(new TrashNotepadWindow(this, new Rectangle(x, y, 300, 150)));
        } else {
            GuiManager.getInstance().addWindow(new SaveNotepadWindow(this, new Rectangle(x, y, 300, 150)));
        }
    }

    private int addItemToList(boolean isTrash) {
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) { error("Phải cầm item trên tay!"); return 0; }
        String key = makeItemKey(stack);
        List<String> list = isTrash ? trashListSetting.getValue() : saveListSetting.getValue();
        if (!list.contains(key)) {
            list.add(key);
            if (isTrash) trashListSetting.setValue(list); else saveListSetting.setValue(list);
            info("Đã thêm " + stack.getName().getString() + " vào " + (isTrash ? "Trash" : "Save") + " List.");
        }
        return 1;
    }

    private String makeItemKey(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).toString() + "|" + stack.getName().getString();
    }

    private int getResetSlot() {
        try {
            int slot = Integer.parseInt(resetSlotSetting.getValue().trim()) - 1;
            return Math.max(0, Math.min(8, slot));
        } catch (Exception e) {
            return 0;
        }
    }

    private enum CycleState { IDLE, START_SAVE_PHASE, WAITING_SAVE_GUI, CLOSE_SAVE_GUI, START_TRASH_PHASE, WAITING_TRASH_GUI, FINISH_CYCLE }
}