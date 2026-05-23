package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.effect.negative.BadCompoundVEffect;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Mimic — Copy another entity's Compound V power.
 *
 * Press V while looking at a LivingEntity within reach range that has a CompoundVEffect.
 * Copies their effect for a configurable duration (default 30 sec).
 * Can only copy if the player has no other CompoundVEffect besides Mimic itself.
 * While a copied power is active, V activates that power's abilities instead.
 *
 * The target keeps their power — this is a copy, not a steal.
 */
public class MimicEffect extends CompoundVEffect {

    private static final double COPY_RANGE = 5.0;
    private static final int COPY_COOLDOWN = 10; // 0.5 seconds before copied power activates
    private static final DustParticleOptions MIRROR_PARTICLE = new DustParticleOptions(
            new Vector3f(0.7f, 0.9f, 1.0f), 1.2f);

    // Track when a copy was made — powers don't activate until cooldown expires
    private static final java.util.Map<java.util.UUID, Long> copyCooldownUntil = new java.util.concurrent.ConcurrentHashMap<>();

    public MimicEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // No passive combat buffs — mimic relies entirely on copied powers

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        // Check if player already has a copied power active (any CompoundVEffect besides Mimic)
        for (MobEffectInstance inst : player.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !(inst.getEffect() instanceof MimicEffect)
                    && !(inst.getEffect() instanceof BadCompoundVEffect)) {
                // Already has a copied power — V should activate that power instead.
                // The keybind handler routes to the non-Mimic effect, so this shouldn't fire.
                // But just in case, do nothing here.
                return;
            }
        }

        // Raycast for entity with CompoundV effect within reach
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(COPY_RANGE));

        AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(COPY_RANGE)).inflate(1.0);
        LivingEntity target = null;
        MobEffectInstance targetEffect = null;
        double closest = COPY_RANGE + 1;

        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            AABB entBox = e.getBoundingBox().inflate(0.3);
            var hitResult = entBox.clip(eyePos, endPos);
            if (hitResult.isPresent()) {
                double dist = eyePos.distanceTo(hitResult.get());
                if (dist < closest && e instanceof LivingEntity living) {
                    // Find their CompoundV effect
                    for (MobEffectInstance inst : living.getActiveEffects()) {
                        if (inst.getEffect() instanceof CompoundVEffect
                                && !(inst.getEffect() instanceof MimicEffect)
                                && !(inst.getEffect() instanceof BadCompoundVEffect)) {
                            closest = dist;
                            target = living;
                            targetEffect = inst;
                            break;
                        }
                    }
                }
            }
        }

        if (target == null || targetEffect == null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7No power to copy in range"),
                    true);
            return;
        }

        // Copy the power — special case: if target has flight + lasers, give advanced lasers instead
        int duration = Config.mimicDuration;
        boolean targetHasFlight = target.hasEffect(
                blueduck.compound_v.registry.EffectReg.CREATIVE_FLIGHT.get());
        boolean targetHasBasicLaser = target.hasEffect(
                blueduck.compound_v.registry.EffectReg.LASER_EYES_BASIC.get());
        boolean targetHasAnyLaser = targetHasBasicLaser || target.hasEffect(
                blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED.get());

        if (targetHasFlight && targetHasAnyLaser) {
            // Flight + any laser = give advanced laser eyes
            MobEffectInstance copy = new MobEffectInstance(
                    blueduck.compound_v.registry.EffectReg.LASER_EYES_ADVANCED.get(),
                    duration, 0, false, false, false);
            copy.setCurativeItems(new java.util.ArrayList<>());
            player.addEffect(copy);
        } else {
            MobEffectInstance copy = new MobEffectInstance(
                    targetEffect.getEffect(), duration, targetEffect.getAmplifier(),
                    false, false, false);
            copy.setCurativeItems(new java.util.ArrayList<>());
            player.addEffect(copy);
        }

        // Set copy cooldown — prevents immediately using the copied power
        copyCooldownUntil.put(player.getUUID(), level.getGameTime() + COPY_COOLDOWN);

        // Visual feedback — mirror/glass shatter effect
        level.sendParticles(MIRROR_PARTICLE,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
        level.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                15, 0.5, 0.5, 0.5, 0.3);

        // Particles on target too — they see the copy happen
        level.sendParticles(MIRROR_PARTICLE,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10, 0.3, 0.3, 0.3, 0.05);

        // Sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6F, 1.2F);

        // Get the effect name for the message
        String effectName = targetEffect.getEffect().getDescriptionId();
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§b§lPower copied!"),
                true);
    }

    /**
     * Check if a player has a copied (non-Mimic) CompoundV effect active AND the copy cooldown has expired.
     * Used by the keybind handler to route V presses to the copied power.
     * Returns null if in cooldown — V routes to Mimic instead.
     */
    public static MobEffectInstance getCopiedEffect(Player player) {
        // Check copy cooldown
        Long cooldown = copyCooldownUntil.get(player.getUUID());
        if (cooldown != null && player.level().getGameTime() < cooldown) {
            return null; // still in cooldown, don't route to copied power
        }

        for (MobEffectInstance inst : player.getActiveEffects()) {
            if (inst.getEffect() instanceof CompoundVEffect
                    && !(inst.getEffect() instanceof MimicEffect)
                    && !(inst.getEffect() instanceof BadCompoundVEffect)) {
                return inst;
            }
        }
        return null;
    }
}
