package com.vanphuc.module.modules;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.window.windows.PickUpNotepadWindow;
import com.vanphuc.gui.Rectangle;
import com.vanphuc.gui.colors.Color;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.ActionSetting;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.NumberSetting;
import com.vanphuc.module.settings.StringListSetting;
import com.vanphuc.module.settings.EnumSetting;
import com.vanphuc.module.settings.StringSetting;
import com.vanphuc.utils.BaritoneHelper;
import com.vanphuc.utils.FriendManager;
import com.vanphuc.utils.render.RenderWorldUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoPickUp extends Module {

    // 1. Khai báo Enum Mode
    public enum Mode {
        Default,
        Custom
    }

    // --- SETTINGS ---
    public final NumberSetting scanRadius = new NumberSetting("Phạm vi nhặt", 10.0, 5.0, 50.0);

    // 2. Thêm Setting chọn Chế độ và Ô nhập tọa độ (Có ẩn hiện)
    public final EnumSetting<Mode> anchorMode = new EnumSetting<>("Chế độ tâm", Mode.Default);
    public final StringSetting customAnchorPos = new StringSetting("Tọa độ tâm (x y z)", "0 80 0") {
        @Override
        public boolean isVisible() {
            return anchorMode.getValue() == Mode.Custom; // Chỉ hiện khi chọn Custom
        }
    };

    public final NumberSetting playerDetectRadius = new NumberSetting("Phạm vi radar né", 30.0, 10.0, 100.0);
    public final BooleanSetting useWhitelist = new BooleanSetting("Né người lạ (Friend)", true);
    public final BooleanSetting returnToAnchor = new BooleanSetting("Quay về tâm", true);
    public final StringListSetting priorityItems = new StringListSetting("PriorityItems", new ArrayList<>());

    public final ActionSetting openListSetting = new ActionSetting("Danh sách ưu tiên", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        if (GuiManager.getInstance().activePage != null) {
            GuiManager.getInstance().activePage.windows.removeIf(w -> w instanceof PickUpNotepadWindow);
        }
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new PickUpNotepadWindow(this, new Rectangle(x, y, 250, 150)));
    });

    public final ActionSetting clearListSetting = new ActionSetting("Xoá danh sách", () -> {
        priorityItems.getValue().clear();
        info("Đã xóa sạch danh sách! Chuyển sang chế độ nhặt mọi thứ 🗑️");
        if (GuiManager.getInstance().activePage != null) {
            for (com.vanphuc.gui.Window w : GuiManager.getInstance().activePage.windows) {
                if (w instanceof PickUpNotepadWindow pnw) pnw.refresh();
            }
        }
    });

    // --- RUNTIME VARIABLES ---
    private State state = State.IDLE;
    private int timer = 0;
    private ItemEntity currentLootEntity = null;
    private Vec3d anchorPos = null;
    private final List<String> strangerLog = new ArrayList<>(); // Log lại tên người lạ đi ngang qua

    public AutoPickUp() {
        super("AutoPickUp", "Tự động nhặt đồ bằng Baritone", Items.HOPPER.getDefaultStack());

        addSetting(scanRadius);
        // 3. Đăng ký setting mới
        addSetting(anchorMode);
        addSetting(customAnchorPos);
        addSetting(playerDetectRadius);
        addSetting(useWhitelist);
        addSetting(returnToAnchor);
        addSetting(openListSetting);
        addSetting(clearListSetting);
        addSetting(priorityItems);

        // Render Vòng Tròn (Visual)
        WorldRenderEvents.LAST.register(context -> {
            if (isActive() && anchorPos != null) {
                // Vẽ vòng tròn màu Xanh Blue Sleek Carbon (#0F4C81) tại vị trí chốt tâm
                RenderWorldUtils.drawCircle(context.matrixStack(), anchorPos, scanRadius.getValue(), new Color(0xFF0F4C81));
            }
        });

        // Chỉ giữ lại lệnh /vpItem add
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("vpItem")
                    .then(ClientCommandManager.literal("add").executes(ctx -> {
                        if (mc.player != null) {
                            ItemStack handStack = mc.player.getMainHandStack();
                            if (!handStack.isEmpty()) {
                                String itemId = Registries.ITEM.getId(handStack.getItem()).toString();
                                String itemKey = itemId + "|" + handStack.getName().getString();

                                List<String> list = priorityItems.getValue();
                                if (!list.contains(itemKey)) {
                                    list.add(itemKey);
                                    priorityItems.setValue(list);
                                    if (GuiManager.getInstance().activePage != null) {
                                        for (com.vanphuc.gui.Window w : GuiManager.getInstance().activePage.windows) {
                                            if (w instanceof PickUpNotepadWindow pnw) pnw.refresh();
                                        }
                                    }
                                }
                                // Đã sửa thành info nội bộ thay vì chat tổng
                                info("Đã thêm vào ưu tiên: " + handStack.getName().getString());
                            } else {
                                // Đã sửa thành báo lỗi nội bộ
                                error("Bạn phải cầm một item trên tay!");
                            }
                        }
                        return 1;
                    }))
            );
        });
    }

    // --- Public Accessors for GUI ---
    public List<String> getListContent() { return priorityItems.getValue(); }
    public void updateList(List<String> newContent) { priorityItems.setValue(new ArrayList<>(newContent)); }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null) return;

        // 4. Logic xử lý tâm khi kích hoạt
        if (anchorMode.getValue() == Mode.Default) {
            this.anchorPos = mc.player.getPos();
            info("Đã lấy tọa độ chân làm tâm nhặt đồ 📍");
        } else {
            try {
                String[] p = customAnchorPos.getValue().trim().split("\\s+");
                if (p.length >= 3) {
                    double x = Double.parseDouble(p[0]);
                    double y = Double.parseDouble(p[1]);
                    double z = Double.parseDouble(p[2]);
                    this.anchorPos = new Vec3d(x, y, z);
                    info(String.format("Đã chốt tâm Custom: %.1f %.1f %.1f 🎯", x, y, z));
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                error("Tọa độ sai định dạng! Lấy tọa độ chân làm dự phòng.");
                this.anchorPos = mc.player.getPos();
            }
        }

        this.state = State.SCANNING;
        strangerLog.clear();
    }

    @Override
    public void onDeactivate() {
        BaritoneHelper.stop();
        this.state = State.IDLE;
        this.anchorPos = null;
        super.onDeactivate();
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null || mc.world == null || anchorPos == null) return;

        // --- CƠ CHẾ SINH TỒN ---
        if (useWhitelist.isEnabled() && isStrangerNearby()) {
            if (state != State.WAIT_FOR_STRANGER && state != State.WAIT_AFTER_STRANGER) {
                info("Phát hiện người lạ! Dừng🛑");
                BaritoneHelper.stop(); // Ngắt Baritone ngay lập tức
                state = State.WAIT_FOR_STRANGER;
            }
            if (state == State.WAIT_AFTER_STRANGER) state = State.WAIT_FOR_STRANGER;
        }

        // --- STATE MACHINE ---
        switch (state) {
            case WAIT_FOR_STRANGER -> {
                if (!isStrangerNearby()) {
                    timer = 200; // Đứng chờ thêm 10s cho an toàn
                    state = State.WAIT_AFTER_STRANGER;
                }
            }
            case WAIT_AFTER_STRANGER -> {
                BaritoneHelper.stop();
                if (--timer <= 0) {
                    info("An toàn, tiếp tục🚜");
                    state = State.SCANNING;
                }
            }
            case SCANNING -> {
                currentLootEntity = findLootEntity(anchorPos, mc.player.getPos());
                if (currentLootEntity != null) {
                    state = State.GOTO_LOOT;
                } else if (returnToAnchor.isEnabled() && mc.player.getPos().distanceTo(anchorPos) > 1.5) {
                    // Có bật tính năng thì mới quay về tâm
                    state = State.RETURN_TO_ANCHOR;
                } else if (!returnToAnchor.isEnabled()) {
                    // Nếu tắt thì Baritone đứng im tại chỗ chờ đồ rớt tiếp
                    BaritoneHelper.stop();
                }
            }
            case GOTO_LOOT -> {
                double radiusSq = scanRadius.getValue() * scanRadius.getValue();
                if (currentLootEntity == null || !currentLootEntity.isAlive() || currentLootEntity.getPos().squaredDistanceTo(anchorPos) > radiusSq) {
                    BaritoneHelper.stop();
                    state = State.SCANNING;
                    return;
                }

                Vec3d target = currentLootEntity.getPos();
                if (mc.player.getPos().distanceTo(target) <= 1.2) {
                    BaritoneHelper.stop();
                    timer = 5; // Chờ 5 ticks để đồ bay vào mồm
                    state = State.WAIT_PICKUP;
                } else {
                    // Để Baritone tự lo việc di chuyển thay vì tính toán Yaw/Pitch thủ công
                    if (!BaritoneHelper.isPathing()) {
                        BaritoneHelper.goTo(currentLootEntity.getBlockPos());
                    }
                }
            }
            case WAIT_PICKUP -> {
                if (--timer <= 0) state = State.SCANNING;
            }
            case RETURN_TO_ANCHOR -> {
                if (mc.player.getPos().distanceTo(anchorPos) <= 1.0) {
                    BaritoneHelper.stop();
                    state = State.SCANNING;
                } else {
                    if (!BaritoneHelper.isPathing()) {
                        BaritoneHelper.goTo(BlockPos.ofFloored(anchorPos));
                    }
                }
            }
            default -> BaritoneHelper.stop();
        }
    }

    private ItemEntity findLootEntity(Vec3d center, Vec3d playerPos) {
        double radius = scanRadius.getValue();
        Box box = new Box(center.add(-radius, -4, -radius), center.add(radius, 4, radius));

        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, box, ItemEntity::isOnGround);
        return items.stream()
                .filter(e -> {
                    if (priorityItems.getValue().isEmpty()) return true;
                    String key = Registries.ITEM.getId(e.getStack().getItem()).toString() + "|" + e.getStack().getName().getString();
                    return priorityItems.getValue().contains(key);
                })
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(playerPos)))
                .orElse(null);
    }

    private boolean isStrangerNearby() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            if (mc.player.distanceTo(player) <= playerDetectRadius.getValue()) {
                String name = player.getName().getString();
                // Kéo FriendManager ra xài
                if (!FriendManager.isFriend(name)) {
                    if (!strangerLog.contains(name)) {
                        strangerLog.add(name);
                        error("Phát hiện người lạ: " + name);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private enum State { IDLE, SCANNING, GOTO_LOOT, WAIT_PICKUP, RETURN_TO_ANCHOR, WAIT_FOR_STRANGER, WAIT_AFTER_STRANGER }
}