package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aimlock — Hold V to lock onto the mob/entity you are looking at (long range).
 * Holding V while looking at nothing clears the lock. While a target is locked,
 * every projectile the player fires homes onto the locked target.
 *
 * The lock is per-player. Homing projectiles are tracked globally and steered each
 * server tick by {@link #tickHomingProjectiles(ServerLevel)} (called from ForgeEvents).
 */
public class AimlockEffect extends CompoundVEffect {

    // player UUID -> locked target UUID
    private static final Map<UUID, UUID> lockedTargets = new ConcurrentHashMap<>();
    // debounce so holding V doesn't re-acquire/clear every tick
    private static final Map<UUID, Long> nextToggleAllowed = new ConcurrentHashMap<>();
    // projectile UUID -> target UUID (homing registry)
    private static final Map<UUID, UUID> homingProjectiles = new ConcurrentHashMap<>();
    // projectiles that have been blocked/deflected/stuck — never home these again
    private static final java.util.Set<UUID> homingBlocklist = ConcurrentHashMap.newKeySet();

    private static final int TOGGLE_COOLDOWN = 8; // ticks between lock changes while holding

    public AimlockEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // No passive combat buffs.

    /** Hold-V: acquire the entity under the crosshair, or clear if looking at nothing. */
    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;

        UUID uuid = player.getUUID();
        long now = level.getGameTime();
        if (now < nextToggleAllowed.getOrDefault(uuid, 0L)) return;

        double range = Config.aimlockRange;
        LivingEntity target = raycastEntity(player, level, range);

        if (target != null) {
            UUID prev = lockedTargets.get(uuid);
            if (prev != null && prev.equals(target.getUUID())) {
                return; // already locked on this target, nothing to do
            }
            lockedTargets.put(uuid, target.getUUID());
            nextToggleAllowed.put(uuid, now + TOGGLE_COOLDOWN);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a7c\u00a7l\u2316 Locked: \u00a7f" + target.getDisplayName().getString()), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.6F, 1.6F);
            // Mark target with a brief flash
            level.sendParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(),
                    8, 0.2, 0.2, 0.2, 0.02);
        } else {
            // Looking at nothing clears the lock.
            if (lockedTargets.remove(uuid) != null) {
                nextToggleAllowed.put(uuid, now + TOGGLE_COOLDOWN);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "\u00a77\u2316 Target cleared"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.4F, 0.8F);
            }
        }
    }

    /** Per-tick upkeep: show a marker on the locked target and drop stale locks. */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        UUID targetId = lockedTargets.get(uuid);
        if (targetId == null) return;

        Entity target = level.getEntity(targetId);
        if (!(target instanceof LivingEntity living) || !living.isAlive()
                || living.distanceTo(player) > Config.aimlockRange * 1.5) {
            lockedTargets.remove(uuid); // target gone / dead / far out of range
            return;
        }

        // Reticle particle on the locked target.
        if (player.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.CRIT,
                    living.getX(), living.getY() + living.getBbHeight() + 0.3, living.getZ(),
                    2, 0.15, 0.05, 0.15, 0.0);
        }
    }

    /** Long-range raycast for a living entity under the crosshair. */
    private static LivingEntity raycastEntity(ServerPlayer player, ServerLevel level, double range) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(range));

        AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(range)).inflate(2.0);
        LivingEntity best = null;
        double closest = range + 1;

        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            // Slightly inflated hitbox so far/small targets are easier to catch.
            AABB box = e.getBoundingBox().inflate(0.35);
            var hit = box.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closest) {
                    closest = dist;
                    best = (LivingEntity) e;
                }
            }
        }
        return best;
    }

    /** Returns the player's current locked target entity id, or null. */
    public static UUID getLockedTarget(UUID playerId) {
        return lockedTargets.get(playerId);
    }

    /** Registers a freshly-fired projectile to home on the shooter's locked target. */
    public static void registerProjectile(Projectile projectile, UUID targetId) {
        if (homingBlocklist.contains(projectile.getUUID())) return;
        homingProjectiles.put(projectile.getUUID(), targetId);
    }

    /**
     * Permanently stops a projectile from homing (e.g. it was deflected by Projectile
     * Immunity, blocked, or stuck in a block). It is removed from the homing registry
     * and blocklisted so the per-tick scan never re-binds it — from now on it flies
     * like a normal projectile.
     */
    public static void stopHoming(UUID projectileId) {
        homingProjectiles.remove(projectileId);
        homingBlocklist.add(projectileId);
        // Bound the blocklist so it can't grow without limit over a long session.
        if (homingBlocklist.size() > 4096) homingBlocklist.clear();
    }

    /**
     * Steers homing projectiles toward their targets. Called once per server tick.
     *
     * Rather than relying on EntityJoinLevelEvent (where Projectile.getOwner() is
     * frequently null because the owner UUID hasn't resolved yet), we scan all
     * projectiles each tick and match their owner against the set of players who
     * currently have an Aimlock lock. This is robust to owner-resolution timing and
     * lets the target update live. A projectile is only ever bound to the target
     * that was locked at the moment it was first seen, so re-locking mid-flight
     * doesn't yank existing shots.
     */
    public static void tickHomingProjectiles(ServerLevel level) {
        double turn = Config.aimlockHomingStrength;
        if (turn <= 0) return;

        // Bind newly-seen projectiles whose owner is a locked Aimlock player.
        // Scan a generous box around each locked shooter (cheaper and uses a
        // definitely-available API rather than iterating every entity in the level).
        if (!lockedTargets.isEmpty()) {
            for (Map.Entry<UUID, UUID> lock : lockedTargets.entrySet()) {
                Entity shooterEnt = level.getEntity(lock.getKey());
                if (!(shooterEnt instanceof Player shooter)) continue;
                if (CompoundVEffect.arePowersSuppressed(shooter)) continue;
                if (!shooter.hasEffect(blueduck.compound_v.registry.EffectReg.AIMLOCK.get())) continue;
                UUID targetId = lock.getValue();
                if (targetId == null || targetId.equals(shooter.getUUID())) continue;

                AABB scanBox = shooter.getBoundingBox().inflate(24.0);
                for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, scanBox,
                        p -> p.isAlive() && shooter.equals(p.getOwner())
                                && !homingBlocklist.contains(p.getUUID()))) {
                    homingProjectiles.putIfAbsent(projectile.getUUID(), targetId);
                }
            }
        }

        if (homingProjectiles.isEmpty()) return;

        homingProjectiles.entrySet().removeIf(entry -> {
            Entity pe = level.getEntity(entry.getKey());
            if (!(pe instanceof Projectile projectile) || !projectile.isAlive()) {
                return true; // projectile gone
            }
            Entity te = level.getEntity(entry.getValue());
            if (!(te instanceof LivingEntity target) || !target.isAlive()) {
                return true; // target gone
            }

            Vec3 motion = projectile.getDeltaMovement();
            double speed = motion.length();
            // Near-zero motion means the projectile is stuck/blocked (e.g. arrow lodged
            // in a block). Stop homing it and let it behave normally from here on.
            if (speed < 1.0E-3) {
                if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                    arrow.setNoGravity(false);
                }
                homingBlocklist.add(entry.getKey());
                return true;
            }

            // Aim toward the target's center mass.
            Vec3 aimPoint = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 toTarget = aimPoint.subtract(projectile.position()).normalize();

            // Blend current heading toward the target by the homing strength (0-1).
            Vec3 newDir = motion.normalize().scale(1.0 - turn).add(toTarget.scale(turn)).normalize();
            Vec3 newMotion = newDir.scale(speed);
            projectile.setDeltaMovement(newMotion);
            projectile.hasImpulse = true;
            // Arrows zero out their motion once stuck in a block — keep them un-stuck.
            if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                arrow.setNoGravity(true);
            }

            // Keep arrow-like projectiles visually aligned with travel.
            double horiz = Math.sqrt(newMotion.x * newMotion.x + newMotion.z * newMotion.z);
            projectile.setYRot((float) (Math.atan2(newMotion.x, newMotion.z) * (180.0 / Math.PI)));
            projectile.setXRot((float) (Math.atan2(newMotion.y, horiz) * (180.0 / Math.PI)));
            projectile.yRotO = projectile.getYRot();
            projectile.xRotO = projectile.getXRot();

            // Homing trail so it's visible.
            level.sendParticles(ParticleTypes.CRIT,
                    projectile.getX(), projectile.getY(), projectile.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            return false;
        });
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity != null) {
            lockedTargets.remove(entity.getUUID());
            nextToggleAllowed.remove(entity.getUUID());
        }
    }
}
