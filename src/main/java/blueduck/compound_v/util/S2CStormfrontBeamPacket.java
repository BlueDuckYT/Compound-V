package blueduck.compound_v.util;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client packet syncing Stormfront ground lightning beam positions.
 * Carries the entity ID and up to 3 beam ground contact points.
 */
public class S2CStormfrontBeamPacket {

    public final int entityId;
    public final double[] beamX;
    public final double[] beamY;
    public final double[] beamZ;
    public final int beamCount;

    public S2CStormfrontBeamPacket(int entityId, double[] bx, double[] by, double[] bz, int count) {
        this.entityId = entityId;
        this.beamX = bx;
        this.beamY = by;
        this.beamZ = bz;
        this.beamCount = count;
    }

    public S2CStormfrontBeamPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.beamCount = buf.readByte();
        this.beamX = new double[beamCount];
        this.beamY = new double[beamCount];
        this.beamZ = new double[beamCount];
        for (int i = 0; i < beamCount; i++) {
            beamX[i] = buf.readDouble();
            beamY[i] = buf.readDouble();
            beamZ[i] = buf.readDouble();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeByte(beamCount);
        for (int i = 0; i < beamCount; i++) {
            buf.writeDouble(beamX[i]);
            buf.writeDouble(beamY[i]);
            buf.writeDouble(beamZ[i]);
        }
    }

    public boolean handle(java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        net.minecraftforge.network.NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                blueduck.compound_v.client.StormfrontBeamRenderer.receiveBeamData(
                        entityId, beamX, beamY, beamZ, beamCount);
            });
        });
        return true;
    }
}
