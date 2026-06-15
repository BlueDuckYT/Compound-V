package blueduck.compound_v.util;

import blueduck.compound_v.client.LaserClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CLaserSyncPacket {

    /**
     * Laser color indices:
     *   0 = Orange (default / Blaze)
     *   1 = Blue
     *   2 = Red (Homelander advanced)
     *   3 = Green
     *   4 = Purple (Enderman, rare)
     *   5 = Yellow (Husk, rare)
     */
    public static final int COLOR_ORANGE = 0;
    public static final int COLOR_BLUE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_GREEN = 3;
    public static final int COLOR_PURPLE = 4;
    public static final int COLOR_YELLOW = 5;
    public static final int COLOR_CHEST_BLAST = 6;
    public static final int COLOR_RAINBOW = 7;
    public static final int COLOR_BLACK = 8;
    public static final int COLOR_WHITE = 9;

    private final int entityId;
    private final double hitX, hitY, hitZ;
    private final int colorIndex;
    private final float intensity;

    public S2CLaserSyncPacket(int entityId, double hitX, double hitY, double hitZ, int colorIndex) {
        this(entityId, hitX, hitY, hitZ, colorIndex, 1.0f);
    }

    public S2CLaserSyncPacket(int entityId, double hitX, double hitY, double hitZ, int colorIndex, float intensity) {
        this.entityId = entityId;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.colorIndex = colorIndex;
        this.intensity = intensity;
    }

    public S2CLaserSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.hitX = buf.readDouble();
        this.hitY = buf.readDouble();
        this.hitZ = buf.readDouble();
        this.colorIndex = buf.readInt();
        this.intensity = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(hitX);
        buf.writeDouble(hitY);
        buf.writeDouble(hitZ);
        buf.writeInt(colorIndex);
        buf.writeFloat(intensity);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LaserClientData.setLaserActive(entityId, hitX, hitY, hitZ, colorIndex, intensity);
            });
        });
        return true;
    }
}
