package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class InvisibilityEffect extends CompoundVEffect {
    public InvisibilityEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.hasEffect(MobEffects.INVISIBILITY) || !(entity instanceof Player)) {
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false, false));

        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (!player.hasEffect(MobEffects.INVISIBILITY)) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false, false));
        }
        else {
            player.removeEffect(MobEffects.INVISIBILITY);
        }

    }
}
