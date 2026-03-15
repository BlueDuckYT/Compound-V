package blueduck.compound_v.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Regeneration (Kimiko-style).
 *
 * After not taking damage for 12 seconds, the player heals 1 HP every 10 ticks (Regen II rate).
 * Works regardless of hunger level — does NOT use vanilla Regeneration.
 * Stops when health is full. Taking damage resets the timer.
 */
public class EnhancedRegenEffect extends CompoundVEffect {

    private static final int NO_DAMAGE_TICKS = 240; // 12 seconds
    private static final float HEAL_PER_TICK = 1.0f;  // 1 HP per effect tick (every 10 ticks = Regen II rate)

    private static final Map<UUID, Long> lastDamageTick = new ConcurrentHashMap<>();
    private static final Set<UUID> wasHealing = ConcurrentHashMap.newKeySet();

    public EnhancedRegenEffect(MobEffectCategory category) {
        super(category);
    }

    public static void onPlayerDamaged(UUID playerUUID, long gameTime) {
        lastDamageTick.put(playerUUID, gameTime);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        long now = level.getGameTime();
        long lastHit = lastDamageTick.getOrDefault(uuid, 0L);
        long ticksSinceDamage = now - lastHit;

        boolean healthFull = player.getHealth() >= player.getMaxHealth();

        if (ticksSinceDamage >= NO_DAMAGE_TICKS && !healthFull) {
            // First tick of healing: activation burst
            if (!wasHealing.contains(uuid)) {
                wasHealing.add(uuid);
                level.sendParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        5, 0.4, 0.4, 0.4, 0.02);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        8, 0.5, 0.5, 0.5, 0.05);
            }

            // Direct heal — bypasses hunger requirement entirely
            player.heal(HEAL_PER_TICK);

            // Periodic heart particles
            if (player.tickCount % 20 == 0) {
                level.sendParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.2, player.getZ(),
                        1, 0.3, 0.3, 0.3, 0.01);
            }
        } else {
            wasHealing.remove(uuid);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return tick % 10 == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            lastDamageTick.remove(player.getUUID());
            wasHealing.remove(player.getUUID());
        }
    }
}
