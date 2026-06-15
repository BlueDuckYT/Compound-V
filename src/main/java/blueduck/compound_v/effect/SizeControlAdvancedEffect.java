package blueduck.compound_v.effect;

import blueduck.compound_v.Config;
import blueduck.compound_v.util.PehkuiHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

/**
 * Size Control (Advanced) — scroll the wheel while holding V to grow or shrink
 * smoothly. Stats scale continuously with size:
 *   smaller  -> faster, lighter (more knockback taken), no fall damage, weaker
 *   larger   -> stronger, slower, knockback-resistant, slower attack speed
 *
 * Uses the generic scroll framework (usesScroll/scrollAdjust). All size-derived stat
 * modifiers are recomputed each tick from the current Pehkui scale, so there is no
 * discrete tier — it is a smooth curve around the player's current size.
 */
public class SizeControlAdvancedEffect extends CompoundVEffect {

    private static final UUID SC_SPEED = UUID.fromString("5121e00a-0001-4000-8000-000000000001");
    private static final UUID SC_ATTACK = UUID.fromString("5121e00a-0001-4000-8000-000000000002");
    private static final UUID SC_ATTACK_SPEED = UUID.fromString("5121e00a-0001-4000-8000-000000000003");
    private static final UUID SC_KBR = UUID.fromString("5121e00a-0001-4000-8000-000000000004");
    private static final UUID SC_STEP = UUID.fromString("5121e00a-0001-4000-8000-000000000005");
    private static final UUID SC_BLOCK_REACH = UUID.fromString("5121e00a-0001-4000-8000-000000000006");
    private static final UUID SC_ENTITY_REACH = UUID.fromString("5121e00a-0001-4000-8000-000000000007");

    // Per-player desired scale. Scroll nudges this target; an explicit current value
    // (currentScale) eases toward it each tick, and BOTH the Pehkui visual scale and the
    // size-derived stats are driven from that one eased value — so fast scrolling reads as
    // a single continuous glide and stats never snap ahead of the body.
    private static final java.util.Map<UUID, Float> targetScale = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<UUID, Float> currentScale = new java.util.concurrent.ConcurrentHashMap<>();

