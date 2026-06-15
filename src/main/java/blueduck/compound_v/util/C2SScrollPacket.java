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
 * Client -> server: the player scrolled the wheel while a scroll-aware power was active.
 * Routes the scroll direction to each active CompoundV effect's scrollAdjust hook.
 */
public class C2SScrollPacket {

    private final int dir; // +1 = scroll up, -1 = scroll down

    public C2SScrollPacket(int dir) {
        this.dir = dir;
    }

    public C2SScrollPacket(FriendlyByteBuf buf) {
        this.dir = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(dir);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (CompoundVEffect.arePowersSuppressed(player)) return;
            ServerLevel level = player.serverLevel();
            List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect() instanceof CompoundVEffect compoundEffect
                        && compoundEffect.usesScroll(player)) {
                    compoundEffect.scrollAdjust(player, instance.getAmplifier(), level, dir);
                }
            }
        });
        return true;
    }
}
