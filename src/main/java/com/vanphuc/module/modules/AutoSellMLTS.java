package com.vanphuc.module.modules;

import com.vanphuc.event.game.ReceiveMessageEvent;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSellMLTS extends Module {
    // --- SETTINGS ---
    public final NumberSetting cycleDelay = new NumberSetting("Delay vòng lặp (s)", 30.0, 5.0, 600.0);
    public final NumberSetting actionDelay = new NumberSetting("Delay thao tác (ms)", 1000.0, 200.0, 3000.0);

    private State state = State.IDLE;
    private long timer = 0L;
    private long nextCycleTime = 0L;

    public AutoSellMLTS() {
        super("AutoSellMLTS", "Tự động bán Minh Lam Tinh Sa.", Items.EMERALD.getDefaultStack());
        addSetting(cycleDelay);
        addSetting(actionDelay);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        state = State.PHASE_1_SEND_COMMAND;
        timer = 0;
        nextCycleTime = 0;
        info("Khởi động AutoSell!!... 🚀");
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        state = State.IDLE;
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null) return;

        long now = System.currentTimeMillis();

        // Nếu đang ở Phase 5 (Cooldown) thì kiểm tra thời gian để quay lại Phase 1
        if (state == State.PHASE_5_COOLDOWN) {
            if (now >= nextCycleTime) {
                state = State.PHASE_1_SEND_COMMAND;
                timer = 0;
            }
            return;
        }

        // Delay giữa các thao tác click/gửi lệnh
        if (now < timer) return;
        long msDelay = actionDelay.getValue().longValue();

        switch (state) {
            case PHASE_1_SEND_COMMAND -> {
                mc.player.networkHandler.sendChatCommand("shop");
                state = State.WAIT_MAIN_GUI;
                timer = now + msDelay;
            }

            case WAIT_MAIN_GUI -> {
                if (isGuiTitle("Thương Hội")) {
                    clickSlot(21, 0, SlotActionType.PICKUP); // Click trái vào Nguyên Liệu
                    state = State.WAIT_CATEGORY_GUI;
                    timer = now + msDelay;
                    info("Đã vào Thương Hội -> Chọn Nguyên Liệu 🛠️");
                }
            }

            case WAIT_CATEGORY_GUI -> {
                if (isGuiTitle("Nguyên Liệu Luyện Khí")) {
                    clickSlot(13, 1, SlotActionType.PICKUP); // CLICK CHUỘT PHẢI vào Minh Lam Tinh Sa
                    state = State.WAIT_SELL_GUI;
                    timer = now + msDelay;
                    info("Đã chọn mục NL -> Click Chuột Phải để bán MLTS 💎");
                }
            }

            case WAIT_SELL_GUI -> {
                if (isGuiTitle("Đang Bán Minh Lam Tinh Sa")) {
                    clickSlot(40, 0, SlotActionType.PICKUP); // Click "Bán tất cả"
                    state = State.WAIT_FOR_CHAT_CONFIRM;
                    timer = now + 5000; // Timeout 5s nếu lag không thấy chat
                    info("Đã click Bán tất cả... Đang chờ server xác nhận ⌛");
                }
            }

            case WAIT_FOR_CHAT_CONFIRM -> {
                if (now > timer) {
                    error("Quá thời gian chờ xác nhận! Có vẻ server lag, thử lại từ đầu... ⚠️");
                    state = State.PHASE_1_SEND_COMMAND;
                }
            }

            default -> {}
        }
    }

    // --- LẮNG NGHE CHAT EVENT MỚI ---
    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (state == State.WAIT_FOR_CHAT_CONFIRM) {
            String msg = event.getMessage().getString();

            // Kiểm tra các nội dung chat từ Thương Hội
            boolean daBanXong = msg.contains("Thương Hội > Ngài đã bán tất cả Minh Lam Tinh Sa");
            boolean hetHang = msg.contains("Thương Hội > Không có đủ 1 Minh Lam Tinh Sa để bán.");

            if (daBanXong || hetHang) {
                if (daBanXong) {
                    info("Giao dịch thành công: " + msg + " ✨");
                } else {
                    info("Thông báo: Hết MLTS để bán! Chuyển sang chế độ nghỉ ngơi. 😴");
                }

                // Đóng GUI ngay lập tức
                if (mc.currentScreen != null) {
                    mc.player.closeHandledScreen();
                }

                // Chuyển sang Phase 5: Nghỉ X giây để bắt đầu vòng lặp mới
                nextCycleTime = System.currentTimeMillis() + (cycleDelay.getValue().longValue() * 1000L);
                state = State.PHASE_5_COOLDOWN;
            }
        }
    }

    private boolean isGuiTitle(String targetTitle) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            return container.getTitle().getString().contains(targetTitle);
        }
        return false;
    }

    private void clickSlot(int slotId, int button, SlotActionType type) {
        if (mc.currentScreen instanceof GenericContainerScreen container) {
            mc.interactionManager.clickSlot(container.getScreenHandler().syncId, slotId, button, type, mc.player);
        }
    }
    public long getRemainingSeconds() {
        if (!isActive()) return -1;
        if (state != State.PHASE_5_COOLDOWN) return 0;

        long remainingMs = nextCycleTime - System.currentTimeMillis();
        return Math.max(0, remainingMs / 1000L);
    }

    private enum State {
        IDLE,
        PHASE_1_SEND_COMMAND,
        WAIT_MAIN_GUI,
        WAIT_CATEGORY_GUI,
        WAIT_SELL_GUI,
        WAIT_FOR_CHAT_CONFIRM,
        PHASE_5_COOLDOWN
    }
}