package blueduck.compound_v;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // --- Loot ---
    private static final ForgeConfigSpec.BooleanValue ADD_TEMP_V_TO_BURIED_TREASURE = BUILDER
            .comment("Whether to add Temp V to Buried Treasure Chests")
            .define("add_to_buried_treasure", true);
    private static final ForgeConfigSpec.BooleanValue ADD_COMPOUND_V_TO_ANCIENT_CITIES = BUILDER
            .comment("Whether to add Compound V to Ancient City Chests")
            .define("add_v_to_ancient_cities", true);
    private static final ForgeConfigSpec.BooleanValue ADD_TEMP_V_TO_ANCIENT_CITIES = BUILDER
            .comment("Whether to add Temp V to Ancient City Chests")
            .define("add_temp_v_to_ancient_cities", true);
    private static final ForgeConfigSpec.BooleanValue ADD_TEMP_V_TO_BASTIONS = BUILDER
            .comment("Whether to add Temp V to Piglin Bastion Chests")
            .define("add_to_bastions", true);
    private static final ForgeConfigSpec.BooleanValue ADD_COMPOUND_V_TO_END_CITIES = BUILDER
            .comment("Whether to add Compound V and Temp V to End Cities")
            .define("add_to_end_cities", true);
    private static final ForgeConfigSpec.BooleanValue ADD_V1_TO_ANCIENT_CITIES = BUILDER
            .comment("Whether to add V1 (original formula) to Ancient City Chests")
            .define("add_v1_to_ancient_cities", true);
    private static final ForgeConfigSpec.BooleanValue ADD_V1_TO_END_CITIES = BUILDER
            .comment("Whether to add V1 (original formula) to End City Chests")
            .define("add_v1_to_end_cities", true);
    private static final ForgeConfigSpec.BooleanValue TEMP_V_FROM_WANDERING_TRADER = BUILDER
            .comment("Whether the Wandering Trader can rarely sell Temp V")
            .define("temp_v_from_trader", true);

    // --- General ---
    private static final ForgeConfigSpec.IntValue TEMP_V_DURATION = BUILDER
            .comment("Duration (in ticks) of Temp V's effects")
            .defineInRange("tempVDuration", 24000, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_BAD_EFFECT_CHANCE = BUILDER
            .comment("Chance of getting a bad outcome when taking Compound V (Setting this to 0 will disable it)")
            .defineInRange("badReactionChance", 0.1, 0, 1);
    private static final ForgeConfigSpec.DoubleValue TEMP_V_BAD_REACTION_CHANCE = BUILDER
            .comment("Chance of getting a bad outcome when taking Temp V (0 = disabled, 0.1 = 10%)")
            .defineInRange("tempVBadReactionChance", 0.0, 0, 1);

    // --- Combat ---
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken while on Compound V")
            .defineInRange("damageReduction", 0.75, 0, 1);
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_STRENGTH_MULTIPLIER = BUILDER
            .comment("Damage multiplier of damage dealt while on Compound V")
            .defineInRange("strengthMultiplier", 1.2, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_KNOCKBACK_REDUCTION = BUILDER
            .comment("Knockback multiplier of knockback taken while on Compound V")
            .defineInRange("knockbackReduction", 0.8, 0, 1);

    // --- Effect Weights ---
    private static final ForgeConfigSpec.IntValue WEIGHT_GENERIC = BUILDER
            .comment("Weight of obtaining Generic effect (No extra abilities) when taking Compound V")
            .defineInRange("weight_generic", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SPEEDSTER = BUILDER
            .comment("Weight of obtaining Speedster powers when taking Compound V")
            .defineInRange("weight_speedster", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue SPEEDSTER_SPEED_ATTACK = BUILDER
            .comment("Whether Speedster powers allow you to damage mobs while sprinting")
            .define("speedster_speed_attack", true);
    private static final ForgeConfigSpec.IntValue WEIGHT_WATER = BUILDER
            .comment("Weight of obtaining Water Powers when taking Compound V")
            .defineInRange("weight_water_power", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_TELEPORT = BUILDER
            .comment("Weight of obtaining Teleportation power when taking Compound V")
            .defineInRange("weight_teleportation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue TELEPORT_RANGE = BUILDER
            .comment("Range of the teleportation power")
            .defineInRange("teleport_range", 36, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ATOM_CHARGING = BUILDER
            .comment("Weight of obtaining Atom Charging power when taking Compound V")
            .defineInRange("weight_atom_charging", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVISIBILITY = BUILDER
            .comment("Weight of obtaining Invisibility when taking Compound V")
            .defineInRange("weight_invisibility", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_NIGHT_VISION = BUILDER
            .comment("Weight of obtaining Night Vision when taking Compound V")
            .defineInRange("weight_night_vision", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LEVITATION = BUILDER
            .comment("Weight of obtaining Levitation-based flight when taking Compound V")
            .defineInRange("weight_levitation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVINCIBLE = BUILDER
            .comment("Weight of obtaining Invincibility when taking Compound V")
            .defineInRange("weight_invincible", 1, 0, Integer.MAX_VALUE);

    // --- New Powers ---
    private static final ForgeConfigSpec.IntValue WEIGHT_CREATIVE_FLIGHT = BUILDER
            .comment("Weight of obtaining Creative Flight when taking Compound V")
            .defineInRange("weight_creative_flight", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LASER_EYES_BASIC = BUILDER
            .comment("Weight of obtaining Basic Laser Eyes when taking Compound V")
            .defineInRange("weight_laser_eyes_basic", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LASER_EYES_ADVANCED = BUILDER
            .comment("Weight of obtaining Advanced (Homelander) Laser Eyes when taking Compound V")
            .defineInRange("weight_laser_eyes_advanced", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SHRINK = BUILDER
            .comment("Weight of obtaining Shrink powers when taking Compound V (requires Pehkui)")
            .defineInRange("weight_shrink", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ENLARGE = BUILDER
            .comment("Weight of obtaining Enlarge powers when taking Compound V (requires Pehkui)")
            .defineInRange("weight_enlarge", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_POWER_ABSORPTION = BUILDER
            .comment("Weight of obtaining Power Absorption (Powerplex) when taking Compound V")
            .defineInRange("weight_power_absorption", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SONIC_SCREAM = BUILDER
            .comment("Weight of obtaining Sonic Scream when taking Compound V")
            .defineInRange("weight_sonic_scream", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_HEAD_POP = BUILDER
            .comment("Weight of obtaining Head Pop (Blood Manipulation) when taking Compound V")
            .defineInRange("weight_head_pop", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ENHANCED_REGEN = BUILDER
            .comment("Weight of obtaining Enhanced Regeneration when taking Compound V")
            .defineInRange("weight_enhanced_regen", 8, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_DENSITY = BUILDER
            .comment("Weight of obtaining Density Manipulation when taking Compound V")
            .defineInRange("weight_density", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue DENSITY_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken while on Compound V")
            .defineInRange("damageReduction", 0.5, 0, 1);
    private static final ForgeConfigSpec.IntValue WEIGHT_SPIDER = BUILDER
            .comment("Weight of obtaining Spider powers when taking Compound V (Currently Disabled)")
            .defineInRange("weight_spider", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INSTAKILL = BUILDER
            .comment("Weight of obtaining Instakill power when taking Compound V")
            .defineInRange("weight_instakill", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_MIND_CONTROL = BUILDER
            .comment("Weight of obtaining Mind Control power when taking Compound V (experimental, currently disabled)")
            .defineInRange("weight_mind_control", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_BERSERKER = BUILDER
            .comment("Weight of obtaining Berserker power when taking Compound V")
            .defineInRange("weight_berserker", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_PROJECTILE_IMMUNITY = BUILDER
            .comment("Weight of obtaining Projectile Immunity (Rubber Body) when taking Compound V")
            .defineInRange("weight_projectile_immunity", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_RADIOACTIVE = BUILDER
            .comment("Weight of obtaining Radioactive power when taking Compound V (requires Alex's Caves)")
            .defineInRange("weight_radioactive", 4, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_STAR_POWER = BUILDER
            .comment("Weight of obtaining Star Power when taking Compound V (experimental)")
            .defineInRange("weight_star_power", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue STAR_POWER_UNLIMITED = BUILDER
            .comment("If true, Star Power has no cooldown and can be toggled on/off at will")
            .define("starPowerUnlimited", false);
    private static final ForgeConfigSpec.IntValue WEIGHT_CHEST_BLAST = BUILDER
            .comment("Weight of obtaining Chest Blast (Soldier Boy) power from V1")
            .defineInRange("weight_chest_blast", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BEAM_DAMAGE = BUILDER
            .comment("Damage per tick of the Chest Blast beam (fires 20x/sec, so 2.0 = 40 dps before armor)")
            .defineInRange("chestBlastBeamDamage", 3.0, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BURST_DAMAGE = BUILDER
            .comment("Maximum damage of the initial Chest Blast explosion at point blank (falls off with distance)")
            .defineInRange("chestBlastBurstDamage", 8.0, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_DURATION = BUILDER
            .comment("Duration (in ticks) of the Chest Blast beam (80 = 4 seconds)")
            .defineInRange("chestBlastDuration", 80, 20, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_IN_REGULAR_POOL = BUILDER
            .comment("Whether Chest Blast can be obtained from regular Compound V (not just V1)")
            .define("chestBlastInRegularPool", false);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_RANGE = BUILDER
            .comment("Range (in blocks) of the Chest Blast beam")
            .defineInRange("chestBlastRange", 32, 8, 256);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Chance per tick per block of the Chest Blast beam destroying blocks in its path (0 = disabled)")
            .defineInRange("chestBlastBlockBreakChance", 0.15, 0.0, 1.0);

    // --- Mob Combat Stats ---
    private static final ForgeConfigSpec.DoubleValue MOB_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken by mobs with Compound V (1.0 = normal, 0.5 = half)")
            .defineInRange("mobDamageReduction", 1.0, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue MOB_STRENGTH_MULTIPLIER = BUILDER
            .comment("Damage multiplier of damage dealt by mobs with Compound V (1.0 = normal, 2.0 = double)")
            .defineInRange("mobStrengthMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MOB_KNOCKBACK_REDUCTION = BUILDER
            .comment("Knockback multiplier for mobs with Compound V (1.0 = normal, 0.25 = quarter)")
            .defineInRange("mobKnockbackReduction", 1.0, 0.0, 1.0);

    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_DAMAGE = BUILDER
            .comment("Damage per tick for Basic Laser Eyes (fires 20x/sec, so 0.15 = 3 dps before armor)")
            .defineInRange("laserBasicDamage", 0.15, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_DAMAGE = BUILDER
            .comment("Damage per tick for Advanced Laser Eyes (fires 20x/sec, so 0.35 = 7 dps before armor)")
            .defineInRange("laserAdvancedDamage", 0.35, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue LASER_BASIC_RANGE = BUILDER
            .comment("Range (in blocks) for Basic Laser Eyes")
            .defineInRange("laserBasicRange", 40, 1, 256);
    private static final ForgeConfigSpec.IntValue LASER_ADVANCED_RANGE = BUILDER
            .comment("Range (in blocks) for Advanced Laser Eyes")
            .defineInRange("laserAdvancedRange", 80, 1, 256);
    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_FIRE_CHANCE = BUILDER
            .comment("Chance per tick of basic laser setting fire to blocks (divided by 4 internally)")
            .defineInRange("laserBasicFireChance", 0.03, 0, 1);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_FIRE_CHANCE = BUILDER
            .comment("Chance per tick of advanced laser setting fire to blocks (divided by 4 internally)")
            .defineInRange("laserAdvancedFireChance", 0.12, 0, 1);
    private static final ForgeConfigSpec.DoubleValue SHRINK_SCALE = BUILDER
            .comment("Scale factor when using Shrink power (0.25 = 25% of normal size)")
            .defineInRange("shrinkScale", 0.25, 0.01, 1.0);

    private static final ForgeConfigSpec.EnumValue<LaserVisualMode> LASER_VISUAL_MODE = BUILDER
            .comment("Visual mode for laser eyes: BEAM (rendered glowing beams) or PARTICLE (particle trail)")
            .defineEnum("laserVisualMode", LaserVisualMode.BEAM);

    // --- Mob Powers ---
    private static final ForgeConfigSpec.BooleanValue ENABLE_MOB_POWERS = BUILDER
            .comment("Whether hostile mobs can spawn with Compound V powers (disabled by default)")
            .define("enableMobPowers", false);
    private static final ForgeConfigSpec.BooleanValue PERSIST_POWERS_ON_DEATH = BUILDER
            .comment("Whether Compound V powers persist through death (disabled by default)")
            .define("persistPowersOnDeath", false);
    private static final ForgeConfigSpec.DoubleValue MOB_POWER_SPAWN_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a hostile mob spawns with a power when mob powers are enabled")
            .defineInRange("mobPowerSpawnChance", 0.05, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue MOB_COMPOUND_V_DROP_CHANCE = BUILDER
            .comment("Chance for a powered mob to drop Compound V on death (0 = disabled, for challenge modes)")
            .defineInRange("mobCompoundVDropChance", 0.0, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue MOB_TEMP_V_DROP_CHANCE = BUILDER
            .comment("Chance for a powered mob to drop Temp V on death (0 = disabled, only drops if Compound V didn't)")
            .defineInRange("mobTempVDropChance", 0.0, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue MOB_V1_DROP_CHANCE = BUILDER
            .comment("Chance for a powered mob to drop V1 on death (0 = disabled, takes priority over Compound V and Temp V)")
            .defineInRange("mobV1DropChance", 0.0, 0.0, 1.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public enum LaserVisualMode {
        BEAM,
        PARTICLE
    }

    // --- Public fields ---
    public static boolean addToBuriedTreasure;
    public static boolean addVToAncientCities;
    public static boolean addTempVToAncientCities;
    public static boolean addToBastions;
    public static boolean addToEndCities;
    public static boolean addV1ToAncientCities;
    public static boolean addV1ToEndCities;
    public static boolean tempVFromTrader;
    public static int tempVDuration;
    public static double badOutcomeChance;
    public static double tempVBadOutcomeChance;
    public static double damageReduction;
    public static double strengthMultiplier;
    public static double knockbackReduction;
    public static int weightGeneric;
    public static int weightSpeedster;
    public static boolean speedsterMobAttack;
    public static int weightWater;
    public static int weightTeleport;
    public static int teleportRange;
    public static int weightAtomCharging;
    public static int weightInvisibility;
    public static int weightNightVision;
    public static int weightLevitation;
    public static int weightInvincible;
    public static int weightCreativeFlight;
    public static int weightLaserEyesBasic;
    public static int weightLaserEyesAdvanced;
    public static int weightShrink;
    public static int weightEnlarge;
    public static int weightPowerAbsorption;
    public static int weightSonicScream;
    public static int weightHeadPop;
    public static int weightEnhancedRegen;
    public static int weightDensity;
    public static double densityDamageMultiplier;
    public static int weightSpider;
    public static int weightInstakill;
    public static int weightMindControl;
    public static int weightBerserker;
    public static int weightProjectileImmunity;
    public static int weightRadioactive;
    public static int weightStarPower;
    public static boolean starPowerUnlimited;
    public static int weightChestBlast;
    public static double chestBlastBeamDamage;
    public static double chestBlastBurstDamage;
    public static int chestBlastDuration;
    public static boolean chestBlastInRegularPool;
    public static int chestBlastRange;
    public static double chestBlastBlockBreakChance;
    public static double mobDamageReduction;
    public static double mobStrengthMultiplier;
    public static double mobKnockbackReduction;
    public static double laserBasicDamage;
    public static double laserAdvancedDamage;
    public static int laserBasicRange;
    public static int laserAdvancedRange;
    public static double laserBasicFireChance;
    public static double laserAdvancedFireChance;
    public static float shrinkScale;
    public static LaserVisualMode laserVisualMode;
    public static boolean enableMobPowers;
    public static boolean persistPowersOnDeath;
    public static double mobPowerSpawnChance;
    public static double mobCompoundVDropChance;
    public static double mobTempVDropChance;
    public static double mobV1DropChance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        addToBuriedTreasure = ADD_TEMP_V_TO_BURIED_TREASURE.get();
        addVToAncientCities = ADD_COMPOUND_V_TO_ANCIENT_CITIES.get();
        addTempVToAncientCities = ADD_TEMP_V_TO_ANCIENT_CITIES.get();
        addToBastions = ADD_TEMP_V_TO_BASTIONS.get();
        addToEndCities = ADD_COMPOUND_V_TO_END_CITIES.get();
        addV1ToAncientCities = ADD_V1_TO_ANCIENT_CITIES.get();
        addV1ToEndCities = ADD_V1_TO_END_CITIES.get();
        tempVFromTrader = TEMP_V_FROM_WANDERING_TRADER.get();
        tempVDuration = TEMP_V_DURATION.get();
        badOutcomeChance = COMPOUND_V_BAD_EFFECT_CHANCE.get();
        tempVBadOutcomeChance = TEMP_V_BAD_REACTION_CHANCE.get();
        damageReduction = COMPOUND_V_DAMAGE_REDUCTION.get();
        strengthMultiplier = COMPOUND_V_STRENGTH_MULTIPLIER.get();
        knockbackReduction = COMPOUND_V_KNOCKBACK_REDUCTION.get();
        weightGeneric = WEIGHT_GENERIC.get();
        weightSpeedster = WEIGHT_SPEEDSTER.get();
        speedsterMobAttack = SPEEDSTER_SPEED_ATTACK.get();
        weightWater = WEIGHT_WATER.get();
        weightTeleport = WEIGHT_TELEPORT.get();
        teleportRange = TELEPORT_RANGE.get();
        weightAtomCharging = WEIGHT_ATOM_CHARGING.get();
        weightInvisibility = WEIGHT_INVISIBILITY.get();
        weightNightVision = WEIGHT_NIGHT_VISION.get();
        weightLevitation = WEIGHT_LEVITATION.get();
        weightInvincible = WEIGHT_INVINCIBLE.get();
        weightCreativeFlight = WEIGHT_CREATIVE_FLIGHT.get();
        weightLaserEyesBasic = WEIGHT_LASER_EYES_BASIC.get();
        weightLaserEyesAdvanced = WEIGHT_LASER_EYES_ADVANCED.get();
        weightShrink = WEIGHT_SHRINK.get();
        weightEnlarge = WEIGHT_ENLARGE.get();
        weightPowerAbsorption = WEIGHT_POWER_ABSORPTION.get();
        weightSonicScream = WEIGHT_SONIC_SCREAM.get();
        weightHeadPop = WEIGHT_HEAD_POP.get();
        weightEnhancedRegen = WEIGHT_ENHANCED_REGEN.get();
        weightDensity = WEIGHT_DENSITY.get();
        densityDamageMultiplier = COMPOUND_V_DAMAGE_REDUCTION.get();
        weightSpider = WEIGHT_SPIDER.get();
        weightInstakill = WEIGHT_INSTAKILL.get();
        weightMindControl = WEIGHT_MIND_CONTROL.get();
        weightBerserker = WEIGHT_BERSERKER.get();
        weightProjectileImmunity = WEIGHT_PROJECTILE_IMMUNITY.get();
        weightRadioactive = WEIGHT_RADIOACTIVE.get();
        weightStarPower = WEIGHT_STAR_POWER.get();
        starPowerUnlimited = STAR_POWER_UNLIMITED.get();
        weightChestBlast = WEIGHT_CHEST_BLAST.get();
        chestBlastBeamDamage = CHEST_BLAST_BEAM_DAMAGE.get();
        chestBlastBurstDamage = CHEST_BLAST_BURST_DAMAGE.get();
        chestBlastDuration = CHEST_BLAST_DURATION.get();
        chestBlastInRegularPool = CHEST_BLAST_IN_REGULAR_POOL.get();
        chestBlastRange = CHEST_BLAST_RANGE.get();
        chestBlastBlockBreakChance = CHEST_BLAST_BLOCK_BREAK_CHANCE.get();
        mobDamageReduction = MOB_DAMAGE_REDUCTION.get();
        mobStrengthMultiplier = MOB_STRENGTH_MULTIPLIER.get();
        mobKnockbackReduction = MOB_KNOCKBACK_REDUCTION.get();
        laserBasicDamage = LASER_BASIC_DAMAGE.get();
        laserAdvancedDamage = LASER_ADVANCED_DAMAGE.get();
        laserBasicRange = LASER_BASIC_RANGE.get();
        laserAdvancedRange = LASER_ADVANCED_RANGE.get();
        laserBasicFireChance = LASER_BASIC_FIRE_CHANCE.get();
        laserAdvancedFireChance = LASER_ADVANCED_FIRE_CHANCE.get();
        shrinkScale = SHRINK_SCALE.get().floatValue();
        laserVisualMode = LASER_VISUAL_MODE.get();
        enableMobPowers = ENABLE_MOB_POWERS.get();
        persistPowersOnDeath = PERSIST_POWERS_ON_DEATH.get();
        mobPowerSpawnChance = MOB_POWER_SPAWN_CHANCE.get();
        mobCompoundVDropChance = MOB_COMPOUND_V_DROP_CHANCE.get();
        mobTempVDropChance = MOB_TEMP_V_DROP_CHANCE.get();
        mobV1DropChance = MOB_V1_DROP_CHANCE.get();
    }
}
