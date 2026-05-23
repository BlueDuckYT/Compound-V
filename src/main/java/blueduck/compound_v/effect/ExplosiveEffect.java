package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Explosive — Creeper-style charged explosion on demand.
 *
 * Press V → 3 second charge (creeper hiss, escalating particles) → explosion.
 * User is immune to their own blast. Configurable block damage for players and mobs independently.
 * 30 second cooldown.
 */
public class ExplosiveEffect extends CompoundVEffect {

    private static final int CHARGE_DURATION = 60; // 3 seconds

    public enum ChargeState { IDLE, CHARGING }

    private static final Map<UUID, ChargeState> stateMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> chargeTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHoldTick = new ConcurrentHashMap<>();

    public ExplosiveEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // Moderate passive buffs — expendable brawler
    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.25;
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.8;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        // No-op — charge/cancel handled by holdActivate + release detection in applyEffectTick
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();
        lastHoldTick.put(uuid, now);

        // Cooldown check
        if (now < cooldownUntil.getOrDefault(uuid, 0L)) {
            int remaining = (int) ((cooldownUntil.get(uuid) - now) / 20);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7Explosive cooldown: " + remaining + "s"), true);
            return;
        }

        // Start or continue charging
        ChargeState state = stateMap.getOrDefault(uuid, ChargeState.IDLE);
        if (state != ChargeState.CHARGING) {
            stateMap.put(uuid, ChargeState.CHARGING);
            chargeTimer.put(uuid, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        int timer = chargeTimer.getOrDefault(uuid, 0) + 1;
        chargeTimer.put(uuid, timer);

        float chargePercent = (float) timer / CHARGE_DURATION;
        int secondsLeft = Math.max(0, (CHARGE_DURATION - timer) / 20);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§6§lExplosive charging: " + secondsLeft + "s"),
                true);

        // Escalating particles
        if (timer % 4 == 0) {
            int count = (int) (2 + chargePercent * 10);
            level.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    count, 0.3, 0.3, 0.3, 0.02);
        }

        if (chargePercent > 0.7 && timer % 3 == 0) {
            level.sendParticles(ParticleTypes.FLASH,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    1, 0, 0, 0, 0);
        }

        // Creeper hiss escalation
        if (timer % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS,
                    0.5F + chargePercent * 0.5F, 0.8F + chargePercent * 0.6F);
        }

        // Detonate at full charge
        if (timer >= CHARGE_DURATION) {
            detonate(player, level);
            stateMap.put(uuid, ChargeState.IDLE);
            chargeTimer.remove(uuid);
            cooldownUntil.put(uuid, level.getGameTime() + Config.explosiveCooldown);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();

        // Release detection — if V was released (holdActivate not called for 2 ticks), cancel charge
        ChargeState state = stateMap.getOrDefault(uuid, ChargeState.IDLE);
        if (state == ChargeState.CHARGING) {
            long lastHold = lastHoldTick.getOrDefault(uuid, 0L);
            long now = level.getGameTime();
            if (now - lastHold >= 2) {
                stateMap.put(uuid, ChargeState.IDLE);
                chargeTimer.remove(uuid);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§7Explosion cancelled"), true);
            }
        }
    }

    private void detonate(ServerPlayer player, ServerLevel level) {
        float radius = (float) Config.explosiveBlastRadius;

        // Make player immune to their own explosion
        int prevInvuln = player.invulnerableTime;
        player.invulnerableTime = 20; // immune for 1 second

        // Damage nearby entities (not self)
        var box = player.getBoundingBox().inflate(radius);
        for (var e : level.getEntities(player, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > radius) continue;

            float falloff = 1.0f - (float) (dist / radius);
            float damage = radius * 3.0f * falloff;
            target.hurt(player.damageSources().playerAttack(player), damage);

            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            double knockStrength = 2.0 * falloff;
            target.setDeltaMovement(knockDir.x * knockStrength, 0.5 * falloff, knockDir.z * knockStrength);
            target.hurtMarked = true;
        }

        // Visual explosion — block damage configurable
        Level.ExplosionInteraction interaction = Config.explosivePlayerBlockDamage
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(player, player.getX(), player.getY() + 0.5, player.getZ(),
                radius, interaction);

        // Extra particles
        level.sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                40, 2.0, 1.0, 2.0, 0.1);
    }

    /**
     * Detonate as a mob (used by MobPowerManager).
     */
    public static void mobDetonate(net.minecraft.world.entity.Mob mob, ServerLevel level) {
        float radius = (float) Config.explosiveBlastRadius;

        var box = mob.getBoundingBox().inflate(radius);
        for (var e : level.getEntities(mob, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != mob)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(mob);
            if (dist > radius) continue;

            float falloff = 1.0f - (float) (dist / radius);
            float damage = radius * 3.0f * falloff;
            target.hurt(mob.damageSources().mobAttack(mob), damage);

            Vec3 knockDir = target.position().subtract(mob.position()).normalize();
            double knockStrength = 2.0 * falloff;
            target.setDeltaMovement(knockDir.x * knockStrength, 0.5 * falloff, knockDir.z * knockStrength);
            target.hurtMarked = true;
        }

        Level.ExplosionInteraction interaction = Config.explosiveMobBlockDamage
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(null, mob.getX(), mob.getY() + 0.5, mob.getZ(),
                radius, interaction);

        level.sendParticles(ParticleTypes.FLAME,
                mob.getX(), mob.getY() + 1, mob.getZ(),
                40, 2.0, 1.0, 2.0, 0.1);
    }

    public static boolean isCharging(UUID uuid) {
        return stateMap.getOrDefault(uuid, ChargeState.IDLE) == ChargeState.CHARGING;
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
            chargeTimer.remove(uuid);
            cooldownUntil.remove(uuid);
            lastHoldTick.remove(uuid);
        }
    }
}
