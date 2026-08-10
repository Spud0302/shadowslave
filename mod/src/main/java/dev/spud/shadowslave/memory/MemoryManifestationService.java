package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.item.AshCompassMemoryItem;
import dev.spud.shadowslave.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * NeoForge/Minecraft execution adapter for soul-stored Memory ownership.
 * Inventory stacks are manifestations only and never establish ownership.
 */
public final class MemoryManifestationService {
    private MemoryManifestationService() {
    }

    public static ManifestResult summonAshCompass(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, AshCompassMemoryItem.MEMORY_ID)) {
            return ManifestResult.NOT_OWNED;
        }
        if (hasAshCompass(player.getInventory())) {
            return ManifestResult.ALREADY_SUMMONED;
        }
        ItemStack stack = new ItemStack(ModItems.ASH_COMPASS_MEMORY.get());
        if (!player.addItem(stack)) {
            return ManifestResult.INVENTORY_FULL;
        }
        return ManifestResult.SUMMONED;
    }

    public static ManifestResult dismissAshCompass(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (!MemoryOwnershipService.owns(player, AshCompassMemoryItem.MEMORY_ID)) {
            return ManifestResult.NOT_OWNED;
        }
        Inventory inventory = player.getInventory();
        boolean removed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ModItems.ASH_COMPASS_MEMORY.get())) {
                inventory.setItem(slot, ItemStack.EMPTY);
                removed = true;
            }
        }
        return removed ? ManifestResult.DISMISSED : ManifestResult.NOT_SUMMONED;
    }

    static boolean hasAshCompass(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(ModItems.ASH_COMPASS_MEMORY.get())) {
                return true;
            }
        }
        return false;
    }

    public enum ManifestResult {
        SUMMONED,
        DISMISSED,
        NOT_OWNED,
        ALREADY_SUMMONED,
        NOT_SUMMONED,
        INVENTORY_FULL
    }
}
