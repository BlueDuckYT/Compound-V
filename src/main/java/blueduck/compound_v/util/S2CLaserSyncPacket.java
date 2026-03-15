package blueduck.compound_v.util;

import blueduck.compound_v.client.LaserClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CLaserSyncPacket {

    private final int entityId;
    private final double hitX, hitY, hitZ;
    private final boolean advanced;
    private final boolean blueVariant;

    public S2CLaserSyncPacket(int entityId, double hitX, double hitY, double hitZ, boolean advanced, boolean blueVariant) {
        this.entityId = entityId;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.advanced = advanced;
        this.blueVariant = blueVariant;
    }

    public S2CLaserSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.hitX = buf.readDouble();
        this.hitY = buf.readDouble();
        this.hitZ = buf.readDouble();
        this.advanced = buf.readBoolean();
        this.blueVariant = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(hitX);
        buf.writeDouble(hitY);
        buf.writeDouble(hitZ);
        buf.writeBoolean(advanced);
        buf.writeBoolean(blueVariant);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LaserClientData.setLaserActive(entityId, hitX, hitY, hitZ, advanced, blueVariant);
            });
        });
        return true;
    }
}
