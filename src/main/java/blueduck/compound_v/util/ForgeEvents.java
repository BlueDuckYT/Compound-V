package blueduck.compound_v.util;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.BerserkerEffect;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.DensityEffect;
import blueduck.compound_v.effect.StarPowerEffect;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = CompoundVMod.MODID)
public class ForgeEvents {

    public static HashMap<Player, Collection<MobEffectInstance>> effectMap = new HashMap<>();
    public static HashMap<Player, Boolean> wasInEnd = new HashMap<>();
    // Totem of Undying protection: saves CompoundV effects before a lethal hit
    private static final Map<UUID, java.util.List<MobEffectInstance>> totemEffectSave = new ConcurrentHashMap<>();

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

            // Totem of Undying restore: if we saved effects and the player survived, reapply them
            List<MobEffectInstance> savedEffects = totemEffectSave.remove(player.getUUID());
            if (savedEffects != null && player.isAlive()) {
                // Check that CompoundV effects are actually gone (totem cleared them)
                boolean hasCompV = false;
                for (MobEffectInstance inst : player.getActiveEffects()) {
                    if (inst.getEffect() instanceof CompoundVEffect) {
                        hasCompV = true;
                        break;
                    }
                }
                if (!hasCompV) {
                    for (MobEffectInstance saved : savedEffects) {
                        MobEffectInstance restored = new MobEffectInstance(saved.getEffect(),
                                saved.getDuration(), saved.getAmplifier(), false, false, false);
                        restored.setCurativeItems(new ArrayList<>());
                        player.addEffect(restored);
                    }
                }
            }

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

