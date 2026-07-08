package blueduck.compound_v.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of other players' forcefield render state, populated by
 * {@link S2CForcefieldSyncPacket}. The forcefield render layer reads this for non-local players
 * (whose mob-effect data vanilla never syncs to this client).
 */
@OnlyIn(Dist.CLIENT)
public class ClientForcefieldData {

    private static final Map<Integer, Boolean> active = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> health = new ConcurrentHashMap<>();

    public static void apply(int entityId, boolean isActive, float healthFraction) {
        if (isActive) {
            active.put(entityId, true);
            health.put(entityId, healthFraction);
        } else {
            active.remove(entityId);
            health.remove(entityId);
        }
    }

    public static boolean isActive(int entityId) {
        return active.getOrDefault(entityId, false);
    }

    public static float getHealth(int entityId) {
        return health.getOrDefault(entityId, 1.0f);
    }
}