    public SizeControlAdvancedEffect(MobEffectCategory category) {
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
     * Damage-reduction multiplier for the current size, congruent with the Enlarge power:
     * 1.0 (no reduction) at scale <= 1.0, reaching Enlarge's 0.6 (40% reduction) at
     * ENLARGE_SCALE (3.0) and holding there for larger sizes. Returns 1.0 when not loaded
     * or not enlarged. Used by ForgeEvents so a big Size Control body is as tanky as Enlarge.
     */
    public static float damageReductionFactor(LivingEntity entity) {
        if (!isPehkuiLoaded()) return 1.0f;
        float s = PehkuiHelper.getCurrentScale(entity);
        if (s <= 1.0f) return 1.0f;
        double enlargeScale = blueduck.compound_v.effect.EnlargeEffect.ENLARGE_SCALE; // 3.0
        double full = 1.0 - blueduck.compound_v.effect.EnlargeEffect.ENLARGE_DAMAGE_REDUCTION; // 0.4
        double t = Math.min(1.0, (s - 1.0) / Math.max(0.0001, enlargeScale - 1.0));
        double reduction = full * t; // 0 -> 0.4
        return (float) (1.0 - reduction); // 1.0 -> 0.6
    }

    /** Scroll-aware whenever the player holds the power key (gated client-side too). */
    @Override
    public boolean usesScroll(ServerPlayer player) {
        return true;
    }

    @Override
    public void scrollAdjust(ServerPlayer player, int amplifier, ServerLevel level, int dir) {
        if (!isPehkuiLoaded()) return;
        UUID uuid = player.getUUID();

        // Accumulate onto the existing target (seed from live scale the first time).
        float cur = targetScale.getOrDefault(uuid, PehkuiHelper.getTargetScale(player));
        float step = (float) Config.sizeControlScrollStep;
        float next = cur + dir * step;
        next = Math.max((float) Config.sizeControlMinScale, Math.min((float) Config.sizeControlMaxScale, next));
        targetScale.put(uuid, next);
        currentScale.putIfAbsent(uuid, PehkuiHelper.getCurrentScale(player));

        // Light cue, throttled so fast scrolling doesn't stutter with particle/sound spam.
        if (player.tickCount % 3 == 0) {
            level.sendParticles(dir > 0 ? ParticleTypes.CLOUD : ParticleTypes.POOF,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ(),
                    3, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    dir > 0 ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT,
                    SoundSource.PLAYERS, 0.3F, dir > 0 ? 0.7F : 1.4F);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
        if (!isPehkuiLoaded()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();

        Float target = targetScale.get(uuid);
        if (target == null) {
            // No active size adjustment — still keep stats in sync with the live scale.
            applyScaledStats(player, PehkuiHelper.getCurrentScale(player));
            return;
        }

        // Ease our tracked current value toward the target by a fixed fraction per tick
        // (exponential approach => smooth, frame-independent, no stair-stepping). Snap when
        // close enough to avoid an endless crawl.
        float now = currentScale.getOrDefault(uuid, PehkuiHelper.getCurrentScale(player));
        float eased = now + (target - now) * 0.35f;
        if (Math.abs(target - eased) < 0.01f) eased = target;
        currentScale.put(uuid, eased);

        // Drive BOTH the visual scale and the stats from the same eased value. A tiny tick
        // delay lets Pehkui smooth between our per-tick steps for extra fluidity.
        PehkuiHelper.setScaleSmooth(player, eased, 2);
        applyScaledStats(player, eased);
    }

    /**
     * Recomputes all size-derived stat modifiers from the current scale, kept CONGRUENT with
     * the dedicated Shrink and Enlarge powers:
     *   scale 1.0                    -> neutral (no modifiers)
     *   scale sizeControlMinScale    -> exactly the Shrink power's values (+300% speed,
     *                                   +0.4 step, jump x2.4 via ForgeEvents)
     *   scale ENLARGE_SCALE (3.0)    -> exactly the Enlarge power's values (-25% speed,
     *                                   +4 damage, -40% attack speed, +0.8 KBR, +3 reach,
     *                                   40% damage reduction via ForgeEvents)
     * Values interpolate proportionally between those anchors, so a player passing through a
     * given size has the same stats the standalone power would grant at that size.
     */
    private static void applyScaledStats(ServerPlayer player, float scale) {
        float s = Math.max(0.1f, Math.min(8.0f, scale));

        // Shrink anchors (kept in sync with ShrinkEffect / ForgeEvents jump).
        final double SHRINK_SPEED = 3.0;   // +300% MULTIPLY_TOTAL
        final double SHRINK_STEP = 0.4;    // +0.4 step height
        // Enlarge anchors (kept in sync with EnlargeEffect).
        final double ENLARGE_SCALE = blueduck.compound_v.effect.EnlargeEffect.ENLARGE_SCALE; // 3.0
        final double ENLARGE_SPEED = -0.25;     // -25% MULTIPLY_TOTAL
        final double ENLARGE_DAMAGE = 4.0;      // +4 ADDITION
        final double ENLARGE_ATK_SPEED = -0.4;  // -40% MULTIPLY_TOTAL
        final double ENLARGE_KBR = 0.8;         // +0.8 ADDITION
        final double ENLARGE_REACH = 3.0;       // +3 block & entity reach

        // Smallness factor: 0 at scale>=1, 1.0 at sizeControlMinScale.
        double minScale = Config.sizeControlMinScale;
        double tSmall = 0.0;
        if (s < 1.0) {
            tSmall = clamp((1.0 - s) / Math.max(0.0001, 1.0 - minScale), 0.0, 1.0);
        }
        // Largeness factor: 0 at scale<=1, 1.0 at ENLARGE_SCALE, and continues past 1.0 above
        // it (so beyond Enlarge size the trends keep going rather than capping early).
        double tLarge = 0.0;
        if (s > 1.0) {
            tLarge = (s - 1.0) / Math.max(0.0001, ENLARGE_SCALE - 1.0);
        }

        // --- Movement speed: Shrink boost when small, Enlarge slow when large ---
        double speedMod;
        if (s < 1.0) {
            speedMod = SHRINK_SPEED * tSmall;
        } else {
            speedMod = clamp(ENLARGE_SPEED * tLarge, -0.6, 0.0); // -25% at 3.0, floored at -60%
        }
        setMod(player, Attributes.MOVEMENT_SPEED, SC_SPEED, "SizeControl speed", speedMod,
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        // --- Attack damage: ramps to Enlarge's +4 at 3.0, keeps climbing past it ---
        double dmgAdd = clamp(ENLARGE_DAMAGE * tLarge, 0.0, 24.0);
        setMod(player, Attributes.ATTACK_DAMAGE, SC_ATTACK, "SizeControl damage", dmgAdd,
                AttributeModifier.Operation.ADDITION);

        // --- Attack speed: ramps to Enlarge's -40% at 3.0 ---
        double atkSpeedMod = clamp(ENLARGE_ATK_SPEED * tLarge, -0.6, 0.0);
        setMod(player, Attributes.ATTACK_SPEED, SC_ATTACK_SPEED, "SizeControl swing", atkSpeedMod,
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        // --- Knockback resistance: ramps to Enlarge's +0.8 at 3.0, capped at full ---
        double kbr = clamp(ENLARGE_KBR * tLarge, 0.0, 1.0);
        setMod(player, Attributes.KNOCKBACK_RESISTANCE, SC_KBR, "SizeControl kbr", kbr,
                AttributeModifier.Operation.ADDITION);

        // --- Step height: ramps to Shrink's +0.4 at min size ---
        double step = SHRINK_STEP * tSmall;
        setMod(player, net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get(), SC_STEP,
                "SizeControl step", step, AttributeModifier.Operation.ADDITION);

        // --- Reach: ramps to Enlarge's +3 at 3.0 (block + entity), matching the big-body feel ---
        double reach = clamp(ENLARGE_REACH * tLarge, 0.0, ENLARGE_REACH * 2.0);
        setMod(player, net.minecraftforge.common.ForgeMod.BLOCK_REACH.get(), SC_BLOCK_REACH,
                "SizeControl block reach", reach, AttributeModifier.Operation.ADDITION);
        setMod(player, net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), SC_ENTITY_REACH,
                "SizeControl entity reach", reach, AttributeModifier.Operation.ADDITION);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static void setMod(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attr,
                               UUID id, String name, double amount, AttributeModifier.Operation op) {
        var inst = player.getAttribute(attr);
        if (inst == null) return;
        var existing = inst.getModifier(id);
        if (existing != null) {
            if (existing.getAmount() == amount) return; // unchanged
            inst.removeModifier(id);
        }
        if (amount != 0.0) {
            inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
        }
    }

    private static void clearStats(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        for (UUID id : new UUID[]{SC_SPEED, SC_ATTACK, SC_ATTACK_SPEED, SC_KBR, SC_STEP,
                SC_BLOCK_REACH, SC_ENTITY_REACH}) {
            removeFrom(player, Attributes.MOVEMENT_SPEED, id);
            removeFrom(player, Attributes.ATTACK_DAMAGE, id);
            removeFrom(player, Attributes.ATTACK_SPEED, id);
            removeFrom(player, Attributes.KNOCKBACK_RESISTANCE, id);
            removeFrom(player, net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get(), id);
            removeFrom(player, net.minecraftforge.common.ForgeMod.BLOCK_REACH.get(), id);
            removeFrom(player, net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), id);
        }
    }

    private static void removeFrom(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id) {
        var inst = player.getAttribute(attr);
        if (inst != null) inst.removeModifier(id);
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        clearStats(entity);
        if (entity != null) {
            targetScale.remove(entity.getUUID());
            currentScale.remove(entity.getUUID());
        }
        if (isPehkuiLoaded()) {
            PehkuiHelper.resetScale(entity);
        }
    }
}
