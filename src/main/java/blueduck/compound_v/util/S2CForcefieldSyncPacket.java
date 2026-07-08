package blueduck.compound_v.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> tracking clients: broadcasts a player's forcefield render state (active + shield
 * health fraction). Vanilla only syncs a player's mob effects to that player's OWN client, so
 * other players never saw the forcefield amplifier and the shield rendered for nobody but the
 * owner. This packet carries the state to everyone tracking the entity so the render layer can
 * draw other players' shields too.
 */
public class S2CForcefieldSyncPacket {

    private final int entityId;
    private final boolean active;
    private final float health; // 0..1

    public S2CForcefieldSyncPacket(int entityId, boolean active, float health) {
        this.entityId = entityId;
        this.active = active;
        this.health = health;
    }

    public S2CForcefieldSyncPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.active = buf.readBoolean();
        this.health = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(active);
        buf.writeFloat(health);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientForcefieldData.apply(entityId, active, health)));
        return true;
    }
}
