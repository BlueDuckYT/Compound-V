package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.entity.IceProjectileEntity;
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
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cryokinesis — ice control. Counterpart to Pyrokinesis.
 *
 * - Tap V: throw a bouncing ice ball ({@link IceProjectileEntity}) that freezes the
 *   first entity it hits and skips across water. Costs 1 charge from a hidden pool.
 * - Sneak + V: toggle a frost aura. While active it slows and freezes nearby mobs,
 *   leaves a light snowfall, and slightly slows the caster as its upkeep cost.
 *
 * The ice-ball capacity is intentionally NOT shown to the player (learn it by feel).
 */
public class CryokinesisEffect extends CompoundVEffect {

    private static class CryoState {
        int charges;
        int regenCounter;
        boolean auraActive;
        long lastTapTick = -1;
        boolean initialized;
        boolean heldThisTick;   // set by holdActivate each tick V is held
        boolean charging;       // currently charging a big cryoball
        int chargeTicks;        // how long the charged shot has been building
    }

    private static final Map<UUID, CryoState> stateMap = new ConcurrentHashMap<>();
    private static final UUID AURA_SLOW_ID = UUID.fromString("c0ffee00-1ce0-4aaa-bbbb-c0ffeec0ffee");

    public CryokinesisEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    private static CryoState state(UUID uuid) {
        return stateMap.computeIfAbsent(uuid, k -> {
            CryoState s = new CryoState();
            s.charges = Config.cryoMaxCharges;
            s.initialized = true;
            return s;
        });
    }

    public static boolean isAuraActive(UUID uuid) {
        CryoState s = stateMap.get(uuid);
        return s != null && s.auraActive;
    }

    // === Tap V ===

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        UUID uuid = player.getUUID();
        CryoState s = state(uuid);

