package blueduck.compound_v.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: set the LOCAL player's noPhysics flag so the client stops applying
 * block collision locally. Without this, the client re-collides and the player only
 * hovers instead of passing through walls (server-side noPhysics alone is insufficient
 * for the local player). Mixin-free phase relies on this sync.
 */
public class S2CPhaseSyncPacket {

    private final boolean phased;

    public S2CPhaseSyncPacket(boolean phased) {
        this.phased = phased;
    }

    public S2CPhaseSyncPacket(FriendlyByteBuf buf) {
        this.phased = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(phased);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPhaseApplier.apply(phased)));
        return true;
    }

    /** Isolated so the client-only class reference is never loaded on the server. */
    private static class ClientPhaseApplier {
        static void apply(boolean phased) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.noPhysics = phased;
            }
        }
    }
}
