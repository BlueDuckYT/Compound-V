package blueduck.compound_v;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = CompoundVMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

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
    private static final ForgeConfigSpec.BooleanValue TEMP_V_FROM_WANDERING_TRADER = BUILDER
            .comment("Whether the Wandering Trader can rarely sell Temp V")
            .define("temp_v_from_trader", true);

    private static final ForgeConfigSpec.IntValue TEMP_V_DURATION = BUILDER
            .comment("Duration (in ticks) of Temp V's effects")
            .defineInRange("tempVDuration", 24000, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_BAD_EFFECT_CHANCE = BUILDER
            .comment("Chance of getting a bad outcome when taking Compound V (Setting this to 0 will disable it)")
            .defineInRange("badReactionChance", 0.1, 0, 1);
    private static final ForgeConfigSpec.BooleanValue CAN_TEMP_GIVE_BAD_EFFECTS = BUILDER
            .comment("Whether Temp V can give negative outcomes")
            .define("tempVBadReaction", false);

    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_DAMAGE_REDUCTION = BUILDER
            .comment("Damage multiplier of damage taken while on Compound V")
            .defineInRange("damageReduction", 0.75, 0, 1);
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_STRENGTH_MULTIPLIER = BUILDER
            .comment("Damage multiplier of damage dealt while on Compound V")
            .defineInRange("strengthMultiplier", 1.2, 0, Double.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue COMPOUND_V_KNOCKBACK_REDUCTION = BUILDER
            .comment("Knockback multiplier of knockback taken while on Compound V")
            .defineInRange("knockbackReduction", 0.8, 0, 1);

    private static final ForgeConfigSpec.IntValue WEIGHT_GENERIC = BUILDER
            .comment("Weight of obtaining Generic effect (No extra abilities) when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_generic", 15, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_SPEEDSTER = BUILDER
            .comment("Weight of obtaining Speedster powers when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_speedster", 10, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_WATER = BUILDER
            .comment("Weight of obtaining Water Powers when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_water_power", 10, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_TELEPORT = BUILDER
            .comment("Weight of obtaining Teleportation power when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_teleportation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_ATOM_CHARGING = BUILDER
            .comment("Weight of obtaining Atom Charging power (Projectile Explosion) when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_atom_charging", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVISIBILITY = BUILDER
            .comment("Weight of obtaining Invisibility when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_invisibility", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_NIGHT_VISION = BUILDER
            .comment("Weight of obtaining Night Vision when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_night_vision", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_LEVITATION = BUILDER
            .comment("Weight of obtaining Levitation-based flight when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_levitation", 5, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue WEIGHT_INVINCIBLE = BUILDER
            .comment("Weight of obtaining Invincibility when taking Compound V (Putting this to 0 will disable it)")
            .defineInRange("weight_invincible", 1, 0, Integer.MAX_VALUE);





    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean addToBuriedTreasure;
    public static boolean addVToAncientCities;
    public static boolean addTempVToAncientCities;
    public static boolean addToBastions;
    public static boolean addToEndCities;
    public static boolean tempVFromTrader;
    public static int tempVDuration;
    public static double badOutcomeChance;
    public static boolean tempVBadOutcomes;
    public static double damageReduction;
    public static double strengthMultiplier;
    public static double knockbackReduction;
    public static int weightGeneric;
    public static int weightSpeedster;
    public static int weightWater;
    public static int weightTeleport;
    public static int weightAtomCharging;
    public static int weightInvisibility;
    public static int weightNightVision;
    public static int weightLevitation;
    public static int weightInvincible;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        addToBuriedTreasure = ADD_TEMP_V_TO_BURIED_TREASURE.get();
        addVToAncientCities = ADD_COMPOUND_V_TO_ANCIENT_CITIES.get();
        addTempVToAncientCities = ADD_TEMP_V_TO_ANCIENT_CITIES.get();
        addToBastions = ADD_TEMP_V_TO_BASTIONS.get();
        addToEndCities = ADD_COMPOUND_V_TO_END_CITIES.get();
        tempVFromTrader = TEMP_V_FROM_WANDERING_TRADER.get();
        tempVDuration = TEMP_V_DURATION.get();
        badOutcomeChance = COMPOUND_V_BAD_EFFECT_CHANCE.get();
        tempVBadOutcomes = CAN_TEMP_GIVE_BAD_EFFECTS.get();
        damageReduction = COMPOUND_V_DAMAGE_REDUCTION.get();
        strengthMultiplier = COMPOUND_V_STRENGTH_MULTIPLIER.get();
        knockbackReduction = COMPOUND_V_KNOCKBACK_REDUCTION.get();
        weightGeneric = WEIGHT_GENERIC.get();
        weightSpeedster = WEIGHT_SPEEDSTER.get();
        weightWater = WEIGHT_WATER.get();
        weightTeleport = WEIGHT_TELEPORT.get();
        weightAtomCharging = WEIGHT_ATOM_CHARGING.get();
        weightInvisibility = WEIGHT_INVISIBILITY.get();
        weightNightVision = WEIGHT_NIGHT_VISION.get();
        weightLevitation = WEIGHT_LEVITATION.get();
        weightInvincible = WEIGHT_INVINCIBLE.get();

    }
}
