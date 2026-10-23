package dev.spud.shadowslave.weaving;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Weaving table's slots: four for materials and one the woven Memory
 * appears in. Buttons ask the server to weave, raise a branch or respec;
 * the server decides everything.
 */
public class WeavingMenu extends AbstractContainerMenu {
    public static final int BUTTON_WEAVE = 0;
    public static final int BUTTON_RESPEC = 1;
    /** Raising branch i is button {@code BUTTON_RAISE + i}, in {@link WeavingContent#branchOrder()} order. */
    public static final int BUTTON_RAISE = 10;

    public static final int[][] MATERIAL_POS = {{34, 30}, {52, 30}, {34, 48}, {52, 48}};
    public static final int OUTPUT_X = 124;
    public static final int OUTPUT_Y = 39;
    public static final int INVENTORY_Y = 140;

    private final ContainerLevelAccess access;
    final SimpleContainer materials = new SimpleContainer(WeavingRules.MATERIAL_SLOTS);
    final SimpleContainer output = new SimpleContainer(1);

    /** Client side. */
    public WeavingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public WeavingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(WeavingRegistry.WEAVING_MENU.get(), id);
        this.access = access;
        for (int i = 0; i < WeavingRules.MATERIAL_SLOTS; i++) {
            addSlot(new Slot(materials, i, MATERIAL_POS[i][0], MATERIAL_POS[i][1]));
        }
        addSlot(new Slot(output, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }
    }

    /** What lies on the table, as item id to total amount. */
    public Map<ResourceLocation, Integer> materialAmounts() {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        for (int i = 0; i < materials.getContainerSize(); i++) {
            ItemStack stack = materials.getItem(i);
            if (!stack.isEmpty()) {
                amounts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount(), Integer::sum);
            }
        }
        return amounts;
    }

    public Container materials() {
        return materials;
    }

    public Container output() {
        return output;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player instanceof ServerPlayer serverPlayer) {
            WeavingService.handleButton(serverPlayer, this, id);
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int tableSlots = WeavingRules.MATERIAL_SLOTS + 1;
        if (index < tableSlots) {
            if (!moveItemStackTo(stack, tableSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, WeavingRules.MATERIAL_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, WeavingRegistry.WEAVING_TABLE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            clearContainer(player, materials);
            clearContainer(player, output);
        }
    }
}
