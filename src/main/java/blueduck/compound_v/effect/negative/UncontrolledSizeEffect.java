package blueduck.compound_v.effect.negative;

import blueduck.compound_v.Config;
import blueduck.compound_v.util.PehkuiHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

/**
 * Uncontrolled Size — the victim randomly and uncontrollably shrinks or grows at intervals,
 * never settling at a useful size. Purely disruptive; no stat benefits.
 */
public class UncontrolledSizeEffect extends BadCompoundVEffect {

    // Per-entity game-time tick at which the next random size change is due. Each change
    // schedules the next one a fresh random distance away (between the min and max interval),
    // so changes are irregular and can be anywhere from ~1s to a minute or more apart.
    private static final java.util.Map<java.util.UUID, Long> nextChange = new java.util.concurrent.ConcurrentHashMap<>();

    public UncontrolledSizeEffect(MobEffectCategory category) {
        super(category);
    }

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded("pehkui");
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    private static long rollNextDelay(LivingEntity entity) {
        int min = Math.max(20, Config.uncontrolledSizeIntervalMinTicks);
        int max = Math.max(min, Config.uncontrolledSizeIntervalMaxTicks);
        if (max == min) return min;
        // Uniform in [min, max]. Wide range => highly irregular timing.
        return min + entity.getRandom().nextInt((max - min) + 1);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!isPehkuiLoaded()) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        java.util.UUID uuid = entity.getUUID();
        long now = level.getGameTime();
        Long due = nextChange.get(uuid);
        if (due == null) {
            // First tick with the effect: schedule the first change a random distance out
            // rather than firing immediately, so onset timing is unpredictable too.
            nextChange.put(uuid, now + rollNextDelay(entity));
            return;
        }
        if (now < due) return;

        // Time for a change: roll a new size and schedule the next change a fresh random
        // interval away.
        nextChange.put(uuid, now + rollNextDelay(entity));

        float min = (float) Config.uncontrolledSizeMinScale;
        float max = (float) Config.uncontrolledSizeMaxScale;
        float scale = min + entity.getRandom().nextFloat() * (max - min);
        PehkuiHelper.setScale(entity, scale);

        level.sendParticles(scale < 1.0f ? ParticleTypes.POOF : ParticleTypes.CLOUD,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                10, 0.4, 0.4, 0.4, 0.03);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                scale < 1.0f ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND,
                SoundSource.PLAYERS, 0.5F, scale < 1.0f ? 1.5F : 0.6F);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        if (entity != null) nextChange.remove(entity.getUUID());
        if (isPehkuiLoaded() && entity != null) {
            PehkuiHelper.resetScale(entity);
        }
    }
}
