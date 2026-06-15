package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forcefield — A Smash Bros. Ultimate-style toggleable shield.
 *
 * Players:
 * - Off (and non-rendering) until enabled. Toggle on/off at will with V.
 * - Shield health bar shrinks as it absorbs damage and gradually heals (slowly).
 * - Health is MAINTAINED across re-enabling (and heals passively even while off).
 * - While active, blocks ALL incoming damage and idly pushes mobs that come within
 *   {@link Config#forcefieldPushRadius} (~1) block.
 * - When broken, the owner takes break damage AND a damaging shockwave erupts,
 *   hurting and flinging nearby entities. The owner is then unguarded and cannot
 *   re-enable until health recovers to {@link Config#forcefieldReenablePercent}.
 *
 * Mobs:
 * - Cannot press V, so the shield is auto-maintained: it stays active whenever the
 *   mob has the power and is not suppressed, using the same HP / break / regen rules.
 *
 * Amplifier convention (players only): 0 = inactive (no render), 1 = active (render).
 */
public class ForcefieldEffect extends CompoundVEffect {

    private static final int TOGGLE_COOLDOWN = 10; // 0.5s debounce between toggles

    private static final Map<UUID, Boolean> fieldActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> shieldHealth = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> toggleCooldownUntil = new ConcurrentHashMap<>();
    // Set true when the shield breaks; cleared once health recovers to the re-enable threshold.
    private static final Map<UUID, Boolean> brokenLockout = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> currentlyToggling = ConcurrentHashMap.newKeySet();

    public ForcefieldEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // No passive combat buffs — the shield IS the defense.
    @Override
    public double getStrengthMultiplier(int amplifier) {
        return 1.0;
    }

    private static float maxHp() {
        return (float) Config.forcefieldMaxHp;
    }

    private static float currentHp(UUID uuid) {
        return shieldHealth.computeIfAbsent(uuid, u -> maxHp());
    }

    // === Player toggle ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();

        if (now < toggleCooldownUntil.getOrDefault(uuid, 0L)) return;
        toggleCooldownUntil.put(uuid, now + TOGGLE_COOLDOWN);

        boolean active = fieldActive.getOrDefault(uuid, false);
        float hp = currentHp(uuid);

        if (active) {
            // Disable — health is preserved.
            fieldActive.put(uuid, false);
            setAmplifier(player, 0);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00a77Forcefield: OFF"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6F, 1.0F);
        } else {
            // Locked out after a break until health recovers to the re-enable threshold.
            float reenableHp = maxHp() * (float) Config.forcefieldReenablePercent;
            if (brokenLockout.getOrDefault(uuid, false) || hp < reenableHp) {
                int percent = (int) ((hp / maxHp()) * 100);
                int needed = (int) (Config.forcefieldReenablePercent * 100);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "\u00a7cShield recharging: " + percent + "% (need " + needed + "%)"),
                        true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.4F, 1.5F);
                return;
            }

            fieldActive.put(uuid, true);
            setAmplifier(player, 1);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00a7b\u00a7lForcefield: ON"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    // === Player tick ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        float hp = currentHp(uuid);
        float max = maxHp();
        boolean active = fieldActive.getOrDefault(uuid, false);

        // Passive regen — always, even when off.
        if (hp < max) {
            hp = Math.min(max, hp + (float) Config.forcefieldRegenPerTick);
            shieldHealth.put(uuid, hp);
        }

        // Clear the broken lockout once health recovers to the re-enable threshold.
        if (brokenLockout.getOrDefault(uuid, false)
                && hp >= max * (float) Config.forcefieldReenablePercent) {
            brokenLockout.put(uuid, false);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00a7aForcefield ready"), true);
        }

        if (!active) return;

        // Drawback: projecting the shield slows the player down.
        if (Config.forcefieldActiveSlowness >= 0) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    10, Config.forcefieldActiveSlowness, false, false, false));
        }

        float frac = hp / max;

        // Display shield health on the action bar.
        int percent = (int) (frac * 100);
        String barColor = percent > 50 ? "\u00a7b" : percent > 25 ? "\u00a7e" : "\u00a7c";
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(barColor + "\u00a7lShield: " + percent + "%"),
                true);

        pushNearby(player, level, frac);
    }

    // === Mob tick (auto-maintained) — called from MobPowerManager.onMobTick ===

    /**
     * Auto-maintains a mob's forcefield. Mobs can't toggle, so the shield is active
     * whenever the mob isn't in a post-break lockout. Same HP / regen / break rules.
     */
    public static void tickMobForcefield(Mob mob, ServerLevel level) {
        UUID uuid = mob.getUUID();
        float hp = currentHp(uuid);
        float max = maxHp();

        // Passive regen.
        if (hp < max) {
            hp = Math.min(max, hp + (float) Config.forcefieldRegenPerTick);
            shieldHealth.put(uuid, hp);
        }

        // Recover from a break once health passes the re-enable threshold.
        if (brokenLockout.getOrDefault(uuid, false)) {
            if (hp >= max * (float) Config.forcefieldReenablePercent) {
                brokenLockout.put(uuid, false);
                fieldActive.put(uuid, true);
            } else {
                fieldActive.put(uuid, false);
                return;
            }
        } else {
            fieldActive.put(uuid, true);
        }

        pushNearby(mob, level, hp / max);

        // Subtle shield shimmer.
        if (mob.tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    1 + Math.round(2 * (hp / max)), 0.5, 0.5, 0.5, 0.0);
        }
    }

    /** Shared idle push: shoves living entities within the push radius away from the owner. */
    private static void pushNearby(LivingEntity owner, ServerLevel level, float frac) {
        double pushRadius = Config.forcefieldPushRadius;
        if (pushRadius <= 0) return;
        AABB fieldBox = owner.getBoundingBox().inflate(pushRadius);
        for (Entity e : level.getEntities(owner, fieldBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != owner)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(owner);
            if (dist > pushRadius || dist < 0.001) continue;

            Vec3 pushDir = target.position().subtract(owner.position()).normalize();
            double pushForce = 0.6 * (1.0 - dist / (pushRadius + 0.5));
            target.setDeltaMovement(target.getDeltaMovement().add(
                    pushDir.x * pushForce, pushForce * 0.1, pushDir.z * pushForce));
            target.hurtMarked = true;
        }

        // Ambient particle (denser at full health).
        if (owner.tickCount % 3 == 0) {
            int count = 1 + Math.round(3 * frac);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    owner.getX(), owner.getY() + owner.getBbHeight() * 0.5, owner.getZ(),
                    count, 0.6, 0.6, 0.6, 0.0);
        }
    }

    // === Damage absorption — called from ForgeEvents ===

    /**
     * Absorbs incoming damage for any forcefield owner (player or mob). Returns the
     * unblocked remainder (0 if fully absorbed). When the shield breaks under a hit,
     * the owner takes break damage and a damaging shockwave erupts.
     */
    public static float absorbDamage(LivingEntity owner, float damage) {
        UUID uuid = owner.getUUID();
        if (!fieldActive.getOrDefault(uuid, false)) return damage;

        // Incoming damage drains the shield faster than the raw amount.
        float drain = damage * (float) Config.forcefieldDamageMultiplier;

        float hp = currentHp(uuid);
        if (hp <= 0) {
            breakShield(owner, uuid);
            return damage;
        }

        if (drain >= hp) {
            shieldHealth.put(uuid, 0.0f);
            breakShield(owner, uuid);
            return (float) Config.forcefieldBreakDamage;
        } else {
            shieldHealth.put(uuid, hp - drain);
            return 0;
        }
    }

    private static void breakShield(LivingEntity owner, UUID uuid) {
        fieldActive.put(uuid, false);
        brokenLockout.put(uuid, true);
        if (owner instanceof Player player) {
            setAmplifier(player, 0);
            // Announce the break on the actionbar (same spot as the "Shield: N%" readout).
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00A7cForcefield broken!"), true);
        }

        if (owner.level() instanceof ServerLevel sl) {
            // Big break visual.
            sl.sendParticles(ParticleTypes.FLASH,
                    owner.getX(), owner.getY() + 1, owner.getZ(), 5, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    owner.getX(), owner.getY() + owner.getBbHeight() * 0.5, owner.getZ(), 1, 0, 0, 0, 0);
            sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                    SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.2F, 0.6F);
            sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.2F);

            // Damaging shockwave: hurt and fling nearby entities.
            double aoeRadius = Config.forcefieldBreakAoeRadius;
            float aoeDamage = (float) Config.forcefieldBreakAoeDamage;
            if (aoeRadius > 0 && aoeDamage > 0) {
                AABB box = owner.getBoundingBox().inflate(aoeRadius);
                for (Entity e : sl.getEntities(owner, box,
                        ent -> ent instanceof LivingEntity && ent.isAlive() && ent != owner)) {
                    LivingEntity victim = (LivingEntity) e;
                    double dist = victim.distanceTo(owner);
                    if (dist > aoeRadius) continue;

                    // Falloff with distance.
                    float falloff = (float) (1.0 - dist / (aoeRadius + 0.5));
                    victim.hurt(owner.damageSources().magic(), aoeDamage * falloff);

                    Vec3 dir = victim.position().subtract(owner.position()).normalize();
                    double force = 1.2 * falloff;
                    victim.setDeltaMovement(dir.x * force, 0.5 * falloff + 0.2, dir.z * force);
                    victim.hurtMarked = true;
                }
            }
        }
    }

    private static void setAmplifier(Player player, int amp) {
        UUID uuid = player.getUUID();
        currentlyToggling.add(uuid);
        player.removeEffect(blueduck.compound_v.registry.EffectReg.FORCEFIELD.get());
        MobEffectInstance inst = new MobEffectInstance(
                blueduck.compound_v.registry.EffectReg.FORCEFIELD.get(),
                MobEffectInstance.INFINITE_DURATION,
                amp, false, false, false);
        inst.setCurativeItems(new ArrayList<>());
        player.addEffect(inst);
        currentlyToggling.remove(uuid);
    }

    public static boolean isActive(UUID uuid) {
        return fieldActive.getOrDefault(uuid, false);
    }

    /** Live shield health fraction (0-1), used by the renderer for alpha/animation scaling. */
    public static float getHealthFraction(UUID uuid) {
        Float hp = shieldHealth.get(uuid);
        if (hp == null) return 1.0f; // unknown (e.g. client without server data) → render full
        return Math.max(0f, Math.min(1f, hp / maxHp()));
    }

    /** Frees per-owner shield state (called on mob death/cleanup). */
    public static void clear(UUID uuid) {
        fieldActive.remove(uuid);
        shieldHealth.remove(uuid);
        toggleCooldownUntil.remove(uuid);
        brokenLockout.remove(uuid);
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
            if (currentlyToggling.contains(uuid)) return; // just toggling the amplifier
            clear(uuid);
        }
    }
}
