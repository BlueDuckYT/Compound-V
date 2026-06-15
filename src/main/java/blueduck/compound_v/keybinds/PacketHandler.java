package blueduck.compound_v.keybinds;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.util.C2SHeldPowerPacket;
import blueduck.compound_v.util.C2SPushPacket;
import blueduck.compound_v.util.S2CLaserSyncPacket;
import blueduck.compound_v.util.S2CStormfrontBeamPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
                    new ResourceLocation(CompoundVMod.MODID, "main"))
            .serverAcceptedVersions((status) -> true)
            .clientAcceptedVersions((status) -> true)
            .networkProtocolVersion(() -> "1.10")
            .simpleChannel();

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(C2SPushPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SPushPacket::new)
                .encoder(C2SPushPacket::toBytes)
                .consumerMainThread(C2SPushPacket::handle)
                .add();

        INSTANCE.messageBuilder(C2SHeldPowerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SHeldPowerPacket::new)
                .encoder(C2SHeldPowerPacket::toBytes)
                .consumerMainThread(C2SHeldPowerPacket::handle)
                .add();

        INSTANCE.messageBuilder(blueduck.compound_v.util.C2SScrollPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(blueduck.compound_v.util.C2SScrollPacket::new)
                .encoder(blueduck.compound_v.util.C2SScrollPacket::toBytes)
                .consumerMainThread(blueduck.compound_v.util.C2SScrollPacket::handle)
                .add();

        INSTANCE.messageBuilder(blueduck.compound_v.util.C2SReleasePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(blueduck.compound_v.util.C2SReleasePacket::new)
                .encoder(blueduck.compound_v.util.C2SReleasePacket::toBytes)
                .consumerMainThread(blueduck.compound_v.util.C2SReleasePacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CLaserSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CLaserSyncPacket::new)
                .encoder(S2CLaserSyncPacket::toBytes)
                .consumerMainThread(S2CLaserSyncPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CStormfrontBeamPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CStormfrontBeamPacket::new)
                .encoder(S2CStormfrontBeamPacket::toBytes)
                .consumerMainThread(S2CStormfrontBeamPacket::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllClients(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static void sendToTrackingAndSelf(Object msg, net.minecraft.world.entity.Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }
}
