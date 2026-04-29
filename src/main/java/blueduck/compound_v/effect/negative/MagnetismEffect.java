package blueduck.compound_v.effect.negative;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Magnetism — negative Compound V effect. Works on both players and mobs.
 *
 * Always-on magnetic field that pulls everything toward the affected entity:
 *
 * - Items & XP orbs within 16 blocks fly toward them (players only).
 * - In-flight projectiles within 12 blocks curve hard toward them.
 * - Living entities within 8 blocks are gently pulled toward them.
 *
 * For mobs: projectiles curve into them and nearby entities get dragged
 * in — makes the mob a magnet that disrupts combat around it.
 */
public class MagnetismEffect extends BadCompoundVEffect {

    private static final double ITEM_RANGE = 16.0;
    private static final double PROJECTILE_RANGE = 12.0;
    private static final double ENTITY_PULL_RANGE = 8.0;

    private static final double ITEM_PULL_STRENGTH = 0.08;
    private static final double PROJECTILE_CURVE_STRENGTH = 0.15;
    private static final double ENTITY_PULL_STRENGTH = 0.02;

    private static final DustParticleOptions MAGNETIC_PARTICLE = new DustParticleOptions(
            new Vector3f(0.6f, 0.6f, 0.65f), 0.6f);

    public MagnetismEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Vec3 centerPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        // === Items & XP orbs: players only ===
        if (entity instanceof Player) {
            AABB itemBox = entity.getBoundingBox().inflate(ITEM_RANGE);
            for (Entity e : level.getEntities(entity, itemBox,
                    ent -> ent instanceof ItemEntity || ent instanceof ExperienceOrb)) {
                Vec3 toEntity = centerPos.subtract(e.position()).normalize();
                double dist = e.distanceTo(entity);
                double strength = ITEM_PULL_STRENGTH * (1.0 + (ITEM_RANGE - dist) / ITEM_RANGE);
                e.setDeltaMovement(e.getDeltaMovement().add(toEntity.scale(strength)));
                e.hurtMarked = true;
            }
        }

        // === Projectiles: curve toward this entity ===
        AABB projBox = entity.getBoundingBox().inflate(PROJECTILE_RANGE);
        for (Entity e : level.getEntities(entity, projBox,
                ent -> ent instanceof Projectile)) {
            Projectile proj = (Projectile) e;

            // Don't curve this entity's own projectiles
            if (proj.getOwner() == entity) continue;

            Vec3 toEntity = entity.getEyePosition().subtract(proj.position()).normalize();
            Vec3 currentVel = proj.getDeltaMovement();
            double speed = currentVel.length();

            if (speed < 0.01) continue;

            Vec3 newVel = currentVel.normalize()
                    .add(toEntity.scale(PROJECTILE_CURVE_STRENGTH))
                    .normalize()
                    .scale(speed);

            proj.setDeltaMovement(newVel);
            proj.hurtMarked = true;

            if (entity.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        proj.getX(), proj.getY(), proj.getZ(),
                        1, 0.05, 0.05, 0.05, 0.02);
            }
        }

        // === Living entities: gentle pull toward this entity ===
        AABB pullBox = entity.getBoundingBox().inflate(ENTITY_PULL_RANGE);
        for (Entity e : level.getEntities(entity, pullBox,
                ent -> ent instanceof LivingEntity && ent.isAlive())) {
            LivingEntity target = (LivingEntity) e;
            Vec3 toEntity = centerPos.subtract(target.position()).normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(toEntity.scale(ENTITY_PULL_STRENGTH)));
            target.hurtMarked = true;
        }

        // === Visual feedback ===
        if (entity.tickCount % 6 == 0) {
            for (int i = 0; i < 3; i++) {
                double angle = entity.getRandom().nextDouble() * Math.PI * 2;
                double radius = 2.0 + entity.getRandom().nextDouble() * 3.0;
                double px = entity.getX() + Math.cos(angle) * radius;
                double pz = entity.getZ() + Math.sin(angle) * radius;
                double py = entity.getY() + 0.5 + entity.getRandom().nextDouble() * 1.5;
                level.sendParticles(MAGNETIC_PARTICLE,
                        px, py, pz,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        if (entity.tickCount % 60 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.NEUTRAL, 0.15F, 0.5F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }
}
