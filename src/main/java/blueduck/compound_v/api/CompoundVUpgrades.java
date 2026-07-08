package blueduck.compound_v.api;

import net.minecraft.world.effect.MobEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public API for addon mods to extend Compound V's V1 upgrade behavior.
 *
 * <p>Drinking V1 while holding a "lesser" power can promote it to a "greater" power (granted at
 * max level) instead of simply maxing the lesser power. Compound V ships several such paths and
 * exposes more through the {@code v1UpgradePaths} config. Addon mods can register their own paths
 * in code through this API - useful when the effects involved are added by the addon itself.</p>
 *
 * <p>Registered paths are merged with the config-defined ones. If both define an upgrade for the
 * same source power, the config wins (so server owners can always override an addon). Register
 * during common setup (e.g. {@code FMLCommonSetupEvent}); registrations made before the effect
 * registry is populated are fine because this stores the effect references directly.</p>
 *
 * <pre>{@code
 * // In your addon's setup:
 * CompoundVUpgrades.register(MyEffects.LESSER_POWER.get(), MyEffects.GREATER_POWER.get());
 * }</pre>
 */
public final class CompoundVUpgrades {

    private static final Map<MobEffect, MobEffect> ADDON_PATHS = new ConcurrentHashMap<>();

    private CompoundVUpgrades() {}

    /**
     * Register a V1 upgrade path: holding {@code from} and drinking V1 promotes it to {@code to}
     * (granted at max level). Null arguments are ignored.
     */
    public static void register(MobEffect from, MobEffect to) {
        if (from == null || to == null) return;
        ADDON_PATHS.put(from, to);
    }

    /** Remove a previously registered addon upgrade path. */
    public static void unregister(MobEffect from) {
        if (from != null) ADDON_PATHS.remove(from);
    }

    /** The addon-registered paths. Compound V merges these with the config-defined paths. */
    public static Map<MobEffect, MobEffect> getRegisteredPaths() {
        return ADDON_PATHS;
    }
}
