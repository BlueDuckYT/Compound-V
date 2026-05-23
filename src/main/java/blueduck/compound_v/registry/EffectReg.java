package blueduck.compound_v.registry;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.*;
import blueduck.compound_v.effect.negative.BlindnessEffect;
import blueduck.compound_v.effect.negative.FloatingEffect;
import blueduck.compound_v.effect.negative.MagnetismEffect;
import blueduck.compound_v.effect.negative.SlowEffect;
import blueduck.compound_v.effect.negative.UncontrolledTeleportEffect;
import blueduck.compound_v.util.CompoundVEffectGiver;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectReg {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CompoundVMod.MODID);

    // Original effects
    public static final RegistryObject<MobEffect> GENERIC = EFFECTS.register("generic", () -> new GenericEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> DEEP = EFFECTS.register("deep", () -> new DeepEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> NIGHT_VISION = EFFECTS.register("night_vision", () -> new NightVisionEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> SPEEDSTER = EFFECTS.register("speedster", () -> new SpeedsterEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> INVISIBILITY = EFFECTS.register("invisibility", () -> new InvisibilityEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> TELEPORT = EFFECTS.register("teleport", () -> new TeleportEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> ATOM_CHARGING = EFFECTS.register("atom_charging", () -> new AtomChargingEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> CHARGING = EFFECTS.register("charging", () -> new ChargingEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LEVITATION = EFFECTS.register("levitation", () -> new LevitationEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> INVINCIBLE = EFFECTS.register("invincible", () -> new InvincibleEffect(MobEffectCategory.BENEFICIAL));

    // New effects
    public static final RegistryObject<MobEffect> CREATIVE_FLIGHT = EFFECTS.register("creative_flight", () -> new CreativeFlightEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LASER_EYES_BASIC = EFFECTS.register("laser_eyes_basic", () -> new LaserEyesEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LASER_EYES_ADVANCED = EFFECTS.register("laser_eyes_advanced", () -> new LaserEyesAdvancedEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> SHRINK = EFFECTS.register("shrink", () -> new ShrinkEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> ENLARGE = EFFECTS.register("enlarge", () -> new EnlargeEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> POWER_ABSORPTION = EFFECTS.register("energy_absorption", () -> new PowerAbsorptionEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> SONIC_SCREAM = EFFECTS.register("sonic_scream", () -> new SonicScreamEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> HEAD_POP = EFFECTS.register("head_pop", () -> new HeadPopEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> ENHANCED_REGEN = EFFECTS.register("enhanced_regen", () -> new EnhancedRegenEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> DENSITY = EFFECTS.register("density", () -> new DensityEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> SPIDER = EFFECTS.register("spider", () -> new SpiderEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> INSTAKILL = EFFECTS.register("instakill", () -> new InstakillEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> MIND_CONTROL = EFFECTS.register("mind_control", () -> new MindControlEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> BERSERKER = EFFECTS.register("berserker", () -> new BerserkerEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> PROJECTILE_IMMUNITY = EFFECTS.register("projectile_immunity", () -> new ProjectileImmunityEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> STAR_POWER = EFFECTS.register("star_power", () -> new StarPowerEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> CHEST_BLAST = EFFECTS.register("chest_blast", () -> new ChestBlastEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LEAP = EFFECTS.register("leap", () -> new LeapEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> EXPLOSIVE = EFFECTS.register("explosive", () -> new ExplosiveEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> HEALING = EFFECTS.register("healing", () -> new HealingEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> MIMIC = EFFECTS.register("mimic", () -> new MimicEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> PETRIFYING_GAZE = EFFECTS.register("petrifying_gaze", () -> new PetrifyingGazeEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> STORMFRONT = EFFECTS.register("stormfront", () -> new StormfrontEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> FORCEFIELD = EFFECTS.register("forcefield", () -> new ForcefieldEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LUCK = EFFECTS.register("luck", () -> new LuckEffect(MobEffectCategory.BENEFICIAL));

    // Negative effects
    public static final RegistryObject<MobEffect> SLOWNESS = EFFECTS.register("slowness", () -> new SlowEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> FLOATING = EFFECTS.register("floating", () -> new FloatingEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> UNCONTROLLED_TELEPORT = EFFECTS.register("uncontrolled_teleport", () -> new UncontrolledTeleportEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> BLINDNESS = EFFECTS.register("blindness", () -> new BlindnessEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> MAGNETISM = EFFECTS.register("magnetism", () -> new MagnetismEffect(MobEffectCategory.HARMFUL));

    // Marker effects
    public static final RegistryObject<MobEffect> NULLIFIED = EFFECTS.register("nullified", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x555555) {});

    // Experimental
    public static final RegistryObject<MobEffect> NULLIFY = EFFECTS.register("nullify", () -> new blueduck.compound_v.effect.NullifyEffect(MobEffectCategory.BENEFICIAL));

    public static void addEffectsToMatrix() {
        // Original effects
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(GENERIC.get(), 3), Config.weightGeneric);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(SPEEDSTER.get(), 5), Config.weightSpeedster);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(DEEP.get(), 1), Config.weightWater);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(TELEPORT.get(), 1), Config.weightTeleport);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(ATOM_CHARGING.get(), 3), Config.weightAtomCharging);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(INVISIBILITY.get(), 1), Config.weightInvisibility);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(NIGHT_VISION.get(), 1), Config.weightNightVision);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LEVITATION.get(), 1), Config.weightLevitation);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(INVINCIBLE.get(), 1), Config.weightInvincible);

        // New effects
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(CREATIVE_FLIGHT.get(), 1), Config.weightCreativeFlight);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LASER_EYES_BASIC.get(), 1), Config.weightLaserEyesBasic);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LASER_EYES_ADVANCED.get(), 1), Config.weightLaserEyesAdvanced);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(POWER_ABSORPTION.get(), 1), Config.weightPowerAbsorption);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(SONIC_SCREAM.get(), 1), Config.weightSonicScream);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(HEAD_POP.get(), 3), Config.weightHeadPop);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(ENHANCED_REGEN.get(), 1), Config.weightEnhancedRegen);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(DENSITY.get(), 1), Config.weightDensity);
        // Spider power — in development, disabled for now
        // CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(SPIDER.get(), 1), Config.weightSpider);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(MIND_CONTROL.get(), 1), Config.weightMindControl);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(INSTAKILL.get(), 1), Config.weightInstakill);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(BERSERKER.get(), 1), Config.weightBerserker);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(PROJECTILE_IMMUNITY.get(), 1), Config.weightProjectileImmunity);
        // Star Power — experimental
         CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(STAR_POWER.get(), 1), Config.weightStarPower);
        // Chest Blast — optionally in regular pool
        if (Config.chestBlastInRegularPool && Config.weightChestBlast > 0) {
            CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(CHEST_BLAST.get(), 1), Config.weightChestBlast);
        }
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LEAP.get(), 1), Config.weightLeap);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(EXPLOSIVE.get(), 1), Config.weightExplosive);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(HEALING.get(), 1), Config.weightHealing);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(MIMIC.get(), 1), Config.weightMimic);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(PETRIFYING_GAZE.get(), 1), Config.weightPetrifyingGaze);
        if (Config.stormfrontInRegularPool && Config.weightStormfront > 0) {
            CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(STORMFRONT.get(), 1), Config.weightStormfront);
        }
        //CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(FORCEFIELD.get(), 1), Config.weightForcefield);
        //CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(NULLIFY.get(), 1), Config.weightNullify); // experimental
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LUCK.get(), 3), Config.weightLuck);

        // Pehkui-dependent effects
        if (ModList.get().isLoaded("pehkui")) {
            CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(SHRINK.get(), 1), Config.weightShrink);
            CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(ENLARGE.get(), 1), Config.weightEnlarge);
            com.mojang.logging.LogUtils.getLogger().info("Compound V: Pehkui detected, Shrink and Enlarge powers enabled!");
        } else {
            com.mojang.logging.LogUtils.getLogger().info("Compound V: Pehkui not found, Shrink and Enlarge powers disabled.");
        }

        // Failure effects
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(SLOWNESS.get(), 2), 5);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(FLOATING.get(), 1), 5);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(UNCONTROLLED_TELEPORT.get(), 1), 15);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(BLINDNESS.get(), 1), 5);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(MAGNETISM.get(), 1), 5);

        // === Mob-injectable pools (right-click with Compound V / Temp V) ===
        // Only powers that mechanically function on non-player entities.
        // Excludes: Generic, Mind Control, Head Pop, Sonic Scream, Power Absorption,
        //           Density, Creative Flight, Laser Eyes Advanced, Instakill, Spider
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(SPEEDSTER.get(), 5), Config.weightSpeedster);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(DEEP.get(), 1), Config.weightWater);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(TELEPORT.get(), 1), Config.weightTeleport);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(ATOM_CHARGING.get(), 3), Config.weightAtomCharging);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(INVISIBILITY.get(), 1), Config.weightInvisibility);
        //CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(STAR_POWER.get(), 1), Config.weightStarPower);
