package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
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
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase / Intangibility (mixin-free implementation).
 *
 * Sneak+V toggles a phased state. While phased the player has Entity.noPhysics = true
 * (passes through blocks) and is granted hover-flight so they don't fall through the
 * world. They're immune to suffocation and most damage while phased, with a translucent
 * shimmer. Toggling off does a safe-eject: if the player is embedded in blocks, they are
 * nudged to the nearest open space before solidifying so they never suffocate.
 *
 * This is "noclip-hover" phasing rather than walk-on-the-floor-through-walls, which would
 * require a collision mixin. It is deliberately mixin-free for build robustness.
 */
public class PhaseEffect extends CompoundVEffect {

    private static final Set<UUID> phased = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> nextToggleAllowed = new ConcurrentHashMap<>();

    public PhaseEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public static boolean isPhased(UUID uuid) {
        return phased.contains(uuid);
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        // Only the sneak action toggles phase (keeps tap V free for a future blink).
        if (!player.isShiftKeyDown()) return;

        UUID uuid = player.getUUID();
        long now = level.getGameTime();
        if (now < nextToggleAllowed.getOrDefault(uuid, 0L)) return;
        nextToggleAllowed.put(uuid, now + 8);

        if (phased.contains(uuid)) {
            disablePhase(player, level);
        } else {
            enablePhase(player, level);
        }
    }

    private void enablePhase(ServerPlayer player, ServerLevel level) {
        phased.add(player.getUUID());
        player.noPhysics = true;
        blueduck.compound_v.keybinds.PacketHandler.sendToPlayer(
                new blueduck.compound_v.util.S2CPhaseSyncPacket(true), player);
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "\u00a7d\u00a7lPhase: ON"), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.6F);
        level.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.8, 0.4, 0.1);
    }

    private void disablePhase(ServerPlayer player, ServerLevel level) {
        // Safe-eject: if solidifying here would trap the player inside blocks, find the
        // nearest open space and move them there first.
        if (isStuckInBlocks(player)) {
            Vec3 safe = findSafeSpot(player);
            if (safe != null) {
                player.teleportTo(safe.x, safe.y, safe.z);
            }
        }
        clearPhaseState(player);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "\u00a77Phase: OFF"), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.7F, 0.8F);
    }

    /** Removes phase and restores normal physics/flight. Safe to call unconditionally. */
    private static void clearPhaseState(ServerPlayer player) {
        phased.remove(player.getUUID());
        player.noPhysics = false;
        blueduck.compound_v.keybinds.PacketHandler.sendToPlayer(
                new blueduck.compound_v.util.S2CPhaseSyncPacket(false), player);
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        UUID uuid = player.getUUID();

        // If powers get suppressed (virus/nullify) while phased, force-solidify safely.
        if (CompoundVEffect.arePowersSuppressed(entity)) {
            if (phased.contains(uuid)) {
                if (isStuckInBlocks(player)) {
                    Vec3 safe = findSafeSpot(player);
                    if (safe != null) player.teleportTo(safe.x, safe.y, safe.z);
                }
                clearPhaseState(player);
            }
            return;
        }

        if (!phased.contains(uuid)) return;

        // Keep noPhysics + flight asserted (other systems can clear them).
        player.noPhysics = true;
        if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        // Suffocation safety while inside blocks.
        player.setAirSupply(player.getMaxAirSupply());

        // Translucent shimmer.
        if (player.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    2, 0.3, 0.5, 0.3, 0.02);
        }
    }

    /** True if the player's body currently overlaps non-passable blocks. */
    private static boolean isStuckInBlocks(ServerPlayer player) {
        return !player.level().noCollision(player, player.getBoundingBox().deflate(0.05));
    }

    /**
     * Searches upward then outward for an open space the player fits in. Returns a safe
     * position or null if none found nearby (caller leaves the player where they are).
     */
    private static Vec3 findSafeSpot(ServerPlayer player) {
        var level = player.level();
        double x = player.getX(), y = player.getY(), z = player.getZ();
        // Try straight up first (climb out of a wall/floor), then a small 3D search.
        for (int dy = 0; dy <= 6; dy++) {
            if (fits(player, x, y + dy, z)) return new Vec3(x, y + dy, z);
        }
        for (int r = 1; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -2; dy <= 4; dy++) {
                        double nx = x + dx, ny = y + dy, nz = z + dz;
                        if (fits(player, nx, ny, nz)) return new Vec3(nx, ny, nz);
                    }
                }
            }
        }
        return null;
    }

    private static boolean fits(ServerPlayer player, double x, double y, double z) {
        var box = player.getDimensions(player.getPose()).makeBoundingBox(new Vec3(x, y, z));
        return player.level().noCollision(player, box);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof ServerPlayer player) {
            // Always restore physics/flight when the effect ends, with a safe-eject.
            if (phased.contains(player.getUUID())) {
                if (isStuckInBlocks(player)) {
                    Vec3 safe = findSafeSpot(player);
                    if (safe != null) player.teleportTo(safe.x, safe.y, safe.z);
                }
                clearPhaseState(player);
            }
            nextToggleAllowed.remove(player.getUUID());
        }
    }
}
