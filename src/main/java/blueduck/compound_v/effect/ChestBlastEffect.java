package blueduck.compound_v.effect;

import blueduck.compound_v.item.AntiVItem;
import blueduck.compound_v.keybinds.PacketHandler;
import blueduck.compound_v.registry.EffectReg;
import blueduck.compound_v.util.S2CLaserSyncPacket;
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
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chest Blast — Soldier Boy's signature power. V1 exclusive.
 *
 * Passive bonuses:
 * - Double damage output (2x strength multiplier on top of base)
 * - Significantly more durable (extra damage reduction)
 * - Heavy knockback resistance
 *
 * Active ability (V key):
 * - 10 second charge-up with escalating particles and sound
 * - Then fires a wide devastating beam from the chest for 10 seconds
 * - Beam follows player look direction
 * - Strips Compound V powers from anything it hits (like Anti-V)
 * - Deals heavy damage (8 per tick, 160 dps)
 * - 60 second cooldown after blast ends
 *
 * Beam rendering: wide golden/orange particle cone from chest height,
 * with nuclear flash particles and radiation crackling sounds.
 */
public class ChestBlastEffect extends CompoundVEffect {

    private static final double BLAST_WIDTH = 0.8;          // beam cone half-width — tighter to match visuals

    private static final DustParticleOptions BLAST_CORE = new DustParticleOptions(
            new Vector3f(1.0f, 0.85f, 0.2f), 2.0f); // bright gold
    private static final DustParticleOptions BLAST_EDGE = new DustParticleOptions(
            new Vector3f(1.0f, 0.5f, 0.1f), 1.5f);  // orange edge
    private static final DustParticleOptions CHARGE_PARTICLE = new DustParticleOptions(
            new Vector3f(1.0f, 0.7f, 0.1f), 0.8f);  // charge glow

    private enum BlastState { IDLE, CHARGING, BLASTING, NOVA_CHARGING }

    private static class PlayerBlastState {
        BlastState state = BlastState.IDLE;
        int ticksRemaining;
        long cooldownEndTime;
        Vec3 beamDirection = null;
        final java.util.Set<UUID> strippedThisBlast = new java.util.HashSet<>();
    }

    private static final double PLAYER_BLAST_TURN_SPEED = 5.0; // max degrees per tick

    private static final Map<UUID, PlayerBlastState> stateMap = new ConcurrentHashMap<>();

