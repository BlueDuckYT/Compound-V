package blueduck.compound_v.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forcefield — Defensive bubble that absorbs damage until it breaks.
 *
 * Toggle with V. When active:
 * - 3 block radius shield around the player
 * - Absorbs ALL incoming damage (player takes nothing)
 * - Shield has 100 HP, displayed on action bar
 * - Deflects projectiles outward
 * - Pushes mobs out of the radius
 * - Regenerates at 0.5% max HP per tick (~10 sec to full heal)
 * - Cannot re-enable when shield health is below 10%
 * - Disabling does NOT reset health — it heals passively even when off
 * - Auto-disables when shield HP reaches 0
 *
 * Renders as a glowing cube only when active.
 * Color is deterministic per player UUID (pink or gold).
 */
public class ForcefieldEffect extends CompoundVEffect {

    public static final double FIELD_RADIUS = 2.0;
    private static final float MAX_SHIELD_HP = 100.0f;
    private static final float REGEN_PER_TICK = MAX_SHIELD_HP * 0.005f;
    private static final float MIN_ENABLE_PERCENT = 0.10f;
    private static final double PUSH_STRENGTH = 0.8; // stronger push to keep mobs at bay
    private static final int TOGGLE_COOLDOWN = 40; // 2 seconds between toggle

    private static final DustParticleOptions PINK_PARTICLE = new DustParticleOptions(
            new Vector3f(1.0f, 0.4f, 0.7f), 1.0f);
    private static final DustParticleOptions GOLD_PARTICLE = new DustParticleOptions(
            new Vector3f(1.0f, 0.85f, 0.3f), 1.0f);

    private static final Map<UUID, Boolean> fieldActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> shieldHealth = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> toggleCooldownUntil = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> currentlyToggling = ConcurrentHashMap.newKeySet();

    public ForcefieldEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // No passive combat buffs — the shield IS the defense
    @Override
    public double getStrengthMultiplier(int amplifier) {
        return 1.0;
    }

    // === Toggle ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        boolean active = fieldActive.getOrDefault(uuid, false);
        float hp = shieldHealth.getOrDefault(uuid, MAX_SHIELD_HP);
        long now = level.getGameTime();

