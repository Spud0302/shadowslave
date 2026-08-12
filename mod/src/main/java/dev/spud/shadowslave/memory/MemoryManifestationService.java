package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.item.AshCompassMemoryItem;
import dev.spud.shadowslave.item.GlassRoadMemoryItem;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.item.StonewakeShieldMemoryItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** NeoForge/Minecraft execution adapter for soul-stored Memory ownership. Inventory stacks never establish ownership. */
public final class MemoryManifestationService {
    private MemoryManifestationService() {}

    public static ManifestResult summonAshCompass(ServerPlayer player) {
        return summon(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY.get());
    }

    public static ManifestResult dismissAshCompass(ServerPlayer player) {
        return dismiss(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY.get());
    }

    public static ManifestResult summonGlassRoad(ServerPlayer player) {
        return summon(player, GlassRoadMemoryItem.MEMORY_ID, ModItems.GLASS_ROAD_MEMORY.get());
    }

    public static ManifestResult dismissGlassRoad(ServerPlayer player) {
        return dismiss(player, GlassRoadMemoryItem.MEMORY_ID, ModItems.GLASS_ROAD_MEMORY.get());
    }

    public static ManifestResult summonStonewakeShield(ServerPlayer player) {
        return summon(player, StonewakeShieldMemoryItem.MEMORY_ID, ModItems.STONEWAKE_SHIELD_MEMORY.get());
    }

    public static ManifestResult dismissStonewakeShield(ServerPlayer player) {
        return dismiss(player, StonewakeShieldMemoryItem.MEMORY_ID, ModItems.STONEWAKE_SHIELD_MEMORY.get());
    }

    public static void clearAshCompassManifestations(ServerPlayer player) {
        removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.ASH_COMPASS_MEMORY.get());
    }

    public static void clearGlassRoadManifestations(ServerPlayer player) {
        removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.GLASS_ROAD_MEMORY.get());
    }

    public static void clearStonewakeShieldManifestations(ServerPlayer player) {
        removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.STONEWAKE_SHIELD_MEMORY.get());
    }

    static boolean hasAshCompass(Inventory inventory) {
        return hasManifestation(inventory, ModItems.ASH_COMPASS_MEMORY.get());
    }

    static boolean hasStonewakeShield(Inventory inventory) {
        return hasManifestation(inventory, ModItems.STONEWAKE_SHIELD_MEMORY.get());
    }

    private static ManifestResult summon(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Item item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        if (hasManifestation(player.getInventory(), item)) return ManifestResult.ALREADY_SUMMONED;
        if (!player.addItem(new ItemStack(item))) return ManifestResult.INVENTORY_FULL;
        return ManifestResult.SUMMONED;
    }

    private static ManifestResult dismiss(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Item item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        return removeManifestations(player.getInventory(), item) ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    private static boolean hasManifestation(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) return true;
        }
        return false;
    }

    private static boolean removeManifestations(Inventory inventory, Item item) {
        boolean removed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                inventory.setItem(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        return removed;
    }

    public enum ManifestResult { SUMMONED, DISMISSED, NOT_OWNED, ALREADY_SUMMONED, NOT_SUMMONED, INVENTORY_FULL }
}
