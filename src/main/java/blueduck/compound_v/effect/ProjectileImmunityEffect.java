package blueduck.compound_v.effect;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * Projectile Immunity (Rubber Body) power: all projectiles bounce off.
 *
 * The actual deflection logic lives in ForgeEvents.projectileHit —
 * when a projectile hits an entity that has this effect, the impact
 * is canceled and the projectile is reflected back toward its owner.
 *
 * Additionally, in ForgeEvents.entityHurtEvent, all projectile-type
 * damage (arrows, tridents, fireballs, etc.) is set to zero as a
 * safety net for any projectile that bypasses the impact event.
 *
 * This is a passive effect with no activation key needed.
 * Works for both players and mobs.
 */
public class ProjectileImmunityEffect extends CompoundVEffect {
    public ProjectileImmunityEffect(MobEffectCategory category) {
        super(category);
    }

}
