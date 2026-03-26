package com.vanphuc.module.modules;

import com.vanphuc.event.events.packets.PacketEvent;
import com.vanphuc.mixin.IClientPlayerEntity;
import com.vanphuc.mixin.IPlayerMoveC2SPacket;
import com.vanphuc.module.Module;
import com.vanphuc.module.settings.BooleanSetting;
import com.vanphuc.module.settings.EnumSetting;
import com.vanphuc.module.settings.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.util.math.Vec3d;

public class AnhVuPhiHanh extends Module {
    // --- ENUMS ---
    public enum FlightMode { Abilities, Velocity, Vanilla }
    public enum AntiKickMode { Packet, Normal, None }

    // --- SETTINGS ---
    private final EnumSetting<FlightMode> mode = new EnumSetting<>("Mode", FlightMode.Abilities);
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.0, 10.0);
    private final NumberSetting abilitiesSpeed = new NumberSetting("Abilities Speed", 0.150, 0.0, 0.2).setPrecision(3);
    private final BooleanSetting verticalSpeedMatch = new BooleanSetting("Vertical Speed Match", false);
    private final BooleanSetting noSneak = new BooleanSetting("No Sneak", false);
    private final EnumSetting<AntiKickMode> antiKick = new EnumSetting<>("Anti Kick", AntiKickMode.Packet);
    private final NumberSetting delay = new NumberSetting("Delay", 20.0, 1.0, 200.0);
    private final NumberSetting offTime = new NumberSetting("Off Time", 1.0, 1.0, 20.0);

    // Setting thời gian tự động ngắt bay sau khi bắt được nhịp lướt
    private final NumberSetting flightDuration = new NumberSetting("Thời gian bay (ms)", 1500.0, 100.0, 5000.0);

    // --- LOGIC VARIABLES ---
    private int delayLeft;
    private int offLeft;
    private double lastPacketY = Double.MAX_VALUE;

    private int lastSlowFallingDuration = 0;
    private boolean isFlying = false;
    private long flightEndTime = 0;

    public AnhVuPhiHanh() {
        super("AnhVuPhiHanh", "Tự động kích hoạt Flight khi dùng skill Ảnh Vũ Phi Hành lần 2.", Items.FEATHER.getDefaultStack());
        addSetting(mode);
        addSetting(speed);
        addSetting(abilitiesSpeed);
        addSetting(verticalSpeedMatch);
        addSetting(noSneak);
        addSetting(antiKick);
        addSetting(delay);
        addSetting(offTime);
        addSetting(flightDuration);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        delayLeft = ((Number) delay.getValue()).intValue();
        offLeft = ((Number) offTime.getValue()).intValue();
        lastSlowFallingDuration = 0;
        isFlying = false;
        flightEndTime = 0;
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null && isFlying) {
            stopFlying();
        }
        isFlying = false;
        super.onDeactivate();
    }

    private void stopFlying() {
        applyAbilities(false);
        mc.player.setVelocity(0, 0, 0); // Triệt tiêu quán tính
    }

    @Override
    public void onUpdate() {
        if (!isActive() || mc.player == null) return;

        // Kiểm tra hiệu ứng Slow Falling
        StatusEffectInstance slowFalling = mc.player.getStatusEffect(StatusEffects.SLOW_FALLING);
        int currentDuration = slowFalling != null ? slowFalling.getDuration() : 0;

        // Phát hiện reset thời gian (Kích hoạt lướt lần 2)
        // Nếu trước đó đang có hiệu ứng (>0) và bỗng nhiên tăng vọt (>= 10 ticks)
        if (lastSlowFallingDuration > 0 && currentDuration > lastSlowFallingDuration + 10) {
            isFlying = true;
            flightEndTime = System.currentTimeMillis() + flightDuration.getValue().longValue();
            info("Phát hiện lướt! Kích hoạt Flight 🚀");
            if (mode.getValue() == FlightMode.Abilities && !mc.player.isSpectator()) {
                applyAbilities(true);
            }
        }
        lastSlowFallingDuration = currentDuration;

        // Logic quản lý khi đang bay
        if (isFlying) {
            if (System.currentTimeMillis() > flightEndTime || currentDuration == 0) {
                isFlying = false;
                stopFlying();
                info("Hết thời gian bay🪂");
                return;
            }

            // --- ANTI KICK LOGIC ---
            if (delayLeft > 0) delayLeft--;

            if (offLeft <= 0 && delayLeft <= 0) {
                delayLeft = ((Number) delay.getValue()).intValue();
                offLeft = ((Number) offTime.getValue()).intValue();

                if (antiKick.getValue() == AntiKickMode.Packet) {
                    ((IClientPlayerEntity) mc.player).setTicksSinceLastPositionPacketSent(20);
                }
            } else if (delayLeft <= 0) {
                if (antiKick.getValue() == AntiKickMode.Normal) {
                    if (mode.getValue() == FlightMode.Abilities) {
                        applyAbilities(false); // Tắt tạm thời để bypass
                    }
                } else if (antiKick.getValue() == AntiKickMode.Packet && offLeft == ((Number) offTime.getValue()).intValue()) {
                    ((IClientPlayerEntity) mc.player).setTicksSinceLastPositionPacketSent(20);
                }
                offLeft--;
            }

            // --- MOVEMENT LOGIC ---
            switch (mode.getValue()) {
                case Velocity -> {
                    mc.player.getAbilities().flying = false;
                    mc.player.setVelocity(0, 0, 0);
                    Vec3d velocity = mc.player.getVelocity();
                    double s = ((Number) speed.getValue()).doubleValue();

                    if (mc.options.jumpKey.isPressed())
                        velocity = velocity.add(0, s * (verticalSpeedMatch.isEnabled() ? 1.0 : 0.5), 0);
                    if (mc.options.sneakKey.isPressed())
                        velocity = velocity.subtract(0, s * (verticalSpeedMatch.isEnabled() ? 1.0 : 0.5), 0);

                    mc.player.setVelocity(velocity);
                    if (noSneak.isEnabled()) mc.player.setOnGround(false);
                }
                case Vanilla -> {
                    mc.player.getAbilities().setFlySpeed(0.05f);
                    mc.player.getAbilities().flying = true;
                    if (!mc.player.getAbilities().creativeMode) mc.player.getAbilities().allowFlying = true;
                }
                case Abilities -> {
                    if (mc.player.isSpectator()) return;
                    applyAbilities(true);
                }
            }
        }
    }

    private void applyAbilities(boolean state) {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = state;
        mc.player.getAbilities().setFlySpeed(state ? ((Number) abilitiesSpeed.getValue()).floatValue() : 0.05f);
        if (mc.player.getAbilities().creativeMode) return;
        mc.player.getAbilities().allowFlying = state;
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        if (!isFlying || mc.player == null || !(event.packet instanceof PlayerMoveC2SPacket packet) || antiKick.getValue() != AntiKickMode.Packet) return;

        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            antiKickPacket(packet, currentY);
        } else {
            PlayerMoveC2SPacket fullPacket;
            if (packet.changesLook()) {
                fullPacket = new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), packet.getYaw(0), packet.getPitch(0), packet.isOnGround(), mc.player.horizontalCollision);
            } else {
                fullPacket = new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), packet.isOnGround(), mc.player.horizontalCollision);
            }
            event.setCancelled(true);
            antiKickPacket(fullPacket, mc.player.getY());
            mc.getNetworkHandler().sendPacket(fullPacket);
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!isFlying || mc.player == null || !(event.packet instanceof PlayerAbilitiesS2CPacket packet) || mode.getValue() != FlightMode.Abilities) return;

        event.setCancelled(true);
        mc.player.getAbilities().invulnerable = packet.isInvulnerable();
        mc.player.getAbilities().creativeMode = packet.isCreativeMode();
        mc.player.getAbilities().setWalkSpeed(packet.getWalkSpeed());
    }

    private void antiKickPacket(PlayerMoveC2SPacket packet, double currentY) {
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE &&
                (currentY >= lastPacketY || lastPacketY - currentY < 0.03130D) && isEntityOnAir(mc.player)) {
            ((IPlayerMoveC2SPacket) packet).setY(lastPacketY - 0.03130D);
        } else {
            lastPacketY = currentY;
        }
    }

    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }
}