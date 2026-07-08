package blueduck.compound_v.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Powerplex-style power absorption.
 *
 * Taking damage charges a meter (0–100). Pressing V discharges it as an
 * electric burst that damages nearby mobs. Below 10% charge the discharge
 * fizzles. Above that, range (4–16 blocks), damage (2–20), and particle
 * count scale linearly. At high charge (>50%) the burst lingers, pulsing
 * follow-up damage ticks that decay over time.
 */
public class PowerAbsorptionEffect extends CompoundVEffect {

    public static final float MAX_CHARGE = 100.0f;
    private static final float FIZZLE_THRESHOLD = 0.10f;

    private static final float MIN_RANGE = 2.0f;
    private static final float MAX_RANGE = 8.0f;
    private static final float MIN_DAMAGE = 1.0f;
    private static final float MAX_DAMAGE = 10.0f;
    private static final int MIN_PARTICLES = 10;
    private static final int MAX_PARTICLES = 40;

    /** Server-side charge per player. */
    private static final Map<UUID, Float> chargeMap = new ConcurrentHashMap<>();

    /** Active lingering discharges. */

    public PowerAbsorptionEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // ---- Charge API (called from ForgeEvents) ----

    /**
     * Add charge from taking damage.
     * @param playerUUID the player's UUID
     * @param damageAmount raw damage taken
     */
    public static void addCharge(UUID playerUUID, float damageAmount) {
        float current = chargeMap.getOrDefault(playerUUID, 0.0f);
        // Charge gain = damage * 3 (so ~33 raw damage = full charge)
        float gain = damageAmount * 3.0f;
        chargeMap.put(playerUUID, Math.min(MAX_CHARGE, current + gain));
    }

    public static float getChargePercent(UUID playerUUID) {
        return chargeMap.getOrDefault(playerUUID, 0.0f) / MAX_CHARGE;
    }

    public static void clearCharge(UUID playerUUID) {
        chargeMap.remove(playerUUID);
        lastDischargeTick.remove(playerUUID);
    }

    // ---- Effect ticking ----

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;

        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        // Ambient sparks that intensify with charge
        float percent = getChargePercent(uuid);
        if (percent > 0.05f) {
            int count = (int) (1 + percent * 5);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    count, 0.3, 0.5, 0.3, 0.02);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 5 == 0;
    }

    // ---- Press V: display charge level ----
    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();
        int chargeDisplay = (int) (getChargePercent(uuid) * 100);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6Charge: §e" + chargeDisplay + "%"), true);
    }

    // ---- Hold V: continuous discharge ----
    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        float charge = chargeMap.getOrDefault(uuid, 0.0f);
        float percent = charge / MAX_CHARGE;
        if (percent < FIZZLE_THRESHOLD) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7Charge too low..."), true);
            return;
        }
        long now = level.getGameTime();
        int tickRate = blueduck.compound_v.Config.powerplexDischargeTickRate;
        long lastTick = lastDischargeTick.getOrDefault(uuid, 0L);
        if (now - lastTick < tickRate) return;
        lastDischargeTick.put(uuid, now);
        float drainPerTick = 1.0f;
        chargeMap.put(uuid, Math.max(0, charge - drainPerTick));
        net.minecraft.world.damagesource.DamageSource source = player.damageSources().playerAttack(player);
        int chargeDisplay = (int) (getChargePercent(uuid) * 100);

        // FOCUSED MODE: if you're LOOKING AT a single mob (no crouch needed), channel concentrated
        // energy into just that target for much higher damage - enough to melt one mob quickly -
        // instead of the area discharge. Looking at nothing falls back to the AOE burst below.
        LivingEntity focus = getLookedAtEntity(player, level,
                blueduck.compound_v.Config.powerplexFocusRange);
        if (focus != null) {
            float focusDamage = (float) blueduck.compound_v.Config.powerplexFocusDamage * percent;
            // Reset the hit-immunity window so every channel tick lands (otherwise iframes eat
            // most of the rapid ticks and the "melt one target" never happens). This is the
            // focused beam's whole point - concentrated, uninterrupted damage on one mob.
            focus.invulnerableTime = 0;
            CompoundVEffect.powerHurt(focus, source, focusDamage);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§b§lChanneling... §6" + chargeDisplay + "%"), true);
            // Concentrated beam of sparks from player to the focused target.
            Vec3 from = player.position().add(0, player.getBbHeight() * 0.6, 0);
            Vec3 to = focus.position().add(0, focus.getBbHeight() * 0.5, 0);
            Vec3 dir = to.subtract(from);
            int steps = (int) (dir.length() * 3) + 1;
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Vec3 p = from.add(dir.scale(t));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.0);
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    focus.getX(), focus.getY() + focus.getBbHeight() * 0.5, focus.getZ(),
                    6, 0.2, 0.3, 0.2, 0.1);
            level.playSound(null, focus.getX(), focus.getY(), focus.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4F, 1.8F);
            return;
        }

        double radius = blueduck.compound_v.Config.powerplexDischargeRadius;
        float damage = (float) blueduck.compound_v.Config.powerplexDischargeDamage * percent;
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c§lDischarging... §6" + chargeDisplay + "%"), true);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                (int) (8 + 12 * percent), radius * 0.3, 0.5, radius * 0.3, 0.15);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3F, 1.5F);
        AABB searchBox = player.getBoundingBox().inflate(radius);
        for (net.minecraft.world.entity.Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            if (target.distanceTo(player) > radius) continue;
            target.invulnerableTime = 0;
            CompoundVEffect.powerHurt(target, source, damage);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
        }
    }

    /** Raycast for the single living entity the player is looking at, within range. */
    private static LivingEntity getLookedAtEntity(ServerPlayer player, ServerLevel level, double range) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        LivingEntity best = null;
        double closest = range + 1;
        for (net.minecraft.world.entity.Entity e : level.getEntities(player, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            AABB eb = e.getBoundingBox().inflate(0.3);
            var hit = eb.clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceTo(hit.get());
                if (d < closest) { closest = d; best = (LivingEntity) e; }
            }
        }
        return best;
    }

    private static final Map<UUID, Long> lastDischargeTick = new ConcurrentHashMap<>();


    // ---- Burst logic ----


    @Override
    public void removeAttributeModifiers(LivingEntity entity,
                                         net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            clearCharge(player.getUUID());
            lastDischargeTick.remove(player.getUUID());
        }
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.75; // Energy absorption amplifies melee output
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.8; // Absorbs some incoming damage as energy
    }

    // ---- Internal ----

}
