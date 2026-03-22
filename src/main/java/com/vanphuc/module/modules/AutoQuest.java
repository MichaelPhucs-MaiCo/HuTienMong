package com.vanphuc.module.modules;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.window.windows.AutoQuestWindow;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.StringListSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoQuest extends Module {
    // --- SETTINGS MỚI ---
    public final NumberSetting actionDelay = new NumberSetting("Delay thao tác (ms)", 1000.0, 100.0, 5000.0);
    public final NumberSetting waitCycle = new NumberSetting("Đợi check lại sau (s)", 30.0, 1.0, 300.0);

    // --- SETTINGS CŨ ---
    public final StringListSetting questNameList = new StringListSetting("Tên Nhiệm Vụ", new ArrayList<>(List.of("Đề Thăng Căn Cơ")));
    public final StringListSetting bossBarNameList = new StringListSetting("Tên BossBar", new ArrayList<>(List.of("Đề Thăng Căn Cơ")));

    public final ActionSetting openQuestBtn = new ActionSetting("Sửa Tên Nhiệm Vụ", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        if (GuiManager.getInstance().activePage != null) {
            GuiManager.getInstance().activePage.windows.removeIf(w -> w instanceof AutoQuestWindow);
        }
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new AutoQuestWindow(this, questNameList, new Rectangle(x, y, 250, 150)));
    });

    public final ActionSetting openBossBarBtn = new ActionSetting("Sửa Tên BossBar", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        if (GuiManager.getInstance().activePage != null) {
            GuiManager.getInstance().activePage.windows.removeIf(w -> w instanceof AutoQuestWindow);
        }
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new AutoQuestWindow(this, bossBarNameList, new Rectangle(x, y, 250, 150)));
    });

    private State state = State.IDLE;
    private long delayTimer = 0L;
    private long cooldownEndTime = 0L;

    private final int[] IGNORED_SLOTS = {7, 8, 16, 17, 25, 26, 34, 35, 43, 44};

    public AutoQuest() {
        super("AutoQuest", "Nhận quest tự động", Items.WRITABLE_BOOK.getDefaultStack());
        addSetting(questNameList);
        addSetting(bossBarNameList);
        addSetting(actionDelay);
        addSetting(waitCycle);

        addSetting(openQuestBtn);
        addSetting(openBossBarBtn);
    }

    private String getTargetQuest() {
        return questNameList.getValue().isEmpty() ? "Đề Thăng Căn Cơ" : questNameList.getValue().get(0);
    }

    private String getTargetBossBar() {
        return bossBarNameList.getValue().isEmpty() ? "Đề Thăng Căn Cơ" : bossBarNameList.getValue().get(0);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        state = State.PHASE_1_OPEN_GUI;
        delayTimer = 0;
        cooldownEndTime = 0;
        info("Khởi động nhận Quest! Kiểm tra nhiệm vụ đang làm🕵️‍♂️");
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        state = State.IDLE;
        if (mc.currentScreen instanceof GenericContainerScreen) {
            mc.player.closeHandledScreen();
        }
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null) return;
        long now = System.currentTimeMillis();

        if (now < delayTimer) return;


        long msDelay = actionDelay.getValue().longValue();

        switch (state) {
            case PHASE_1_OPEN_GUI -> {
                mc.player.networkHandler.sendChatCommand("quest");
                state = State.PHASE_1_WAIT_GUI;
                delayTimer = now + msDelay;
            }
            case PHASE_1_WAIT_GUI -> {
                if (isQuestGuiOpen()) {
                    clickSlot(26);
                    state = State.PHASE_1_CHECK_ITEMS;
                    delayTimer = now + msDelay;
                } else if (now > delayTimer + (msDelay * 2)) { // Timeout linh hoạt dựa theo delay
                    state = State.PHASE_1_OPEN_GUI;
                }
            }
            case PHASE_1_CHECK_ITEMS -> {
                if (!isQuestGuiOpen()) { state = State.PHASE_1_OPEN_GUI; break; }
                int slot = scanForQuestItem();
                if (slot != -1) {
                    mc.player.closeHandledScreen();
                    state = State.WAIT_30S;
                    // Sử dụng giá trị từ setting waitCycle (giây sang ms) [cite: 1783]
                    long waitMs = waitCycle.getValue().longValue() * 1000L;
                    delayTimer = now + waitMs;
                    info(String.format("Quest đã được nhận, nghỉ %ds ☕", waitCycle.getValue().intValue()));
                } else {
                    state = State.PHASE_2_CLICK_35;
                }
            }
            case WAIT_30S -> {
                state = State.PHASE_1_OPEN_GUI;
            }

            case PHASE_2_CLICK_35 -> {
                if (!isQuestGuiOpen()) {
                    mc.player.networkHandler.sendChatCommand("quest");
                    delayTimer = now + msDelay;
                    return;
                }
                clickSlot(35);
                state = State.PHASE_2_CHECK_ITEMS;
                delayTimer = now + msDelay;
            }
            case PHASE_2_CHECK_ITEMS -> {
                if (!isQuestGuiOpen()) { state = State.PHASE_1_OPEN_GUI; break; }
                int slot = scanForQuestItem();
                if (slot != -1) {
                    clickSlot(slot);
                    state = State.PHASE_2_WAIT_BOSSBAR;
                    delayTimer = now + msDelay;
                } else {
                    state = State.PHASE_3_CLICK_17;
                }
            }
            case PHASE_2_WAIT_BOSSBAR -> {
                if (checkBossBar(getTargetBossBar())) {
                    mc.player.closeHandledScreen();
                    state = State.MONITOR_BOSSBAR;
                    info("Đã nhận Quest thành công! Check BossBar... ⚔️");
                } else if (now > delayTimer + (msDelay * 3)) {
                    state = State.PHASE_1_OPEN_GUI;
                }
            }

            case MONITOR_BOSSBAR -> {
                if (!checkBossBar(getTargetBossBar())) {
                    info("BossBar biến mất! Test lại Phase 1 🔄");
                    state = State.PHASE_1_OPEN_GUI;
                    delayTimer = now + msDelay;
                }
            }

            case PHASE_3_CLICK_17 -> {
                if (!isQuestGuiOpen()) { state = State.PHASE_1_OPEN_GUI; break; }
                clickSlot(17);
                state = State.PHASE_3_CHECK_ITEMS;
                delayTimer = now + msDelay;
            }
            case PHASE_3_CHECK_ITEMS -> {
                if (!isQuestGuiOpen()) { state = State.PHASE_1_OPEN_GUI; break; }
                int slot = scanForQuestItem();
                if (slot != -1) {
                    ItemStack stack = mc.player.currentScreenHandler.getSlot(slot).getStack();
                    String lore = getFullLore(stack);

                    if (lore.contains("Bạn có thể bắt đầu lại nhiệm vụ này!")) {
                        info("Có thể nhận nhiệm vụ. Quay về Phase 1 🏎️");
                        state = State.PHASE_1_OPEN_GUI;
                        delayTimer = now + msDelay;

                    } else if (lore.contains("Bạn có thể bắt đầu lại nhiệm vụ này sau")) {
                        long cdSeconds = extractTimeFromLore(lore);
                        cooldownEndTime = now + (cdSeconds * 1000L);
                        mc.player.closeHandledScreen();
                        state = State.COOLDOWN_WAIT;
                        info(String.format("Quest đang trong thời gian hồi! Đợi %d giây nữa⏳", cdSeconds));
                    } else {
                        state = State.PHASE_1_OPEN_GUI;
                    }
                } else {
                    mc.player.closeHandledScreen();
                    state = State.PHASE_1_OPEN_GUI;
                    delayTimer = now + (msDelay * 5); // Timeout dài khi không thấy item
                }
            }
            case COOLDOWN_WAIT -> {
                if (now >= cooldownEndTime) {
                    info("Hết thời gian đợi 💥");
                    state = State.PHASE_1_OPEN_GUI;
                }
            }
        }
    }

    private boolean isQuestGuiOpen() {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            String title = container.getTitle().getString();
            return title.contains("Nhiệm vụ của");
        }
        return false;
    }

    private void clickSlot(int slotId) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int scanForQuestItem() {
        if (!(mc.currentScreen instanceof GenericContainerScreen container)) return -1;
        String targetName = getTargetQuest();

        for (int i = 0; i <= 42; i++) {
            if (isIgnoredSlot(i)) continue;

            Slot slot = container.getScreenHandler().getSlot(i);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                String customName = stack.getName().getString();
                if (customName.contains(targetName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isIgnoredSlot(int id) {
        for (int i : IGNORED_SLOTS) {
            if (i == id) return true;
        }
        return false;
    }

    private boolean checkBossBar(String targetText) {
        var bossBars = ((com.vanphuc.mixin.BossBarHudAccessor) mc.inGameHud.getBossBarHud()).getBossBars();

        for (net.minecraft.client.gui.hud.ClientBossBar bossBar : bossBars.values()) {
            if (bossBar.getName().getString().contains(targetText)) {
                return true;
            }
        }
        return false;
    }

    private String getFullLore(ItemStack stack) {
        StringBuilder sb = new StringBuilder();
        if (stack.contains(DataComponentTypes.LORE)) {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            for (Text line : lore.lines()) {
                sb.append(line.getString()).append(" ");
            }
        }
        return sb.toString();
    }

    private long extractTimeFromLore(String lore) {
        long totalSecs = 0;

        Matcher h = Pattern.compile("(\\d+)\\s*giờ").matcher(lore);
        if (h.find()) totalSecs += Long.parseLong(h.group(1)) * 3600;

        Matcher m = Pattern.compile("(\\d+)\\s*phút").matcher(lore);
        if (m.find()) totalSecs += Long.parseLong(m.group(1)) * 60;

        Matcher s = Pattern.compile("(\\d+)\\s*giây").matcher(lore);
        if (s.find()) totalSecs += Long.parseLong(s.group(1));

        return totalSecs > 0 ? totalSecs : 60;
    }

    public long getRemainingCooldown() {
        if (!isActive()) return -1;
        long now = System.currentTimeMillis();

        if (state == State.COOLDOWN_WAIT) {
            return Math.max(0, (cooldownEndTime - now) / 1000L);
        } else if (state == State.WAIT_30S) {
            return Math.max(0, (delayTimer - now) / 1000L);
        }
        return -1;
    }

    public String getCurrentStateName() {
        return state.name();
    }

    private enum State {
        IDLE,
        PHASE_1_OPEN_GUI, PHASE_1_WAIT_GUI, PHASE_1_CHECK_ITEMS,
        WAIT_30S,
        PHASE_2_CLICK_35, PHASE_2_CHECK_ITEMS, PHASE_2_WAIT_BOSSBAR,
        MONITOR_BOSSBAR,
        PHASE_3_CLICK_17, PHASE_3_CHECK_ITEMS,
        COOLDOWN_WAIT
    }
}