package blueduck.compound_v.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Victoria Neuman-style head pop / blood manipulation.
 *
 * Hold V while looking at a living entity to build focus.
 * - NO damage during focus — only visual/audio feedback.
 * - Redstone-colored particles at the target's head intensify with focus.
 * - After 4 seconds of sustained focus, the head pops: up to 40 damage.
 * - Works on all mobs, but damage is capped so bosses survive.
 * - Looking away or target going out of range (32 blocks) resets focus.
 * - Kill triggers a massive blood particle explosion.
 */
public class HeadPopEffect extends CompoundVEffect {

    private static final double RANGE = 32.0;
    private static final int TICKS_TO_POP = 80;
    private static final float POP_DAMAGE = 40.0f;

    // Blood particles
    private static final DustParticleOptions BLOOD_DARK = new DustParticleOptions(
            new Vector3f(0.5f, 0.0f, 0.0f), 1.0f);
    private static final DustParticleOptions BLOOD_BRIGHT = new DustParticleOptions(
            new Vector3f(0.9f, 0.1f, 0.05f), 1.5f);
    private static final DustParticleOptions BLOOD_MIST = new DustParticleOptions(
            new Vector3f(0.7f, 0.05f, 0.02f), 0.8f);

    // Per-player focus tracking
    private static final Map<UUID, FocusData> focusMap = new ConcurrentHashMap<>();

    private static class FocusData {
        int targetId;
        int ticksFocused;

        FocusData(int targetId) {
            this.targetId = targetId;
            this.ticksFocused = 0;
        }
    }

    public HeadPopEffect(MobEffectCategory category) {
        super(category);
    }


    @Override
    public PowerType getPowerType() {
        return PowerType.ACTIVE;
    }

    @Override
    public void holdActivate(ServerPlayer player, int amplifier, ServerLevel level) {
        super.holdActivate(player, amplifier, level);

        UUID uuid = player.getUUID();

        // Raycast to find target
        LivingEntity target = getTargetedEntity(player, level);

        if (target == null) {
            // Not looking at anything — reset focus
            if (focusMap.containsKey(uuid)) {
                resetFocus(uuid, level);
            }
            return;
        }

        // Get or create focus data
        FocusData focus = focusMap.get(uuid);
        if (focus == null || focus.targetId != target.getId()) {
            // New target or switched target — start fresh
            if (focus != null) {
                resetFocus(uuid, level);
            }
            focus = new FocusData(target.getId());
            focusMap.put(uuid, focus);
        }

        focus.ticksFocused++;
        float progress = Math.min(1.0f, (float) focus.ticksFocused / (TICKS_TO_POP - amplifier * 15));

        // --- Visual feedback: redstone particles at head, escalating ---
        double headY = target.getY() + target.getBbHeight() - 0.2;
        double headX = target.getX();
        double headZ = target.getZ();

        // Dark blood particles — count and spread increase
        int darkCount = 1 + (int) (progress * 6);
        double spread = 0.12 + progress * 0.08;
        level.sendParticles(BLOOD_DARK,
                headX, headY, headZ,
                darkCount, spread, spread, spread, 0.005);

        // Bright blood particles appear past 30%
        if (progress > 0.3f) {
            int brightCount = (int) ((progress - 0.3f) / 0.7f * 8);
            level.sendParticles(BLOOD_BRIGHT,
                    headX, headY, headZ,
                    brightCount, spread * 0.7, spread * 0.7, spread * 0.7, 0.01);
        }

        // Blood mist halo past 60%
        if (progress > 0.6f && focus.ticksFocused % 2 == 0) {
            level.sendParticles(BLOOD_MIST,
                    headX, headY + 0.1, headZ,
                    2, 0.2, 0.15, 0.2, 0.02);
        }

        // Dripping blood past 75%
        if (progress > 0.75f && focus.ticksFocused % 4 == 0) {
            level.sendParticles(ParticleTypes.DRIPPING_LAVA,
                    headX, headY, headZ,
                    1, 0.1, 0.0, 0.1, 0.0);
        }

        // --- Audio feedback: wet squelching, escalating ---
        if (focus.ticksFocused % 20 == 0) {
            float volume = 0.15f + progress * 0.5f;
            float pitch = 0.5f + progress * 0.6f;
            level.playSound(null, headX, target.getY(), headZ,
                    SoundEvents.HONEY_BLOCK_STEP, SoundSource.HOSTILE, volume, pitch);
        }
        if (progress > 0.4f && focus.ticksFocused % 15 == 0) {
            level.playSound(null, headX, target.getY(), headZ,
                    SoundEvents.MUD_STEP, SoundSource.HOSTILE, 0.2f + progress * 0.4f, 0.8f);
        }
        // Crunching sound in final moments
        if (progress > 0.85f && focus.ticksFocused % 8 == 0) {
            level.playSound(null, headX, target.getY(), headZ,
                    SoundEvents.BONE_BLOCK_BREAK, SoundSource.HOSTILE, 0.4f, 0.6f);
        }

        // --- NO damage during focus. Only the pop deals damage. ---

        // --- Pop! ---
        if (focus.ticksFocused >= (TICKS_TO_POP - amplifier * 10)) {
            popHead(player, target, level);
            focusMap.remove(uuid);
        }
    }

