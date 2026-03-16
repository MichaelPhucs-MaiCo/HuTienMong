package com.vanphuc.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import java.util.function.Predicate;

public class BaritoneHelper {

    public static IBaritone getBaritone() {
        return BaritoneAPI.getProvider().getPrimaryBaritone();
    }

    public static void goTo(int x, int y, int z) {
        IBaritone baritone = getBaritone();
        if (baritone != null) {
            // Kiểm tra: Nếu KHÔNG PHẢI đang tìm đường thì mới ra lệnh
            if (!baritone.getPathingBehavior().isPathing()) {
                baritone.getCustomGoalProcess().setGoalAndPath(new baritone.api.pathing.goals.GoalBlock(x, y, z));
            }
        }
    }

    public static void goTo(BlockPos pos) {
        goTo(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Fix lỗi Cannot access class_1297:
     * Ép kiểu Predicate về raw để bỏ qua kiểm tra kiểu dữ liệu lúc compile
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void followEntity(Entity entity) {
        Predicate rawPredicate = e -> {
            // Ép kiểu Object e sang Entity (Yarn)
            Entity target = (Entity) e;
            return target.getUuid().equals(entity.getUuid());
        };
        getBaritone().getFollowProcess().follow(rawPredicate);
    }

    public static void stop() {
        getBaritone().getPathingBehavior().cancelEverything();
    }

    public static boolean isPathing() {
        return getBaritone().getPathingBehavior().isPathing();
    }
}