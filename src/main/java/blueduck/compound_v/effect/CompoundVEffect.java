package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class CompoundVEffect extends MobEffect {
    public CompoundVEffect(MobEffectCategory category) {
        super(category, 1333402);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {


    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 100 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        } else {
            return true;
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {


    }
}
