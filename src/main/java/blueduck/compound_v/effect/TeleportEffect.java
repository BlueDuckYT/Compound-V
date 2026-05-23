package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TeleportEffect extends CompoundVEffect {
    public TeleportEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        int range = getTeleportRange();

        Vec3 endPos = eyePos.add(lookVec.scale(range));

        BlockHitResult hit = level.clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {

            BlockPos target = hit.getBlockPos().relative(hit.getDirection());

            if (level.isEmptyBlock(target) && level.isEmptyBlock(target.above())) {

                level.sendParticles(
                        ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(),
                        30, 0.5, 1, 0.5, 0.5
                );

                double x = target.getX() + 0.5;
                double y = target.getY();
                double z = target.getZ() + 0.5;

                player.teleportTo(x, y, z);

                // Arrival particles
                level.sendParticles(
                        ParticleTypes.PORTAL,
                        x, y + 1, z,
                        30, 0.5, 1, 0.5, 0.5
                );
            }
        }
    }

    public int getTeleportRange() {
        return Config.teleportRange;
    }
}
