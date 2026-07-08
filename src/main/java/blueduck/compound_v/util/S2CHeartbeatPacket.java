package blueduck.compound_v.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> a single client: play a Warden heartbeat at a world position, heard ONLY by the
 * receiving player. Used for the Advanced Laser Eyes "predator sense" - the holder hears the
 * heartbeat of nearby low-health players (directional, with distance falloff), and nobody else
 * hears it because it's a client-local sound triggered by this targeted packet.
 */
public class S2CHeartbeatPacket {

    private final double x, y, z;
    private final float pitch;

    public S2CHeartbeatPacket(double x, double y, double z, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
    }

    public S2CHeartbeatPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pitch = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(pitch);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientHeartbeat.play(x, y, z, pitch)));
        return true;
    }
}