    public ChestBlastEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // === Combat stat overrides — Soldier Boy is a tank ===

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 2.0;
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.5; // Takes half damage on top of base reduction
    }

    @Override
    public double getKnockbackReduction(int amplifier) {
        return super.getKnockbackReduction(amplifier) * 0.3; // Barely moves
    }

    // === Activation ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        PlayerBlastState state = stateMap.computeIfAbsent(uuid, k -> new PlayerBlastState());
        long now = level.getGameTime();

        // Already in any active state — ignore
        if (state.state != BlastState.IDLE) return;

        // Shared cooldown for both beam and nova
        if (now < state.cooldownEndTime) {
            int remaining = (int) ((state.cooldownEndTime - now) / 20);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7Chest Blast cooldown: " + remaining + "s"), true);
            return;
        }

        if (player.isShiftKeyDown()) {
            // Sneak + V: nova burst
            state.state = BlastState.NOVA_CHARGING;
            state.ticksRemaining = blueduck.compound_v.Config.chestBlastNovaChargeTime;

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 2.0F, 0.3F);
        } else {
            // Standing + V: beam blast
            state.state = BlastState.CHARGING;
            state.ticksRemaining = blueduck.compound_v.Config.chestBlastChargeTime;

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 0.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }

    // === Effect tick ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        PlayerBlastState state = stateMap.get(uuid);
        if (state == null || state.state == BlastState.IDLE) return;

        state.ticksRemaining--;

        if (state.state == BlastState.CHARGING) {
            tickCharging(player, level, state);
        } else if (state.state == BlastState.BLASTING) {
            tickBlasting(player, level, state);
        } else if (state.state == BlastState.NOVA_CHARGING) {
            tickNovaCharging(player, level, state);
        }
    }

    private void tickCharging(ServerPlayer player, ServerLevel level, PlayerBlastState state) {
        int elapsed = blueduck.compound_v.Config.chestBlastChargeTime - state.ticksRemaining;
        float chargePercent = (float) elapsed / blueduck.compound_v.Config.chestBlastChargeTime;
        int secondsLeft = state.ticksRemaining / 20;

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§6§lChest Blast charging: " + secondsLeft + "s"),
                true);

        // Escalating charge particles — more intense as charge builds
        int particleCount = (int) (2 + chargePercent * 10);
        double chestY = player.getY() + player.getBbHeight() * 0.6;

        if (player.tickCount % 2 == 0) {
            // Particles spiral inward toward chest
            for (int i = 0; i < particleCount; i++) {
                double angle = (player.tickCount * 0.3 + i * (Math.PI * 2.0 / particleCount)) % (Math.PI * 2);
                double radius = 1.5 * (1.0 - chargePercent * 0.6); // tightens as charge builds
                double px = player.getX() + Math.cos(angle) * radius;
                double pz = player.getZ() + Math.sin(angle) * radius;
                level.sendParticles(CHARGE_PARTICLE, px, chestY, pz,
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Magnetism-style ambient drift particles — spawn at random positions and drift inward
        if (player.tickCount % 4 == 0) {
            int driftCount = (int) (1 + chargePercent * 4);
            for (int i = 0; i < driftCount; i++) {
                double angle = player.getRandom().nextDouble() * Math.PI * 2;
                double radius = 1.5 + player.getRandom().nextDouble() * 2.0;
                double py = chestY - 0.5 + player.getRandom().nextDouble() * 1.0;
                double px = player.getX() + Math.cos(angle) * radius;
                double pz = player.getZ() + Math.sin(angle) * radius;
                level.sendParticles(BLAST_EDGE, px, py, pz,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Electric sparks as charge builds past 50%
        if (chargePercent > 0.5 && player.tickCount % 6 == 0) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2;
            double radius = 0.5 + player.getRandom().nextDouble() * 1.0;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX() + Math.cos(angle) * radius,
                    chestY + (player.getRandom().nextDouble() - 0.5) * 0.5,
                    player.getZ() + Math.sin(angle) * radius,
                    1, 0.05, 0.05, 0.05, 0.02);
        }

        // Core glow at chest — intensifies
        if (player.tickCount % 3 == 0 && chargePercent > 0.3) {
            level.sendParticles(BLAST_CORE,
                    player.getX(), chestY, player.getZ(),
                    (int) (chargePercent * 5), 0.15, 0.15, 0.15, 0.01);
        }

        // Escalating sound
        if (player.tickCount % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS,
                    0.5F + chargePercent * 1.0F, 0.5F + chargePercent * 0.8F);
        }
        if (chargePercent > 0.7 && player.tickCount % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8F, 1.5F);
        }

        // Charge complete — transition to blasting
        if (state.ticksRemaining <= 0) {
            state.state = BlastState.BLASTING;
            state.ticksRemaining = blueduck.compound_v.Config.chestBlastDuration;
            state.strippedThisBlast.clear();
            state.beamDirection = null; // fresh start for turn-speed limiting

            // === Initial explosive burst — radial knockback + damage around the player ===
            AABB burstBox = player.getBoundingBox().inflate(8.0);
            for (Entity e : level.getEntities(player, burstBox,
                    ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
                LivingEntity target = (LivingEntity) e;
                double dist = target.distanceTo(player);
                if (dist > 8.0) continue;

                // Damage falls off with distance
                float burstDamage = (float) (blueduck.compound_v.Config.chestBlastBurstDamage * (1.0 - dist / 8.0));
                target.hurt(player.damageSources().playerAttack(player), burstDamage);

                // Knockback away from player — stronger at close range
                Vec3 knockDir = target.position().subtract(player.position()).normalize();
                double knockStrength = 2.0 * (1.0 - dist / 8.0);
                target.setDeltaMovement(knockDir.x * knockStrength, 0.5, knockDir.z * knockStrength);
                target.hurtMarked = true;
            }

            // Non-destructive explosion visual + sound (no block damage)
            level.explode(player, player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                    0.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);

            // Big flash on blast start
            level.sendParticles(ParticleTypes.FLASH,
                    player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                    3, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                    5, 0.5, 0.3, 0.5, 0.0);
            // Shockwave ring particles
            for (int i = 0; i < 24; i++) {
                double angle = (i / 24.0) * Math.PI * 2;
                double px = player.getX() + Math.cos(angle) * 3.0;
                double pz = player.getZ() + Math.sin(angle) * 3.0;
                level.sendParticles(BLAST_CORE, px, player.getY() + player.getBbHeight() * 0.6, pz,
                        1, 0.1, 0.1, 0.1, 0.05);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.3F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.4F);
        }
    }

    private void tickBlasting(ServerPlayer player, ServerLevel level, PlayerBlastState state) {
        int secondsLeft = state.ticksRemaining / 20;
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§c§lChest Blast active: " + secondsLeft + "s"),
                true);

        // Freeze player in place — can still look around but cannot move
        player.setDeltaMovement(0, player.onGround() ? 0 : player.getDeltaMovement().y, 0);
        player.hurtMarked = true;
        // High-level slowness to override client-side WASD input
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false, false));

        Vec3 desiredDir = player.getLookAngle();
        Vec3 chestPos = new Vec3(player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ());
        double blastRange = blueduck.compound_v.Config.chestBlastRange;

        // Turn speed limiting — beam can only rotate a few degrees per tick
        if (state.beamDirection == null) {
            state.beamDirection = desiredDir;
        }
        double angleBetween = Math.acos(Math.max(-1, Math.min(1, state.beamDirection.dot(desiredDir))));
        double maxAngle = Math.toRadians(PLAYER_BLAST_TURN_SPEED);
        Vec3 lookDir;
        if (angleBetween <= maxAngle || angleBetween < 0.001) {
            lookDir = desiredDir;
        } else {
            double t = maxAngle / angleBetween;
            lookDir = new Vec3(
                    state.beamDirection.x + (desiredDir.x - state.beamDirection.x) * t,
                    state.beamDirection.y + (desiredDir.y - state.beamDirection.y) * t,
                    state.beamDirection.z + (desiredDir.z - state.beamDirection.z) * t
            ).normalize();
        }
        state.beamDirection = lookDir;

        // === Raycast against blocks — beam stops at walls if configured ===
        Vec3 beamEnd = chestPos.add(lookDir.scale(blastRange));
        double effectiveRange = blastRange;
        Vec3 hitPos;
        if (blueduck.compound_v.Config.chestBlastBlockedByWalls) {
            BlockHitResult blockHit = level.clip(new ClipContext(
                    chestPos, beamEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (blockHit.getType() != HitResult.Type.MISS) {
                hitPos = blockHit.getLocation();
                effectiveRange = chestPos.distanceTo(hitPos);
            } else {
                hitPos = beamEnd;
            }
        } else {
            hitPos = beamEnd;
        }
        PacketHandler.sendToTrackingAndSelf(
                new S2CLaserSyncPacket(player.getId(), hitPos.x, hitPos.y, hitPos.z,
                        S2CLaserSyncPacket.COLOR_CHEST_BLAST),
                player);

        // Chest glow particles on the player
        level.sendParticles(BLAST_CORE,
                player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                3, 0.2, 0.15, 0.2, 0.01);

        // Impact flash at end of beam
        if (player.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.EXPLOSION,
                    hitPos.x, hitPos.y, hitPos.z,
                    1, 1.0, 1.0, 1.0, 0.0);
        }

        // === Destroy blocks in the beam's path ===
        double blockBreakChance = blueduck.compound_v.Config.chestBlastBlockBreakChance;
        if (blockBreakChance > 0) {
            double step = 0.8;
            int steps = (int) (blastRange / step);
            for (int i = 2; i <= steps; i++) {
                Vec3 beamPoint = chestPos.add(lookDir.scale(i * step));
                net.minecraft.core.BlockPos corePos = net.minecraft.core.BlockPos.containing(beamPoint.x, beamPoint.y, beamPoint.z);
                tryBreakBlock(level, corePos, player, 0.95);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                            double chance = manhattan <= 1 ? blockBreakChance : blockBreakChance * 0.3;
                            tryBreakBlock(level, corePos.offset(dx, dy, dz), player, chance);
                        }
                    }
                }
            }
        }

        // === Damage and power-strip entities in the cone ===
        AABB searchBox = player.getBoundingBox().inflate(effectiveRange);
        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;

            // Check if entity is within the cone
            Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(chestPos);
            double distToTarget = toTarget.length();
            if (distToTarget > effectiveRange || distToTarget < 0.5) continue;

            // Dot product check — how aligned is the target with the look direction
            double dot = toTarget.normalize().dot(lookDir);
            // Required alignment loosens with distance (wider cone)
            double coneWidthAtDist = BLAST_WIDTH / distToTarget;
            double minDot = 1.0 - Math.min(coneWidthAtDist, 0.25);

            if (dot < minDot) continue;

            // === Hit! ===
            boolean alreadyStripped = state.strippedThisBlast.contains(target.getUUID());

            // Deal damage — respect mercy iframes for recently stripped targets
            if (!alreadyStripped || target.invulnerableTime <= 0) {
                target.invulnerableTime = 0;
                if (target instanceof Player) {
                    target.hurt(player.damageSources().playerAttack(player),
                            (float) blueduck.compound_v.Config.chestBlastBeamDamage * 0.5f);
                } else {
                    target.hurt(player.damageSources().playerAttack(player),
                            (float) blueduck.compound_v.Config.chestBlastBeamDamage);
                }
            }

            // Strip Compound V powers — once per target per blast (if enabled)
            if (!alreadyStripped && blueduck.compound_v.Config.chestBlastStripsPowers) {
                boolean isInvincible = target.hasEffect(EffectReg.INVINCIBLE.get());
                boolean canStrip = !isInvincible || blueduck.compound_v.Config.chestBlastStripsInvincible;
                boolean shieldBlocking = blueduck.compound_v.Config.chestBlastShieldBlocksStrip && target.isBlocking();
                if (shieldBlocking) canStrip = false;
                // Check if target actually has CompoundV effects to strip
                boolean hadCompoundV = false;
                for (var inst : target.getActiveEffects()) {
                    if (inst.getEffect() instanceof CompoundVEffect) {
                        hadCompoundV = true;
                        break;
                    }
                }

                if (hadCompoundV && canStrip) {
                    AntiVItem.stripCompoundVEffects(target);
                    state.strippedThisBlast.add(target.getUUID());

                    // Blast the target away from the beam — should launch them out of range
                    Vec3 knockDir = toTarget.normalize();
                    double knockStrength = 2.5;
                    target.setDeltaMovement(knockDir.x * knockStrength, 0.6, knockDir.z * knockStrength);
                    target.hurtMarked = true;

                    // Mercy iframes after being stripped — 40 ticks (2 seconds) to escape
                    target.invulnerableTime = 40;

                    // Dramatic strip visual
                    level.sendParticles(ParticleTypes.FLASH,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            2, 0, 0, 0, 0);
                    level.sendParticles(ParticleTypes.SQUID_INK,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            8, 0.3, 0.4, 0.3, 0.05);
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
                } else if (hadCompoundV && !canStrip) {
                    // Invincible or shield resists the strip — sparks fly but nothing happens
                    state.strippedThisBlast.add(target.getUUID()); // don't retry
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            15, 0.3, 0.4, 0.3, 0.1);
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.5F);
                }
                // Entities without CompoundV get no mercy — continuous beam damage
            }

            // Hit particles on target (ongoing damage visual)
            if (player.tickCount % 5 == 0) {
                level.sendParticles(BLAST_EDGE,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
            }
        }

        // === Sound ===
        if (player.tickCount % 3 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.3F);
        }
        if (player.tickCount % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.5F, 0.4F);
        }

        // === Blast ends ===
        if (state.ticksRemaining <= 0) {
            state.state = BlastState.IDLE;
            state.cooldownEndTime = level.getGameTime() + blueduck.compound_v.Config.chestBlastCooldown;

            // Wind-down effects
            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + player.getBbHeight() * 0.6, player.getZ(),
                    20, 0.5, 0.3, 0.5, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }

    /**
     * Check if a player is currently charging or blasting (for external use, e.g. rendering).
     */
    public static boolean isBlasting(UUID uuid) {
        PlayerBlastState state = stateMap.get(uuid);
        return state != null && state.state == BlastState.BLASTING;
    }

    public static boolean isCharging(UUID uuid) {
        PlayerBlastState state = stateMap.get(uuid);
        return state != null && state.state == BlastState.CHARGING;
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

    private static void tryBreakBlock(ServerLevel level, net.minecraft.core.BlockPos pos, ServerPlayer player, double chance) {
        if (player.getRandom().nextDouble() >= chance) return;
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0 || hardness >= 50) return;
        level.destroyBlock(pos, blueduck.compound_v.Config.chestBlastBlockBreakDrops, player);
    }

    // === Nova Burst (sneak + V) ===

    private void tickNovaCharging(ServerPlayer player, ServerLevel level, PlayerBlastState state) {
        float chargePercent = 1.0f - ((float) state.ticksRemaining / blueduck.compound_v.Config.chestBlastNovaChargeTime);
        int secondsLeft = Math.max(0, state.ticksRemaining / 20);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§c§lNova charging: " + secondsLeft + "s"), true);

        // Intensifying inward particles — pulling energy toward the player
        int particleCount = (int) (3 + chargePercent * 15);
        double radius = blueduck.compound_v.Config.chestBlastNovaRadius;
        level.sendParticles(CHARGE_PARTICLE,
                player.getX(), player.getY() + 1.0, player.getZ(),
                particleCount, radius * 0.5 * (1 - chargePercent), 0.5, radius * 0.5 * (1 - chargePercent), 0.02);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                (int) (2 + chargePercent * 8), 0.5, 0.5, 0.5, 0.05 + chargePercent * 0.1);

        // Rumble sound as charge builds
        if (state.ticksRemaining % 20 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.5F + chargePercent, 0.3F + chargePercent * 0.5F);
        }

        if (state.ticksRemaining <= 0) {
            detonateNova(player, level, state);
        }
    }

    private void detonateNova(ServerPlayer player, ServerLevel level, PlayerBlastState state) {
        double radius = blueduck.compound_v.Config.chestBlastNovaRadius;
        float damage = (float) blueduck.compound_v.Config.chestBlastNovaDamage;
        double knockback = blueduck.compound_v.Config.chestBlastNovaKnockback;

        // === Heavy visual and audio ===
        // Explosion particle burst
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1.0, player.getZ(),
                3, 0.5, 0.5, 0.5, 0.0);
        level.sendParticles(BLAST_CORE,
                player.getX(), player.getY() + 1.0, player.getZ(),
                40, radius * 0.3, 0.5, radius * 0.3, 0.3);
        level.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 1.0, player.getZ(),
                5, 0.1, 0.1, 0.1, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                60, radius * 0.5, 1.0, radius * 0.5, 0.2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0F, 0.3F);

        // === Damage and depower entities ===
        AABB searchBox = player.getBoundingBox().inflate(radius);
        for (net.minecraft.world.entity.Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > radius) continue;

            // Check if target has Compound V powers
            boolean hasCompV = false;
            for (MobEffectInstance inst : target.getActiveEffects()) {
                if (inst.getEffect() instanceof CompoundVEffect) { hasCompV = true; break; }
            }

            // Zero I-frames and deal damage
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), damage);

            // Knockback away from player — scaled by proximity
            double distFactor = 1.0 - (dist / radius); // closer = stronger
            Vec3 pushDir = target.position().subtract(player.position()).normalize();
            pushDir = new Vec3(pushDir.x, 0.3, pushDir.z).normalize(); // slight upward launch
            target.setDeltaMovement(target.getDeltaMovement().add(pushDir.scale(knockback * distFactor)));
            target.hurtMarked = true;

            // Strip powers from powered entities
            if (hasCompV && blueduck.compound_v.Config.chestBlastStripsPowers) {
                boolean isInvincible = target.hasEffect(EffectReg.INVINCIBLE.get());
                boolean canStrip = !isInvincible || blueduck.compound_v.Config.chestBlastStripsInvincible;
                if (canStrip) {
                    blueduck.compound_v.item.AntiVItem.stripCompoundVEffects(target);
                    level.sendParticles(ParticleTypes.FLASH,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            2, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }

        // === Sphere block destruction ===
        int blockRadius = (int) Math.ceil(radius);
        net.minecraft.core.BlockPos center = player.blockPosition();
        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -blockRadius; dy <= blockRadius; dy++) {
                for (int dz = -blockRadius; dz <= blockRadius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    double maxDistSq = radius * radius;
                    if (distSq > maxDistSq) continue;

                    // Core (inner 60%): near-guaranteed break
                    // Edge (outer 40%): chance-based for rough look
                    double distRatio = Math.sqrt(distSq) / radius;
                    double chance = distRatio < 0.6 ? 0.95 : 0.4 * (1.0 - distRatio);
                    tryBreakBlock(level, center.offset(dx, dy, dz), player, chance);
                }
            }
        }

        // === Cooldown and reset (shared with beam) ===
        state.state = BlastState.IDLE;
        state.cooldownEndTime = level.getGameTime() + blueduck.compound_v.Config.chestBlastCooldown;
    }
}