    /**
     * The head pop: capped damage + massive blood explosion.
     */
    private void popHead(ServerPlayer player, LivingEntity target, ServerLevel level) {
        double headX = target.getX();
        double headY = target.getY() + target.getBbHeight();
        double headZ = target.getZ();

        // --- Damage: capped at POP_DAMAGE, so bosses survive ---
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().indirectMagic(player, player), POP_DAMAGE);

        boolean killed = !target.isAlive();

        // === BLOOD EXPLOSION ===

        // Core burst: dense bright blood at head
        level.sendParticles(BLOOD_BRIGHT,
                headX, headY, headZ,
                80, 0.3, 0.3, 0.3, 0.2);

        // Dark blood spray — wider spread
        level.sendParticles(BLOOD_DARK,
                headX, headY, headZ,
                120, 0.8, 0.6, 0.8, 0.15);

        // Blood mist cloud
        level.sendParticles(BLOOD_MIST,
                headX, headY - 0.3, headZ,
                60, 1.0, 0.8, 1.0, 0.08);

        // Vanilla redstone dust for extra red splatter
        level.sendParticles(ParticleTypes.ITEM_SLIME,
                headX, headY, headZ,
                25, 0.5, 0.4, 0.5, 0.15);

        // Outward directional blood sprays (8 directions)
        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI * i) / 12.0;
            for (double r = 0.5; r < 2.5; r += 0.6) {
                double px = headX + Math.cos(angle) * r;
                double pz = headZ + Math.sin(angle) * r;
                level.sendParticles(BLOOD_DARK,
                        px, headY - r * 0.3, pz,
                        2, 0.1, 0.15, 0.1, 0.03);
            }
        }

        // Dripping aftermath
        for (int i = 0; i < 8; i++) {
            double ox = (player.getRandom().nextDouble() - 0.5) * 1.5;
            double oz = (player.getRandom().nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.DRIPPING_LAVA,
                    headX + ox, headY + 0.2, headZ + oz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        // If killed: extra dramatic burst
        if (killed) {
            level.sendParticles(BLOOD_BRIGHT,
                    headX, headY, headZ,
                    60, 1.5, 1.2, 1.5, 0.25);
            level.sendParticles(BLOOD_DARK,
                    headX, headY - 0.5, headZ,
                    80, 2.0, 1.5, 2.0, 0.12);
        }

        // --- Sound: pop + wet splatter ---
        level.playSound(null, headX, target.getY(), headZ,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7F, 1.8F);
        level.playSound(null, headX, target.getY(), headZ,
                SoundEvents.HONEY_BLOCK_BREAK, SoundSource.HOSTILE, 1.2F, 0.4F);
        level.playSound(null, headX, target.getY(), headZ,
                SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    private void resetFocus(UUID uuid, ServerLevel level) {
        FocusData focus = focusMap.remove(uuid);
        if (focus != null) {
            Entity target = level.getEntity(focus.targetId);
            if (target != null) {
                level.sendParticles(ParticleTypes.SMOKE,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                        5, 0.2, 0.2, 0.2, 0.02);
            }
        }
    }

    /**
     * Raycasts to find the LivingEntity the player is looking at.
     */
    private LivingEntity getTargetedEntity(ServerPlayer player, ServerLevel level) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(RANGE));

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(RANGE)).inflate(1.0);
        double closestDist = RANGE;
        LivingEntity closest = null;

        for (Entity entity : level.getEntities(player, searchBox, e -> e instanceof LivingEntity && e.isAlive())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = entityBox.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = (LivingEntity) entity;
                }
            }
        }

        return closest;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            focusMap.remove(player.getUUID());
        }
    }
}
