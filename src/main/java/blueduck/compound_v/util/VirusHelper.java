package blueduck.compound_v.util;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class VirusHelper {
    private static boolean modChecked = false;
    private static boolean modLoaded = false;
    private static MobEffect cachedVirusEffect = null;

    public static boolean hasVirus(LivingEntity entity, boolean isPlayer) {
        if (isPlayer && !Config.virusDisablesPlayerPowers) return false;
        if (!isPlayer && !Config.virusDisablesMobPowers) return false;
        if (!modChecked) {
            modLoaded = ModList.get().isLoaded("compound_v_gone_viral");
            modChecked = true;
        }
        if (!modLoaded) return false;
        if (cachedVirusEffect == null) {
            cachedVirusEffect = ForgeRegistries.MOB_EFFECTS.getValue(
                    new ResourceLocation("compound_v_gone_viral", "virus"));
            if (cachedVirusEffect == null) return false;
        }
        return entity.hasEffect(cachedVirusEffect);
    }

    public static int getHighestTierOrdinal(LivingEntity entity) {
        int bestOrdinal = -1;
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            MobEffect effect = instance.getEffect();
            if (effect instanceof CompoundVEffect) {
                int ordinal = Config.getEffectTier(effect).ordinal();
                if (bestOrdinal == -1 || ordinal < bestOrdinal) {
                    bestOrdinal = ordinal;
                }
            }
        }
        return bestOrdinal;
    }

    public static void invalidateCache() {
        modChecked = false;
        cachedVirusEffect = null;
    }
}
