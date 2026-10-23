package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fury's signature rule: one rage meter, 0 to 100, drives everything. Taking
 * hits fills it fast and landing blows a little; five seconds out of the
 * fight it cools. Rage makes a Fury bigger, longer-reaching and harder-hitting.
 * At full rage Red Haze takes hold (the Flaw): no eating and no inventory or
 * chests until the rage has cooled to {@link #HAZE_CLEARS}.
 */
public final class FuryRage {
    public static final float MAX = 100.0F;
    /** Rage per point of damage taken, and per blow landed. */
    static final float PER_DAMAGE_TAKEN = 8.0F;
    static final float PER_BLOW = 3.0F;
    /** How long after the last hit rage starts to cool, and how fast (per second). */
    static final int CALM_TICKS = 20 * 5;
    static final float COOL_PER_SECOND = 5.0F;
    static final float HAZE_CLEARS = 60.0F;
    /** At full rage: a quarter bigger, a block and a half more reach, three more melee damage. */
    static final double FULL_SCALE = 0.25;
    static final double FULL_REACH = 1.5;
    static final double FULL_DAMAGE = 3.0;
    static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "fury_rage_scale");
    static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "fury_rage_reach");
    static final ResourceLocation DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "fury_rage_damage");

    record Rage(float value, long lastFight, boolean hazed) {
    }

    private static final Map<UUID, Rage> RAGE = new ConcurrentHashMap<>();

    private FuryRage() {
    }

    static boolean isFury(ServerPlayer player) {
        return KitRules.hasInnate(player, AspectKitCatalog.FURY.innateId());
    }

    public static float rage(ServerPlayer player) {
        Rage rage = RAGE.get(player.getUUID());
        return rage == null ? 0.0F : rage.value();
    }

    public static boolean hazed(ServerPlayer player) {
        Rage rage = RAGE.get(player.getUUID());
        return rage != null && rage.hazed();
    }

    /** The meter for the HUD: none for anyone but a Fury. */
    public static AbilityStatusPayload.Rage status(ServerPlayer player) {
        return isFury(player) ? new AbilityStatusPayload.Rage(rage(player), hazed(player)) : AbilityStatusPayload.Rage.NONE;
    }

    /** Adds rage (or takes it away, if negative), keeping the size and Red Haze in step. */
    static void add(ServerPlayer player, float amount) {
        set(player, rage(player) + amount, amount > 0);
    }

    static void set(ServerPlayer player, float value, boolean fighting) {
        Rage old = RAGE.getOrDefault(player.getUUID(), new Rage(0.0F, 0L, false));
        float clamped = Math.max(0.0F, Math.min(MAX, value));
        boolean haze = KitRules.hasFlaw(player, AspectKitCatalog.FURY.flawEffectId());
        boolean hazed = haze && (clamped >= MAX || (old.hazed() && clamped > HAZE_CLEARS));
        long lastFight = fighting ? player.serverLevel().getGameTime() : old.lastFight();
        RAGE.put(player.getUUID(), new Rage(clamped, lastFight, hazed));
        if (hazed && !old.hazed()) {
            player.displayClientMessage(Component.literal("Red Haze: you see nothing but the fight.").withStyle(ChatFormatting.DARK_RED), true);
            player.closeContainer();
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8F, 1.2F);
        } else if (!hazed && old.hazed()) {
            player.displayClientMessage(Component.literal("The red haze lifts.").withStyle(ChatFormatting.GRAY), true);
        }
        grow(player, clamped / MAX);
        if (Math.round(old.value()) != Math.round(clamped) || hazed != old.hazed()) {
            AbilityService.syncStatus(player);
        }
    }

    /** Rage makes you bigger, reach further and hit harder. */
    static void grow(ServerPlayer player, float share) {
        KitModifiers.keep(player, Attributes.SCALE, SCALE_ID, FULL_SCALE * share);
        KitModifiers.keep(player, Attributes.ENTITY_INTERACTION_RANGE, REACH_ID, FULL_REACH * share);
        KitModifiers.keep(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, FULL_DAMAGE * share);
    }

    /** Hits taken fill the meter fast; blows a Fury lands fill it a little. */
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player && isFury(player)) {
            add(player, event.getNewDamage() * PER_DAMAGE_TAKEN);
            player.serverLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getEyeY() + 0.4, player.getZ(),
                    1, 0.2, 0.1, 0.2, 0.0);
        }
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof ServerPlayer player
                && event.getEntity() != player && isFury(player)) {
            add(player, PER_BLOW);
        }
    }

    /** Once a second: rage cools after the fight, and leaves when the kit does. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        Rage rage = RAGE.get(player.getUUID());
        if (!isFury(player)) {
            if (rage != null) {
                RAGE.remove(player.getUUID());
                grow(player, 0.0F);
            }
            return;
        }
        if (rage != null && rage.value() > 0 && player.serverLevel().getGameTime() - rage.lastFight() >= CALM_TICKS) {
            set(player, rage.value() - COOL_PER_SECOND, false);
        }
        if (rage != null && rage.value() >= MAX * 0.75F) {
            player.serverLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getEyeY() + 0.5, player.getZ(),
                    1, 0.3, 0.1, 0.3, 0.0);
        }
    }

    /** Red Haze: no eating. */
    public static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && hazed(player) && event.getItem().has(DataComponents.FOOD)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("The haze will not let you eat.").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    /** Red Haze: no chests or other containers; the client keeps the inventory shut too. */
    public static void onOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && hazed(player) && event.getContainer() != player.inventoryMenu) {
            player.closeContainer();
            player.displayClientMessage(Component.literal("The haze will not let you look away from the fight.")
                    .withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    static void forget(UUID player) {
        RAGE.remove(player);
    }
}
