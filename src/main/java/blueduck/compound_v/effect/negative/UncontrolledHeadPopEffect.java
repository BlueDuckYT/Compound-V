package blueduck.compound_v.effect.negative;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uncontrolled Head Pop — a grim countdown. ~10 seconds after onset the victim's head
 * pops (lethal), with escalating warning particles/sounds as the timer runs down.
 */
public class UncontrolledHeadPopEffect extends BadCompoundVEffect {

    private static final Map<UUID, Integer> countdown = new ConcurrentHashMap<>();
    private static final int FUSE_TICKS = 200; // 10 seconds

    public UncontrolledHeadPopEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        UUID uuid = entity.getUUID();
        int t = countdown.getOrDefault(uuid, FUSE_TICKS);

        double hx = entity.getX(), hy = entity.getY() + entity.getBbHeight(), hz = entity.getZ();
        // Escalating warning: pressure builds at the head.
        float progress = 1.0f - (float) t / FUSE_TICKS;
        if (t % Math.max(2, (int) (12 * (1.0f - progress))) == 0) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, hx, hy, hz,
                    1 + (int) (progress * 6), 0.2, 0.2, 0.2, 0.0);
            level.playSound(null, hx, entity.getY(), hz,
                    SoundEvents.CONDUIT_AMBIENT, SoundSource.HOSTILE, 0.6F, 0.5F + progress);
        }

        t--;
        if (t <= 0) {
            countdown.remove(uuid);
            popHead(entity, level, hx, hy, hz);
        } else {
            countdown.put(uuid, t);
        }
    }

    private void popHead(LivingEntity entity, ServerLevel level, double hx, double hy, double hz) {
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().magic(), 1000.0f); // lethal
        // Reuse the EXACT visuals/sound of the controlled Head Pop power.
        blueduck.compound_v.effect.HeadPopEffect.spawnHeadPopBurst(
                level, hx, hy, hz, entity.getY(), true, entity.getRandom());
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (entity != null) countdown.remove(entity.getUUID());
    }
}
