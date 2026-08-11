package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.item.AshCompassMemoryItem;
import dev.spud.shadowslave.item.BellglassTokenMemoryItem;
import dev.spud.shadowslave.item.BorrowedDawnMemoryItem;
import dev.spud.shadowslave.item.ModItems;
import dev.spud.shadowslave.item.RedThreadBraceletMemoryItem;
import dev.spud.shadowslave.item.VeilStitchCaseMemoryItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** NeoForge/Minecraft execution adapter for soul-stored Memory ownership. Inventory stacks never establish ownership. */
public final class MemoryManifestationService {
    private MemoryManifestationService() {}

    public static ManifestResult summonAshCompass(ServerPlayer player) { return summon(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY.get()); }
    public static ManifestResult dismissAshCompass(ServerPlayer player) { return dismiss(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY.get()); }
    public static ManifestResult summonBellglassToken(ServerPlayer player) { return summon(player, BellglassTokenMemoryItem.MEMORY_ID, ModItems.BELLGLASS_TOKEN_MEMORY.get()); }
    public static ManifestResult dismissBellglassToken(ServerPlayer player) { return dismiss(player, BellglassTokenMemoryItem.MEMORY_ID, ModItems.BELLGLASS_TOKEN_MEMORY.get()); }
    public static ManifestResult summonRedThreadBracelet(ServerPlayer player) { return summon(player, RedThreadBraceletMemoryItem.MEMORY_ID, ModItems.RED_THREAD_BRACELET_MEMORY.get()); }
    public static ManifestResult dismissRedThreadBracelet(ServerPlayer player) { return dismiss(player, RedThreadBraceletMemoryItem.MEMORY_ID, ModItems.RED_THREAD_BRACELET_MEMORY.get()); }
    public static ManifestResult summonBorrowedDawn(ServerPlayer player) { return summon(player, BorrowedDawnMemoryItem.MEMORY_ID, ModItems.BORROWED_DAWN_MEMORY.get()); }
    public static ManifestResult dismissBorrowedDawn(ServerPlayer player) { return dismiss(player, BorrowedDawnMemoryItem.MEMORY_ID, ModItems.BORROWED_DAWN_MEMORY.get()); }
    public static ManifestResult summonVeilStitchCase(ServerPlayer player) { return summon(player, VeilStitchCaseMemoryItem.MEMORY_ID, ModItems.VEIL_STITCH_CASE_MEMORY.get()); }
    public static ManifestResult dismissVeilStitchCase(ServerPlayer player) { return dismiss(player, VeilStitchCaseMemoryItem.MEMORY_ID, ModItems.VEIL_STITCH_CASE_MEMORY.get()); }

    public static void clearAshCompassManifestations(ServerPlayer player) { removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.ASH_COMPASS_MEMORY.get()); }
    public static void clearBellglassTokenManifestations(ServerPlayer player) { removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.BELLGLASS_TOKEN_MEMORY.get()); }
    public static void clearRedThreadBraceletManifestations(ServerPlayer player) { removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.RED_THREAD_BRACELET_MEMORY.get()); }
    public static void clearBorrowedDawnManifestations(ServerPlayer player) { removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.BORROWED_DAWN_MEMORY.get()); }
    public static void clearVeilStitchCaseManifestations(ServerPlayer player) { removeManifestations(Objects.requireNonNull(player, "player").getInventory(), ModItems.VEIL_STITCH_CASE_MEMORY.get()); }

    static boolean hasAshCompass(Inventory inventory) { return hasManifestation(inventory, ModItems.ASH_COMPASS_MEMORY.get()); }
    static boolean hasBellglassToken(Inventory inventory) { return hasManifestation(inventory, ModItems.BELLGLASS_TOKEN_MEMORY.get()); }
    static boolean hasRedThreadBracelet(Inventory inventory) { return hasManifestation(inventory, ModItems.RED_THREAD_BRACELET_MEMORY.get()); }
    static boolean hasBorrowedDawn(Inventory inventory) { return hasManifestation(inventory, ModItems.BORROWED_DAWN_MEMORY.get()); }
    static boolean hasVeilStitchCase(Inventory inventory) { return hasManifestation(inventory, ModItems.VEIL_STITCH_CASE_MEMORY.get()); }

    private static ManifestResult summon(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Item item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        if (hasManifestation(player.getInventory(), item)) return ManifestResult.ALREADY_SUMMONED;
        ItemStack stack = new ItemStack(item);
        if (!player.addItem(stack)) return ManifestResult.INVENTORY_FULL;
        return ManifestResult.SUMMONED;
    }

    private static ManifestResult dismiss(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Item item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        return removeManifestations(player.getInventory(), item) ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    private static boolean hasManifestation(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) if (inventory.getItem(slot).is(item)) return true;
        return false;
    }

    private static boolean removeManifestations(Inventory inventory, Item item) {
        boolean removed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) { inventory.setItem(slot, ItemStack.EMPTY); removed = true; }
        }
        return removed;
    }

    public enum ManifestResult { SUMMONED, DISMISSED, NOT_OWNED, ALREADY_SUMMONED, NOT_SUMMONED, INVENTORY_FULL }
}
