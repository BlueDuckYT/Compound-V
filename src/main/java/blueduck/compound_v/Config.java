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
    private static final ForgeConfigSpec.IntValue MOB_WEIGHT_LIFESTEAL = BUILDER
            .comment("Universal mob-pool base weight: Lifesteal")
            .defineInRange("mobWeight.lifesteal", 1, 0, Integer.MAX_VALUE);
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
    private static final ForgeConfigSpec.BooleanValue ENABLE_MULTI_POWERS = BUILDER.comment("Allow Compound V to grant multiple powers").define("enableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("multiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue TEMP_V_ENABLE_MULTI_POWERS = BUILDER.define("tempVEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue TEMP_V_MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("tempVMultiPowerMaxCount", 2, 1, 3);
    private static final ForgeConfigSpec.BooleanValue MOB_ENABLE_MULTI_POWERS = BUILDER.define("mobEnableMultiPowers", false);
    private static final ForgeConfigSpec.IntValue MOB_MULTI_POWER_MAX_COUNT = BUILDER.defineInRange("mobMultiPowerMaxCount", 2, 1, 3);
    // --- Player pickup (big player carries smaller player) ---
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
    private static final ForgeConfigSpec.BooleanValue LEVEL_UP_ON_DRINK = BUILDER
            .comment("If true, drinking another permanent Compound V while already powered raises the level of your current effect(s) by 1, up to their max level.")
            .define("levelUpOnDrink", true);
    private static final ForgeConfigSpec.BooleanValue V1_LEVEL_UP_MAXED = BUILDER
            .comment("If true, using V1 on a multi-level power that is ALREADY at max level pushes it one level beyond its normal max (overcharge). Disabled by default.")
            .define("v1LevelUpMaxed", false);

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
    private static final ForgeConfigSpec.ConfigValue<String> TIER_SPIDER = BUILDER.define("tierOf.spider", "A");

    // Tier SP — legendary (no powers by default, custom assignable)
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

    // --- Combat ---

    // --- Effect Weights ---
    private static final ForgeConfigSpec.BooleanValue SPEEDSTER_SPEED_ATTACK = BUILDER
            .comment("Whether Speedster powers allow you to damage mobs while sprinting")
            .define("speedster_speed_attack", true);
    private static final ForgeConfigSpec.IntValue SPEEDSTER_SPEED_LEVELS_PER_AMP = BUILDER
            .comment("Speed effect levels added per Speedster amplifier (base Speed III at amp 0, +N per amp)")
            .defineInRange("speedsterSpeedLevelsPerAmp", 2, 0, 10);
    // --- Aimlock ---
    private static final ForgeConfigSpec.DoubleValue AIMLOCK_RANGE = BUILDER
            .comment("Maximum range (in blocks) at which Aimlock can lock onto a target")
            .defineInRange("aimlockRange", 96.0, 1.0, 512.0);
    private static final ForgeConfigSpec.DoubleValue AIMLOCK_HOMING_STRENGTH = BUILDER
            .comment("How aggressively homing projectiles turn toward the locked target each tick (0 = none, 1 = instant snap)")
            .defineInRange("aimlockHomingStrength", 0.25, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue TELEPORT_RANGE = BUILDER
            .comment("Range of the teleportation power")
            .defineInRange("teleport_range", 36, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue TELEPORT_COOLDOWN = BUILDER
            .comment("Cooldown in ticks after teleporting (0 = no cooldown)")
            .defineInRange("teleportCooldown", 0, 0, Integer.MAX_VALUE);
    // --- Pyrokinesis (Fire) ---
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
    // --- Cryokinesis (Ice) ---
    // --- Lifesteal ---
    private static final ForgeConfigSpec.ConfigValue<java.util.List<? extends Double>> LIFESTEAL_LEVEL_FRACTIONS = BUILDER
            .comment("Fraction of melee damage healed at each Lifesteal level (level 1 = first entry, level 2 = second, ...).",
                     "The number of entries determines how many levels Lifesteal has. Default: 0.2, 0.4, 0.6 (20%, 40%, 60%).")
            .defineList("lifestealLevelFractions",
                    java.util.Arrays.asList(0.2, 0.4, 0.6),
                    o -> o instanceof Double d && d >= 0.0 && d <= 5.0);
    private static final ForgeConfigSpec.BooleanValue LIFESTEAL_BLOCKS_ALL_HEALING = BUILDER
            .comment("If true, Lifesteal blocks ALL other healing (potions, golden apples, regen) so dealing melee damage is the ONLY way to heal. If false (default), only natural hunger-regen is blocked.")
            .define("lifestealBlocksAllHealing", false);
    private static final ForgeConfigSpec.BooleanValue LIFESTEAL_MOBS = BUILDER
            .comment("Allow mobs to spawn with Lifesteal (they heal when they hit you)")
            .define("mobPowerLifesteal", true);
    // --- Telekinesis ---
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
            .comment("A held entity is only launched (with force/damage) if it has been reeled within this distance of the player. Released harmlessly if farther — prevents damaging far-off mobs by spam-tapping. Should be slightly more than telekinesisHoldDistance.")
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
    // --- Negative effects (new) ---
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
    // --- Size Control (Advanced) ---
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_SCROLL_STEP = BUILDER
            .comment("How much the size changes per scroll notch")
            .defineInRange("sizeControlScrollStep", 0.15, 0.01, 2.0);
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_MIN_SCALE = BUILDER
            .comment("Smallest size reachable with Size Control")
            .defineInRange("sizeControlMinScale", 0.25, 0.05, 1.0);
    private static final ForgeConfigSpec.DoubleValue SIZE_CONTROL_MAX_SCALE = BUILDER
            .comment("Largest size reachable with Size Control")
            .defineInRange("sizeControlMaxScale", 4.0, 1.0, 8.0);
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
    // --- Forcefield ---
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_MAX_HP = BUILDER
            .comment("Maximum forcefield shield health")
            .defineInRange("forcefieldMaxHp", 100.0, 1.0, 10000.0);
    private static final ForgeConfigSpec.DoubleValue FORCEFIELD_REGEN_PER_TICK = BUILDER
            .comment("Forcefield shield health regenerated per tick (heals even while disabled)")
            .defineInRange("forcefieldRegenPerTick", 0.1, 0.0, 1000.0);
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
    // --- Nullify ---
    private static final ForgeConfigSpec.DoubleValue NULLIFY_RADIUS = BUILDER
            .comment("Radius (in blocks) of the nullification aura")
            .defineInRange("nullifyRadius", 10.0, 1.0, 64.0);
    // --- Laser Pushback ---
    private static final ForgeConfigSpec.BooleanValue LASER_BASIC_PUSH_ENABLED = BUILDER.define("laserBasicPushEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_PUSH_STRENGTH = BUILDER.defineInRange("laserBasicPushStrength", 0.02, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_BASIC_SHIELD_PUSH_MULTIPLIER = BUILDER.defineInRange("laserBasicShieldPushMultiplier", 3.0, 0.0, 20.0);
    private static final ForgeConfigSpec.BooleanValue LASER_ADVANCED_PUSH_ENABLED = BUILDER.define("laserAdvancedPushEnabled", true);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_PUSH_STRENGTH = BUILDER.defineInRange("laserAdvancedPushStrength", 0.04, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue LASER_ADVANCED_SHIELD_PUSH_MULTIPLIER = BUILDER.defineInRange("laserAdvancedShieldPushMultiplier", 4.0, 0.0, 20.0);
    private static final ForgeConfigSpec.IntValue LASER_BASIC_DAMAGE_TICK_RATE = BUILDER.defineInRange("laserBasicDamageTickRate", 1, 1, 40);
    private static final ForgeConfigSpec.BooleanValue LASER_DISABLED_WHILE_MOVING = BUILDER
            .comment("If true, lasers will NOT fire (no damage/breaking) while the player is moving",
                     "at a decent speed (running/sprinting) — they only show the harmless intimidation",
                     "glow until you slow down. A drawback: you can't laser on the move. Off by default.")
            .define("laserDisabledWhileMoving", false);
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
    private static final ForgeConfigSpec.IntValue LASER_ADVANCED_DAMAGE_TICK_RATE = BUILDER.defineInRange("laserAdvancedDamageTickRate", 1, 1, 40);
    // --- Laser Block Breaking ---
    private static final ForgeConfigSpec.BooleanValue LASER_BASIC_BREAK_BLOCKS = BUILDER
            .comment("Whether basic laser eyes break blocks along the beam (like chest blast)")
            .define("laserBasicBreakBlocks", false);
    private static final ForgeConfigSpec.BooleanValue LASER_ADVANCED_BREAK_BLOCKS = BUILDER
            .comment("Whether advanced laser eyes break blocks along the beam")
            .define("laserAdvancedBreakBlocks", true);
    private static final ForgeConfigSpec.DoubleValue LASER_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Chance per block per tick for laser block breaking (lower = slower carving)")
            .defineInRange("laserBlockBreakChance", 0.15, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue LASER_BLOCK_BREAK_DROPS = BUILDER
            .comment("Whether laser block breaking drops items (false = less lag)")
            .define("laserBlockBreakDrops", false);
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
    // --- Commands ---
    private static final ForgeConfigSpec.BooleanValue LASER_COLOR_COMMAND_OP_ONLY = BUILDER
            .comment("Restrict /lasercolor to operators only (even for setting your own color)")
            .define("laserColorCommandOpOnly", true);

    // --- New Powers ---
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
    private static final ForgeConfigSpec.IntValue STORMFRONT_LIGHTNING_COOLDOWN = BUILDER
            .comment("Cooldown (in ticks) between Stormfront lightning strikes (60 = 3 seconds)")
            .defineInRange("stormfrontLightningCooldown", 8, 0, Integer.MAX_VALUE);

    // Spider is now a real pool entry (no longer experimental).
    // private static final ForgeConfigSpec.IntValue WEIGHT_FORCEFIELD = ...
    // --- Spider web tuning (revamp) ---
    private static final ForgeConfigSpec.DoubleValue SPIDER_WEB_SPEED = BUILDER
            .comment("Launch speed of the web projectile").defineInRange("spiderWebSpeed", 2.8, 0.5, 10.0);
    private static final ForgeConfigSpec.IntValue SPIDER_FIRE_COOLDOWN = BUILDER
            .comment("Minimum ticks between web shots (20 = 1s). Prevents spamming webs by holding V.").defineInRange("spiderFireCooldown", 10, 0, 200);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MIN_ROPE = BUILDER
            .comment("Shortest the swing rope can be reeled to").defineInRange("spiderMinRope", 3.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MAX_ROPE = BUILDER
            .comment("Longest the swing rope can be extended to").defineInRange("spiderMaxRope", 40.0, 4.0, 128.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_STEP = BUILDER
            .comment("How much rope length changes per scroll notch").defineInRange("spiderReelStep", 0.5, 0.1, 16.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_REEL_PULL = BUILDER
            .comment("Inward pull strength when reeling the rope shorter").defineInRange("spiderReelPull", 0.18, 0.0, 2.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_SWING_CONTROL = BUILDER
            .comment("How strongly look direction steers the swing").defineInRange("spiderSwingControl", 0.04, 0.0, 0.5);
    private static final ForgeConfigSpec.DoubleValue SPIDER_MAX_SWING_SPEED = BUILDER
            .comment("Maximum swing speed (caps momentum)").defineInRange("spiderMaxSwingSpeed", 1.8, 0.2, 5.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_FLING_FORCE = BUILDER
            .comment("Launch force when flinging a reeled-in mob by punching it and releasing the web.")
            .defineInRange("spiderFlingForce", 2.5, 0.5, 10.0);
    private static final ForgeConfigSpec.DoubleValue SPIDER_FLING_DAMAGE = BUILDER
            .comment("Bonus damage dealt to a mob flung by the punch-and-release combo.")
            .defineInRange("spiderFlingDamage", 6.0, 0.0, 100.0);

    // --- Spider-Sense (passive bundled with the Spider power) ---
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

    // --- Mob power pool: species bias ---
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

    // --- Mob power pool: per-power BASE weights for the universal pool (every injectable
    //     mob). These are the weights BEFORE any species bias is added on top. Set to 0 to
    //     remove a power from the universal mob pool entirely. (Species blocks may still add
    //     it for thematic mobs, scaled by mobSpeciesBiasMultiplier.)

    // --- V1 Pool Weights ---
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
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_BLOCKED_BY_WALLS = BUILDER.define("chestBlastBlockedByWalls", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_POWERS = BUILDER.define("chestBlastStripsPowers", true);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_SHIELD_BLOCKS_STRIP = BUILDER.define("chestBlastShieldBlocksStrip", true);
    // --- Chest Blast Nova (sneak + V) ---
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_RADIUS = BUILDER
            .comment("Radius of the Soldier Boy nova depower/power-strip effect (sneak + V)")
            .defineInRange("chestBlastNovaRadius", 8.0, 2.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_POWER = BUILDER
            .comment("Explosion power of the nova burst (vanilla TNT is 4.0; higher = bigger blast/damage/crater)")
            .defineInRange("chestBlastNovaPower", 7.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_DAMAGE = BUILDER
            .comment("Damage dealt by the nova burst to powered entities")
            .defineInRange("chestBlastNovaDamage", 15.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_NOVA_KNOCKBACK = BUILDER
            .comment("Knockback strength of the nova burst")
            .defineInRange("chestBlastNovaKnockback", 2.5, 0.0, 10.0);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_NOVA_CHARGE_TIME = BUILDER
            .comment("Charge time in ticks for nova burst (sneak + hold V). Beam and nova share the same cooldown (chestBlastCooldown).")
            .defineInRange("chestBlastNovaChargeTime", 200, 0, Integer.MAX_VALUE);
    // --- Virus Integration ---
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_PLAYER_POWERS = BUILDER.define("virusDisablesPlayerPowers", true);
    private static final ForgeConfigSpec.BooleanValue VIRUS_DISABLES_MOB_POWERS = BUILDER.define("virusDisablesMobPowers", true);
    // --- Mob Laser ---
    private static final ForgeConfigSpec.DoubleValue MOB_LASER_DAMAGE_CONFIG = BUILDER.defineInRange("mobLaserDamage", 0.02, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue MOB_ADVANCED_LASER_DAMAGE_CONFIG = BUILDER.defineInRange("mobAdvancedLaserDamage", 0.06, 0.0, 10.0);
    // --- Discharge ---
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_DAMAGE = BUILDER.defineInRange("powerplexDischargeDamage", 2.0, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue POWERPLEX_DISCHARGE_RADIUS = BUILDER.defineInRange("powerplexDischargeRadius", 5.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue POWERPLEX_DISCHARGE_TICK_RATE = BUILDER.defineInRange("powerplexDischargeTickRate", 4, 1, 40);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_DAMAGE = BUILDER.defineInRange("stormfrontDischargeDamage", 1.6, 0.0, 50.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_DISCHARGE_RADIUS = BUILDER.defineInRange("stormfrontDischargeRadius", 6.0, 1.0, 32.0);
    private static final ForgeConfigSpec.IntValue STORMFRONT_DISCHARGE_TICK_RATE = BUILDER.defineInRange("stormfrontDischargeTickRate", 4, 1, 40);
    // Chain lightning (tap V at a mob in view)
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_DAMAGE = BUILDER.comment("Damage of the first chain-lightning hit").defineInRange("stormfrontChainDamage", 7.0, 0.0, 100.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_FALLOFF = BUILDER.comment("Damage multiplier applied per additional chain jump (0-1)").defineInRange("stormfrontChainFalloff", 0.8, 0.0, 1.0);
    private static final ForgeConfigSpec.IntValue STORMFRONT_CHAIN_MAX_JUMPS = BUILDER.comment("Maximum number of mobs a chain-lightning bolt arcs through").defineInRange("stormfrontChainMaxJumps", 5, 1, 32);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_JUMP_RANGE = BUILDER.comment("Max distance the bolt can arc from one mob to the next").defineInRange("stormfrontChainJumpRange", 8.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue STORMFRONT_CHAIN_AIM_RANGE = BUILDER.comment("Max distance to acquire the initial chain-lightning target in your view direction").defineInRange("stormfrontChainAimRange", 40.0, 1.0, 128.0);
    private static final ForgeConfigSpec.IntValue CHEST_BLAST_RANGE = BUILDER
            .comment("Range (in blocks) of the Chest Blast beam")
            .defineInRange("chestBlastRange", 32, 8, 256);
    private static final ForgeConfigSpec.DoubleValue CHEST_BLAST_BLOCK_BREAK_CHANCE = BUILDER
            .comment("Chance per tick per block of the Chest Blast beam destroying blocks in its path (0 = disabled). WARNING: Values above 0 cause significant lag due to per-tick block iteration across the beam cone.")
            .defineInRange("chestBlastBlockBreakChance", 0.0, 0.0, 1.0);
    private static final ForgeConfigSpec.BooleanValue CHEST_BLAST_STRIPS_INVINCIBLE = BUILDER
            .comment("Whether the Chest Blast beam can strip Invincibility (if false, Invincible blocks the strip)")
            .define("chestBlastStripsInvincible", true);
    private static final ForgeConfigSpec.BooleanValue MOB_CHEST_BLAST_STRIPS_POWERS = BUILDER
            .comment("Whether mob Chest Blast strips Compound V powers from targets (like the player version)")
            .define("mobChestBlastStripsPowers", false);
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
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_FORCEFIELD = BUILDER.comment("Allow mobs to spawn with Forcefield. Disabled — mobs cannot render the shield bubble.").define("mobPowerForcefield", false);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_PYROKINESIS = BUILDER.comment("Allow mobs to spawn with Pyrokinesis (fireballs + flame wave). Blazes are always exempt.").define("mobPowerPyrokinesis", true);
    private static final ForgeConfigSpec.BooleanValue MOB_POWER_CRYOKINESIS = BUILDER.comment("Allow mobs to spawn with Cryokinesis (ice balls + frost aura). Nether mobs are always exempt.").define("mobPowerCryokinesis", true);
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
    public static boolean mobPowerForcefield;
    public static boolean mobPowerPyrokinesis;
    public static boolean mobPowerCryokinesis;
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
    public static double tempVBadOutcomeChance;
    // Tier system
    public static CompoundVEffect.PowerTier defaultPowerTier;
    public static double[][] tierStats;
    public static java.util.Map<net.minecraft.world.effect.MobEffect, CompoundVEffect.PowerTier> effectTierMap = new java.util.HashMap<>();
    // Multi-power
    public static boolean enableMultiPowers;
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
    public static double laserMoveSpeedThreshold;
    public static boolean laserIntensityAdjustable;
    public static double laserIntensityScrollStep;
    public static double laserBreakCriticalIntensity;
    public static double laserFireCriticalIntensity;
    public static int laserAdvancedDamageTickRate;
    public static boolean laserBasicBreakBlocks;
    public static boolean laserAdvancedBreakBlocks;
    public static double laserBlockBreakChance;
    public static boolean laserBlockBreakDrops;
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
    public static boolean mobPowerLifesteal;
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
    public static int spiderFireCooldown;
    public static double spiderMinRope;
    public static double spiderMaxRope;
    public static double spiderReelStep;
    public static double spiderReelPull;
    public static double spiderSwingControl;
    public static double spiderMaxSwingSpeed;
    public static double spiderFlingForce;
    public static double spiderFlingDamage;
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
    public static int mobWeightLeap;
    public static int mobWeightSpider;
    public static int mobWeightHealing;
    public static int mobWeightLifesteal;
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
    public static double chestBlastBurstDamage;
    public static int chestBlastDuration;
    public static int chestBlastChargeTime;
    public static int chestBlastCooldown;
    public static int chestBlastRange;
    public static double chestBlastBlockBreakChance;
    public static boolean chestBlastStripsInvincible;
    public static boolean mobPowerCreativeFlight;
    public static boolean mobPowerChestBlast;
    public static boolean mobPowerLeap;
    public static boolean mobPowerExplosive;
    public static boolean mobPowerHealing;
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
        tempVEnableMultiPowers = TEMP_V_ENABLE_MULTI_POWERS.get(); tempVMultiPowerMaxCount = TEMP_V_MULTI_POWER_MAX_COUNT.get();
        mobEnableMultiPowers = MOB_ENABLE_MULTI_POWERS.get(); mobMultiPowerMaxCount = MOB_MULTI_POWER_MAX_COUNT.get();
        weightGeneric = WEIGHT_GENERIC.get();
        weightSpeedster = WEIGHT_SPEEDSTER.get();
        speedsterMobAttack = SPEEDSTER_SPEED_ATTACK.get();
        speedsterSpeedLevelsPerAmp = SPEEDSTER_SPEED_LEVELS_PER_AMP.get();
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
        mobPowerLifesteal = LIFESTEAL_MOBS.get();
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
        spiderFireCooldown = SPIDER_FIRE_COOLDOWN.get();
        spiderMinRope = SPIDER_MIN_ROPE.get();
        spiderMaxRope = SPIDER_MAX_ROPE.get();
        spiderReelStep = SPIDER_REEL_STEP.get();
        spiderReelPull = SPIDER_REEL_PULL.get();
        spiderSwingControl = SPIDER_SWING_CONTROL.get();
        spiderMaxSwingSpeed = SPIDER_MAX_SWING_SPEED.get();
        spiderFlingForce = SPIDER_FLING_FORCE.get();
        spiderFlingDamage = SPIDER_FLING_DAMAGE.get();
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
        mobWeightLeap = MOB_WEIGHT_LEAP.get();
        mobWeightSpider = MOB_WEIGHT_SPIDER.get();
        mobWeightHealing = MOB_WEIGHT_HEALING.get();
        mobWeightLifesteal = MOB_WEIGHT_LIFESTEAL.get();
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
        teleportRange = TELEPORT_RANGE.get();
        teleportCooldown = TELEPORT_COOLDOWN.get();
        forcefieldMaxHp = FORCEFIELD_MAX_HP.get();
        forcefieldRegenPerTick = FORCEFIELD_REGEN_PER_TICK.get();
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
        laserMoveSpeedThreshold = LASER_MOVE_SPEED_THRESHOLD.get();
        laserIntensityAdjustable = LASER_INTENSITY_ADJUSTABLE.get();
        laserIntensityScrollStep = LASER_INTENSITY_SCROLL_STEP.get();
        laserBreakCriticalIntensity = LASER_BREAK_CRITICAL.get();
        laserFireCriticalIntensity = LASER_FIRE_CRITICAL.get();
        laserBasicBreakBlocks = LASER_BASIC_BREAK_BLOCKS.get(); laserAdvancedBreakBlocks = LASER_ADVANCED_BREAK_BLOCKS.get();
        laserBlockBreakChance = LASER_BLOCK_BREAK_CHANCE.get(); laserBlockBreakDrops = LASER_BLOCK_BREAK_DROPS.get();
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
        weightHeadPop = WEIGHT_HEAD_POP.get();
        weightEnhancedRegen = WEIGHT_ENHANCED_REGEN.get();
        weightDensity = WEIGHT_DENSITY.get();
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
        stormfrontDischargeDamage = STORMFRONT_DISCHARGE_DAMAGE.get(); stormfrontDischargeRadius = STORMFRONT_DISCHARGE_RADIUS.get();
        stormfrontChainDamage = STORMFRONT_CHAIN_DAMAGE.get();
        stormfrontChainFalloff = STORMFRONT_CHAIN_FALLOFF.get();
        stormfrontChainMaxJumps = STORMFRONT_CHAIN_MAX_JUMPS.get();
        stormfrontChainJumpRange = STORMFRONT_CHAIN_JUMP_RANGE.get();
        stormfrontChainAimRange = STORMFRONT_CHAIN_AIM_RANGE.get();
        stormfrontDischargeTickRate = STORMFRONT_DISCHARGE_TICK_RATE.get();
        chestBlastBlockBreakChance = CHEST_BLAST_BLOCK_BREAK_CHANCE.get();
        chestBlastStripsInvincible = CHEST_BLAST_STRIPS_INVINCIBLE.get();
        mobPowerCreativeFlight = MOB_POWER_CREATIVE_FLIGHT.get();
        mobPowerChestBlast = MOB_POWER_CHEST_BLAST.get();
        mobPowerForcefield = MOB_POWER_FORCEFIELD.get();
        mobPowerPyrokinesis = MOB_POWER_PYROKINESIS.get();
        mobPowerCryokinesis = MOB_POWER_CRYOKINESIS.get();
        mobPyroFireCooldown = MOB_PYRO_FIRE_COOLDOWN.get();
        mobCryoFireCooldown = MOB_CRYO_FIRE_COOLDOWN.get();
        mobProjectileRange = MOB_PROJECTILE_RANGE.get();
        mobPowerFriendlyFire = MOB_POWER_FRIENDLY_FIRE.get();
        mobPyroMaxCharges = MOB_PYRO_MAX_CHARGES.get();
        mobCryoMaxCharges = MOB_CRYO_MAX_CHARGES.get();
        mobChargeRegenTicks = MOB_CHARGE_REGEN_TICKS.get();
        mobPowerLeap = MOB_POWER_LEAP.get();
        mobPowerExplosive = MOB_POWER_EXPLOSIVE.get();
        mobPowerHealing = MOB_POWER_HEALING.get();
        mobChestBlastWeight = MOB_CHEST_BLAST_WEIGHT.get();
        mobChestBlastStripsPowers = MOB_CHEST_BLAST_STRIPS_POWERS.get();
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
