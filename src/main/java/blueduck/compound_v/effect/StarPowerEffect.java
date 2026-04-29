package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Star Power — invincibility rampage mode.
 *
 * Press V to activate. For 10 seconds:
 * - You are completely invulnerable (damage set to 0 in ForgeEvents)
 * - Mobs that touch you (within 1.5 blocks) instantly die
 * - You glow with a cycling rainbow particle aura
 * - Glowing effect applied for outline visibility through walls
 * - Speed boost while active
 * - Dramatic jingle sound on activation
 *
 * After the 10 seconds expire, 60-second cooldown before next use.
 * Action bar shows remaining active time or cooldown.
 *
 * For mobs: always-on contact damage aura (smaller radius), rainbow
 * particles, and glowing. Touching the mob hurts you for heavy damage.
 */
public class StarPowerEffect extends CompoundVEffect {

    private static final int ACTIVE_DURATION_TICKS = 200; // 10 seconds
    private static final int COOLDOWN_TICKS = 1200; // 60 seconds
    private static final double KILL_RADIUS = 1.5;
    private static final double MOB_DAMAGE_RADIUS = 1.2;
    private static final float MOB_CONTACT_DAMAGE = 15.0f;

    // Per-player state tracking
    private static final Map<UUID, StarState> stateMap = new ConcurrentHashMap<>();

