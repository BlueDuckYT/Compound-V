package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slime: a toggleable power (like Density). While active the player is a bouncy green blob -
 * bounces off floors/walls, takes no fall damage, takes far MORE knockback but far LESS damage,
 * and moves in a floppy/slippery way with amplified jumps. Green tint + slime particles render
 * client-side via a render layer.
 */
public class SlimeEffect extends CompoundVEffect {

    private static final Set<UUID> slimeActive = ConcurrentHashMap.newKeySet();
    // Persisted (NBT) toggle flag, so slime state survives death and dimension changes.
    private static final String SLIME_ON_TAG = "compound_v_slime_on";
    // Track previous-tick vertical velocity to detect landing impact for the bounce.
    private static final java.util.Map<UUID, Double> prevYMotion = new ConcurrentHashMap<>();
    // Tracks who was on the ground last tick, to detect the jump launch edge.
    private static final Set<UUID> wasOnGround = ConcurrentHashMap.newKeySet();

    public SlimeEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public static boolean isSlime(UUID uuid) {
        return slimeActive.contains(uuid);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();
        // Always-active mode: slime is permanently on while you hold the power, so V does nothing
        // (you can't toggle it off). The tick keeps slimeActive populated.
        if (Config.slimeAlwaysActive) {
            return;
        }
        if (slimeActive.contains(uuid)) {
            slimeActive.remove(uuid);
            player.getPersistentData().putBoolean(SLIME_ON_TAG, false);
            player.removeEffect(net.minecraft.world.effect.MobEffects.JUMP);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 0.6F, 1.2F);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a77Slime: OFF"), true);
        } else {
            slimeActive.add(uuid);
            player.getPersistentData().putBoolean(SLIME_ON_TAG, true);
            // Apply a high-amplitude, short-duration Jump Boost. It's continuously refreshed in
            // the tick (like Speedster's speed) rather than being a single permanent effect - so
            // the hops are punchy (high amplitude) but not floaty (short duration). It also lets
            // the client render layer detect slime mode (SLIME effect + JUMP = tint on).
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, Config.slimeJumpDuration,
                    Config.slimeJumpAmplifier, false, false, false));
            level.sendParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY() + 0.5, player.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.7F, 0.8F);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a7a\u00a7lSlime: ON"), true);
        }
    }

    @Override
    public void clearSecondaryEffects(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        slimeActive.remove(uuid);
        prevYMotion.remove(uuid);
        wasOnGround.remove(uuid);
        entity.removeEffect(net.minecraft.world.effect.MobEffects.JUMP);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true; // tick every tick so jump-refresh / walk-block / bounce logic runs
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) {
            if (slimeActive.remove(entity.getUUID()) && entity instanceof Player sp) {
                sp.removeEffect(net.minecraft.world.effect.MobEffects.JUMP);
            }
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        UUID uuid = player.getUUID();

        // Always-active mode: engage slime automatically (no V toggle needed) for any holder.
        if (Config.slimeAlwaysActive && !slimeActive.contains(uuid)) {
            slimeActive.add(uuid);
        }

        // The in-memory active set is cleared when the player is recreated (death, dimension
        // change), but the persisted effect and the NBT toggle flag survive. Re-sync the active
        // set from the flag so the toggle state isn't lost across those events.
        if (!slimeActive.contains(uuid) && player.getPersistentData().getBoolean(SLIME_ON_TAG)) {
            slimeActive.add(uuid);
        }

        if (!slimeActive.contains(uuid)) {
            prevYMotion.remove(uuid);
            return;
        }

        // Keep the high-amplitude jump boost topped up (short duration, refreshed like Speedster).
        double prevY = prevYMotion.getOrDefault(uuid, 0.0);
        if (!player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)
                || player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getDuration() < 5) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, Config.slimeJumpDuration,
                    Config.slimeJumpAmplifier, false, false, false));
        }

        // NOTE: walk-blocking, the jump hop launch, and bounce are handled CLIENT-SIDE (in
        // ClientForgeHandler.clientSlimeMovement), because the local player's movement is client-
        // authoritative - server-side velocity edits here get overwritten by client prediction.
        // The bounce SFX/particles below are driven by the server detecting a hard landing.

        // Bounce feedback (particles + sound) on a hard, non-sneaking landing.
        if (player.onGround() && prevY < -Config.slimeBounceMinImpact && !player.isShiftKeyDown()) {
            double bounce = Math.min(-prevY * Config.slimeBounceFactor, Config.slimeMaxBounce);
            if (bounce > 0.1) {
                player.fallDistance = 0;
                level.sendParticles(ParticleTypes.ITEM_SLIME,
                        player.getX(), player.getY(), player.getZ(), 8, 0.3, 0.05, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SLIME_BLOCK_FALL, SoundSource.PLAYERS, 0.5F, 0.9F);
            }
        }

        player.fallDistance = 0; // no fall damage while slimed (also cancelled in hurt event)

        // Ambient slime particles.
        if (player.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    2, 0.3, 0.4, 0.3, 0.01);
        }

        prevYMotion.put(uuid, player.getDeltaMovement().y);
    }
}
