package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.item.AshCompassMemoryItem;
import dev.spud.shadowslave.item.BlackwaterHookMemoryItem;
import dev.spud.shadowslave.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

/** NeoForge/Minecraft execution adapter for soul-stored Memory ownership. Inventory stacks never establish ownership. */
public final class MemoryManifestationService {
    private MemoryManifestationService() {}

    public static ManifestResult summonAshCompass(ServerPlayer player) {
        return summon(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY);
    }

    public static ManifestResult dismissAshCompass(ServerPlayer player) {
        return dismiss(player, AshCompassMemoryItem.MEMORY_ID, ModItems.ASH_COMPASS_MEMORY);
    }

    public static ManifestResult summonBlackwaterHook(ServerPlayer player) {
        return summon(player, BlackwaterHookMemoryItem.MEMORY_ID, ModItems.BLACKWATER_HOOK_MEMORY);
    }

    public static ManifestResult dismissBlackwaterHook(ServerPlayer player) {
        return dismiss(player, BlackwaterHookMemoryItem.MEMORY_ID, ModItems.BLACKWATER_HOOK_MEMORY);
    }

    public static void clearAshCompassManifestations(ServerPlayer player) {
        remove(Objects.requireNonNull(player, "player").getInventory(), ModItems.ASH_COMPASS_MEMORY);
    }

    public static void clearBlackwaterHookManifestations(ServerPlayer player) {
        remove(Objects.requireNonNull(player, "player").getInventory(), ModItems.BLACKWATER_HOOK_MEMORY);
    }

    static boolean hasAshCompass(Inventory inventory) {
        return has(inventory, ModItems.ASH_COMPASS_MEMORY);
    }

    private static ManifestResult summon(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Supplier<? extends Item> item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        if (has(player.getInventory(), item)) return ManifestResult.ALREADY_SUMMONED;
        ItemStack stack = new ItemStack(item.get());
        if (!player.addItem(stack)) return ManifestResult.INVENTORY_FULL;
        return ManifestResult.SUMMONED;
    }

    private static ManifestResult dismiss(ServerPlayer player, net.minecraft.resources.ResourceLocation memoryId, Supplier<? extends Item> item) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, memoryId)) return ManifestResult.NOT_OWNED;
        return remove(player.getInventory(), item) ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    private static boolean has(Inventory inventory, Supplier<? extends Item> item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item.get())) return true;
        }
        return false;
    }

    private static boolean remove(Inventory inventory, Supplier<? extends Item> item) {
        boolean removed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item.get())) {
                inventory.setItem(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        return removed;
    }

    public enum ManifestResult { SUMMONED, DISMISSED, NOT_OWNED, ALREADY_SUMMONED, NOT_SUMMONED, INVENTORY_FULL }
}
