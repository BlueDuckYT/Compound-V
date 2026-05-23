package blueduck.compound_v.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DensityEffect extends CompoundVEffect {

    private static final Set<UUID> denseActive = new HashSet<>();
    private static final Set<UUID> wasInAir = new HashSet<>();
    private static final Map<UUID, Double> fallStartY = new ConcurrentHashMap<>();

    public DensityEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public static boolean isDense(UUID uuid) {
        return denseActive.contains(uuid);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();

        if (denseActive.contains(uuid)) {
            denseActive.remove(uuid);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.4, 0.5, 0.4, 0.03);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 0.5F, 1.5F);
        } else {
            denseActive.add(uuid);
            level.sendParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    15, 0.3, 0.3, 0.3, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 0.6F, 0.6F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.2F, 0.5F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        if (!denseActive.contains(uuid)) {
            wasInAir.remove(uuid);
            fallStartY.remove(uuid);
            return;
        }

        if (player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();

            // Prevent swimming entirely — you're too heavy
            player.setSwimming(false);
            player.setSprinting(false);

            // Strong downward pull that overwhelms jump/swim input
            double sinkRate = -0.08;
            double maxSinkSpeed = -0.6;
            double newY = motion.y - sinkRate;
            // Cancel any upward movement from swimming/jumping in water
            if (newY > 0) {
                newY = -0.04;
            }
            newY = Math.max(newY, maxSinkSpeed);

            // Heavily dampen horizontal movement — walking on the bottom, not swimming
            player.setDeltaMovement(motion.x * 0.7, newY, motion.z * 0.7);
            player.hurtMarked = true;

            // Bubble particles as you sink
            if (player.tickCount % 5 == 0) {
                level.sendParticles(ParticleTypes.BUBBLE,
                        player.getX(), player.getY() + player.getBbHeight(), player.getZ(),
                        3, 0.2, 0.1, 0.2, 0.02);
            }
        }
        else {
            Vec3 motion = player.getDeltaMovement();

            double sinkRate = -0.15;
            double newY = Math.max(motion.y + sinkRate, -1.0);
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, false));

        boolean onGround = player.onGround();
        boolean airLastTick = wasInAir.contains(uuid);

        if (!onGround) {
            wasInAir.add(uuid);
            if (!fallStartY.containsKey(uuid) || player.getDeltaMovement().y >= 0) {
                fallStartY.put(uuid, player.getY());
            }
        }

        if (onGround && airLastTick) {
            wasInAir.remove(uuid);
            Double startY = fallStartY.remove(uuid);

            if (startY != null) {
                double fallDist = startY - player.getY();

                if (fallDist > 1.5) {
                    float stompDamage = Math.min(40.0f, (float) (fallDist * 2.0));
                    double stompRange = Math.min(4.0, 1.0 + fallDist * 0.15);

                    BlockPos landPos = player.blockPosition();
                    AABB aabb = (new AABB(landPos)).inflate(stompRange);
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb);

                    for (LivingEntity target : targets) {
                        if (target == player || !target.isAlive() || target.isRemoved()) continue;
                        double dist = target.position().distanceTo(player.position());
                        if (dist > stompRange) continue;

                        float falloff = 1.0f - (float) (dist / stompRange) * 0.6f;
                        target.invulnerableTime = 0;
                        target.hurt(player.damageSources().playerAttack(player), stompDamage * falloff);

                        Vec3 knockDir = target.position().subtract(player.position()).normalize();
                        target.push(knockDir.x * 0.5, 0.3 + fallDist * 0.05, knockDir.z * 0.5);
                        target.hurtMarked = true;
                    }

                    BlockState belowState = level.getBlockState(landPos.below());
                    int particleCount = (int) Math.min(40, 8 + fallDist * 3);

                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, belowState),
                            player.getX(), player.getY(), player.getZ(),
                            particleCount, stompRange * 0.3, 0.1, stompRange * 0.3, 0.15);
                    level.sendParticles(ParticleTypes.CRIT,
                            player.getX(), player.getY() + 0.2, player.getZ(),
                            particleCount / 2, stompRange * 0.2, 0.2, stompRange * 0.2, 0.1);

                    int ringCount = (int) (8 + fallDist * 2);
                    for (int i = 0; i < ringCount; i++) {
                        double angle = (2 * Math.PI * i) / ringCount;
                        double px = player.getX() + Math.cos(angle) * stompRange * 0.7;
                        double pz = player.getZ() + Math.sin(angle) * stompRange * 0.7;
                        level.sendParticles(ParticleTypes.CLOUD,
                                px, player.getY() + 0.1, pz,
                                1, 0.1, 0.05, 0.1, 0.01);
                    }

                    float volume = Math.min(1.5f, 0.4f + (float) fallDist * 0.08f);
                    float pitch = Math.max(0.5f, 1.0f - (float) fallDist * 0.03f);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, volume, pitch);
                    if (fallDist > 6) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, volume * 0.4f, 0.8f);
                    }
                }
            }
        }

        if (onGround && player.getDeltaMovement().horizontalDistance() > 0.05 && player.tickCount % 8 == 0) {
            BlockState belowState = level.getBlockState(player.blockPosition().below());
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, belowState),
                    player.getX(), player.getY(), player.getZ(),
                    2, 0.2, 0.05, 0.2, 0.01);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            UUID uuid = player.getUUID();
            denseActive.remove(uuid);
            wasInAir.remove(uuid);
            fallStartY.remove(uuid);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.5; // 50% more damage — heavy hitter
    }
}