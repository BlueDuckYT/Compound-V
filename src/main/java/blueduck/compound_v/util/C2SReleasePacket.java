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

/**
 * Sent on the exact tick the power key is released. Lets effects react to release
 * immediately instead of waiting for the server's hold-timeout window to lapse — used by
 * Spider so the web is cut the instant V goes up (no 0.5-1s lingering strand).
 */
public class C2SReleasePacket {

    public C2SReleasePacket() {
    }

    public C2SReleasePacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();
            List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect() instanceof CompoundVEffect compoundEffect) {
                    compoundEffect.onRelease(player, instance.getAmplifier(), level);
                }
            }
        });
        return true;
    }
}
