package blueduck.compound_v.entity;

import blueduck.compound_v.Config;
import blueduck.compound_v.registry.EntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ice Ball — a bouncing frost projectile (Pyrokinesis's ice counterpart).
 *
 * - Bounces off blocks (velocity reflected on the hit face, with damping) instead of
 *   breaking on impact.
 * - Skips across water like a stone: it freezes the water surface just ahead into ice
 *   and then bounces off that newly-created ice.
 * - Freezes the first living entity it strikes (frozen ticks + Slowness), then despawns.
 * - Despawns after a configurable lifetime (default 5s) rather than a bounce count.
 *
 * Renders with the vanilla snowball model via ThrownItemRenderer + SNOWBALL default item.
 */
public class IceProjectileEntity extends ThrowableItemProjectile {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_CHARGED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    IceProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private int stagnantTicks = 0; // consecutive ticks spent barely moving

    public IceProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public IceProjectileEntity(Level level, LivingEntity shooter) {
        super(EntityReg.ICE_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGED, false);
    }

    /** Mark this as a big charged cryoball (slower, larger, AOE-freeze on expire). */
    public void setCharged(boolean v) { this.entityData.set(DATA_CHARGED, v); }
    public boolean isCharged() { return this.entityData.get(DATA_CHARGED); }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("CompoundVCharged", isCharged());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("CompoundVCharged")) setCharged(true);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected float getGravity() {
        // Charged ball is clunkier — heavier arc that drops faster.
        return isCharged() ? 0.09F : 0.03F;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        if (result.getEntity() instanceof LivingEntity target) {
            freezeEntity(target);
            // Big charged ball deals impact damage; the small ball deals none.
            if (isCharged() && Config.cryoChargedImpactDamage > 0) {
                target.hurt(damageSources().freeze(), (float) Config.cryoChargedImpactDamage);
            }
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        20, 0.3, 0.5, 0.3, 0.05);
            }
            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
            // The big charged ball also detonates its AOE freeze burst on a direct mob hit, not
            // just on expiry.
            if (isCharged()) detonateFreeze();
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Do NOT call super (which would normally stop the projectile). Bounce instead.
        if (level().isClientSide) return;

        Direction face = result.getDirection();
        Vec3 motion = getDeltaMovement();
        double damping = Config.cryoBounceDamping;

        // Reflect the velocity component along the hit face's axis.
        double vx = motion.x, vy = motion.y, vz = motion.z;
        switch (face.getAxis()) {
            case X -> vx = -vx;
            case Y -> vy = -vy;
            case Z -> vz = -vz;
        }
        Vec3 bounced = new Vec3(vx, vy, vz).scale(damping);

        // Nudge the projectile back out of the surface so it doesn't immediately re-collide.
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        setPos(position().add(normal.scale(0.1)));
        setDeltaMovement(bounced);
        hasImpulse = true;

        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.02);
            sl.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    getX(), getY(), getZ(), 5, 0.1, 0.1, 0.1, 0.02);
        }
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.SNOW_BREAK, SoundSource.PLAYERS, 0.5F, 1.3F);
    }

    @Override
    public void tick() {
        // Water-skip: freeze the water surface just ahead/below so the projectile
        // bounces off the new ice instead of plunging in.
        if (!level().isClientSide && Config.cryoBallFreezesWater) {
            freezeWaterAround();
        }

        super.tick();

        // Frost trail while flying.
        if (level() instanceof ServerLevel sl && tickCount % 2 == 0) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(), 1, 0.02, 0.02, 0.02, 0.0);
        }

        // Stagnation poof: if the ball has nearly stopped (e.g. wedged in a corner
        // bouncing imperceptibly), count it down and poof after a short grace period
        // rather than letting it linger until its lifetime expires. Skip the first few
        // ticks so a freshly-thrown ball isn't caught before it gets up to speed.
        if (!level().isClientSide && tickCount > 3) {
            if (getDeltaMovement().lengthSqr() < 0.0025) { // < 0.05 blocks/tick
                stagnantTicks++;
            } else {
                stagnantTicks = 0;
            }
            if (stagnantTicks >= 10) {
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 12, 0.15, 0.15, 0.15, 0.03);
                    sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, getX(), getY(), getZ(), 6, 0.1, 0.1, 0.1, 0.02);
                }
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.SNOW_BREAK, SoundSource.PLAYERS, 0.5F, 1.5F);
                if (isCharged()) detonateFreeze();
                discard();
                return;
            }
        }

        // Lifetime cap (default 5s) instead of a bounce count.
        if (tickCount > Config.cryoLifetimeTicks) {
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.02);
            }
            if (isCharged()) detonateFreeze();
            discard();
        }
    }

    /**
     * Charged cryoball detonation: freeze every mob in a small radius solid. Called when the
     * big charged ball expires (lifetime or stagnation).
     */
    private void detonateFreeze() {
        if (!(level() instanceof ServerLevel sl)) return;
        double r = Config.cryoChargedFreezeRadius;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                getX() - r, getY() - r, getZ() - r, getX() + r, getY() + r, getZ() + r);
        int freezeTicks = Config.cryoChargedFreezeTicks;
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.distanceToSqr(this) <= r * r)) {
            // Don't affect the thrower.
            if (getOwner() != null && le.getUUID().equals(getOwner().getUUID())) continue;
            le.setTicksFrozen(Math.max(le.getTicksFrozen(), freezeTicks));
            le.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    freezeTicks, Config.cryoSlownessAmplifier, false, true, true));
            // Small burst damage.
            if (Config.cryoChargedBurstDamage > 0) {
                le.hurt(damageSources().freeze(), (float) Config.cryoChargedBurstDamage);
            }
            // Small outward knockback from the blast center.
            if (Config.cryoChargedBurstKnockback > 0) {
                Vec3 away = le.position().subtract(position());
                if (away.lengthSqr() < 1.0e-4) away = new Vec3(0, 1, 0); // dead center -> pop up
                away = away.normalize();
                double kb = Config.cryoChargedBurstKnockback;
                le.push(away.x * kb, away.y * kb * 0.5 + 0.15, away.z * kb);
                le.hurtMarked = true;
            }
        }
        // Burst visual + sound.
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(),
                60, r * 0.5, r * 0.5, r * 0.5, 0.05);
        sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, getX(), getY(), getZ(),
                30, r * 0.4, r * 0.4, r * 0.4, 0.08);
        sl.playSound(null, getX(), getY(), getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        sl.playSound(null, getX(), getY(), getZ(),
                SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    /** Freezes a patch of water around/below the projectile's path into frosted ice (Frost Walker style, melts after a bit). */
    private void freezeWaterAround() {
        BlockPos center = blockPosition();
        int r = Math.max(0, Config.cryoBallFreezeRadius);
        boolean frozeAny = false;
        // Scan a disc at the projectile's level and one below (the surface it skims).
        for (int dy = 0; dy >= -1; dy--) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r) continue; // circular patch
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState bs = level().getBlockState(pos);
                    FluidState fs = bs.getFluidState();
                    if (!fs.isEmpty() && fs.is(net.minecraft.tags.FluidTags.WATER) && fs.isSource()) {
                        // Frost Walker's temporary frosted ice — solid enough to skip off,
                        // melts back to water on its own after a short while.
                        level().setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                        // Schedule the same random melt tick Frost Walker uses.
                        level().scheduleTick(pos, Blocks.FROSTED_ICE,
                                net.minecraft.util.Mth.nextInt(level().getRandom(), 60, 120));
                        frozeAny = true;
                    }
                }
            }
        }
        if (frozeAny && level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY() + 0.2, getZ(), 10, r * 0.4, 0.05, r * 0.4, 0.02);
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.5F, 1.4F);
        }
    }

    private void freezeEntity(LivingEntity target) {
        int freezeTicks = Config.cryoFreezeTicks;
        // Vanilla powder-snow freezing: raise the frozen-tick counter.
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), freezeTicks));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                freezeTicks, Config.cryoSlownessAmplifier, false, true, true));
        if (Config.cryoBallDamage > 0) {
            // Attribute the hit to the shooter so kills/knockback credit the player.
            // The freeze EFFECT (frozen ticks + Slowness) above is independent of this.
            net.minecraft.world.entity.Entity owner = getOwner();
            net.minecraft.world.damagesource.DamageSource src =
                    damageSources().mobProjectile(this,
                            owner instanceof LivingEntity le ? le : null);
            target.hurt(src, (float) Config.cryoBallDamage);
        }
    }
}
