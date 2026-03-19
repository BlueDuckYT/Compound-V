package blueduck.compound_v.client;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaserClientData {

    public static class LaserInfo {
        public double hitX, hitY, hitZ;
        public int colorIndex;
        public int ticksRemaining;

        public LaserInfo(double hitX, double hitY, double hitZ, int colorIndex) {
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
            this.colorIndex = colorIndex;
            this.ticksRemaining = 2;
        }
    }

    private static final Map<Integer, LaserInfo> activeLasers = new ConcurrentHashMap<>();

    public static void setLaserActive(int entityId, double hitX, double hitY, double hitZ, int colorIndex) {
        activeLasers.put(entityId, new LaserInfo(hitX, hitY, hitZ, colorIndex));
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
