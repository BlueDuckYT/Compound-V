package blueduck.compound_v.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public class CreativeFlightEffect extends CompoundVEffect {

    private static final DustParticleOptions WIND_PARTICLE = new DustParticleOptions(
            new Vector3f(0.9f, 0.95f, 1.0f), 0.8f);

    public CreativeFlightEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        // Spawn subtle wind/cloud particles while flying
        if (entity instanceof ServerPlayer player && entity.level() instanceof ServerLevel serverLevel) {
            if (player.getAbilities().flying) {
                double speed = entity.getDeltaMovement().length();
                if (speed > 0.05) {
                    // Cloud puff trail
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            entity.getX(), entity.getY(), entity.getZ(),
                            (int) (1 + speed * 3), 0.3, 0.1, 0.3, 0.01);
                    // White wind streaks
                    serverLevel.sendParticles(WIND_PARTICLE,
                            entity.getX(), entity.getY() + 0.5, entity.getZ(),
                            (int) (2 + speed * 4), 0.4, 0.3, 0.4, 0.05);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        // Tick every 10 ticks for particle spawning
        return tick % 10 == 0;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        boolean wasFlying = player.getAbilities().mayfly;

        if (wasFlying && !player.isCreative() && !player.isSpectator()) {
            // Turn off flight
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();

            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    10, 0.5, 0.3, 0.5, 0.05);
        } else if (!wasFlying) {
            // Turn on flight
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();

            // Dramatic launch particles
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    25, 0.8, 0.5, 0.8, 0.1);
            level.sendParticles(WIND_PARTICLE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    15, 0.5, 1.0, 0.5, 0.15);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5F, 1.2F);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // When the effect is removed, take away flight (unless creative/spectator)
        if (entity instanceof ServerPlayer player) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
