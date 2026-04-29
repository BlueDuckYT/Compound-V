package blueduck.compound_v.effect.negative;

import blueduck.compound_v.effect.RadioactiveEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Radioactive Decay — negative Compound V effect. Requires Alex's Caves.
 *
 * Always-on radioactive aura with no toggle. You are a walking hazard.
 *
 * - You permanently have Irradiated I (green glow, no regen).
 * - Nearby entities get escalating Irradiated levels:
 *     12-8 blocks: Irradiated I
 *     8-4 blocks:  Irradiated II
 *     0-4 blocks:  Irradiated III
 * - Affects everything: mobs, villagers, animals, teammates.
 *
 * Uses the same AC effect lookup as RadioactiveEffect. Does nothing
 * if Alex's Caves is not installed.
 */
public class RadioactiveDecayEffect extends BadCompoundVEffect {

    private static final double OUTER_RANGE = 12.0;
    private static final double MID_RANGE = 8.0;
    private static final double INNER_RANGE = 4.0;
    private static final int IRRADIATE_DURATION = 300;

    public RadioactiveDecayEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return;

        MobEffect irradiated = RadioactiveEffect.getIrradiatedEffect();
        if (irradiated == null) return;

        ServerLevel level = player.serverLevel();

        // Always irradiated I on self
        player.addEffect(new MobEffectInstance(irradiated, IRRADIATE_DURATION, 0, false, false, true));

        // Irradiate nearby entities based on distance
        AABB auraBox = player.getBoundingBox().inflate(OUTER_RANGE);
        for (Entity e : level.getEntities(player, auraBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);

            int irradiatedLevel;
            if (dist <= INNER_RANGE) {
                irradiatedLevel = 2;
            } else if (dist <= MID_RANGE) {
                irradiatedLevel = 1;
            } else if (dist <= OUTER_RANGE) {
                irradiatedLevel = 0;
            } else {
                continue;
            }

            target.addEffect(new MobEffectInstance(irradiated, IRRADIATE_DURATION, irradiatedLevel, false, false, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 20 == 0;
    }
}