    /**
     * Shrink jump boost: fires at the exact moment of a jump, before physics.
     * Multiplies Y velocity for higher jumps. Works for both players and mobs.
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(EffectReg.SHRINK.get())) return;
        if (!net.minecraftforge.fml.ModList.get().isLoaded("pehkui")) return;
        if (blueduck.compound_v.util.PehkuiHelper.getTargetScale(entity) >= 0.9f) return;

        // Multiply jump velocity — 2.4x gives ~3-4 blocks
        net.minecraft.world.phys.Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, motion.y * 2.4, motion.z);
    }

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
        // === Unstoppable Force vs Immovable Object ===
        // Instakill attacker hits Invincible defender: massive mutual knockback, no damage
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.hasEffect(EffectReg.INSTAKILL.get())
                && event.getEntity().hasEffect(EffectReg.INVINCIBLE.get())) {

            event.setAmount(0);

            // Calculate knockback direction from attacker to defender
            net.minecraft.world.phys.Vec3 knockDir = event.getEntity().position()
                    .subtract(attacker.position()).normalize();
            double knockStrength = 3.0;
            double knockUpward = 0.8;

            // Defender flies backward
            event.getEntity().setDeltaMovement(
                    knockDir.x * knockStrength, knockUpward, knockDir.z * knockStrength);
            event.getEntity().hurtMarked = true;

            // Attacker flies backward (opposite direction)
            attacker.setDeltaMovement(
                    -knockDir.x * knockStrength, knockUpward, -knockDir.z * knockStrength);
            attacker.hurtMarked = true;

            // Shockwave visual and sound
            if (event.getEntity().level() instanceof ServerLevel sl) {
                double midX = (attacker.getX() + event.getEntity().getX()) / 2;
                double midY = (attacker.getY() + event.getEntity().getY()) / 2 + 1.0;
                double midZ = (attacker.getZ() + event.getEntity().getZ()) / 2;

                // Expanding ring of particles
                for (int i = 0; i < 24; i++) {
                    double angle = (2 * Math.PI * i) / 24.0;
                    for (double r = 0.5; r < 3.0; r += 0.8) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                                midX + Math.cos(angle) * r, midY, midZ + Math.sin(angle) * r,
                                1, 0.05, 0.1, 0.05, 0.02);
                    }
                }

                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                        midX, midY, midZ,
                        3, 0.5, 0.3, 0.5, 0.0);
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        midX, midY, midZ,
                        30, 0.8, 0.5, 0.8, 0.2);

                sl.playSound(null, midX, midY, midZ,
                        net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
                sl.playSound(null, midX, midY, midZ,
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.7F);
            }
            return;
        }

        // Star Power: full invulnerability while active (players only)
        if (event.getEntity() instanceof ServerPlayer starPlayer
                && starPlayer.hasEffect(EffectReg.STAR_POWER.get())
                && StarPowerEffect.isStarActive(starPlayer.getUUID())
                && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            event.setAmount(0);
            return;
        }

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
            // Shrink: no fall damage (slow falling handles most, but this catches edge cases)
            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.SHRINK.get())
                    && net.minecraftforge.fml.ModList.get().isLoaded("pehkui")
                    && blueduck.compound_v.util.PehkuiHelper.getTargetScale(event.getEntity()) < 0.9f) {
                event.setAmount(0);
            }
            // Projectile Immunity: negate all projectile damage (safety net for deflection)
            else if (instance.getEffect().equals(EffectReg.PROJECTILE_IMMUNITY.get())
                    && event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) {
                event.setAmount(0);
            }
            // Enlarge: damage reduction while enlarged (works for both players and mobs)
            else if (instance.getEffect().equals(EffectReg.ENLARGE.get())
                    && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && net.minecraftforge.fml.ModList.get().isLoaded("pehkui")
                    && blueduck.compound_v.effect.EnlargeEffect.isEnlarged(event.getEntity())) {
                event.setAmount(event.getAmount() * blueduck.compound_v.effect.EnlargeEffect.ENLARGE_DAMAGE_REDUCTION);
            }
            // General Compound V damage reduction (players use per-effect getter, mobs use config)
            else if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && instance.getEffect() instanceof CompoundVEffect cvEffect) {
                if (event.getEntity() instanceof Player) {
                    event.setAmount((float) (event.getAmount() * cvEffect.getDamageReduction(instance.getAmplifier())));
                } else {
                    event.setAmount((float) (event.getAmount() * Config.mobDamageReduction));
                }
            }
        }

        // Strength multiplier for players with Compound V
        if (event.getSource().getEntity() instanceof Player attacker) {
            for (MobEffectInstance instance : new ArrayList<>(attacker.getActiveEffects())) {
                if (instance.getEffect() instanceof CompoundVEffect cvEffect) {
                    event.setAmount((float) (event.getAmount() * cvEffect.getStrengthMultiplier(instance.getAmplifier())));
                }
            }

            // Instakill: melee attacks instantly kill the target
            if (attacker.hasEffect(EffectReg.INSTAKILL.get())
                    && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                    && !(event.getEntity() instanceof Player)) {
                event.setAmount(Float.MAX_VALUE);
            }
        }
        // Strength multiplier for mobs with Compound V
        else if (event.getSource().getEntity() instanceof LivingEntity mobAttacker
                && !(mobAttacker instanceof Player)) {
            for (MobEffectInstance instance : new ArrayList<>(mobAttacker.getActiveEffects())) {
                if (instance.getEffect() instanceof CompoundVEffect) {
                    event.setAmount((float) (event.getAmount() * Config.mobStrengthMultiplier));
                    break; // only apply once
                }
            }
        }

        // Berserker: damage scales with missing health (works for both players and mobs)
        if (event.getSource().getEntity() instanceof LivingEntity berserkerAttacker
                && berserkerAttacker.hasEffect(EffectReg.BERSERKER.get())) {
            float multiplier = BerserkerEffect.getDamageMultiplier(berserkerAttacker);
            event.setAmount(event.getAmount() * multiplier);

            // Show multiplier on action bar for players when above base damage
            if (berserkerAttacker instanceof ServerPlayer berserkerPlayer && multiplier > 1.05f) {
                berserkerPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                String.format("§c%.1fx damage", multiplier)),
                        true);
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
            else if (instance.getEffect() instanceof CompoundVEffect cvEffect) {
                if (event.getEntity() instanceof Player) {
                    event.setStrength((float) (event.getOriginalStrength() * cvEffect.getKnockbackReduction(instance.getAmplifier())));
                } else {
                    event.setStrength((float) (event.getOriginalStrength() * Config.mobKnockbackReduction));
                }
            }
        }
    }

    /**
     * Save CompoundV effects before a lethal hit so they survive Totem of Undying.
     * The totem calls removeAllEffects() which wipes everything — we snapshot here
     * and restore on the next tick if the player is still alive.
     */
    @SubscribeEvent
    public static void onLivingDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // If this hit would be lethal, save CompoundV effects
        if (event.getAmount() >= player.getHealth()) {
            List<MobEffectInstance> saved = new ArrayList<>();
            for (MobEffectInstance inst : player.getActiveEffects()) {
                if (inst.getEffect() instanceof CompoundVEffect) {
                    saved.add(new MobEffectInstance(inst.getEffect(),
                            inst.getDuration(), inst.getAmplifier(), false, false, false));
                }
            }
            if (!saved.isEmpty()) {
                totemEffectSave.put(player.getUUID(), saved);
            }
        }
    }

    @SubscribeEvent
    public static void projectileHit(ProjectileImpactEvent event) {
        // Projectile Immunity: deflect projectiles that hit entities with this effect
        if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity hitEntity
                && hitEntity.hasEffect(EffectReg.PROJECTILE_IMMUNITY.get())) {

            net.minecraft.world.entity.projectile.Projectile projectile = event.getProjectile();

            // Cancel the impact
            event.setCanceled(true);

            // Calculate reflect direction
            net.minecraft.world.phys.Vec3 reflectDir;
            double speed = projectile.getDeltaMovement().length();
            if (speed < 0.01) speed = 1.0; // safety for near-stationary projectiles

            if (projectile.getOwner() != null && projectile.getOwner().isAlive()) {
                reflectDir = projectile.getOwner().getEyePosition()
                        .subtract(projectile.position()).normalize();
            } else {
                reflectDir = projectile.getDeltaMovement().normalize().reverse();
            }

            // Set velocity
            projectile.setDeltaMovement(reflectDir.scale(speed));

            // Fireballs (AbstractHurtingProjectile) have xPower/yPower/zPower acceleration
            // fields that override deltaMovement. Must set those too or the fireball
            // curves back. Also change owner so reflected fireball damages the original shooter.
            if (projectile instanceof net.minecraft.world.entity.projectile.AbstractHurtingProjectile fireball) {
                fireball.xPower = reflectDir.x * 0.1;
                fireball.yPower = reflectDir.y * 0.1;
                fireball.zPower = reflectDir.z * 0.1;
                fireball.setOwner(hitEntity);
            }

            projectile.hurtMarked = true;

            // Visual/audio feedback
            if (hitEntity.level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        8, 0.2, 0.2, 0.2, 0.1);
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        projectile.getX(), projectile.getY(), projectile.getZ(),
                        5, 0.15, 0.15, 0.15, 0.05);
                sl.playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                        net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, net.minecraft.sounds.SoundSource.PLAYERS,
                        0.8F, 1.5F);
            }
            return;
        }

        // Charging: explode on projectile impact
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

        // Powered mob V drops (challenge mode feature) — priority: V1 > Compound V > Temp V
        if (event.getEntity() instanceof Mob mob
                && mob.getPersistentData().getBoolean("compound_v_natural")
                && !event.getEntity().level().isClientSide()) {
            double roll = mob.getRandom().nextDouble();
            if (Config.mobV1DropChance > 0 && roll < Config.mobV1DropChance) {
                mob.spawnAtLocation(new ItemStack(ItemReg.V1.get()));
            } else if (Config.mobCompoundVDropChance > 0 && roll < Config.mobCompoundVDropChance) {
                mob.spawnAtLocation(new ItemStack(ItemReg.COMPOUND_V.get()));
            } else if (Config.mobTempVDropChance > 0 && roll < Config.mobTempVDropChance) {
                mob.spawnAtLocation(new ItemStack(ItemReg.TEMP_V.get()));
            }
        }
    }

    /**
     * Handle effect persistence across death/respawn and End dimension.
     *
     * If persistPowersOnDeath is enabled, Compound V effects are saved from
     * the old player entity and reapplied to the new one on respawn.
     * End dimension effect data is always cleared on death regardless.
     */
    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();

            // Persist powers through death if config enabled
            if (Config.persistPowersOnDeath) {
                oldPlayer.reviveCaps(); // required to access capabilities on dead entity
                Player newPlayer = event.getEntity();

                for (MobEffectInstance inst : oldPlayer.getActiveEffects()) {
                    if (inst.getEffect() instanceof CompoundVEffect) {
                        MobEffectInstance copy = new MobEffectInstance(
                                inst.getEffect(), inst.getDuration(), inst.getAmplifier(),
                                false, false, false);
                        copy.setCurativeItems(new java.util.ArrayList<>());
                        newPlayer.addEffect(copy);
                    }
                }
                oldPlayer.invalidateCaps();
            }

            // Always clear End dimension tracking and totem save on death
            effectMap.remove(oldPlayer);
            wasInEnd.remove(oldPlayer);
            totemEffectSave.remove(oldPlayer.getUUID());
        }
    }

    /**
     * Right-click a mob with Compound V or Temp V to inject them.
     * Uses the mob-specific power pool (only powers that function on non-players).
     * Respects bad outcome chance. Sets MobPowerManager NBT tags for tick visuals.
     */
    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (target instanceof Player) return; // Don't inject players this way

        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;
        ItemStack held = player.getItemInHand(event.getHand());

        boolean isCompoundV = held.getItem() == ItemReg.COMPOUND_V.get();
        boolean isTempV = held.getItem() == ItemReg.TEMP_V.get();
        boolean isV1 = held.getItem() == ItemReg.V1.get();
        boolean isAntiV = held.getItem() == ItemReg.ANTI_V.get();

        // === Anti-V: depower a mob ===
        if (isAntiV) {
            boolean hadEffects = false;
            for (net.minecraft.world.effect.MobEffectInstance inst : new java.util.ArrayList<>(target.getActiveEffects())) {
                if (inst.getEffect() instanceof blueduck.compound_v.effect.CompoundVEffect
                        || inst.getEffect() instanceof blueduck.compound_v.effect.negative.BadCompoundVEffect) {
                    target.removeEffect(inst.getEffect());
                    hadEffects = true;
                }
            }
            if (hadEffects) {
                // Reset Pehkui scale
                if (net.minecraftforge.fml.ModList.get().isLoaded("pehkui")) {
                    blueduck.compound_v.util.PehkuiHelper.resetScale(target);
                }
                // Clear MobPowerManager NBT
                if (target instanceof Mob mob) {
                    mob.getPersistentData().remove("compound_v_powered");
                    mob.getPersistentData().remove("compound_v_checked");
                    mob.getPersistentData().remove("compound_v_natural");
                    mob.getPersistentData().remove("compound_v_laser_color");
                    mob.getPersistentData().remove("compound_v_shrink_stealth");
                    mob.getPersistentData().remove("compound_v_shrink_retreating");
                }
                if (event.getLevel() instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            15, 0.3, 0.4, 0.3, 0.05);
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                            net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
                }
                if (!player.getAbilities().instabuild) held.shrink(1);
                event.setCanceled(true);
            }
            return;
        }

        if (!isCompoundV && !isTempV && !isV1) return;

        // Check if mob already has a Compound V effect
        for (net.minecraft.world.effect.MobEffectInstance inst : target.getActiveEffects()) {
            if (inst.getEffect() instanceof blueduck.compound_v.effect.CompoundVEffect
                    || inst.getEffect() instanceof blueduck.compound_v.effect.negative.BadCompoundVEffect) {
                return;
            }
        }

        // Roll for bad outcome (V1 never has bad outcomes)
        double badChance = isV1 ? 0 : (isCompoundV ? Config.badOutcomeChance : Config.tempVBadOutcomeChance);
        boolean isBad = event.getLevel().getRandom().nextDouble() < badChance;

        boolean permanent = isCompoundV || isV1;

        if (isBad && !blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_FAILURE_MATRIX.isEmpty()) {
            blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_FAILURE_MATRIX.get(
                    event.getLevel().getRandom().nextInt(
                            blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_FAILURE_MATRIX.size()
                    )).apply(target, permanent);
        } else if (!blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_EFFECT_MATRIX.isEmpty()) {
            if (isV1) {
                // V1: max level
                blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_EFFECT_MATRIX.get(
                        event.getLevel().getRandom().nextInt(
                                blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_EFFECT_MATRIX.size()
                        )).applyMaxLevel(target);
            } else {
                blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_EFFECT_MATRIX.get(
                        event.getLevel().getRandom().nextInt(
                                blueduck.compound_v.registry.CompoundVEffectMatrix.MOB_EFFECT_MATRIX.size()
                        )).apply(target, permanent);
            }
        }

        // Set MobPowerManager NBT tags so the mob gets blue sparkle visuals and active AI
        if (target instanceof Mob mob) {
            mob.getPersistentData().putBoolean("compound_v_checked", true);
            mob.getPersistentData().putBoolean("compound_v_powered", true);

            // Roll laser color if the mob got laser eyes
            if (mob.hasEffect(EffectReg.LASER_EYES_BASIC.get())
                    && !mob.getPersistentData().contains("compound_v_laser_color")) {
                mob.getPersistentData().putInt("compound_v_laser_color",
                        event.getLevel().getRandom().nextInt(6));
            }
        }

        // Consume item (unless creative)
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        // Visual/audio feedback
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1);
            sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                    net.minecraft.sounds.SoundEvents.WITCH_DRINK,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        event.setCanceled(true);
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
        // V1 — rare find in ancient cities and end cities
        if (Config.addV1ToAncientCities && event.getName().equals(new ResourceLocation("minecraft:chests/ancient_city"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/v1"), 2, 0));
            }
        }
        if (Config.addV1ToEndCities && event.getName().equals(new ResourceLocation("minecraft:chests/end_city_treasure"))) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                addEntry(pool, getInjectEntry(new ResourceLocation("compound_v", "chests/v1"), 3, 0));
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