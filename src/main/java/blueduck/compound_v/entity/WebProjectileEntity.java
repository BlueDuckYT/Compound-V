package blueduck.compound_v.entity;

import blueduck.compound_v.registry.EntityReg;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class WebProjectileEntity extends ThrowableItemProjectile {

    // Thin white-gray string particles
    private static final DustParticleOptions WEB_STRING = new DustParticleOptions(
            new Vector3f(0.9f, 0.9f, 0.9f), 0.4f);
    private static final DustParticleOptions WEB_STRING_THIN = new DustParticleOptions(
            new Vector3f(0.85f, 0.85f, 0.85f), 0.25f);

    private boolean stuck = false;
    private Vec3 stuckPos = null;

    public WebProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public WebProjectileEntity(Level level, LivingEntity shooter) {
        super(EntityReg.WEB_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STRING;
    }

    @Override
    protected float getGravity() {
        return stuck ? 0.0F : 0.01F;
    }

    public boolean isStuck() {
        return stuck;
    }

    public Vec3 getStuckPos() {
        return stuckPos;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target && !level().isClientSide) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));

            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(WEB_STRING,
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        15, 0.4, 0.5, 0.4, 0.05);
            }

            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            // Stick to the block surface
            stuck = true;
            stuckPos = result.getLocation();
            setDeltaMovement(Vec3.ZERO);
            setPos(stuckPos);
            setNoGravity(true);

            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(WEB_STRING,
                        stuckPos.x, stuckPos.y, stuckPos.z,
                        8, 0.15, 0.15, 0.15, 0.01);
            }
            level().playSound(null, stuckPos.x, stuckPos.y, stuckPos.z,
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.5F, 1.2F);
        }
    }

    @Override
    public void tick() {
        if (stuck) {
            // Stay in place
            setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null) {
                setPos(stuckPos);
            }

            // Draw particle line from anchor to owner
            Entity owner = getOwner();
            if (owner != null && level() instanceof ServerLevel sl && tickCount % 2 == 0) {
                Vec3 start = owner.position().add(0, owner.getEyeHeight() * 0.7, 0);
                Vec3 end = position();
                Vec3 dir = end.subtract(start);
                double length = dir.length();
                if (length > 0.5) {
                    dir = dir.normalize();
                    double step = 1.2;
                    for (double d = 0; d < length; d += step) {
                        double x = start.x + dir.x * d;
                        double y = start.y + dir.y * d;
                        double z = start.z + dir.z * d;
                        sl.sendParticles(WEB_STRING_THIN, x, y, z,
                                1, 0.01, 0.01, 0.01, 0.0);
                    }
                }
            }

            // Despawn if owner gone or too far away
            if (owner == null || owner.distanceTo(this) > 80 || tickCount > 600) {
                discard();
            }
            return;
        }

        super.tick();

        // Trail particles while flying
        if (level() instanceof ServerLevel sl && tickCount % 2 == 0) {
            sl.sendParticles(WEB_STRING_THIN,
                    getX(), getY(), getZ(), 1, 0.02, 0.02, 0.02, 0.0);
        }

        // Draw line from player to projectile while in flight
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel sl && tickCount % 3 == 0) {
            Vec3 start = owner.position().add(0, owner.getEyeHeight() * 0.7, 0);
            Vec3 end = position();
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            if (length > 1.0) {
                dir = dir.normalize();
                for (double d = 0; d < length; d += 1.5) {
                    sl.sendParticles(WEB_STRING_THIN,
                            start.x + dir.x * d, start.y + dir.y * d, start.z + dir.z * d,
                            1, 0.01, 0.01, 0.01, 0.0);
                }
            }
        }

        if (tickCount > 60) discard(); // 3 second flight max
    }

    @Override
    public boolean isNoGravity() {
        return stuck || super.isNoGravity();
    }
}