    // Rainbow color cycle
    private static final DustParticleOptions[] RAINBOW = {
            new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.2f), 1.2f),   // Red
            new DustParticleOptions(new Vector3f(1.0f, 0.6f, 0.1f), 1.2f),   // Orange
            new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.1f), 1.2f),   // Yellow
            new DustParticleOptions(new Vector3f(0.2f, 1.0f, 0.2f), 1.2f),   // Green
            new DustParticleOptions(new Vector3f(0.2f, 0.6f, 1.0f), 1.2f),   // Blue
            new DustParticleOptions(new Vector3f(0.6f, 0.2f, 1.0f), 1.2f),   // Purple
    };

    private static class StarState {
        int activeTicksRemaining;
        long cooldownEndTime;

        StarState() {
            this.activeTicksRemaining = 0;
            this.cooldownEndTime = 0;
        }
    }

    public StarPowerEffect(MobEffectCategory category) {
        super(category);
    }

    public static boolean isStarActive(UUID uuid) {
        StarState state = stateMap.get(uuid);
        return state != null && state.activeTicksRemaining > 0;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        StarState state = stateMap.computeIfAbsent(uuid, k -> new StarState());
        long now = level.getGameTime();

        // === Unlimited mode: simple toggle ===
        if (Config.starPowerUnlimited) {
            if (state.activeTicksRemaining > 0) {
                // Deactivate
                state.activeTicksRemaining = 0;
                level.sendParticles(ParticleTypes.CLOUD,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        15, 0.5, 0.5, 0.5, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                // Activate (no duration limit — stays on until toggled off)
                state.activeTicksRemaining = Integer.MAX_VALUE;
                activationBurst(player, level);
            }
            return;
        }

        // === Normal mode: timed activation with cooldown ===
        // Already active — ignore
        if (state.activeTicksRemaining > 0) return;

        // On cooldown
        if (now < state.cooldownEndTime) {
            int remaining = (int) ((state.cooldownEndTime - now) / 20);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Star Power cooldown: " + remaining + "s"),
                    true);
            return;
        }

        // Activate!
        state.activeTicksRemaining = ACTIVE_DURATION_TICKS;
        activationBurst(player, level);
    }

    private void activationBurst(ServerPlayer player, ServerLevel level) {
        level.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 1.0, player.getZ(),
                1, 0, 0, 0, 0);
        for (DustParticleOptions color : RAINBOW) {
            level.sendParticles(color,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 1.0, 1.0, 1.0, 0.1);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.2F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) return;

        // === Mob behavior: always-on contact damage aura ===
        if (!(entity instanceof ServerPlayer)) {
            tickMobStarPower(entity, level);
            return;
        }

        ServerPlayer player = (ServerPlayer) entity;
        UUID uuid = player.getUUID();
        StarState state = stateMap.get(uuid);

        if (state == null || state.activeTicksRemaining <= 0) return;

        // Only decrement in normal (non-unlimited) mode
        if (!Config.starPowerUnlimited) {
            state.activeTicksRemaining--;

            // Show remaining time
            int secondsLeft = state.activeTicksRemaining / 20;
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6§l★ " + secondsLeft + "s ★"),
                    true);
        } else {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6§l★ ACTIVE ★"),
                    true);
        }

        // Speed boost while active
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 2, false, false, false));

        // === Rainbow particle aura ===
        int colorIndex = (player.tickCount / 3) % RAINBOW.length;
        DustParticleOptions currentColor = RAINBOW[colorIndex];
        DustParticleOptions nextColor = RAINBOW[(colorIndex + 1) % RAINBOW.length];

        // Inner glow — tight to body
        level.sendParticles(currentColor,
                player.getX(), player.getY() + 1.0, player.getZ(),
                3, 0.3, 0.5, 0.3, 0.01);

        // Outer ring — cycling second color
        if (player.tickCount % 2 == 0) {
            double angle1 = (player.tickCount * 0.15) % (Math.PI * 2);
            double angle2 = angle1 + Math.PI;
            double radius = 0.8;
            level.sendParticles(nextColor,
                    player.getX() + Math.cos(angle1) * radius,
                    player.getY() + 0.8 + Math.sin(player.tickCount * 0.1) * 0.4,
                    player.getZ() + Math.sin(angle1) * radius,
                    1, 0.05, 0.05, 0.05, 0.0);
            level.sendParticles(currentColor,
                    player.getX() + Math.cos(angle2) * radius,
                    player.getY() + 1.2 + Math.sin(player.tickCount * 0.1 + Math.PI) * 0.4,
                    player.getZ() + Math.sin(angle2) * radius,
                    1, 0.05, 0.05, 0.05, 0.0);
        }

        // Note particles for music effect
        if (player.tickCount % 8 == 0) {
            level.sendParticles(ParticleTypes.NOTE,
                    player.getX(), player.getY() + 2.0, player.getZ(),
                    1, 0.3, 0.2, 0.3, 0.5);
        }

        // Periodic jingle reminder
        if (player.tickCount % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS,
                    0.3F, 0.8f + (player.tickCount % 40 == 0 ? 0.4f : 0.0f));
        }

        // === Contact kills ===
        AABB killBox = player.getBoundingBox().inflate(KILL_RADIUS);
        for (Entity e : level.getEntities(player, killBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && !(ent instanceof Player))) {
            LivingEntity target = (LivingEntity) e;

            // Kill with extreme damage
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);

            // Death burst — rainbow explosion at kill site
            for (DustParticleOptions color : RAINBOW) {
                level.sendParticles(color,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        5, 0.3, 0.3, 0.3, 0.1);
            }
            level.sendParticles(ParticleTypes.FLASH,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    1, 0, 0, 0, 0);

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4F, 2.0F);
        }

        // === Deactivation when time runs out (normal mode only) ===
        if (!Config.starPowerUnlimited && state.activeTicksRemaining <= 0) {
            state.cooldownEndTime = level.getGameTime() + COOLDOWN_TICKS;

            // Power down effects
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    /**
     * Mob star power: always-on contact damage aura. Touching the mob hurts
     * nearby entities. Rainbow particles + glowing to signal danger.
     */
    private void tickMobStarPower(LivingEntity mob, ServerLevel level) {
        // Glowing
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25, 0, false, false, false));

        // Rainbow particles (less intense than player version)
        if (mob.tickCount % 3 == 0) {
            int colorIndex = (mob.tickCount / 3) % RAINBOW.length;
            level.sendParticles(RAINBOW[colorIndex],
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
                    2, 0.3, 0.4, 0.3, 0.01);
        }

        // Contact damage to nearby entities
        if (mob.tickCount % 5 == 0) { // every quarter second to avoid performance issues
            AABB damageBox = mob.getBoundingBox().inflate(MOB_DAMAGE_RADIUS);
            for (Entity e : level.getEntities(mob, damageBox,
                    ent -> ent instanceof LivingEntity && ent.isAlive() && ent != mob)) {
                LivingEntity target = (LivingEntity) e;
                target.invulnerableTime = 0;
                target.hurt(mob.damageSources().mobAttack((Mob) mob), MOB_CONTACT_DAMAGE);

                // Small rainbow burst on hit
                int colorIndex = (mob.tickCount / 3) % RAINBOW.length;
                level.sendParticles(RAINBOW[colorIndex],
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        3, 0.2, 0.2, 0.2, 0.05);
            }
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
            stateMap.remove(player.getUUID());
        }
    }
}
