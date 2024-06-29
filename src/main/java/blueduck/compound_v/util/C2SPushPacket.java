package blueduck.compound_v.util;

import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPushPacket {

    public C2SPushPacket() {

    }

    public C2SPushPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = context.getSender().serverLevel();

            if (!player.getActiveEffects().isEmpty()) {
                for (int i = 0; i < player.getActiveEffects().size(); i++) {
                    if (((MobEffectInstance) player.getActiveEffects().toArray()[i]).getEffect() instanceof CompoundVEffect) {
                        ((CompoundVEffect) ((MobEffectInstance) player.getActiveEffects().toArray()[i]).getEffect()).activate(player, ((MobEffectInstance) player.getActiveEffects().toArray()[i]).getAmplifier(), level);
                    }
                }
            }
        });
        return true;
    }

}
