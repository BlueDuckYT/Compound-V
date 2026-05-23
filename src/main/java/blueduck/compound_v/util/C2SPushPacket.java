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
            if (player == null) return;
            if (player.getPersistentData().getBoolean("compound_v_petrified")) return; // frozen by gaze
            if (CompoundVEffect.arePowersSuppressed(player)) return; // virus / nullification field
            ServerLevel level = player.serverLevel();

            // BUG FIX: iterate over a copy to avoid ConcurrentModificationException
            // when activate() adds/removes effects
            List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());

            // Mimic routing: if player has Mimic + a copied power, skip Mimic
            boolean hasMimic = player.hasEffect(blueduck.compound_v.registry.EffectReg.MIMIC.get());
            MobEffectInstance copiedEffect = hasMimic
                    ? blueduck.compound_v.effect.MimicEffect.getCopiedEffect(player) : null;

            for (MobEffectInstance instance : effects) {
                if (instance.getEffect() instanceof CompoundVEffect compoundEffect) {
                    // If Mimic + copied power: only activate the copied power
                    if (hasMimic && copiedEffect != null) {
                        if (compoundEffect instanceof blueduck.compound_v.effect.MimicEffect) continue;
                    }
                    compoundEffect.activate(player, instance.getAmplifier(), level);
                }
            }
        });
        return true;
    }
}
