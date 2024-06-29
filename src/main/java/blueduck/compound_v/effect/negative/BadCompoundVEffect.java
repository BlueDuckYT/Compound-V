package blueduck.compound_v.effect.negative;

import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BadCompoundVEffect extends CompoundVEffect {

    public BadCompoundVEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {

    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 80 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        } else {
            return true;
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {


    }
}
