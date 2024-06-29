package blueduck.compound_v.effect;

import blueduck.compound_v.registry.EffectReg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

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

        if (!player.hasEffect(EffectReg.CHARGING.get())) {
            player.addEffect(new MobEffectInstance(EffectReg.CHARGING.get(), 200, amplifier));
        }
        else {
             player.removeEffect(EffectReg.CHARGING.get());
        }
    }
}
