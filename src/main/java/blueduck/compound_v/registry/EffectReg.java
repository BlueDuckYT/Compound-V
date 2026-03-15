package blueduck.compound_v.registry;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.*;
import blueduck.compound_v.effect.negative.FloatingEffect;
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
    public static final RegistryObject<MobEffect> GENERIC = EFFECTS.register("generic", () -> new CompoundVEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> DEEP = EFFECTS.register("deep", () -> new DeepEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> NIGHT_VISION = EFFECTS.register("night_vision", () -> new NightVisionEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> SPEEDSTER = EFFECTS.register("speedster", () -> new SpeedsterEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> INVISIBILITY = EFFECTS.register("invisibility", () -> new InvisibilityEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> TELEPORT = EFFECTS.register("teleport", () -> new TeleportEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> ATOM_CHARGING = EFFECTS.register("atom_charging", () -> new AtomChargingEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> CHARGING = EFFECTS.register("charging", () -> new ChargingEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> LEVITATION = EFFECTS.register("levitation", () -> new LevitationEffect(MobEffectCategory.BENEFICIAL));
    public static final RegistryObject<MobEffect> INVINCIBLE = EFFECTS.register("invincible", () -> new CompoundVEffect(MobEffectCategory.BENEFICIAL));

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

    // Negative effects
    public static final RegistryObject<MobEffect> SLOWNESS = EFFECTS.register("slowness", () -> new SlowEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> FLOATING = EFFECTS.register("floating", () -> new FloatingEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> UNCONTROLLED_TELEPORT = EFFECTS.register("uncontrolled_teleport", () -> new UncontrolledTeleportEffect(MobEffectCategory.HARMFUL));

    public static void addEffectsToMatrix() {
        // Original effects
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(GENERIC.get(), 1), Config.weightGeneric);
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
    }
}