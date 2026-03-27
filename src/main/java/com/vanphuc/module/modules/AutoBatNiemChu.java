package com.vanphuc.module.modules;

import com.vanphuc.module.Module;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.mixin.InGameHudAccessor;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoBatNiemChu extends Module {

    public final NumberSetting weaponSlot = new NumberSetting("Slot vũ khí (1-9)", 2.0, 1.0, 9.0);
    public final NumberSetting checkDelay = new NumberSetting("Delay kiểm tra (ms)", 10000.0, 100.0, 10000.0);

    private long lastCheckTime = 0L;
    private long stepTimer = 0L;
    private boolean isWaitingToSwap = false;

    public AutoBatNiemChu() {
        super("AutoBatNiemChu", "Tự động ghim slot và bật Niệm Chú (F) 🔮", Items.BLAZE_POWDER.getDefaultStack());
        addSetting(weaponSlot);
        addSetting(checkDelay);
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null || mc.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();

        // 1. Kiểm tra Delay 10s (Chỉ khi không đang trong quy trình bấm F)
        if (!isWaitingToSwap && (now - lastCheckTime < checkDelay.getValue())) return;

        // 2. Lấy dữ liệu ActionBar và thời gian tồn tại
        String actionBarText = "";
        boolean isMessageVisible = false;

        if (mc.inGameHud != null) {
            InGameHudAccessor accessor = (InGameHudAccessor) mc.inGameHud;
            Text overlay = accessor.getOverlayMessage();
            // Chỉ lấy text nếu tin nhắn còn thời gian hiển thị (> 0 tick)
            if (overlay != null && accessor.getOverlayRemaining() > 0) {
                actionBarText = overlay.getString();
                isMessageVisible = true;
            }
        }

        // 3. Xử lý logic theo ActionBar

        // 3.1. Nếu THỰC SỰ đang hiện chữ Cooldown thì mới bỏ qua
        if (isMessageVisible && (actionBarText.contains("Ngươi cần đợi") || actionBarText.contains("tiếp tục sử dụng"))) {
            // Lưu ý: Nếu server báo "0 giây" thì có thể là do lag, vẫn nên đợi lượt sau
            info("§cServer đang cooldown thực tế! Đợi thêm lượt sau. ⏳");
            lastCheckTime = now;
            isWaitingToSwap = false;
            return;
        }

        // 3.2. Nếu đã đang Niệm Chú (Có dấu [ ])
        boolean isCasting = isMessageVisible && actionBarText.contains("[") && actionBarText.contains("]");
        if (isCasting) {
            lastCheckTime = now;
            isWaitingToSwap = false;
            return;
        }

        // 4. QUY TRÌNH KÍCH HOẠT (Nếu không vướng 2 điều kiện trên)
        int targetSlot = weaponSlot.getValue().intValue() - 1;

        if (!isWaitingToSwap) {
            // Giai đoạn 1: Chuyển slot (Điều kiện cần)
            mc.player.getInventory().selectedSlot = targetSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(targetSlot));

            info("§b[1/2] Đã ghim slot vũ khí " + (targetSlot + 1));
            stepTimer = now + 300; // Đợi 300ms cho chắc bài
            isWaitingToSwap = true;
        } else {
            // Giai đoạn 2: Bấm F
            if (now >= stepTimer) {
                sendSwapPacket();
                info("§e[2/2] Kích hoạt Niệm Chú! ✨");
                lastCheckTime = now;
                isWaitingToSwap = false;
            }
        }
    }

    private void sendSwapPacket() {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ORIGIN,
                    Direction.DOWN
            ));
        }
    }
}