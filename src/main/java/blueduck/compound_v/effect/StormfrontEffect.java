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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stormfront — V1 exclusive. Lightning-powered flight.
 *
 * Toggle flight with V (single press). While flying:
 * - Electric particle aura crackles around the player
 * - Lightning trails beneath feet
 * - Hold V to summon lightning bolts at the block/entity you're looking at
 *   (48 block range, configurable cooldown between strikes)
 *
 * The lightning is a real LightningBolt entity — sets fires, damages entities,
 * charges creepers. Player is immune to their own lightning.
 *
 * Combat stats: 1.5x strength, 0.7x damage reduction.
 */
public class StormfrontEffect extends CompoundVEffect {

    private static final double LIGHTNING_RANGE = 48.0;
    private static final int BEAM_LIFETIME = 8; // ticks before ground beams fade

    private static final DustParticleOptions ELECTRIC_PARTICLE = new DustParticleOptions(
            new Vector3f(0.6f, 0.4f, 1.0f), 0.8f); // purple electric
    private static final DustParticleOptions LIGHTNING_GLOW = new DustParticleOptions(
            new Vector3f(0.9f, 0.85f, 1.0f), 1.2f); // white-purple glow

    private static final Map<UUID, Long> lightningCooldownUntil = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHoldTick = new ConcurrentHashMap<>();

    // Ground beam positions — each entry is (position, tick spawned)
    private static final Map<UUID, java.util.List<long[]>> activeBeams = new ConcurrentHashMap<>();

    public StormfrontEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    // === Combat stats ===

    @Override
    public double getStrengthMultiplier(int amplifier) {
        return super.getStrengthMultiplier(amplifier) * 1.5;
    }

    @Override
    public double getDamageReduction(int amplifier) {
        return super.getDamageReduction(amplifier) * 0.5; // 2x standard durability — very tanky
    }

