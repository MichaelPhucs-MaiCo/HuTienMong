package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.module.settings.StringListSetting;
import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.window.NotepadWindow;
import com.vanphuc.gui.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoSwitchHotbar extends Module {
    public final StringListSetting listSetting = new StringListSetting("ListContent", new ArrayList<>(List.of("Slot 4 delay 5s", "Slot 5 delay 11s", "Slot 6 delay 8s")));
    private final List<Task> tasks = new ArrayList<>();

    // Quản lý Phase 1 (Startup)
    private int startupIndex = 0;
    private long nextStartupTime = 0;
    private boolean isStartupPhase = true;
    private int lastListHash = 0;

    public final ActionSetting openListSetting = new ActionSetting("List", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        MinecraftClient mc = MinecraftClient.getInstance();
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new NotepadWindow(this, new Rectangle(x, y, 250, 150)));
    });

    public AutoSwitchHotbar() {
        super("AutoSwitchHotbar", "Nhảy slot tự động với đồng hồ riêng biệt cho mỗi slot.", Items.REDSTONE_TORCH.getDefaultStack());
        addSetting(openListSetting);
        addSetting(listSetting);
    }

    public List<String> getListContent() {
        return listSetting.getValue();
    }

    public void updateList(List<String> newContent) {
        listSetting.setValue(newContent);
        parseTasks();
    }

    public List<Task> getTasks() {
        // Lấy mã Hash của cái list hiện tại trong Config
        int currentHash = listSetting.getValue().hashCode();

        // Nếu mã Hash khác với lần trước (tức là config vừa được load từ JSON, hoặc cậu vừa sửa GUI)
        // thì lập tức parse lại danh sách luôn!
        if (lastListHash != currentHash) {
            parseTasks();
            lastListHash = currentHash;
        }

        return tasks;
    }
    public boolean isStartupPhase() {
        return isStartupPhase;
    }

    public void parseTasks() {
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
        startupIndex = 0;
        nextStartupTime = 0;
        isStartupPhase = true;

        if (tasks.isEmpty()) {
            info("Danh sách trống hoặc sai cú pháp! Hãy kiểm tra lại ❌");
            toggle();
        } else {
            info("Bắt đầu scrips ⚡");
        }
    }

    @Override
    public void onUpdate() {
        if (!isActive() || tasks.isEmpty() || mc.player == null) return;

        long now = System.currentTimeMillis();

        // PHASE 1: STARTUP (Kích hoạt nhanh toàn bộ danh sách)
        if (isStartupPhase) {
            if (now >= nextStartupTime) {
                Task currentTask = tasks.get(startupIndex);

                // Thực hiện chuyển slot ngay lập tức
                mc.player.getInventory().selectedSlot = currentTask.slot - 1;
                currentTask.lastExecutedTime = now; // Bắt đầu tính delay từ lúc này

                startupIndex++;
                nextStartupTime = now + 500; // Delay 500ms giữa các slot lúc startup

                if (startupIndex >= tasks.size()) {
                    isStartupPhase = false;
                    info("Chuyển sang Phase 2: Chạy độc lập từng slot ⏱️");
                }
            }
            return; // Đang startup thì không chạy logic Phase 2
        }

        // PHASE 2: INDEPENDENT TIMERS (Thằng nào hết hạn thì thằng đó nhảy)
        for (Task task : tasks) {
            if (now - task.lastExecutedTime >= task.delayMs) {
                // Nhảy slot
                mc.player.getInventory().selectedSlot = task.slot - 1;
                // Cập nhật lại thời gian thực thi cuối cùng để bắt đầu chu kỳ mới
                task.lastExecutedTime = now;
            }
        }
    }

    public static class Task {
        public int slot;
        public long delayMs;
        public long lastExecutedTime = 0;

        public Task(int slot, int delaySeconds) {
            this.slot = slot;
            this.delayMs = delaySeconds * 1000L;
        }
    }
}