        if (player.isShiftKeyDown()) {
            // Sneak + V: toggle frost aura.
            toggleAura(player, level, s);
            return;
        }
        // Plain V: the ball is thrown on RELEASE by the charge state machine in applyEffectTick
        // (short hold = normal ball, long hold = big charged ball). When charging is disabled in
        // config, a tap throws a basic ball once per press (handled in the tick machine).
    }

    private void toggleAura(ServerPlayer player, ServerLevel level, CryoState s) {
        s.auraActive = !s.auraActive;
        if (s.auraActive) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a7b\u00a7lFrost Aura: ON"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.7F, 0.6F);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a77Frost Aura: OFF"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, 0.8F);
            removeSelfSlow(player);
        }
    }

    /** Throw a normal (tap) ice ball. */
    private void throwNormalBall(ServerPlayer player, ServerLevel level, CryoState s) {
        if (s.charges <= 0) {
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.6F);
            return;
        }
        s.charges--;
        s.lastTapTick = level.getGameTime();

        Vec3 look = player.getLookAngle();
        IceProjectileEntity ball = new IceProjectileEntity(level, player);
        ball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        ball.shoot(look.x, look.y, look.z, (float) Config.cryoBallSpeed, 0.5F);
        level.addFreshEntity(ball);

        level.sendParticles(ParticleTypes.SNOWFLAKE,
                player.getX(), player.getEyeY(), player.getZ(), 8, 0.1, 0.1, 0.1, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    /** Throw the big, clunky charged cryoball that AOE-freezes on expiry. */
    private void throwChargedBall(ServerPlayer player, ServerLevel level, CryoState s) {
        if (s.charges <= 0) {
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 1.6F);
            return;
        }
        s.charges--;
        s.lastTapTick = level.getGameTime();

        Vec3 look = player.getLookAngle();
        IceProjectileEntity ball = new IceProjectileEntity(level, player);
        ball.setCharged(true); // bigger, heavier, AOE-freeze on expire
        ball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        // Slower than a normal throw — clunkier lob.
        ball.shoot(look.x, look.y, look.z, (float) Config.cryoChargedBallSpeed, 1.0F);
        level.addFreshEntity(ball);

        level.sendParticles(ParticleTypes.SNOWFLAKE,
                player.getX(), player.getEyeY(), player.getZ(), 20, 0.2, 0.2, 0.2, 0.04);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                player.getX(), player.getEyeY(), player.getZ(), 10, 0.15, 0.15, 0.15, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    // === Hold V: charge a big cryoball ===

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        if (CompoundVEffect.arePowersSuppressed(player)) return;
        if (player.isShiftKeyDown()) return; // sneaking is the aura toggle, not charging
        state(player.getUUID()).heldThisTick = true;
    }

    // === Tick: capacity regen + frost aura ===

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) {
            if (entity instanceof Player p) removeSelfSlow(p);
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        UUID uuid = player.getUUID();
        CryoState s = state(uuid);

        // Regenerate stored ice balls.
        if (s.charges < Config.cryoMaxCharges) {
            s.regenCounter++;
            if (s.regenCounter >= Config.cryoChargeRegenTicks) {
                s.regenCounter = 0;
                s.charges = Math.min(Config.cryoMaxCharges, s.charges + 1);
            }
        }

        // Charge / release state machine. Holding plain V builds charge; releasing throws —
        // short hold = normal ball, long hold = big charged cryoball that AOE-freezes.
        boolean held = s.heldThisTick;
        s.heldThisTick = false;

        if (!Config.cryoChargeEnabled) {
            // Charge-up disabled: V is a simple tap that throws a basic ice ball once per press.
            if (held) {
                if (!s.charging) { s.charging = true; throwNormalBall(player, level, s); }
            } else {
                s.charging = false;
            }
        } else if (held) {
            if (!s.charging) {
                s.charging = true;
                s.chargeTicks = 0;
            } else {
                s.chargeTicks = Math.min(Config.cryoMaxChargeTime, s.chargeTicks + 1);
            }
            // Charge-up frost gathering at the hand.
            Vec3 look = player.getLookAngle();
            Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 hand = player.getEyePosition().add(look.scale(1.1)).add(right.scale(0.35)).add(0, -0.35, 0);
            int count = 1 + (int) (3 * ((float) s.chargeTicks / Math.max(1, Config.cryoMaxChargeTime)));
            level.sendParticles(ParticleTypes.SNOWFLAKE, hand.x, hand.y, hand.z, count, 0.06, 0.06, 0.06, 0.005);
            if (s.chargeTicks == Config.cryoMaxChargeTime) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.6F, 0.5F);
            }
        } else if (s.charging) {
            s.charging = false;
            if (s.chargeTicks >= Config.cryoChargedMinHoldTicks) {
                throwChargedBall(player, level, s);
            } else {
                throwNormalBall(player, level, s);
            }
            s.chargeTicks = 0;
        }

        if (!s.auraActive) {
            removeSelfSlow(player);
            return;
        }

        // Frost aura upkeep: slight self-slow.
        applySelfSlow(player);

        double radius = Config.cryoAuraRadius;
        AABB box = player.getBoundingBox().inflate(radius);
        for (Entity e : level.getEntities(player, box,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            if (target.distanceTo(player) > radius) continue;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    20, Config.cryoAuraSlownessAmplifier, false, false, true));
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 20,
                    target.getTicksFrozen() + 8));
        }

        // Frost Walker-style: freeze water directly beneath the player while aura is on.
        if (Config.cryoAuraFreezesWater) {
            freezeUnderfoot(player, level);
        }

        // Light snowfall around the player.
        if (player.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    4, radius * 0.4, 0.6, radius * 0.4, 0.01);
        }
    }

    private void freezeUnderfoot(ServerPlayer player, ServerLevel level) {
        int r = 1;
        net.minecraft.core.BlockPos base = player.blockPosition().below();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                net.minecraft.core.BlockPos pos = base.offset(dx, 0, dz);
                var bs = level.getBlockState(pos);
                var fs = bs.getFluidState();
                if (!fs.isEmpty() && fs.is(net.minecraft.tags.FluidTags.WATER)
                        && fs.isSource()
                        && level.getBlockState(pos.above()).isAir()) {
                    level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState());
                }
            }
        }
    }

    private void applySelfSlow(Player player) {
        var attr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        if (attr.getModifier(AURA_SLOW_ID) == null) {
            attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    AURA_SLOW_ID, "Frost aura upkeep",
                    -Config.cryoAuraSelfSlow,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private void removeSelfSlow(Player player) {
        var attr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(AURA_SLOW_ID) != null) {
            attr.removeModifier(AURA_SLOW_ID);
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
            removeSelfSlow(player);
            stateMap.remove(player.getUUID());
        }
    }
}
