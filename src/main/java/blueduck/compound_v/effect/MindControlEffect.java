package blueduck.compound_v.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mind Control power (experimental, alongside Spider).
 *
 * Press V while looking at a mob within 32 blocks to take control of its
 * targeting. The mob's attack target is set to the nearest OTHER hostile
 * mob within 24 blocks of it. Has a 10-second (200 tick) cooldown.
 *
 * Visual/audio feedback:
 * - Purple portal particles swirl around the controlled mob's head.
 * - Enchantment sound plays on successful mind control.
 * - If no valid hostile target is found, a fizzle sound plays instead.
 */
public class MindControlEffect extends CompoundVEffect {

    private static final double RANGE = 32.0;
    private static final double MOB_SEARCH_RANGE = 24.0;
    private static final int COOLDOWN_TICKS = 200; // 10 seconds

    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();

    public MindControlEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();
        long lastUse = cooldownMap.getOrDefault(uuid, 0L);

        if (now - lastUse < COOLDOWN_TICKS) {
            // Still on cooldown — show remaining seconds in action bar
            int remaining = (int) ((COOLDOWN_TICKS - (now - lastUse)) / 20);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Mind Control on cooldown: " + remaining + "s"),
                    true);
            return;
        }

        // Raycast to find targeted mob
        Mob target = getTargetedMob(player, level);
        if (target == null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("No mob targeted"),
                    true);
            return;
        }

        // Find the nearest hostile mob near the target (not the target itself, not the player)
        Mob nearestHostile = findNearestHostile(target, level);
        if (nearestHostile == null) {
            // No valid target found — fizzle
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.5F);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("No hostile mobs nearby for target to attack"),
                    true);
            return;
        }

        // Set the controlled mob's target
        target.setTarget(nearestHostile);

        // Start cooldown
        cooldownMap.put(uuid, now);

        // === Visual feedback ===
        double headY = target.getY() + target.getBbHeight();

        // Purple portal particles swirl around the mob's head
        level.sendParticles(ParticleTypes.PORTAL,
                target.getX(), headY, target.getZ(),
                30, 0.3, 0.3, 0.3, 0.5);
        level.sendParticles(ParticleTypes.ENCHANT,
                target.getX(), headY + 0.5, target.getZ(),
                15, 0.3, 0.3, 0.3, 0.3);

        // Small particle trail from player to target
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 dir = end.subtract(start);
        double length = dir.length();
        Vec3 norm = dir.normalize();
        for (double d = 1.0; d < length; d += 1.5) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    start.x + norm.x * d, start.y + norm.y * d, start.z + norm.z * d,
                    1, 0.05, 0.05, 0.05, 0.01);
        }

        // Sound
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("Mind controlled!"),
                true);
    }

    /**
     * Raycasts from the player's eye position to find the Mob they're looking at.
     */
    private Mob getTargetedMob(ServerPlayer player, ServerLevel level) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(RANGE));

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(RANGE)).inflate(1.0);
        double closestDist = RANGE;
        Mob closest = null;

        for (Entity entity : level.getEntities(player, searchBox, e -> e instanceof Mob && e.isAlive())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = entityBox.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = (Mob) entity;
                }
            }
        }

        return closest;
    }

    /**
     * Finds the nearest hostile Mob within MOB_SEARCH_RANGE of the controlled mob,
     * excluding the controlled mob itself and any players.
     */
    private Mob findNearestHostile(Mob controlled, ServerLevel level) {
        AABB search = controlled.getBoundingBox().inflate(MOB_SEARCH_RANGE);
        double closestDist = Double.MAX_VALUE;
        Mob closest = null;

        for (Entity entity : level.getEntities(controlled, search,
                e -> e instanceof Mob && e.isAlive() && !(e instanceof Player))) {
            Mob mob = (Mob) entity;
            if (mob == controlled) continue;
            double dist = controlled.distanceTo(mob);
            if (dist < closestDist) {
                closestDist = dist;
                closest = mob;
            }
        }

        return closest;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            cooldownMap.remove(player.getUUID());
        }
    }
}
