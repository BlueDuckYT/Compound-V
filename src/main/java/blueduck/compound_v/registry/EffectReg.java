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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectReg {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CompoundVMod.MODID);

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


    //Negative Effects
    public static final RegistryObject<MobEffect> SLOWNESS = EFFECTS.register("slowness", () -> new SlowEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> FLOATING = EFFECTS.register("floating", () -> new FloatingEffect(MobEffectCategory.HARMFUL));
    public static final RegistryObject<MobEffect> UNCONTROLLED_TELEPORT = EFFECTS.register("uncontrolled_teleport", () -> new UncontrolledTeleportEffect(MobEffectCategory.HARMFUL));


    public static void addEffectsToMatrix() {
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(GENERIC.get(), 1), Config.weightGeneric);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(SPEEDSTER.get(), 5), Config.weightSpeedster);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(DEEP.get(), 1), Config.weightWater);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(TELEPORT.get(), 1), Config.weightTeleport);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(ATOM_CHARGING.get(), 3), Config.weightAtomCharging);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(INVISIBILITY.get(), 1), Config.weightInvisibility);
        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(NIGHT_VISION.get(), 1), Config.weightNightVision);

        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(LEVITATION.get(), 1), Config.weightLevitation);

        CompoundVEffectMatrix.addEffect(new CompoundVEffectGiver(INVINCIBLE.get(), 1), Config.weightInvincible);

        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(SLOWNESS.get(), 2), 5);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(FLOATING.get(), 1), 5);
        CompoundVEffectMatrix.addFailureEffect(new CompoundVEffectGiver(UNCONTROLLED_TELEPORT.get(), 1), 15);
    }

}
