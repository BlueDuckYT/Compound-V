package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class SpeedsterEffect extends CompoundVEffect {

    private static final DustParticleOptions BLUE_LIGHTNING = new DustParticleOptions(
            new Vector3f(0.2f, 0.6f, 1.0f), 1.0f);
    private static final DustParticleOptions BRIGHT_BLUE = new DustParticleOptions(
            new Vector3f(0.4f, 0.8f, 1.0f), 0.6f);

    // Blood mist for speed kills (same colors as HeadPopEffect but reused here)
    private static final DustParticleOptions BLOOD_DARK = new DustParticleOptions(
            new Vector3f(0.5f, 0.0f, 0.0f), 1.0f);
    private static final DustParticleOptions BLOOD_BRIGHT = new DustParticleOptions(
            new Vector3f(0.9f, 0.1f, 0.05f), 1.5f);
    private static final DustParticleOptions BLOOD_MIST = new DustParticleOptions(
            new Vector3f(0.7f, 0.05f, 0.02f), 0.8f);

    public SpeedsterEffect(MobEffectCategory category) {
        super(category);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        boolean hasSpeedEffects = entity.hasEffect(MobEffects.MOVEMENT_SPEED) && entity.hasEffect(MobEffects.DIG_SPEED);
        if (hasSpeedEffects || !(entity instanceof Player)) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, amplifier * 2));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, amplifier));

            // Blue lightning particles when moving
            if (entity.level() instanceof ServerLevel serverLevel && entity instanceof Player) {
                double speed = entity.getDeltaMovement().horizontalDistance();
                if (speed > 0.1) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            entity.getX(), entity.getY() + 0.5, entity.getZ(),
                            (int) (3 + speed * 5), 0.3, 0.5, 0.3, 0.05);
                    serverLevel.sendParticles(BLUE_LIGHTNING,
                            entity.getX(), entity.getY() + 0.8, entity.getZ(),
                            (int) (2 + speed * 4), 0.4, 0.6, 0.4, 0.02);
                    serverLevel.sendParticles(BRIGHT_BLUE,
                            entity.getX(), entity.getY() + 1.0, entity.getZ(),
                            (int) (1 + speed * 2), 0.15, 0.3, 0.15, 0.01);
                    if (speed > 0.4) {
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                entity.getX(), entity.getY() + 0.3, entity.getZ(),
                                (int) (speed * 3), 0.2, 0.1, 0.2, 0.02);
                    }
                }
            }

            // Sprint damage to nearby mobs
            if (Config.speedsterMobAttack && amplifier > 1 && entity.isSprinting()) {
                BlockPos blockpos = entity.blockPosition();
                AABB aabb = (new AABB(blockpos)).inflate(0.6D);
                List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(LivingEntity.class, aabb);
                if (!entity.level().isClientSide) {
                    for (LivingEntity livingentity : nearbyEntities) {
                        if (livingentity.isAlive() && !livingentity.isRemoved()
                                && blockpos.closerToCenterThan(livingentity.position(), 2.0D)
                                && !livingentity.equals(entity)) {

                            // Increased damage: base 2 + 1.5 per amplifier level above 1
                            // At level 5 (amplifier=4): 2 + 1.5*3 = 6.5 per hit, hits every tick while sprinting
                            float damage = 2.0f + 1.5f * (amplifier - 1);
                            float healthBefore = livingentity.getHealth();

                            livingentity.invulnerableTime = 0;
                            livingentity.hurt(entity.damageSources().mobAttack(entity), damage);

                            // Impact sparks
                            if (entity.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                        livingentity.getX(), livingentity.getY() + livingentity.getBbHeight() / 2,
                                        livingentity.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
                            }

                            // Red mist blood cloud if the hit killed them
                            if (!livingentity.isAlive() && entity.level() instanceof ServerLevel sl) {
                                double mx = livingentity.getX();
                                double my = livingentity.getY() + livingentity.getBbHeight() * 0.6;
                                double mz = livingentity.getZ();

                                // Core blood burst
                                sl.sendParticles(BLOOD_BRIGHT, mx, my, mz,
                                        30, 0.3, 0.4, 0.3, 0.12);
                                sl.sendParticles(BLOOD_DARK, mx, my, mz,
                                        50, 0.6, 0.5, 0.6, 0.08);
                                sl.sendParticles(BLOOD_MIST, mx, my - 0.2, mz,
                                        25, 0.8, 0.6, 0.8, 0.05);
                                sl.sendParticles(ParticleTypes.ITEM_SLIME, mx, my, mz,
                                        8, 0.3, 0.3, 0.3, 0.1);

                                // Directional splatter (6 directions, smaller than head pop)
                                for (int i = 0; i < 6; i++) {
                                    double angle = (2 * Math.PI * i) / 6.0;
                                    for (double r = 0.3; r < 1.2; r += 0.4) {
                                        double px = mx + Math.cos(angle) * r;
                                        double pz = mz + Math.sin(angle) * r;
                                        sl.sendParticles(BLOOD_DARK, px, my - r * 0.2, pz,
                                                1, 0.05, 0.1, 0.05, 0.02);
                                    }
                                }

                                // Wet impact sound
                                sl.playSound(null, mx, my, mz,
                                        SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 0.6F, 0.8F);
                            }
                        }
                    }
                }
            }
        }
    }

    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);

        if (player.hasEffect(MobEffects.MOVEMENT_SPEED) && player.hasEffect(MobEffects.DIG_SPEED)) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.DIG_SPEED);
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, amplifier * 2));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, amplifier));

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.5, 1.0, 0.5, 0.15);
            level.sendParticles(BLUE_LIGHTNING,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.6, 1.2, 0.6, 0.08);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.3F, 1.5F);
        }
    }

    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        int k = 20 >> p_19456_;
        if (k > 0) {
            return p_19455_ % k == 0;
        } else {
            return true;
        }
    }
}