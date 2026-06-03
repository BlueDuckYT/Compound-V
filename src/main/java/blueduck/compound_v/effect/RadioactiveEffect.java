package blueduck.compound_v.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Radioactive power (positive, toggleable). Requires Alex's Caves.
 *
 * Press V to toggle your radioactive aura on/off.
 *
 * While active:
 * - You receive Irradiated I (200 ticks / 10 sec) — gives the green glow
 *   visual and suppresses natural regen, but doesn't deal damage.
 * - Nearby living entities receive escalating Irradiated levels based on
 *   proximity:
 *     12-8 blocks: Irradiated I   (regen suppression only)
 *     8-4 blocks:  Irradiated II  (slow damage)
 *     0-4 blocks:  Irradiated III (faster damage)
 * - Duration applied is 300 ticks (15 sec) so it lingers after leaving range.
 * - Affects EVERYTHING — mobs, villagers, animals, other players.
 *
 * If Alex's Caves is not installed, the effect does nothing and logs a
 * warning on first toggle.
 */
public class RadioactiveEffect extends CompoundVEffect {

    private static final double OUTER_RANGE = 12.0;
    private static final double MID_RANGE = 8.0;
    private static final double INNER_RANGE = 4.0;
    private static final int IRRADIATE_DURATION = 300; // 15 seconds

    private static final Set<UUID> activeAuras = new HashSet<>();

    // Cached irradiated effect reference — null until first lookup
    private static MobEffect cachedIrradiated = null;
    private static boolean lookupAttempted = false;

    public RadioactiveEffect(MobEffectCategory category) {
        super(category);
    }

    public static boolean isAuraActive(UUID uuid) {
        return activeAuras.contains(uuid);
    }

    /**
     * Looks up Alex's Caves' Irradiated MobEffect from the registry.
     * Returns null if AC is not loaded or the effect can't be found.
     */
    public static MobEffect getIrradiatedEffect() {
        if (!lookupAttempted) {
            lookupAttempted = true;
            if (ModList.get().isLoaded("alexscaves")) {
                cachedIrradiated = ForgeRegistries.MOB_EFFECTS.getValue(
                        new ResourceLocation("alexscaves", "irradiated"));
                if (cachedIrradiated == null) {
                    com.mojang.logging.LogUtils.getLogger().warn(
                            "Compound V: Alex's Caves loaded but 'irradiated' effect not found in registry");
                }
            }
        }
        return cachedIrradiated;
    }

    @Override
    public void activate(ServerPlayer player, int amplifier, ServerLevel level) {
        UUID uuid = player.getUUID();

        if (getIrradiatedEffect() == null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7Radioactive requires Alex's Caves"),
                    true);
            return;
        }

        if (activeAuras.contains(uuid)) {
            activeAuras.remove(uuid);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Radioactive aura: OFF"),
                    true);
        } else {
            activeAuras.add(uuid);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Radioactive aura: ON"),
                    true);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffect irradiated = getIrradiatedEffect();
        if (irradiated == null) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        // Mobs: always irradiated (visual glow), aura handled by MobPowerManager
        if (!(entity instanceof ServerPlayer player)) {
            entity.addEffect(new MobEffectInstance(irradiated, IRRADIATE_DURATION, 0, false, false, false));
            return;
        }

        // Players: only when toggled on
        if (!activeAuras.contains(player.getUUID())) return;

        // Apply Irradiated I to self (visual glow + regen suppression, no damage)
        player.addEffect(new MobEffectInstance(irradiated, IRRADIATE_DURATION, 0, false, false, true));

        // Irradiate nearby entities based on distance
        AABB auraBox = player.getBoundingBox().inflate(OUTER_RANGE);
        for (Entity e : level.getEntities(player, auraBox,
                ent -> ent instanceof LivingEntity && ent.isAlive() && ent != player)) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.distanceTo(player);

            int irradiatedLevel;
            if (dist <= INNER_RANGE) {
                irradiatedLevel = 2; // Irradiated III
            } else if (dist <= MID_RANGE) {
                irradiatedLevel = 1; // Irradiated II
            } else if (dist <= OUTER_RANGE) {
                irradiatedLevel = 0; // Irradiated I
            } else {
                continue;
            }

            target.addEffect(new MobEffectInstance(irradiated, IRRADIATE_DURATION, irradiatedLevel, false, false, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int tick, int amplifier) {
        // Tick every 20 ticks (once per second) — no need to re-apply every tick
        return tick % 20 == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Player player) {
            activeAuras.remove(player.getUUID());
        }
    }
}
