package blueduck.compound_v.util;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.DensityEffect;
import blueduck.compound_v.effect.EnhancedRegenEffect;
import blueduck.compound_v.effect.PowerAbsorptionEffect;
import blueduck.compound_v.registry.EffectReg;
import blueduck.compound_v.registry.ItemReg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID)
public class ForgeEvents {

    public static HashMap<Player, Collection<MobEffectInstance>> effectMap = new HashMap<>();
    public static HashMap<Player, Boolean> wasInEnd = new HashMap<>();

    @SubscribeEvent
    public static void playerTickEvent(LivingEvent.LivingTickEvent event) {
        Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;

        if (player instanceof ServerPlayer) {
            if (!wasInEnd.containsKey(player)) {
                wasInEnd.put(player, player.level().dimension().location().equals(new ResourceLocation("the_end")));
            }
            if (wasInEnd.get(player)) {
                if (!player.level().dimension().location().equals(new ResourceLocation("the_end"))) {
                    Collection<MobEffectInstance> effects = effectMap.get(player);
                    if (effects != null && effects.size() > 0) {
                        for (MobEffectInstance effect : effects) {
                            player.addEffect(effect);
                        }
                    }
                } else {
                    effectMap.put(player, new ArrayList<>(player.getActiveEffects()));
                }
            }
            wasInEnd.put(player, player.level().dimension().location().equals(new ResourceLocation("the_end")));

            if (player.hasEffect(EffectReg.CREATIVE_FLIGHT.get()) ||
                    player.hasEffect(EffectReg.LASER_EYES_ADVANCED.get())) {
                if (!player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            }
        }
    }

    // --- Mob Power Events ---

    @SubscribeEvent
    public static void entityJoinLevel(EntityJoinLevelEvent event) {
        if (!Config.enableMobPowers) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel level)) return;
        MobPowerManager.onMobJoinLevel(mob, level);
    }

    @SubscribeEvent
    public static void mobPowerTick(LivingEvent.LivingTickEvent event) {
        if (!Config.enableMobPowers) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel level)) return;
        MobPowerManager.onMobTick(mob, level);
    }

    @SubscribeEvent
    public static void entityHurtEvent(LivingHurtEvent event) {
        List<MobEffectInstance> effects = new ArrayList<>(event.getEntity().getActiveEffects());
        for (MobEffectInstance instance : effects) {

            if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && instance.getEffect().equals(EffectReg.INVINCIBLE.get())) {
                event.setAmount(0);
            }

            if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.CREATIVE_FLIGHT.get())) {
                event.setAmount(0);
            }

            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.LASER_EYES_ADVANCED.get())) {
                event.setAmount(0);
            }

            else if (instance.getEffect().equals(EffectReg.DENSITY.get())
                    && event.getEntity() instanceof Player p
                    && DensityEffect.isDense(p.getUUID())) {
                if (event.getSource().is(DamageTypes.FALL)) {
                    event.setAmount(0);
                }
                else {
                    event.setAmount((float) (event.getAmount() * Config.densityDamageMultiplier));
                }
            }

            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.SPIDER.get())) {
                event.setAmount(0);
            }
            // General Compound V damage reduction (players only — mobs don't get this passive buff)
            else if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && event.getEntity() instanceof Player
                    && instance.getEffect() instanceof CompoundVEffect) {
                event.setAmount((float) (event.getAmount() * Config.damageReduction));
            }
        }

        // Strength multiplier for players with Compound V (mobs don't get this passive buff)
        if (event.getSource().getEntity() instanceof Player attacker) {
            for (MobEffectInstance instance : new ArrayList<>(attacker.getActiveEffects())) {
                if (instance.getEffect() instanceof CompoundVEffect) {
                    event.setAmount((float) (event.getAmount() * Config.strengthMultiplier));
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer player
                && player.hasEffect(EffectReg.POWER_ABSORPTION.get())
                && event.getAmount() > 0) {
            PowerAbsorptionEffect.addCharge(player.getUUID(), event.getAmount());

            if (event.getSource().is(DamageTypes.FALL)) {
                event.setAmount(0);
            }
        }

        if (event.getEntity() instanceof ServerPlayer player2
                && player2.hasEffect(EffectReg.ENHANCED_REGEN.get())
                && event.getAmount() > 0) {
            EnhancedRegenEffect.onPlayerDamaged(player2.getUUID(), player2.serverLevel().getGameTime());
        }

        // Track damage for powered mob regen
        if (Config.enableMobPowers
                && event.getEntity() instanceof Mob mob
                && mob.hasEffect(EffectReg.ENHANCED_REGEN.get())
                && mob.level() instanceof ServerLevel sl
                && event.getAmount() > 0) {
            MobPowerManager.onMobDamaged(mob.getUUID(), sl.getGameTime());
        }
    }

    @SubscribeEvent
    public static void entityKnockbackEvent(LivingKnockBackEvent event) {
        List<MobEffectInstance> effects = new ArrayList<>(event.getEntity().getActiveEffects());
        for (MobEffectInstance instance : effects) {
            if (instance.getEffect().equals(EffectReg.INVINCIBLE.get())) {
                event.setStrength(0);
            }
            // Density: full knockback negation when dense
            else if (instance.getEffect().equals(EffectReg.DENSITY.get())
                    && event.getEntity() instanceof Player p
                    && DensityEffect.isDense(p.getUUID())) {
                event.setStrength(0);
            }
            else if (instance.getEffect() instanceof CompoundVEffect
                    && event.getEntity() instanceof Player) {
                event.setStrength((float) (event.getOriginalStrength() * Config.knockbackReduction));
            }
        }
    }

    @SubscribeEvent
    public static void projectileHit(ProjectileImpactEvent event) {
        if (!(event.getProjectile().getOwner() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.hasEffect(EffectReg.CHARGING.get()) && event.getEntity().level() instanceof ServerLevel) {
            event.getProjectile().level().explode(entity,
                    event.getProjectile().getBlockX(), event.getProjectile().getBlockY(), event.getProjectile().getBlockZ(),
                    (float) (entity.getEffect(EffectReg.CHARGING.get()).getAmplifier() * 1.5),
                    Level.ExplosionInteraction.MOB);
        }
    }

    @SubscribeEvent
    public static void entityDieEvent(LivingDeathEvent event) {
        if (event.getEntity().hasEffect(EffectReg.CHARGING.get()) && event.getEntity().level() instanceof ServerLevel) {
            event.getEntity().level().explode(event.getEntity(),
                    event.getEntity().getBlockX(), event.getEntity().getBlockY(), event.getEntity().getBlockZ(),
                    (float) (event.getEntity().getEffect(EffectReg.CHARGING.get()).getAmplifier() * 2.25),
                    Level.ExplosionInteraction.MOB);
        }
    }

    @SubscribeEvent
    public static void WandererTradesSetup(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
        if (Config.tempVFromTrader) {
            rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 64),
                    new ItemStack(ItemReg.TEMP_V.get(), 1),
                    1, 14, .2f));
        }
    }

    @SubscribeEvent
    public static void lootLoad(LootTableLoadEvent event) {
        if (Config.addToBuriedTreasure && event.getName().equals(new ResourceLocation("minecraft:chests/buried_treasure"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/temp_v"), 15, 0));
            }
        }
        if (Config.addVToAncientCities && Config.addTempVToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/compound_and_temp_v"), 8, 0));
            }
        } else if (Config.addVToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/compound_v"), 2, 0));
            }
        } else if (Config.addTempVToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/temp_v"), 6, 0));
            }
        }
        if (Config.addToBastions && event.getName().equals(new ResourceLocation("minecraft:chests/bastion_treasure"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/temp_v"), 15, 0));
            }
        }
        if (Config.addToBastions && event.getName().equals(new ResourceLocation("minecraft:chests/bastion_other"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/temp_v"), 5, 0));
            }
        }
        if (Config.addToEndCities && event.getName().equals(new ResourceLocation("minecraft:chests/end_city_treasure"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/compound_and_temp_v"), 8, 0));
            }
        }
    }

    private static LootPoolEntryContainer getInjectEntry(ResourceLocation location, int weight, int quality) {
        return LootTableReference.lootTableReference(location).setWeight(weight).setQuality(quality).build();
    }

    private static void addEntry(LootPool pool, LootPoolEntryContainer entry) {
        ArrayList<LootPoolEntryContainer> lootPoolEntriesArray = new ArrayList<>(List.of(pool.entries));
        ArrayList<LootPoolEntryContainer> newLootEntries = new ArrayList<>(lootPoolEntriesArray);
        newLootEntries.add(entry);
        pool.entries = newLootEntries.toArray(new LootPoolEntryContainer[]{});
    }
}