package blueduck.compound_v.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Soft-dependency helpers for Alex's Caves integration. Kept standalone (not on a power class) so
 * features like "irradiation weakens supes" don't depend on any particular Compound V power being
 * present.
 */
public final class AlexsCavesCompat {

    private static MobEffect cachedIrradiated = null;
    private static boolean lookupAttempted = false;

    private AlexsCavesCompat() {}

    /**
     * Looks up Alex's Caves' Irradiated MobEffect from the registry.
     * Returns null if AC is not loaded or the effect can't be found.
     */
    public static MobEffect getIrradiatedEffect() {
        if (!lookupAttempted) {
            lookupAttempted = true;
            if (ModList.get().isLoaded("alexscaves")) {
                cachedIrradiated = ForgeRegistries.MOB_EFFECTS.getValue(
                        new ResourceLocation("alexscaves", "irradiated"));
                if (cachedIrradiated == null) {
                    com.mojang.logging.LogUtils.getLogger().warn(
                            "Compound V: Alex's Caves loaded but 'irradiated' effect not found in registry");
                }
            }
        }
        return cachedIrradiated;
    }
}
