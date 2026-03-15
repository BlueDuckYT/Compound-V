package blueduck.compound_v.client;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaserClientData {

    public static class LaserInfo {
        public double hitX, hitY, hitZ;
        public boolean advanced;
        public boolean blueVariant;
        public int ticksRemaining;

        public LaserInfo(double hitX, double hitY, double hitZ, boolean advanced, boolean blueVariant) {
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
            this.advanced = advanced;
            this.blueVariant = blueVariant;
            this.ticksRemaining = 3;
        }
    }

    private static final Map<Integer, LaserInfo> activeLasers = new ConcurrentHashMap<>();

    public static void setLaserActive(int entityId, double hitX, double hitY, double hitZ, boolean advanced, boolean blueVariant) {
        activeLasers.put(entityId, new LaserInfo(hitX, hitY, hitZ, advanced, blueVariant));
    }

    public static Map<Integer, LaserInfo> getActiveLasers() {
        return Collections.unmodifiableMap(activeLasers);
    }

    public static void tick() {
        activeLasers.entrySet().removeIf(entry -> {
            entry.getValue().ticksRemaining--;
            return entry.getValue().ticksRemaining <= 0;
        });
    }

    public static void clear() {
        activeLasers.clear();
    }
}
