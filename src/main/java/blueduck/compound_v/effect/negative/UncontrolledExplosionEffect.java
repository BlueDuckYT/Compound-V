package blueduck.compound_v.effect.negative;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uncontrolled — the victim lights a fuse and then detonates, dying in a large explosion
 * that destroys the surrounding area. A hissing fuse with escalating sparks telegraphs
 * the blast (like a primed creeper) before it goes off.
 */
public class UncontrolledExplosionEffect extends BadCompoundVEffect {

    private static final Map<UUID, Integer> fuse = new ConcurrentHashMap<>();
    private static final int FUSE_TICKS = 60; // 3 second fuse

    public UncontrolledExplosionEffect(MobEffectCategory category) {
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
        int t = fuse.getOrDefault(uuid, FUSE_TICKS);

        float progress = 1.0f - (float) t / FUSE_TICKS;
        // Hissing fuse: spark rate accelerates toward detonation.
        int rate = Math.max(1, (int) (8 * (1.0f - progress)));
        if (t % rate == 0) {
            level.sendParticles(ParticleTypes.SMOKE,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.7, entity.getZ(),
                    2, 0.2, 0.3, 0.2, 0.01);
            level.sendParticles(ParticleTypes.CRIT,
                    entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(),
                    1 + (int) (progress * 4), 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.4F, 0.8F + progress);
        }

        t--;
        if (t <= 0) {
            fuse.remove(uuid);
            detonate(entity, level);
        } else {
            fuse.put(uuid, t);
        }
    }

    private void detonate(LivingEntity entity, ServerLevel level) {
        double x = entity.getX(), y = entity.getY() + entity.getBbHeight() * 0.5, z = entity.getZ();
        float power = (float) Config.uncontrolledExplosionPower;
        Level.ExplosionInteraction interaction = Config.uncontrolledExplosionBreaksBlocks
                ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        // Kill the victim outright, then detonate centered on them.
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().explosion(null, null), 1000.0f);
        level.explode(null, entity.damageSources().explosion(null, null), null,
                x, y, z, power, false, interaction);
        // Match the controlled Explosive power's flame burst exactly.
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                entity.getX(), entity.getY() + 1, entity.getZ(),
                40, 2.0, 1.0, 2.0, 0.1);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (entity != null) fuse.remove(entity.getUUID());
    }
}
