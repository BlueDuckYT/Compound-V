package blueduck.compound_v.effect;

import blueduck.compound_v.util.PehkuiHelper;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

/**
 * Enlarge power: grow to 3x size with bonus damage, damage reduction,
 * and trampling of smaller entities.
 *
 * Trampling: while enlarged and moving, entities whose bounding box height
 * is less than half of the enlarged entity's are knocked aside and take
 * moderate damage. Uses Pehkui-aware bounding box heights.
 *
 * Damage reduction: enlarged entities take 40% less damage (stacks with
 * general Compound V reduction for players). Handled in ForgeEvents.
 */
public class EnlargeEffect extends CompoundVEffect {

    private static final UUID ENLARGE_DAMAGE_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d21");
    private static final UUID ENLARGE_BLOCK_REACH_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d22");
    private static final UUID ENLARGE_ENTITY_REACH_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d23");
    private static final UUID ENLARGE_ATTACK_SPEED_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d24");
    private static final UUID ENLARGE_SLOW_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d25");
    private static final UUID ENLARGE_KBR_UUID = UUID.fromString("d5e78c9a-1b3f-4a7e-9c2d-8f6b5a4e3d26");
    public static final float ENLARGE_SCALE = 3.0f;
    public static final float ENLARGE_DAMAGE_REDUCTION = 0.6f; // take 60% damage = 40% reduction

    private static final float TRAMPLE_DAMAGE = 5.0f;

