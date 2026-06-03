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

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
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
     * Gets laser color for this player from persistent NBT.
     * Basic lasers: stored under "compound_v_laser_color", 1/200 rainbow chance on first roll.
     * Advanced lasers: stored under "compound_v_adv_laser_color", usually red but
     *   1/200 chance rainbow, 1/100 chance blue on first roll.
     * Can be modified by addon mods via NBT.
     */
    protected int getPlayerColorIndex(ServerPlayer player) {
        net.minecraft.nbt.CompoundTag data = player.getPersistentData();

        if (isAdvanced()) {
            String key = "compound_v_adv_laser_color";
            if (data.contains(key)) return data.getInt(key);
            int color = rollAdvancedLaserColor(player);
            data.putInt(key, color);
            return color;
        } else {
            String key = "compound_v_laser_color";
            if (data.contains(key)) return data.getInt(key);
            int color = rollPlayerLaserColor(player);
            data.putInt(key, color);
            return color;
        }
    }

    /**
     * Rolls a new laser color for basic laser eyes.
     * 1/200 rainbow, otherwise seeded 5-way split.
     */
    private int rollPlayerLaserColor(ServerPlayer player) {
        if (player.getRandom().nextInt(200) == 0) {
            return S2CLaserSyncPacket.COLOR_RAINBOW;
        }
        long uuidHash = player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits();
        long seed = player.serverLevel().getSeed();
        long hash = uuidHash * 6364136223846793005L + seed;
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

    /**
     * Rolls a new laser color for advanced laser eyes.
     * 1/200 rainbow, 1/100 blue, otherwise red.
     */
    private int rollAdvancedLaserColor(ServerPlayer player) {
        int roll = player.getRandom().nextInt(200);
        if (roll == 0) return S2CLaserSyncPacket.COLOR_RAINBOW;
        if (roll <= 2) return S2CLaserSyncPacket.COLOR_BLUE; // rolls 1 and 2 = 2/200 = 1/100
        return S2CLaserSyncPacket.COLOR_RED;
    }

    protected DustParticleOptions getCoreParticle(int colorIndex) {
        if (colorIndex == S2CLaserSyncPacket.COLOR_RAINBOW) {
            // Cycle through core particles for rainbow shimmer
            return switch ((int) (System.currentTimeMillis() / 100 % 5)) {
                case 0 -> LASER_CORE_ORANGE;
                case 1 -> LASER_CORE_BLUE;
                case 2 -> LASER_CORE_GREEN;
                case 3 -> LASER_CORE_PURPLE;
                case 4 -> LASER_CORE_YELLOW;
                default -> LASER_CORE_ORANGE;
            };
        }
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
                level.destroyBlock(hitBlockPos, Config.laserBlockBreakDrops);
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

        // --- Damage entities along beam (gated by tick rate config) ---
        int tickRate = isAdvanced() ? Config.laserAdvancedDamageTickRate : Config.laserBasicDamageTickRate;
        boolean isDamageTick = (player.tickCount % tickRate == 0);

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
                    if (isDamageTick) {
                        applyLaserDamage(player, target, level);
                    }
                }
            }
        }

        // --- Block breaking along beam path (if enabled) ---
        boolean breakBlocks = isAdvanced() ? Config.laserAdvancedBreakBlocks : Config.laserBasicBreakBlocks;
        if (breakBlocks && isDamageTick) {
            double breakChance = Config.laserBlockBreakChance;
            boolean drops = Config.laserBlockBreakDrops;
            double step = 1.0;
            // Extend slightly past beam end to include the hit block
            double breakRange = hitSolidBlock ? beamLength + 1.0 : beamLength;
            int steps = (int) (breakRange / step);
            for (int i = 1; i <= steps; i++) {
                Vec3 beamPoint = eyePos.add(lookVec.scale(i * step));
                BlockPos bPos = BlockPos.containing(beamPoint.x, beamPoint.y, beamPoint.z);
                BlockState bState = level.getBlockState(bPos);
                if (!bState.isAir()) {
                    float hardness = bState.getDestroySpeed(level, bPos);
                    if (hardness >= 0 && hardness < 50 && player.getRandom().nextDouble() < breakChance) {
                        level.destroyBlock(bPos, drops, player);
                        level.sendParticles(ParticleTypes.FLAME,
                                bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5,
                                2, 0.2, 0.2, 0.2, 0.02);
                    }
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
        boolean advanced = isAdvanced();
        boolean pushEnabled = advanced ? Config.laserAdvancedPushEnabled : Config.laserBasicPushEnabled;
        double pushStrength = advanced ? Config.laserAdvancedPushStrength : Config.laserBasicPushStrength;
        double shieldPushMult = advanced ? Config.laserAdvancedShieldPushMultiplier : Config.laserBasicShieldPushMultiplier;
        Vec3 pushDir = target.position().subtract(player.position()).normalize();
        pushDir = new Vec3(pushDir.x, 0, pushDir.z).normalize();

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
                if (pushEnabled && pushStrength > 0) {
                    Vec3 motion = target.getDeltaMovement();
                    target.setDeltaMovement(motion.add(pushDir.scale(pushStrength * shieldPushMult)));
                    target.hurtMarked = true;
                }
                return;
            }
        }

        if (target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            damage *= 0.3f;
        }

        Vec3 motionBefore = target.getDeltaMovement();
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), damage);
        target.setDeltaMovement(motionBefore);
        if (pushEnabled && pushStrength > 0) {
            target.setDeltaMovement(motionBefore.add(pushDir.scale(pushStrength)));
        }
        target.hurtMarked = true;

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