//        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(NIGHT_VISION.get(), 1), Config.weightNightVision);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(INVINCIBLE.get(), 1), Config.weightInvincible);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(LASER_EYES_BASIC.get(), 1), Config.weightLaserEyesBasic);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(LASER_EYES_ADVANCED.get(), 1), Config.weightLaserEyesAdvanced);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(LEAP.get(), 1), Config.weightLeap);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(EXPLOSIVE.get(), 1), Config.weightExplosive);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(HEALING.get(), 1), Config.weightHealing);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(ENHANCED_REGEN.get(), 1), Config.weightEnhancedRegen);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(BERSERKER.get(), 1), Config.weightBerserker);
        CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(PROJECTILE_IMMUNITY.get(), 1), Config.weightProjectileImmunity);
        if (ModList.get().isLoaded("pehkui")) {
            CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(SHRINK.get(), 1), Config.weightShrink);
            CompoundVEffectMatrix.addMobEffect(new CompoundVEffectGiver(ENLARGE.get(), 1), Config.weightEnlarge);
        }

        // Mob-injectable failure effects (excludes player-only: Magnetism)
        CompoundVEffectMatrix.addMobFailureEffect(new CompoundVEffectGiver(SLOWNESS.get(), 2), 5);
        CompoundVEffectMatrix.addMobFailureEffect(new CompoundVEffectGiver(FLOATING.get(), 1), 5);
        CompoundVEffectMatrix.addMobFailureEffect(new CompoundVEffectGiver(UNCONTROLLED_TELEPORT.get(), 1), 15);
        CompoundVEffectMatrix.addMobFailureEffect(new CompoundVEffectGiver(MAGNETISM.get(), 1), 5);

        // === V1 pool (original formula) — curated powerful effects at max level ===
        // V1 pool — only include effects whose weight is > 0 (disabled = excluded)
        if (Config.weightSpeedster > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(SPEEDSTER.get(), 5), 3);
        if (Config.weightLaserEyesAdvanced > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(LASER_EYES_ADVANCED.get(), 1), 3);
        if (Config.weightHeadPop > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(HEAD_POP.get(), 3), 3);
        if (Config.weightCreativeFlight > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(CREATIVE_FLIGHT.get(), 1), 2);
        if (Config.weightTeleport > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(TELEPORT.get(), 1), 2);
        if (Config.weightMindControl > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(MIND_CONTROL.get(), 1), 2);
        if (ModList.get().isLoaded("pehkui") && Config.weightEnlarge > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(ENLARGE.get(), 1), 2);
        if (Config.weightInstakill > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(INSTAKILL.get(), 1), 1);
        if (Config.weightInvincible > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(INVINCIBLE.get(), 1), 1);
        if (Config.weightStarPower > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(STAR_POWER.get(), 1), 1);
        if (Config.weightChestBlast > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(CHEST_BLAST.get(), 1), Config.weightChestBlast);
        if (Config.weightStormfront > 0)
            CompoundVEffectMatrix.addV1Effect(new CompoundVEffectGiver(STORMFRONT.get(), 1), Config.weightStormfront);
    }
}