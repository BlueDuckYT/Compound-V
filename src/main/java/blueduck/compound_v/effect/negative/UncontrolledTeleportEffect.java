package blueduck.compound_v.effect.negative;

import blueduck.compound_v.effect.CompoundVEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class UncontrolledTeleportEffect extends BadCompoundVEffect {
    public UncontrolledTeleportEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        Level level = entity.level();
        if (level instanceof ServerLevel && entity.getRandom().nextDouble() < 0.015) {

            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();

            for(int i = 0; i < 16; ++i) {
                double d3 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 32.0D;
                double d4 = Mth.clamp(entity.getY() + (double)(entity.getRandom().nextInt(16) - 8), (double)level.getMinBuildHeight(), (double)(level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
                double d5 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 32.0D;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }

                Vec3 vec3 = entity.position();
                level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(entity, d3, d4, d5);
                if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = SoundEvents.ENDERMAN_TELEPORT;
                    level.playSound(null, d0, d1, d2, soundevent, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    entity.playSound(soundevent, 1.0F, 1.0F);
                    break;
                }
            }{

            }
        }
    }

    public void activate(ServerPlayer entity, int amplifier, ServerLevel level) {
        super.activate(entity, amplifier, level);


    }
}
