package blueduck.compound_v.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DeepEffect extends CompoundVEffect {
    public DeepEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.isInWater()) {
            entity.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, false, false, false));
        }
        if (entity.isEyeInFluid(FluidTags.WATER)) {
             entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
            if (entity instanceof Player) {
                entity.setAirSupply(300);
            }
        }
        else {
            entity.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 10 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        } else {
            return true;
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (player.isInWaterOrRain() && false) {
            float f7 = player.getYRot();
            float f = player.getXRot();
            float f1 = -Mth.sin(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
            float f2 = -Mth.sin(f * ((float)Math.PI / 180F));
            float f3 = Mth.cos(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
            float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
            float f5 = 3.0F;
            f1 *= f5 / f4;
            f2 *= f5 / f4;
            f3 *= f5 / f4;
            player.push((double)f1, (double)f2, (double)f3);
            player.startAutoSpinAttack(20);
            if (player.onGround()) {
                float f6 = 1.1999999F;
                player.move(MoverType.SELF, new Vec3(0.0D, (double)1.1999999F, 0.0D));
            }

            SoundEvent soundevent = SoundEvents.TRIDENT_RIPTIDE_3;


//            p_43395_.playSound((Player)null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
