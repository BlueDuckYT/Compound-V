package blueduck.compound_v.effect;

import blueduck.compound_v.entity.WebProjectileEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spider powers:
 * - Wall climbing: passive — stick to walls when touching them
 * - V press: fires a web projectile. Hits mob = slow/weaken. Hits block = sticks as anchor.
 * - Hold V: pendulum swing from anchored webs (momentum-based arc)
 * - Sneak + hold V: grapple pull directly toward nearest anchor
 * - Max 2 anchored webs at a time. Firing a 3rd removes the oldest.
 * - Particle string line drawn by the projectile entity itself.
 */
public class SpiderEffect extends CompoundVEffect {

    private static final DustParticleOptions WEB_PARTICLE = new DustParticleOptions(
            new Vector3f(0.9f, 0.9f, 0.9f), 0.4f);

    // Track stuck web entity IDs per player (insertion-ordered so oldest is first)
    private static final Map<UUID, LinkedList<Integer>> activeWebs = new ConcurrentHashMap<>();

    private static final int MAX_WEBS = 2;
    private static final double SWING_FORCE = 0.12;
    private static final double GRAPPLE_FORCE = 0.25;
    private static final double MAX_SWING_SPEED = 1.6;

    public SpiderEffect(MobEffectCategory category) {
        super(category);
    }

    // --- Wall climbing (every tick, passive) ---

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;

        // Wall climbing
        if (player.horizontalCollision && !player.onGround() && !player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();
            if (player.isShiftKeyDown()) {
                player.setDeltaMovement(motion.x, -0.05, motion.z);
            } else {
                player.setDeltaMovement(motion.x, 0.12, motion.z);
            }
            player.fallDistance = 0;
            player.hurtMarked = true;

            // Subtle web string particles while clinging
            if (player.tickCount % 10 == 0 && player.level() instanceof ServerLevel sl) {
                sl.sendParticles(WEB_PARTICLE,
                        player.getX(), player.getY() + 0.5, player.getZ(),
                        2, 0.2, 0.1, 0.2, 0.01);
            }
        }

        // Cleanup dead/discarded web entities from tracking
        cleanupWebs(player);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    // --- V press: fire web projectile ---

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        // Remove oldest anchor if at max
        LinkedList<Integer> webs = activeWebs.computeIfAbsent(player.getUUID(), k -> new LinkedList<>());
        if (webs.size() >= MAX_WEBS) {
            int oldestId = webs.removeFirst();
            Entity oldest = level.getEntity(oldestId);
            if (oldest != null) oldest.discard();
        }

        // Fire web projectile
        WebProjectileEntity web = new WebProjectileEntity(level, player);
        web.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.5F);
        level.addFreshEntity(web);

        // Track it (will only stay in list if it sticks to a block)
        webs.add(web.getId());

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.5F, 1.8F);
    }

    // --- Hold V: swing or grapple from anchored webs ---

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.holdActivate(player, amplifier, level);

        LinkedList<Integer> webs = activeWebs.get(player.getUUID());
        if (webs == null || webs.isEmpty()) return;

        // Find the nearest stuck anchor
        Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.5, 0);
        WebProjectileEntity nearestAnchor = null;
        double nearestDist = Double.MAX_VALUE;

        for (int id : webs) {
            Entity e = level.getEntity(id);
            if (e instanceof WebProjectileEntity web && web.isStuck() && web.getStuckPos() != null) {
                double dist = playerPos.distanceTo(web.getStuckPos());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestAnchor = web;
                }
            }
        }

        if (nearestAnchor == null) return;

        Vec3 anchor = nearestAnchor.getStuckPos();
        Vec3 toAnchor = anchor.subtract(playerPos);
        double dist = toAnchor.length();

        if (dist < 1.5) return; // Too close, don't pull

        Vec3 ropeDir = toAnchor.normalize();
        Vec3 currentMotion = player.getDeltaMovement();

        Vec3 newMotion;

        if (player.isShiftKeyDown()) {
            // --- GRAPPLE: pull directly toward anchor ---
            Vec3 pull = ropeDir.scale(GRAPPLE_FORCE);
            newMotion = currentMotion.add(pull);
        } else {
            // --- SWING: pendulum physics ---
            // Remove the component of velocity along the rope (keeps tangential momentum)
            // Then add a small centripetal correction + gravity compensation

            double ropeLength = dist; // Current distance acts as rope length

            // Component of velocity along the rope direction
            double radialSpeed = currentMotion.dot(ropeDir);

            // If moving away from anchor (stretching rope), apply corrective pull
            if (radialSpeed < 0) {
                // Moving away — pull back proportional to how much
                Vec3 correction = ropeDir.scale(-radialSpeed * 0.8);
                newMotion = currentMotion.add(correction);
            } else {
                newMotion = currentMotion;
            }

            // Small centripetal force to maintain arc
            double centripetalForce = SWING_FORCE * (1.0 + Math.max(0, currentMotion.horizontalDistance()) * 0.5);
            newMotion = newMotion.add(ropeDir.scale(centripetalForce));

            // Slight upward bias when below anchor (creates the upswing)
            double yDiff = anchor.y - playerPos.y;
            if (yDiff > 2.0) {
                newMotion = newMotion.add(0, 0.03, 0);
            }
        }

        // Clamp speed
        if (newMotion.length() > MAX_SWING_SPEED) {
            newMotion = newMotion.normalize().scale(MAX_SWING_SPEED);
        }

        player.setDeltaMovement(newMotion);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }

    // --- Cleanup: remove tracking for discarded/non-stuck webs ---

    private void cleanupWebs(ServerPlayer player) {
        LinkedList<Integer> webs = activeWebs.get(player.getUUID());
        if (webs == null) return;

        webs.removeIf(id -> {
            Entity e = player.serverLevel().getEntity(id);
            if (e == null) return true; // Entity gone
            if (e instanceof WebProjectileEntity web) {
                return !web.isAlive(); // Discarded
            }
            return true; // Not a web entity somehow
        });
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            // Discard all active webs
            LinkedList<Integer> webs = activeWebs.remove(player.getUUID());
            if (webs != null && entity.level() instanceof ServerLevel sl) {
                for (int id : webs) {
                    Entity e = sl.getEntity(id);
                    if (e != null) e.discard();
                }
            }
        }
    }
}
