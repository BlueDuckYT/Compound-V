package blueduck.compound_v.util;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.registry.EffectReg;
import blueduck.compound_v.registry.ItemReg;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.enchantment.UntouchingEnchantment;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
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
                    if (effects.size() > 0) {
                        for (int i = 0; i < effects.size(); i++) {
                            player.addEffect((MobEffectInstance) effects.toArray()[i]);
                        }
                    }
                }
                else {
                    effectMap.put(player, player.getActiveEffects());
                }
            }
            wasInEnd.put(player, player.level().dimension().location().equals(new ResourceLocation("the_end")));

        }


    }

    @SubscribeEvent
    public static void entityHurtEvent(LivingHurtEvent event) {
       for (int i = 0; i < event.getEntity().getActiveEffects().size(); i++) {
           if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) && ((MobEffectInstance)event.getEntity().getActiveEffects().toArray()[i]).getEffect().equals(EffectReg.INVINCIBLE.get())) {
               event.setAmount(0);
           }
           if (event.getSource().is(DamageTypes.FALL) && ((MobEffectInstance)event.getEntity().getActiveEffects().toArray()[i]).getEffect().equals(EffectReg.LEVITATION.get())) {
               event.setAmount(0);
           }
           else if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD) && ((MobEffectInstance)event.getEntity().getActiveEffects().toArray()[i]).getEffect() instanceof CompoundVEffect) {
               event.setAmount((float) (event.getAmount() * Config.damageReduction));
           }
       }
       if (event.getSource().getEntity() instanceof LivingEntity) {
           for (int i = 0; i < ((LivingEntity) event.getSource().getEntity()).getActiveEffects().size(); i++) {
               if (((MobEffectInstance)((LivingEntity) event.getSource().getEntity()).getActiveEffects().toArray()[i]).getEffect() instanceof CompoundVEffect) {
                   event.setAmount((float) (event.getAmount() * Config.strengthMultiplier));
               }
           }
       }
    }

    @SubscribeEvent
    public static void entityKnockbackEvent(LivingKnockBackEvent event) {
        for (int i = 0; i < event.getEntity().getActiveEffects().size(); i++) {
            if (((MobEffectInstance)event.getEntity().getActiveEffects().toArray()[i]).getEffect().equals(EffectReg.INVINCIBLE.get())) {
                event.setStrength(0);
            }
            else if (((MobEffectInstance)event.getEntity().getActiveEffects().toArray()[i]).getEffect() instanceof CompoundVEffect) {
                event.setStrength((float) (event.getOriginalStrength() * Config.knockbackReduction));
            }
        }
    }

    @SubscribeEvent
    public static void projectileHit(ProjectileImpactEvent event) {
        LivingEntity entity = (LivingEntity) event.getProjectile().getOwner();
        if (entity == null) {
            return;
        }
        if (entity.hasEffect(EffectReg.CHARGING.get()) && event.getEntity().level() instanceof ServerLevel) {
            event.getProjectile().level().explode(entity, event.getProjectile().getBlockX(), event.getProjectile().getBlockY(), event.getProjectile().getBlockZ(), (float) (entity.getEffect(EffectReg.CHARGING.get()).getAmplifier() * 1.5), Level.ExplosionInteraction.MOB);
        }
    }

    @SubscribeEvent
    public static void entityDieEvent(LivingDeathEvent event) {

        if (event.getEntity().hasEffect(EffectReg.CHARGING.get()) && event.getEntity().level() instanceof ServerLevel) {
            event.getEntity().level().explode(event.getEntity(), event.getEntity().getBlockX(), event.getEntity().getBlockY(), event.getEntity().getBlockZ(), (float) (event.getEntity().getEffect(EffectReg.CHARGING.get()).getAmplifier() * 2.25), Level.ExplosionInteraction.MOB);
        }
    }

    @SubscribeEvent
    public static void WandererTradesSetup(WandererTradesEvent event) {

        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
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
        }
        else if (Config.addVToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/compound_v"), 2, 0));
            }
        }
        else if (Config.addTempVToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
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