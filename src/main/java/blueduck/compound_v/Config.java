package blueduck.compound_v;

import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ============================================================
    //  ALL POWER WEIGHTS (grouped). Renders as [regularWeight],
    //  [failureWeight], [v1Weight], [mobWeight] TOML sections.
    // ============================================================
    // ===== REGULAR-V POOL WEIGHTS =====
    private static final ForgeConfigSpec.IntValue WEIGHT_AIMLOCK = BUILDER
            .comment("Weight of obtaining Aimlock power when taking Compound V")
            .defineInRange("regularWeight.aimlock", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ATOM_CHARGING = BUILDER
            .comment("Weight of obtaining Atom Charging power when taking Compound V")
            .defineInRange("regularWeight.atom_charging", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_BERSERKER = BUILDER
            .comment("Weight of obtaining Berserker power when taking Compound V")
            .defineInRange("regularWeight.berserker", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_CHEST_BLAST = BUILDER
            .comment("Weight of Chest Blast (Soldier Boy) in the REGULAR Compound V pool. Default 0 (Chest Blast is V1-exclusive); its V1 weight is v1_weight_chest_blast.")
            .defineInRange("regularWeight.chest_blast", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_CREATIVE_FLIGHT = BUILDER
            .comment("Weight of obtaining Creative Flight when taking Compound V")
            .defineInRange("regularWeight.creative_flight", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_CRYOKINESIS = BUILDER
            .comment("Weight of obtaining Cryokinesis (ice) power when taking Compound V")
            .defineInRange("regularWeight.cryokinesis", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_DENSITY = BUILDER
            .comment("Weight of obtaining Density Manipulation when taking Compound V")
            .defineInRange("regularWeight.density", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SLIME = BUILDER
            .comment("Weight of obtaining Slime when taking Compound V")
            .defineInRange("regularWeight.slime", 4, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ENHANCED_REGEN = BUILDER
            .comment("Weight of obtaining Enhanced Regeneration when taking Compound V")
            .defineInRange("regularWeight.enhanced_regen", 8, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ENLARGE = BUILDER
            .comment("Weight of obtaining Enlarge powers when taking Compound V (requires Pehkui)")
            .defineInRange("regularWeight.enlarge", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_EXPLOSIVE = BUILDER
            .comment("Weight of obtaining Explosive power when taking Compound V")
            .defineInRange("regularWeight.explosive", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_FORCEFIELD = BUILDER
            .comment("Weight of obtaining Forcefield (shield bubble) when taking Compound V")
            .defineInRange("regularWeight.forcefield", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_GENERIC = BUILDER
            .comment("Weight of obtaining Generic effect (No extra abilities) when taking Compound V")
            .defineInRange("regularWeight.generic", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_HEAD_POP = BUILDER
            .comment("Weight of obtaining Head Pop (Blood Manipulation) when taking Compound V")
            .defineInRange("regularWeight.head_pop", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_HEALING = BUILDER
            .comment("Weight of obtaining Healing power when taking Compound V")
            .defineInRange("regularWeight.healing", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INSTAKILL = BUILDER
            .comment("Weight of obtaining Instakill power when taking Compound V")
            .defineInRange("regularWeight.instakill", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVINCIBLE = BUILDER
            .comment("Weight of obtaining Invincibility when taking Compound V")
            .defineInRange("regularWeight.invincible", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVISIBILITY = BUILDER
            .comment("Weight of obtaining Invisibility when taking Compound V")
            .defineInRange("regularWeight.invisibility", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LASER_EYES_ADVANCED = BUILDER
            .comment("Weight of obtaining Advanced (Homelander) Laser Eyes when taking Compound V")
            .defineInRange("regularWeight.laser_eyes_advanced", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LASER_EYES_BASIC = BUILDER
            .comment("Weight of obtaining Basic Laser Eyes when taking Compound V")
            .defineInRange("regularWeight.laser_eyes_basic", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LEAP = BUILDER
            .comment("Weight of obtaining Leap (Queen Maeve) when taking Compound V")
            .defineInRange("regularWeight.leap", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LEVITATION = BUILDER
            .comment("Weight of obtaining Levitation-based flight when taking Compound V")
            .defineInRange("regularWeight.levitation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LIFESTEAL = BUILDER
            .comment("Weight of obtaining Lifesteal power when taking Compound V")
            .defineInRange("regularWeight.lifesteal", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LUCK = BUILDER
            .comment("Weight of obtaining Luck when taking Compound V (comes in 3 levels)")
            .defineInRange("regularWeight.luck", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_MIMIC = BUILDER
            .comment("Weight of obtaining Mimic (power copying) when taking Compound V")
            .defineInRange("regularWeight.mimic", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_MIND_CONTROL = BUILDER
            .comment("Weight of obtaining Mind Control power when taking Compound V (experimental, currently disabled)")
            .defineInRange("regularWeight.mind_control", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_NIGHT_VISION = BUILDER
            .comment("Weight of obtaining Night Vision when taking Compound V")
            .defineInRange("regularWeight.night_vision", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_NULLIFY = BUILDER
            .comment("Weight of obtaining Nullify (power-suppression aura) when taking Compound V")
            .defineInRange("regularWeight.nullify", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_PETRIFYING_GAZE = BUILDER
            .comment("Weight of obtaining Petrifying Gaze when taking Compound V")
            .defineInRange("regularWeight.petrifying_gaze", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_POWER_ABSORPTION = BUILDER
            .comment("Weight of obtaining Power Absorption (Powerplex) when taking Compound V")
            .defineInRange("regularWeight.power_absorption", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_PROJECTILE_IMMUNITY = BUILDER
            .comment("Weight of obtaining Projectile Immunity (Rubber Body) when taking Compound V")
            .defineInRange("regularWeight.projectile_immunity", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_PYROKINESIS = BUILDER
            .comment("Weight of obtaining Pyrokinesis (fire) power when taking Compound V")
            .defineInRange("regularWeight.pyrokinesis", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SHRINK = BUILDER
            .comment("Weight of obtaining Shrink powers when taking Compound V (requires Pehkui)")
            .defineInRange("regularWeight.shrink", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SIZE_CONTROL = BUILDER
            .comment("Weight of obtaining Size Control (Advanced) when taking Compound V")
            .defineInRange("regularWeight.sizeControlAdvanced", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SONIC_SCREAM = BUILDER
            .comment("Weight of obtaining Sonic Scream when taking Compound V")
            .defineInRange("regularWeight.sonic_scream", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SPEEDSTER = BUILDER
            .comment("Weight of obtaining Speedster powers when taking Compound V")
            .defineInRange("regularWeight.speedster", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SPIDER = BUILDER
            .comment("Weight of obtaining the Spider power from V1 (0 = never).")
            .defineInRange("regularWeight.spider", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_STAR_POWER = BUILDER
            .comment("Weight of obtaining Star Power when taking Compound V (experimental)")
            .defineInRange("regularWeight.star_power", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_STORMFRONT = BUILDER
            .comment("Weight of Stormfront in the REGULAR Compound V pool. Default 0 (Stormfront is V1-exclusive); its V1 weight is v1_weight_stormfront.")
            .defineInRange("regularWeight.stormfront", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_TELEKINESIS = BUILDER
            .comment("Weight of obtaining Telekinesis power when taking Compound V")
            .defineInRange("regularWeight.telekinesis", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_TELEPORT = BUILDER
            .comment("Weight of obtaining Teleportation power when taking Compound V")
            .defineInRange("regularWeight.teleportation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_WATER = BUILDER
            .comment("Weight of obtaining Water Powers when taking Compound V")
            .defineInRange("regularWeight.water_power", 5, 0, Integer.MAX_VALUE);

    // ===== FAILURE (NEGATIVE) POOL WEIGHTS =====
    private static final ForgeConfigSpec.IntValue WEIGHT_FAIL_BLINDNESS = BUILDER
            .comment("Weight of the Blindness failure outcome")
            .defineInRange("failureWeight.blindness", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_FAIL_FLOATING = BUILDER
            .comment("Weight of the Infinite Levitation (Floating) failure outcome")
            .defineInRange("failureWeight.floating", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_FAIL_MAGNETISM = BUILDER
            .comment("Weight of the Magnetism failure outcome")
            .defineInRange("failureWeight.magnetism", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_FAIL_SLOWNESS = BUILDER
            .comment("Weight of the Slowness failure outcome")
            .defineInRange("failureWeight.slowness", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_FAIL_UNCONTROLLED_TELEPORT = BUILDER
            .comment("Weight of the Uncontrolled Teleport failure outcome")
            .defineInRange("failureWeight.uncontrolledTeleport", 15, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_UNCONTROLLED_EXPLOSION = BUILDER
            .comment("Weight of the Uncontrolled Explosion failure outcome")
            .defineInRange("failureWeight.uncontrolledExplosion", 6, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_HEAD_POP_FAILURE = BUILDER
            .comment("Weight of the Uncontrolled Head Pop failure outcome")
            .defineInRange("failureWeight.uncontrolledHeadPop", 6, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_UNCONTROLLED_SIZE = BUILDER
            .comment("Weight of the Uncontrolled Size failure outcome")
            .defineInRange("failureWeight.uncontrolledSize", 8, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_WITHER = BUILDER
            .comment("Weight of the Wither failure outcome")
            .defineInRange("failureWeight.wither", 8, 0, Integer.MAX_VALUE);

    // ===== V1 POOL WEIGHTS (full roster; powers not meant for V1 default to 0) =====
    private static final ForgeConfigSpec.IntValue V1_W_AIMLOCK = BUILDER
            .comment("V1 pool weight: Aimlock (disabled by default)")
            .defineInRange("v1Weight.aimlock", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_ATOM_CHARGING = BUILDER
            .comment("V1 pool weight: Atom Charging (disabled by default)")
            .defineInRange("v1Weight.atom_charging", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_BERSERKER = BUILDER
            .comment("V1 pool weight: Berserker (disabled by default)")
            .defineInRange("v1Weight.berserker", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_CHEST_BLAST = BUILDER
            .comment("V1 pool weight: Chest Blast")
            .defineInRange("v1Weight.chest_blast", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_CREATIVE_FLIGHT = BUILDER
            .comment("V1 pool weight: Creative Flight")
            .defineInRange("v1Weight.creative_flight", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_CRYOKINESIS = BUILDER
            .comment("V1 pool weight: Cryokinesis (disabled by default)")
            .defineInRange("v1Weight.cryokinesis", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_DEEP = BUILDER
            .comment("V1 pool weight: Deep (Water) (disabled by default)")
            .defineInRange("v1Weight.deep", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_DENSITY = BUILDER
            .comment("V1 pool weight: Density (disabled by default)")
            .defineInRange("v1Weight.density", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SLIME = BUILDER
            .comment("V1 pool weight: Slime (disabled by default)")
            .defineInRange("v1Weight.slime", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_ENHANCED_REGEN = BUILDER
            .comment("V1 pool weight: Enhanced Regen (disabled by default)")
            .defineInRange("v1Weight.enhanced_regen", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_ENLARGE = BUILDER
            .comment("V1 pool weight: Enlarge (disabled by default)")
            .defineInRange("v1Weight.enlarge", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_EXPLOSIVE = BUILDER
            .comment("V1 pool weight: Explosive (disabled by default)")
            .defineInRange("v1Weight.explosive", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_FORCEFIELD = BUILDER
            .comment("V1 pool weight: Forcefield (disabled by default)")
            .defineInRange("v1Weight.forcefield", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_GENERIC = BUILDER
            .comment("V1 pool weight: Generic (disabled by default)")
            .defineInRange("v1Weight.generic", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_HEAD_POP = BUILDER
            .comment("V1 pool weight: Head Pop")
            .defineInRange("v1Weight.head_pop", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_HEALING = BUILDER
            .comment("V1 pool weight: Healing (disabled by default)")
            .defineInRange("v1Weight.healing", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_INSTAKILL = BUILDER
            .comment("V1 pool weight: Instakill")
            .defineInRange("v1Weight.instakill", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_INVINCIBLE = BUILDER
            .comment("V1 pool weight: Invincible")
            .defineInRange("v1Weight.invincible", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_INVISIBILITY = BUILDER
            .comment("V1 pool weight: Invisibility (disabled by default)")
            .defineInRange("v1Weight.invisibility", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LASER_EYES_ADVANCED = BUILDER
            .comment("V1 pool weight: Advanced Laser Eyes")
            .defineInRange("v1Weight.laser_eyes_advanced", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LASER_EYES_BASIC = BUILDER
            .comment("V1 pool weight: Basic Laser Eyes (disabled by default)")
            .defineInRange("v1Weight.laser_eyes_basic", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LEAP = BUILDER
            .comment("V1 pool weight: Leap (disabled by default)")
            .defineInRange("v1Weight.leap", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LEVITATION = BUILDER
            .comment("V1 pool weight: Levitation (disabled by default)")
            .defineInRange("v1Weight.levitation", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LIFESTEAL = BUILDER
            .comment("V1 pool weight: Lifesteal (disabled by default)")
            .defineInRange("v1Weight.lifesteal", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_LUCK = BUILDER
            .comment("V1 pool weight: Luck (disabled by default)")
            .defineInRange("v1Weight.luck", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_MIMIC = BUILDER
            .comment("V1 pool weight: Mimic (disabled by default)")
            .defineInRange("v1Weight.mimic", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_MIND_CONTROL = BUILDER
            .comment("V1 pool weight: Mind Control")
            .defineInRange("v1Weight.mind_control", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_NIGHT_VISION = BUILDER
            .comment("V1 pool weight: Night Vision (disabled by default)")
            .defineInRange("v1Weight.night_vision", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_NULLIFY = BUILDER
            .comment("V1 pool weight: Nullify (disabled by default)")
            .defineInRange("v1Weight.nullify", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_PETRIFYING_GAZE = BUILDER
            .comment("V1 pool weight: Petrifying Gaze (disabled by default)")
            .defineInRange("v1Weight.petrifying_gaze", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_POWER_ABSORPTION = BUILDER
            .comment("V1 pool weight: Power Absorption (disabled by default)")
            .defineInRange("v1Weight.power_absorption", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_PROJECTILE_IMMUNITY = BUILDER
            .comment("V1 pool weight: Projectile Immunity (disabled by default)")
            .defineInRange("v1Weight.projectile_immunity", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_PYROKINESIS = BUILDER
            .comment("V1 pool weight: Pyrokinesis (disabled by default)")
            .defineInRange("v1Weight.pyrokinesis", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SHRINK = BUILDER
            .comment("V1 pool weight: Shrink (disabled by default)")
            .defineInRange("v1Weight.shrink", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SIZE_CONTROL_ADVANCED = BUILDER
            .comment("V1 pool weight: Size Control Advanced")
            .defineInRange("v1Weight.size_control_advanced", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SONIC_SCREAM = BUILDER
            .comment("V1 pool weight: Sonic Scream (disabled by default)")
            .defineInRange("v1Weight.sonic_scream", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SPEEDSTER = BUILDER
            .comment("V1 pool weight: Speedster")
            .defineInRange("v1Weight.speedster", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_SPIDER = BUILDER
            .comment("V1 pool weight: Spider (disabled by default)")
            .defineInRange("v1Weight.spider", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_STAR_POWER = BUILDER
            .comment("V1 pool weight: Star Power")
            .defineInRange("v1Weight.star_power", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_STORMFRONT = BUILDER
            .comment("V1 pool weight: Stormfront")
            .defineInRange("v1Weight.stormfront", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_TELEKINESIS = BUILDER
            .comment("V1 pool weight: Telekinesis (disabled by default)")
            .defineInRange("v1Weight.telekinesis", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue V1_W_TELEPORT = BUILDER
            .comment("V1 pool weight: Teleport")
            .defineInRange("v1Weight.teleport", 2, 0, Integer.MAX_VALUE);

    // ===== MOB UNIVERSAL POOL WEIGHTS =====
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_CREATIVE_FLIGHT = BUILDER
            .comment("Universal mob-pool base weight: Creative Flight")
            .defineInRange("mobWeight.creative_flight", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_CRYOKINESIS = BUILDER
            .comment("Universal mob-pool base weight: Cryokinesis")
            .defineInRange("mobWeight.cryokinesis", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_ENHANCED_REGEN = BUILDER
            .comment("Universal mob-pool base weight: Enhanced Regen")
            .defineInRange("mobWeight.enhanced_regen", 3, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_ENLARGE = BUILDER
            .comment("Universal mob-pool base weight: Enlarge (Pehkui)")
            .defineInRange("mobWeight.enlarge", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_SIZE_CONTROL_ADVANCED = BUILDER
            .comment("Universal mob-pool base weight: Advanced Size Control (Pehkui) - adaptive AI",
                     "that shrinks to ambush/escape and grows to attack.")
            .defineInRange("mobWeight.sizeControlAdvanced", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_HEALING = BUILDER
            .comment("Universal mob-pool base weight: Healing")
            .defineInRange("mobWeight.healing", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_INVISIBILITY = BUILDER
            .comment("Universal mob-pool base weight: Invisibility")
            .defineInRange("mobWeight.invisibility", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_LASER_BASIC = BUILDER
            .comment("Universal mob-pool base weight: Laser Eyes (basic)")
            .defineInRange("mobWeight.laser_basic", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_LEAP = BUILDER
            .comment("Universal mob-pool base weight: Leap")
            .defineInRange("mobWeight.leap", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_SLIME = BUILDER
            .comment("Universal mob-pool base weight: Slime")
            .defineInRange("mobWeight.slime", 2, 0, Integer.MAX_VALUE);
    // ===================================================================
    //  General - Loot & Structures
    // ===================================================================
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

    // ===================================================================
    //  General - Drink / Death / Virus
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_BAD_EFFECT_CHANCE = BUILDER
            .comment("Chance of getting a bad outcome when taking Compound V (Setting this to 0 will disable it)")
            .defineInRange("badReactionChance", 0.1, 0, 1);
    private static final ForgeConfigSpec.BooleanValue IRRADIATION_WEAKENS_SUPES = BUILDER
            .comment("If true, Alex's Caves irradiation WEAKENS Compound V holders: while irradiated",
                     "at/above irradiationWeakenMinLevel, all their powers (active + passive) are",
                     "suppressed, kryptonite-style. Only applies when Alex's Caves is loaded.")
            .define("irradiationWeakensSupes", false);
    private static final ForgeConfigSpec.IntValue IRRADIATION_WEAKEN_MIN_LEVEL = BUILDER
            .comment("Minimum Irradiated amplifier (0 = Irradiated I, 1 = II, 2 = III) required to",
                     "suppress powers. Default 1 so ambient/low-level irradiation doesn't weaken a",
                     "supe - only stronger external irradiation does.")
            .defineInRange("irradiationWeakenMinLevel", 1, 0, 10);
    private static final ForgeConfigSpec.BooleanValue LEVEL_UP_ON_DRINK = BUILDER
            .comment("If true, drinking another permanent Compound V while already powered raises the level of your current effect(s) by 1, up to their max level.")
            .define("levelUpOnDrink", true);
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_PLAYER_POWERS = BUILDER.define("virusDisablesPlayerPowers", true);
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_MOB_POWERS = BUILDER.define("virusDisablesMobPowers", true);

    // ===================================================================
    //  General - Multi-Power & Temp V
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue TEMP_V_FROM_WANDERING_TRADER = BUILDER
            .comment("Whether the Wandering Trader can rarely sell Temp V")
            .define("temp_v_from_trader", true);
    private static final ForgeConfigSpec.IntValue TEMP_V_DURATION = BUILDER
            .comment("Duration (in ticks) of Temp V's effects")
            .defineInRange("tempVDuration", 24000, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue TEMP_V_BAD_REACTION_CHANCE = BUILDER
            .comment("Chance of getting a bad outcome when taking Temp V (0 = disabled, 0.1 = 10%)")
            .defineInRange("tempVBadReactionChance", 0.0, 0, 1);
    private static final ForgeConfigSpec.BooleanValue ENABLE_MULTI_POWERS = BUILDER.comment("Allow Compound V to grant multiple powers").define("enableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("multiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue TEMP_V_ENABLE_MULTI_POWERS = BUILDER.define("tempVEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue TEMP_V_MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("tempVMultiPowerMaxCount", 2, 1, 3);

    // ===================================================================
    //  General - V1 Serum
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue V1_LEVEL_UP_MAXED = BUILDER
            .comment("If true, using V1 on a multi-level power that is ALREADY at max level pushes it one level beyond its normal max (overcharge). Disabled by default.")
            .define("v1LevelUpMaxed", false);
    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> V1_UPGRADE_PATHS = BUILDER
            .comment("Power-to-power upgrades when drinking V1: drinking V1 while holding the LEFT",
                     "power promotes it to the RIGHT power (granted at max level) instead of just",
                     "maxing the left power's own level. Format: \"from_id->to_id\" using the effect",
                     "registry path (no namespace = compound_v). Examples below. Remove a line to",
                     "disable that upgrade; add lines for new ones.")
            .defineList("v1UpgradePaths", java.util.List.of(
                    "energy_absorption->stormfront",
                    "shrink->size_control_advanced",
                    "enlarge->size_control_advanced",
                    "laser_eyes_basic->laser_eyes_advanced",
                    "levitation->creative_flight"
            ), o -> o instanceof String s && s.contains("->"));

    // ===================================================================
    //  General - Player Pickup / Carry
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue PLAYER_PICKUP_ENABLED = BUILDER
            .comment("Allow a larger player to pick up a smaller player (sneak + right-click). The",
                     "smaller player rides on the carrier; sneak or right-click again to set down.")
            .define("playerPickupEnabled", true);
    private static final ForgeConfigSpec.BooleanValue PLAYER_PICKUP_REQUIRES_COMPOUND_V = BUILDER
            .comment("If true, player pickup only works when at least one of the two players (carrier",
                     "OR the one being picked up) has a Compound V power. If false, any player big",
                     "enough can pick up a smaller one.")
            .define("playerPickupRequiresCompoundV", true);
    private static final ForgeConfigSpec.DoubleValue PLAYER_PICKUP_SIZE_RATIO = BUILDER
            .comment("How much bigger (by Pehkui scale) the carrier must be than the target to pick",
                     "them up. 1.5 = carrier must be at least 50% larger. Requires Pehkui for scales to",
                     "differ; without it all players are scale 1.0 and nobody qualifies.")
            .defineInRange("playerPickupSizeRatio", 1.5, 1.0, 10.0);

    // ===================================================================
    //  Laser Eyes
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue LASER_BASIC_PUSH_ENABLED = BUILDER.define("laserBasicPushEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_PUSH_STRENGTH = BUILDER.defineInRange("laserBasicPushStrength", 0.02, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_SHIELD_PUSH_MULTIPLIER = BUILDER.defineInRange("laserBasicShieldPushMultiplier", 3.0, 0.0, 20.0);
    private static final ForgeConfigSpec.BooleanValue LASER_ADVANCED_PUSH_ENABLED = BUILDER.define("laserAdvancedPushEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_PUSH_STRENGTH = BUILDER.defineInRange("laserAdvancedPushStrength", 0.04, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_SHIELD_PUSH_MULTIPLIER = BUILDER.defineInRange("laserAdvancedShieldPushMultiplier", 4.0, 0.0, 20.0);
    private static final ForgeConfigSpec.IntValue LASER_BASIC_DAMAGE_TICK_RATE = BUILDER.defineInRange("laserBasicDamageTickRate", 1, 1, 40);
    private static final ForgeConfigSpec.BooleanValue LASER_DISABLED_WHILE_MOVING = BUILDER
            .comment("If true, lasers will NOT fire (no damage/breaking) while the player is moving",
                     "at a decent speed (running/sprinting) - they only show the harmless intimidation",
                     "glow until you slow down. A drawback: you can't laser on the move. Off by default.")
            .define("laserDisabledWhileMoving", false);
    private static final ForgeConfigSpec.DoubleValue LASER_FIRST_PERSON_OPACITY = BUILDER
            .comment("Opacity of YOUR OWN laser-eye beams in FIRST PERSON only (1.0 = full, 0.0 =",
                     "invisible). Lets you make your lasers less obtrusive over your own view.",
                     "Third person and other players' lasers are completely unaffected.")
            .defineInRange("laserFirstPersonOpacity", 1.0, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue LASER_INTIMIDATION_FIRST_PERSON = BUILDER
            .comment("Whether the harmless intimidation GLOW (the short stub beams when the laser is",
                     "at minimum intensity) renders in FIRST PERSON. Off by default so it doesn't",
                     "clutter your own view; third person and other players always see it.")
            .define("laserIntimidationFirstPerson", false);
    private static final ForgeConfigSpec.DoubleValue LASER_MOVE_SPEED_THRESHOLD = BUILDER
            .comment("Horizontal speed (blocks/tick) above which lasers are suppressed when",
                     "laserDisabledWhileMoving is true. Normal walking is ~0.13, sprinting ~0.17.",
                     "0.1 catches running but allows slow sneaking/aiming.")
            .defineInRange("laserMoveSpeedThreshold", 0.1, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue LASER_INTENSITY_ADJUSTABLE = BUILDER
            .comment("If true, lasers can be scrolled between intimidation glow (0) and full power (1).",
                     "If false, laser intensity control is disabled and lasers ALWAYS fire at full blast",
                     "(scroll does nothing for lasers).")
            .define("laserIntensityAdjustable", true);
    private static final ForgeConfigSpec.DoubleValue LASER_INTENSITY_SCROLL_STEP = BUILDER
            .comment("How much laser intensity (power level) changes per scroll notch while holding V.",
                     "Intensity ranges 0.0 (harmless glow, intimidation only) to 1.0 (full damage +",
                     "block breaking). 0.2 = five notches from off to full.")
            .defineInRange("laserIntensityScrollStep", 0.2, 0.02, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BREAK_CRITICAL = BUILDER
            .comment("Intensity (0-1) at which laser BLOCK BREAKING begins. Below this, no breaking.",
                     "From this point up to full intensity (1.0), the break chance is linearly",
                     "interpolated from 0 up to the configured laserBlockBreakChance. e.g. 0.85 means",
                     "breaking only in the top 15% of the power range, ramping in.")
            .defineInRange("laserBreakCriticalIntensity", 0.85, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_FIRE_CRITICAL = BUILDER
            .comment("Intensity (0-1) at which laser FIRE STARTING / soft-block burn-through begins.",
                     "Below this, no fire/burn-through. From this point to full intensity the chance is",
                     "linearly interpolated from 0 up to the configured fire/burn chance.")
            .defineInRange("laserFireCriticalIntensity", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue LASER_IGNITE_ENABLED = BUILDER
            .comment("Whether the laser can start fires and prime TNT (separate from soft-block",
                     "burn-through). Igniting happens at a lower intensity than burn-through.")
            .define("laserIgniteEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_IGNITE_CHANCE = BUILDER
            .comment("Per-tick chance (0-1) that the laser ignites the block/entity it is hitting,",
                     "at full intensity. Scaled down by how far intensity is above the ignite point,",
                     "so lower intensity ignites less often. Set to 0 to disable ignition entirely",
                     "even with laserIgniteEnabled on; lower it to make fires rare.")
            .defineInRange("laserIgniteChance", 0.05, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_IGNITE_CRITICAL = BUILDER
            .comment("Intensity (0-1) at which the laser starts fires / ignites entities / primes",
                     "TNT. Lower than laserFireCriticalIntensity so ignition kicks in before the",
                     "hotter burn-through does. Chance ramps from 0 at this point to full intensity.")
            .defineInRange("laserIgniteCriticalIntensity", 0.3, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue LASER_IGNITE_ENTITY_SECONDS = BUILDER
            .comment("Seconds an entity caught in the beam burns for, once ignite intensity is met.")
            .defineInRange("laserIgniteEntitySeconds", 4, 0, 60);
    private static final ForgeConfigSpec.IntValue LASER_ADVANCED_DAMAGE_TICK_RATE = BUILDER.defineInRange("laserAdvancedDamageTickRate", 1, 1, 40);
    private static final ForgeConfigSpec.BooleanValue LASER_BASIC_BREAK_BLOCKS = BUILDER
            .comment("Whether basic laser eyes break blocks along the beam (like chest blast)")
            .define("laserBasicBreakBlocks", false);
    private static final ForgeConfigSpec.BooleanValue LASER_ADVANCED_BREAK_BLOCKS = BUILDER
            .comment("Whether advanced laser eyes break blocks along the beam")
            .define("laserAdvancedBreakBlocks", true);
    private static final ForgeConfigSpec.DoubleValue LASER_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Unused. Laser block breaking is progressive and hardness-based; see",
                     "laserBreakSpeed and the hardness/resistance weight settings.")
            .defineInRange("laserBlockBreakChance", 0.15, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BREAK_SPEED = BUILDER
            .comment("Overall laser mining speed. Higher = blocks break faster. This is the top-level",
                     "modifier: fraction of a 'toughness-1' block mined per damage tick before the",
                     "hardness/resistance division. Raise for faster carving, lower for slower.")
            .defineInRange("laserBreakSpeed", 0.5, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BREAK_HARDNESS_WEIGHT = BUILDER
            .comment("How much a block's HARDNESS slows laser breaking. Time-to-break scales with",
                     "(hardnessWeight * hardness + resistanceWeight * blastResistance). Set the",
                     "resistance weight to 0 to make breaking depend on hardness only.")
            .defineInRange("laserBreakHardnessWeight", 1.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BREAK_RESISTANCE_WEIGHT = BUILDER
            .comment("How much a block's BLAST RESISTANCE slows laser breaking (obsidian etc. are far",
                     "tougher by resistance than hardness). 0 = ignore resistance, break purely on",
                     "hardness.")
            .defineInRange("laserBreakResistanceWeight", 0.1, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BREAK_DECAY = BUILDER
            .comment("How fast a partially-broken block RECOVERS once the beam moves off it (progress",
                     "lost per tick). Higher = you must keep the beam steady; 0 = progress never",
                     "decays (blocks stay cracked until finished).")
            .defineInRange("laserBreakDecay", 0.05, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue LASER_HEARTBEAT_ENABLED = BUILDER
            .comment("Advanced Laser Eyes 'predator sense': the holder hears the heartbeat of nearby",
                     "low-health players. Only the holder hears it (private, directional at the",
                     "target). Players only, not mobs.")
            .define("laserHeartbeatEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_HEARTBEAT_RANGE = BUILDER
            .comment("How close (blocks) a wounded player must be for the holder to hear their",
                     "heartbeat. Heard through walls (predator sense).")
            .defineInRange("laserHeartbeatRange", 24.0, 1.0, 128.0);
    private static final ForgeConfigSpec.DoubleValue LASER_HEARTBEAT_HEALTH_THRESHOLD = BUILDER
            .comment("Health FRACTION (0..1) at or below which a player's heartbeat becomes audible.",
                     "0.4 = 40% health.")
            .defineInRange("laserHeartbeatHealthThreshold", 0.4, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue LASER_HEARTBEAT_SLOW_INTERVAL = BUILDER
            .comment("Ticks between beats when a target is right at the health threshold (slowest).",
                     "20 = 1s.")
            .defineInRange("laserHeartbeatSlowInterval", 24, 1, 200);
    private static final ForgeConfigSpec.IntValue LASER_HEARTBEAT_FAST_INTERVAL = BUILDER
            .comment("Ticks between beats when a target is near death (fastest). The beat quickens",
                     "from the slow interval to this as their health falls.")
            .defineInRange("laserHeartbeatFastInterval", 8, 1, 200);
    private static final ForgeConfigSpec.BooleanValue LASER_BLOCK_BREAK_DROPS = BUILDER
            .comment("Whether laser block breaking drops items (false = less lag)")
            .define("laserBlockBreakDrops", false);
    private static final ForgeConfigSpec.BooleanValue LASER_COLOR_COMMAND_OP_ONLY = BUILDER
            .comment("Restrict /lasercolor to operators only (even for setting your own color)")
            .define("laserColorCommandOpOnly", true);

    // ===================================================================
    //  Chest Blast
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_BLOCK_BREAK_DROPS = BUILDER
            .comment("Whether chest blast BEAM block breaking drops items (false = less lag)")
            .define("chestBlastBlockBreakDrops", false);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_NOVA_BLOCK_BREAK_DROPS = BUILDER
            .comment("Whether the chest blast NOVA explosion drops broken blocks as items (false = less lag)")
            .define("chestBlastNovaBlockBreakDrops", false);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_NOVA_ENABLED = BUILDER
            .comment("If true, sneak+V triggers the nova burst. If false, sneak+V fires a normal chest blast beam instead.")
            .define("chestBlastNovaEnabled", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_NOVA_BREAKS_BLOCKS = BUILDER
            .comment("Whether the chest blast nova destroys blocks at all")
            .define("chestBlastNovaBreaksBlocks", true);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_GUARANTEED_BREAK_FRACTION = BUILDER
            .comment("Fraction (0-1) of the nova radius within which block destruction is guaranteed. Outside this, break chance falls off toward the edge.")
            .defineInRange("chestBlastNovaGuaranteedBreakFraction", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BEAM_DAMAGE = BUILDER
            .comment("Damage per tick of the Chest Blast beam (fires 20x/sec, so 2.0 = 40 dps before armor)")
            .defineInRange("chestBlastBeamDamage", 3.0, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_FORCEFIELD_DAMAGE = BUILDER
            .comment("BONUS damage per tick dealt straight to an active Forcefield's health when the",
                     "Chest Blast beam hits it (on top of the beam's normal damage the field absorbs).",
                     "A forcefield BLOCKS the beam's power-strip but takes heavy damage - this makes",
                     "chest blast collapse a shield quickly. Fires ~20x/sec.")
            .defineInRange("chestBlastForcefieldDamage", 4.0, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_FORCEFIELD_KNOCKBACK = BUILDER
            .comment("Horizontal knockback applied to a Forcefield holder hit by the Chest Blast beam.")
            .defineInRange("chestBlastForcefieldKnockback", 0.8, 0, 10.0);
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
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_BLOCKED_BY_WALLS = BUILDER.define("chestBlastBlockedByWalls", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_POWERS = BUILDER.define("chestBlastStripsPowers", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_SHIELD_BLOCKS_STRIP = BUILDER.define("chestBlastShieldBlocksStrip", true);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_RADIUS = BUILDER
            .comment("Radius of the Soldier Boy nova depower/power-strip effect (sneak + V)")
            .defineInRange("chestBlastNovaRadius", 8.0, 2.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_POWER = BUILDER
            .comment("Explosion power of the nova burst (vanilla TNT is 4.0; higher = bigger blast/damage/crater)")
            .defineInRange("chestBlastNovaPower", 10.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_DAMAGE = BUILDER
            .comment("Damage dealt by the nova burst to powered entities")
            .defineInRange("chestBlastNovaDamage", 15.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_KNOCKBACK = BUILDER
            .comment("Knockback strength of the nova burst")
            .defineInRange("chestBlastNovaKnockback", 2.5, 0.0, 10.0);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_NOVA_CHARGE_TIME = BUILDER
            .comment("Charge time in ticks for nova burst (sneak + hold V). Beam and nova share the same cooldown (chestBlastCooldown).")
            .defineInRange("chestBlastNovaChargeTime", 200, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_RANGE = BUILDER
            .comment("Range (in blocks) of the Chest Blast beam")
            .defineInRange("chestBlastRange", 32, 8, 256);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Chance per tick per block of the Chest Blast beam destroying blocks in its path (0 = disabled). WARNING: Values above 0 cause significant lag due to per-tick block iteration across the beam cone.")
            .defineInRange("chestBlastBlockBreakChance", 0.0, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_INVINCIBLE = BUILDER
            .comment("Whether the Chest Blast beam can strip Invincibility (if false, Invincible blocks the strip)")
            .define("chestBlastStripsInvincible", true);

    // ===================================================================
    //  Spider
    // ===================================================================
    // private static final ForgeConfigSpec.IntValue WEIGHT_FORCEFIELD = ...
    // --- Spider web tuning (revamp) ---
    private static final ForgeConfigSpec.DoubleValue SPIDER_WEB_SPEED = BUILDER
            .comment("Launch speed of the web projectile").defineInRange("spiderWebSpeed", 3.6, 0.5, 12.0);
    private static final ForgeConfigSpec.BooleanValue SPIDER_WALL_CLIMB_ENABLED = BUILDER
            .comment("Hold SNEAK while a Spider holder is against a wall or beneath a ceiling to",
                     "cling and climb (gravity cancelled; WASD/look move you along the surface).",
                     "Release sneak to drop with your momentum intact.")
            .define("spiderWallClimbEnabled", true);
    private static final ForgeConfigSpec.DoubleValue SPIDER_CLIMB_SPEED = BUILDER
            .comment("Movement speed while wall/ceiling climbing (blocks per tick).")
            .defineInRange("spiderClimbSpeed", 0.18, 0.02, 1.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_CLIMB_STICK_GAP = BUILDER
            .comment("How close (blocks) the player's hitbox must be to a wall/ceiling face to",
                     "cling to it. Small = must be right up against it; larger = sticks from a small",
                     "gap (also smooths going around convex corners). 0.12 is snug.")
            .defineInRange("spiderClimbStickGap", 0.2, 0.0, 0.5);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_GROUND_LIFT = BUILDER
            .comment("Minimum upward velocity applied when reeling toward an anchor ABOVE you while",
                     "standing on the ground, so the reel can actually lift you off the floor",
                     "instead of being cancelled by ground friction. ~0.42 clears a jump's worth.")
            .defineInRange("spiderReelGroundLift", 0.42, 0.0, 3.0);
    private static final ForgeConfigSpec.BooleanValue SPIDER_CLIMB_LOOK_RELATIVE = BUILDER
            .comment("If true, wall climbing is directional/look-relative: W/S move along the way",
                     "you're looking on the wall and A/D perpendicular, so you climb diagonally in",
                     "the direction you aim. If false, axis-locked (W straight up, A/D flat sideways).")
            .define("spiderClimbLookRelative", true);
    private static final ForgeConfigSpec.BooleanValue SPIDER_CEILING_CLIMB_ENABLED = BUILDER
            .comment("Whether Spider holders can cling to and crawl across CEILINGS (separate from",
                     "wall climbing). Disabled for now while the ceiling crawl is being reworked.")
            .define("spiderCeilingClimbEnabled", false);
    private static final ForgeConfigSpec.BooleanValue SPIDER_WALL_JUMP_ENABLED = BUILDER
            .comment("Press the JUMP key while wall/ceiling climbing to launch off in your look",
                     "direction (momentum-carrying wall jump). Reuses space - no extra keybind.")
            .define("spiderWallJumpEnabled", true);
    private static final ForgeConfigSpec.DoubleValue SPIDER_WALL_JUMP_POWER = BUILDER
            .comment("Launch speed of a wall jump along your look direction.")
            .defineInRange("spiderWallJumpPower", 0.9, 0.1, 5.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_WALL_JUMP_LIFT = BUILDER
            .comment("Guaranteed upward boost added to every wall jump so it always pops off.")
            .defineInRange("spiderWallJumpLift", 0.42, 0.0, 3.0);
    private static final ForgeConfigSpec.BooleanValue SPIDER_RAYCAST_WEBBING = BUILDER
            .comment("If true, webbing uses an instant RAYCAST instead of a traveling projectile:",
                     "firing immediately anchors to the first block or mob along your look vector",
                     "(no projectile travel time, can't be dodged or miss due to your movement).",
                     "If false (default), the original projectile web is used. Note: the projectile",
                     "is what lets thrown webs arc and be dodged by mobs, so raycast mode is more of",
                     "a precise grappling style.")
            .define("spiderRaycastWebbing", false);
    private static final ForgeConfigSpec.DoubleValue SPIDER_WEB_FALL_COMPENSATION = BUILDER
            .comment("How much of the player's downward fall speed is added back into the web's",
                     "upward launch, so you can still hit ceilings while falling (the projectile",
                     "otherwise ignores your motion and barely climbs as you drop). 1.0 = fully",
                     "cancel your fall (web leaves you at full speed regardless of fall), 0 = off.")
            .defineInRange("spiderWebFallCompensation", 1.0, 0.0, 2.0);
    private static final ForgeConfigSpec.IntValue SPIDER_FIRE_COOLDOWN = BUILDER
            .comment("Minimum ticks between web shots (20 = 1s). Prevents spamming webs by holding V.").defineInRange("spiderFireCooldown", 10, 0, 200);
    private static final ForgeConfigSpec.IntValue SPIDER_MOB_WEB_STUCK_MAX_TICKS = BUILDER
            .comment("How long (ticks, 20 = 1s) a MOB-fired web may stay stuck before it self-",
                     "cleans. Mob webs have no release/reel controls, so without this a mob web that",
                     "sticks to a block or the player draws a strand forever. Player webs are exempt.")
            .defineInRange("spiderMobWebStuckMaxTicks", 200, 20, 12000);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MOB_WEB_TRAP_CHANCE = BUILDER
            .comment("Chance per eligible check that a Spider-power mob springs its cobweb TRAP",
                     "(ensnaring the target in temporary cobwebs) when off cooldown and in range.",
                     "Low by default so it's a rare surprise, not a constant lockdown. 0 disables it.")
            .defineInRange("spiderMobWebTrapChance", 0.05, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue SPIDER_MOB_WEB_TRAP_COOLDOWN = BUILDER
            .comment("Minimum ticks (20 = 1s) between cobweb-trap attempts for a Spider mob. Higher =",
                     "rarer. Default 700 (~35s).")
            .defineInRange("spiderMobWebTrapCooldown", 700, 20, 24000);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MIN_ROPE = BUILDER
            .comment("Shortest the swing rope can be reeled to").defineInRange("spiderMinRope", 3.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MAX_ROPE = BUILDER
            .comment("Longest the swing rope can be extended to").defineInRange("spiderMaxRope", 40.0, 4.0, 128.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_STEP = BUILDER
            .comment("How much rope length changes per scroll notch").defineInRange("spiderReelStep", 0.5, 0.1, 16.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_PULL = BUILDER
            .comment("Inward pull strength when reeling the rope shorter").defineInRange("spiderReelPull", 0.18, 0.0, 2.0);
    private static final ForgeConfigSpec.BooleanValue SPIDER_REEL_MASS_ENABLED = BUILDER
            .comment("If true, heavier latched mobs (by health + size) reel in more slowly. If",
                     "false, all mobs reel at the same rate (mass resistance off). Default off",
                     "while the reel feel is being tuned.")
            .define("spiderReelMassEnabled", false);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MOB_REEL_SPRING = BUILDER
            .comment("Spring strength pulling a latched mob back to the rope length (divided by the",
                     "mob's mass). Lower = gentler, more gradual reel.")
            .defineInRange("spiderMobReelSpring", 0.15, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MOB_REEL_MAX_SPEED = BUILDER
            .comment("Cap on how fast (blocks/tick) a latched mob is reeled inward, so even a big",
                     "rope-length change reels in smoothly instead of near-instantly.")
            .defineInRange("spiderMobReelMaxSpeed", 0.4, 0.05, 3.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_HEALTH_WEIGHT = BUILDER
            .comment("How much a latched mob's MAX HEALTH adds to its reel 'mass' (heavier = reels",
                     "in slower). Mass = 1 + maxHealth*this + sizeVolume*sizeWeight. 0 = health ignored.")
            .defineInRange("spiderReelHealthWeight", 0.05, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_SIZE_WEIGHT = BUILDER
            .comment("How much a latched mob's hitbox VOLUME (×Pehkui scale cubed) adds to its reel",
                     "'mass'. Bigger mobs reel in slower. 0 = size ignored.")
            .defineInRange("spiderReelSizeWeight", 0.4, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_MAX_MASS = BUILDER
            .comment("Cap on reel 'mass' so even the heaviest mob stays haulable (reel pull is",
                     "divided by mass). Higher = heavy mobs can get much harder to reel.")
            .defineInRange("spiderReelMaxMass", 8.0, 1.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_CONTROL = BUILDER
            .comment("How strongly A/D leans the swing left/right. Keep SMALL - it's a lean, not",
                     "full directional control, and should never overpower gravity.").defineInRange("spiderSwingControl", 0.03, 0.0, 0.5);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SWING_JUMP_ENABLED = BUILDER
            .comment("Press JUMP while swinging on a rope to release and launch off, keeping your",
                     "swing momentum plus an upward boost. Reuses space - no extra keybind.")
            .define("spiderSwingJumpEnabled", true);
    private static final ForgeConfigSpec.IntValue SPIDER_SWING_JUMP_GROUND_GRACE = BUILDER
            .comment("Ticks you must be airborne before a JUMP press counts as a swing-jump (rope",
                     "release + launch). This stops a jump made while standing on the ground from",
                     "cutting your web - instead the line stays tethered so you hop off the floor",
                     "INTO a swing on your existing line. 3 = ~0.15s.")
            .defineInRange("spiderSwingJumpGroundGrace", 3, 0, 40);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SWING_JUMP_REFIRES = BUILDER
            .comment("If true, jumping off a rope with space ALSO immediately fires a new web at",
                     "whatever you're looking at, so you flow straight into the next swing (easy",
                     "web-to-web ceiling traversal). If false, a rope jump just releases.")
            .define("spiderSwingJumpRefires", true);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_JUMP_MOMENTUM = BUILDER
            .comment("How much of your current swing velocity carries into the launch. 1.0 = keep",
                     "all of it, >1 = amplified launch, <1 = dampened.")
            .defineInRange("spiderSwingJumpMomentum", 1.1, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_JUMP_LIFT = BUILDER
            .comment("Guaranteed upward boost added to a rope jump so it always pops up, on top of",
                     "any upward momentum from the swing. ~0.42 is a vanilla jump's worth.")
            .defineInRange("spiderSwingJumpLift", 0.42, 0.0, 3.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_PUMP = BUILDER
            .comment("Momentum gained per tick by 'pumping' the swing: hold W to push along your",
                     "current swing direction and build speed (like pumping your legs), S to slow.",
                     "You must actively pump to go fast - hanging passively just gravity-swings.",
                     "Compounds with the swing, bounded by maxSwingSpeed. 0.04 is a steady build.")
            .defineInRange("spiderSwingPump", 0.04, 0.0, 0.5);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_GRAVITY = BUILDER
            .comment("Gravity applied to the pendulum swing each tick - the PRIMARY driver of the",
                     "swing, building momentum through the arc. ~0.06 feels weighty; vanilla fall",
                     "gravity is ~0.08.")
            .defineInRange("spiderSwingGravity", 0.06, 0.0, 0.3);
    private static final ForgeConfigSpec.DoubleValue SPIDER_WEB_GRAVITY_MULT = BUILDER
            .comment("Multiplier on the web projectile's gravity while in flight. Lower = the web",
                     "arcs less and flies straighter/farther. 1.0 = original, 0.9 = slightly floatier.")
            .defineInRange("spiderWebGravityMult", 0.9, 0.0, 2.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MAX_SWING_SPEED = BUILDER
            .comment("Maximum swing speed (caps momentum)").defineInRange("spiderMaxSwingSpeed", 2.6, 0.2, 6.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_BOOST = BUILDER
            .comment("Per-tick amplification of the along-the-arc (tangential) swing speed, so",
                     "swings build real momentum instead of just coasting on gravity. Keep this",
                     "SMALL (it compounds each tick) - 1.04 = +4%/tick, bounded by maxSwingSpeed.",
                     "1.0 = no boost (pure pendulum).")
            .defineInRange("spiderSwingBoost", 1.04, 1.0, 1.5);
    private static final ForgeConfigSpec.DoubleValue SPIDER_FLING_FORCE = BUILDER
            .comment("Launch force when flinging a reeled-in mob by punching it and releasing the web.")
            .defineInRange("spiderFlingForce", 2.5, 0.5, 10.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_FLING_DAMAGE = BUILDER
            .comment("Bonus damage dealt to a mob flung by the punch-and-release combo.")
            .defineInRange("spiderFlingDamage", 6.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SLAM_PITCH_THRESHOLD = BUILDER
            .comment("Look pitch (degrees down) above which a punch on a reeled mob SLAMS it into",
                     "the ground instead of flinging it forward. 35 = looking moderately down.")
            .defineInRange("spiderSlamPitchThreshold", 35.0, 0.0, 90.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SLAM_FORCE = BUILDER
            .comment("Downward velocity applied to a slammed mob.")
            .defineInRange("spiderSlamForce", 2.2, 0.5, 10.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SLAM_DAMAGE = BUILDER
            .comment("Damage dealt by a web slam (typically higher than a fling).")
            .defineInRange("spiderSlamDamage", 9.0, 0.0, 100.0);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SENSE_ENABLED = BUILDER
            .comment("Enable Spider-Sense: a passive danger warning (tingle) for Spider holders.")
            .define("spiderSenseEnabled", true);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SENSE_RADIUS = BUILDER
            .comment("Radius (blocks) scanned for threats by Spider-Sense.")
            .defineInRange("spiderSenseRadius", 16.0, 4.0, 64.0);
    private static final ForgeConfigSpec.IntValue SPIDER_SENSE_SCAN_INTERVAL = BUILDER
            .comment("Ticks between Spider-Sense threat scans (lower = more responsive, costlier).")
            .defineInRange("spiderSenseScanInterval", 5, 1, 40);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SENSE_DETECT_PROJECTILES = BUILDER
            .comment("Spider-Sense warns about incoming projectiles aimed at you.")
            .define("spiderSenseDetectProjectiles", true);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SENSE_DETECT_AGGRO = BUILDER
            .comment("Spider-Sense warns about hostile mobs targeting you.")
            .define("spiderSenseDetectAggro", true);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SENSE_DETECT_CREEPERS = BUILDER
            .comment("Spider-Sense warns about primed (fusing) creepers nearby.")
            .define("spiderSenseDetectCreepers", true);
    private static final ForgeConfigSpec.BooleanValue SPIDER_SENSE_DODGE_PAYOFF = BUILDER
            .comment("If true, reacting to a Spider-Sense warning grants brief damage reduction.")
            .define("spiderSenseDodgePayoff", true);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SENSE_DR = BUILDER
            .comment("Damage taken multiplier during the Spider-Sense reaction window (0.6 = 40% reduction).")
            .defineInRange("spiderSenseDamageMultiplier", 0.6, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue SPIDER_SENSE_WINDOW = BUILDER
            .comment("Ticks the Spider-Sense reaction (damage-reduction) window lasts after a warning.")
            .defineInRange("spiderSenseWindowTicks", 12, 1, 100);
    private static final ForgeConfigSpec.IntValue SPIDER_SENSE_COOLDOWN = BUILDER
            .comment("Minimum ticks between Spider-Sense warning cues (prevents tingle spam).")
            .defineInRange("spiderSenseCooldownTicks", 16, 0, 200);

    // ===================================================================
    //  Slime
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue SLIME_BOUNCE_FACTOR = BUILDER
            .comment("How much landing impact velocity is converted into an upward bounce while",
                     "Slime is active. 0.8 = bounce back at 80% of impact speed.")
            .defineInRange("slimeBounceFactor", 0.8, 0.0, 2.0);
    private static final ForgeConfigSpec.DoubleValue SLIME_MAX_BOUNCE = BUILDER
            .comment("Cap on bounce-up speed so huge falls don't launch you absurdly far.")
            .defineInRange("slimeMaxBounce", 1.4, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue SLIME_BOUNCE_MIN_IMPACT = BUILDER
            .comment("Minimum downward speed on landing (blocks/tick) needed to bounce. ~0.4",
                     "corresponds to falling more than ~1 block; shorter drops don't bounce.")
            .defineInRange("slimeBounceMinImpact", 0.4, 0.0, 2.0);
    private static final ForgeConfigSpec.IntValue SLIME_JUMP_AMPLIFIER = BUILDER
            .comment("Jump Boost amplifier while Slime is active (higher amplitude hops). 0 = JBI,",
                     "3 = much higher. Slime can't walk, so this is your main way to move.")
            .defineInRange("slimeJumpAmplifier", 3, 0, 10);
    private static final ForgeConfigSpec.IntValue SLIME_JUMP_DURATION = BUILDER
            .comment("Duration (ticks) of each Jump Boost application. Short and continuously",
                     "refreshed (like Speedster) so the boost is punchy, not lingering. 15 = 0.75s.")
            .defineInRange("slimeJumpDuration", 15, 2, 200);
    private static final ForgeConfigSpec.DoubleValue SLIME_HOP_SPEED = BUILDER
            .comment("Horizontal launch speed applied when you jump with a movement key held, so",
                     "jump-only movement actually carries you (you hop in the input direction).")
            .defineInRange("slimeHopSpeed", 0.55, 0.0, 3.0);
    private static final ForgeConfigSpec.BooleanValue SLIME_ALWAYS_ACTIVE = BUILDER
            .comment("If true, Slime mode is ALWAYS on while you hold the power - no V toggle needed,",
                     "and V can't turn it off. If false (default), Slime is toggled on/off with V.")
            .define("slimeAlwaysActive", false);
    private static final ForgeConfigSpec.DoubleValue SLIME_DAMAGE_TAKEN = BUILDER
            .comment("Damage multiplier taken while Slime is active (you're squishy and absorb",
                     "blows). 0.4 = take 40% damage.")
            .defineInRange("slimeDamageTaken", 0.4, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue SLIME_KNOCKBACK_TAKEN = BUILDER
            .comment("Knockback multiplier taken from melee/projectile hits while Slime is active",
                     "(you get flung far). 6.0 = six times normal knockback.")
            .defineInRange("slimeKnockbackTaken", 6.0, 1.0, 30.0);
    private static final ForgeConfigSpec.DoubleValue SLIME_EXPLOSION_KNOCKBACK = BUILDER
            .comment("Extra outward launch velocity applied to a slimed player hit by an EXPLOSION",
                     "(creeper, TNT). Explosions bypass the normal knockback event, so this is a",
                     "direct velocity shove on top of the blast's own knockback. ~2.5 sends you",
                     "flying; raise for absurd launches.")
            .defineInRange("slimeExplosionKnockback", 2.5, 0.0, 10.0);

    // ===================================================================
    //  Speedster
    // ===================================================================
    private static final ForgeConfigSpec.BooleanValue SPEEDSTER_SPEED_ATTACK = BUILDER
            .comment("Whether Speedster powers allow you to damage mobs while sprinting")
            .define("speedster_speed_attack", true);
    private static final ForgeConfigSpec.IntValue SPEEDSTER_SPEED_LEVELS_PER_AMP = BUILDER
            .comment("Speed effect levels added per Speedster amplifier (base Speed III at amp 0, +N per amp)")
            .defineInRange("speedsterSpeedLevelsPerAmp", 2, 0, 10);
    private static final ForgeConfigSpec.DoubleValue SPEEDSTER_SPRINT_DAMAGE = BUILDER
            .comment("Base contact damage dealt to nearby entities while sprinting (Speedster level",
                     ">1). NOTE: this can apply every tick while sprinting through a mob, so small",
                     "values add up fast.")
            .defineInRange("speedsterSprintDamage", 2.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue SPEEDSTER_SPRINT_DAMAGE_PER_AMP = BUILDER
            .comment("Extra sprint contact damage per Speedster amplifier level above 1.",
                     "Total = speedsterSprintDamage + this * (amplifier - 1).")
            .defineInRange("speedsterSprintDamagePerAmp", 1.5, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue SPEEDSTER_PLAYER_DAMAGE_MULT = BUILDER
            .comment("Multiplier on Speedster sprint contact damage when the target is a PLAYER",
                     "(1.0 = same as mobs, 0.5 = half). Lets you soften PvP without nerfing PvE.")
            .defineInRange("speedsterPlayerDamageMult", 0.5, 0.0, 1.0);

    // ===================================================================
    //  Pyrokinesis
    // ===================================================================
    private static final ForgeConfigSpec.IntValue PYRO_MAX_CHARGES = BUILDER
            .comment("Maximum number of fireballs a Pyrokinesis user can have stored")
            .defineInRange("pyroMaxCharges", 3, 1, 64);
    private static final ForgeConfigSpec.IntValue PYRO_CHARGE_REGEN_TICKS = BUILDER
            .comment("Ticks to regenerate one stored fireball (20 ticks = 1 second)")
            .defineInRange("pyroChargeRegenTicks", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue PYRO_CHARGE_ENABLED = BUILDER
            .comment("If true, holding V charges a bigger explosive fireball (release to fire).",
                     "If false, the charge-up is disabled and V is a simple tap that fires a basic",
                     "fireball immediately.")
            .define("pyroChargeEnabled", true);
    private static final ForgeConfigSpec.IntValue PYRO_MAX_CHARGE_TIME = BUILDER
            .comment("Ticks to fully charge a held fireball for maximum size/speed/explosion")
            .defineInRange("pyroMaxChargeTime", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue PYRO_MIN_EXPLOSION = BUILDER
            .comment("Explosion power of an uncharged (tapped) blaze fireball (0 = no explosion)")
            .defineInRange("pyroMinExplosion", 1.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue PYRO_MAX_EXPLOSION = BUILDER
            .comment("Explosion power of a fully-charged fireball")
            .defineInRange("pyroMaxExplosion", 4.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue PYRO_MIN_SPEED = BUILDER
            .comment("Projectile speed of an uncharged (tapped) fireball")
            .defineInRange("pyroMinSpeed", 1.2, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue PYRO_MAX_SPEED = BUILDER
            .comment("Projectile speed of a fully-charged fireball")
            .defineInRange("pyroMaxSpeed", 2.5, 0.1, 10.0);
    private static final ForgeConfigSpec.BooleanValue PYRO_FIREBALL_BREAKS_BLOCKS = BUILDER
            .comment("Whether charged fireball explosions destroy blocks")
            .define("pyroFireballBreaksBlocks", true);
    private static final ForgeConfigSpec.DoubleValue PYRO_FLAME_WAVE_RADIUS = BUILDER
            .comment("Radius of the sneak+V flame wave")
            .defineInRange("pyroFlameWaveRadius", 6.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue PYRO_FLAME_WAVE_DAMAGE = BUILDER
            .comment("Peak damage of the flame wave (falls off with distance)")
            .defineInRange("pyroFlameWaveDamage", 8.0, 0.0, 100.0);
    private static final ForgeConfigSpec.IntValue PYRO_FLAME_WAVE_FIRE_SECONDS = BUILDER
            .comment("How many seconds mobs caught in the flame wave burn for")
            .defineInRange("pyroFlameWaveFireSeconds", 6, 0, 120);
    private static final ForgeConfigSpec.IntValue PYRO_FLAME_WAVE_COOLDOWN = BUILDER
            .comment("Cooldown in ticks for the flame wave")
            .defineInRange("pyroFlameWaveCooldown", 50, 0, Integer.MAX_VALUE);

    // ===================================================================
    //  Cryokinesis
    // ===================================================================
    private static final ForgeConfigSpec.IntValue CRYO_MAX_CHARGES = BUILDER
            .comment("Maximum number of ice balls a Cryokinesis user can have stored")
            .defineInRange("cryoMaxCharges", 3, 1, 64);
    private static final ForgeConfigSpec.IntValue CRYO_CHARGE_REGEN_TICKS = BUILDER
            .comment("Ticks to regenerate one stored ice ball (20 ticks = 1 second)")
            .defineInRange("cryoChargeRegenTicks", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CRYO_BALL_SPEED = BUILDER
            .comment("Launch speed of the ice ball")
            .defineInRange("cryoBallSpeed", 1.4, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue CRYO_BALL_DAMAGE = BUILDER
            .comment("Damage dealt by an ice ball hitting an entity (0 = none, freeze only)")
            .defineInRange("cryoBallDamage", 3.0, 0.0, 100.0);
    private static final ForgeConfigSpec.IntValue CRYO_FREEZE_TICKS = BUILDER
            .comment("How long (ticks) an entity hit by an ice ball is frozen/slowed")
            .defineInRange("cryoFreezeTicks", 100, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CRYO_SLOWNESS_AMPLIFIER = BUILDER
            .comment("Slowness amplifier applied by an ice ball hit (0 = Slowness I)")
            .defineInRange("cryoSlownessAmplifier", 2, 0, 10);
    private static final ForgeConfigSpec.DoubleValue CRYO_BOUNCE_DAMPING = BUILDER
            .comment("Velocity retained after each ice-ball bounce (0-1; lower = bounces die faster)")
            .defineInRange("cryoBounceDamping", 0.7, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue CRYO_LIFETIME_TICKS = BUILDER
            .comment("Max lifetime of an ice ball in ticks before it despawns (20 = 1s)")
            .defineInRange("cryoLifetimeTicks", 100, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue CRYO_CHARGE_ENABLED = BUILDER
            .comment("If true, holding V charges the big cryoball (release to throw).",
                     "If false, the charge-up is disabled and V is a simple tap that throws a basic",
                     "ice ball immediately.")
            .define("cryoChargeEnabled", true);
    private static final ForgeConfigSpec.DoubleValue CRYO_CHARGED_IMPACT_DAMAGE = BUILDER
            .comment("Direct-hit impact damage dealt by the big charged cryoball (the small ball",
                     "deals none by default). In half-hearts.")
            .defineInRange("cryoChargedImpactDamage", 8.0, 0.0, 100.0);
    private static final ForgeConfigSpec.IntValue CRYO_MAX_CHARGE_TIME = BUILDER
            .comment("Max ticks the charged cryoball builds (full-charge point).")
            .defineInRange("cryoMaxChargeTime", 30, 1, 200);
    private static final ForgeConfigSpec.IntValue CRYO_CHARGED_MIN_HOLD = BUILDER
            .comment("Minimum hold (ticks) to throw the BIG charged cryoball instead of a normal",
                     "ball on release. Holds shorter than this just throw a normal ball.")
            .defineInRange("cryoChargedMinHoldTicks", 8, 1, 200);
    private static final ForgeConfigSpec.DoubleValue CRYO_CHARGED_BALL_SPEED = BUILDER
            .comment("Throw speed of the big charged cryoball (slower/clunkier than a normal ball).")
            .defineInRange("cryoChargedBallSpeed", 0.9, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue CRYO_CHARGED_FREEZE_RADIUS = BUILDER
            .comment("Radius (blocks) of the AOE freeze burst when the charged cryoball expires.")
            .defineInRange("cryoChargedFreezeRadius", 4.0, 0.5, 32.0);
    private static final ForgeConfigSpec.IntValue CRYO_CHARGED_FREEZE_TICKS = BUILDER
            .comment("How long (ticks) mobs caught in the charged cryoball's burst stay frozen.")
            .defineInRange("cryoChargedFreezeTicks", 140, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue CRYO_CHARGED_BURST_DAMAGE = BUILDER
            .comment("Small damage dealt to each mob caught in the charged cryoball's explosion",
                     "(separate from the direct-hit impact damage). In half-hearts. 0 = none.")
            .defineInRange("cryoChargedBurstDamage", 3.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue CRYO_CHARGED_BURST_KNOCKBACK = BUILDER
            .comment("Small outward knockback strength applied to mobs in the charged cryoball's",
                     "explosion. 0 = none. ~0.4 is a gentle shove.")
            .defineInRange("cryoChargedBurstKnockback", 0.4, 0.0, 5.0);
    private static final ForgeConfigSpec.BooleanValue CRYO_BALL_FREEZES_WATER = BUILDER
            .comment("Whether the ice ball freezes water surfaces it travels over (and skips off them)")
            .define("cryoBallFreezesWater", true);
    private static final ForgeConfigSpec.IntValue CRYO_BALL_FREEZE_RADIUS = BUILDER
            .comment("Radius (in blocks) of the frost patch the ice ball lays on water as it skips")
            .defineInRange("cryoBallFreezeRadius", 2, 0, 8);
    private static final ForgeConfigSpec.DoubleValue CRYO_AURA_RADIUS = BUILDER
            .comment("Radius of the frost aura")
            .defineInRange("cryoAuraRadius", 5.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue CRYO_AURA_SLOWNESS_AMPLIFIER = BUILDER
            .comment("Slowness amplifier applied to mobs inside the frost aura (0 = Slowness I)")
            .defineInRange("cryoAuraSlownessAmplifier", 1, 0, 10);
    private static final ForgeConfigSpec.DoubleValue CRYO_AURA_SELF_SLOW = BUILDER
            .comment("Fraction (0-1) the caster is slowed while the frost aura is active (upkeep cost)")
            .defineInRange("cryoAuraSelfSlow", 0.15, 0.0, 0.9);
    private static final ForgeConfigSpec.BooleanValue CRYO_AURA_FREEZES_WATER = BUILDER
            .comment("Whether the frost aura freezes water beneath the caster (Frost Walker style)")
            .define("cryoAuraFreezesWater", true);

    // ===================================================================
    //  Stormfront
    // ===================================================================
    private static final ForgeConfigSpec.IntValue STORMFRONT_LIGHTNING_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) between Stormfront lightning strikes (60 = 3 seconds)")
            .defineInRange("stormfrontLightningCooldown", 8, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_DAMAGE = BUILDER.defineInRange("stormfrontDischargeDamage", 1.6, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_RADIUS = BUILDER.defineInRange("stormfrontDischargeRadius", 6.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue STORMFRONT_DISCHARGE_TICK_RATE = BUILDER.defineInRange("stormfrontDischargeTickRate", 4, 1, 40);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_DAMAGE = BUILDER.comment("Damage of the first chain-lightning hit").defineInRange("stormfrontChainDamage", 7.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_FALLOFF = BUILDER.comment("Damage multiplier applied per additional chain jump (0-1)").defineInRange("stormfrontChainFalloff", 0.8, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue STORMFRONT_CHAIN_MAX_JUMPS = BUILDER.comment("Maximum number of mobs a chain-lightning bolt arcs through").defineInRange("stormfrontChainMaxJumps", 5, 1, 32);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_JUMP_RANGE = BUILDER.comment("Max distance the bolt can arc from one mob to the next").defineInRange("stormfrontChainJumpRange", 8.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_AIM_RANGE = BUILDER.comment("Max distance to acquire the initial chain-lightning target in your view direction").defineInRange("stormfrontChainAimRange", 8.0, 1.0, 128.0);

    // ===================================================================
    //  Forcefield
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_MAX_HP = BUILDER
            .comment("Maximum forcefield shield health")
            .defineInRange("forcefieldMaxHp", 100.0, 1.0, 10000.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_REGEN_PER_TICK = BUILDER
            .comment("Forcefield shield health regenerated per tick (heals even while disabled)")
            .defineInRange("forcefieldRegenPerTick", 0.1, 0.0, 1000.0);
    private static final ForgeConfigSpec.IntValue FORCEFIELD_REGEN_LOCKOUT_TICKS = BUILDER
            .comment("Ticks the forcefield must go WITHOUT taking a hit before it resumes healing.",
                     "Taking damage pauses regen for this long (20 ticks = 1s). 80 = 4 seconds.",
                     "0 disables the lockout (heals immediately as before).")
            .defineInRange("forcefieldRegenLockoutTicks", 80, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_DAMAGE_MULTIPLIER = BUILDER
            .comment("Multiplier applied to incoming damage when draining the shield (higher = shield drains faster)")
            .defineInRange("forcefieldDamageMultiplier", 1.75, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_REENABLE_PERCENT = BUILDER
            .comment("Shield health fraction (0-1) required before the forcefield can be re-enabled after breaking")
            .defineInRange("forcefieldReenablePercent", 0.25, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_BREAK_DAMAGE = BUILDER
            .comment("Damage dealt to the forcefield owner when the forcefield breaks")
            .defineInRange("forcefieldBreakDamage", 6.0, 0.0, 1000.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_BREAK_AOE_DAMAGE = BUILDER
            .comment("Damage dealt to nearby entities by the shockwave when the forcefield breaks (0 = no shockwave)")
            .defineInRange("forcefieldBreakAoeDamage", 8.0, 0.0, 1000.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_BREAK_AOE_RADIUS = BUILDER
            .comment("Radius (in blocks) of the forcefield break shockwave")
            .defineInRange("forcefieldBreakAoeRadius", 4.0, 0.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_PUSH_RADIUS = BUILDER
            .comment("Radius (in blocks) within which the forcefield idly pushes mobs away")
            .defineInRange("forcefieldPushRadius", 1.0, 0.0, 10.0);
    private static final ForgeConfigSpec.IntValue FORCEFIELD_SLOWNESS = BUILDER
            .comment("Slowness amplifier applied to the player while the forcefield is ACTIVE (the",
                     "drawback of projecting a shield). 0 = Slowness I, 1 = Slowness II, etc. Set to",
                     "-1 to disable the slowdown entirely.")
            .defineInRange("forcefieldActiveSlowness", 1, -1, 10);

    // ===================================================================
    //  Telekinesis
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_RANGE = BUILDER
            .comment("Max distance at which Telekinesis can grab an entity you look at")
            .defineInRange("telekinesisRange", 48.0, 1.0, 256.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_HOLD_DISTANCE = BUILDER
            .comment("How far in front of you a telekinetically-held entity floats")
            .defineInRange("telekinesisHoldDistance", 4.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_HOLD_STRENGTH = BUILDER
            .comment("Spring strength steering a held entity toward the hold point (higher = snappier tracking)")
            .defineInRange("telekinesisHoldStrength", 0.4, 0.05, 2.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_LAUNCH_FORCE = BUILDER
            .comment("Velocity applied when launching a held entity on release")
            .defineInRange("telekinesisLaunchForce", 2.5, 0.0, 20.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_LAUNCH_RANGE = BUILDER
            .comment("A held entity is only launched (with force/damage) if it has been reeled within this distance of the player. Released harmlessly if farther - prevents damaging far-off mobs by spam-tapping. Should be slightly more than telekinesisHoldDistance.")
            .defineInRange("telekinesisLaunchRange", 5.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_LAUNCH_DAMAGE = BUILDER
            .comment("Damage dealt to a launched entity (0 = none)")
            .defineInRange("telekinesisLaunchDamage", 4.0, 0.0, 100.0);
    private static final ForgeConfigSpec.BooleanValue TELEKINESIS_GRABS_PLAYERS = BUILDER
            .comment("Whether Telekinesis can grab and launch other players")
            .define("telekinesisGrabsPlayers", true);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_SCROLL_MIN = BUILDER
            .comment("Minimum hold distance reachable by scrolling the wheel while holding an entity")
            .defineInRange("telekinesisScrollMinDistance", 2.0, 1.0, 64.0);
    private static final ForgeConfigSpec.DoubleValue TELEKINESIS_SCROLL_MAX = BUILDER
            .comment("Maximum hold distance reachable by scrolling the wheel while holding an entity")
            .defineInRange("telekinesisScrollMaxDistance", 32.0, 1.0, 256.0);

    // ===================================================================
    //  Size Control
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_SCROLL_STEP = BUILDER
            .comment("How much the size changes per scroll notch")
            .defineInRange("sizeControlScrollStep", 0.15, 0.01, 2.0);
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_MIN_SCALE = BUILDER
            .comment("Smallest size reachable with Size Control")
            .defineInRange("sizeControlMinScale", 0.25, 0.05, 1.0);
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_MAX_SCALE = BUILDER
            .comment("Largest size reachable with Size Control")
            .defineInRange("sizeControlMaxScale", 4.0, 1.0, 8.0);

    // ===================================================================
    //  Power Absorption
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_DAMAGE = BUILDER.defineInRange("powerplexDischargeDamage", 2.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_RADIUS = BUILDER.defineInRange("powerplexDischargeRadius", 5.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue POWERPLEX_DISCHARGE_TICK_RATE = BUILDER.defineInRange("powerplexDischargeTickRate", 4, 1, 40);
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_FOCUS_DAMAGE = BUILDER
            .comment("Damage per discharge tick when channeling Power Absorption into a SINGLE mob",
                     "you're looking at (hold V, no crouch). Scaled by current charge. Much higher",
                     "than the AOE discharge so it melts one target quickly.")
            .defineInRange("powerplexFocusDamage", 8.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_FOCUS_RANGE = BUILDER
            .comment("Max range (blocks) to acquire the single focused target.")
            .defineInRange("powerplexFocusRange", 12.0, 1.0, 48.0);

    // ===================================================================
    //  Lifesteal
    // ===================================================================
    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends Double>> LIFESTEAL_LEVEL_FRACTIONS = BUILDER
            .comment("Fraction of melee damage healed at each Lifesteal level (level 1 = first entry, level 2 = second, ...).",
                     "The number of entries determines how many levels Lifesteal has. Default: 0.2, 0.4, 0.6 (20%, 40%, 60%).")
            .defineList("lifestealLevelFractions",
                    java.util.Arrays.asList(0.2, 0.4, 0.6),
                    o -> o instanceof Double d && d >= 0.0 && d <= 5.0);
    private static final ForgeConfigSpec.BooleanValue LIFESTEAL_BLOCKS_ALL_HEALING = BUILDER
            .comment("If true, Lifesteal blocks ALL other healing (potions, golden apples, regen) so dealing melee damage is the ONLY way to heal. If false (default), only natural hunger-regen is blocked.")
            .define("lifestealBlocksAllHealing", false);

    // ===================================================================
    //  Aimlock
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue AIMLOCK_RANGE = BUILDER
            .comment("Maximum range (in blocks) at which Aimlock can lock onto a target")
            .defineInRange("aimlockRange", 96.0, 1.0, 512.0);
    private static final ForgeConfigSpec.DoubleValue AIMLOCK_HOMING_STRENGTH = BUILDER
            .comment("How aggressively homing projectiles turn toward the locked target each tick (0 = none, 1 = instant snap)")
            .defineInRange("aimlockHomingStrength", 0.25, 0.0, 1.0);

    // ===================================================================
    //  Nullify
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue NULLIFY_RADIUS = BUILDER
            .comment("Radius (in blocks) of the nullification aura")
            .defineInRange("nullifyRadius", 10.0, 1.0, 64.0);

    // ===================================================================
    //  Other Powers
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue HEALING_SELF_PER_TICK = BUILDER
            .comment("Self-heal rate (HP per tick) when sneak+holding V with the Healing power.",
                     "Slower than healing a target you look at. 0.2 = ~4 HP/sec.")
            .defineInRange("healingSelfPerTick", 0.2, 0.0, 20.0);
    private static final ForgeConfigSpec.IntValue HEALING_SELF_DELAY_TICKS = BUILDER
            .comment("Ticks the Healing user must go WITHOUT taking damage before self-heal starts",
                     "(20 = 1s). Default 50 (~2.5s), so you can't top yourself off mid-fight.")
            .defineInRange("healingSelfDelayTicks", 50, 0, 600);
    private static final ForgeConfigSpec.IntValue TELEPORT_RANGE = BUILDER
            .comment("Range of the teleportation power")
            .defineInRange("teleport_range", 36, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue TELEPORT_COOLDOWN = BUILDER
            .comment("Cooldown in ticks after teleporting (0 = no cooldown)")
            .defineInRange("teleportCooldown", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue DENSITY_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier when in Dense state (lower = more tanky)")
            .defineInRange("densityDamageMultiplier", 0.5, 0, 1);
    private static final ForgeConfigSpec.BooleanValue STAR_POWER_UNLIMITED = BUILDER
            .comment("If true, Star Power has no cooldown and can be toggled on/off at will")
            .define("starPowerUnlimited", false);
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
    private static final ForgeConfigSpec.DoubleValue PETRIFYING_GAZE_RANGE = BUILDER
            .comment("Range in blocks for Petrifying Gaze to freeze targets")
            .defineInRange("petrifyingGazeRange", 40.0, 5.0, 128.0);
    private static final ForgeConfigSpec.BooleanValue PETRIFYING_GAZE_AFFECTS_PLAYERS = BUILDER
            .comment("Whether Petrifying Gaze can freeze other players")
            .define("petrifyingGazeAffectsPlayers", true);

    // ===================================================================
    //  Mob Powers
    // ===================================================================
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_LIFESTEAL = BUILDER
            .comment("Universal mob-pool base weight: Lifesteal")
            .defineInRange("mobWeight.lifesteal", 1, 0, Integer.MAX_VALUE);
    // Species-pool gates: these powers are added only through species-specific pools (not the
    // universal pool). The weight here acts as an on/off gate for whether a mob may roll them at
    // all - set to 0 to disable the power for mobs entirely (the species pools keep their own
    // internal bias weights). Chest Blast, Explosive, and Forcefield default to 0 (off).
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_DEEP = BUILDER
            .comment("Mob gate: Deep (water powers). 0 = mobs never roll it.")
            .defineInRange("mobWeight.deep", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_BERSERKER = BUILDER
            .comment("Mob gate: Berserker. 0 = mobs never roll it.")
            .defineInRange("mobWeight.berserker", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_INVINCIBLE = BUILDER
            .comment("Mob gate: Invincibility. 0 = mobs never roll it.")
            .defineInRange("mobWeight.invincible", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_ATOM_CHARGING = BUILDER
            .comment("Mob gate: Atom Charging. 0 = mobs never roll it.")
            .defineInRange("mobWeight.atom_charging", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_MAGNETISM = BUILDER
            .comment("Mob gate: Magnetism. 0 = mobs never roll it.")
            .defineInRange("mobWeight.magnetism", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_LASER_ADVANCED = BUILDER
            .comment("Mob gate: Advanced Laser Eyes. 0 = mobs never roll it.")
            .defineInRange("mobWeight.laser_advanced", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_CHEST_BLAST = BUILDER
            .comment("Mob gate: Chest Blast (Soldier Boy). 0 = mobs never roll it. Off by default.")
            .defineInRange("mobWeight.chest_blast", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_EXPLOSIVE = BUILDER
            .comment("Mob gate: Explosive. 0 = mobs never roll it. Off by default.")
            .defineInRange("mobWeight.explosive", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_FORCEFIELD = BUILDER
            .comment("Mob gate: Forcefield. 0 = mobs never roll it. Off by default (mobs can't render the bubble).")
            .defineInRange("mobWeight.forcefield", 0, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_PROJECTILE_IMMUNITY = BUILDER
            .comment("Universal mob-pool base weight: Projectile Immunity")
            .defineInRange("mobWeight.projectile_immunity", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_PYROKINESIS = BUILDER
            .comment("Universal mob-pool base weight: Pyrokinesis")
            .defineInRange("mobWeight.pyrokinesis", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_SHRINK = BUILDER
            .comment("Universal mob-pool base weight: Shrink (Pehkui)")
            .defineInRange("mobWeight.shrink", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_SPEEDSTER = BUILDER
            .comment("Universal mob-pool base weight: Speedster")
            .defineInRange("mobWeight.speedster", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_SPIDER = BUILDER
            .comment("Universal mob-pool base weight: Spider")
            .defineInRange("mobWeight.spider", 2, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_TELEPORT = BUILDER
            .comment("Universal mob-pool base weight: Teleport")
            .defineInRange("mobWeight.teleport", 1, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue MOB_ENABLE_MULTI_POWERS = BUILDER.define("mobEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MOB_MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("mobMultiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue MOB_SPECIES_BIAS_ENABLED = BUILDER
            .comment("If true, mobs are biased toward thematic powers for their species (e.g. spiders",
                     "lean Spider, blazes lean fire). If false, every mob rolls only from the flat",
                     "universal pool with no species weighting.")
            .define("mobSpeciesBiasEnabled", true);
    private static final ForgeConfigSpec.DoubleValue MOB_SPECIES_BIAS_MULTIPLIER = BUILDER
            .comment("Global multiplier applied to all species-specific power weights. 1.0 = default,",
                     "0.0 = species themes ignored (pure universal pool), 2.0 = themes twice as strong.",
                     "Lets you dial how hard mobs lean into their thematic powers.")
            .defineInRange("mobSpeciesBiasMultiplier", 1.0, 0.0, 16.0);
    private static final ForgeConfigSpec.DoubleValue MOB_LASER_DAMAGE_CONFIG = BUILDER.defineInRange("mobLaserDamage", 0.02, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue MOB_ADVANCED_LASER_DAMAGE_CONFIG = BUILDER.defineInRange("mobAdvancedLaserDamage", 0.06, 0.0, 10.0);
    private static final ForgeConfigSpec.BooleanValue MOB_CHEST_BLAST_STRIPS_POWERS = BUILDER
            .comment("Whether mob Chest Blast strips Compound V powers from targets (like the player version)")
            .define("mobChestBlastStripsPowers", false);
    private static final ForgeConfigSpec.DoubleValue MOB_CHEST_BLAST_INACCURACY = BUILDER
            .comment("Maximum aim deviation in degrees for mob Chest Blast (0 = perfect aim, 5 = dodgeable)")
            .defineInRange("mobChestBlastInaccuracy", 5.0, 0.0, 45.0);

    // ===================================================================
    //  Negative Effects
    // ===================================================================
    private static final ForgeConfigSpec.DoubleValue UNCONTROLLED_EXPLOSION_POWER = BUILDER
            .comment("Explosion power when an Uncontrolled victim detonates (TNT = 4.0)").defineInRange("uncontrolledExplosionPower", 6.0, 0.0, 50.0);
    private static final ForgeConfigSpec.BooleanValue UNCONTROLLED_EXPLOSION_BREAKS_BLOCKS = BUILDER
            .comment("Whether the Uncontrolled detonation destroys blocks").define("uncontrolledExplosionBreaksBlocks", true);
    private static final ForgeConfigSpec.IntValue WITHER_AMPLIFIER = BUILDER
            .comment("Wither amplifier inflicted by Wither (0 = Wither I)").defineInRange("witherAmplifier", 3, 0, 10);
    private static final ForgeConfigSpec.IntValue UNCONTROLLED_SIZE_INTERVAL_MIN = BUILDER
            .comment("Minimum ticks between random size changes for Uncontrolled Size (20 = 1s)")
            .defineInRange("uncontrolledSizeIntervalMinTicks", 60, 20, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue UNCONTROLLED_SIZE_INTERVAL_MAX = BUILDER
            .comment("Maximum ticks between random size changes for Uncontrolled Size (1200 = 60s). Each wait is randomized between min and max, so changes are irregular and can be a minute or more apart.")
            .defineInRange("uncontrolledSizeIntervalMaxTicks", 1400, 20, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue UNCONTROLLED_SIZE_MIN = BUILDER
            .comment("Smallest random scale for Uncontrolled Size").defineInRange("uncontrolledSizeMinScale", 0.3, 0.05, 1.0);
    private static final ForgeConfigSpec.DoubleValue UNCONTROLLED_SIZE_MAX = BUILDER
            .comment("Largest random scale for Uncontrolled Size").defineInRange("uncontrolledSizeMaxScale", 2.5, 1.0, 8.0);


    // --- Tier System ---
    private static final ForgeConfigSpec.ConfigValue<String> DEFAULT_POWER_TIER = BUILDER.comment("Default tier. Valid: S, A, B, C, D").define("defaultPowerTier", "C");
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
    private static final ForgeConfigSpec.ConfigValue<String> TIER_AIMLOCK = BUILDER.define("tierOf.aimlock", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_PYROKINESIS = BUILDER.define("tierOf.pyrokinesis", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_CRYOKINESIS = BUILDER.define("tierOf.cryokinesis", "A");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SPEEDSTER = BUILDER.define("tierOf.speedster", "SPECIAL");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LIFESTEAL = BUILDER.define("tierOf.lifesteal", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_TELEKINESIS = BUILDER.define("tierOf.telekinesis", "B");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SIZE_CONTROL = BUILDER.define("tierOf.sizeControlAdvanced", "SPECIAL");
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
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SLIME = BUILDER.define("tierOf.slime", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_ENLARGE = BUILDER.define("tierOf.enlarge", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SHRINK = BUILDER.define("tierOf.shrink", "C");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LUCK = BUILDER.define("tierOf.luck", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_LEVITATION = BUILDER.define("tierOf.levitation", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_MIMIC = BUILDER.define("tierOf.mimic", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SONIC_SCREAM = BUILDER.define("tierOf.sonic_scream", "D");
    private static final ForgeConfigSpec.DoubleValue SONIC_SCREAM_DAMAGE = BUILDER
            .comment("Base Sonic Scream cone damage (bypasses armor) at point-blank; falls off with",
                     "distance across the cone.")
            .defineInRange("sonicScreamDamage", 10.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue SONIC_SCREAM_PLAYER_DAMAGE_MULT = BUILDER
            .comment("Multiplier on Sonic Scream damage when the target is a PLAYER (1.0 = same as",
                     "mobs, 0.5 = half). Lets you soften PvP without nerfing PvE.")
            .defineInRange("sonicScreamPlayerDamageMult", 0.5, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue SONIC_SCREAM_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (ticks, 20 = 1s) between Sonic Scream uses, starting when a scream",
                     "fires. Default 300 = 15 seconds.")
            .defineInRange("sonicScreamCooldownTicks", 300, 0, 12000);
    private static final ForgeConfigSpec.ConfigValue<String> TIER_PROJECTILE_IMMUNITY = BUILDER.define("tierOf.projectile_immunity", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_ATOM_CHARGING = BUILDER.define("tierOf.atom_charging", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_STAR_POWER = BUILDER.define("tierOf.star_power", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_FORCEFIELD = BUILDER.define("tierOf.forcefield", "D");
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SPIDER = BUILDER.define("tierOf.spider", "A");

    // Tier SP - legendary (no powers by default, custom assignable)
    private static final ForgeConfigSpec.DoubleValue TIER_SP_DR = BUILDER.defineInRange("tierSP.damageReduction", 0.15, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_DR_PL = BUILDER.defineInRange("tierSP.damageReductionPerLevel", -0.03, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_STR = BUILDER.defineInRange("tierSP.strengthMultiplier", 3.0, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_STR_PL = BUILDER.defineInRange("tierSP.strengthPerLevel", 0.75, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_KB = BUILDER.defineInRange("tierSP.knockbackReduction", 0.15, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_KB_PL = BUILDER.defineInRange("tierSP.knockbackPerLevel", -0.03, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_KBD = BUILDER.defineInRange("tierSP.knockbackDealt", 2.5, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_SP_KBD_PL = BUILDER.defineInRange("tierSP.knockbackDealtPerLevel", 0.5, -10, 10);

    // Tier S
    private static final ForgeConfigSpec.DoubleValue TIER_S_DR = BUILDER.defineInRange("tierS.damageReduction", 0.3, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_DR_PL = BUILDER.defineInRange("tierS.damageReductionPerLevel", -0.05, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_STR = BUILDER.defineInRange("tierS.strengthMultiplier", 2.0, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_S_STR_PL = BUILDER.defineInRange("tierS.strengthPerLevel", 0.5, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KB = BUILDER.defineInRange("tierS.knockbackReduction", 0.3, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KB_PL = BUILDER.defineInRange("tierS.knockbackPerLevel", -0.05, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KBD = BUILDER.defineInRange("tierS.knockbackDealt", 2.0, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_S_KBD_PL = BUILDER.defineInRange("tierS.knockbackDealtPerLevel", 0.3, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_A_DR = BUILDER.defineInRange("tierA.damageReduction", 0.4, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_DR_PL = BUILDER.defineInRange("tierA.damageReductionPerLevel", -0.04, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_STR = BUILDER.defineInRange("tierA.strengthMultiplier", 1.75, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_A_STR_PL = BUILDER.defineInRange("tierA.strengthPerLevel", 0.35, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KB = BUILDER.defineInRange("tierA.knockbackReduction", 0.4, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KB_PL = BUILDER.defineInRange("tierA.knockbackPerLevel", -0.04, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KBD = BUILDER.defineInRange("tierA.knockbackDealt", 1.75, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_A_KBD_PL = BUILDER.defineInRange("tierA.knockbackDealtPerLevel", 0.2, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_B_DR = BUILDER.defineInRange("tierB.damageReduction", 0.5, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_DR_PL = BUILDER.defineInRange("tierB.damageReductionPerLevel", -0.03, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_STR = BUILDER.defineInRange("tierB.strengthMultiplier", 1.5, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_B_STR_PL = BUILDER.defineInRange("tierB.strengthPerLevel", 0.25, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KB = BUILDER.defineInRange("tierB.knockbackReduction", 0.5, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KB_PL = BUILDER.defineInRange("tierB.knockbackPerLevel", -0.03, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KBD = BUILDER.defineInRange("tierB.knockbackDealt", 1.5, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_B_KBD_PL = BUILDER.defineInRange("tierB.knockbackDealtPerLevel", 0.15, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_C_DR = BUILDER.defineInRange("tierC.damageReduction", 0.65, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_DR_PL = BUILDER.defineInRange("tierC.damageReductionPerLevel", -0.02, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_STR = BUILDER.defineInRange("tierC.strengthMultiplier", 1.25, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_C_STR_PL = BUILDER.defineInRange("tierC.strengthPerLevel", 0.15, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KB = BUILDER.defineInRange("tierC.knockbackReduction", 0.65, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KB_PL = BUILDER.defineInRange("tierC.knockbackPerLevel", -0.02, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KBD = BUILDER.defineInRange("tierC.knockbackDealt", 1.25, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_C_KBD_PL = BUILDER.defineInRange("tierC.knockbackDealtPerLevel", 0.1, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_D_DR = BUILDER.defineInRange("tierD.damageReduction", 0.8, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_DR_PL = BUILDER.defineInRange("tierD.damageReductionPerLevel", -0.01, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_STR = BUILDER.defineInRange("tierD.strengthMultiplier", 1.1, 0, 50);
    private static final ForgeConfigSpec.DoubleValue TIER_D_STR_PL = BUILDER.defineInRange("tierD.strengthPerLevel", 0.05, -10, 10);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KB = BUILDER.defineInRange("tierD.knockbackReduction", 0.8, 0, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KB_PL = BUILDER.defineInRange("tierD.knockbackPerLevel", -0.01, -5, 5);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KBD = BUILDER.defineInRange("tierD.knockbackDealt", 1.1, 0, 20);
    private static final ForgeConfigSpec.DoubleValue TIER_D_KBD_PL = BUILDER.defineInRange("tierD.knockbackDealtPerLevel", 0.05, -10, 10);

    // --- Mob Power Toggles (all enabled by default) ---
    private static final ForgeConfigSpec.IntValue MOB_PYRO_FIRE_COOLDOWN = BUILDER.comment("Ticks between a mob's fireball throws").defineInRange("mobPyroFireCooldown", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_CRYO_FIRE_COOLDOWN = BUILDER.comment("Ticks between a mob's ice ball throws").defineInRange("mobCryoFireCooldown", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue MOB_PROJECTILE_RANGE = BUILDER.comment("Max distance at which a Pyro/Cryo mob will throw projectiles at its target").defineInRange("mobProjectileRange", 24.0, 1.0, 128.0);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_FRIENDLY_FIRE = BUILDER.comment("If true, mob fire/ice AoE (flame wave, frost aura) and projectile explosions can harm passive/allied mobs. Projectiles already hit whatever is directly in their path regardless.").define("mobPowerFriendlyFire", false);
    private static final ForgeConfigSpec.IntValue MOB_PYRO_MAX_CHARGES = BUILDER.comment("Max stored fireballs for a Pyro mob").defineInRange("mobPyroMaxCharges", 3, 1, 64);
    private static final ForgeConfigSpec.IntValue MOB_CRYO_MAX_CHARGES = BUILDER.comment("Max stored ice balls for a Cryo mob").defineInRange("mobCryoMaxCharges", 3, 1, 64);
    private static final ForgeConfigSpec.IntValue MOB_CHARGE_REGEN_TICKS = BUILDER.comment("Ticks for a Pyro/Cryo mob to regenerate one stored projectile").defineInRange("mobChargeRegenTicks", 40, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MOB_CHEST_BLAST_WEIGHT = BUILDER
            .comment("Weight of Chest Blast in the mob power pool")
            .defineInRange("mobChestBlastWeight", 1, 1, Integer.MAX_VALUE);

    public static int mobPyroFireCooldown;
    public static int mobCryoFireCooldown;
    public static double mobProjectileRange;
    public static boolean mobPowerFriendlyFire;
    public static int mobPyroMaxCharges;
    public static int mobCryoMaxCharges;
    public static int mobChargeRegenTicks;
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
    private static final ForgeConfigSpec.BooleanValue PERSIST_FAILURE_EFFECTS_ON_DEATH = BUILDER
            .comment("Only matters when persistPowersOnDeath is on. If true, failure/negative effects",
                     "(Slowness, Wither, the Uncontrolled ones, etc.) also persist through death. Off",
                     "by default, so a bad roll doesn't follow you past dying - real powers still do.")
            .define("persistFailureEffectsOnDeath", false);
    private static final ForgeConfigSpec.BooleanValue VIRUS_REMOVES_POWER_ON_DEATH = BUILDER
            .comment("If persistPowersOnDeath is on and player has the virus, dying removes one power")
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
    public static boolean levelUpOnDrink;
    public static boolean playerPickupEnabled;
    public static boolean playerPickupRequiresCompoundV;
    public static double playerPickupSizeRatio;
    public static boolean v1LevelUpMaxed;
    /** Parsed V1 upgrade paths: lesser power -> greater power. Built from v1UpgradePaths at load. */
    public static java.util.Map<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> v1UpgradePaths = new java.util.HashMap<>();
    public static double tempVBadOutcomeChance;
    // Tier system
    public static CompoundVEffect.PowerTier defaultPowerTier;
    public static double[][] tierStats;
    public static java.util.Map<net.minecraft.world.effect.MobEffect, CompoundVEffect.PowerTier> effectTierMap = new java.util.HashMap<>();
    // Multi-power
    public static boolean enableMultiPowers;
    public static boolean irradiationWeakensSupes;
    public static int irradiationWeakenMinLevel;
    public static int multiPowerMaxCount;
    public static boolean tempVEnableMultiPowers;
    public static int tempVMultiPowerMaxCount;
    public static boolean mobEnableMultiPowers;
    public static int mobMultiPowerMaxCount;
    // Laser push
    public static boolean laserBasicPushEnabled;
    public static double laserBasicPushStrength;
    public static double laserBasicShieldPushMultiplier;
    public static boolean laserAdvancedPushEnabled;
    public static double laserAdvancedPushStrength;
    public static double laserAdvancedShieldPushMultiplier;
    public static int laserBasicDamageTickRate;
    public static boolean laserDisabledWhileMoving;
    public static double laserFirstPersonOpacity;
    public static boolean laserIntimidationFirstPerson;
    public static double laserMoveSpeedThreshold;
    public static boolean laserIntensityAdjustable;
    public static double laserIntensityScrollStep;
    public static double laserBreakCriticalIntensity;
    public static double laserFireCriticalIntensity;
    public static boolean laserIgniteEnabled;
    public static double laserIgniteChance;
    public static double laserIgniteCriticalIntensity;
    public static int laserIgniteEntitySeconds;
    public static int laserAdvancedDamageTickRate;
    public static boolean laserBasicBreakBlocks;
    public static boolean laserAdvancedBreakBlocks;
    public static double laserBlockBreakChance;
    public static double laserBreakSpeed;
    public static double laserBreakHardnessWeight;
    public static double laserBreakResistanceWeight;
    public static double laserBreakDecay;
    public static boolean laserBlockBreakDrops;
    public static boolean laserHeartbeatEnabled;
    public static double laserHeartbeatRange;
    public static double laserHeartbeatHealthThreshold;
    public static int laserHeartbeatSlowInterval;
    public static int laserHeartbeatFastInterval;
    public static boolean chestBlastBlockBreakDrops;
    public static boolean chestBlastNovaBlockBreakDrops;
    public static boolean chestBlastNovaBreaksBlocks;
    public static boolean chestBlastNovaEnabled;
    public static double chestBlastNovaGuaranteedBreakFraction;
    public static boolean laserColorCommandOpOnly;
    // Teleport
    public static int teleportCooldown;
    public static double forcefieldMaxHp;
    public static double forcefieldRegenPerTick;
    public static int forcefieldRegenLockoutTicks;
    public static double forcefieldDamageMultiplier;
    public static double forcefieldReenablePercent;
    public static double forcefieldBreakDamage;
    public static double forcefieldBreakAoeDamage;
    public static double forcefieldBreakAoeRadius;
    public static double forcefieldPushRadius;
    public static int forcefieldActiveSlowness;
    public static double nullifyRadius;
    // Speedster
    public static int speedsterSpeedLevelsPerAmp;
    public static double speedsterSprintDamage;
    public static double speedsterSprintDamagePerAmp;
    public static double speedsterPlayerDamageMult;
    // Chest blast extras
    public static boolean chestBlastBlockedByWalls;
    public static boolean chestBlastStripsPowers;
    public static boolean chestBlastShieldBlocksStrip;
    public static double chestBlastNovaRadius;
    public static double chestBlastNovaPower;
    public static double chestBlastNovaDamage;
    public static double chestBlastNovaKnockback;
    public static int chestBlastNovaChargeTime;
    // Virus
    public static boolean virusDisablesPlayerPowers;
    public static boolean virusDisablesMobPowers;
    // Mob laser
    public static double mobLaserDamage;
    public static double mobAdvancedLaserDamage;
    // Discharge
    public static double powerplexDischargeDamage;
    public static double powerplexDischargeRadius;
    public static int powerplexDischargeTickRate;
    public static double powerplexFocusDamage;
    public static double powerplexFocusRange;
    public static double stormfrontDischargeDamage;
    public static double stormfrontChainDamage;
    public static double stormfrontChainFalloff;
    public static int stormfrontChainMaxJumps;
    public static double stormfrontChainJumpRange;
    public static double stormfrontChainAimRange;
    public static double stormfrontDischargeRadius;
    public static int stormfrontDischargeTickRate;
    // V1 weights
    public static int v1WeightAimlock;
    public static int v1WeightAtomCharging;
    public static int v1WeightBerserker;
    public static int v1WeightChestBlast;
    public static int v1WeightCreativeFlight;
    public static int v1WeightCryokinesis;
    public static int v1WeightDeep;
    public static int v1WeightDensity;
    public static int v1WeightSlime;
    public static int v1WeightEnhancedRegen;
    public static int v1WeightEnlarge;
    public static int v1WeightExplosive;
    public static int v1WeightForcefield;
    public static int v1WeightNullify;
    public static int v1WeightGeneric;
    public static int v1WeightHeadPop;
    public static int v1WeightHealing;
    public static int v1WeightInstakill;
    public static int v1WeightInvincible;
    public static int v1WeightInvisibility;
    public static int v1WeightLaserAdvanced;
    public static int v1WeightLaserBasic;
    public static int v1WeightLeap;
    public static int v1WeightLevitation;
    public static int v1WeightLifesteal;
    public static int v1WeightLuck;
    public static int v1WeightMimic;
    public static int v1WeightMindControl;
    public static int v1WeightNightVision;
    public static int v1WeightPetrifyingGaze;
    public static int v1WeightPowerAbsorption;
    public static int v1WeightProjectileImmunity;
    public static int v1WeightPyrokinesis;
    public static int v1WeightShrink;
    public static int v1WeightSizeControl;
    public static int v1WeightSonicScream;
    public static int v1WeightSpeedster;
    public static int v1WeightSpider;
    public static int v1WeightStarPower;
    public static int v1WeightStormfront;
    public static int v1WeightTelekinesis;
    public static int v1WeightTeleport;
    // Death persist
    public static boolean virusRemovesPowerOnDeath;
    public static int weightGeneric;
    public static int weightSpeedster;
    public static boolean speedsterMobAttack;
    public static int weightWater;
    public static int weightTeleport;
    public static int weightAimlock;
    public static int weightPyrokinesis;
    public static int pyroMaxCharges;
    public static int pyroChargeRegenTicks;
    public static boolean pyroChargeEnabled;
    public static int pyroMaxChargeTime;
    public static double pyroMinExplosion;
    public static double pyroMaxExplosion;
    public static double pyroMinSpeed;
    public static double pyroMaxSpeed;
    public static boolean pyroFireballBreaksBlocks;
    public static double pyroFlameWaveRadius;
    public static double pyroFlameWaveDamage;
    public static int pyroFlameWaveFireSeconds;
    public static int pyroFlameWaveCooldown;
    public static int weightCryokinesis;
    public static int weightLifesteal;
    public static double[] lifestealLevelFractions;
    public static boolean lifestealBlocksAllHealing;
    public static int weightTelekinesis;
    public static double telekinesisRange;
    public static double telekinesisHoldDistance;
    public static double telekinesisHoldStrength;
    public static double telekinesisLaunchForce;
    public static double telekinesisLaunchRange;
    public static double telekinesisLaunchDamage;
    public static boolean telekinesisGrabsPlayers;
    public static double telekinesisScrollMinDistance;
    public static double telekinesisScrollMaxDistance;
    public static int weightSizeControlAdvanced;
    public static double spiderWebSpeed;
    public static boolean spiderWallClimbEnabled;
    public static boolean spiderCeilingClimbEnabled;
    public static double spiderClimbSpeed;
    public static double spiderClimbStickGap;
    public static boolean spiderClimbLookRelative;
    public static double spiderReelGroundLift;
    public static boolean spiderWallJumpEnabled;
    public static double spiderWallJumpPower;
    public static double spiderWallJumpLift;
    public static boolean spiderRaycastWebbing;
    public static double spiderWebFallCompensation;
    public static int spiderFireCooldown;
    public static int spiderMobWebStuckMaxTicks;
    public static double spiderMobWebTrapChance;
    public static int spiderMobWebTrapCooldown;
    public static double spiderMinRope;
    public static double spiderMaxRope;
    public static double spiderReelStep;
    public static double spiderReelPull;
    public static boolean spiderReelMassEnabled;
    public static double spiderMobReelSpring;
    public static double spiderMobReelMaxSpeed;
    public static double spiderReelHealthWeight;
    public static double spiderReelSizeWeight;
    public static double spiderReelMaxMass;
    public static double spiderSwingControl;
    public static boolean spiderSwingJumpEnabled;
    public static int spiderSwingJumpGroundGrace;
    public static boolean spiderSwingJumpRefires;
    public static double spiderSwingJumpMomentum;
    public static double spiderSwingJumpLift;
    public static double spiderSwingPump;
    public static double spiderSwingGravity;
    public static double spiderWebGravityMult;
    public static double spiderMaxSwingSpeed;
    public static double spiderSwingBoost;
    public static double spiderFlingForce;
    public static double spiderFlingDamage;
    public static double spiderSlamPitchThreshold;
    public static double spiderSlamForce;
    public static double spiderSlamDamage;
    public static boolean spiderSenseEnabled;
    public static double spiderSenseRadius;
    public static int spiderSenseScanInterval;
    public static boolean spiderSenseDetectProjectiles;
    public static boolean spiderSenseDetectAggro;
    public static boolean spiderSenseDetectCreepers;
    public static boolean spiderSenseDodgePayoff;
    public static double spiderSenseDamageMultiplier;
    public static int spiderSenseWindowTicks;
    public static int spiderSenseCooldownTicks;
    public static boolean mobSpeciesBiasEnabled;
    public static double mobSpeciesBiasMultiplier;
    public static int mobWeightLaserBasic;
    public static int mobWeightEnhancedRegen;
    public static int mobWeightInvisibility;
    public static int mobWeightProjectileImmunity;
    public static int mobWeightSpeedster;
    public static int mobWeightTeleport;
    public static int mobWeightCreativeFlight;
    public static int mobWeightShrink;
    public static int mobWeightEnlarge;
    public static int mobWeightSizeControlAdvanced;
    public static int mobWeightLeap;
    public static int mobWeightSlime;
    public static int mobWeightSpider;
    public static int mobWeightHealing;
    public static int mobWeightLifesteal;
    public static int mobWeightDeep;
    public static int mobWeightBerserker;
    public static int mobWeightInvincible;
    public static int mobWeightAtomCharging;
    public static int mobWeightMagnetism;
    public static int mobWeightLaserAdvanced;
    public static int mobWeightChestBlast;
    public static int mobWeightExplosive;
    public static int mobWeightForcefield;
    public static int mobWeightPyrokinesis;
    public static int mobWeightCryokinesis;
    public static int weightHeadPopFailure;
    public static int weightUncontrolledExplosion;
    public static int weightWither;
    public static int weightUncontrolledSize;
    public static int weightFailBlindness;
    public static int weightFailFloating;
    public static int weightFailMagnetism;
    public static int weightFailSlowness;
    public static int weightFailUncontrolledTeleport;
    public static double uncontrolledExplosionPower;
    public static boolean uncontrolledExplosionBreaksBlocks;
    public static int witherAmplifier;
    public static int uncontrolledSizeIntervalMinTicks;
    public static int uncontrolledSizeIntervalMaxTicks;
    public static double uncontrolledSizeMinScale;
    public static double uncontrolledSizeMaxScale;
    public static double sizeControlScrollStep;
    public static double sizeControlMinScale;
    public static double sizeControlMaxScale;
    public static int cryoMaxCharges;
    public static int cryoChargeRegenTicks;
    public static double cryoBallSpeed;
    public static double cryoBallDamage;
    public static int cryoFreezeTicks;
    public static int cryoSlownessAmplifier;
    public static double cryoBounceDamping;
    public static int cryoLifetimeTicks;
    public static boolean cryoChargeEnabled;
    public static double cryoChargedImpactDamage;
    public static int cryoMaxChargeTime;
    public static int cryoChargedMinHoldTicks;
    public static double cryoChargedBallSpeed;
    public static double cryoChargedFreezeRadius;
    public static int cryoChargedFreezeTicks;
    public static double cryoChargedBurstDamage;
    public static double cryoChargedBurstKnockback;
    public static boolean cryoBallFreezesWater;
    public static int cryoBallFreezeRadius;
    public static double cryoAuraRadius;
    public static int cryoAuraSlownessAmplifier;
    public static double cryoAuraSelfSlow;
    public static boolean cryoAuraFreezesWater;
    public static double aimlockRange;
    public static double aimlockHomingStrength;
    public static double healingSelfPerTick;
    public static int healingSelfDelayTicks;
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
    public static double sonicScreamDamage;
    public static double sonicScreamPlayerDamageMult;
    public static int sonicScreamCooldownTicks;
    public static int weightHeadPop;
    public static int weightEnhancedRegen;
    public static int weightDensity;
    public static int weightSlime;
    public static double slimeBounceFactor;
    public static double slimeMaxBounce;
    public static double slimeBounceMinImpact;
    public static int slimeJumpAmplifier;
    public static int slimeJumpDuration;
    public static double slimeHopSpeed;
    public static boolean slimeAlwaysActive;
    public static double slimeDamageTaken;
    public static double slimeKnockbackTaken;
    public static double slimeExplosionKnockback;
    public static double densityDamageMultiplier;
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
    public static int weightLuck;
    public static int weightSpider;
    public static int weightForcefield;
    public static int weightNullify;
    public static int weightChestBlast;
    public static double chestBlastBeamDamage;
    public static double chestBlastForcefieldDamage;
    public static double chestBlastForcefieldKnockback;
    public static double chestBlastBurstDamage;
    public static int chestBlastDuration;
    public static int chestBlastChargeTime;
    public static int chestBlastCooldown;
    public static int chestBlastRange;
    public static double chestBlastBlockBreakChance;
    public static boolean chestBlastStripsInvincible;
    public static int mobChestBlastWeight;
    public static boolean mobChestBlastStripsPowers;
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
    public static boolean persistFailureEffectsOnDeath;
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
        levelUpOnDrink = LEVEL_UP_ON_DRINK.get();
        playerPickupEnabled = PLAYER_PICKUP_ENABLED.get();
        playerPickupRequiresCompoundV = PLAYER_PICKUP_REQUIRES_COMPOUND_V.get();
        playerPickupSizeRatio = PLAYER_PICKUP_SIZE_RATIO.get();
        v1LevelUpMaxed = V1_LEVEL_UP_MAXED.get();
        // Build the V1 upgrade map. Addon-registered paths (via the public API) are applied first,
        // then config-defined paths layer on top so server owners can always override an addon.
        // Unknown config ids are skipped with a warning; the default namespace is compound_v.
        v1UpgradePaths = new java.util.HashMap<>();
        v1UpgradePaths.putAll(blueduck.compound_v.api.CompoundVUpgrades.getRegisteredPaths());
        for (String entry : V1_UPGRADE_PATHS.get()) {
            String[] parts = entry.split("->");
            if (parts.length != 2) continue;
            net.minecraft.world.effect.MobEffect from = resolveEffectId(parts[0].trim());
            net.minecraft.world.effect.MobEffect to = resolveEffectId(parts[1].trim());
            if (from != null && to != null) {
                v1UpgradePaths.put(from, to);
            } else {
                com.mojang.logging.LogUtils.getLogger().warn(
                        "Compound V: ignoring invalid v1UpgradePaths entry '{}'", entry);
            }
        }
        tempVBadOutcomeChance = TEMP_V_BAD_REACTION_CHANCE.get();
        // Tier system
        String tierStr = DEFAULT_POWER_TIER.get().toUpperCase();
        try { defaultPowerTier = CompoundVEffect.PowerTier.valueOf(tierStr); } catch (Exception e) { defaultPowerTier = CompoundVEffect.PowerTier.C; }
        tierStats = new double[6][8];
        tierStats[0] = new double[]{ TIER_SP_DR.get(), TIER_SP_DR_PL.get(), TIER_SP_STR.get(), TIER_SP_STR_PL.get(), TIER_SP_KB.get(), TIER_SP_KB_PL.get(), TIER_SP_KBD.get(), TIER_SP_KBD_PL.get() };
        tierStats[1] = new double[]{ TIER_S_DR.get(), TIER_S_DR_PL.get(), TIER_S_STR.get(), TIER_S_STR_PL.get(), TIER_S_KB.get(), TIER_S_KB_PL.get(), TIER_S_KBD.get(), TIER_S_KBD_PL.get() };
        tierStats[2] = new double[]{ TIER_A_DR.get(), TIER_A_DR_PL.get(), TIER_A_STR.get(), TIER_A_STR_PL.get(), TIER_A_KB.get(), TIER_A_KB_PL.get(), TIER_A_KBD.get(), TIER_A_KBD_PL.get() };
        tierStats[3] = new double[]{ TIER_B_DR.get(), TIER_B_DR_PL.get(), TIER_B_STR.get(), TIER_B_STR_PL.get(), TIER_B_KB.get(), TIER_B_KB_PL.get(), TIER_B_KBD.get(), TIER_B_KBD_PL.get() };
        tierStats[4] = new double[]{ TIER_C_DR.get(), TIER_C_DR_PL.get(), TIER_C_STR.get(), TIER_C_STR_PL.get(), TIER_C_KB.get(), TIER_C_KB_PL.get(), TIER_C_KBD.get(), TIER_C_KBD_PL.get() };
        tierStats[5] = new double[]{ TIER_D_DR.get(), TIER_D_DR_PL.get(), TIER_D_STR.get(), TIER_D_STR_PL.get(), TIER_D_KB.get(), TIER_D_KB_PL.get(), TIER_D_KBD.get(), TIER_D_KBD_PL.get() };
        // Multi-power
        enableMultiPowers = ENABLE_MULTI_POWERS.get(); multiPowerMaxCount = MULTI_POWER_MAX_COUNT.get();
        irradiationWeakensSupes = IRRADIATION_WEAKENS_SUPES.get();
        irradiationWeakenMinLevel = IRRADIATION_WEAKEN_MIN_LEVEL.get();
        tempVEnableMultiPowers = TEMP_V_ENABLE_MULTI_POWERS.get(); tempVMultiPowerMaxCount = TEMP_V_MULTI_POWER_MAX_COUNT.get();
        mobEnableMultiPowers = MOB_ENABLE_MULTI_POWERS.get(); mobMultiPowerMaxCount = MOB_MULTI_POWER_MAX_COUNT.get();
        weightGeneric = WEIGHT_GENERIC.get();
        weightSpeedster = WEIGHT_SPEEDSTER.get();
        speedsterMobAttack = SPEEDSTER_SPEED_ATTACK.get();
        speedsterSpeedLevelsPerAmp = SPEEDSTER_SPEED_LEVELS_PER_AMP.get();
        speedsterSprintDamage = SPEEDSTER_SPRINT_DAMAGE.get();
        speedsterSprintDamagePerAmp = SPEEDSTER_SPRINT_DAMAGE_PER_AMP.get();
        speedsterPlayerDamageMult = SPEEDSTER_PLAYER_DAMAGE_MULT.get();
        weightWater = WEIGHT_WATER.get();
        weightTeleport = WEIGHT_TELEPORT.get();
        weightAimlock = WEIGHT_AIMLOCK.get();
        aimlockRange = AIMLOCK_RANGE.get();
        aimlockHomingStrength = AIMLOCK_HOMING_STRENGTH.get();
        weightPyrokinesis = WEIGHT_PYROKINESIS.get();
        pyroMaxCharges = PYRO_MAX_CHARGES.get();
        pyroChargeRegenTicks = PYRO_CHARGE_REGEN_TICKS.get();
        pyroChargeEnabled = PYRO_CHARGE_ENABLED.get();
        pyroMaxChargeTime = PYRO_MAX_CHARGE_TIME.get();
        pyroMinExplosion = PYRO_MIN_EXPLOSION.get();
        pyroMaxExplosion = PYRO_MAX_EXPLOSION.get();
        pyroMinSpeed = PYRO_MIN_SPEED.get();
        pyroMaxSpeed = PYRO_MAX_SPEED.get();
        pyroFireballBreaksBlocks = PYRO_FIREBALL_BREAKS_BLOCKS.get();
        pyroFlameWaveRadius = PYRO_FLAME_WAVE_RADIUS.get();
        pyroFlameWaveDamage = PYRO_FLAME_WAVE_DAMAGE.get();
        pyroFlameWaveFireSeconds = PYRO_FLAME_WAVE_FIRE_SECONDS.get();
        pyroFlameWaveCooldown = PYRO_FLAME_WAVE_COOLDOWN.get();
        weightCryokinesis = WEIGHT_CRYOKINESIS.get();
        weightLifesteal = WEIGHT_LIFESTEAL.get();
        java.util.List<? extends Double> lsFractions = LIFESTEAL_LEVEL_FRACTIONS.get();
        if (lsFractions == null || lsFractions.isEmpty()) {
            lifestealLevelFractions = new double[]{ 0.2, 0.4, 0.6 };
        } else {
            lifestealLevelFractions = new double[lsFractions.size()];
            for (int i = 0; i < lsFractions.size(); i++) lifestealLevelFractions[i] = lsFractions.get(i);
        }
        lifestealBlocksAllHealing = LIFESTEAL_BLOCKS_ALL_HEALING.get();
        weightTelekinesis = WEIGHT_TELEKINESIS.get();
        telekinesisRange = TELEKINESIS_RANGE.get();
        telekinesisHoldDistance = TELEKINESIS_HOLD_DISTANCE.get();
        telekinesisHoldStrength = TELEKINESIS_HOLD_STRENGTH.get();
        telekinesisLaunchForce = TELEKINESIS_LAUNCH_FORCE.get();
        telekinesisLaunchRange = TELEKINESIS_LAUNCH_RANGE.get();
        telekinesisLaunchDamage = TELEKINESIS_LAUNCH_DAMAGE.get();
        telekinesisGrabsPlayers = TELEKINESIS_GRABS_PLAYERS.get();
        telekinesisScrollMinDistance = TELEKINESIS_SCROLL_MIN.get();
        telekinesisScrollMaxDistance = TELEKINESIS_SCROLL_MAX.get();
        weightSizeControlAdvanced = WEIGHT_SIZE_CONTROL.get();
        spiderWebSpeed = SPIDER_WEB_SPEED.get();
        spiderWallClimbEnabled = SPIDER_WALL_CLIMB_ENABLED.get();
        spiderCeilingClimbEnabled = SPIDER_CEILING_CLIMB_ENABLED.get();
        spiderClimbSpeed = SPIDER_CLIMB_SPEED.get();
        spiderClimbStickGap = SPIDER_CLIMB_STICK_GAP.get();
        spiderClimbLookRelative = SPIDER_CLIMB_LOOK_RELATIVE.get();
        spiderReelGroundLift = SPIDER_REEL_GROUND_LIFT.get();
        spiderWallJumpEnabled = SPIDER_WALL_JUMP_ENABLED.get();
        spiderWallJumpPower = SPIDER_WALL_JUMP_POWER.get();
        spiderWallJumpLift = SPIDER_WALL_JUMP_LIFT.get();
        spiderRaycastWebbing = SPIDER_RAYCAST_WEBBING.get();
        spiderWebFallCompensation = SPIDER_WEB_FALL_COMPENSATION.get();
        spiderFireCooldown = SPIDER_FIRE_COOLDOWN.get();
        spiderMobWebStuckMaxTicks = SPIDER_MOB_WEB_STUCK_MAX_TICKS.get();
        spiderMobWebTrapChance = SPIDER_MOB_WEB_TRAP_CHANCE.get();
        spiderMobWebTrapCooldown = SPIDER_MOB_WEB_TRAP_COOLDOWN.get();
        spiderMinRope = SPIDER_MIN_ROPE.get();
        spiderMaxRope = SPIDER_MAX_ROPE.get();
        spiderReelStep = SPIDER_REEL_STEP.get();
        spiderReelPull = SPIDER_REEL_PULL.get();
        spiderReelMassEnabled = SPIDER_REEL_MASS_ENABLED.get();
        spiderMobReelSpring = SPIDER_MOB_REEL_SPRING.get();
        spiderMobReelMaxSpeed = SPIDER_MOB_REEL_MAX_SPEED.get();
        spiderReelHealthWeight = SPIDER_REEL_HEALTH_WEIGHT.get();
        spiderReelSizeWeight = SPIDER_REEL_SIZE_WEIGHT.get();
        spiderReelMaxMass = SPIDER_REEL_MAX_MASS.get();
        spiderSwingControl = SPIDER_SWING_CONTROL.get();
        spiderSwingJumpEnabled = SPIDER_SWING_JUMP_ENABLED.get();
        spiderSwingJumpGroundGrace = SPIDER_SWING_JUMP_GROUND_GRACE.get();
        spiderSwingJumpRefires = SPIDER_SWING_JUMP_REFIRES.get();
        spiderSwingJumpMomentum = SPIDER_SWING_JUMP_MOMENTUM.get();
        spiderSwingJumpLift = SPIDER_SWING_JUMP_LIFT.get();
        spiderSwingPump = SPIDER_SWING_PUMP.get();
        spiderSwingGravity = SPIDER_SWING_GRAVITY.get();
        spiderWebGravityMult = SPIDER_WEB_GRAVITY_MULT.get();
        spiderMaxSwingSpeed = SPIDER_MAX_SWING_SPEED.get();
        spiderSwingBoost = SPIDER_SWING_BOOST.get();
        spiderFlingForce = SPIDER_FLING_FORCE.get();
        spiderFlingDamage = SPIDER_FLING_DAMAGE.get();
        spiderSlamPitchThreshold = SPIDER_SLAM_PITCH_THRESHOLD.get();
        spiderSlamForce = SPIDER_SLAM_FORCE.get();
        spiderSlamDamage = SPIDER_SLAM_DAMAGE.get();
        spiderSenseEnabled = SPIDER_SENSE_ENABLED.get();
        spiderSenseRadius = SPIDER_SENSE_RADIUS.get();
        spiderSenseScanInterval = SPIDER_SENSE_SCAN_INTERVAL.get();
        spiderSenseDetectProjectiles = SPIDER_SENSE_DETECT_PROJECTILES.get();
        spiderSenseDetectAggro = SPIDER_SENSE_DETECT_AGGRO.get();
        spiderSenseDetectCreepers = SPIDER_SENSE_DETECT_CREEPERS.get();
        spiderSenseDodgePayoff = SPIDER_SENSE_DODGE_PAYOFF.get();
        spiderSenseDamageMultiplier = SPIDER_SENSE_DR.get();
        spiderSenseWindowTicks = SPIDER_SENSE_WINDOW.get();
        spiderSenseCooldownTicks = SPIDER_SENSE_COOLDOWN.get();
        mobSpeciesBiasEnabled = MOB_SPECIES_BIAS_ENABLED.get();
        mobSpeciesBiasMultiplier = MOB_SPECIES_BIAS_MULTIPLIER.get();
        mobWeightLaserBasic = MOB_WEIGHT_LASER_BASIC.get();
        mobWeightEnhancedRegen = MOB_WEIGHT_ENHANCED_REGEN.get();
        mobWeightInvisibility = MOB_WEIGHT_INVISIBILITY.get();
        mobWeightProjectileImmunity = MOB_WEIGHT_PROJECTILE_IMMUNITY.get();
        mobWeightSpeedster = MOB_WEIGHT_SPEEDSTER.get();
        mobWeightTeleport = MOB_WEIGHT_TELEPORT.get();
        mobWeightCreativeFlight = MOB_WEIGHT_CREATIVE_FLIGHT.get();
        mobWeightShrink = MOB_WEIGHT_SHRINK.get();
        mobWeightEnlarge = MOB_WEIGHT_ENLARGE.get();
        mobWeightSizeControlAdvanced = MOB_WEIGHT_SIZE_CONTROL_ADVANCED.get();
        mobWeightLeap = MOB_WEIGHT_LEAP.get();
        mobWeightSlime = MOB_WEIGHT_SLIME.get();
        mobWeightSpider = MOB_WEIGHT_SPIDER.get();
        mobWeightHealing = MOB_WEIGHT_HEALING.get();
        mobWeightLifesteal = MOB_WEIGHT_LIFESTEAL.get();
        mobWeightDeep = MOB_WEIGHT_DEEP.get();
        mobWeightBerserker = MOB_WEIGHT_BERSERKER.get();
        mobWeightInvincible = MOB_WEIGHT_INVINCIBLE.get();
        mobWeightAtomCharging = MOB_WEIGHT_ATOM_CHARGING.get();
        mobWeightMagnetism = MOB_WEIGHT_MAGNETISM.get();
        mobWeightLaserAdvanced = MOB_WEIGHT_LASER_ADVANCED.get();
        mobWeightChestBlast = MOB_WEIGHT_CHEST_BLAST.get();
        mobWeightExplosive = MOB_WEIGHT_EXPLOSIVE.get();
        mobWeightForcefield = MOB_WEIGHT_FORCEFIELD.get();
        mobWeightPyrokinesis = MOB_WEIGHT_PYROKINESIS.get();
        mobWeightCryokinesis = MOB_WEIGHT_CRYOKINESIS.get();
        weightHeadPopFailure = WEIGHT_HEAD_POP_FAILURE.get();
        weightUncontrolledExplosion = WEIGHT_UNCONTROLLED_EXPLOSION.get();
        weightWither = WEIGHT_WITHER.get();
        weightFailBlindness = WEIGHT_FAIL_BLINDNESS.get();
        weightFailFloating = WEIGHT_FAIL_FLOATING.get();
        weightFailMagnetism = WEIGHT_FAIL_MAGNETISM.get();
        weightFailSlowness = WEIGHT_FAIL_SLOWNESS.get();
        weightFailUncontrolledTeleport = WEIGHT_FAIL_UNCONTROLLED_TELEPORT.get();
        weightUncontrolledSize = WEIGHT_UNCONTROLLED_SIZE.get();
        uncontrolledExplosionPower = UNCONTROLLED_EXPLOSION_POWER.get();
        uncontrolledExplosionBreaksBlocks = UNCONTROLLED_EXPLOSION_BREAKS_BLOCKS.get();
        witherAmplifier = WITHER_AMPLIFIER.get();
        uncontrolledSizeIntervalMinTicks = UNCONTROLLED_SIZE_INTERVAL_MIN.get();
        uncontrolledSizeIntervalMaxTicks = UNCONTROLLED_SIZE_INTERVAL_MAX.get();
        uncontrolledSizeMinScale = UNCONTROLLED_SIZE_MIN.get();
        uncontrolledSizeMaxScale = UNCONTROLLED_SIZE_MAX.get();
        sizeControlScrollStep = SIZE_CONTROL_SCROLL_STEP.get();
        sizeControlMinScale = SIZE_CONTROL_MIN_SCALE.get();
        sizeControlMaxScale = SIZE_CONTROL_MAX_SCALE.get();
        cryoMaxCharges = CRYO_MAX_CHARGES.get();
        cryoChargeRegenTicks = CRYO_CHARGE_REGEN_TICKS.get();
        cryoBallSpeed = CRYO_BALL_SPEED.get();
        cryoBallDamage = CRYO_BALL_DAMAGE.get();
        cryoFreezeTicks = CRYO_FREEZE_TICKS.get();
        cryoSlownessAmplifier = CRYO_SLOWNESS_AMPLIFIER.get();
        cryoBounceDamping = CRYO_BOUNCE_DAMPING.get();
        cryoLifetimeTicks = CRYO_LIFETIME_TICKS.get();
        cryoChargeEnabled = CRYO_CHARGE_ENABLED.get();
        cryoChargedImpactDamage = CRYO_CHARGED_IMPACT_DAMAGE.get();
        cryoMaxChargeTime = CRYO_MAX_CHARGE_TIME.get();
        cryoChargedMinHoldTicks = CRYO_CHARGED_MIN_HOLD.get();
        cryoChargedBallSpeed = CRYO_CHARGED_BALL_SPEED.get();
        cryoChargedFreezeRadius = CRYO_CHARGED_FREEZE_RADIUS.get();
        cryoChargedFreezeTicks = CRYO_CHARGED_FREEZE_TICKS.get();
        cryoChargedBurstDamage = CRYO_CHARGED_BURST_DAMAGE.get();
        cryoChargedBurstKnockback = CRYO_CHARGED_BURST_KNOCKBACK.get();
        cryoBallFreezesWater = CRYO_BALL_FREEZES_WATER.get();
        cryoBallFreezeRadius = CRYO_BALL_FREEZE_RADIUS.get();
        cryoAuraRadius = CRYO_AURA_RADIUS.get();
        cryoAuraSlownessAmplifier = CRYO_AURA_SLOWNESS_AMPLIFIER.get();
        cryoAuraSelfSlow = CRYO_AURA_SELF_SLOW.get();
        cryoAuraFreezesWater = CRYO_AURA_FREEZES_WATER.get();
        healingSelfPerTick = HEALING_SELF_PER_TICK.get();
        healingSelfDelayTicks = HEALING_SELF_DELAY_TICKS.get();
        teleportRange = TELEPORT_RANGE.get();
        teleportCooldown = TELEPORT_COOLDOWN.get();
        forcefieldMaxHp = FORCEFIELD_MAX_HP.get();
        forcefieldRegenPerTick = FORCEFIELD_REGEN_PER_TICK.get();
        forcefieldRegenLockoutTicks = FORCEFIELD_REGEN_LOCKOUT_TICKS.get();
        forcefieldDamageMultiplier = FORCEFIELD_DAMAGE_MULTIPLIER.get();
        forcefieldReenablePercent = FORCEFIELD_REENABLE_PERCENT.get();
        forcefieldBreakDamage = FORCEFIELD_BREAK_DAMAGE.get();
        forcefieldBreakAoeDamage = FORCEFIELD_BREAK_AOE_DAMAGE.get();
        forcefieldBreakAoeRadius = FORCEFIELD_BREAK_AOE_RADIUS.get();
        forcefieldPushRadius = FORCEFIELD_PUSH_RADIUS.get();
        forcefieldActiveSlowness = FORCEFIELD_SLOWNESS.get();
        nullifyRadius = NULLIFY_RADIUS.get();
        laserBasicPushEnabled = LASER_BASIC_PUSH_ENABLED.get(); laserBasicPushStrength = LASER_BASIC_PUSH_STRENGTH.get();
        laserBasicShieldPushMultiplier = LASER_BASIC_SHIELD_PUSH_MULTIPLIER.get();
        laserAdvancedPushEnabled = LASER_ADVANCED_PUSH_ENABLED.get(); laserAdvancedPushStrength = LASER_ADVANCED_PUSH_STRENGTH.get();
        laserAdvancedShieldPushMultiplier = LASER_ADVANCED_SHIELD_PUSH_MULTIPLIER.get();
        laserBasicDamageTickRate = LASER_BASIC_DAMAGE_TICK_RATE.get(); laserAdvancedDamageTickRate = LASER_ADVANCED_DAMAGE_TICK_RATE.get();
        laserDisabledWhileMoving = LASER_DISABLED_WHILE_MOVING.get();
        laserFirstPersonOpacity = LASER_FIRST_PERSON_OPACITY.get();
        laserIntimidationFirstPerson = LASER_INTIMIDATION_FIRST_PERSON.get();
        laserMoveSpeedThreshold = LASER_MOVE_SPEED_THRESHOLD.get();
        laserIntensityAdjustable = LASER_INTENSITY_ADJUSTABLE.get();
        laserIntensityScrollStep = LASER_INTENSITY_SCROLL_STEP.get();
        laserBreakCriticalIntensity = LASER_BREAK_CRITICAL.get();
        laserFireCriticalIntensity = LASER_FIRE_CRITICAL.get();
        laserIgniteEnabled = LASER_IGNITE_ENABLED.get();
        laserIgniteChance = LASER_IGNITE_CHANCE.get();
        laserIgniteCriticalIntensity = LASER_IGNITE_CRITICAL.get();
        laserIgniteEntitySeconds = LASER_IGNITE_ENTITY_SECONDS.get();
        laserBasicBreakBlocks = LASER_BASIC_BREAK_BLOCKS.get(); laserAdvancedBreakBlocks = LASER_ADVANCED_BREAK_BLOCKS.get();
        laserBlockBreakChance = LASER_BLOCK_BREAK_CHANCE.get(); laserBlockBreakDrops = LASER_BLOCK_BREAK_DROPS.get();
        laserHeartbeatEnabled = LASER_HEARTBEAT_ENABLED.get();
        laserHeartbeatRange = LASER_HEARTBEAT_RANGE.get();
        laserHeartbeatHealthThreshold = LASER_HEARTBEAT_HEALTH_THRESHOLD.get();
        laserHeartbeatSlowInterval = LASER_HEARTBEAT_SLOW_INTERVAL.get();
        laserHeartbeatFastInterval = LASER_HEARTBEAT_FAST_INTERVAL.get();
        laserBreakSpeed = LASER_BREAK_SPEED.get();
        laserBreakHardnessWeight = LASER_BREAK_HARDNESS_WEIGHT.get();
        laserBreakResistanceWeight = LASER_BREAK_RESISTANCE_WEIGHT.get();
        laserBreakDecay = LASER_BREAK_DECAY.get();
        chestBlastBlockBreakDrops = CHEST_BLAST_BLOCK_BREAK_DROPS.get();
        chestBlastNovaBlockBreakDrops = CHEST_BLAST_NOVA_BLOCK_BREAK_DROPS.get();
        chestBlastNovaBreaksBlocks = CHEST_BLAST_NOVA_BREAKS_BLOCKS.get();
        chestBlastNovaEnabled = CHEST_BLAST_NOVA_ENABLED.get();
        chestBlastNovaGuaranteedBreakFraction = CHEST_BLAST_NOVA_GUARANTEED_BREAK_FRACTION.get();
        laserColorCommandOpOnly = LASER_COLOR_COMMAND_OP_ONLY.get();
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
        sonicScreamDamage = SONIC_SCREAM_DAMAGE.get();
        sonicScreamPlayerDamageMult = SONIC_SCREAM_PLAYER_DAMAGE_MULT.get();
        sonicScreamCooldownTicks = SONIC_SCREAM_COOLDOWN_TICKS.get();
        weightHeadPop = WEIGHT_HEAD_POP.get();
        weightEnhancedRegen = WEIGHT_ENHANCED_REGEN.get();
        weightDensity = WEIGHT_DENSITY.get();
        weightSlime = WEIGHT_SLIME.get();
        slimeBounceFactor = SLIME_BOUNCE_FACTOR.get();
        slimeMaxBounce = SLIME_MAX_BOUNCE.get();
        slimeBounceMinImpact = SLIME_BOUNCE_MIN_IMPACT.get();
        slimeJumpAmplifier = SLIME_JUMP_AMPLIFIER.get();
        slimeJumpDuration = SLIME_JUMP_DURATION.get();
        slimeHopSpeed = SLIME_HOP_SPEED.get();
        slimeAlwaysActive = SLIME_ALWAYS_ACTIVE.get();
        slimeDamageTaken = SLIME_DAMAGE_TAKEN.get();
        slimeKnockbackTaken = SLIME_KNOCKBACK_TAKEN.get();
        slimeExplosionKnockback = SLIME_EXPLOSION_KNOCKBACK.get();
        densityDamageMultiplier = DENSITY_DAMAGE_REDUCTION.get();
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
        weightLuck = WEIGHT_LUCK.get();
        weightSpider = WEIGHT_SPIDER.get();
        weightForcefield = WEIGHT_FORCEFIELD.get();
        weightNullify = WEIGHT_NULLIFY.get();
        weightChestBlast = WEIGHT_CHEST_BLAST.get();
        v1WeightAimlock = V1_W_AIMLOCK.get();
        v1WeightAtomCharging = V1_W_ATOM_CHARGING.get();
        v1WeightBerserker = V1_W_BERSERKER.get();
        v1WeightChestBlast = V1_W_CHEST_BLAST.get();
        v1WeightCreativeFlight = V1_W_CREATIVE_FLIGHT.get();
        v1WeightCryokinesis = V1_W_CRYOKINESIS.get();
        v1WeightDeep = V1_W_DEEP.get();
        v1WeightDensity = V1_W_DENSITY.get();
        v1WeightSlime = V1_W_SLIME.get();
        v1WeightEnhancedRegen = V1_W_ENHANCED_REGEN.get();
        v1WeightEnlarge = V1_W_ENLARGE.get();
        v1WeightExplosive = V1_W_EXPLOSIVE.get();
        v1WeightForcefield = V1_W_FORCEFIELD.get();
        v1WeightNullify = V1_W_NULLIFY.get();
        v1WeightGeneric = V1_W_GENERIC.get();
        v1WeightHeadPop = V1_W_HEAD_POP.get();
        v1WeightHealing = V1_W_HEALING.get();
        v1WeightInstakill = V1_W_INSTAKILL.get();
        v1WeightInvincible = V1_W_INVINCIBLE.get();
        v1WeightInvisibility = V1_W_INVISIBILITY.get();
        v1WeightLaserAdvanced = V1_W_LASER_EYES_ADVANCED.get();
        v1WeightLaserBasic = V1_W_LASER_EYES_BASIC.get();
        v1WeightLeap = V1_W_LEAP.get();
        v1WeightLevitation = V1_W_LEVITATION.get();
        v1WeightLifesteal = V1_W_LIFESTEAL.get();
        v1WeightLuck = V1_W_LUCK.get();
        v1WeightMimic = V1_W_MIMIC.get();
        v1WeightMindControl = V1_W_MIND_CONTROL.get();
        v1WeightNightVision = V1_W_NIGHT_VISION.get();
        v1WeightPetrifyingGaze = V1_W_PETRIFYING_GAZE.get();
        v1WeightPowerAbsorption = V1_W_POWER_ABSORPTION.get();
        v1WeightProjectileImmunity = V1_W_PROJECTILE_IMMUNITY.get();
        v1WeightPyrokinesis = V1_W_PYROKINESIS.get();
        v1WeightShrink = V1_W_SHRINK.get();
        v1WeightSizeControl = V1_W_SIZE_CONTROL_ADVANCED.get();
        v1WeightSonicScream = V1_W_SONIC_SCREAM.get();
        v1WeightSpeedster = V1_W_SPEEDSTER.get();
        v1WeightSpider = V1_W_SPIDER.get();
        v1WeightStarPower = V1_W_STAR_POWER.get();
        v1WeightStormfront = V1_W_STORMFRONT.get();
        v1WeightTelekinesis = V1_W_TELEKINESIS.get();
        v1WeightTeleport = V1_W_TELEPORT.get();
        chestBlastBeamDamage = CHEST_BLAST_BEAM_DAMAGE.get();
        chestBlastForcefieldDamage = CHEST_BLAST_FORCEFIELD_DAMAGE.get();
        chestBlastForcefieldKnockback = CHEST_BLAST_FORCEFIELD_KNOCKBACK.get();
        chestBlastBurstDamage = CHEST_BLAST_BURST_DAMAGE.get();
        chestBlastDuration = CHEST_BLAST_DURATION.get();
        chestBlastChargeTime = CHEST_BLAST_CHARGE_TIME.get();
        chestBlastCooldown = CHEST_BLAST_COOLDOWN.get();
        chestBlastRange = CHEST_BLAST_RANGE.get();
        chestBlastBlockedByWalls = CHEST_BLAST_BLOCKED_BY_WALLS.get();
        chestBlastStripsPowers = CHEST_BLAST_STRIPS_POWERS.get();
        chestBlastShieldBlocksStrip = CHEST_BLAST_SHIELD_BLOCKS_STRIP.get();
        chestBlastNovaRadius = CHEST_BLAST_NOVA_RADIUS.get();
        chestBlastNovaPower = CHEST_BLAST_NOVA_POWER.get();
        chestBlastNovaDamage = CHEST_BLAST_NOVA_DAMAGE.get();
        chestBlastNovaKnockback = CHEST_BLAST_NOVA_KNOCKBACK.get();
        chestBlastNovaChargeTime = CHEST_BLAST_NOVA_CHARGE_TIME.get();
        virusDisablesPlayerPowers = VIRUS_DISABLES_PLAYER_POWERS.get(); virusDisablesMobPowers = VIRUS_DISABLES_MOB_POWERS.get();
        mobLaserDamage = MOB_LASER_DAMAGE_CONFIG.get(); mobAdvancedLaserDamage = MOB_ADVANCED_LASER_DAMAGE_CONFIG.get();
        powerplexDischargeDamage = POWERPLEX_DISCHARGE_DAMAGE.get(); powerplexDischargeRadius = POWERPLEX_DISCHARGE_RADIUS.get();
        powerplexDischargeTickRate = POWERPLEX_DISCHARGE_TICK_RATE.get();
        powerplexFocusDamage = POWERPLEX_FOCUS_DAMAGE.get();
        powerplexFocusRange = POWERPLEX_FOCUS_RANGE.get();
        stormfrontDischargeDamage = STORMFRONT_DISCHARGE_DAMAGE.get(); stormfrontDischargeRadius = STORMFRONT_DISCHARGE_RADIUS.get();
        stormfrontChainDamage = STORMFRONT_CHAIN_DAMAGE.get();
        stormfrontChainFalloff = STORMFRONT_CHAIN_FALLOFF.get();
        stormfrontChainMaxJumps = STORMFRONT_CHAIN_MAX_JUMPS.get();
        stormfrontChainJumpRange = STORMFRONT_CHAIN_JUMP_RANGE.get();
        stormfrontChainAimRange = STORMFRONT_CHAIN_AIM_RANGE.get();
        stormfrontDischargeTickRate = STORMFRONT_DISCHARGE_TICK_RATE.get();
        chestBlastBlockBreakChance = CHEST_BLAST_BLOCK_BREAK_CHANCE.get();
        chestBlastStripsInvincible = CHEST_BLAST_STRIPS_INVINCIBLE.get();
        mobPyroFireCooldown = MOB_PYRO_FIRE_COOLDOWN.get();
        mobCryoFireCooldown = MOB_CRYO_FIRE_COOLDOWN.get();
        mobProjectileRange = MOB_PROJECTILE_RANGE.get();
        mobPowerFriendlyFire = MOB_POWER_FRIENDLY_FIRE.get();
        mobPyroMaxCharges = MOB_PYRO_MAX_CHARGES.get();
        mobCryoMaxCharges = MOB_CRYO_MAX_CHARGES.get();
        mobChargeRegenTicks = MOB_CHARGE_REGEN_TICKS.get();
        mobChestBlastWeight = MOB_CHEST_BLAST_WEIGHT.get();
        mobChestBlastStripsPowers = MOB_CHEST_BLAST_STRIPS_POWERS.get();
        mobChestBlastInaccuracy = MOB_CHEST_BLAST_INACCURACY.get();
        mobDamageReduction = MOB_DAMAGE_REDUCTION.get();
        mobStrengthMultiplier = MOB_STRENGTH_MULTIPLIER.get();
        mobKnockbackReduction = MOB_KNOCKBACK_REDUCTION.get();
        friendlyMobDamageReduction = FRIENDLY_MOB_DAMAGE_REDUCTION.get();
        friendlyMobStrengthMultiplier = FRIENDLY_MOB_STRENGTH_MULTIPLIER.get();
        friendlyMobKnockbackReduction = FRIENDLY_MOB_KNOCKBACK_REDUCTION.get();
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
        persistFailureEffectsOnDeath = PERSIST_FAILURE_EFFECTS_ON_DEATH.get();
        virusRemovesPowerOnDeath = VIRUS_REMOVES_POWER_ON_DEATH.get();
        mobPowerSpawnChance = MOB_POWER_SPAWN_CHANCE.get();
        mobCompoundVDropChance = MOB_COMPOUND_V_DROP_CHANCE.get();
        mobTempVDropChance = MOB_TEMP_V_DROP_CHANCE.get();
        mobV1DropChance = MOB_V1_DROP_CHANCE.get();

        // Rebuild matrices on config reload
        try {
            if (blueduck.compound_v.registry.EffectReg.GENERIC.isPresent()) {
                blueduck.compound_v.registry.EffectReg.addEffectsToMatrix();
                buildEffectTierMap();
            }
        } catch (Exception ignored) {}
    }

    // --- Tier accessors ---
    public static double getTierDamageReduction(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][0]; }
    public static double getTierDamageReductionPerLevel(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][1]; }
    public static double getTierStrengthMultiplier(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][2]; }
    public static double getTierStrengthPerLevel(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][3]; }
    public static double getTierKnockbackReduction(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][4]; }
    public static double getTierKnockbackPerLevel(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][5]; }
    public static double getTierKnockbackDealt(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][6]; }
    public static double getTierKnockbackDealtPerLevel(CompoundVEffect.PowerTier t) { return tierStats[t.ordinal()][7]; }

    private static CompoundVEffect.PowerTier parseTier(String s) {
        if (s == null || s.equalsIgnoreCase("SPECIAL")) return null;
        try { return CompoundVEffect.PowerTier.valueOf(s.toUpperCase()); } catch (Exception e) { return defaultPowerTier; }
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
        mapTier(blueduck.compound_v.registry.EffectReg.AIMLOCK, TIER_AIMLOCK);
        mapTier(blueduck.compound_v.registry.EffectReg.PYROKINESIS, TIER_PYROKINESIS);
        mapTier(blueduck.compound_v.registry.EffectReg.CRYOKINESIS, TIER_CRYOKINESIS);
        mapTier(blueduck.compound_v.registry.EffectReg.SPEEDSTER, TIER_SPEEDSTER);
        mapTier(blueduck.compound_v.registry.EffectReg.LIFESTEAL, TIER_LIFESTEAL);
        mapTier(blueduck.compound_v.registry.EffectReg.TELEKINESIS, TIER_TELEKINESIS);
        mapTier(blueduck.compound_v.registry.EffectReg.SIZE_CONTROL_ADVANCED, TIER_SIZE_CONTROL);
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
        mapTier(blueduck.compound_v.registry.EffectReg.SLIME, TIER_SLIME);
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

    /** Resolve an effect registry id (default namespace compound_v) to a MobEffect, or null. */
    private static net.minecraft.world.effect.MobEffect resolveEffectId(String id) {
        if (id == null || id.isEmpty()) return null;
        net.minecraft.resources.ResourceLocation rl = id.contains(":")
                ? new net.minecraft.resources.ResourceLocation(id)
                : new net.minecraft.resources.ResourceLocation("compound_v", id);
        return net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(rl);
    }

    private static void mapTier(net.minecraftforge.registries.RegistryObject<net.minecraft.world.effect.MobEffect> effect, ForgeConfigSpec.ConfigValue<String> cfg) {
        CompoundVEffect.PowerTier tier = parseTier(cfg.get());
        if (tier != null && effect.isPresent()) effectTierMap.put(effect.get(), tier);
    }

    public static CompoundVEffect.PowerTier getEffectTier(net.minecraft.world.effect.MobEffect effect) {
        return effectTierMap.getOrDefault(effect, defaultPowerTier);
    }

    /** Called from commonSetup to ensure tier map is built after registries are ready. */
    public static void rebuildTierMap() {
        buildEffectTierMap();
    }
}
