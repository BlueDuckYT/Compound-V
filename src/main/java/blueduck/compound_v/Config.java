package blueduck.compound_v;

import blueduck.compound_v.effect.CompoundVEffect;
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

    // --- Multi-Power ---
    private static final ForgeConfigSpec.BooleanValue ENABLE_MULTI_POWERS = BUILDER
            .comment("Allow Compound V to grant multiple powers at once (1 to multiPowerMaxCount)")
            .define("enableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MULTI_POWER_MAX_COUNT = BUILDER
            .comment("Maximum number of powers rolled when taking Compound V (randomly 1 to this value)")
            .defineInRange("multiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue TEMP_V_ENABLE_MULTI_POWERS = BUILDER
            .comment("Allow Temp V to grant multiple powers at once (separate from permanent Compound V)")
            .define("tempVEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue TEMP_V_MULTI_POWER_MAX_COUNT = BUILDER
            .comment("Maximum number of powers rolled when taking Temp V (randomly 1 to this value)")
            .defineInRange("tempVMultiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue MOB_ENABLE_MULTI_POWERS = BUILDER
            .comment("Allow mobs to spawn with multiple powers at once")
            .define("mobEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MOB_MULTI_POWER_MAX_COUNT = BUILDER
            .comment("Maximum number of powers a mob can spawn with (randomly 1 to this value)")
            .defineInRange("mobMultiPowerMaxCount", 2, 1, 3);

    // --- Combat ---
    // --- Player Stat Tiers ---
    // Each tier has base + perLevel for damage reduction, strength, and knockback
    // Formula: stat = base + (amplifier * perLevel)
    // Damage reduction: lower = tankier. Strength: higher = stronger. Knockback: lower = less knockback.

    private static final ForgeConfigSpec.ConfigValue<String> DEFAULT_POWER_TIER = BUILDER
            .comment("Default tier for powers that don't have a specific config. Valid: S, A, B, C, D")
            .define("defaultPowerTier", "C");

    // Per-power tier overrides (set to tier letter: S, A, B, C, D)
    private static final ForgeConfigSpec.ConfigValue<String> TIER_GENERIC = BUILDER.define("tierOf.generic", "SPECIAL");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_STORMFRONT = BUILDER.define("tierOf.stormfront", "S");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_INSTAKILL = BUILDER.define("tierOf.instakill", "S");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_CHEST_BLAST = BUILDER.define("tierOf.chest_blast", "S");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_INVINCIBLE = BUILDER.define("tierOf.invincible", "S");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LASER_ADVANCED = BUILDER.define("tierOf.laser_advanced", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_POWER_ABSORPTION = BUILDER.define("tierOf.power_absorption", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_NULLIFY = BUILDER.define("tierOf.nullify", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_HEAD_POP = BUILDER.define("tierOf.head_pop", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LASER_BASIC = BUILDER.define("tierOf.laser_basic", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LEAP = BUILDER.define("tierOf.leap", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_TELEPORT = BUILDER.define("tierOf.teleport", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SPEEDSTER = BUILDER.define("tierOf.speedster", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_BERSERKER = BUILDER.define("tierOf.berserker", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_HEALING = BUILDER.define("tierOf.healing", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_EXPLOSIVE = BUILDER.define("tierOf.explosive", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_PETRIFYING_GAZE = BUILDER.define("tierOf.petrifying_gaze", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_MIND_CONTROL = BUILDER.define("tierOf.mind_control", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_CREATIVE_FLIGHT = BUILDER.define("tierOf.creative_flight", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_ENHANCED_REGEN = BUILDER.define("tierOf.enhanced_regen", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_NIGHT_VISION = BUILDER.define("tierOf.night_vision", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_INVISIBILITY = BUILDER.define("tierOf.invisibility", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_DEEP = BUILDER.define("tierOf.deep", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_DENSITY = BUILDER.define("tierOf.density", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_ENLARGE = BUILDER.define("tierOf.enlarge", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SHRINK = BUILDER.define("tierOf.shrink", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LUCK = BUILDER.define("tierOf.luck", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LEVITATION = BUILDER.define("tierOf.levitation", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_MIMIC = BUILDER.define("tierOf.mimic", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SONIC_SCREAM = BUILDER.define("tierOf.sonic_scream", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_PROJECTILE_IMMUNITY = BUILDER.define("tierOf.projectile_immunity", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_ATOM_CHARGING = BUILDER.define("tierOf.atom_charging", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_STAR_POWER = BUILDER.define("tierOf.star_power", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_FORCEFIELD = BUILDER.define("tierOf.forcefield", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SPIDER = BUILDER.define("tierOf.spider", "D");

    // Tier S — top-tier (Stormfront, Star Power, Instakill, Chest Blast)
    private static final ForgeConfigSpec.DoubleValue TIER_S_DR = BUILDER.defineInRange("tierS.damageReduction", 0.3, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_DR_PL = BUILDER.defineInRange("tierS.damageReductionPerLevel", -0.05, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_STR = BUILDER.defineInRange("tierS.strengthMultiplier", 2.0, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_S_STR_PL = BUILDER.defineInRange("tierS.strengthPerLevel", 0.5, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KB = BUILDER.defineInRange("tierS.knockbackReduction", 0.3, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KB_PL = BUILDER.defineInRange("tierS.knockbackPerLevel", -0.05, -5, 5);

    // Tier A — high-tier (Laser Advanced, Invincible, PowerAbsorption, Explosive, Petrifying Gaze)
    private static final ForgeConfigSpec.DoubleValue TIER_A_DR = BUILDER.defineInRange("tierA.damageReduction", 0.4, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_DR_PL = BUILDER.defineInRange("tierA.damageReductionPerLevel", -0.04, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_STR = BUILDER.defineInRange("tierA.strengthMultiplier", 1.75, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_A_STR_PL = BUILDER.defineInRange("tierA.strengthPerLevel", 0.35, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KB = BUILDER.defineInRange("tierA.knockbackReduction", 0.4, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KB_PL = BUILDER.defineInRange("tierA.knockbackPerLevel", -0.04, -5, 5);

    // Tier B — mid-tier (Laser Basic, Leap, Teleport, Speedster, Berserker, Healing)
    private static final ForgeConfigSpec.DoubleValue TIER_B_DR = BUILDER.defineInRange("tierB.damageReduction", 0.5, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_DR_PL = BUILDER.defineInRange("tierB.damageReductionPerLevel", -0.03, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_STR = BUILDER.defineInRange("tierB.strengthMultiplier", 1.5, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_B_STR_PL = BUILDER.defineInRange("tierB.strengthPerLevel", 0.25, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KB = BUILDER.defineInRange("tierB.knockbackReduction", 0.5, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KB_PL = BUILDER.defineInRange("tierB.knockbackPerLevel", -0.03, -5, 5);

    // Tier C — low-tier (Flight, EnhancedRegen, NightVision, Invisibility, Deep, Density, Enlarge, Shrink)
    private static final ForgeConfigSpec.DoubleValue TIER_C_DR = BUILDER.defineInRange("tierC.damageReduction", 0.65, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_DR_PL = BUILDER.defineInRange("tierC.damageReductionPerLevel", -0.02, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_STR = BUILDER.defineInRange("tierC.strengthMultiplier", 1.25, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_C_STR_PL = BUILDER.defineInRange("tierC.strengthPerLevel", 0.15, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KB = BUILDER.defineInRange("tierC.knockbackReduction", 0.65, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KB_PL = BUILDER.defineInRange("tierC.knockbackPerLevel", -0.02, -5, 5);

    // Tier D — utility (Luck, Levitation, Mimic, Rex Splode, Sonic Scream, etc.)
    private static final ForgeConfigSpec.DoubleValue TIER_D_DR = BUILDER.defineInRange("tierD.damageReduction", 0.8, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_DR_PL = BUILDER.defineInRange("tierD.damageReductionPerLevel", -0.01, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_STR = BUILDER.defineInRange("tierD.strengthMultiplier", 1.1, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_D_STR_PL = BUILDER.defineInRange("tierD.strengthPerLevel", 0.05, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KB = BUILDER.defineInRange("tierD.knockbackReduction", 0.8, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KB_PL = BUILDER.defineInRange("tierD.knockbackPerLevel", -0.01, -5, 5);

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
    // Spider — experimental, commented out
    // private static final ForgeConfigSpec.IntValue WEIGHT_SPIDER = BUILDER
    //         .comment("Weight of obtaining Spider powers when taking Compound V (Currently Disabled)")
    //         .defineInRange("weight_spider", 5, 0, Integer.MAX_VALUE);
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
    private static final ForgeConfigSpec.IntValue WEIGHT_STAR_POWER = BUILDER
            .comment("Weight of obtaining Star Power when taking Compound V (experimental)")
            .defineInRange("weight_star_power", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue STAR_POWER_UNLIMITED = BUILDER
            .comment("If true, Star Power has no cooldown and can be toggled on/off at will")
            .define("starPowerUnlimited", false);
    private static final ForgeConfigSpec.IntValue WEIGHT_LEAP = BUILDER
            .comment("Weight of obtaining Leap (Queen Maeve) when taking Compound V")
            .defineInRange("weight_leap", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_EXPLOSIVE = BUILDER
            .comment("Weight of obtaining Explosive power when taking Compound V")
            .defineInRange("weight_explosive", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_HEALING = BUILDER
            .comment("Weight of obtaining Healing power when taking Compound V")
            .defineInRange("weight_healing", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_MIMIC = BUILDER
            .comment("Weight of obtaining Mimic (power copying) when taking Compound V")
            .defineInRange("weight_mimic", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue EXPLOSIVE_PLAYER_BLOCK_DAMAGE = BUILDER
            .comment("Whether player Explosive power destroys blocks")
            .define("explosivePlayerBlockDamage", true);
    private static final ForgeConfigSpec.BooleanValue EXPLOSIVE_MOB_BLOCK_DAMAGE = BUILDER
            .comment("Whether mob Explosive power destroys blocks (disabled by default to prevent grief)")
            .define("explosiveMobBlockDamage", false);
    private static final ForgeConfigSpec.DoubleValue EXPLOSIVE_BLAST_RADIUS = BUILDER
            .comment("Blast radius of the Explosive power (creeper is 3.0)")
            .defineInRange("explosiveBlastRadius", 4.0, 1.0, 20.0);
    private static final ForgeConfigSpec.IntValue EXPLOSIVE_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) for Explosive power (600 = 30 seconds)")
            .defineInRange("explosiveCooldown", 600, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MIMIC_DURATION = BUILDER
            .comment("Duration (in ticks) of copied power from Mimic (600 = 30 seconds)")
            .defineInRange("mimicDuration", 600, 20, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_PETRIFYING_GAZE = BUILDER
            .comment("Weight of obtaining Petrifying Gaze when taking Compound V")
            .defineInRange("weight_petrifying_gaze", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PETRIFYING_GAZE_RANGE = BUILDER
            .comment("Range in blocks for Petrifying Gaze to freeze targets")
            .defineInRange("petrifyingGazeRange", 40.0, 5.0, 128.0);
    private static final ForgeConfigSpec.BooleanValue PETRIFYING_GAZE_AFFECTS_PLAYERS = BUILDER
            .comment("Whether Petrifying Gaze can freeze other players")
            .define("petrifyingGazeAffectsPlayers", true);
    private static final ForgeConfigSpec.IntValue WEIGHT_STORMFRONT = BUILDER
            .comment("Weight of obtaining Stormfront (lightning flight) from V1")
            .defineInRange("weight_stormfront", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue STORMFRONT_LIGHTNING_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) between Stormfront lightning strikes (60 = 3 seconds)")
            .defineInRange("stormfrontLightningCooldown", 60, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue STORMFRONT_IN_REGULAR_POOL = BUILDER
            .comment("Whether Stormfront can be obtained from regular Compound V (not just V1)")
            .define("stormfrontInRegularPool", false);
    // Forcefield — experimental, commented out
    // private static final ForgeConfigSpec.IntValue WEIGHT_FORCEFIELD = BUILDER
    //         .comment("Weight of obtaining Forcefield when taking Compound V")
    //         .defineInRange("weight_forcefield", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LUCK = BUILDER
            .comment("Weight of obtaining Luck when taking Compound V (comes in 3 levels)")
            .defineInRange("weight_luck", 3, 0, Integer.MAX_VALUE);
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
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_CHARGE_TIME = BUILDER
            .comment("Charge-up time (in ticks) before the Chest Blast fires (200 = 10 seconds)")
            .defineInRange("chestBlastChargeTime", 200, 20, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) after the Chest Blast ends before it can be used again (1200 = 60 seconds)")
            .defineInRange("chestBlastCooldown", 1200, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_IN_REGULAR_POOL = BUILDER
            .comment("Whether Chest Blast can be obtained from regular Compound V (not just V1)")
            .define("chestBlastInRegularPool", false);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_RANGE = BUILDER
            .comment("Range (in blocks) of the Chest Blast beam")
            .defineInRange("chestBlastRange", 32, 8, 256);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Chance per tick per block of the Chest Blast beam destroying blocks in its path (0 = disabled). WARNING: Values above 0 cause significant lag due to per-tick block iteration across the beam cone.")
            .defineInRange("chestBlastBlockBreakChance", 0.0, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_BLOCKED_BY_WALLS = BUILDER
            .comment("Whether the player's Chest Blast beam is blocked by solid blocks (beam stops at walls)")
            .define("chestBlastBlockedByWalls", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_INVINCIBLE = BUILDER
            .comment("Whether the Chest Blast beam can strip Invincibility (if false, Invincible blocks the strip)")
            .define("chestBlastStripsInvincible", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_POWERS = BUILDER
            .comment("Whether the player's Chest Blast strips Compound V powers from targets (like Anti-V)")
            .define("chestBlastStripsPowers", true);
    private static final ForgeConfigSpec.BooleanValue MOB_CHEST_BLAST_STRIPS_POWERS = BUILDER
            .comment("Whether mob Chest Blast strips Compound V powers from targets (like the player version)")
            .define("mobChestBlastStripsPowers", false);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_SHIELD_BLOCKS_STRIP = BUILDER
            .comment("Whether holding a shield blocks Chest Blast's power-stripping effect (damage is still dealt)")
            .define("chestBlastShieldBlocksStrip", true);

    // --- Compound V: Gone Viral integration ---
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_PLAYER_POWERS = BUILDER
            .comment("If Compound V: Gone Viral is installed, the virus effect disables all Compound V powers for players")
            .define("virusDisablesPlayerPowers", true);
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_MOB_POWERS = BUILDER
            .comment("If Compound V: Gone Viral is installed, the virus effect disables all Compound V powers for mobs")
            .define("virusDisablesMobPowers", true);

    // --- Mob Laser ---
    private static final ForgeConfigSpec.DoubleValue MOB_LASER_DAMAGE_CONFIG = BUILDER
            .comment("Damage per tick for mob basic laser eyes (~20 ticks = 1 second)")
            .defineInRange("mobLaserDamage", 0.02, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue MOB_ADVANCED_LASER_DAMAGE_CONFIG = BUILDER
            .comment("Damage per tick for mob advanced laser eyes")
            .defineInRange("mobAdvancedLaserDamage", 0.06, 0.0, 10.0);

    // --- PowerPlex Discharge ---
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_DAMAGE = BUILDER
            .comment("Damage per tick from PowerPlex electric discharge (hold V), scaled by charge level")
            .defineInRange("powerplexDischargeDamage", 2.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_RADIUS = BUILDER
            .comment("Radius of the PowerPlex discharge field in blocks")
            .defineInRange("powerplexDischargeRadius", 5.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue POWERPLEX_DISCHARGE_TICK_RATE = BUILDER
            .comment("Damage applied every N ticks during discharge (lower = faster, 4 = 5 times/sec)")
            .defineInRange("powerplexDischargeTickRate", 4, 1, 40);

    // --- Stormfront Discharge ---
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_DAMAGE = BUILDER
            .comment("Damage per tick from Stormfront electric discharge (sneak + hold V)")
            .defineInRange("stormfrontDischargeDamage", 0.8, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_RADIUS = BUILDER
            .comment("Radius of the Stormfront discharge field in blocks")
            .defineInRange("stormfrontDischargeRadius", 6.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue STORMFRONT_DISCHARGE_TICK_RATE = BUILDER
            .comment("Damage applied every N ticks during discharge (lower = faster)")
            .defineInRange("stormfrontDischargeTickRate", 4, 1, 40);
    private static final ForgeConfigSpec.DoubleValue MOB_CHEST_BLAST_INACCURACY = BUILDER
            .comment("Maximum aim deviation in degrees for mob Chest Blast (0 = perfect aim, 5 = dodgeable)")
            .defineInRange("mobChestBlastInaccuracy", 5.0, 0.0, 45.0);

    // --- Mob Power Toggles (all enabled by default) ---
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_SPEEDSTER = BUILDER.comment("Allow mobs to spawn with Speedster").define("mobPowerSpeedster", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_DEEP = BUILDER.comment("Allow mobs to spawn with Deep (water powers)").define("mobPowerDeep", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_TELEPORT = BUILDER.comment("Allow mobs to spawn with Teleportation").define("mobPowerTeleport", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_ATOM_CHARGING = BUILDER.comment("Allow mobs to spawn with Atom Charging").define("mobPowerAtomCharging", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_INVISIBILITY = BUILDER.comment("Allow mobs to spawn with Invisibility").define("mobPowerInvisibility", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_INVINCIBLE = BUILDER.comment("Allow mobs to spawn with Invincibility").define("mobPowerInvincible", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_LASER_BASIC = BUILDER.comment("Allow mobs to spawn with Basic Laser Eyes").define("mobPowerLaserBasic", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_LASER_ADVANCED = BUILDER.comment("Allow mobs to spawn with Advanced Laser Eyes").define("mobPowerLaserAdvanced", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_ENHANCED_REGEN = BUILDER.comment("Allow mobs to spawn with Enhanced Regeneration").define("mobPowerEnhancedRegen", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_BERSERKER = BUILDER.comment("Allow mobs to spawn with Berserker").define("mobPowerBerserker", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_PROJECTILE_IMMUNITY = BUILDER.comment("Allow mobs to spawn with Projectile Immunity").define("mobPowerProjectileImmunity", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_MAGNETISM = BUILDER.comment("Allow mobs to spawn with Magnetism").define("mobPowerMagnetism", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_SHRINK = BUILDER.comment("Allow mobs to spawn with Shrink (requires Pehkui)").define("mobPowerShrink", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_ENLARGE = BUILDER.comment("Allow mobs to spawn with Enlarge (requires Pehkui)").define("mobPowerEnlarge", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_CREATIVE_FLIGHT = BUILDER.comment("Allow mobs to spawn with Creative Flight (fly toward target, floating turret with lasers)").define("mobPowerCreativeFlight", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_CHEST_BLAST = BUILDER.comment("Allow mobs to spawn with Chest Blast (Soldier Boy). Disabled by default.").define("mobPowerChestBlast", false);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_LEAP = BUILDER.comment("Allow mobs to spawn with Leap (Queen Maeve)").define("mobPowerLeap", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_EXPLOSIVE = BUILDER.comment("Allow mobs to spawn with Explosive. Disabled by default.").define("mobPowerExplosive", false);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_HEALING = BUILDER.comment("Allow mobs to spawn with Healing aura").define("mobPowerHealing", true);
    private static final ForgeConfigSpec.IntValue MOB_CHEST_BLAST_WEIGHT = BUILDER
            .comment("Weight of Chest Blast in the mob power pool")
            .defineInRange("mobChestBlastWeight", 1, 1, Integer.MAX_VALUE);

    public static boolean mobPowerSpeedster;
    public static boolean mobPowerDeep;
    public static boolean mobPowerTeleport;
    public static boolean mobPowerAtomCharging;
    public static boolean mobPowerInvisibility;
    public static boolean mobPowerInvincible;
    public static boolean mobPowerLaserBasic;
    public static boolean mobPowerLaserAdvanced;
    public static boolean mobPowerEnhancedRegen;
    public static boolean mobPowerBerserker;
    public static boolean mobPowerProjectileImmunity;
    public static boolean mobPowerMagnetism;
    public static boolean mobPowerShrink;
    public static boolean mobPowerEnlarge;
    // mobPowerCreativeFlight and mobPowerChestBlast are declared above with chestBlastStripsInvincible
    private static final ForgeConfigSpec.DoubleValue MOB_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken by hostile mobs with Compound V (1.0 = normal, 0.5 = half)")
            .defineInRange("mobDamageReduction", 1.0, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue MOB_STRENGTH_MULTIPLIER = BUILDER
            .comment("Damage multiplier of damage dealt by hostile mobs with Compound V (1.0 = normal, 2.0 = double)")
            .defineInRange("mobStrengthMultiplier", 1.0, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MOB_KNOCKBACK_REDUCTION = BUILDER
            .comment("Knockback multiplier for hostile mobs with Compound V (1.0 = normal, 0.25 = quarter)")
            .defineInRange("mobKnockbackReduction", 1.0, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue FRIENDLY_MOB_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken by friendly mobs with Compound V (golems, wolves, animals)")
            .defineInRange("friendlyMobDamageReduction", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue FRIENDLY_MOB_STRENGTH_MULTIPLIER = BUILDER
            .comment("Damage multiplier of damage dealt by friendly mobs with Compound V")
            .defineInRange("friendlyMobStrengthMultiplier", 1.75, 0.0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue FRIENDLY_MOB_KNOCKBACK_REDUCTION = BUILDER
            .comment("Knockback multiplier for friendly mobs with Compound V")
            .defineInRange("friendlyMobKnockbackReduction", 0.5, 0.0, 1.0);

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
    private static final ForgeConfigSpec.BooleanValue VIRUS_REMOVES_POWER_ON_DEATH = BUILDER
            .comment("If persistPowersOnDeath is enabled and the player has the virus (Compound V: Gone Viral), dying removes one random Compound V power permanently")
            .define("virusRemovesPowerOnDeath", true);
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
    public static boolean enableMultiPowers;
    public static int multiPowerMaxCount;
    public static boolean tempVEnableMultiPowers;
    public static int tempVMultiPowerMaxCount;
    public static boolean mobEnableMultiPowers;
    public static int mobMultiPowerMaxCount;
    // Tier system
    public static CompoundVEffect.PowerTier defaultPowerTier;
    public static double[][] tierStats; // [tier ordinal][stat index: 0=DR, 1=DR_PL, 2=STR, 3=STR_PL, 4=KB, 5=KB_PL]
    public static java.util.Map<net.minecraft.world.effect.MobEffect, CompoundVEffect.PowerTier> effectTierMap = new java.util.HashMap<>();
    public static boolean chestBlastStripsPowers;
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
    // public static int weightSpider; // experimental
    public static int weightInstakill;
    public static int weightMindControl;
    public static int weightBerserker;
    public static int weightProjectileImmunity;
    public static int weightStarPower;
    public static boolean starPowerUnlimited;
    public static int weightLeap;
    public static int weightExplosive;
    public static int weightHealing;
    public static int weightMimic;
    public static boolean explosivePlayerBlockDamage;
    public static boolean explosiveMobBlockDamage;
    public static double explosiveBlastRadius;
    public static int explosiveCooldown;
    public static int mimicDuration;
    public static int weightPetrifyingGaze;
    public static double petrifyingGazeRange;
    public static boolean petrifyingGazeAffectsPlayers;
    public static int weightStormfront;
    public static int stormfrontLightningCooldown;
    public static boolean stormfrontInRegularPool;
    // public static int weightForcefield; // experimental
    public static int weightLuck;
    public static int weightChestBlast;
    public static double chestBlastBeamDamage;
    public static double chestBlastBurstDamage;
    public static int chestBlastDuration;
    public static int chestBlastChargeTime;
    public static int chestBlastCooldown;
    public static boolean chestBlastInRegularPool;
    public static int chestBlastRange;
    public static double chestBlastBlockBreakChance;
    public static boolean chestBlastBlockedByWalls;
    public static boolean chestBlastStripsInvincible;
    public static boolean mobPowerCreativeFlight;
    public static boolean mobPowerChestBlast;
    public static boolean mobPowerLeap;
    public static boolean mobPowerExplosive;
    public static boolean mobPowerHealing;
    public static int mobChestBlastWeight;
    public static boolean mobChestBlastStripsPowers;
    public static boolean chestBlastShieldBlocksStrip;
    public static boolean virusDisablesPlayerPowers;
    public static boolean virusDisablesMobPowers;
    public static double mobLaserDamage;
    public static double mobAdvancedLaserDamage;
    public static double powerplexDischargeDamage;
    public static double powerplexDischargeRadius;
    public static int powerplexDischargeTickRate;
    public static double stormfrontDischargeDamage;
    public static double stormfrontDischargeRadius;
    public static int stormfrontDischargeTickRate;
    public static double mobChestBlastInaccuracy;
    public static double mobDamageReduction;
    public static double mobStrengthMultiplier;
    public static double mobKnockbackReduction;
    public static double friendlyMobDamageReduction;
    public static double friendlyMobStrengthMultiplier;
    public static double friendlyMobKnockbackReduction;
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
    public static boolean virusRemovesPowerOnDeath;
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
        enableMultiPowers = ENABLE_MULTI_POWERS.get();
        multiPowerMaxCount = MULTI_POWER_MAX_COUNT.get();
        tempVEnableMultiPowers = TEMP_V_ENABLE_MULTI_POWERS.get();
        tempVMultiPowerMaxCount = TEMP_V_MULTI_POWER_MAX_COUNT.get();
        mobEnableMultiPowers = MOB_ENABLE_MULTI_POWERS.get();
        mobMultiPowerMaxCount = MOB_MULTI_POWER_MAX_COUNT.get();
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
        densityDamageMultiplier = DENSITY_DAMAGE_REDUCTION.get();
        // weightSpider = WEIGHT_SPIDER.get(); // experimental
        weightInstakill = WEIGHT_INSTAKILL.get();
        weightMindControl = WEIGHT_MIND_CONTROL.get();
        weightBerserker = WEIGHT_BERSERKER.get();
        weightProjectileImmunity = WEIGHT_PROJECTILE_IMMUNITY.get();
        weightStarPower = WEIGHT_STAR_POWER.get();
        starPowerUnlimited = STAR_POWER_UNLIMITED.get();
        weightLeap = WEIGHT_LEAP.get();
        weightExplosive = WEIGHT_EXPLOSIVE.get();
        weightHealing = WEIGHT_HEALING.get();
        weightMimic = WEIGHT_MIMIC.get();
        explosivePlayerBlockDamage = EXPLOSIVE_PLAYER_BLOCK_DAMAGE.get();
        explosiveMobBlockDamage = EXPLOSIVE_MOB_BLOCK_DAMAGE.get();
        explosiveBlastRadius = EXPLOSIVE_BLAST_RADIUS.get();
        explosiveCooldown = EXPLOSIVE_COOLDOWN.get();
        mimicDuration = MIMIC_DURATION.get();
        weightPetrifyingGaze = WEIGHT_PETRIFYING_GAZE.get();
        petrifyingGazeRange = PETRIFYING_GAZE_RANGE.get();
        petrifyingGazeAffectsPlayers = PETRIFYING_GAZE_AFFECTS_PLAYERS.get();
        weightStormfront = WEIGHT_STORMFRONT.get();
        stormfrontLightningCooldown = STORMFRONT_LIGHTNING_COOLDOWN.get();
        stormfrontInRegularPool = STORMFRONT_IN_REGULAR_POOL.get();
        // weightForcefield = WEIGHT_FORCEFIELD.get(); // experimental
        weightLuck = WEIGHT_LUCK.get();
        weightChestBlast = WEIGHT_CHEST_BLAST.get();
        chestBlastBeamDamage = CHEST_BLAST_BEAM_DAMAGE.get();
        chestBlastBurstDamage = CHEST_BLAST_BURST_DAMAGE.get();
        chestBlastDuration = CHEST_BLAST_DURATION.get();
        chestBlastChargeTime = CHEST_BLAST_CHARGE_TIME.get();
        chestBlastCooldown = CHEST_BLAST_COOLDOWN.get();
        chestBlastInRegularPool = CHEST_BLAST_IN_REGULAR_POOL.get();
        chestBlastRange = CHEST_BLAST_RANGE.get();
        chestBlastBlockBreakChance = CHEST_BLAST_BLOCK_BREAK_CHANCE.get();
        chestBlastBlockedByWalls = CHEST_BLAST_BLOCKED_BY_WALLS.get();
        chestBlastStripsInvincible = CHEST_BLAST_STRIPS_INVINCIBLE.get();
        chestBlastStripsPowers = CHEST_BLAST_STRIPS_POWERS.get();

        // Tier system
        String tierStr = DEFAULT_POWER_TIER.get().toUpperCase();
        try {
            defaultPowerTier = CompoundVEffect.PowerTier.valueOf(tierStr);
        } catch (IllegalArgumentException e) {
            defaultPowerTier = CompoundVEffect.PowerTier.C;
        }
        tierStats = new double[5][6]; // S=0, A=1, B=2, C=3, D=4
        tierStats[0] = new double[]{ TIER_S_DR.get(), TIER_S_DR_PL.get(), TIER_S_STR.get(), TIER_S_STR_PL.get(), TIER_S_KB.get(), TIER_S_KB_PL.get() };
        tierStats[1] = new double[]{ TIER_A_DR.get(), TIER_A_DR_PL.get(), TIER_A_STR.get(), TIER_A_STR_PL.get(), TIER_A_KB.get(), TIER_A_KB_PL.get() };
        tierStats[2] = new double[]{ TIER_B_DR.get(), TIER_B_DR_PL.get(), TIER_B_STR.get(), TIER_B_STR_PL.get(), TIER_B_KB.get(), TIER_B_KB_PL.get() };
        tierStats[3] = new double[]{ TIER_C_DR.get(), TIER_C_DR_PL.get(), TIER_C_STR.get(), TIER_C_STR_PL.get(), TIER_C_KB.get(), TIER_C_KB_PL.get() };
        tierStats[4] = new double[]{ TIER_D_DR.get(), TIER_D_DR_PL.get(), TIER_D_STR.get(), TIER_D_STR_PL.get(), TIER_D_KB.get(), TIER_D_KB_PL.get() };

        // Build per-effect tier map (deferred — effects must be registered first)
        // This is called from a config reload event, so EffectReg is already populated
        buildEffectTierMap();
        mobPowerCreativeFlight = MOB_POWER_CREATIVE_FLIGHT.get();
        mobPowerChestBlast = MOB_POWER_CHEST_BLAST.get();
        mobPowerLeap = MOB_POWER_LEAP.get();
        mobPowerExplosive = MOB_POWER_EXPLOSIVE.get();
        mobPowerHealing = MOB_POWER_HEALING.get();
        mobChestBlastWeight = MOB_CHEST_BLAST_WEIGHT.get();
        mobChestBlastStripsPowers = MOB_CHEST_BLAST_STRIPS_POWERS.get();
        chestBlastShieldBlocksStrip = CHEST_BLAST_SHIELD_BLOCKS_STRIP.get();
        virusDisablesPlayerPowers = VIRUS_DISABLES_PLAYER_POWERS.get();
        virusDisablesMobPowers = VIRUS_DISABLES_MOB_POWERS.get();
        mobLaserDamage = MOB_LASER_DAMAGE_CONFIG.get();
        mobAdvancedLaserDamage = MOB_ADVANCED_LASER_DAMAGE_CONFIG.get();
        powerplexDischargeDamage = POWERPLEX_DISCHARGE_DAMAGE.get();
        powerplexDischargeRadius = POWERPLEX_DISCHARGE_RADIUS.get();
        powerplexDischargeTickRate = POWERPLEX_DISCHARGE_TICK_RATE.get();
        stormfrontDischargeDamage = STORMFRONT_DISCHARGE_DAMAGE.get();
        stormfrontDischargeRadius = STORMFRONT_DISCHARGE_RADIUS.get();
        stormfrontDischargeTickRate = STORMFRONT_DISCHARGE_TICK_RATE.get();
        mobChestBlastInaccuracy = MOB_CHEST_BLAST_INACCURACY.get();
        mobDamageReduction = MOB_DAMAGE_REDUCTION.get();
        mobStrengthMultiplier = MOB_STRENGTH_MULTIPLIER.get();
        mobKnockbackReduction = MOB_KNOCKBACK_REDUCTION.get();
        friendlyMobDamageReduction = FRIENDLY_MOB_DAMAGE_REDUCTION.get();
        friendlyMobStrengthMultiplier = FRIENDLY_MOB_STRENGTH_MULTIPLIER.get();
        friendlyMobKnockbackReduction = FRIENDLY_MOB_KNOCKBACK_REDUCTION.get();
        mobPowerSpeedster = MOB_POWER_SPEEDSTER.get();
        mobPowerDeep = MOB_POWER_DEEP.get();
        mobPowerTeleport = MOB_POWER_TELEPORT.get();
        mobPowerAtomCharging = MOB_POWER_ATOM_CHARGING.get();
        mobPowerInvisibility = MOB_POWER_INVISIBILITY.get();
        mobPowerInvincible = MOB_POWER_INVINCIBLE.get();
        mobPowerLaserBasic = MOB_POWER_LASER_BASIC.get();
        mobPowerLaserAdvanced = MOB_POWER_LASER_ADVANCED.get();
        mobPowerEnhancedRegen = MOB_POWER_ENHANCED_REGEN.get();
        mobPowerBerserker = MOB_POWER_BERSERKER.get();
        mobPowerProjectileImmunity = MOB_POWER_PROJECTILE_IMMUNITY.get();
        mobPowerMagnetism = MOB_POWER_MAGNETISM.get();
        mobPowerShrink = MOB_POWER_SHRINK.get();
        mobPowerEnlarge = MOB_POWER_ENLARGE.get();
        // mobPowerCreativeFlight and mobPowerChestBlast are loaded above with chestBlastStripsInvincible
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
        virusRemovesPowerOnDeath = VIRUS_REMOVES_POWER_ON_DEATH.get();
        mobPowerSpawnChance = MOB_POWER_SPAWN_CHANCE.get();
        mobCompoundVDropChance = MOB_COMPOUND_V_DROP_CHANCE.get();
        mobTempVDropChance = MOB_TEMP_V_DROP_CHANCE.get();
        mobV1DropChance = MOB_V1_DROP_CHANCE.get();
    }

    // --- Tier stat accessors (called by CompoundVEffect.PowerTier) ---

    public static double getTierDamageReduction(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][0];
    }
    public static double getTierDamageReductionPerLevel(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][1];
    }
    public static double getTierStrengthMultiplier(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][2];
    }
    public static double getTierStrengthPerLevel(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][3];
    }
    public static double getTierKnockbackReduction(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][4];
    }
    public static double getTierKnockbackPerLevel(CompoundVEffect.PowerTier tier) {
        return tierStats[tier.ordinal()][5];
    }

    private static CompoundVEffect.PowerTier parseTier(String s, CompoundVEffect.PowerTier fallback) {
        if (s == null || s.equalsIgnoreCase("SPECIAL")) return null; // Generic uses its own logic
        try {
            return CompoundVEffect.PowerTier.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static void buildEffectTierMap() {
        effectTierMap.clear();
        var reg = blueduck.compound_v.registry.EffectReg.class;
        mapTier(blueduck.compound_v.registry.EffectReg.STORMFRONT, TIER_STORMFRONT);
        mapTier(blueduck.compound_v.registry.EffectReg.INSTAKILL, TIER_INSTAKILL);
        mapTier(blueduck.compound_v.registry.EffectReg.CHEST_BLAST, TIER_CHEST_BLAST);
        mapTier(blueduck.compound_v.registry.EffectReg.INVINCIBLE, TIER_INVINCIBLE);
        mapTier(blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED, TIER_LASER_ADVANCED);
        mapTier(blueduck.compound_v.registry.EffectReg.POWER_ABSORPTION, TIER_POWER_ABSORPTION);
        mapTier(blueduck.compound_v.registry.EffectReg.NULLIFY, TIER_NULLIFY);
        mapTier(blueduck.compound_v.registry.EffectReg.HEAD_POP, TIER_HEAD_POP);
        mapTier(blueduck.compound_v.registry.EffectReg.LASER_EYES_BASIC, TIER_LASER_BASIC);
        mapTier(blueduck.compound_v.registry.EffectReg.LEAP, TIER_LEAP);
        mapTier(blueduck.compound_v.registry.EffectReg.TELEPORT, TIER_TELEPORT);
        mapTier(blueduck.compound_v.registry.EffectReg.SPEEDSTER, TIER_SPEEDSTER);
        mapTier(blueduck.compound_v.registry.EffectReg.BERSERKER, TIER_BERSERKER);
        mapTier(blueduck.compound_v.registry.EffectReg.HEALING, TIER_HEALING);
        mapTier(blueduck.compound_v.registry.EffectReg.EXPLOSIVE, TIER_EXPLOSIVE);
        mapTier(blueduck.compound_v.registry.EffectReg.PETRIFYING_GAZE, TIER_PETRIFYING_GAZE);
        mapTier(blueduck.compound_v.registry.EffectReg.MIND_CONTROL, TIER_MIND_CONTROL);
        mapTier(blueduck.compound_v.registry.EffectReg.CREATIVE_FLIGHT, TIER_CREATIVE_FLIGHT);
        mapTier(blueduck.compound_v.registry.EffectReg.ENHANCED_REGEN, TIER_ENHANCED_REGEN);
        mapTier(blueduck.compound_v.registry.EffectReg.NIGHT_VISION, TIER_NIGHT_VISION);
        mapTier(blueduck.compound_v.registry.EffectReg.INVISIBILITY, TIER_INVISIBILITY);
        mapTier(blueduck.compound_v.registry.EffectReg.DEEP, TIER_DEEP);
        mapTier(blueduck.compound_v.registry.EffectReg.DENSITY, TIER_DENSITY);
        mapTier(blueduck.compound_v.registry.EffectReg.ENLARGE, TIER_ENLARGE);
        mapTier(blueduck.compound_v.registry.EffectReg.SHRINK, TIER_SHRINK);
        mapTier(blueduck.compound_v.registry.EffectReg.LUCK, TIER_LUCK);
        mapTier(blueduck.compound_v.registry.EffectReg.LEVITATION, TIER_LEVITATION);
        mapTier(blueduck.compound_v.registry.EffectReg.MIMIC, TIER_MIMIC);
        mapTier(blueduck.compound_v.registry.EffectReg.SONIC_SCREAM, TIER_SONIC_SCREAM);
        mapTier(blueduck.compound_v.registry.EffectReg.PROJECTILE_IMMUNITY, TIER_PROJECTILE_IMMUNITY);
        mapTier(blueduck.compound_v.registry.EffectReg.ATOM_CHARGING, TIER_ATOM_CHARGING);
        mapTier(blueduck.compound_v.registry.EffectReg.STAR_POWER, TIER_STAR_POWER);
        mapTier(blueduck.compound_v.registry.EffectReg.FORCEFIELD, TIER_FORCEFIELD);
        mapTier(blueduck.compound_v.registry.EffectReg.SPIDER, TIER_SPIDER);
    }

    private static void mapTier(net.minecraftforge.registries.RegistryObject<net.minecraft.world.effect.MobEffect> effect,
                                 ForgeConfigSpec.ConfigValue<String> configValue) {
        CompoundVEffect.PowerTier tier = parseTier(configValue.get(), defaultPowerTier);
        if (tier != null && effect.isPresent()) {
            effectTierMap.put(effect.get(), tier);
        }
    }

    /**
     * Look up a specific effect's configured tier. Returns defaultPowerTier if not mapped.
     */
    public static CompoundVEffect.PowerTier getEffectTier(net.minecraft.world.effect.MobEffect effect) {
        return effectTierMap.getOrDefault(effect, defaultPowerTier);
    }
}
