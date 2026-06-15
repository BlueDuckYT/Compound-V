package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.registry.EffectReg;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nullify — projects a nullification aura around the holder.
 *
 * Toggle with V. While active:
 * - Every living entity (mobs AND players) within {@link Config#nullifyRadius} is
 *   afflicted with the NULLIFIED effect, which disables their powers.
 * - Other Nullify holders are IMMUNE — they never receive NULLIFIED.
 * - NULLIFIED suppresses powers (active + passive abilities) but, by design, leaves
 *   the victim's passive stat boosts (strength, damage reduction, knockback) intact.
 * - A light scatter of gray ash particles (basalt-delta style) marks the field.
 */
public class NullifyEffect extends CompoundVEffect {
    private static final Map<UUID, Boolean> activeState = new ConcurrentHashMap<>();
    // Re-applied every tick the entity is in-field. Kept short so powers return
    // almost immediately once the entity leaves the radius or the field is disabled.
    private static final int EFFECT_REFRESH_DURATION = 3;

    public NullifyEffect(MobEffectCategory category) { super(category); }

    @Override
    public PowerType getPowerType() { return PowerType.ACTIVE; }

    // Tick every game tick so the field re-applies NULLIFIED continuously; combined
    // with the short refresh duration, victims regain powers the moment they leave.
    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) { return true; }

    public static boolean isActive(UUID uuid) { return activeState.getOrDefault(uuid, false); }

    /** True if the entity is itself a Nullify holder (and therefore immune to the field). */
    private static boolean isImmune(LivingEntity entity) {
        return entity.hasEffect(EffectReg.NULLIFY.get());
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.activate(player, amplifier, level);
        UUID uuid = player.getUUID();
        boolean nowActive = !activeState.getOrDefault(uuid, false);
        activeState.put(uuid, nowActive);
        if (nowActive) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a78\u00a7lNullification Field: ACTIVE"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "\u00a77Nullification Field: OFF"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (!(entity instanceof ServerPlayer player)) return;
        if (!isActive(player.getUUID())) return;
        ServerLevel level = player.serverLevel();
        double radius = Config.nullifyRadius;

        AABB searchBox = player.getBoundingBox().inflate(radius);
        for (Entity e : level.getEntities(player, searchBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            // Distance gate (AABB is a box; keep the field circular).
            if (target.distanceTo(player) > radius) continue;
            // Other Nullify holders are immune.
            if (isImmune(target)) continue;
            // Afflict mobs and players alike.
            target.addEffect(new MobEffectInstance(
                    EffectReg.NULLIFIED.get(), EFFECT_REFRESH_DURATION, 0, false, true, true));
        }

        // Light gray ash drift around the field edge (basalt-delta style).
        if (player.tickCount % 8 == 0) {
            int points = 16;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI * i) / points + (player.tickCount * 0.01);
                double rr = radius * (0.85 + player.getRandom().nextDouble() * 0.15);
                double px = player.getX() + Math.cos(angle) * rr;
                double pz = player.getZ() + Math.sin(angle) * rr;
                double py = player.getY() + 0.2 + player.getRandom().nextDouble() * 1.5;
                level.sendParticles(ParticleTypes.WHITE_ASH, px, py, pz, 1, 0.05, 0.1, 0.05, 0.0);
            }
        }
        // Occasional ash near the caster for presence.
        if (player.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.WHITE_ASH,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    2, radius * 0.4, 0.8, radius * 0.4, 0.0);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity != null) activeState.remove(entity.getUUID());
    }
}
