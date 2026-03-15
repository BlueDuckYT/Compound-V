package blueduck.compound_v.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sonic Scream.
 *
 * Press V → 1.5 second charge with escalating audio/visuals → warden-style sonic boom cone.
 * 30 second cooldown. Cone deals 10 damage (sonic boom, bypasses armor), knockback, Darkness.
 */
public class SonicScreamEffect extends CompoundVEffect {

    private static final int CHARGE_TICKS = 30;        // 1.5 seconds (ticks every tick while charging)
    private static final int COOLDOWN_TICKS = 300;      // 15 seconds
    private static final float DAMAGE = 10.0f;
    private static final double RANGE = 15.0;
    private static final double CONE_HALF_ANGLE = 45.0; // degrees

    private static final Map<UUID, Integer> chargingTicks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

    public SonicScreamEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();

        // Check cooldown
        long now = level.getGameTime();
        long cdEnd = cooldownUntil.getOrDefault(uuid, 0L);
        if (now < cdEnd) {
            level.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    5, 0.3, 0.3, 0.3, 0.01);
            return;
        }

        if (chargingTicks.containsKey(uuid)) {
            // Cancel charge
            chargingTicks.remove(uuid);
            level.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    8, 0.3, 0.5, 0.3, 0.02);
        } else {
            // Begin charge — play initial roar immediately
            chargingTicks.put(uuid, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ANGRY, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        Integer ticks = chargingTicks.get(uuid);
        if (ticks == null) return;

        ticks++;
        chargingTicks.put(uuid, ticks);

        float progress = Math.min(1.0f, (float) ticks / CHARGE_TICKS);

        // --- Escalating charge-up particles ---
        if (ticks % 3 == 0) {
            int sparkCount = (int) (1 + progress * 8);
            double spread = 0.3 + progress * 0.4;
            level.sendParticles(ParticleTypes.SCULK_SOUL,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    sparkCount, spread, 0.4, spread, 0.02);
        }

        // Sonic boom preview particles in the last third of charge
        if (progress > 0.66f && ticks % 5 == 0) {
            level.sendParticles(ParticleTypes.SONIC_BOOM,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    1, 0.3, 0.2, 0.3, 0.0);
        }

        // --- Escalating rumble sound (3 stages) ---
        if (ticks == 8) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.PLAYERS, 0.6F, 0.7F);
        }
        if (ticks == 18) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        if (ticks == 25) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.5F, 1.3F);
        }

        // --- Fire when fully charged ---
        if (ticks >= CHARGE_TICKS) {
            fireScream(player, amplifier, level);
            chargingTicks.remove(uuid);
            cooldownUntil.put(uuid, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        // Tick every single tick so charging feels responsive
        return true;
    }

    private void fireScream(ServerPlayer player, int amplifier, ServerLevel level) {
        Vec3 lookDir = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        double cosThreshold = Math.cos(Math.toRadians(CONE_HALF_ANGLE));

        // --- Sonic boom sound ---
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5F, 1.0F);

        // --- Cone damage ---
        BlockPos pos = player.blockPosition();
        AABB aabb = (new AABB(pos)).inflate(RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity target : targets) {
            if (target == player) continue;
            if (!target.isAlive() || target.isRemoved()) continue;

            Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2, 0).subtract(playerPos);
            double dist = toTarget.length();
            if (dist > RANGE || dist < 0.5) continue;

            // Cone check
            double dot = toTarget.normalize().dot(lookDir);
            if (dot < cosThreshold) continue;

            // Sonic boom damage (bypasses armor)
            float falloff = 1.0f - (float) (dist / RANGE) * 0.4f;
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().sonicBoom(player), DAMAGE * falloff);

            // Heavy knockback
            double knockScale = 2.0 - (dist / RANGE);
            Vec3 knockDir = toTarget.normalize();
            target.push(knockDir.x * knockScale, 0.4 * knockScale, knockDir.z * knockScale);
            target.hurtMarked = true;

            // Darkness
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false, true));

            // Hit particles
            level.sendParticles(ParticleTypes.SONIC_BOOM,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 0.3, 0.3, 0.3, 0.0);
        }

        // --- Cone visual ---
        for (int i = 0; i < 40; i++) {
            double radYaw = Math.toRadians(player.getYRot() + (player.getRandom().nextDouble() - 0.5) * CONE_HALF_ANGLE * 2);
            double radPitch = Math.toRadians(player.getXRot() + (player.getRandom().nextDouble() - 0.5) * CONE_HALF_ANGLE * 2);

            double dist = 2.0 + player.getRandom().nextDouble() * (RANGE - 2.0);
            double px = playerPos.x - Math.sin(radYaw) * Math.cos(radPitch) * dist;
            double py = playerPos.y - Math.sin(radPitch) * dist;
            double pz = playerPos.z + Math.cos(radYaw) * Math.cos(radPitch) * dist;

            level.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }

        level.sendParticles(ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            chargingTicks.remove(player.getUUID());
            cooldownUntil.remove(player.getUUID());
        }
    }
}
