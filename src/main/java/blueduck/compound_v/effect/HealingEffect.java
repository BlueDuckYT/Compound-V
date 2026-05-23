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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Healing — Support power.
 *
 * Player: Hold V to heal the entity you're looking at within reach range.
 *   1 HP per tick while holding. Heart particles on target, golden glow on healer.
 *   Also has passive self-regen (0.5 HP/sec out of combat).
 *
 * Mob: Passive healing aura (~5 blocks). Heals allied mobs at 0.5 HP/sec.
 *   Alliance: hostile heals hostile, non-hostile heals non-hostile.
 *   Visible heart particles trail between healer and healed.
 */
public class HealingEffect extends CompoundVEffect {

    private static final float PLAYER_HEAL_PER_TICK = 0.5f;  // 10 HP/sec — strong but not instant
    private static final float MOB_AURA_HEAL_PER_SEC = 0.5f;
    private static final double MOB_AURA_RANGE = 5.0;
    private static final double PLAYER_HEAL_RANGE = 5.0;
    private static final float SELF_REGEN_PER_SEC = 0.5f;

    public HealingEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // Tanky support — survives to keep healing
    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.7;
    }

    // === Player: hold V to heal target ===

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        // Raycast for entity in front of player within reach range
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(PLAYER_HEAL_RANGE));

        // Find entities along the ray
        AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(PLAYER_HEAL_RANGE)).inflate(1.0);
        LivingEntity target = null;
        double closest = PLAYER_HEAL_RANGE + 1;

        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            AABB entBox = e.getBoundingBox().inflate(0.3);
            var hitResult = entBox.clip(eyePos, endPos);
            if (hitResult.isPresent()) {
                double dist = eyePos.distanceTo(hitResult.get());
                if (dist < closest) {
                    closest = dist;
                    target = (LivingEntity) e;
                }
            }
        }

        if (target == null) return;

        // Don't heal if at full health
        if (target.getHealth() >= target.getMaxHealth()) {
            // Show no particles — nothing to heal
            return;
        }

        // Heal
        target.heal(PLAYER_HEAL_PER_TICK);

        // Heart particles on target
        level.sendParticles(ParticleTypes.HEART,
                target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(),
                1, 0.3, 0.2, 0.3, 0.0);

        // Golden glow particles on healer's hands
        Vec3 handOffset = lookDir.scale(0.8);
        level.sendParticles(ParticleTypes.GLOW,
                player.getX() + handOffset.x, player.getY() + player.getBbHeight() * 0.7 + handOffset.y,
                player.getZ() + handOffset.z,
                1, 0.1, 0.1, 0.1, 0.0);

        // Healing beam particles between healer and target
        if (player.tickCount % 3 == 0) {
            Vec3 from = player.position().add(0, player.getBbHeight() * 0.7, 0);
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 dir = to.subtract(from);
            int steps = (int) (dir.length() * 2);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                level.sendParticles(ParticleTypes.HEART,
                        from.x + dir.x * t, from.y + dir.y * t, from.z + dir.z * t,
                        1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Sound
        if (player.tickCount % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3F, 1.8F);
        }
    }

    // === Tick: passive self-regen + mob aura healing ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (entity.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) entity.level();

        // Passive self-regen once per second
        if (entity.tickCount % 20 == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(SELF_REGEN_PER_SEC);
        }

        // Mob aura healing
        if (entity instanceof Mob mob && entity.tickCount % 20 == 0) {
            tickMobAuraHealing(mob, level);
        }
    }

    private void tickMobAuraHealing(Mob healer, ServerLevel level) {
        boolean isHostile = healer instanceof net.minecraft.world.entity.monster.Enemy;

        AABB box = healer.getBoundingBox().inflate(MOB_AURA_RANGE);
        for (Entity e : level.getEntities(healer, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != healer)) {
            LivingEntity target = (LivingEntity) e;
            if (target.distanceTo(healer) > MOB_AURA_RANGE) continue;

            // Alliance check: hostiles heal hostiles, non-hostiles heal non-hostiles + players
            boolean targetIsHostile = target instanceof net.minecraft.world.entity.monster.Enemy;
            if (isHostile && !targetIsHostile) continue;
            if (!isHostile && targetIsHostile) continue;

            if (target.getHealth() >= target.getMaxHealth()) continue;

            // Heal
            target.heal(MOB_AURA_HEAL_PER_SEC);

            // Heart particles on healed target
            level.sendParticles(ParticleTypes.HEART,
                    target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(),
                    2, 0.3, 0.2, 0.3, 0.0);

            // Healing beam trail from healer to target
            Vec3 from = healer.position().add(0, healer.getBbHeight() * 0.7, 0);
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 dir = to.subtract(from);
            int steps = Math.max(2, (int) (dir.length() * 1.5));
            for (int i = 0; i <= steps; i += 2) {
                double t = (double) i / steps;
                level.sendParticles(ParticleTypes.HEART,
                        from.x + dir.x * t, from.y + dir.y * t, from.z + dir.z * t,
                        1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Ambient glow on healer to make them identifiable
        if (healer.tickCount % 10 == 0) {
            level.sendParticles(ParticleTypes.GLOW,
                    healer.getX(), healer.getY() + healer.getBbHeight() * 0.5, healer.getZ(),
                    3, 0.3, 0.3, 0.3, 0.0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }
}
