package blueduck.compound_v.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Leap — Queen Maeve's signature power.
 *
 * State machine: IDLE → CHARGING (hold V on ground) → AIRBORNE (released V) → IDLE (landed)
 *
 * Passive: 1.5x strength, 0.75x damage reduction, fall damage immunity.
 * Active: Hold V to charge, release to launch in look direction.
 * Landing with speed creates a ground slam AoE.
 */
public class LeapEffect extends CompoundVEffect {

    private static final int MAX_CHARGE_TICKS = 25; // ~1.25 seconds
    private static final int MIN_CHARGE_TICKS = 3;  // minimum hold before launch
    private static final float SLAM_RADIUS = 4.0f;
    private static final float SLAM_MAX_DAMAGE = 8.0f;

    private enum State { IDLE, CHARGING, AIRBORNE }

    private static final Map<UUID, State> stateMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> chargeTicks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHoldTick = new ConcurrentHashMap<>();

    public LeapEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.5;
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.75;
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        State state = stateMap.getOrDefault(uuid, State.IDLE);
        lastHoldTick.put(uuid, level.getGameTime());

        // Can only charge while IDLE and on the ground
        if (state == State.AIRBORNE) return;
        if (state == State.IDLE && !player.onGround()) return;

        // Start or continue charging
        stateMap.put(uuid, State.CHARGING);
        int charge = Math.min(chargeTicks.getOrDefault(uuid, 0) + 1, MAX_CHARGE_TICKS);
        chargeTicks.put(uuid, charge);

        float chargePercent = (float) charge / MAX_CHARGE_TICKS;

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§6§lLeap charge: " + (int) (chargePercent * 100) + "%"),
                true);

        if (charge % 5 == 0) {
            level.sendParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY(), player.getZ(),
                    (int) (3 + chargePercent * 8), 0.5, 0.1, 0.5, 0.05);
        }

        if (charge % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS,
                    0.2F + chargePercent * 0.3F, 1.5F + chargePercent * 0.5F);
        }
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        // No-op — charging and launching handled by holdActivate + tick
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        State state = stateMap.getOrDefault(uuid, State.IDLE);

        // === CHARGING: detect V release and launch ===
        if (state == State.CHARGING) {
            long lastHold = lastHoldTick.getOrDefault(uuid, 0L);
            long now = level.getGameTime();

            // V released — 3 tick gap to account for network jitter
            if (now - lastHold >= 3) {
                int charge = chargeTicks.getOrDefault(uuid, 0);
                if (charge >= MIN_CHARGE_TICKS) {
                    doLaunch(player, level, charge);
                } else {
                    // Too short — cancel
                    stateMap.put(uuid, State.IDLE);
                    chargeTicks.remove(uuid);
                }
            }
        }

        // === AIRBORNE: detect landing for ground slam ===
        if (state == State.AIRBORNE && player.onGround()) {
            float fallDist = player.fallDistance;
            if (fallDist > 3.0f) {
                doGroundSlam(player, level, fallDist);
            }
            stateMap.put(uuid, State.IDLE);
            chargeTicks.remove(uuid);
            lastHoldTick.remove(uuid);
        }
    }

    private void doLaunch(ServerPlayer player, ServerLevel level, int charge) {
        UUID uuid = player.getUUID();
        float chargePercent = (float) charge / MAX_CHARGE_TICKS;

        Vec3 look = player.getLookAngle();
        double horizontalPower = 1.0 + chargePercent * 3.0;
        double verticalPower = 0.5 + chargePercent * 1.5;

        player.setDeltaMovement(
                look.x * horizontalPower,
                verticalPower,
                look.z * horizontalPower);
        player.hurtMarked = true;

        stateMap.put(uuid, State.AIRBORNE);

        // Shockwave ring
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX() + Math.cos(angle) * 1.5, player.getY(),
                    player.getZ() + Math.sin(angle) * 1.5,
                    1, 0.1, 0.05, 0.1, 0.02);
        }
        level.sendParticles(ParticleTypes.EXPLOSION,
                player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.7F, 1.2F);
    }

    private void doGroundSlam(ServerPlayer player, ServerLevel level, float fallDist) {
        float damage = Math.min(SLAM_MAX_DAMAGE, fallDist * 1.5f);

        AABB box = player.getBoundingBox().inflate(SLAM_RADIUS);
        for (var e : level.getEntities(player, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > SLAM_RADIUS) continue;

            float falloff = 1.0f - (float) (dist / SLAM_RADIUS) * 0.5f;
            target.hurt(player.damageSources().playerAttack(player), damage * falloff);

            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 1.2, 0.4, knockDir.z * 1.2);
            target.hurtMarked = true;
        }

        level.sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY(), player.getZ(), 20, 1.5, 0.3, 1.5, 0.05);
        level.sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY(), player.getZ(), 30, 2.0, 0.1, 2.0, 0.1);
        level.explode(player, player.getX(), player.getY(), player.getZ(),
                0.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            UUID uuid = player.getUUID();
            stateMap.remove(uuid);
            chargeTicks.remove(uuid);
            lastHoldTick.remove(uuid);
        }
    }
}
