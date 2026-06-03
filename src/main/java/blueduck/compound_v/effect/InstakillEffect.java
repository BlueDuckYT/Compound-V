package blueduck.compound_v.effect;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * Instakill power: any mob the player hits with melee is instantly killed.
 *
 * The actual kill logic lives in ForgeEvents.entityHurtEvent — when the
 * attacker has this effect and the damage source is entity_attack (melee),
 * the damage is set to Float.MAX_VALUE.
 *
 * This is a passive effect with no activation key needed.
 */
public class InstakillEffect extends CompoundVEffect {
    public InstakillEffect(MobEffectCategory category) {
        super(category);
    }
}
