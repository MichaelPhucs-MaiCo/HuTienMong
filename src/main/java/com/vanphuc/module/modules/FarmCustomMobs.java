package com.vanphuc.module.modules;

import com.vanphuc.gui.GuiManager;
import com.vanphuc.gui.Rectangle;

import com.vanphuc.gui.colors.Color;
import com.vanphuc.gui.window.windows.FarmMobsNotepadWindow;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class FarmCustomMobs extends Module {

    public enum Mode {
        Default,
        Custom
    }

    // --- SETTINGS ---
    public final NumberSetting scanRadius = new NumberSetting("Phạm vi tìm quái", 30.0, 5.0, 100.0);
    public final NumberSetting maintainDistance = new NumberSetting("Khoảng cách đánh", 2.0, 0.5, 6.0);
    public final NumberSetting playerDetectRadius = new NumberSetting("Phạm vi quét né", 150.0, 10.0, 300.0);
    public final BooleanSetting useWhitelist = new BooleanSetting("Né người lạ (Friend)", true);
    public final BooleanSetting enableKiting = new BooleanSetting("Đi lùi (Bunny Hop lùi)", true);
    public final BooleanSetting returnToAnchor = new BooleanSetting("Quay về tâm", false);
    public final BooleanSetting enableLooting = new BooleanSetting("Tự động nhặt đồ", true);

    public final EnumSetting<Mode> anchorMode = new EnumSetting<>("Chế độ tâm", Mode.Default);
    public final StringSetting customAnchorPos = new StringSetting("Tọa độ tâm (x y z)", "0 80 0") {
        @Override
        public boolean isVisible() {
            return anchorMode.getValue() == Mode.Custom; // Chỉ hiện khi chọn Custom
        }
    };
    public final StringListSetting targetListSetting = new StringListSetting("TargetList", new ArrayList<>());

    public final ActionSetting openListSetting = new ActionSetting("Danh sách Mục tiêu", () -> {
        GuiManager.getInstance().closeSettingsWindows();
        if (GuiManager.getInstance().activePage != null) {
            GuiManager.getInstance().activePage.windows.removeIf(w -> w instanceof FarmMobsNotepadWindow);
        }
        float x = (mc.getWindow().getScaledWidth() - 250) / 2f;
        float y = mc.getWindow().getScaledHeight() / 3f;
        GuiManager.getInstance().addWindow(new FarmMobsNotepadWindow(this, new Rectangle(x, y, 250, 150)));
    });

    public final ActionSetting clearListSetting = new ActionSetting("Xoá danh sách", () -> {
        targetListSetting.getValue().clear();
        info("Đã xóa sạch danh sách mục tiêu 🗑️");
        com.vanphuc.utils.ConfigManager.save();
        if (GuiManager.getInstance().activePage != null) {
            for (com.vanphuc.gui.Window w : GuiManager.getInstance().activePage.windows) {
                if (w instanceof FarmMobsNotepadWindow fmnw) fmnw.refresh();
            }
        }
    });

    // --- RUNTIME VARIABLES ---
    private State state = State.IDLE;
    private int timer = 0;
    private Entity currentTargetEntity = null;
    private ItemEntity currentLootEntity = null;
    private Vec3d anchorPos = null;
    private final List<String> strangerLog = new ArrayList<>();
    private final Map<UUID, StationaryInfo> stationaryMap = new HashMap<>();

    public FarmCustomMobs() {
        super("FarmCustomMobs", "Auto farm quái custom kết hợp Baritone.", Items.DIAMOND_SWORD.getDefaultStack());

        addSetting(anchorMode);
        addSetting(customAnchorPos);
        addSetting(scanRadius);
        addSetting(maintainDistance);
        addSetting(playerDetectRadius);
        addSetting(useWhitelist);
        addSetting(enableKiting);
        addSetting(returnToAnchor);
        addSetting(enableLooting);
        addSetting(openListSetting);
        addSetting(clearListSetting);
        addSetting(targetListSetting);

        WorldRenderEvents.LAST.register(context -> {
            if (isActive() && anchorPos != null) {
                RenderWorldUtils.drawCircle(context.matrixStack(), anchorPos, scanRadius.getValue(), new Color(0xFF0F4C81));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("vpMob")
                    .then(ClientCommandManager.literal("target")
                            .then(ClientCommandManager.literal("add").executes(ctx -> {
                                if (mc.player != null) {
                                    Entity entity = null;
                                    HitResult hitResult = mc.crosshairTarget;
                                    if (hitResult instanceof EntityHitResult eHit) {
                                        entity = eHit.getEntity();
                                    }
                                    if (entity == null) entity = getTargetedEntity(20.0);

                                    if (entity != null) {
                                        Box bb = entity.getBoundingBox();
                                        float w = (float)(bb.maxX - bb.minX);
                                        float h = (float)(bb.maxY - bb.minY);
                                        float d = (float)(bb.maxZ - bb.minZ);
                                        String name = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getName().getString();
                                        String clazz = entity.getClass().getSimpleName();

                                        String saveString = String.format(java.util.Locale.US, "%s|%s|%.2f|%.2f", clazz, name, w, h);
                                        List<String> list = new ArrayList<>(targetListSetting.getValue());

                                        if (!list.contains(saveString)) {
                                            list.add(saveString);
                                            targetListSetting.setValue(list);
                                            com.vanphuc.utils.ConfigManager.save();

                                            if (GuiManager.getInstance().activePage != null) {
                                                for (com.vanphuc.gui.Window win : GuiManager.getInstance().activePage.windows) {
                                                    if (win instanceof FarmMobsNotepadWindow fmnw) fmnw.refresh();
                                                }
                                            }
                                        }

                                        int priority = list.size();
                                        info("§6=== ĐÃ THÊM MỤC TIÊU MỚI ===");
                                        info("§bTên: §f" + name);
                                        info("§aClass: §f" + clazz);
                                        info(String.format(java.util.Locale.US, "§dSize: §f%.2f x %.2f x %.2f", w, h, d));
                                        info("§cMức Ưu Tiên: §fTop " + priority);
                                        info("§6=======================");
                                    } else {
                                        error("§cKhông tìm thấy mục tiêu nào trong tầm nhìn!");
                                    }
                                }
                                return 1;
                            }))
                    )
            );
        });
    }

    public List<String> getListContent() { return targetListSetting.getValue(); }
    public void updateList(List<String> newContent) { targetListSetting.setValue(new ArrayList<>(newContent)); }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null) return;

        if (targetListSetting.getValue().isEmpty()) {
            error("Danh sách mục tiêu trống! Dùng '/vpMob target add' hoặc chỉnh trong UI trước ❌");
            toggle();
            return;
        }

        if (anchorMode.getValue() == Mode.Default) {
            anchorPos = mc.player.getPos();
            info("Đã lấy tọa độ hiện tại làm tâm farm 📍");
        } else {
            try {
                String text = customAnchorPos.getValue().trim();
                String[] parts = text.split("\\s+");
                if (parts.length >= 3) {
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);
                    anchorPos = new net.minecraft.util.math.Vec3d(x, y, z);
                    info("Đã thiết lập tâm Custom: §b" + x + " " + y + " " + z);
                } else {
                    throw new Exception("Thiếu tham số tọa độ");
                }
            } catch (Exception e) {
                error("Lỗi định dạng tọa độ Custom! (Ví dụ đúng: 257 84 -46)");
                error("Tạm thời lấy tọa độ hiện tại làm tâm.");
                anchorPos = mc.player.getPos();
            }
        }

        // Reset các biến runtime khác
        this.strangerLog.clear();
        this.stationaryMap.clear();
        state = State.SCANNING; // Giữ SCANNING để vào đúng vòng lặp farm thay vì IDLE làm treo module
        timer = 0;
        currentTargetEntity = null;
        currentLootEntity = null;
    }

    @Override
    public void onDeactivate() {
        BaritoneHelper.stop();
        stopManualMovement();
        setState(State.IDLE);
        this.anchorPos = null;
        super.onDeactivate();
    }

    private void setState(State newState) {
        if (this.state == newState) return;
        this.state = newState;

        if (newState == State.SCANNING || newState == State.WAIT_AFTER_STRANGER ||
                newState == State.WAIT_FOR_STRANGER || newState == State.IDLE ||
                newState == State.WAIT_PICKUP || newState == State.RETURN_ANCHOR) {
            BaritoneHelper.stop();
            resetMovementKeys();
        }
    }

    private void resetMovementKeys() {
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
        }
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null || mc.world == null || anchorPos == null) return;

        updateStationaryStatus();

        if (useWhitelist.isEnabled() && isStrangerNearby()) {
            if (state != State.WAIT_FOR_STRANGER && state != State.WAIT_AFTER_STRANGER && state != State.IDLE) {
                info("Phát hiện người lạ! Đứng hình ngay 🛑");
                setState(State.WAIT_FOR_STRANGER);
            }
            if (state == State.WAIT_AFTER_STRANGER) setState(State.WAIT_FOR_STRANGER);
        }

        switch (state) {
            case WAIT_FOR_STRANGER -> {
                if (!isStrangerNearby()) {
                    timer = 200;
                    setState(State.WAIT_AFTER_STRANGER);
                }
            }
            case WAIT_AFTER_STRANGER -> {
                if (--timer <= 0) {
                    info("Đã an toàn. Tiếp tục farm 🗡️");
                    setState(State.SCANNING);
                }
            }
            case SCANNING -> {
                if (enableLooting.isEnabled()) {
                    currentLootEntity = findLootEntity(anchorPos, mc.player.getPos());
                    if (currentLootEntity != null) {
                        setState(State.GOTO_LOOT);
                        return;
                    }
                }

                currentTargetEntity = findEntityByHitbox(anchorPos);
                if (currentTargetEntity != null) {
                    setState(State.CHASE_TARGET);
                    return;
                }

                if (returnToAnchor.isEnabled() && mc.player.squaredDistanceTo(anchorPos) > 4.0) {
                    setState(State.RETURN_ANCHOR);
                }
            }
            case RETURN_ANCHOR -> {
                if (!returnToAnchor.isEnabled() || mc.player.squaredDistanceTo(anchorPos) <= 4.0) {
                    setState(State.SCANNING);
                    return;
                }

                boolean thayDoRong = enableLooting.isEnabled() && findLootEntity(anchorPos, mc.player.getPos()) != null;

                if (thayDoRong || findEntityByHitbox(anchorPos) != null) {
                    setState(State.SCANNING);
                    return;
                }

                if (!BaritoneHelper.isPathing()) {
                    BaritoneHelper.goTo(BlockPos.ofFloored(anchorPos.x, anchorPos.y, anchorPos.z));
                }
            }
            case CHASE_TARGET -> {
                if (currentTargetEntity == null || !currentTargetEntity.isAlive() ||
                        currentTargetEntity.squaredDistanceTo(anchorPos) > Math.pow(scanRadius.getValue(), 2)) {
                    setState(State.SCANNING);
                    return;
                }

                double dist = mc.player.distanceTo(currentTargetEntity);
                if (dist > maintainDistance.getValue() + 0.5) {
                    if (!BaritoneHelper.isPathing()) {
                        BaritoneHelper.followEntity(currentTargetEntity);
                    }
                } else {
                    setState(State.WAIT_DEATH);
                }
            }
            case WAIT_DEATH -> {
                if (currentTargetEntity == null || !currentTargetEntity.isAlive() ||
                        currentTargetEntity.squaredDistanceTo(anchorPos) > Math.pow(scanRadius.getValue(), 2)) {
                    timer = 10;
                    setState(State.SCANNING);
                    return;
                }

                double dist = mc.player.distanceTo(currentTargetEntity);
                lookAtEntity(currentTargetEntity);

                if (dist > maintainDistance.getValue() + 1.5) {
                    resetMovementKeys();
                    setState(State.CHASE_TARGET);
                }
                else if (enableKiting.isEnabled() && dist < maintainDistance.getValue() - 0.5) {
                    mc.options.backKey.setPressed(true);
                    mc.options.jumpKey.setPressed(true);
                }
                else {
                    mc.options.backKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                }
            }
            case GOTO_LOOT -> {
                if (currentLootEntity != null && currentLootEntity.isAlive()) {
                    if (currentLootEntity.squaredDistanceTo(anchorPos) > Math.pow(scanRadius.getValue(), 2)) {
                        setState(State.SCANNING);
                        return;
                    }

                    if (mc.player.distanceTo(currentLootEntity) <= 1.2) {
                        timer = 15;
                        setState(State.WAIT_PICKUP);
                    } else {
                        if (!BaritoneHelper.isPathing()) {
                            BaritoneHelper.goTo(currentLootEntity.getBlockPos());
                        }
                    }
                } else {
                    setState(State.SCANNING);
                }
            }
            case WAIT_PICKUP -> {
                if (--timer <= 0) setState(State.SCANNING);
            }
        }
    }

    private void stopManualMovement() {
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
        }
    }

    private void lookAtEntity(Entity target) {
        if (mc.player == null) return;
        Vec3d targetPos = target.getBoundingBox().getCenter();
        Vec3d eyePos = mc.player.getEyePos();

        double diffX = targetPos.x - eyePos.x;
        double diffY = targetPos.y - eyePos.y;
        double diffZ = targetPos.z - eyePos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

        mc.player.setYaw(MathHelper.wrapDegrees(yaw));
        mc.player.setPitch(MathHelper.wrapDegrees(pitch));
        mc.player.setBodyYaw(mc.player.getYaw());
        mc.player.setHeadYaw(mc.player.getYaw());
    }

    private void updateStationaryStatus() {
        if (anchorPos != null && mc.world != null) {
            double rad = scanRadius.getValue();
            Box box = new Box(anchorPos.add(-rad, -rad, -rad), anchorPos.add(rad, rad, rad));
            List<Entity> entities = mc.world.getEntitiesByClass(Entity.class, box, ex -> ex.isAlive() && !(ex instanceof ItemEntity));
            Set<UUID> currentUUIDs = new HashSet<>();

            for (Entity e : entities) {
                UUID uuid = e.getUuid();
                currentUUIDs.add(uuid);
                StationaryInfo info = stationaryMap.getOrDefault(uuid, new StationaryInfo(e.getPos()));
                if (e.getPos().squaredDistanceTo(info.lastPos) < 1.0E-4) {
                    info.ticksStill++;
                } else {
                    info.ticksStill = 0;
                    info.lastPos = e.getPos();
                }
                stationaryMap.put(uuid, info);
            }
            stationaryMap.keySet().removeIf(uuid -> !currentUUIDs.contains(uuid));
        }
    }

    private ItemEntity findLootEntity(Vec3d center, Vec3d playerPos) {
        double rad = scanRadius.getValue();
        Box box = new Box(center.add(-rad, -rad, -rad), center.add(rad, rad, rad));
        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, box, e ->
                e.isOnGround() && !e.getStack().isEmpty() && e.squaredDistanceTo(center) <= rad * rad
        );
        return items.stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(playerPos)))
                .orElse(null);
    }

    private Entity findEntityByHitbox(Vec3d center) {
        double rad = scanRadius.getValue();
        Box box = new Box(center.add(-rad, -rad, -rad), center.add(rad, rad, rad));
        Entity bestTarget = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDistanceSq = Double.MAX_VALUE;

        List<Entity> entities = mc.world.getEntitiesByClass(Entity.class, box, ex -> {
            if (!ex.isAlive() || ex == mc.player || ex instanceof ItemEntity) return false;
            if (ex.squaredDistanceTo(center) > rad * rad) return false;

            StationaryInfo info = stationaryMap.get(ex.getUuid());
            return info == null || info.ticksStill < 20;
        });

        List<TargetData> parsedTargets = parseTargetList();

        for (Entity e : entities) {
            Box entityBb = e.getBoundingBox();
            float w = (float)(entityBb.maxX - entityBb.minX);
            float h = (float)(entityBb.maxY - entityBb.minY);
            String entityName = e.hasCustomName() ? e.getCustomName().getString() : e.getName().getString();

            int currentPriority = -1;
            for (int i = 0; i < parsedTargets.size(); i++) {
                TargetData t = parsedTargets.get(i);
                boolean classMatch = e.getClass().getSimpleName().equals(t.className);
                boolean hitboxMatch = Math.abs(w - t.width) <= 0.1F && Math.abs(h - t.height) <= 0.1F;
                boolean nameMatch = t.name.equals(entityName);

                if (classMatch && hitboxMatch && nameMatch) {
                    currentPriority = i;
                    break;
                }
            }

            if (currentPriority != -1) {
                double distSq = e.squaredDistanceTo(mc.player);
                if (currentPriority < bestPriority || (currentPriority == bestPriority && distSq < bestDistanceSq)) {
                    bestPriority = currentPriority;
                    bestDistanceSq = distSq;
                    bestTarget = e;
                }
            }
        }
        return bestTarget;
    }

    private List<TargetData> parseTargetList() {
        List<TargetData> res = new ArrayList<>();
        for (String s : targetListSetting.getValue()) {
            String[] parts = s.split("\\|");
            if (parts.length >= 4) {
                try {
                    res.add(new TargetData(parts[0], parts[1], Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                } catch (Exception ignored) {}
            }
        }
        return res;
    }

    private Entity getTargetedEntity(double range) {
        Entity camera = mc.getCameraEntity();
        if (camera == null) return null;
        Vec3d eyePos = camera.getCameraPosVec(1.0F);
        Vec3d lookVec = camera.getRotationVec(1.0F);
        Vec3d endVec = eyePos.add(lookVec.multiply(range));
        Box box = camera.getBoundingBox().stretch(lookVec.multiply(range)).expand(1.0, 1.0, 1.0);
        EntityHitResult hitResult = ProjectileUtil.raycast(camera, eyePos, endVec, box, entity -> !entity.isSpectator() && entity.canHit(), range * range);
        return hitResult != null ? hitResult.getEntity() : null;
    }

    private boolean isStrangerNearby() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (mc.player.distanceTo(player) <= playerDetectRadius.getValue()) {
                String name = player.getName().getString();
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

    private enum State { IDLE, SCANNING, CHASE_TARGET, WAIT_DEATH, GOTO_LOOT, WAIT_PICKUP, WAIT_FOR_STRANGER, WAIT_AFTER_STRANGER, RETURN_ANCHOR }

    private static class StationaryInfo {
        Vec3d lastPos;
        int ticksStill;
        public StationaryInfo(Vec3d pos) { this.lastPos = pos; this.ticksStill = 0; }
    }

    private static class TargetData {
        String className;
        String name;
        float width;
        float height;
        TargetData(String c, String n, float w, float h) {
            this.className = c; this.name = n; this.width = w; this.height = h;
        }
    }
}