    // === Always-on flight — granted on effect application ===

    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof ServerPlayer player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    // === V key: summon lightning at look target ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (player.isShiftKeyDown()) {
            // Sneak + press: single discharge pulse
            dischargeAOE(player, level);
        } else {
            // Normal press: summon lightning
            summonLightning(player, level);
        }
    }

    // Track discharge state
    private static final Map<UUID, Long> lastDischargeTick = new ConcurrentHashMap<>();

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();
        lastHoldTick.put(uuid, level.getGameTime());

        if (player.isShiftKeyDown()) {
            // Sneaking + hold V: electric discharge AoE
            dischargeAOE(player, level);
        } else {
            // Standing + hold V: rapid lightning (still respects cooldown)
            summonLightning(player, level);
        }
    }

    /**
     * Electric discharge: damages all entities in radius while sneaking + holding V.
     * Similar to PowerPlex but with Stormfront's own config values.
     */
    private void dischargeAOE(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();

        int tickRate = Config.stormfrontDischargeTickRate;
        long lastTick = lastDischargeTick.getOrDefault(uuid, 0L);
        if (now - lastTick < tickRate) return;
        lastDischargeTick.put(uuid, now);

        double radius = Config.stormfrontDischargeRadius;
        float damage = (float) Config.stormfrontDischargeDamage;

        // Purple electric sparks in a sphere
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                20, radius * 0.4, 0.6, radius * 0.4, 0.2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.5F, 1.2F);

        AABB searchBox = player.getBoundingBox().inflate(radius);
        net.minecraft.world.damagesource.DamageSource source = player.damageSources().lightningBolt();

        for (net.minecraft.world.entity.Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);
            if (dist > radius) continue;

            // Zero I-frames for stun effect
            target.invulnerableTime = 0;
            target.hurt(source, damage);

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    5, 0.2, 0.3, 0.2, 0.1);
        }
    }

    private void summonLightning(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();

        if (now < lightningCooldownUntil.getOrDefault(uuid, 0L)) {
            int remaining = (int) ((lightningCooldownUntil.get(uuid) - now) / 20);
            if (remaining > 0) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                "§7Lightning cooldown: " + remaining + "s"),
                        true);
            }
            return;
        }

        // Raycast
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(LIGHTNING_RANGE));

        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        Vec3 strikePos;
        if (hitResult.getType() != HitResult.Type.MISS) {
            strikePos = Vec3.atBottomCenterOf(hitResult.getBlockPos().above());
        } else {
            strikePos = endPos;
        }

        // Summon lightning
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(strikePos.x, strikePos.y, strikePos.z);
            bolt.setCause(player);
            level.addFreshEntity(bolt);
        }

        // Electric burst at player's hands
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + player.getBbHeight() * 0.7, player.getZ(),
                15, 0.3, 0.2, 0.3, 0.15);

        lightningCooldownUntil.put(uuid, now + Config.stormfrontLightningCooldown);
    }

    // === Tick: flight particles + ground lightning beams ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();

        // Re-sync flight if something cleared it
        if (!player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        // Slower flight speed than creative (0.03 vs default 0.05)
        if (player.getAbilities().flying && !player.isCreative()) {
            player.getAbilities().setFlyingSpeed(0.03f);
        }

        boolean isAirborne = !player.onGround();

        // === Visual effects ONLY while flying ===
        if (isAirborne && player.getAbilities().flying) {
            // Electric aura — crackling particles around body
            if (player.tickCount % 2 == 0) {
                for (int i = 0; i < 3; i++) {
                    level.sendParticles(ELECTRIC_PARTICLE,
                            player.getX() + (player.getRandom().nextDouble() - 0.5) * 1.2,
                            player.getY() + player.getRandom().nextDouble() * player.getBbHeight(),
                            player.getZ() + (player.getRandom().nextDouble() - 0.5) * 1.2,
                            1, 0.05, 0.05, 0.05, 0.02);
                }
            }

            // Electric sparks
            if (player.tickCount % 3 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                        4, 0.5, 0.3, 0.5, 0.08);
            }

            // Ground lightning beams
            // Spawn new beams periodically
            if (player.tickCount % 4 == 0) {
                spawnGroundBeam(player, level);
            }

            // Manage beam lifetimes and send render data to clients
            java.util.List<long[]> beams = activeBeams.computeIfAbsent(uuid, k -> new java.util.ArrayList<>());
            long now = level.getGameTime();
            beams.removeIf(beam -> now - beam[3] > BEAM_LIFETIME);

            // Send beam positions to clients for rendered glow
            if (!beams.isEmpty()) {
                int count = beams.size();
                double[] bx = new double[count];
                double[] by = new double[count];
                double[] bz = new double[count];
                for (int i = 0; i < count; i++) {
                    bx[i] = Double.longBitsToDouble(beams.get(i)[0]);
                    by[i] = Double.longBitsToDouble(beams.get(i)[1]);
                    bz[i] = Double.longBitsToDouble(beams.get(i)[2]);
                }
                blueduck.compound_v.keybinds.PacketHandler.sendToTrackingAndSelf(
                        new blueduck.compound_v.util.S2CStormfrontBeamPacket(
                                player.getId(), bx, by, bz, count),
                        player);

                // Sparks at ground contact points
                for (long[] beam : beams) {
                    float age = (float) (now - beam[3]) / BEAM_LIFETIME;
                    if (age < 0.5f) {
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                Double.longBitsToDouble(beam[0]),
                                Double.longBitsToDouble(beam[1]),
                                Double.longBitsToDouble(beam[2]),
                                2, 0.2, 0.1, 0.2, 0.05);
                    }
                }
            }

            // Purple glow beneath feet
            if (player.tickCount % 2 == 0) {
                level.sendParticles(LIGHTNING_GLOW,
                        player.getX(), player.getY() - 0.3, player.getZ(),
                        2, 0.15, 0.0, 0.15, 0.01);
            }

            // Ambient electric hum
            if (player.tickCount % 30 == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.2F, 2.0F);
            }
        } else {
            // On the ground — clear beams
            activeBeams.remove(uuid);
        }
    }

    /**
     * Spawn a lightning beam from player down to the nearest ground below.
     * The beam attaches to a random nearby ground position.
     */
    private void spawnGroundBeam(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();
        java.util.List<long[]> beams = activeBeams.computeIfAbsent(uuid, k -> new java.util.ArrayList<>());

        // Max 3 active beams at a time
        if (beams.size() >= 3) return;

        // Pick a random point near the player's feet, offset horizontally
        double offsetX = (player.getRandom().nextDouble() - 0.5) * 4.0;
        double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4.0;
        double startX = player.getX() + offsetX;
        double startZ = player.getZ() + offsetZ;

        // Raycast down to find ground
        Vec3 rayStart = new Vec3(startX, player.getY(), startZ);
        Vec3 rayEnd = new Vec3(startX, player.getY() - 30, startZ);
        BlockHitResult hit = level.clip(new ClipContext(
                rayStart, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 groundPos = Vec3.atBottomCenterOf(hit.getBlockPos().above());
            beams.add(new long[]{
                    Double.doubleToLongBits(groundPos.x),
                    Double.doubleToLongBits(groundPos.y),
                    Double.doubleToLongBits(groundPos.z),
                    level.getGameTime()
            });
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
            lightningCooldownUntil.remove(uuid);
            lastHoldTick.remove(uuid);
            activeBeams.remove(uuid);
            lastDischargeTick.remove(uuid);

            // Remove flight and restore default flight speed
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f); // restore default
                player.onUpdateAbilities();
            }
        }
    }

    /**
     * Check if a player currently has Stormfront flight active.
     */
    public static boolean isFlying(UUID uuid) {
        return true; // always-on flight
    }
}
