package blueduck.compound_v.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NightVisionEffect extends CompoundVEffect {
    public NightVisionEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));

    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        BlockPos blockpos = player.blockPosition();
        AABB aabb = (new AABB(blockpos)).inflate(48.0D);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, aabb);
        if (!level.isClientSide) {
            for(LivingEntity livingentity : nearbyEntities) {
                if (livingentity.isAlive() && !livingentity.isRemoved() && blockpos.closerToCenterThan(livingentity.position(), 32.0D)) {
                    livingentity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false, false));
                }
            }
        }
    }
}
