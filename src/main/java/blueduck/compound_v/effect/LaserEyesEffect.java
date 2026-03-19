package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.keybinds.PacketHandler;
import blueduck.compound_v.util.S2CLaserSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class LaserEyesEffect extends CompoundVEffect {

    protected static final DustParticleOptions LASER_CORE_ORANGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.6f, 0.1f), 1.2f);
    protected static final DustParticleOptions LASER_GLOW_ORANGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.8f, 0.3f), 0.6f);
    protected static final DustParticleOptions LASER_CORE_BLUE = new DustParticleOptions(
            new Vector3f(0.15f, 0.4f, 1.0f), 1.2f);
    protected static final DustParticleOptions LASER_GLOW_BLUE = new DustParticleOptions(
            new Vector3f(0.3f, 0.6f, 1.0f), 0.6f);
    protected static final DustParticleOptions LASER_CORE_GREEN = new DustParticleOptions(
            new Vector3f(0.1f, 1.0f, 0.2f), 1.2f);
    protected static final DustParticleOptions LASER_GLOW_GREEN = new DustParticleOptions(
            new Vector3f(0.3f, 1.0f, 0.4f), 0.6f);
    protected static final DustParticleOptions LASER_CORE_PURPLE = new DustParticleOptions(
            new Vector3f(0.6f, 0.15f, 1.0f), 1.2f);
    protected static final DustParticleOptions LASER_GLOW_PURPLE = new DustParticleOptions(
            new Vector3f(0.4f, 0.05f, 0.85f), 0.6f);
    protected static final DustParticleOptions LASER_CORE_YELLOW = new DustParticleOptions(
            new Vector3f(1.0f, 0.9f, 0.15f), 1.2f);
    protected static final DustParticleOptions LASER_GLOW_YELLOW = new DustParticleOptions(
            new Vector3f(0.9f, 0.75f, 0.02f), 0.6f);

    public LaserEyesEffect(MobEffectCategory category) {
        super(category);
    }

    protected float getLaserDamage() {
        return (float) Config.laserBasicDamage;
    }

    protected int getLaserRange() {
        return Config.laserBasicRange;
    }

    protected double getFireChance() {
        return Config.laserBasicFireChance;
    }

    protected boolean isAdvanced() {
        return false;
    }

    /**
     * Determines the laser color for this player.
     * Advanced: always red. Basic: even 20% split across orange, blue, green, purple, yellow.
     * Color is stable per-player per-world (seeded from UUID + world seed).
     */
    protected int getPlayerColorIndex(ServerPlayer player) {
        if (isAdvanced()) return S2CLaserSyncPacket.COLOR_RED;

        long uuidHash = player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits();
        long seed = player.serverLevel().getSeed();
        // Mix UUID and world seed so the same player gets different colors on different worlds
        long hash = uuidHash * 6364136223846793005L + seed;
        // Use a wide bit range for a clean 5-way split, deterministic per player+world
        int bucket = (int) (((hash >>> 4) & 0xFFFFFFL) % 5);
        return switch (bucket) {
            case 0 -> S2CLaserSyncPacket.COLOR_ORANGE;
            case 1 -> S2CLaserSyncPacket.COLOR_BLUE;
            case 2 -> S2CLaserSyncPacket.COLOR_GREEN;
            case 3 -> S2CLaserSyncPacket.COLOR_PURPLE;
            case 4 -> S2CLaserSyncPacket.COLOR_YELLOW;
            default -> S2CLaserSyncPacket.COLOR_ORANGE;
        };
    }

    protected DustParticleOptions getCoreParticle(int colorIndex) {
        return switch (colorIndex) {
            case S2CLaserSyncPacket.COLOR_BLUE -> LASER_CORE_BLUE;
            case S2CLaserSyncPacket.COLOR_GREEN -> LASER_CORE_GREEN;
            case S2CLaserSyncPacket.COLOR_PURPLE -> LASER_CORE_PURPLE;
            case S2CLaserSyncPacket.COLOR_YELLOW -> LASER_CORE_YELLOW;
            default -> LASER_CORE_ORANGE;
        };
    }

    protected DustParticleOptions getGlowParticle(int colorIndex) {
        return switch (colorIndex) {
            case S2CLaserSyncPacket.COLOR_BLUE -> LASER_GLOW_BLUE;
            case S2CLaserSyncPacket.COLOR_GREEN -> LASER_GLOW_GREEN;
            case S2CLaserSyncPacket.COLOR_PURPLE -> LASER_GLOW_PURPLE;
            case S2CLaserSyncPacket.COLOR_YELLOW -> LASER_GLOW_YELLOW;
            default -> LASER_GLOW_ORANGE;
        };
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.holdActivate(player, amplifier, level);
        fireLaser(player, amplifier, level);
    }

    /**
     * Checks if a block is soft enough for the laser to burn through instantly.
     */
    private boolean isSoftBlock(BlockState state) {
        Block block = state.getBlock();
        // Plants, bushes, tall grass, flowers, vines, snow layers, webs, etc.
        return block instanceof BushBlock
                || block instanceof DoublePlantBlock
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.LEAVES)
                || block == Blocks.TALL_GRASS
                || block == Blocks.LARGE_FERN
                || block == Blocks.VINE
                || block == Blocks.DEAD_BUSH
                || block == Blocks.COBWEB
                || block == Blocks.SNOW
                || block == Blocks.SUGAR_CANE
                || block == Blocks.KELP
                || block == Blocks.KELP_PLANT
                || block == Blocks.SEAGRASS
                || block == Blocks.TALL_SEAGRASS;
    }

    protected void fireLaser(ServerPlayer player, int amplifier, ServerLevel level) {
        int colorIndex = getPlayerColorIndex(player);

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        int range = getLaserRange();
        Vec3 endPos = eyePos.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);

        // --- Raycast, burning through soft blocks ---
        Vec3 currentStart = eyePos;
        Vec3 hitPos = endPos;
        boolean hitSolidBlock = false;

        for (int attempts = 0; attempts < 10; attempts++) {
            BlockHitResult blockHit = level.clip(new ClipContext(
                    currentStart, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

            if (blockHit.getType() == HitResult.Type.MISS) {
                hitPos = endPos;
                break;
            }

            BlockPos hitBlockPos = blockHit.getBlockPos();
            BlockState hitState = level.getBlockState(hitBlockPos);

            if (isSoftBlock(hitState) && level.random.nextFloat() < 0.625f) {
                // Burn through: destroy the block and continue
                level.destroyBlock(hitBlockPos, false);
                level.sendParticles(ParticleTypes.FLAME,
                        hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5,
                        3, 0.3, 0.3, 0.3, 0.02);
                // Move start just past the destroyed block
                currentStart = blockHit.getLocation().add(lookVec.scale(0.1));
                continue;
            }

            // Hit a solid block
            hitPos = blockHit.getLocation();
            hitSolidBlock = true;
            break;
        }

        double beamLength = eyePos.distanceTo(hitPos);

        // --- Damage entities along beam (every tick, low damage, NO knockback) ---
        AABB beamBox = new AABB(eyePos, hitPos).inflate(0.5);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, beamBox,
                e -> e != player && e.isAlive());

        for (LivingEntity target : entities) {
            Vec3 toEntity = target.position().add(0, target.getBbHeight() / 2, 0).subtract(eyePos);
            double dot = toEntity.dot(lookVec);
            if (dot > 0 && dot < beamLength) {
                Vec3 closestOnBeam = eyePos.add(lookVec.scale(dot));
                double distToBeam = closestOnBeam.distanceTo(target.position().add(0, target.getBbHeight() / 2, 0));
                if (distToBeam < target.getBbWidth() + 0.5) {
                    applyLaserDamage(player, target, level);
                }
            }
        }

        // --- Fire starting ---
        if (hitSolidBlock && player.getRandom().nextDouble() < getFireChance() / 4.0) {
            // Find the block face we hit and try to place fire adjacent
            BlockHitResult fireCheck = level.clip(new ClipContext(
                    eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (fireCheck.getType() == HitResult.Type.BLOCK) {
                BlockPos firePos = fireCheck.getBlockPos().relative(fireCheck.getDirection());
                if (level.isEmptyBlock(firePos)) {
                    BlockState fireState = BaseFireBlock.getState(level, firePos);
                    level.setBlock(firePos, fireState, 11);
                }
            }
        }

        // --- Impact particles ---
        if (hitSolidBlock) {
            level.sendParticles(ParticleTypes.FLAME,
                    hitPos.x, hitPos.y, hitPos.z, 3, 0.1, 0.1, 0.1, 0.02);
            level.sendParticles(ParticleTypes.SMOKE,
                    hitPos.x, hitPos.y, hitPos.z, 2, 0.1, 0.1, 0.1, 0.01);
        }

        // --- Visual mode ---
        if (Config.laserVisualMode == Config.LaserVisualMode.BEAM) {
            PacketHandler.sendToTrackingAndSelf(
                    new S2CLaserSyncPacket(player.getId(), hitPos.x, hitPos.y, hitPos.z, colorIndex),
                    player);
        } else {
            spawnBeamParticles(level, eyePos, hitPos, beamLength, colorIndex);
        }

        // --- Sound ---
        if (player.tickCount % 5 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.4F, 2.0F);
        }
    }

    /**
     * Applies laser damage with no knockback.
     * Shields block but melt. Fire resistance gives 70% reduction.
     * Uses playerAttack so armor applies and kills credit the player.
     */
    protected void applyLaserDamage(ServerPlayer player, LivingEntity target, ServerLevel level) {
        float damage = getLaserDamage();

        // --- Shield: blocks all damage, melts durability ---
        if (target.isBlocking()) {
            ItemStack shield = target.getUseItem();
            if (shield.getItem() instanceof ShieldItem) {
                shield.hurtAndBreak(3, target, (e) ->
                        e.broadcastBreakEvent(target.getUsedItemHand()));
                if (player.tickCount % 4 == 0) {
                    level.sendParticles(ParticleTypes.FLAME,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            4, 0.2, 0.2, 0.2, 0.03);
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3F, 1.5F);
                }
                return;
            }
        }

        // --- Fire resistance: 70% reduction ---
        if (target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            damage *= 0.3f;
        }

        // --- Apply damage with NO knockback ---
        // Save current motion, apply damage, then restore motion
        Vec3 motionBefore = target.getDeltaMovement();
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), damage);
        target.setDeltaMovement(motionBefore); // Cancel any knockback
        target.hurtMarked = true; // Sync the restored motion to client

        // Keep on fire
        target.setSecondsOnFire(2);

        // Throttled hit particles
        if (player.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 0.2, 0.2, 0.2, 0.05);
        }
        if (player.tickCount % 8 == 0) {
            level.sendParticles(ParticleTypes.LAVA,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    protected void spawnBeamParticles(ServerLevel level, Vec3 start, Vec3 end, double length, int colorIndex) {
        Vec3 dir = end.subtract(start).normalize();
        DustParticleOptions core = getCoreParticle(colorIndex);
        DustParticleOptions glow = getGlowParticle(colorIndex);
        double step = 0.5;
        for (double d = 1.0; d < length; d += step) {
            double x = start.x + dir.x * d;
            double y = start.y + dir.y * d;
            double z = start.z + dir.z * d;
            level.sendParticles(core, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            if (d % 1.0 < step) {
                level.sendParticles(glow, x, y, z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
    }
}
