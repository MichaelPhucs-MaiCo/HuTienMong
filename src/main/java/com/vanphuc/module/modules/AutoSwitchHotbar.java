package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.module.settings.StringListSetting;
import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.NotepadWindow;
import com.vanphuc.gui.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoSwitchHotbar extends Module {
    // Đã chuyển thành StringListSetting để được lưu tự động
    public final StringListSetting listSetting = new StringListSetting("ListContent", new ArrayList<>(List.of("Slot 1 delay 5s", "Slot 2 delay 10s")));
    private final List<Task> tasks = new ArrayList<>();

    private int currentTaskIndex = 0;
    private long nextActionTime = 0;
    private boolean isRandomDelayPhase = false;

    public final ActionSetting openListSetting = new ActionSetting("List", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        MinecraftClient mc = MinecraftClient.getInstance();
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new NotepadWindow(this, new Rectangle(x, y, 250, 150)));
    });

    public AutoSwitchHotbar() {
        super("AutoSwitchHotbar", "Nhảy slot tự động có chống delay.", Items.REDSTONE_TORCH.getDefaultStack());
        addSetting(openListSetting);
        addSetting(listSetting); // Phải add thì ConfigManager mới thấy để lưu nhé!
    }

    public List<String> getListContent() {
        return listSetting.getValue();
    }

    public void updateList(List<String> newContent) {
        listSetting.setValue(newContent);
        parseTasks();
    }

    private void parseTasks() {
        tasks.clear();
        Pattern p = Pattern.compile("(?i)slot\\s+(\\d+)\\s+delay\\s+(\\d+)s?");
        for (String line : listSetting.getValue()) {
            Matcher m = p.matcher(line.trim());
            if (m.find()) {
                int slot = Integer.parseInt(m.group(1));
                int delay = Integer.parseInt(m.group(2));
                tasks.add(new Task(slot, delay));
            }
        }
    }

    @Override
    public void onActivate() {
        super.onActivate();
        parseTasks();
        currentTaskIndex = 0;
        nextActionTime = 0;
        isRandomDelayPhase = false;

        if (tasks.isEmpty()) {
            info("Danh sách trống hoặc sai cú pháp! Hãy mở 'List' để xem lại.");
            toggle();
        } else {
            info("Bắt đầu kịch bản Switch Slot với " + tasks.size() + " hành động.");
        }
    }

    @Override
    public void onUpdate() {
        if (!isActive() || tasks.isEmpty() || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now >= nextActionTime) {
            if (isRandomDelayPhase) {
                Task task = tasks.get(currentTaskIndex);
                mc.player.getInventory().selectedSlot = task.slot - 1;
                nextActionTime = now + (task.delay * 1000L);
                isRandomDelayPhase = false;
            } else {
                currentTaskIndex++;
                if (currentTaskIndex >= tasks.size()) {
                    currentTaskIndex = 0;
                }
                long randomDelay = 500L + (long) (Math.random() * 1500L);
                nextActionTime = now + randomDelay;
                isRandomDelayPhase = true;
            }
        }
    }

    private static class Task {
        int slot;
        int delay;
        Task(int slot, int delay) {
            this.slot = slot;
            this.delay = delay;
        }
    }
}