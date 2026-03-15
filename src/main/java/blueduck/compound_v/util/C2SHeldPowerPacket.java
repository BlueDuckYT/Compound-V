package blueduck.compound_v.util;

import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class C2SHeldPowerPacket {

    public C2SHeldPowerPacket() {
    }

    public C2SHeldPowerPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            // Iterate safely over a copy of active effects
            List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect() instanceof CompoundVEffect compoundEffect) {
                    compoundEffect.holdActivate(player, instance.getAmplifier(), level);
                }
            }
        });
        return true;
    }
}
