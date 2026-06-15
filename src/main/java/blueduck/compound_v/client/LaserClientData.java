package blueduck.compound_v.client;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaserClientData {

    public static class LaserInfo {
        public double hitX, hitY, hitZ;
        public int colorIndex;
        public int ticksRemaining;
        public float intensity;

        public LaserInfo(double hitX, double hitY, double hitZ, int colorIndex, float intensity) {
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
            this.colorIndex = colorIndex;
            this.intensity = intensity;
            this.ticksRemaining = 2;
        }
    }

    private static final Map<Integer, LaserInfo> activeLasers = new ConcurrentHashMap<>();

    public static void setLaserActive(int entityId, double hitX, double hitY, double hitZ, int colorIndex) {
        setLaserActive(entityId, hitX, hitY, hitZ, colorIndex, 1.0f);
    }

    public static void setLaserActive(int entityId, double hitX, double hitY, double hitZ, int colorIndex, float intensity) {
        activeLasers.put(entityId, new LaserInfo(hitX, hitY, hitZ, colorIndex, intensity));
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
