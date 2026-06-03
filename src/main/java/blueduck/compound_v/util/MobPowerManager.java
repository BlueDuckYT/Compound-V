package blueduck.compound_v.util;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.ExplosiveEffect;
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

    // --- Mob berserker particles ---
    private static final DustParticleOptions MOB_RAGE_RED = new DustParticleOptions(
            new Vector3f(0.9f, 0.1f, 0.05f), 1.0f);

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

    // --- Mob chest blast tuning ---
    private static final int MOB_CHEST_BLAST_RANGE = 20;
    private static final float MOB_CHEST_BLAST_DAMAGE = 1.5f;
    private static final int MOB_CHEST_BLAST_CHARGEUP = 100;  // 5 seconds
    private static final int MOB_CHEST_BLAST_DURATION = 60;   // 3 seconds
    private static final int MOB_CHEST_BLAST_COOLDOWN = 400;  // 20 seconds
    private static final double MOB_CHEST_BLAST_TRIGGER_RANGE = 16.0;
    private static final Map<UUID, Integer> mobChestBlastTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> mobChestBlastDir = new ConcurrentHashMap<>(); // current beam direction
    private static final double MOB_CHEST_BLAST_TURN_SPEED = 3.0; // max degrees per tick the beam can rotate

    // --- Mob flight tuning ---
    private static final double MOB_FLIGHT_SPEED = 0.35;

    // --- Mob leap tuning ---
    private static final double MOB_LEAP_TRIGGER_RANGE = 8.0;  // leap when farther than this
    private static final int MOB_LEAP_COOLDOWN = 100;           // 5 seconds between leaps
    private static final Map<UUID, Long> mobLeapCooldown = new ConcurrentHashMap<>();

    // --- Mob explosive tuning ---
    private static final double MOB_EXPLOSIVE_TRIGGER_RANGE = 5.0;
    private static final int MOB_EXPLOSIVE_CHARGEUP = 60; // 3 seconds
    private static final int MOB_EXPLOSIVE_COOLDOWN = 400; // 20 seconds
    private static final Map<UUID, Integer> mobExplosiveTimer = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> mobExplosiveCooldown = new ConcurrentHashMap<>();
    private static final double MOB_FLIGHT_HOVER_HEIGHT = 3.0;
    private static final double MOB_FLIGHT_SNIPER_HEIGHT = 8.0;
    private static final double MOB_FLIGHT_TURRET_HEIGHT = 12.0; // float high as a turret
    private static final double MOB_FLIGHT_ORBIT_RADIUS = 6.0;
    private static final String FLIGHT_MODE_TAG = "compound_v_flight_mode"; // 0=dive, 1=sniper, 2=turret
    private static final String FLIGHT_TURRET_TIMER_TAG = "compound_v_turret_timer";
    private static final String FLIGHT_GROUND_UNTIL_TAG = "compound_v_ground_until"; // tick when flight can resume
    private static final int FLIGHT_GROUND_DURATION = 60; // 3 seconds on the ground before re-entering flight

    // --- Mob teleport tuning ---
    private static final int MOB_TELEPORT_CHANCE = 80; // 1 in N per tick (~every 4 sec avg)
    private static final double MOB_TELEPORT_TRIGGER_RANGE = 16.0;
    private static final double MOB_TELEPORT_MIN_RADIUS = 2.0;
    private static final double MOB_TELEPORT_MAX_RADIUS = 5.0;

    // --- NBT tag to mark mobs that have already been rolled ---
    private static final String CHECKED_TAG = "compound_v_checked";
    private static final String POWERED_TAG = "compound_v_powered";
    private static final String NATURAL_TAG = "compound_v_natural";
    private static final String LASER_COLOR_TAG = "compound_v_laser_color";

    // --- Mob size scaling ---
    private static final float MOB_ENLARGE_SCALE = 2.0f;   // smaller than player's 3.0 to avoid stuck issues
    private static final float MOB_SHRINK_SCALE = 0.35f;    // slightly bigger than player's 0.25 so they're visible

    // --- Weighted power entry (minAmp/maxAmp are amplifier values, so level = amp + 1) ---
    public record WeightedPower(RegistryObject<MobEffect> power, int weight, int minAmp, int maxAmp) {}

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
        mob.getPersistentData().putBoolean(NATURAL_TAG, true);

        // If the mob got laser eyes, roll a species-dependent laser color and store it
        if (chosen.power() == EffectReg.LASER_EYES_BASIC) {
            int color = rollLaserColor(mob);
            mob.getPersistentData().putInt(LASER_COLOR_TAG, color);
        }
        // Advanced laser eyes always get red (Homelander), except Endermen get purple
        if (mob.hasEffect(EffectReg.LASER_EYES_ADVANCED.get())) {
            if (mob instanceof EnderMan) {
                mob.getPersistentData().putInt(LASER_COLOR_TAG, S2CLaserSyncPacket.COLOR_PURPLE);
            } else {
                mob.getPersistentData().putInt(LASER_COLOR_TAG, S2CLaserSyncPacket.COLOR_RED);
            }
        }

        // Shrink mobs start small immediately (stealth — they grow when aggroed)
        if (mob.hasEffect(EffectReg.SHRINK.get()) && ModList.get().isLoaded("pehkui")) {
            PehkuiHelper.setScale(mob, MOB_SHRINK_SCALE);
        }

        // Flying mobs get a bonus laser eyes roll — 35% basic, 10% advanced, 55% none
        if (mob.hasEffect(EffectReg.CREATIVE_FLIGHT.get()) && !mob.hasEffect(EffectReg.LASER_EYES_BASIC.get())
                && !mob.hasEffect(EffectReg.LASER_EYES_ADVANCED.get())) {
            float laserRoll = mob.getRandom().nextFloat();
            RegistryObject<MobEffect> bonusLaser = null;
            if (laserRoll < 0.35f) {
                bonusLaser = EffectReg.LASER_EYES_BASIC;
            } else if (laserRoll < 0.45f) {
                bonusLaser = EffectReg.LASER_EYES_ADVANCED;
            }
            if (bonusLaser != null) {
                MobEffectInstance bonusEffect = new MobEffectInstance(
                        bonusLaser.get(), MobEffectInstance.INFINITE_DURATION, 0, false, false, false);
                bonusEffect.setCurativeItems(new java.util.ArrayList<>());
                mob.addEffect(bonusEffect);
                // Roll laser color
                if (bonusLaser == EffectReg.LASER_EYES_ADVANCED) {
                    mob.getPersistentData().putInt(LASER_COLOR_TAG,
                            mob instanceof EnderMan ? S2CLaserSyncPacket.COLOR_PURPLE : S2CLaserSyncPacket.COLOR_RED);
                } else {
                    mob.getPersistentData().putInt(LASER_COLOR_TAG, rollLaserColor(mob));
                }
            }
        }
    }

    /**
     * Rolls a laser beam color for a mob based on its species.
     * Colors: 0=Orange, 1=Blue, 2=Red, 3=Green
     * (Constants defined in S2CLaserSyncPacket)
     */
    public static int rollLaserColor(Mob mob) {
        // Rare rainbow chance for any mob (1/500)
        if (mob.getRandom().nextInt(500) == 0) {
            return S2CLaserSyncPacket.COLOR_RAINBOW;
        }

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
    /**
     * Builds the weighted power pool based on the entity's class hierarchy.
     * Works for hostile mobs, golems, wolves, and passive animals.
     * Public so the injection system can use the same pools.
     */
    public static List<WeightedPower> getEligiblePowers(LivingEntity mob) {
        List<WeightedPower> eligible = new ArrayList<>();

        // =============================================================
        //  GENERAL POOL — available to ALL mobs (any entity that can be injected)
        //  Species-specific pools below add more entries to bias the roll.
        // =============================================================
        eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 2, 0, 0));
        eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 3, 0, 0));
        eligible.add(new WeightedPower(EffectReg.INVISIBILITY, 1, 0, 0));
        eligible.add(new WeightedPower(EffectReg.PROJECTILE_IMMUNITY, 1, 0, 0));
        eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 1, 0, 2));
        eligible.add(new WeightedPower(EffectReg.TELEPORT, 1, 0, 0));
        if (Config.weightCreativeFlight > 0) {
            eligible.add(new WeightedPower(EffectReg.CREATIVE_FLIGHT, 1, 0, 0));
        }
        if (ModList.get().isLoaded("pehkui")) {
            eligible.add(new WeightedPower(EffectReg.SHRINK, 1, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENLARGE, 1, 0, 0));
        }
        eligible.add(new WeightedPower(EffectReg.LEAP, 2, 0, 0));
        eligible.add(new WeightedPower(EffectReg.HEALING, 1, 0, 0));

        // =============================================================
        //  SPECIES-SPECIFIC POOLS — add more weighted entries to bias the roll
        // =============================================================

        // === Iron Golem (neutral defender) — favor flight, lasers, tank ===
        if (mob instanceof net.minecraft.world.entity.animal.IronGolem) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 8, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_ADVANCED, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.CREATIVE_FLIGHT, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 5, 0, 0));
            eligible.add(new WeightedPower(EffectReg.INVINCIBLE, 2, 0, 0));
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.PROJECTILE_IMMUNITY, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.HEALING, 3, 0, 0)); // great support golem
            eligible.add(new WeightedPower(EffectReg.LEAP, 3, 0, 0));    // golem leap smash
            if (Config.weightChestBlast > 0) {
                eligible.add(new WeightedPower(EffectReg.CHEST_BLAST, 2, 0, 0));
            }
            if (ModList.get().isLoaded("pehkui")) {
                eligible.add(new WeightedPower(EffectReg.ENLARGE, 4, 0, 0));
            }
        }
        // === Snow Golem ===
        else if (mob instanceof net.minecraft.world.entity.animal.SnowGolem) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 6, 0, 0));
            eligible.add(new WeightedPower(EffectReg.CREATIVE_FLIGHT, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 5, 0, 0));
            eligible.add(new WeightedPower(EffectReg.PROJECTILE_IMMUNITY, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 2));
        }
        // === Wolf (loyal companion) — favor offensive ===
        else if (mob instanceof net.minecraft.world.entity.animal.Wolf) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 5, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_ADVANCED, 2, 0, 0));
            eligible.add(new WeightedPower(EffectReg.CREATIVE_FLIGHT, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 6, 0, 3));
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.INVINCIBLE, 1, 0, 0));
        }
        // === Passive animals (non-hostile, non-golem, non-wolf) — bias defensive ===
        else if (mob instanceof net.minecraft.world.entity.animal.Animal
                && !(mob instanceof net.minecraft.world.entity.monster.Enemy)) {
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 5, 0, 0));
            eligible.add(new WeightedPower(EffectReg.INVISIBILITY, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.PROJECTILE_IMMUNITY, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 2));
            if (ModList.get().isLoaded("pehkui")) {
                eligible.add(new WeightedPower(EffectReg.SHRINK, 4, 0, 0));
            }
        }

        // =============================================================
        //  HOSTILE MOB POOLS — add species-specific combat powers
        // =============================================================

        // === Zombie family (Drowned extends Zombie — check Drowned first) ===
        if (mob instanceof Drowned) {
            eligible.add(new WeightedPower(EffectReg.DEEP, 10, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 2, 0, 0));
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 2, 0, 0));
        } else if (mob instanceof Zombie) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 6, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_ADVANCED, 1, 0, 0)); // rare Homelander zombie
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 3));  // levels 1-4
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 4, 0, 0));
        }

        // === Skeleton family (Skeleton, Stray, WitherSkeleton) ===
        if (mob instanceof AbstractSkeleton) {
            eligible.add(new WeightedPower(EffectReg.ATOM_CHARGING, 8, 0, 2));  // levels 1-3
            eligible.add(new WeightedPower(EffectReg.TELEPORT, 4, 0, 0));
            eligible.add(new WeightedPower(EffectReg.PROJECTILE_IMMUNITY, 2, 0, 0)); // ironic — arrows bounce off them
            if (mob instanceof WitherSkeleton) {

                eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 1, 0, 0));
                eligible.add(new WeightedPower(EffectReg.BERSERKER, 3, 0, 0));
            }
        }

        // === Illager family (Pillager, Vindicator, Evoker, etc.) ===
        if (mob instanceof AbstractIllager) {
            eligible.add(new WeightedPower(EffectReg.ATOM_CHARGING, 8, 0, 2));  // levels 1-3
            eligible.add(new WeightedPower(EffectReg.SPEEDSTER, 3, 0, 3));      // levels 1-4
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 4, 0, 0));
        }

        // === Enderman ===
        if (mob instanceof EnderMan) {
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_BASIC, 8, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_ADVANCED, 2, 0, 0)); // rare — purple Homelander enderman
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
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 4, 0, 0));
        }

        // === Additional hostile-only options (on top of general pool) ===
        if (mob instanceof net.minecraft.world.entity.monster.Enemy) {
            eligible.add(new WeightedPower(EffectReg.ENHANCED_REGEN, 3, 0, 0));
            if (Config.weightInvincible > 0) {
                eligible.add(new WeightedPower(EffectReg.INVINCIBLE, 1, 0, 0));
            }
            eligible.add(new WeightedPower(EffectReg.BERSERKER, 3, 0, 0));
            eligible.add(new WeightedPower(EffectReg.LASER_EYES_ADVANCED, 1, 0, 0));
            eligible.add(new WeightedPower(EffectReg.MAGNETISM, 1, 0, 0));
            eligible.add(new WeightedPower(EffectReg.CREATIVE_FLIGHT, 2, 0, 0)); // +2 on top of general pool's 1
            eligible.add(new WeightedPower(EffectReg.LEAP, 3, 0, 0));            // common hostile leap
            eligible.add(new WeightedPower(EffectReg.EXPLOSIVE, 1, 0, 0));        // rare — mob survives its own blast
            if (Config.weightChestBlast > 0) {
                eligible.add(new WeightedPower(EffectReg.CHEST_BLAST, Config.mobChestBlastWeight, 0, 0));
            }
            if (ModList.get().isLoaded("pehkui")) {
                eligible.add(new WeightedPower(EffectReg.SHRINK, 2, 0, 0));
                eligible.add(new WeightedPower(EffectReg.ENLARGE, 2, 0, 0));
            }
        }

        // === Filter out disabled mob powers via config toggles ===
        return applyMobPowerFilter(eligible);
    }

    /**
     * Applies config-based power toggle filters to a power pool.
     */
    private static List<WeightedPower> applyMobPowerFilter(List<WeightedPower> eligible) {
        eligible.removeIf(wp -> {
            if (wp.power() == EffectReg.SPEEDSTER && !Config.mobPowerSpeedster) return true;
            if (wp.power() == EffectReg.DEEP && !Config.mobPowerDeep) return true;
            if (wp.power() == EffectReg.TELEPORT && !Config.mobPowerTeleport) return true;
            if (wp.power() == EffectReg.ATOM_CHARGING && !Config.mobPowerAtomCharging) return true;
            if (wp.power() == EffectReg.INVISIBILITY && !Config.mobPowerInvisibility) return true;
            if (wp.power() == EffectReg.INVINCIBLE && !Config.mobPowerInvincible) return true;
            if (wp.power() == EffectReg.LASER_EYES_BASIC && !Config.mobPowerLaserBasic) return true;
            if (wp.power() == EffectReg.LASER_EYES_ADVANCED && !Config.mobPowerLaserAdvanced) return true;
            if (wp.power() == EffectReg.ENHANCED_REGEN && !Config.mobPowerEnhancedRegen) return true;
            if (wp.power() == EffectReg.BERSERKER && !Config.mobPowerBerserker) return true;
            if (wp.power() == EffectReg.PROJECTILE_IMMUNITY && !Config.mobPowerProjectileImmunity) return true;
            if (wp.power() == EffectReg.MAGNETISM && !Config.mobPowerMagnetism) return true;
            if (wp.power() == EffectReg.SHRINK && !Config.mobPowerShrink) return true;
            if (wp.power() == EffectReg.ENLARGE && !Config.mobPowerEnlarge) return true;
            if (wp.power() == EffectReg.CREATIVE_FLIGHT && !Config.mobPowerCreativeFlight) return true;
            if (wp.power() == EffectReg.CHEST_BLAST && !Config.mobPowerChestBlast) return true;
            if (wp.power() == EffectReg.LEAP && !Config.mobPowerLeap) return true;
            if (wp.power() == EffectReg.EXPLOSIVE && !Config.mobPowerExplosive) return true;
            if (wp.power() == EffectReg.HEALING && !Config.mobPowerHealing) return true;
            return false;
        });
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
            // Restore gravity if this mob was flying
            if (mob.isNoGravity() && mob.hasEffect(EffectReg.CREATIVE_FLIGHT.get())) {
                mob.setNoGravity(false);
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
        // Petrified mobs can't use any powers
        if (mob.getPersistentData().getBoolean("compound_v_petrified")) return;
        // Virus / nullification field suppresses all powers
        if (CompoundVEffect.arePowersSuppressed(mob)) {
            if (mob.isNoGravity() && mob.hasEffect(EffectReg.CREATIVE_FLIGHT.get())) mob.setNoGravity(false);
            if (mob.hasEffect(EffectReg.SPEEDSTER.get())) {
                mob.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
                mob.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED);
            }
            if (mob.hasEffect(EffectReg.INVISIBILITY.get())) mob.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
            return;
        }

        LivingEntity target = mob.getTarget();

        // Laser Eyes: fire at target (basic or advanced)
        boolean hasBasicLaser = mob.hasEffect(EffectReg.LASER_EYES_BASIC.get());
        boolean hasAdvancedLaser = mob.hasEffect(EffectReg.LASER_EYES_ADVANCED.get());
        if (hasBasicLaser || hasAdvancedLaser) {
            if (target != null && target.isAlive()) {
                // Enderman special: only fire when aggroed AND target is NOT looking at them
                if (mob instanceof EnderMan) {
                    if (!isTargetLookingAtMob(target, mob)) {
                        tickMobLaser(mob, target, level, hasAdvancedLaser);
                    }
                } else {
                    tickMobLaser(mob, target, level, hasAdvancedLaser);
                }
            }
        }

        // Teleport: combat teleportation around target (hostile), or defensive escape (passive)
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

        // Berserker: red particle aura that intensifies as health drops
        if (mob.hasEffect(EffectReg.BERSERKER.get())) {
            float missingPercent = 1.0f - (mob.getHealth() / mob.getMaxHealth());
            if (missingPercent > 0.1f && mob.tickCount % 8 == 0) {
                int count = 1 + (int) (missingPercent * 6);
                level.sendParticles(MOB_RAGE_RED,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.7, mob.getZ(),
                        count, 0.25, 0.3, 0.25, 0.01);
            }
            if (missingPercent > 0.6f && mob.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.FLAME,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                        1, 0.2, 0.3, 0.2, 0.01);
            }
        }

        // Creative Flight: fly toward target
        if (mob.hasEffect(EffectReg.CREATIVE_FLIGHT.get())) {
            mob.fallDistance = 0; // prevent fall damage during any flight phase
            tickMobFlight(mob, target, level);
        }

        // Chest Blast: Soldier Boy mob AI
        if (mob.hasEffect(EffectReg.CHEST_BLAST.get())) {
            tickMobChestBlast(mob, target, level);
        }

        // Leap: jump toward target from afar
        if (mob.hasEffect(EffectReg.LEAP.get())) {
            mob.fallDistance = 0; // prevent fall damage accumulation
            tickMobLeap(mob, target, level);
        }

        // Explosive: charge and detonate near target
        if (mob.hasEffect(EffectReg.EXPLOSIVE.get())) {
            tickMobExplosive(mob, target, level);
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
    private static void tickMobLaser(Mob mob, LivingEntity target, ServerLevel level, boolean advanced) {
        // Burst/cooldown cycle: charge for ~1 sec, fire for ~3 sec, rest for ~6 sec
        UUID uuid = mob.getUUID();
        int prevTimer = mobLaserTimer.getOrDefault(uuid, -1);
        int timer = prevTimer + 1;
        // Advanced: longer burst, shorter cooldown
        int chargeUp = advanced ? 15 : MOB_LASER_CHARGEUP_TICKS;
        int burst = advanced ? 80 : MOB_LASER_BURST_TICKS;
        int cooldown = advanced ? 80 : MOB_LASER_COOLDOWN_TICKS;
        int range = advanced ? 24 : MOB_LASER_RANGE;
        float damage = advanced ? (float) Config.mobAdvancedLaserDamage : (float) Config.mobLaserDamage;
        int cycleLength = chargeUp + burst + cooldown;
        timer = timer % cycleLength;
        mobLaserTimer.put(uuid, timer);

        boolean inChargeUp = timer < chargeUp;
        boolean inBurst = timer >= chargeUp
                && timer < chargeUp + burst;

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
            int particleCount = 1 + (timer * 4) / chargeUp;
            if (mob.tickCount % 4 == 0) {
                level.sendParticles(glow,
                        mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(),
                        particleCount, 0.12, 0.08, 0.12, 0.015);
            }
            // Audible hum ramps up near the end
            if (timer == chargeUp - 5) {
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

        if (dist > range) return;

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
        target.hurt(mob.damageSources().mobAttack(mob), damage);
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

    /**
     * Defensive teleport for passive/neutral mobs when damaged.
     * Teleports 8-16 blocks away from the attacker. Called from ForgeEvents on hurt.
     * 50% chance per hit — not guaranteed, so the mob doesn't become unkillable.
     */
    public static void defensiveTeleport(Mob mob, LivingEntity attacker, ServerLevel level) {
        if (mob.getRandom().nextFloat() > 0.5f) return;

        Vec3 awayDir = mob.position().subtract(attacker.position()).normalize();
        for (int attempt = 0; attempt < 10; attempt++) {
            double dist = 8.0 + mob.getRandom().nextDouble() * 8.0;
            double angle = Math.atan2(awayDir.z, awayDir.x)
                    + (mob.getRandom().nextDouble() - 0.5) * Math.PI * 0.5;
            double tx = mob.getX() + Math.cos(angle) * dist;
            double tz = mob.getZ() + Math.sin(angle) * dist;
            int ty = (int) mob.getY();

            for (int dy = 4; dy >= -4; dy--) {
                BlockPos check = new BlockPos((int) tx, ty + dy, (int) tz);
                BlockPos below = check.below();
                if (level.getBlockState(below).isSolidRender(level, below)
                        && level.isEmptyBlock(check)
                        && level.isEmptyBlock(check.above())) {

                    level.sendParticles(ParticleTypes.PORTAL,
                            mob.getX(), mob.getY() + 1, mob.getZ(),
                            20, 0.3, 0.5, 0.3, 0.5);
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.6F, 1.4F);

                    mob.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);

                    level.sendParticles(ParticleTypes.PORTAL,
                            mob.getX(), mob.getY() + 1, mob.getZ(),
                            20, 0.3, 0.5, 0.3, 0.5);
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.6F, 1.0F);
                    return;
                }
            }
        }
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
     * Shrink: mob stays tiny by default (stealth). Two approach strategies:
     *
     * 1) Normal (50% chance): grows to full size immediately when aggroed.
     * 2) Stealth (50% chance): stays tiny and approaches at high speed,
     *    only popping to full size within 2 blocks of target for a surprise hit.
     *
     * At 20% health: shrinks back down, retreats. After recovering above 40%,
     * re-enters stealth approach mode for the next ambush.
     */
    private static final String SHRINK_STEALTH_TAG = "compound_v_shrink_stealth";
    private static final String SHRINK_RETREATING_TAG = "compound_v_shrink_retreating";
    private static final double STEALTH_POP_RANGE = 2.0;

    private static void tickMobShrink(Mob mob, LivingEntity target, ServerLevel level) {
        if (mob.tickCount % 10 != 0) return;

        float currentScale = PehkuiHelper.getTargetScale(mob);
        boolean aggroed = target != null && target.isAlive();
        float healthPercent = mob.getHealth() / mob.getMaxHealth();
        boolean retreating = mob.getPersistentData().getBoolean(SHRINK_RETREATING_TAG);

        // === Low health retreat ===
        if (healthPercent <= 0.2f) {
            if (!retreating) {
                // Initial retreat — shrink, flee, mark retreating
                mob.getPersistentData().putBoolean(SHRINK_RETREATING_TAG, true);
                if (currentScale > MOB_SHRINK_SCALE + 0.1f) {
                    PehkuiHelper.setScale(mob, MOB_SHRINK_SCALE);
                    level.sendParticles(ParticleTypes.POOF,
                            mob.getX(), mob.getY() + 0.5, mob.getZ(),
                            8, 0.3, 0.3, 0.3, 0.04);
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.PISTON_CONTRACT, SoundSource.HOSTILE, 0.4F, 1.8F);
                }
                if (target != null) {
                    Vec3 fleeDir = mob.position().subtract(target.position()).normalize();
                    mob.setDeltaMovement(fleeDir.x * 0.6, 0.3, fleeDir.z * 0.6);
                    mob.hurtMarked = true;
                }
            }
            mob.setTarget(null);
            return;
        }

        // === Recovery from retreat: above 40% health, re-enter stealth mode ===
        if (retreating && healthPercent > 0.4f) {
            mob.getPersistentData().putBoolean(SHRINK_RETREATING_TAG, false);
            mob.getPersistentData().putBoolean(SHRINK_STEALTH_TAG, true); // always stealth on re-ambush
        }

        // === Continue retreating until recovered ===
        if (retreating) {
            mob.setTarget(null);
            return;
        }

        // === Roll stealth mode on first aggro if not already set ===
        if (aggroed && !mob.getPersistentData().contains(SHRINK_STEALTH_TAG)) {
            mob.getPersistentData().putBoolean(SHRINK_STEALTH_TAG, mob.getRandom().nextBoolean());
        }

        boolean stealthMode = mob.getPersistentData().getBoolean(SHRINK_STEALTH_TAG);

        // === Stealth approach: stay small until within range ===
        if (aggroed && stealthMode && currentScale < 0.9f) {
            double dist = mob.distanceTo(target);
            if (dist <= STEALTH_POP_RANGE) {
                // Close enough — pop to full size for the ambush!
                PehkuiHelper.resetScale(mob);
                mob.getPersistentData().putBoolean(SHRINK_STEALTH_TAG, false);

                level.sendParticles(ParticleTypes.POOF,
                        mob.getX(), mob.getY() + 0.3, mob.getZ(),
                        12, 0.4, 0.3, 0.4, 0.05);
                level.sendParticles(POWER_SPARKLE,
                        mob.getX(), mob.getY() + 0.5, mob.getZ(),
                        6, 0.3, 0.3, 0.3, 0.02);
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE, 0.5F, 1.2F);
            }
            // Otherwise stay small — the speed boost makes them zip toward the target
            return;
        }

        // === Normal approach: grow immediately when aggroed ===
        if (aggroed && currentScale < 0.9f) {
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
            // Reset stealth tag so next aggro re-rolls
            mob.getPersistentData().remove(SHRINK_STEALTH_TAG);
            level.sendParticles(ParticleTypes.POOF,
                    mob.getX(), mob.getY() + 0.3, mob.getZ(),
                    5, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // =====================================================================
    //  MOB CREATIVE FLIGHT (experimental)
    // =====================================================================

    /**
     * Makes a mob with Creative Flight fly toward its target using direct velocity.
     * The mob hovers a few blocks above the target and dive-bombs when close.
     * When de-aggroed, gravity is restored and the mob lands.
     *
     * Key: we must stop the navigation system every tick or ground pathfinding
     * will override our velocity. We also force-sync movement to the client.
     *
     * Easy to de-implement: remove this method and the tick call in onMobTick,
     * plus the config options and pool entry. No other code depends on this.
     */
    private static void tickMobFlight(Mob mob, LivingEntity target, ServerLevel level) {
        boolean aggroed = target != null && target.isAlive();

        // Ground cooldown — mob stays grounded for a few seconds after a dive attack
        if (mob.getPersistentData().contains(FLIGHT_GROUND_UNTIL_TAG)) {
            long groundUntil = mob.getPersistentData().getLong(FLIGHT_GROUND_UNTIL_TAG);
            if (level.getGameTime() < groundUntil) {
                return; // still grounded, let normal AI handle things
            }
            mob.getPersistentData().remove(FLIGHT_GROUND_UNTIL_TAG);
        }

        if (aggroed) {
            mob.setNoGravity(true);
            mob.fallDistance = 0;
            mob.getNavigation().stop();

            boolean hasLasers = mob.hasEffect(EffectReg.LASER_EYES_BASIC.get())
                    || mob.hasEffect(EffectReg.LASER_EYES_ADVANCED.get());

            // Roll flight mode on first aggro
            if (!mob.getPersistentData().contains(FLIGHT_MODE_TAG)) {
                int mode;
                if (hasLasers) {
                    // 30% dive, 30% sniper orbit, 40% turret
                    float roll = mob.getRandom().nextFloat();
                    if (roll < 0.30f) mode = 0;
                    else if (roll < 0.60f) mode = 1;
                    else mode = 2;
                } else {
                    // No lasers: 50% dive immediately, 50% circle first for suspense
                    mode = mob.getRandom().nextFloat() < 0.50f ? 0 : 1;
                }
                mob.getPersistentData().putInt(FLIGHT_MODE_TAG, mode);
                mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, 0);
            }

            int flightMode = mob.getPersistentData().getInt(FLIGHT_MODE_TAG);
            int turretTimer = mob.getPersistentData().getInt(FLIGHT_TURRET_TIMER_TAG);

            Vec3 targetPos;

            if (flightMode == 2) {
                // === Turret mode: rise straight up above current position, hover, laser ===
                // Rise to turret height above the TARGET (not self) — tracks horizontally
                targetPos = target.position().add(0, MOB_FLIGHT_TURRET_HEIGHT, 0);

                turretTimer++;
                mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, turretTimer);

                // After 10 seconds (200 ticks) of turret mode, descend to dive/sniper
                if (turretTimer > 200) {
                    mob.getPersistentData().putInt(FLIGHT_MODE_TAG, mob.getRandom().nextFloat() < 0.5f ? 0 : 1);
                    mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, 0);
                }

                // Occasional mode switch (3% per second)
                if (mob.tickCount % 20 == 0 && mob.getRandom().nextFloat() < 0.03f) {
                    mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 0); // dive attack
                    mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, 0);
                }

            } else if (flightMode == 1) {
                // === Orbit mode: circle above the target ===
                double angle = (mob.tickCount * 0.02) % (Math.PI * 2);
                targetPos = target.position().add(
                        Math.cos(angle) * MOB_FLIGHT_ORBIT_RADIUS,
                        MOB_FLIGHT_SNIPER_HEIGHT,
                        Math.sin(angle) * MOB_FLIGHT_ORBIT_RADIUS);

                // Switch modes occasionally
                if (mob.tickCount % 20 == 0) {
                    if (hasLasers) {
                        // Laser mobs: slow transition out of orbit
                        float roll = mob.getRandom().nextFloat();
                        if (roll < 0.03f) {
                            mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 0); // dive
                        } else if (roll < 0.06f) {
                            mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 2); // turret
                            mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, 0);
                        }
                    } else {
                        // No lasers: circle for suspense, then dive more aggressively (15% per second)
                        if (mob.getRandom().nextFloat() < 0.15f) {
                            mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 0); // dive
                        }
                    }
                }

            } else {
                // === Dive mode: fly toward target for melee ===
                double dist = mob.position().distanceTo(target.position());

                if (dist < 3.0) {
                    // Close enough — exit flight, let normal melee AI take over
                    mob.setNoGravity(false);
                    mob.getPersistentData().remove(FLIGHT_MODE_TAG);
                    mob.getPersistentData().remove(FLIGHT_TURRET_TIMER_TAG);
                    mob.getPersistentData().putLong(FLIGHT_GROUND_UNTIL_TAG,
                            level.getGameTime() + FLIGHT_GROUND_DURATION);
                    return; // stop controlling movement this tick
                }

                targetPos = target.position().add(0, MOB_FLIGHT_HOVER_HEIGHT, 0);
                if (dist < 6.0) {
                    targetPos = target.position().add(0, 1.5, 0);
                }

                // Switch modes occasionally
                if (hasLasers && mob.tickCount % 20 == 0) {
                    float roll = mob.getRandom().nextFloat();
                    if (roll < 0.05f) {
                        mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 1); // sniper
                    } else if (roll < 0.10f) {
                        mob.getPersistentData().putInt(FLIGHT_MODE_TAG, 2); // turret
                        mob.getPersistentData().putInt(FLIGHT_TURRET_TIMER_TAG, 0);
                    }
                }
            }

            Vec3 dir = targetPos.subtract(mob.position());
            double length = dir.length();
            if (length > 0.1) {
                dir = dir.normalize();
                // Turret mode rises slower for dramatic effect
                double speed = flightMode == 2
                        ? Math.min(0.2, length * 0.15)
                        : Math.min(MOB_FLIGHT_SPEED, length * 0.3);
                mob.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
                mob.hurtMarked = true;
            } else if (flightMode == 2) {
                // Hovering in place — slight drift for visual interest
                mob.setDeltaMovement(0, Math.sin(mob.tickCount * 0.05) * 0.02, 0);
                mob.hurtMarked = true;
            }

            // Face the target
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
            mob.setYRot(yaw);
            mob.yHeadRot = yaw;
            mob.setYBodyRot(yaw);

            // Flight trail particles
            if (mob.tickCount % 5 == 0) {
                level.sendParticles(ParticleTypes.CLOUD,
                        mob.getX(), mob.getY(), mob.getZ(),
                        2, 0.2, 0.1, 0.2, 0.01);
            }
        } else {
            // De-aggroed — restore gravity, clear flight mode for re-roll
            if (mob.isNoGravity()) {
                mob.setNoGravity(false);
            }
            mob.getPersistentData().remove(FLIGHT_MODE_TAG);
            mob.getPersistentData().remove(FLIGHT_TURRET_TIMER_TAG);
            mob.getPersistentData().remove(FLIGHT_GROUND_UNTIL_TAG);
        }
    }

    // =====================================================================
    //  MOB CHEST BLAST (Soldier Boy mob)
    // =====================================================================

    // --- Chest blast particles ---
    private static final DustParticleOptions MOB_BLAST_CORE = new DustParticleOptions(
            new Vector3f(1.0f, 0.85f, 0.2f), 2.0f);
    private static final DustParticleOptions MOB_BLAST_CHARGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.7f, 0.1f), 0.8f);

    /**
     * Mob Chest Blast AI. When aggroed and target is in range:
     * - Charges for 5 seconds with escalating particles
     * - Fires a golden beam at the target for 3 seconds
     * - 20 second cooldown
     *
     * Compared to the player version: shorter range, less damage, no power stripping,
     * no block breaking. Still very dangerous — deals continuous beam damage.
     *
     * Easy to de-implement: remove this method, the tick call in onMobTick,
     * the pool entry, the tracking map, and the config options.
     */
    private static void tickMobChestBlast(Mob mob, LivingEntity target, ServerLevel level) {
        UUID uuid = mob.getUUID();
        int timer = mobChestBlastTimer.getOrDefault(uuid, -1);
        int cycleLength = MOB_CHEST_BLAST_CHARGEUP + MOB_CHEST_BLAST_DURATION + MOB_CHEST_BLAST_COOLDOWN;

        boolean aggroed = target != null && target.isAlive();

        // Only start a new cycle when aggroed and target in range with line of sight
        if (timer < 0) {
            if (!aggroed) return;
            double dist = mob.distanceTo(target);
            if (dist > MOB_CHEST_BLAST_TRIGGER_RANGE) return;
            if (!mob.hasLineOfSight(target)) return;
            timer = 0;
        }

        timer++;
        if (timer >= cycleLength) {
            timer = -1; // reset to idle
            mobChestBlastDir.remove(uuid);
        }
        mobChestBlastTimer.put(uuid, timer);
        if (timer < 0) return;

        boolean inChargeUp = timer <= MOB_CHEST_BLAST_CHARGEUP;
        boolean inBlast = timer > MOB_CHEST_BLAST_CHARGEUP
                && timer <= MOB_CHEST_BLAST_CHARGEUP + MOB_CHEST_BLAST_DURATION;

        double chestY = mob.getY() + mob.getBbHeight() * 0.6;

        // === Charge-up phase ===
        if (inChargeUp) {
            float chargePercent = (float) timer / MOB_CHEST_BLAST_CHARGEUP;

            // Freeze mob once charge is past 50% — planting feet for the blast
            if (chargePercent > 0.5f) {
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, mob.onGround() ? 0 : mob.getDeltaMovement().y, 0);
                mob.hurtMarked = true;
            }

            // Spiral particles inward
            if (mob.tickCount % 3 == 0) {
                int count = (int) (2 + chargePercent * 6);
                for (int i = 0; i < count; i++) {
                    double angle = (mob.tickCount * 0.3 + i * (Math.PI * 2.0 / count)) % (Math.PI * 2);
                    double radius = 1.2 * (1.0 - chargePercent * 0.5);
                    level.sendParticles(MOB_BLAST_CHARGE,
                            mob.getX() + Math.cos(angle) * radius,
                            chestY,
                            mob.getZ() + Math.sin(angle) * radius,
                            1, 0.05, 0.05, 0.05, 0.01);
                }
            }

            // Core glow
            if (chargePercent > 0.4 && mob.tickCount % 4 == 0) {
                level.sendParticles(MOB_BLAST_CORE,
                        mob.getX(), chestY, mob.getZ(),
                        (int) (chargePercent * 4), 0.1, 0.1, 0.1, 0.01);
            }

            // Sound escalation
            if (mob.tickCount % 20 == 0) {
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE,
                        0.4F + chargePercent * 0.8F, 0.5F + chargePercent * 0.6F);
            }

            // Flash on blast start
            if (timer == MOB_CHEST_BLAST_CHARGEUP) {
                level.sendParticles(ParticleTypes.FLASH,
                        mob.getX(), chestY, mob.getZ(),
                        3, 0, 0, 0, 0);
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5F, 0.4F);
            }
            return;
        }

        // === Blast phase — continues even if target dies ===
        if (inBlast) {
            // Freeze mob in place while blasting
            mob.getNavigation().stop();
            mob.setDeltaMovement(0, mob.onGround() ? 0 : mob.getDeltaMovement().y, 0);
            mob.hurtMarked = true;

            Vec3 mobChest = new Vec3(mob.getX(), chestY, mob.getZ());

            // Compute desired aim direction
            Vec3 desiredDir;
            if (aggroed) {
                Vec3 targetCenter = target.position().add(0, target.getBbHeight() * 0.5, 0);
                desiredDir = targetCenter.subtract(mobChest).normalize();
            } else {
                // Target dead — hold current beam direction
                float yawRad = (float) Math.toRadians(mob.getYRot());
                float pitchRad = (float) Math.toRadians(mob.getXRot());
                desiredDir = new Vec3(
                        -Math.sin(yawRad) * Math.cos(pitchRad),
                        -Math.sin(pitchRad),
                        Math.cos(yawRad) * Math.cos(pitchRad)).normalize();
            }

            // Limit turn speed — beam can only rotate a few degrees per tick
            Vec3 currentDir = mobChestBlastDir.get(uuid);
            if (currentDir == null) {
                currentDir = desiredDir; // first tick: snap to target
            }

            double angleBetween = Math.acos(Math.max(-1, Math.min(1, currentDir.dot(desiredDir))));
            double maxAngle = Math.toRadians(MOB_CHEST_BLAST_TURN_SPEED);

            Vec3 dir;
            if (angleBetween <= maxAngle || angleBetween < 0.001) {
                // Close enough or within turn budget — use desired direction
                dir = desiredDir;
            } else {
                // Interpolate toward desired direction at max turn speed
                double t = maxAngle / angleBetween;
                dir = new Vec3(
                        currentDir.x + (desiredDir.x - currentDir.x) * t,
                        currentDir.y + (desiredDir.y - currentDir.y) * t,
                        currentDir.z + (desiredDir.z - currentDir.z) * t
                ).normalize();
            }
            mobChestBlastDir.put(uuid, dir);

            // Apply aim inaccuracy — deviate up to configured degrees from perfect aim
            double inaccuracy = Config.mobChestBlastInaccuracy;
            if (inaccuracy > 0) {
                double inaccuracyRad = Math.toRadians(inaccuracy);
                // Random rotation around two perpendicular axes
                double yawOffset = (mob.getRandom().nextDouble() - 0.5) * 2.0 * inaccuracyRad;
                double pitchOffset = (mob.getRandom().nextDouble() - 0.5) * 2.0 * inaccuracyRad;
                // Rotate direction vector by yaw (around Y axis)
                double cosYaw = Math.cos(yawOffset);
                double sinYaw = Math.sin(yawOffset);
                double newX = dir.x * cosYaw - dir.z * sinYaw;
                double newZ = dir.x * sinYaw + dir.z * cosYaw;
                // Rotate by pitch (tilt up/down)
                double cosPitch = Math.cos(pitchOffset);
                double sinPitch = Math.sin(pitchOffset);
                double horizLen = Math.sqrt(newX * newX + newZ * newZ);
                double newY = dir.y * cosPitch - horizLen * sinPitch;
                dir = new Vec3(newX, newY, newZ).normalize();
            }

            // Raycast against blocks — beam stops at walls
            Vec3 beamEnd = mobChest.add(dir.scale(MOB_CHEST_BLAST_RANGE));
            BlockHitResult blockHit = level.clip(new ClipContext(
                    mobChest, beamEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
            Vec3 hitPos;
            double effectiveRange;
            if (blockHit.getType() != HitResult.Type.MISS) {
                hitPos = blockHit.getLocation();
                effectiveRange = mobChest.distanceTo(hitPos);
            } else {
                hitPos = beamEnd;
                effectiveRange = MOB_CHEST_BLAST_RANGE;
            }

            PacketHandler.sendToTrackingAndSelf(
                    new S2CLaserSyncPacket(mob.getId(), hitPos.x, hitPos.y, hitPos.z,
                            S2CLaserSyncPacket.COLOR_CHEST_BLAST),
                    mob);

            level.sendParticles(MOB_BLAST_CORE,
                    mob.getX(), chestY, mob.getZ(),
                    2, 0.15, 0.1, 0.15, 0.01);

            AABB searchBox = mob.getBoundingBox().inflate(effectiveRange);
            for (net.minecraft.world.entity.Entity e : level.getEntities(mob, searchBox,
                    ent -> ent instanceof LivingEntity && ent.isAlive() && ent != mob)) {
                LivingEntity beamTarget = (LivingEntity) e;
                Vec3 toTarget = beamTarget.position().add(0, beamTarget.getBbHeight() * 0.5, 0).subtract(mobChest);
                double dist = toTarget.length();
                if (dist > effectiveRange || dist < 0.5) continue;

                double dot = toTarget.normalize().dot(dir);
                double coneWidth = 0.8 / dist;
                double minDot = 1.0 - Math.min(coneWidth, 0.25);
                if (dot < minDot) continue;

                beamTarget.invulnerableTime = 0;
                float dmg = MOB_CHEST_BLAST_DAMAGE;
                if (beamTarget instanceof Player) dmg *= 0.5f;
                beamTarget.hurt(mob.damageSources().mobAttack(mob), dmg);

                // Optional power stripping
                if (Config.mobChestBlastStripsPowers) {
                    boolean hasCompV = false;
                    for (var inst : beamTarget.getActiveEffects()) {
                        if (inst.getEffect() instanceof CompoundVEffect) {
                            hasCompV = true;
                            break;
                        }
                    }
                    boolean shieldBlocking = Config.chestBlastShieldBlocksStrip && beamTarget.isBlocking();
                    if (hasCompV && !shieldBlocking) {
                        blueduck.compound_v.item.AntiVItem.stripCompoundVEffects(beamTarget);
                        // Knockback away from beam
                        Vec3 knockDir = toTarget.normalize();
                        beamTarget.setDeltaMovement(knockDir.x * 2.0, 0.5, knockDir.z * 2.0);
                        beamTarget.hurtMarked = true;
                        beamTarget.invulnerableTime = 30;
                        level.sendParticles(ParticleTypes.FLASH,
                                beamTarget.getX(), beamTarget.getY() + beamTarget.getBbHeight() * 0.5, beamTarget.getZ(),
                                2, 0, 0, 0, 0);
                        level.playSound(null, beamTarget.getX(), beamTarget.getY(), beamTarget.getZ(),
                                SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.0F, 0.5F);
                    } else if (hasCompV && shieldBlocking && beamTarget.tickCount % 10 == 0) {
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                beamTarget.getX(), beamTarget.getY() + beamTarget.getBbHeight() * 0.5, beamTarget.getZ(),
                                10, 0.3, 0.4, 0.3, 0.1);
                        level.playSound(null, beamTarget.getX(), beamTarget.getY(), beamTarget.getZ(),
                                SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 0.5F);
                    }
                }
            }

            // Sound
            if (mob.tickCount % 4 == 0) {
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 0.8F, 0.3F);
            }
        }
    }

    // =====================================================================
    //  MOB LEAP
    // =====================================================================

    /**
     * Mob leaps toward target when they're far away. 5 second cooldown.
     * Falls naturally — fall damage immunity handled by ForgeEvents.
     */
    private static void tickMobLeap(Mob mob, LivingEntity target, ServerLevel level) {
        if (target == null || !target.isAlive()) return;
        if (!mob.onGround()) return; // can only leap from ground

        double dist = mob.distanceTo(target);
        if (dist < MOB_LEAP_TRIGGER_RANGE) return; // close enough for melee

        long now = level.getGameTime();
        if (now < mobLeapCooldown.getOrDefault(mob.getUUID(), 0L)) return;

        // Calculate leap toward target
        Vec3 dir = target.position().subtract(mob.position()).normalize();
        double horizontalPower = Math.min(2.5, dist * 0.15);
        double verticalPower = 0.7 + Math.min(0.8, dist * 0.05);

        mob.setDeltaMovement(dir.x * horizontalPower, verticalPower, dir.z * horizontalPower);
        mob.hurtMarked = true;

        // Launch particles
        level.sendParticles(ParticleTypes.CLOUD,
                mob.getX(), mob.getY(), mob.getZ(),
                10, 0.5, 0.2, 0.5, 0.05);
        level.sendParticles(ParticleTypes.CRIT,
                mob.getX(), mob.getY(), mob.getZ(),
                15, 0.8, 0.1, 0.8, 0.1);
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.5F, 1.2F);

        mobLeapCooldown.put(mob.getUUID(), now + MOB_LEAP_COOLDOWN);
    }

    // =====================================================================
    //  MOB EXPLOSIVE
    // =====================================================================

    /**
     * Mob charges and detonates when close to target. Creeper-style warning.
     * Mob survives the explosion (unlike actual creepers).
     */
    private static void tickMobExplosive(Mob mob, LivingEntity target, ServerLevel level) {
        if (target == null || !target.isAlive()) {
            mobExplosiveTimer.remove(mob.getUUID());
            return;
        }

        double dist = mob.distanceTo(target);
        UUID uuid = mob.getUUID();
        long now = level.getGameTime();

        // Cooldown check
        if (now < mobExplosiveCooldown.getOrDefault(uuid, 0L)) return;

        // Only start charging when close
        if (dist > MOB_EXPLOSIVE_TRIGGER_RANGE) {
            mobExplosiveTimer.remove(uuid);
            return;
        }

        int timer = mobExplosiveTimer.getOrDefault(uuid, 0) + 1;
        mobExplosiveTimer.put(uuid, timer);

        float chargePercent = (float) timer / MOB_EXPLOSIVE_CHARGEUP;

        // Stop moving during charge
        if (chargePercent > 0.3f) {
            mob.getNavigation().stop();
            mob.setDeltaMovement(0, mob.onGround() ? 0 : mob.getDeltaMovement().y, 0);
        }

        // Creeper-style warning particles and sound
        if (timer % 5 == 0) {
            level.sendParticles(ParticleTypes.FLAME,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    (int) (2 + chargePercent * 8), 0.3, 0.3, 0.3, 0.02);
        }
        if (timer == 1 || timer % 20 == 0) {
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE,
                    0.5F + chargePercent * 0.5F, 0.8F + chargePercent * 0.4F);
        }
        if (chargePercent > 0.7f && timer % 3 == 0) {
            level.sendParticles(ParticleTypes.FLASH,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    1, 0, 0, 0, 0);
        }

        // Detonate
        if (timer >= MOB_EXPLOSIVE_CHARGEUP) {
            ExplosiveEffect.mobDetonate(mob, level);
            mobExplosiveTimer.remove(uuid);
            mobExplosiveCooldown.put(uuid, now + MOB_EXPLOSIVE_COOLDOWN);
        }
    }

    // =====================================================================
    //  CLEANUP
    // =====================================================================

    private static void cleanup(UUID uuid) {
        mobLastDamageTick.remove(uuid);
        mobLaserTimer.remove(uuid);
        mobChestBlastTimer.remove(uuid);
        mobChestBlastDir.remove(uuid);
        mobLeapCooldown.remove(uuid);
        mobExplosiveTimer.remove(uuid);
        mobExplosiveCooldown.remove(uuid);
    }
}
