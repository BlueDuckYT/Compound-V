package blueduck.compound_v.effect;

import blueduck.compound_v.registry.EffectReg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AtomChargingEffect extends CompoundVEffect {
    public AtomChargingEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.hasEffect(EffectReg.CHARGING.get()) || !(entity instanceof Player)) {
            entity.addEffect(new MobEffectInstance(EffectReg.CHARGING.get(), 200, amplifier));
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        // Fixed toggle: check first, then add or remove
        if (player.hasEffect(EffectReg.CHARGING.get())) {
            player.removeEffect(EffectReg.CHARGING.get());
        } else {
            player.addEffect(new MobEffectInstance(EffectReg.CHARGING.get(), 200, amplifier));
        }
    }
}
