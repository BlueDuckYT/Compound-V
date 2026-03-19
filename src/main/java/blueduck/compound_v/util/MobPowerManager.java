package blueduck.compound_v.util;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.keybinds.PacketHandler;
import blueduck.compound_v.registry.EffectReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages superpowered mob spawning and AI.
 *
 * On spawn, hostile mobs have a configurable chance to receive a Compound V
 * power from a class-based weighted pool. Powers are applied as infinite-
 * duration mob effects. Passive powers (Atom Charging, Speedster, Deep,
 * Invisibility, Invincible) work automatically through their effect ticks.
 * Active powers (Laser Eyes, Teleport, Enhanced Regen) are driven by custom
 * tick logic here.
 *
 * Class hierarchy is used via instanceof so modded entities that extend
 * vanilla classes (e.g. a mod's custom Zombie) are automatically compatible.
 *
 * All powered mobs emit blue sparkle particles as a visual indicator.
 */
public class MobPowerManager {

    // --- Visual indicators ---
    private static final DustParticleOptions POWER_SPARKLE = new DustParticleOptions(
            new Vector3f(0.2f, 0.5f, 1.0f), 0.8f);
    private static final DustParticleOptions POWER_SPARKLE_BRIGHT = new DustParticleOptions(
            new Vector3f(0.4f, 0.7f, 1.0f), 0.5f);

    // --- Mob laser particles (per-color variants for PARTICLE mode) ---
    private static final DustParticleOptions LASER_CORE_ORANGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.6f, 0.1f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_ORANGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.8f, 0.3f), 0.5f);
    private static final DustParticleOptions LASER_CORE_BLUE = new DustParticleOptions(
            new Vector3f(0.15f, 0.4f, 1.0f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_BLUE = new DustParticleOptions(
            new Vector3f(0.3f, 0.6f, 1.0f), 0.5f);
    private static final DustParticleOptions LASER_CORE_GREEN = new DustParticleOptions(
            new Vector3f(0.1f, 1.0f, 0.2f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_GREEN = new DustParticleOptions(
            new Vector3f(0.3f, 1.0f, 0.4f), 0.5f);
    private static final DustParticleOptions LASER_CORE_RED = new DustParticleOptions(
            new Vector3f(1.0f, 0.1f, 0.05f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_RED = new DustParticleOptions(
            new Vector3f(1.0f, 0.3f, 0.15f), 0.5f);
    private static final DustParticleOptions LASER_CORE_PURPLE = new DustParticleOptions(
            new Vector3f(0.6f, 0.15f, 1.0f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_PURPLE = new DustParticleOptions(
            new Vector3f(0.4f, 0.05f, 0.85f), 0.5f);
    private static final DustParticleOptions LASER_CORE_YELLOW = new DustParticleOptions(
            new Vector3f(1.0f, 0.9f, 0.15f), 1.0f);
    private static final DustParticleOptions LASER_GLOW_YELLOW = new DustParticleOptions(
            new Vector3f(0.9f, 0.75f, 0.02f), 0.5f);

    // --- Mob regen tracking ---
    private static final Map<UUID, Long> mobLastDamageTick = new ConcurrentHashMap<>();
    private static final int MOB_REGEN_DELAY_TICKS = 80;  // 4 seconds
    private static final float MOB_REGEN_AMOUNT = 0.5f;   // per heal tick

    // --- Mob laser tuning ---
    private static final int MOB_LASER_RANGE = 16;
    private static final float MOB_LASER_DAMAGE = 0.02f;  // per tick during burst (~0.8 dps)
    private static final int MOB_LASER_CHARGEUP_TICKS = 20; // ~1 second wind-up before burst
    private static final int MOB_LASER_BURST_TICKS = 60;   // ~3 seconds firing
    private static final int MOB_LASER_COOLDOWN_TICKS = 120; // ~6 seconds resting
    private static final Map<UUID, Integer> mobLaserTimer = new ConcurrentHashMap<>();

    // --- Mob teleport tuning ---
    private static final int MOB_TELEPORT_CHANCE = 80; // 1 in N per tick (~every 4 sec avg)
    private static final double MOB_TELEPORT_TRIGGER_RANGE = 16.0;
    private static final double MOB_TELEPORT_MIN_RADIUS = 2.0;
    private static final double MOB_TELEPORT_MAX_RADIUS = 5.0;

    // --- NBT tag to mark mobs that have already been rolled ---
    private static final String CHECKED_TAG = "compound_v_checked";
    private static final String POWERED_TAG = "compound_v_powered";
    private static final String LASER_COLOR_TAG = "compound_v_laser_color";

    // --- Mob size scaling ---
    private static final float MOB_ENLARGE_SCALE = 2.0f;   // smaller than player's 3.0 to avoid stuck issues
    private static final float MOB_SHRINK_SCALE = 0.35f;    // slightly bigger than player's 0.25 so they're visible

    // --- Weighted power entry (minAmp/maxAmp are amplifier values, so level = amp + 1) ---
    private record WeightedPower(RegistryObject<MobEffect> power, int weight, int minAmp, int maxAmp) {}

    // =====================================================================
    //  SPAWN LOGIC
    // =====================================================================

    /**
     * Called when a mob joins the world. Rolls for a power if not already checked.
     * Uses persistent data tags so chunk loading doesn't re-roll.
     */
    public static void onMobJoinLevel(Mob mob, ServerLevel level) {
        // Only process hostile mobs (Monster is the base class for hostiles)
        if (!(mob instanceof Monster)) return;

        // Creepers excluded: atom charging creates lingering explosion clouds that break balance
        if (mob instanceof Creeper) return;

        // Already checked this mob (loaded from save or previously rolled)
        if (mob.getPersistentData().contains(CHECKED_TAG)) return;
        mob.getPersistentData().putBoolean(CHECKED_TAG, true);

        // Roll spawn chance
        if (mob.getRandom().nextDouble() >= Config.mobPowerSpawnChance) return;

        // Build eligible power pool for this mob's class
        List<WeightedPower> eligible = getEligiblePowers(mob);
        if (eligible.isEmpty()) return;

        // Weighted random selection
        int totalWeight = 0;
        for (WeightedPower wp : eligible) {
            totalWeight += wp.weight();
        }
        int roll = mob.getRandom().nextInt(totalWeight);
        WeightedPower chosen = null;
        int cumulative = 0;
        for (WeightedPower wp : eligible) {
            cumulative += wp.weight();
            if (roll < cumulative) {
                chosen = wp;
                break;
            }
        }
        if (chosen == null) return;

        // Roll amplifier within the power's configured range
        int amp = chosen.minAmp();
        if (chosen.maxAmp() > chosen.minAmp()) {
            amp += mob.getRandom().nextInt(chosen.maxAmp() - chosen.minAmp() + 1);
        }

        // Apply infinite-duration effect (no particles, no icon — the blue sparkles replace those)
        MobEffectInstance effect = new MobEffectInstance(
                chosen.power().get(), MobEffectInstance.INFINITE_DURATION, amp, false, false, false);
        mob.addEffect(effect);
        mob.getPersistentData().putBoolean(POWERED_TAG, true);

        // If the mob got laser eyes, roll a species-dependent laser color and store it
        if (chosen.power() == EffectReg.LASER_EYES_BASIC) {
            int color = rollLaserColor(mob);
            mob.getPersistentData().putInt(LASER_COLOR_TAG, color);
        }

        // Shrink mobs start small immediately (stealth — they grow when aggroed)
        if (chosen.power() == EffectReg.SHRINK && ModList.get().isLoaded("pehkui")) {
            PehkuiHelper.setScale(mob, MOB_SHRINK_SCALE);
        }
    }

    /**
     * Rolls a laser beam color for a mob based on its species.
     * Colors: 0=Orange, 1=Blue, 2=Red, 3=Green
     * (Constants defined in S2CLaserSyncPacket)
     */
    private static int rollLaserColor(Mob mob) {
        float roll = mob.getRandom().nextFloat();

        // Enderman: always purple
        if (mob instanceof EnderMan) {
            return S2CLaserSyncPacket.COLOR_PURPLE;
        }

        // Husk: 60% yellow, 30% orange, 10% red
        if (mob instanceof Husk) {
            if (roll < 0.60f) return S2CLaserSyncPacket.COLOR_YELLOW;
            if (roll < 0.90f) return S2CLaserSyncPacket.COLOR_ORANGE;
            return S2CLaserSyncPacket.COLOR_RED;
        }

        // Zombie family (non-Husk): 2% rare (purple or yellow), then 44% green, 44% blue, 10% orange
        if (mob instanceof Zombie) {
            if (roll < 0.01f) return S2CLaserSyncPacket.COLOR_PURPLE;
            if (roll < 0.02f) return S2CLaserSyncPacket.COLOR_YELLOW;
            if (roll < 0.46f) return S2CLaserSyncPacket.COLOR_GREEN;
            if (roll < 0.90f) return S2CLaserSyncPacket.COLOR_BLUE;
            return S2CLaserSyncPacket.COLOR_ORANGE;
        }

        // Blaze: always orange
        if (mob instanceof Blaze) {
            return S2CLaserSyncPacket.COLOR_ORANGE;
        }

        // Wither Skeleton: 70% red, 30% orange
        if (mob instanceof WitherSkeleton) {
            return roll < 0.70f ? S2CLaserSyncPacket.COLOR_RED : S2CLaserSyncPacket.COLOR_ORANGE;
        }

        // Other skeletons: 50% blue, 30% green, 20% orange
        if (mob instanceof AbstractSkeleton) {
            if (roll < 0.50f) return S2CLaserSyncPacket.COLOR_BLUE;
            if (roll < 0.80f) return S2CLaserSyncPacket.COLOR_GREEN;
            return S2CLaserSyncPacket.COLOR_ORANGE;
        }

        // Default: 40% orange, 30% blue, 20% green, 5% purple, 5% red
        if (roll < 0.40f) return S2CLaserSyncPacket.COLOR_ORANGE;
        if (roll < 0.70f) return S2CLaserSyncPacket.COLOR_BLUE;
        if (roll < 0.90f) return S2CLaserSyncPacket.COLOR_GREEN;
        if (roll < 0.95f) return S2CLaserSyncPacket.COLOR_PURPLE;
        return S2CLaserSyncPacket.COLOR_RED;
    }

    /**
     * Builds the weighted power pool based on the mob's class hierarchy.
     * More specific classes are checked first (Drowned before Zombie) using
     * if/else chains where inheritance would cause overlap. Unrelated branches
     * use independent if-blocks so a mob can only match one branch per family.
     *
     * Modded mobs that extend these vanilla classes are automatically included.
     */
    private static List<WeightedPower> getEligiblePowers(LivingEntity mob) {
        List<WeightedPower> eligible = new ArrayList<>();

        // === Zombie family (Drowned extends Zombie — check Drowned first) ===
        if (mob instanceof Drowned) {
            eligible.add(new WeightedPower(EffectReg.DEEP, 10, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 2, 0, 0));
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 2, 0, 0));
        } else if (mob instanceof Zombie) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 6, 0, 0));
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 3));  // levels 1-4
        }

        // === Skeleton family (Skeleton, Stray, WitherSkeleton) ===
        if (mob instanceof AbstractSkeleton) {
            eligible.add(new WeightedPower(EffectReg.ATOM_CHARGING, 8, 0, 2));  // levels 1-3
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 4, 0, 0));
            if (mob instanceof WitherSkeleton) {

                eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 1, 0, 0));
            }
        }

        // === Illager family (Pillager, Vindicator, Evoker, etc.) ===
        if (mob instanceof AbstractIllager) {
            eligible.add(new WeightedPower(EffectReg.ATOM_CHARGING, 8, 0, 2));  // levels 1-3
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 3));      // levels 1-4
        }

        // === Enderman ===
        if (mob instanceof EnderMan) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 8, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 4, 0, 3));
        }

        // === Spider family (Spider, CaveSpider) ===
        if (mob instanceof Spider) {
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 8, 0, 4));      // levels 1-5
            eligible.add(new WeightedPower(EffectReg.INVISIBILITY, 3, 0, 0));
        }

        // === Phantom ===
        if (mob instanceof Phantom) {
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 10, 1, 4));     // levels 2-5
        }

        // === Witch ===
        if (mob instanceof Witch) {
            eligible.add(new WeightedPower(EffectReg.INVISIBILITY, 8, 0, 0));
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 3, 0, 0));
        }

        // === Blaze ===
        if (mob instanceof Blaze) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 10, 0, 0));
        }

        // === Guardian family (Guardian, ElderGuardian) ===
        if (mob instanceof Guardian) {
            eligible.add(new WeightedPower(EffectReg.DEEP, 10, 0, 0));
        }

        // === Piglin family (Piglin, PiglinBrute) ===
        if (mob instanceof AbstractPiglin) {
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 5, 0, 3));      // levels 1-4
            eligible.add(new WeightedPower(EffectReg.ATOM_CHARGING, 5, 0, 2));  // levels 1-3
        }

        // === Fallback: any Monster with no specific pairings gets regen ===
        if (eligible.isEmpty()) {
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 5, 0, 0));
        }

        // === Universal options for ALL monsters ===
        eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 4, 0, 0));
        eligible.add(new WeightedPower(EffectReg.INVISIBILITY, 2, 0, 0));  // very rare — blue sparkles still visible
        eligible.add(new WeightedPower(EffectReg.INVINCIBLE, 1, 0, 0));    // extremely rare

        // === Pehkui-dependent: Shrink and Enlarge for ALL monsters ===
        if (ModList.get().isLoaded("pehkui")) {
            eligible.add(new WeightedPower(EffectReg.SHRINK, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENLARGE, 3, 0, 0));
        }

        return eligible;
    }

    // =====================================================================
    //  TICK LOGIC
    // =====================================================================

    /**
     * Called every tick for every Mob on the server.
     * Handles blue sparkle particles and active power AI.
     */
    public static void onMobTick(Mob mob, ServerLevel level) {
        // Quick bail: only process powered mobs
        if (!mob.getPersistentData().getBoolean(POWERED_TAG)) return;
        if (!mob.isAlive()) {
            // Reset Pehkui scale if this mob had shrink/enlarge
            if (ModList.get().isLoaded("pehkui")
                    && (mob.hasEffect(EffectReg.SHRINK.get()) || mob.hasEffect(EffectReg.ENLARGE.get()))) {
                PehkuiHelper.resetScale(mob);
            }
            cleanup(mob.getUUID());
            return;
        }

        // --- Blue sparkle indicator (all powered mobs) ---
        if (mob.tickCount % 15 == 0) {
            level.sendParticles(POWER_SPARKLE,
                    mob.getX(), mob.getY() + mob.getBbHeight() + 0.2, mob.getZ(),
                    2, 0.25, 0.15, 0.25, 0.01);
            level.sendParticles(POWER_SPARKLE_BRIGHT,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    1, 0.15, 0.2, 0.15, 0.005);
        }

        // --- Active power AI ---
        LivingEntity target = mob.getTarget();

        // Laser Eyes: fire at target
        if (mob.hasEffect(EffectReg.LASER_EYES_BASIC.get())) {
            if (target != null && target.isAlive()) {
                // Enderman special: only fire when aggroed AND target is NOT looking at them
                if (mob instanceof EnderMan) {
                    if (!isTargetLookingAtMob(target, mob)) {
                        tickMobLaser(mob, target, level);
                    }
                } else {
                    tickMobLaser(mob, target, level);
                }
            }
        }

        // Teleport: combat teleportation around target
        if (mob.hasEffect(EffectReg.TELEPORT.get())) {
            if (target != null && target.isAlive()) {
                tickMobTeleport(mob, target, level);
            }
        }

        // Enhanced Regen: heal when not recently damaged
        if (mob.hasEffect(EffectReg.ENHANCED_REGEN.get())) {
            tickMobRegen(mob, level);
        }

        // Enlarge: grow when aggroed, shrink back when calm
        if (mob.hasEffect(EffectReg.ENLARGE.get()) && ModList.get().isLoaded("pehkui")) {
            tickMobEnlarge(mob, target, level);
        }

        // Shrink: small by default, grow to normal when aggroed (stealth attack)
        if (mob.hasEffect(EffectReg.SHRINK.get()) && ModList.get().isLoaded("pehkui")) {
            tickMobShrink(mob, target, level);
        }
    }

    // =====================================================================
    //  ENDERMAN EYE-CONTACT CHECK
    // =====================================================================

    /**
     * Checks if the target entity is looking at the mob (within ~15 degrees).
     * Used for Enderman laser: they only fire when the target looks away.
     */
    private static boolean isTargetLookingAtMob(LivingEntity target, Mob mob) {
        Vec3 lookVec = target.getViewVector(1.0F).normalize();
        Vec3 toMob = mob.getEyePosition().subtract(target.getEyePosition()).normalize();
        double dot = lookVec.dot(toMob);
        // cos(15°) ≈ 0.966 — if dot product exceeds this, target is staring at the mob
        return dot > 0.966;
    }

    // =====================================================================
    //  MOB LASER EYES
    // =====================================================================

    /**
     * Fires a laser beam from the mob toward its attack target.
     * Reduced damage and range compared to player version. No block
     * destruction or fire — just a damaging beam with particle trail.
     */
    private static void tickMobLaser(Mob mob, LivingEntity target, ServerLevel level) {
        // Burst/cooldown cycle: charge for ~1 sec, fire for ~3 sec, rest for ~6 sec
        UUID uuid = mob.getUUID();
        int prevTimer = mobLaserTimer.getOrDefault(uuid, -1);
        int timer = prevTimer + 1;
        int cycleLength = MOB_LASER_CHARGEUP_TICKS + MOB_LASER_BURST_TICKS + MOB_LASER_COOLDOWN_TICKS;
        timer = timer % cycleLength;
        mobLaserTimer.put(uuid, timer);

        boolean inChargeUp = timer < MOB_LASER_CHARGEUP_TICKS;
        boolean inBurst = timer >= MOB_LASER_CHARGEUP_TICKS
                && timer < MOB_LASER_CHARGEUP_TICKS + MOB_LASER_BURST_TICKS;

        // === Charge-up phase: escalating glow at eyes, no damage ===
        if (inChargeUp) {
            // Flash on charge start
            if (timer == 0) {
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.3F, 2.0F);
            }
            int colorIndex = mob.getPersistentData().getInt(LASER_COLOR_TAG);
            DustParticleOptions glow = getParticleForColor(colorIndex);
            // Particle count ramps up as charge progresses
            int particleCount = 1 + (timer * 4) / MOB_LASER_CHARGEUP_TICKS;
            if (mob.tickCount % 4 == 0) {
                level.sendParticles(glow,
                        mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(),
                        particleCount, 0.12, 0.08, 0.12, 0.015);
            }
            // Audible hum ramps up near the end
            if (timer == MOB_LASER_CHARGEUP_TICKS - 5) {
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.4F, 2.5F);
            }
            return;
        }

        // === Cooldown phase: subtle recharge glow ===
        if (!inBurst) {
            if (mob.tickCount % 10 == 0) {
                int colorIndex = mob.getPersistentData().getInt(LASER_COLOR_TAG);
                DustParticleOptions glow = getParticleForColor(colorIndex);
                level.sendParticles(glow,
                        mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(),
                        1, 0.08, 0.05, 0.08, 0.005);
            }
            return;
        }

        // === Burst phase: firing ===
        Vec3 eyePos = mob.getEyePosition(1.0F);
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        double dist = eyePos.distanceTo(targetCenter);

        if (dist > MOB_LASER_RANGE) return;

        // Line-of-sight check
        Vec3 dir = targetCenter.subtract(eyePos).normalize();
        Vec3 endPos = eyePos.add(dir.scale(dist));
        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mob));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            double blockDist = eyePos.distanceTo(blockHit.getLocation());
            if (blockDist < dist - 0.5) {
                // Blocked by terrain — beam hits wall instead
                endPos = blockHit.getLocation();
                level.sendParticles(ParticleTypes.FLAME,
                        endPos.x, endPos.y, endPos.z, 2, 0.1, 0.1, 0.1, 0.01);
                sendMobBeamVisual(mob, endPos, level);
                return;
            }
        }

        // Shield check: if target is blocking and facing the beam source, deflect
        if (target.isBlocking()) {
            Vec3 targetLook = target.getViewVector(1.0F).normalize();
            Vec3 toMob = mob.getEyePosition().subtract(target.getEyePosition()).normalize();
            double dot = targetLook.dot(toMob);
            // cos(60°) = 0.5 — shield blocks if target is roughly facing the mob
            if (dot > 0.5) {
                // Deflect — sparks off the shield, no damage
                Vec3 shieldPos = target.getEyePosition().add(targetLook.scale(0.5));
                level.sendParticles(ParticleTypes.CRIT,
                        shieldPos.x, shieldPos.y, shieldPos.z,
                        3, 0.15, 0.15, 0.15, 0.1);
                sendMobBeamVisual(mob, shieldPos, level);
                if (mob.tickCount % 15 == 0) {
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.5F);
                }
                return;
            }
        }

        // Damage the target (no knockback)
        Vec3 motionBefore = target.getDeltaMovement();
        target.invulnerableTime = 0;
        target.hurt(mob.damageSources().mobAttack(mob), MOB_LASER_DAMAGE);
        target.setDeltaMovement(motionBefore);
        target.hurtMarked = true;

        // Set briefly on fire
        target.setSecondsOnFire(1);

        // Beam visual
        sendMobBeamVisual(mob, targetCenter, level);

        // Hit sparks on target
        if (mob.tickCount % 12 == 0) {
            level.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    2, 0.15, 0.15, 0.15, 0.03);
        }

        // Sound (throttled)
        if (mob.tickCount % 20 == 0) {
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.3F, 2.0F);
        }
    }

    /**
     * Sends the beam visual for a mob laser, respecting the laserVisualMode config.
     * BEAM: sends S2CLaserSyncPacket (rendered by LaserBeamRenderer on clients).
     * PARTICLE: spawns server-side particle trail in the mob's laser color.
     */
    private static void sendMobBeamVisual(Mob mob, Vec3 hitPos, ServerLevel level) {
        int colorIndex = mob.getPersistentData().getInt(LASER_COLOR_TAG); // defaults to 0 (orange)
        if (Config.laserVisualMode == Config.LaserVisualMode.BEAM) {
            PacketHandler.sendToTrackingAndSelf(
                    new S2CLaserSyncPacket(mob.getId(), hitPos.x, hitPos.y, hitPos.z, colorIndex),
                    mob);
        } else {
            spawnMobBeamParticles(level, mob.getEyePosition(1.0F), hitPos, colorIndex);
        }
    }

    /**
     * Returns the glow particle for a given color index (used for charge-up/cooldown effects).
     */
    private static DustParticleOptions getParticleForColor(int colorIndex) {
        return switch (colorIndex) {
            case S2CLaserSyncPacket.COLOR_BLUE -> LASER_GLOW_BLUE;
            case S2CLaserSyncPacket.COLOR_RED -> LASER_GLOW_RED;
            case S2CLaserSyncPacket.COLOR_GREEN -> LASER_GLOW_GREEN;
            case S2CLaserSyncPacket.COLOR_PURPLE -> LASER_GLOW_PURPLE;
            case S2CLaserSyncPacket.COLOR_YELLOW -> LASER_GLOW_YELLOW;
            default -> LASER_GLOW_ORANGE;
        };
    }

    private static void spawnMobBeamParticles(ServerLevel level, Vec3 start, Vec3 end, int colorIndex) {
        DustParticleOptions core;
        DustParticleOptions glow;
        switch (colorIndex) {
            case S2CLaserSyncPacket.COLOR_BLUE:
                core = LASER_CORE_BLUE; glow = LASER_GLOW_BLUE; break;
            case S2CLaserSyncPacket.COLOR_RED:
                core = LASER_CORE_RED; glow = LASER_GLOW_RED; break;
            case S2CLaserSyncPacket.COLOR_GREEN:
                core = LASER_CORE_GREEN; glow = LASER_GLOW_GREEN; break;
            case S2CLaserSyncPacket.COLOR_PURPLE:
                core = LASER_CORE_PURPLE; glow = LASER_GLOW_PURPLE; break;
            case S2CLaserSyncPacket.COLOR_YELLOW:
                core = LASER_CORE_YELLOW; glow = LASER_GLOW_YELLOW; break;
            default:
                core = LASER_CORE_ORANGE; glow = LASER_GLOW_ORANGE; break;
        }
        Vec3 dir = end.subtract(start);
        double length = dir.length();
        dir = dir.normalize();
        for (double d = 0.5; d < length; d += 0.8) {
            double x = start.x + dir.x * d;
            double y = start.y + dir.y * d;
            double z = start.z + dir.z * d;
            level.sendParticles(core, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            if (d % 1.6 < 0.8) {
                level.sendParticles(glow, x, y, z, 1, 0.04, 0.04, 0.04, 0.0);
            }
        }
    }

    // =====================================================================
    //  MOB TELEPORT
    // =====================================================================

    /**
     * Randomly teleports the mob to a position near its target.
     * Creates an unpredictable combat encounter — the mob appears from
     * different angles, forcing the player to stay alert.
     */
    private static void tickMobTeleport(Mob mob, LivingEntity target, ServerLevel level) {
        // Randomized interval (~every 4 seconds on average)
        if (mob.getRandom().nextInt(MOB_TELEPORT_CHANCE) != 0) return;

        double dist = mob.distanceTo(target);
        if (dist > MOB_TELEPORT_TRIGGER_RANGE || dist < 1.5) return;

        // Try to find a valid landing spot near the target
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            double radius = MOB_TELEPORT_MIN_RADIUS
                    + mob.getRandom().nextDouble() * (MOB_TELEPORT_MAX_RADIUS - MOB_TELEPORT_MIN_RADIUS);
            double tx = target.getX() + Math.cos(angle) * radius;
            double tz = target.getZ() + Math.sin(angle) * radius;
            int ty = (int) target.getY();

            // Search for solid ground within ±4 blocks of target Y
            for (int dy = 4; dy >= -4; dy--) {
                BlockPos check = new BlockPos((int) tx, ty + dy, (int) tz);
                BlockPos below = check.below();
                if (level.getBlockState(below).isSolidRender(level, below)
                        && level.isEmptyBlock(check)
                        && level.isEmptyBlock(check.above())) {

                    // Departure effects
                    level.sendParticles(ParticleTypes.PORTAL,
                            mob.getX(), mob.getY() + 1, mob.getZ(),
                            20, 0.3, 0.5, 0.3, 0.5);
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 1.2F);

                    // Teleport
                    mob.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);

                    // Arrival effects
                    level.sendParticles(ParticleTypes.PORTAL,
                            mob.getX(), mob.getY() + 1, mob.getZ(),
                            20, 0.3, 0.5, 0.3, 0.5);
                    level.sendParticles(POWER_SPARKLE,
                            mob.getX(), mob.getY() + 1, mob.getZ(),
                            5, 0.2, 0.3, 0.2, 0.02);
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.6F, 0.9F);
                    return;
                }
            }
        }
        // Failed to find a spot — silently give up this tick
    }

    // =====================================================================
    //  MOB ENHANCED REGEN
    // =====================================================================

    /**
     * Heals the mob if it hasn't taken damage recently.
     * Shorter delay and lower heal rate than the player version.
     */
    private static void tickMobRegen(Mob mob, ServerLevel level) {
        if (mob.tickCount % 20 != 0) return; // Check once per second

        UUID uuid = mob.getUUID();
        long now = level.getGameTime();
        long lastHit = mobLastDamageTick.getOrDefault(uuid, 0L);

        if (now - lastHit < MOB_REGEN_DELAY_TICKS) return;
        if (mob.getHealth() >= mob.getMaxHealth()) return;

        mob.heal(MOB_REGEN_AMOUNT);

        // Subtle heart particle
        if (mob.tickCount % 40 == 0) {
            level.sendParticles(ParticleTypes.HEART,
                    mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(),
                    1, 0.2, 0.2, 0.2, 0.01);
        }
    }

    /**
     * Called from the damage event handler to track when powered mobs take damage.
     */
    public static void onMobDamaged(UUID mobUUID, long gameTime) {
        mobLastDamageTick.put(mobUUID, gameTime);
    }

    // =====================================================================
    //  MOB ENLARGE
    // =====================================================================

    /**
     * Enlarge: mob grows when aggroed, shrinks back when calm.
     * Checks for space before growing to avoid getting stuck in blocks.
     */
    private static void tickMobEnlarge(Mob mob, LivingEntity target, ServerLevel level) {
        if (mob.tickCount % 10 != 0) return; // check every half second

        float currentScale = PehkuiHelper.getTargetScale(mob);
        boolean aggroed = target != null && target.isAlive();

        if (aggroed && currentScale < MOB_ENLARGE_SCALE - 0.1f) {
            // Check for space: build enlarged AABB from feet upward (don't inflate into ground)
            float enlargedWidth = mob.getBbWidth() * MOB_ENLARGE_SCALE;
            float enlargedHeight = mob.getBbHeight() * MOB_ENLARGE_SCALE;
            double halfW = enlargedWidth * 0.5;
            AABB enlarged = new AABB(
                    mob.getX() - halfW, mob.getY(), mob.getZ() - halfW,
                    mob.getX() + halfW, mob.getY() + enlargedHeight, mob.getZ() + halfW);
            if (level.noCollision(mob, enlarged)) {
                PehkuiHelper.setScale(mob, MOB_ENLARGE_SCALE);
                // Growth particles
                level.sendParticles(ParticleTypes.CLOUD,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                        10, 0.5, 0.5, 0.5, 0.05);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        mob.getX(), mob.getY(), mob.getZ(),
                        5, 0.4, 0.2, 0.4, 0.01);
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 0.5F, 0.5F);
            }
        } else if (!aggroed && currentScale > 1.1f) {
            PehkuiHelper.resetScale(mob);
            level.sendParticles(ParticleTypes.CLOUD,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    6, 0.4, 0.4, 0.4, 0.03);
        }
    }

    // =====================================================================
    //  MOB SHRINK
    // =====================================================================

    /**
     * Shrink: mob stays tiny by default (stealth). Grows to normal size when
     * aggroed — the sudden pop to full size is the surprise attack.
     */
    private static void tickMobShrink(Mob mob, LivingEntity target, ServerLevel level) {
        if (mob.tickCount % 10 != 0) return;

        float currentScale = PehkuiHelper.getTargetScale(mob);
        boolean aggroed = target != null && target.isAlive();

        if (aggroed && currentScale < 0.9f) {
            // Pop to full size — surprise!
            PehkuiHelper.resetScale(mob);
            level.sendParticles(ParticleTypes.POOF,
                    mob.getX(), mob.getY() + 0.3, mob.getZ(),
                    12, 0.4, 0.3, 0.4, 0.05);
            level.sendParticles(POWER_SPARKLE,
                    mob.getX(), mob.getY() + 0.5, mob.getZ(),
                    6, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE, 0.5F, 1.2F);
        } else if (!aggroed && currentScale > MOB_SHRINK_SCALE + 0.1f) {
            // Shrink back down when calm
            PehkuiHelper.setScale(mob, MOB_SHRINK_SCALE);
            level.sendParticles(ParticleTypes.POOF,
                    mob.getX(), mob.getY() + 0.3, mob.getZ(),
                    5, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // =====================================================================
    //  CLEANUP
    // =====================================================================

    private static void cleanup(UUID uuid) {
        mobLastDamageTick.remove(uuid);
        mobLaserTimer.remove(uuid);
    }
}
