package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Petrifying Gaze — While making direct eye contact with a mob or player,
 * they cannot move, jump, or use Compound V powers.
 *
 * Range is configurable (default 40 blocks). Only affects one target at a time
 * (the entity closest to crosshair center). Breaking line of sight or looking
 * away immediately unfreezes the target.
 *
 * Enabled on players by default (configurable). Mobs cannot have this power.
 */
public class PetrifyingGazeEffect extends CompoundVEffect {

    private static final DustParticleOptions STONE_PARTICLE = new DustParticleOptions(
            new Vector3f(0.5f, 0.5f, 0.5f), 1.0f); // gray stone dust

    // Track who each gazer is currently petrifying — store entity directly
    private static final Map<UUID, UUID> gazeTarget = new ConcurrentHashMap<>();
    private static final Map<UUID, LivingEntity> gazeTargetEntity = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> gazeLocked = new ConcurrentHashMap<>();

    public PetrifyingGazeEffect(MobEffectCategory category) {
        super(category);
    }

    // Moderate passive — gaze is the power, not melee
    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.8;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID gazerUUID = player.getUUID();
        double gazeRange = Config.petrifyingGazeRange;

        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();

        LivingEntity prevTarget = gazeTargetEntity.get(gazerUUID);

        // Validate cached entity
        if (prevTarget != null && (!prevTarget.isAlive() || prevTarget.isRemoved())) {
            prevTarget = null;
        }

        // If we have a locked target, check if still looking at them (wide threshold)
        if (prevTarget != null) {
            double dist = prevTarget.distanceTo(player);
            Vec3 targetCenter = prevTarget.position().add(0, prevTarget.getBbHeight() * 0.5, 0);
            Vec3 toTarget = targetCenter.subtract(eyePos).normalize();
            double dot = lookDir.dot(toTarget);

            // Maintain threshold: ~20 degrees (cos 20° ≈ 0.940)
            if (dot > 0.940 && dist <= gazeRange && player.hasLineOfSight(prevTarget)) {
                applyPetrify(player, prevTarget, level, eyePos);
                return;
            }

            // Lost the lock
            unfreezeEntity(prevTarget, level);
            gazeTarget.remove(gazerUUID);
            gazeTargetEntity.remove(gazerUUID);
            gazeLocked.remove(gazerUUID);
        }

        // Search for new target — lock-on threshold: ~10 degrees (cos 10° ≈ 0.985)
        LivingEntity bestTarget = null;
        double bestDot = 0.985;

        AABB searchBox = player.getBoundingBox().inflate(gazeRange);
        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity candidate = (LivingEntity) e;
            if (candidate instanceof Player && !Config.petrifyingGazeAffectsPlayers) continue;

            double dist = candidate.distanceTo(player);
            if (dist > gazeRange) continue;

            Vec3 targetCenter = candidate.position().add(0, candidate.getBbHeight() * 0.5, 0);
            Vec3 toTarget = targetCenter.subtract(eyePos).normalize();
            double dot = lookDir.dot(toTarget);

            if (dot > bestDot && player.hasLineOfSight(candidate)) {
                bestDot = dot;
                bestTarget = candidate;
            }
        }

        if (bestTarget != null) {
            gazeTarget.put(gazerUUID, bestTarget.getUUID());
            gazeTargetEntity.put(gazerUUID, bestTarget);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.5F, 0.3F);
            gazeLocked.put(gazerUUID, true);
            applyPetrify(player, bestTarget, level, eyePos);
        }
    }

    /**
     * Apply petrification effects to the target.
     */
    private void applyPetrify(ServerPlayer player, LivingEntity target, ServerLevel level, Vec3 eyePos) {
        target.setDeltaMovement(0, target.onGround() ? 0 : target.getDeltaMovement().y, 0);
        target.hurtMarked = true;

        if (target instanceof net.minecraft.world.entity.Mob mob) {
            mob.getNavigation().stop();
            mob.setTarget(null);
        }

        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));
        target.addEffect(new MobEffectInstance(
                MobEffects.JUMP, 5, 128, false, false, false));

        target.getPersistentData().putBoolean("compound_v_petrified", true);

        if (player.tickCount % 3 == 0) {
            level.sendParticles(STONE_PARTICLE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    4, 0.4, 0.5, 0.4, 0.01);
        }
        if (player.tickCount % 8 == 0) {
            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.3, target.getZ(),
                    2, 0.3, 0.3, 0.3, 0.02);
        }
        if (player.tickCount % 20 == 0) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3F, 0.5F);
        }
    }

    private void unfreezeEntity(LivingEntity target, ServerLevel level) {
        target.getPersistentData().remove("compound_v_petrified");
        level.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10, 0.3, 0.4, 0.3, 0.1);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.5F, 0.8F);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            UUID gazerUUID = player.getUUID();
            gazeTarget.remove(gazerUUID);
            gazeLocked.remove(gazerUUID);
            LivingEntity target = gazeTargetEntity.remove(gazerUUID);
            if (target != null && target.isAlive() && entity.level() instanceof ServerLevel level) {
                unfreezeEntity(target, level);
            }
        }
    }
}
