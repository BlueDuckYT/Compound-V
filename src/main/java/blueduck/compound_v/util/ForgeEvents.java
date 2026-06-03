package blueduck.compound_v.util;

import blueduck.compound_v.CompoundVMod;
import blueduck.compound_v.Config;
import blueduck.compound_v.effect.BerserkerEffect;
import blueduck.compound_v.effect.CompoundVEffect;
import blueduck.compound_v.effect.DensityEffect;
import blueduck.compound_v.effect.ForcefieldEffect;
import blueduck.compound_v.effect.StarPowerEffect;
import blueduck.compound_v.effect.EnhancedRegenEffect;
import blueduck.compound_v.effect.PowerAbsorptionEffect;
import blueduck.compound_v.registry.EffectReg;
import blueduck.compound_v.registry.ItemReg;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.EnderMan;
import blueduck.compound_v.util.S2CLaserSyncPacket;
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
import net.minecraftforge.event.RegisterCommandsEvent;
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
                            if (effect.getEffect() instanceof CompoundVEffect) {
                                effect.setCurativeItems(new java.util.ArrayList<>());
                            }
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
                if (!CompoundVEffect.arePowersSuppressed(player)
                        && !player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            }

            // Active suppression: strip passive buffs while powers are suppressed
            if (CompoundVEffect.arePowersSuppressed(player)) {
                boolean hasAnyCompV = false;
                for (MobEffectInstance inst : player.getActiveEffects()) {
                    if (inst.getEffect() instanceof CompoundVEffect) { hasAnyCompV = true; break; }
                }
                if (hasAnyCompV) {
                    if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                    if (player.hasEffect(EffectReg.SPEEDSTER.get())) {
                        player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
                        player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED);
                        var stepAttr = player.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get());
                        if (stepAttr != null) stepAttr.removeModifier(blueduck.compound_v.effect.SpeedsterEffect.STEP_HEIGHT_UUID);
                    }
                    if (player.hasEffect(EffectReg.NIGHT_VISION.get())) player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                    if (player.hasEffect(EffectReg.DEEP.get())) {
                        player.removeEffect(net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE);
                        player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                        player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED);
                    }
                    if (player.hasEffect(EffectReg.INVISIBILITY.get())) player.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY);
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
        if (CompoundVEffect.arePowersSuppressed(entity)) return;
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
        // No enableMobPowers gate here — injected mobs should always tick.
        // onMobTick already checks for compound_v_powered tag.
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
                && !CompoundVEffect.arePowersSuppressed(attacker)
                && event.getEntity().hasEffect(EffectReg.INVINCIBLE.get())
                && !CompoundVEffect.arePowersSuppressed(event.getEntity())) {

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
                && !CompoundVEffect.arePowersSuppressed(starPlayer)
                && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            event.setAmount(0);
            return;
        }

        // Forcefield damage absorption — must be checked before other damage modifiers
        if (event.getEntity() instanceof Player shieldPlayer
                && shieldPlayer.hasEffect(EffectReg.FORCEFIELD.get())
                && ForcefieldEffect.isActive(shieldPlayer.getUUID())
                && !CompoundVEffect.arePowersSuppressed(shieldPlayer)) {
            float remaining = ForcefieldEffect.absorbDamage(shieldPlayer, event.getAmount());
            if (remaining <= 0) {
                event.setAmount(0);
                // Shield absorb visual
                if (shieldPlayer.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            shieldPlayer.getX(), shieldPlayer.getY() + 1, shieldPlayer.getZ(),
                            5, 0.5, 0.5, 0.5, 0.05);
                    sl.playSound(null, shieldPlayer.getX(), shieldPlayer.getY(), shieldPlayer.getZ(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.4F, 1.5F);
                }
                return;
            } else {
                event.setAmount(remaining);
                // Shield broke!
                if (shieldPlayer.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH,
                            shieldPlayer.getX(), shieldPlayer.getY() + 1, shieldPlayer.getZ(),
                            3, 0, 0, 0, 0);
                    sl.playSound(null, shieldPlayer.getX(), shieldPlayer.getY(), shieldPlayer.getZ(),
                            SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
                }
            }
        }

        // Defensive teleport: passive mobs with Teleport blink away when damaged
        if (event.getEntity() instanceof Mob hurtMob
                && hurtMob.hasEffect(EffectReg.TELEPORT.get())
                && !(hurtMob instanceof net.minecraft.world.entity.monster.Enemy)
                && hurtMob.getTarget() == null
                && event.getSource().getEntity() instanceof LivingEntity dmgSource
                && hurtMob.level() instanceof ServerLevel sl) {
            MobPowerManager.defensiveTeleport(hurtMob, dmgSource, sl);
        }

        List<MobEffectInstance> effects = new ArrayList<>(event.getEntity().getActiveEffects());
        boolean powersSuppressed = CompoundVEffect.arePowersSuppressed(event.getEntity());
        for (MobEffectInstance instance : effects) {

            if (!powersSuppressed && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && instance.getEffect().equals(EffectReg.INVINCIBLE.get())) {
                event.setAmount(0);
            }

            if (!powersSuppressed && event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.CREATIVE_FLIGHT.get())) {
                event.setAmount(0);
            }

            else if (!powersSuppressed && event.getSource().is(DamageTypes.FALL)
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
            // Creative Flight: no fall damage (mobs land after dive attacks)
            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.CREATIVE_FLIGHT.get())) {
                event.setAmount(0);
            }
            // Leap: no fall damage (ground slam handles the impact instead)
            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.LEAP.get())) {
                event.setAmount(0);
            }
            // Stormfront: no fall damage (flight power)
            else if (event.getSource().is(DamageTypes.FALL)
                    && instance.getEffect().equals(EffectReg.STORMFRONT.get())) {
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
            // General Compound V damage reduction — use best tier across all active effects
            else if (!powersSuppressed && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)
                    && instance.getEffect() instanceof CompoundVEffect cvEffect) {
                // Only apply once per entity (first CompoundVEffect match handles it for all)
                if (!event.getEntity().getPersistentData().getBoolean("compound_v_dr_applied")) {
                    event.getEntity().getPersistentData().putBoolean("compound_v_dr_applied", true);
                    if (event.getEntity() instanceof Player) {
                        // Find best DR across all active CompoundV effects
                        double bestDR = 1.0;
                        for (MobEffectInstance inst2 : effects) {
                            if (inst2.getEffect() instanceof CompoundVEffect cv2) {
                                double dr = cv2.getDamageReduction(inst2.getAmplifier());
                                if (dr < bestDR) bestDR = dr;
                            }
                        }
                        event.setAmount((float) (event.getAmount() * bestDR));
                    } else if (event.getEntity() instanceof net.minecraft.world.entity.monster.Enemy) {
                        event.setAmount((float) (event.getAmount() * Config.mobDamageReduction));
                    } else {
                        event.setAmount((float) (event.getAmount() * Config.friendlyMobDamageReduction));
                    }
                }
            }
        }
        // Clean up per-event DR flag so it doesn't persist to next damage event
        event.getEntity().getPersistentData().remove("compound_v_dr_applied");

        // Strength multiplier for players with Compound V (melee only, best tier)
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof Player attacker
                && !CompoundVEffect.arePowersSuppressed(attacker)) {
            double bestSTR = 1.0;
            for (MobEffectInstance instance : new ArrayList<>(attacker.getActiveEffects())) {
                if (instance.getEffect() instanceof CompoundVEffect cvEffect) {
                    double str = cvEffect.getStrengthMultiplier(instance.getAmplifier());
                    if (str > bestSTR) bestSTR = str;
                }
            }
            if (bestSTR > 1.0) event.setAmount((float) (event.getAmount() * bestSTR));

            // Instakill: melee attacks instantly kill the target
            if (attacker.hasEffect(EffectReg.INSTAKILL.get())
                    && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
                event.setAmount(Float.MAX_VALUE);
            }
        }
        // Strength multiplier for mobs with Compound V (melee only, hostile vs friendly)
        else if (event.getSource().is(DamageTypes.MOB_ATTACK)
                && event.getSource().getEntity() instanceof LivingEntity mobAttacker
                && !(mobAttacker instanceof Player)
                && !CompoundVEffect.arePowersSuppressed(mobAttacker)) {
            for (MobEffectInstance instance : new ArrayList<>(mobAttacker.getActiveEffects())) {
                if (instance.getEffect() instanceof CompoundVEffect) {
                    if (mobAttacker instanceof net.minecraft.world.entity.monster.Enemy) {
                        event.setAmount((float) (event.getAmount() * Config.mobStrengthMultiplier));
                    } else {
                        event.setAmount((float) (event.getAmount() * Config.friendlyMobStrengthMultiplier));
                    }
                    break; // only apply once
                }
            }
        }

        // Berserker: damage scales with missing health (melee only)
        if ((event.getSource().is(DamageTypes.PLAYER_ATTACK) || event.getSource().is(DamageTypes.MOB_ATTACK))
                && event.getSource().getEntity() instanceof LivingEntity berserkerAttacker
                && berserkerAttacker.hasEffect(EffectReg.BERSERKER.get())
                && !CompoundVEffect.arePowersSuppressed(berserkerAttacker)) {
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
        // Defensive: reduce knockback taken
        if (!CompoundVEffect.arePowersSuppressed(event.getEntity())) {
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
                        double bestKB = 1.0;
                        for (MobEffectInstance inst2 : effects) {
                            if (inst2.getEffect() instanceof CompoundVEffect cv2) {
                                double kb = cv2.getKnockbackReduction(inst2.getAmplifier());
                                if (kb < bestKB) bestKB = kb;
                            }
                        }
                        event.setStrength((float) (event.getOriginalStrength() * bestKB));
                    } else if (event.getEntity() instanceof net.minecraft.world.entity.monster.Enemy) {
                        event.setStrength((float) (event.getOriginalStrength() * Config.mobKnockbackReduction));
                    } else {
                        event.setStrength((float) (event.getOriginalStrength() * Config.friendlyMobKnockbackReduction));
                    }
                    break; // only apply once (best tier already computed for players)
                }
            }
        }

        // Offensive: amplify knockback dealt by the attacker
        net.minecraft.world.damagesource.DamageSource lastSource = event.getEntity().getLastDamageSource();
        if (lastSource != null && lastSource.getEntity() instanceof LivingEntity attacker
                && !CompoundVEffect.arePowersSuppressed(attacker)) {
            double bestKBD = 1.0;
            for (MobEffectInstance inst : attacker.getActiveEffects()) {
                if (inst.getEffect() instanceof CompoundVEffect cvEffect) {
                    double kbd = cvEffect.getKnockbackDealtMultiplier(inst.getAmplifier());
                    if (kbd > bestKBD) bestKBD = kbd;
                }
            }
            if (bestKBD != 1.0) {
                event.setStrength((float) (event.getStrength() * bestKBD));
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
                && hitEntity.hasEffect(EffectReg.PROJECTILE_IMMUNITY.get())
                && !CompoundVEffect.arePowersSuppressed(hitEntity)) {

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

        // Save Compound V effects to NBT BEFORE they get cleared by death
        if (Config.persistPowersOnDeath && event.getEntity() instanceof Player player) {
            net.minecraft.nbt.ListTag savedEffects = new net.minecraft.nbt.ListTag();
            for (MobEffectInstance inst : player.getActiveEffects()) {
                if (inst.getEffect() instanceof CompoundVEffect)
                    savedEffects.add(inst.save(new net.minecraft.nbt.CompoundTag()));
            }
            if (!savedEffects.isEmpty())
                player.getPersistentData().put("compound_v_saved_effects", savedEffects);
            if (VirusHelper.hasVirus(player, true))
                player.getPersistentData().putBoolean("compound_v_had_virus_on_death", true);
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
            Player newPlayer = event.getEntity();

            // Always persist laser color across death (cosmetic, not power-dependent)
            oldPlayer.reviveCaps();
            if (oldPlayer.getPersistentData().contains("compound_v_laser_color")) {
                newPlayer.getPersistentData().putInt("compound_v_laser_color",
                        oldPlayer.getPersistentData().getInt("compound_v_laser_color"));
            }
            if (oldPlayer.getPersistentData().contains("compound_v_adv_laser_color")) {
                newPlayer.getPersistentData().putInt("compound_v_adv_laser_color",
                        oldPlayer.getPersistentData().getInt("compound_v_adv_laser_color"));
            }
            oldPlayer.invalidateCaps();

            // Persist powers through death if config enabled
            if (Config.persistPowersOnDeath) {
                oldPlayer.reviveCaps();
                net.minecraft.nbt.ListTag savedEffects = oldPlayer.getPersistentData()
                        .getList("compound_v_saved_effects", net.minecraft.nbt.Tag.TAG_COMPOUND);
                java.util.List<net.minecraft.world.effect.MobEffect> copiedPowers = new java.util.ArrayList<>();
                for (int i = 0; i < savedEffects.size(); i++) {
                    MobEffectInstance loaded = MobEffectInstance.load(savedEffects.getCompound(i));
                    if (loaded != null && loaded.getEffect() instanceof CompoundVEffect) {
                        MobEffectInstance copy = new MobEffectInstance(loaded.getEffect(), loaded.getDuration(), loaded.getAmplifier(), false, false, false);
                        copy.setCurativeItems(new java.util.ArrayList<>());
                        newPlayer.addEffect(copy);
                        copiedPowers.add(loaded.getEffect());
                    }
                }
                oldPlayer.getPersistentData().remove("compound_v_saved_effects");
                boolean hadVirus = oldPlayer.getPersistentData().getBoolean("compound_v_had_virus_on_death");
                oldPlayer.getPersistentData().remove("compound_v_had_virus_on_death");
                if (Config.virusRemovesPowerOnDeath && !copiedPowers.isEmpty() && hadVirus) {
                    net.minecraft.world.effect.MobEffect toRemove = copiedPowers.get(newPlayer.getRandom().nextInt(copiedPowers.size()));
                    newPlayer.removeEffect(toRemove);
                }
                // Re-grant flight if player has a flight power
                if (newPlayer.hasEffect(EffectReg.CREATIVE_FLIGHT.get())
                        || newPlayer.hasEffect(EffectReg.LASER_EYES_ADVANCED.get())
                        || newPlayer.hasEffect(EffectReg.STORMFRONT.get())) {
                    newPlayer.getAbilities().mayfly = true;
                    newPlayer.onUpdateAbilities();
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

        // Creepers excluded — vanilla creates lingering effect clouds on explosion
        if (target instanceof net.minecraft.world.entity.monster.Creeper) return;

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
        } else {
            // Use MobPowerManager's species-specific pool for power selection
            List<MobPowerManager.WeightedPower> pool = MobPowerManager.getEligiblePowers(target);
            if (!pool.isEmpty()) {
                // Build weighted selection
                int totalWeight = 0;
                for (var wp : pool) totalWeight += wp.weight();
                int roll = event.getLevel().getRandom().nextInt(totalWeight);
                MobPowerManager.WeightedPower chosen = pool.get(pool.size() - 1);
                int cumulative = 0;
                for (var wp : pool) {
                    cumulative += wp.weight();
                    if (roll < cumulative) {
                        chosen = wp;
                        break;
                    }
                }

                // Apply the power
                int amp = chosen.minAmp();
                if (chosen.maxAmp() > chosen.minAmp()) {
                    amp += event.getLevel().getRandom().nextInt(chosen.maxAmp() - chosen.minAmp() + 1);
                }
                if (isV1) {
                    // V1: apply at max registered level
                    int maxLevel = blueduck.compound_v.registry.CompoundVEffectMatrix.getMaxLevel(chosen.power().get());
                    if (maxLevel > 0) amp = maxLevel;
                }
                net.minecraft.world.effect.MobEffectInstance inst = new net.minecraft.world.effect.MobEffectInstance(
                        chosen.power().get(),
                        permanent ? net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION : Config.tempVDuration,
                        amp, false, false, false);
                inst.setCurativeItems(new java.util.ArrayList<>());
                target.addEffect(inst);

                // Handle bonus laser eyes for flight
                if (chosen.power() == EffectReg.CREATIVE_FLIGHT && target instanceof Mob mob2) {
                    float laserRoll = mob2.getRandom().nextFloat();
                    if (laserRoll < 0.35f) {
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                EffectReg.LASER_EYES_BASIC.get(),
                                permanent ? net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION : Config.tempVDuration,
                                0, false, false, false));
                        if (target instanceof Mob m) m.getPersistentData().putInt("compound_v_laser_color",
                                MobPowerManager.rollLaserColor(m));
                    } else if (laserRoll < 0.45f) {
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                EffectReg.LASER_EYES_ADVANCED.get(),
                                permanent ? net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION : Config.tempVDuration,
                                0, false, false, false));
                        if (target instanceof Mob m) m.getPersistentData().putInt("compound_v_laser_color",
                                m instanceof EnderMan ? S2CLaserSyncPacket.COLOR_PURPLE : S2CLaserSyncPacket.COLOR_RED);
                    }
                }
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
                        MobPowerManager.rollLaserColor(mob));
            }
            if (mob.hasEffect(EffectReg.LASER_EYES_ADVANCED.get())
                    && !mob.getPersistentData().contains("compound_v_laser_color")) {
                mob.getPersistentData().putInt("compound_v_laser_color",
                        mob instanceof EnderMan ? S2CLaserSyncPacket.COLOR_PURPLE : S2CLaserSyncPacket.COLOR_RED);
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

    /**
     * Luck power: boost looting level on mob kills.
     */
    @SubscribeEvent
    public static void onLootingLevel(net.minecraftforge.event.entity.living.LootingLevelEvent event) {
        if (event.getDamageSource() != null && event.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            if (CompoundVEffect.arePowersSuppressed(attacker)) return;
            int luckLevel = blueduck.compound_v.effect.LuckEffect.getLuckLevel(attacker);
            if (luckLevel > 0) {
                event.setLootingLevel(event.getLootingLevel() + luckLevel);
            }
        }
    }

    /**
     * Luck power: bonus XP from mining. Fortune handled by LuckFortuneModifier (GLM).
     */
    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !CompoundVEffect.arePowersSuppressed(event.getPlayer())) {
            int luckLevel = blueduck.compound_v.effect.LuckEffect.getLuckLevel(event.getPlayer());
            if (luckLevel > 0) {
                event.setExpToDrop(event.getExpToDrop() + luckLevel); // bonus XP
            }
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

    // === Laser Color Command ===

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            net.minecraft.commands.Commands.literal("lasercolor")
                .then(net.minecraft.commands.Commands.literal("basic")
                    .then(buildColorArg("compound_v_laser_color", "basic")))
                .then(net.minecraft.commands.Commands.literal("advanced")
                    .then(buildColorArg("compound_v_adv_laser_color", "advanced")))
        );
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack, String> buildColorArg(
            String nbtKey, String typeName) {
        return net.minecraft.commands.Commands.argument("color",
                com.mojang.brigadier.arguments.StringArgumentType.word())
            .suggests((ctx, builder) -> {
                for (String s : new String[]{"orange","blue","red","green","purple","yellow","rainbow"})
                    builder.suggest(s);
                return builder.buildFuture();
            })
            .executes(ctx -> {
                if (!(ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
                    ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Must be run by a player"));
                    return 0;
                }
                if (Config.laserColorCommandOpOnly && !ctx.getSource().hasPermission(2)) {
                    ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("This command requires operator permissions"));
                    return 0;
                }
                String colorName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "color");
                int colorIndex = parseColorName(colorName);
                if (colorIndex < 0) {
                    ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal(
                            "Unknown color: " + colorName + ". Valid: orange, blue, red, green, purple, yellow, rainbow"));
                    return 0;
                }
                player.getPersistentData().putInt(nbtKey, colorIndex);
                ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                        "Set " + typeName + " laser color to " + colorName), true);
                return 1;
            })
            .then(net.minecraft.commands.Commands.argument("target",
                    net.minecraft.commands.arguments.EntityArgument.player())
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    net.minecraft.server.level.ServerPlayer target =
                            net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "target");
                    String colorName = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "color");
                    int colorIndex = parseColorName(colorName);
                    if (colorIndex < 0) {
                        ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Unknown color: " + colorName));
                        return 0;
                    }
                    target.getPersistentData().putInt(nbtKey, colorIndex);
                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                            "Set " + target.getName().getString() + "'s " + typeName + " laser color to " + colorName), true);
                    return 1;
                })
            );
    }

    private static int parseColorName(String name) {
        return switch (name.toLowerCase()) {
            case "orange" -> S2CLaserSyncPacket.COLOR_ORANGE;
            case "blue" -> S2CLaserSyncPacket.COLOR_BLUE;
            case "red" -> S2CLaserSyncPacket.COLOR_RED;
            case "green" -> S2CLaserSyncPacket.COLOR_GREEN;
            case "purple" -> S2CLaserSyncPacket.COLOR_PURPLE;
            case "yellow" -> S2CLaserSyncPacket.COLOR_YELLOW;
            case "rainbow" -> S2CLaserSyncPacket.COLOR_RAINBOW;
            default -> -1;
        };
    }
}