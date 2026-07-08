package blueduck.compound_v.entity;

import blueduck.compound_v.registry.EntityReg;
import blueduck.compound_v.Config;
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

    // Synced so the CLIENT knows when the web is anchored (the client drives the swing for
    // the local player, where velocity changes are smooth and authoritative). The anchor
    // position itself is already synced via the entity's own position once stuck.
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_STUCK =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    WebProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_ON_MOB =
            net.minecraft.network.syncher.SynchedEntityData.defineId(
                    WebProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    private boolean stuck = false;
    private boolean mobYankShot = false; // true if fired by a spider MOB to yank its target on hit

    public void setMobYankShot(boolean v) { this.mobYankShot = v; }
    private Vec3 stuckPos = null;
    private java.util.UUID stuckEntity = null; // mob the web latched onto (for reeling)
    private int ownerMissingTicks = 0;         // consecutive ticks the owner couldn't be found
    private int stuckTicks = 0;                // ticks elapsed since this web became stuck
    private java.util.UUID shooterUUID = null; // captured at spawn; authoritative owner id

    public WebProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public WebProjectileEntity(Level level, LivingEntity shooter) {
        super(EntityReg.WEB_PROJECTILE.get(), shooter, level);
        if (shooter != null) this.shooterUUID = shooter.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STUCK, false);
        this.entityData.define(DATA_ON_MOB, false);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected float getGravity() {
        return stuck ? 0.0F : 0.01F * (float) blueduck.compound_v.Config.spiderWebGravityMult;
    }

    public boolean isStuck() {
        // On the client, trust the synced flag; on the server, the field.
        return level().isClientSide ? this.entityData.get(DATA_STUCK) : stuck;
    }

    public Vec3 getStuckPos() {
        return stuckPos;
    }

    public java.util.UUID getStuckEntity() {
        return stuckEntity;
    }

    public boolean isOnMob() {
        return level().isClientSide ? this.entityData.get(DATA_ON_MOB) : (stuckEntity != null);
    }

    /**
     * Resolve the shooter authoritatively on the server. Prefers the captured shooter UUID
     * looked up against the connected player list (so a momentarily-null getOwner() doesn't
     * read as "gone"), then falls back to the projectile's own owner reference, then a level
     * entity lookup. Returns null only when the shooter truly isn't present.
     */
    private Entity resolveOwner() {
        if (level() instanceof ServerLevel sl) {
            if (shooterUUID != null && sl.getServer() != null) {
                Entity p = sl.getServer().getPlayerList().getPlayer(shooterUUID);
                if (p != null) return p;
            }
            Entity o = getOwner();
            if (o != null) return o;
            if (shooterUUID != null) {
                Entity e = sl.getEntity(shooterUUID);
                if (e != null) return e;
            }
            return null;
        }
        return getOwner();
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (shooterUUID != null) tag.putUUID("CompoundVShooter", shooterUUID);
        tag.putBoolean("CompoundVStuck", stuck);
        tag.putBoolean("CompoundVMobYank", mobYankShot);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("CompoundVShooter")) shooterUUID = tag.getUUID("CompoundVShooter");
        if (tag.getBoolean("CompoundVMobYank")) mobYankShot = true;
        if (tag.getBoolean("CompoundVStuck")) {
            stuck = true;
            this.entityData.set(DATA_STUCK, true);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target && !level().isClientSide) {
            // Mob-fired yank shot: pull the struck target toward the shooter and root it
            // briefly, then despawn (fire-and-forget; the mob doesn't reel via scroll).
            if (mobYankShot) {
                Entity shooter = getOwner();
                if (shooter != null) {
                    Vec3 toShooter = shooter.position().add(0, shooter.getBbHeight() * 0.5, 0)
                            .subtract(target.position().add(0, target.getBbHeight() * 0.5, 0));
                    double d = toShooter.length();
                    if (d > 1.5) {
                        Vec3 pull = toShooter.normalize().scale(Math.min(1.8, d * 0.22 + 0.4));
                        target.setDeltaMovement(pull.x, Math.min(0.7, pull.y + 0.3), pull.z);
                        target.hurtMarked = true;
                        target.fallDistance = 0;
                    }
                }
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, true, true));
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(WEB_STRING,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            12, 0.4, 0.5, 0.4, 0.05);
                }
                level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.SLIME_BLOCK_PLACE, SoundSource.HOSTILE, 0.8F, 0.9F);
                discard();
                return;
            }

            // Web-root: a solidly webbed mob is slowed heavily and weakened (cocooned feel).
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2, false, true, true));

            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(WEB_STRING,
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        15, 0.4, 0.5, 0.4, 0.05);
            }
            level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);

            // Latch onto the mob so the owner can reel it in via scroll while V is held.
            stuck = true;
            stuckEntity = target.getUUID();
            this.entityData.set(DATA_STUCK, true);
            this.entityData.set(DATA_ON_MOB, true);
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
            return; // do NOT discard - stays as the active web until released/cut
        }
        discard();
    }

    /**
     * Raycast webbing: instantly latch this web to a block position (no projectile travel).
     * Mirrors the stuck state set by a normal block hit.
     */
    public void forceStickToBlock(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
        stuck = true;
        stuckPos = pos;
        this.entityData.set(DATA_STUCK, true);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Raycast webbing: instantly latch this web onto a mob (no projectile travel). Applies the
     * same web-root effects a thrown web would, and marks it latched for scroll-reel/swing.
     */
    public void forceStickToMob(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2, false, true, true));
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(WEB_STRING,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    15, 0.4, 0.5, 0.4, 0.05);
        }
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
        Vec3 tp = target.position().add(0, target.getBbHeight() * 0.5, 0);
        setPos(tp.x, tp.y, tp.z);
        stuck = true;
        stuckEntity = target.getUUID();
        this.entityData.set(DATA_STUCK, true);
        this.entityData.set(DATA_ON_MOB, true);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            // Stick to the block surface
            stuck = true;
            stuckPos = result.getLocation();
            this.entityData.set(DATA_STUCK, true);
            this.entityData.set(DATA_ON_MOB, false);
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
        // Use the synced stuck state so this works on BOTH sides. On the client the raw
        // `stuck` field is never set (only DATA_STUCK syncs), so checking it directly made the
        // client treat a stuck web as still-in-flight and discard it after 3s - the despawn
        // the player was seeing. isStuck() reads the synced flag on the client.
        if (isStuck()) {
            // If latched to a mob, follow it (and drop if it died/vanished).
            if (stuckEntity != null) {
                if (level() instanceof ServerLevel sl) {
                    Entity tgt = sl.getEntity(stuckEntity);
                    if (tgt == null || !tgt.isAlive()) {
                        discard();
                        return;
                    }
                    // Break the strand if the latched mob gets too far from the shooter - the
                    // web shouldn't keep a mob tethered across the world.
                    Entity owner = resolveOwner();
                    if (owner != null && owner.distanceTo(tgt) > Config.spiderMaxRope + 8.0) {
                        discard();
                        return;
                    }
                    stuckPos = tgt.position().add(0, tgt.getBbHeight() * 0.5, 0);
                    setPos(stuckPos.x, stuckPos.y, stuckPos.z);
                }
            }
            // Stay in place
            setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null && stuckEntity == null) {
                setPos(stuckPos);
            }

            // Lifetime / orphan handling is SERVER-side only - the client must never discard a
            // stuck web on its own (the server is authoritative and will sync removal).
            if (level() instanceof ServerLevel sl) {
                stuckTicks++;
                // Resolve the shooter authoritatively: prefer the captured UUID looked up
                // against the connected player list; fall back to the owner reference. A
                // momentarily-null getOwner() must not read as "gone".
                Entity owner = resolveOwner();
                if (owner == null) ownerMissingTicks++;
                else ownerMissingTicks = 0;

                // A web owned by a non-player has no release/reel/scroll mechanism to cut it, so
                // mob webs are given a maximum stuck lifetime and self-clean. Player webs are
                // exempt - they persist while held and are cut by the player's own controls. Only
                // applied when the owner is confirmed a non-player; a momentarily-null owner falls
                // through to the longer orphan timer below so a player's web isn't culled during a
                // brief resolution hiccup.
                if (owner != null && !(owner instanceof net.minecraft.world.entity.player.Player)
                        && stuckTicks > Config.spiderMobWebStuckMaxTicks) {
                    discard();
                    return;
                }

                if (owner != null && tickCount % 2 == 0) {
                    Vec3 start = owner.position().add(0, owner.getEyeHeight() * 0.7, 0);
                    Vec3 end = position();
                    Vec3 dir = end.subtract(start);
                    double length = dir.length();
                    if (length > 0.5) {
                        dir = dir.normalize();
                        double step = 1.2;
                        for (double d = 0; d < length; d += step) {
                            sl.sendParticles(WEB_STRING_THIN,
                                    start.x + dir.x * d, start.y + dir.y * d, start.z + dir.z * d,
                                    1, 0.01, 0.01, 0.01, 0.0);
                        }
                    }
                }

                // While the player is connected, the web NEVER despawns on its own. It only
                // drops if the shooter is genuinely gone for a long sustained stretch (~30s),
                // a safety net so orphaned webs don't leak forever.
                if (ownerMissingTicks > 600) {
                    discard();
                }
            }
            return;
        }

        super.tick();

        // Trail particles while flying (server-side)
        if (level() instanceof ServerLevel sl && tickCount % 2 == 0) {
            sl.sendParticles(WEB_STRING_THIN,
                    getX(), getY(), getZ(), 1, 0.02, 0.02, 0.02, 0.0);
        }

        // Draw line from player to projectile while in flight (server-side)
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

        // Flight timeout - SERVER-side only, and only while genuinely still in flight.
        if (!level().isClientSide && tickCount > 60) discard(); // 3 second flight max
    }

    @Override
    public boolean isNoGravity() {
        return stuck || super.isNoGravity();
    }
}