        // Toggle cooldown
        if (now < toggleCooldownUntil.getOrDefault(uuid, 0L)) {
            int remaining = (int) ((toggleCooldownUntil.get(uuid) - now) / 20);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§7Forcefield cooldown: " + remaining + "s"),
                    true);
            return;
        }

        if (active) {
            fieldActive.put(uuid, false);
            // Set amplifier to 0 = inactive (client-visible)
            currentlyToggling.add(uuid);
            player.removeEffect(blueduck.compound_v.registry.EffectReg.FORCEFIELD.get());
            net.minecraft.world.effect.MobEffectInstance newInst = new net.minecraft.world.effect.MobEffectInstance(
                    blueduck.compound_v.registry.EffectReg.FORCEFIELD.get(),
                    net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION,
                    0, false, false, false);
            newInst.setCurativeItems(new java.util.ArrayList<>());
            player.addEffect(newInst);
            currentlyToggling.remove(uuid);
            toggleCooldownUntil.put(uuid, now + TOGGLE_COOLDOWN);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7Forcefield: OFF"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6F, 1.0F);
        } else {
            if (hp < MAX_SHIELD_HP * MIN_ENABLE_PERCENT) {
                int percent = (int) ((hp / MAX_SHIELD_HP) * 100);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "§cForcefield too weak: " + percent + "% (need 10%)"),
                        true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.5F, 1.5F);
                return;
            }

            fieldActive.put(uuid, true);
            // Set amplifier to 1 = active (client-visible)
            currentlyToggling.add(uuid);
            player.removeEffect(blueduck.compound_v.registry.EffectReg.FORCEFIELD.get());
            net.minecraft.world.effect.MobEffectInstance newInst = new net.minecraft.world.effect.MobEffectInstance(
                    blueduck.compound_v.registry.EffectReg.FORCEFIELD.get(),
                    net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION,
                    1, false, false, false);
            newInst.setCurativeItems(new java.util.ArrayList<>());
            player.addEffect(newInst);
            currentlyToggling.remove(uuid);
            toggleCooldownUntil.put(uuid, now + TOGGLE_COOLDOWN);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b§lForcefield: ON"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);

            // Initial burst — damage scales with shield health percentage
            float hpPercent = hp / MAX_SHIELD_HP;
            float burstDamage = 8.0f * hpPercent; // max 8 at full HP, scales down

            level.sendParticles(ParticleTypes.FLASH,
                    player.getX(), player.getY() + 1, player.getZ(),
                    3, 0, 0, 0, 0);
            level.explode(player, player.getX(), player.getY() + 0.5, player.getZ(),
                    0.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);

            AABB burstBox = player.getBoundingBox().inflate(FIELD_RADIUS);
            for (Entity e : level.getEntities(player, burstBox,
                    ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
                LivingEntity target = (LivingEntity) e;
                double dist = target.distanceTo(player);
                if (dist > FIELD_RADIUS) continue;

                Vec3 pushDir = target.position().subtract(player.position()).normalize();
                double pushForce = 2.0 * (1.0 - dist / FIELD_RADIUS);
                target.setDeltaMovement(pushDir.x * pushForce, 0.5, pushDir.z * pushForce);
                target.hurtMarked = true;
                if (burstDamage > 0.5f) {
                    target.hurt(player.damageSources().magic(), burstDamage);
                }
            }
        }
    }

    // === Tick ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        float hp = shieldHealth.getOrDefault(uuid, MAX_SHIELD_HP);
        boolean active = fieldActive.getOrDefault(uuid, false);

        // Passive regen — always, even when off
        if (hp < MAX_SHIELD_HP) {
            hp = Math.min(MAX_SHIELD_HP, hp + REGEN_PER_TICK);
            shieldHealth.put(uuid, hp);
        }

        if (!active) return;

        // Display shield health
        int percent = (int) ((hp / MAX_SHIELD_HP) * 100);
        String barColor = percent > 50 ? "§b" : percent > 20 ? "§e" : "§c";
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        barColor + "§lShield: " + percent + "%"),
                true);

        int color = getColor(player);
        DustParticleOptions particle = color == 0 ? PINK_PARTICLE : GOLD_PARTICLE;

        // Push mobs out
        AABB fieldBox = player.getBoundingBox().inflate(FIELD_RADIUS);
        for (Entity e : level.getEntities(player, fieldBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > FIELD_RADIUS || dist < 0.5) continue;

            Vec3 pushDir = target.position().subtract(player.position()).normalize();
            double pushForce = PUSH_STRENGTH * (1.0 - dist / FIELD_RADIUS);
            target.setDeltaMovement(target.getDeltaMovement().add(
                    pushDir.x * pushForce, pushForce * 0.15, pushDir.z * pushForce));
            target.hurtMarked = true;
        }

        // Deflect projectiles
        for (Entity e : level.getEntities(player, fieldBox,
                ent -> ent instanceof Projectile && ent.isAlive())) {
            Projectile proj = (Projectile) e;
            double dist = proj.distanceTo(player);
            if (dist > FIELD_RADIUS) continue;

            Vec3 reflectDir = proj.position().subtract(player.position()).normalize();
            Vec3 vel = proj.getDeltaMovement();
            double speed = vel.length();
            proj.setDeltaMovement(reflectDir.x * speed, reflectDir.y * speed * 0.5, reflectDir.z * speed);

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    proj.getX(), proj.getY(), proj.getZ(),
                    3, 0.1, 0.1, 0.1, 0.05);
        }

        // Face particles
        if (player.tickCount % 2 == 0) {
            double r = FIELD_RADIUS;
            double cx = player.getX(), cy = player.getY() + player.getBbHeight() * 0.5, cz = player.getZ();
            for (int i = 0; i < 6; i++) {
                double px, py, pz;
                double u = (player.getRandom().nextDouble() - 0.5) * 2 * r;
                double v = (player.getRandom().nextDouble() - 0.5) * 2 * r;
                switch (i) {
                    case 0 -> { px = cx + r; py = cy + u; pz = cz + v; }
                    case 1 -> { px = cx - r; py = cy + u; pz = cz + v; }
                    case 2 -> { px = cx + u; py = cy + r; pz = cz + v; }
                    case 3 -> { px = cx + u; py = cy - r; pz = cz + v; }
                    case 4 -> { px = cx + u; py = cy + v; pz = cz + r; }
                    default -> { px = cx + u; py = cy + v; pz = cz - r; }
                }
                level.sendParticles(particle, px, py, pz, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Edge particles
        if (player.tickCount % 4 == 0) {
            double r = FIELD_RADIUS;
            double cx = player.getX(), cy = player.getY() + player.getBbHeight() * 0.5, cz = player.getZ();
            double t = (player.getRandom().nextDouble() - 0.5) * 2 * r;
            for (int i = 0; i < 2; i++) {
                int edge = player.getRandom().nextInt(12);
                double px, py, pz;
                switch (edge) {
                    case 0 ->  { px = cx + t; py = cy + r; pz = cz + r; }
                    case 1 ->  { px = cx + t; py = cy + r; pz = cz - r; }
                    case 2 ->  { px = cx + t; py = cy - r; pz = cz + r; }
                    case 3 ->  { px = cx + t; py = cy - r; pz = cz - r; }
                    case 4 ->  { px = cx + r; py = cy + t; pz = cz + r; }
                    case 5 ->  { px = cx + r; py = cy + t; pz = cz - r; }
                    case 6 ->  { px = cx - r; py = cy + t; pz = cz + r; }
                    case 7 ->  { px = cx - r; py = cy + t; pz = cz - r; }
                    case 8 ->  { px = cx + r; py = cy + r; pz = cz + t; }
                    case 9 ->  { px = cx + r; py = cy - r; pz = cz + t; }
                    case 10 -> { px = cx - r; py = cy + r; pz = cz + t; }
                    default -> { px = cx - r; py = cy - r; pz = cz + t; }
                }
                level.sendParticles(particle, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }

        if (player.tickCount % 40 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3F, 1.5F);
        }
    }

    // === Damage absorption — called from ForgeEvents ===

    public static float absorbDamage(Player player, float damage) {
        UUID uuid = player.getUUID();
        if (!fieldActive.getOrDefault(uuid, false)) return damage;

        float hp = shieldHealth.getOrDefault(uuid, MAX_SHIELD_HP);
        if (hp <= 0) {
            fieldActive.put(uuid, false);
            setAmplifier(player, 0);
            return damage;
        }

        if (damage >= hp) {
            float excess = damage - hp;
            shieldHealth.put(uuid, 0.0f);
            fieldActive.put(uuid, false);
            setAmplifier(player, 0);
            return excess;
        } else {
            shieldHealth.put(uuid, hp - damage);
            return 0;
        }
    }

    private static void setAmplifier(Player player, int amp) {
        UUID uuid = player.getUUID();
        currentlyToggling.add(uuid);
        player.removeEffect(blueduck.compound_v.registry.EffectReg.FORCEFIELD.get());
        net.minecraft.world.effect.MobEffectInstance inst = new net.minecraft.world.effect.MobEffectInstance(
                blueduck.compound_v.registry.EffectReg.FORCEFIELD.get(),
                net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION,
                amp, false, false, false);
        inst.setCurativeItems(new java.util.ArrayList<>());
        player.addEffect(inst);
        currentlyToggling.remove(uuid);
    }

    public static boolean isActive(UUID uuid) {
        return fieldActive.getOrDefault(uuid, false);
    }

    public static int getColor(Player player) {
        return Math.abs(player.getUUID().hashCode()) % 2;
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
            // Skip cleanup if we're just toggling the amplifier
            if (currentlyToggling.contains(uuid)) return;
            fieldActive.remove(uuid);
            shieldHealth.remove(uuid);
            toggleCooldownUntil.remove(uuid);
        }
    }
}