    public EnlargeEffect(MobEffectCategory category) {
        super(category);
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    public static boolean isPehkuiLoaded() {
        return ModList.get().isLoaded("pehkui");
    }

    /**
     * Checks if an entity is currently enlarged (scale > 1.5).
     */
    public static boolean isEnlarged(LivingEntity entity) {
        if (!isPehkuiLoaded()) return false;
        return PehkuiHelper.getTargetScale(entity) > 1.5f;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        if (!isPehkuiLoaded()) return;

        float currentScale = PehkuiHelper.getTargetScale(player);
        boolean isCurrentlyEnlarged = currentScale > 1.5f;

        if (isCurrentlyEnlarged) {
            PehkuiHelper.resetScale(player);
            removeBoosts(player);

            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    15, 0.8, 1.0, 0.8, 0.05);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 0.6F, 1.2F);
        } else {
            PehkuiHelper.setScale(player, ENLARGE_SCALE);
            applyBoosts(player);

            level.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 1.5, 2.0, 1.5, 0.1);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX(), player.getY(), player.getZ(),
                    10, 1.0, 0.5, 1.0, 0.02);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.8F, 0.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.3F, 0.6F);
        }
    }

    private static void applyBoosts(Player player) {
        // Damage boost
        var dmg = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null && dmg.getModifier(ENLARGE_DAMAGE_UUID) == null) {
            dmg.addTransientModifier(new AttributeModifier(
                    ENLARGE_DAMAGE_UUID, "Enlarge damage boost", 4.0, AttributeModifier.Operation.ADDITION));
        }
        // Block reach boost (+3 blocks)
        var blockReach = player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get());
        if (blockReach != null && blockReach.getModifier(ENLARGE_BLOCK_REACH_UUID) == null) {
            blockReach.addTransientModifier(new AttributeModifier(
                    ENLARGE_BLOCK_REACH_UUID, "Enlarge block reach", 3.0, AttributeModifier.Operation.ADDITION));
        }
        // Entity reach boost (+3 blocks)
        var entityReach = player.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());
        if (entityReach != null && entityReach.getModifier(ENLARGE_ENTITY_REACH_UUID) == null) {
            entityReach.addTransientModifier(new AttributeModifier(
                    ENLARGE_ENTITY_REACH_UUID, "Enlarge entity reach", 3.0, AttributeModifier.Operation.ADDITION));
        }
        // Slower attack speed (mining-fatigue-like heavy swings): -40%
        var atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpeed != null && atkSpeed.getModifier(ENLARGE_ATTACK_SPEED_UUID) == null) {
            atkSpeed.addTransientModifier(new AttributeModifier(
                    ENLARGE_ATTACK_SPEED_UUID, "Enlarge slow swing", -0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        // Slower movement: -25%
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(ENLARGE_SLOW_UUID) == null) {
            speed.addTransientModifier(new AttributeModifier(
                    ENLARGE_SLOW_UUID, "Enlarge slow", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        // Knockback resistance: nearly immovable
        var kbr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbr != null && kbr.getModifier(ENLARGE_KBR_UUID) == null) {
            kbr.addTransientModifier(new AttributeModifier(
                    ENLARGE_KBR_UUID, "Enlarge knockback resist", 0.8, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeBoosts(Player player) {
        var dmg = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.removeModifier(ENLARGE_DAMAGE_UUID);
        var blockReach = player.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get());
        if (blockReach != null) blockReach.removeModifier(ENLARGE_BLOCK_REACH_UUID);
        var entityReach = player.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());
        if (entityReach != null) entityReach.removeModifier(ENLARGE_ENTITY_REACH_UUID);
        var atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpeed != null) atkSpeed.removeModifier(ENLARGE_ATTACK_SPEED_UUID);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(ENLARGE_SLOW_UUID);
        var kbr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbr != null) kbr.removeModifier(ENLARGE_KBR_UUID);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!isPehkuiLoaded()) return;
        if (!(entity.level() instanceof ServerLevel sl)) return;

        float scale = PehkuiHelper.getTargetScale(entity);
        if (scale <= 1.5f) return; // Not enlarged

        // Ensure boosts are applied (handles login, chunk reload)
        if (entity instanceof Player player) {
            applyBoosts(player);
        }

        // Ground dust while walking
        if (entity instanceof ServerPlayer player) {
            if (player.onGround() && player.getDeltaMovement().horizontalDistance() > 0.05
                    && player.tickCount % 4 == 0) {
                BlockState belowState = sl.getBlockState(player.blockPosition().below());
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, belowState),
                        player.getX(), player.getY(), player.getZ(),
                        2, 0.5, 0.1, 0.5, 0.01);
            }
        }

        // === Trampling: damage entities significantly smaller than us ===
        if (entity.tickCount % 5 != 0) return; // every quarter second
        double speed = entity.getDeltaMovement().horizontalDistance();
        if (speed < 0.03) return; // must be moving

        float myHeight = entity.getBbHeight(); // already Pehkui-scaled
        AABB trampleBox = entity.getBoundingBox().inflate(0.3);

        for (Entity e : sl.getEntities(entity, trampleBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != entity)) {
            LivingEntity target = (LivingEntity) e;
            float targetHeight = target.getBbHeight();

            // Only trample entities whose height is less than half ours
            if (targetHeight >= myHeight * 0.5f) continue;

            // Don't trample players (PvP trampling would be annoying)
            if (target instanceof Player && entity instanceof Player) continue;

            // Cooldown check via invulnerableTime
            if (target.invulnerableTime > 0) continue;

            // Trample! Damage scales slightly with size ratio
            float sizeRatio = myHeight / Math.max(targetHeight, 0.1f);
            float damage = TRAMPLE_DAMAGE * Math.min(sizeRatio * 0.3f, 2.0f);

            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                target.hurt(entity.damageSources().mobAttack(mob), damage);
            } else if (entity instanceof Player player) {
                target.hurt(entity.damageSources().playerAttack(player), damage);
            }

            // Knock them aside
            Vec3 knockDir = target.position().subtract(entity.position()).normalize();
            target.push(knockDir.x * 0.5, 0.2, knockDir.z * 0.5);
            target.hurtMarked = true;

            // Ground impact particles at trample point
            sl.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 0.2, target.getZ(),
                    4, 0.2, 0.1, 0.2, 0.05);
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true; // every tick for trampling checks
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            if (isPehkuiLoaded()) {
                PehkuiHelper.resetScale(player);
            }
            removeBoosts(player);
        }
    }
}
