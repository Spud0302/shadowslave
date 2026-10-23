package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anchor's signature rule: weight tiers. Light jumps further, falls softly and
 * runs a little faster. Heavy shrugs off knockback, sinks, walks slower and
 * digs underwater as fast as on land. Leaden (the Flaw) keeps boats, horses
 * and ladders from bearing you at any weight; the ladder half runs on the
 * client, since climbing is client-driven.
 */
public final class AnchorWeight {
    public enum Weight {
        NORMAL("Normal"), HEAVY("Heavy"), LIGHT("Light");

        final String label;

        Weight(String label) {
            this.label = label;
        }

        Weight next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    static final ResourceLocation GRAVITY_ID = id("gravity");
    static final ResourceLocation JUMP_ID = id("jump");
    static final ResourceLocation SAFE_FALL_ID = id("safe_fall");
    static final ResourceLocation SPEED_ID = id("speed");
    static final ResourceLocation KNOCKBACK_ID = id("knockback");
    static final ResourceLocation SUBMERGED_MINING_ID = id("submerged_mining");
    static final ResourceLocation WATER_MOVEMENT_ID = id("water_movement");

    private static final Map<UUID, Weight> WEIGHTS = new ConcurrentHashMap<>();

    private AnchorWeight() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", "anchor_weight_" + path);
    }

    public static Weight weight(ServerPlayer player) {
        return WEIGHTS.getOrDefault(player.getUUID(), Weight.NORMAL);
    }

    static void set(ServerPlayer player, Weight weight) {
        WEIGHTS.put(player.getUUID(), weight);
        apply(player, weight);
    }

    /** Puts the attribute modifiers for a weight on the player, taking the others off. */
    static void apply(ServerPlayer player, Weight weight) {
        boolean light = weight == Weight.LIGHT;
        boolean heavy = weight == Weight.HEAVY;
        AttributeModifier.Operation multiply = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
        KitModifiers.keep(player, Attributes.GRAVITY, GRAVITY_ID, light ? -0.5 : heavy ? 0.5 : 0.0, multiply);
        KitModifiers.keep(player, Attributes.JUMP_STRENGTH, JUMP_ID, light ? 0.2 : 0.0);
        KitModifiers.keep(player, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_ID, light ? 6.0 : 0.0);
        KitModifiers.keep(player, Attributes.MOVEMENT_SPEED, SPEED_ID, light ? 0.1 : heavy ? -0.15 : 0.0, multiply);
        KitModifiers.keep(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID, heavy ? 1.0 : 0.0);
        KitModifiers.keep(player, Attributes.SUBMERGED_MINING_SPEED, SUBMERGED_MINING_ID, heavy ? 0.8 : 0.0);
        KitModifiers.keep(player, Attributes.WATER_MOVEMENT_EFFICIENCY, WATER_MOVEMENT_ID, heavy ? 0.5 : 0.0);
    }

    /** Once a second: keep the weight's modifiers on an Anchor, and take them off anyone else. */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        if (KitRules.hasInnate(player, AspectKitCatalog.ANCHOR.innateId())) {
            apply(player, weight(player));
        } else if (WEIGHTS.remove(player.getUUID()) != null || player.tickCount % 200 == 0) {
            apply(player, Weight.NORMAL);
        }
    }

    /** Leaden: boats and horses will not bear you. */
    public static void onMount(EntityMountEvent event) {
        if (event.isMounting() && event.getEntityMounting() instanceof ServerPlayer player
                && (event.getEntityBeingMounted() instanceof Boat || event.getEntityBeingMounted() instanceof AbstractHorse)
                && KitRules.hasFlaw(player, AspectKitCatalog.ANCHOR.flawEffectId())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal(event.getEntityBeingMounted() instanceof Boat
                    ? "The boat dips under you and will not bear your weight." : "It shies away from your weight.")
                    .withStyle(ChatFormatting.GRAY), true);
        }
    }

    public static void forget(UUID player) {
        WEIGHTS.remove(player);
    }
}
