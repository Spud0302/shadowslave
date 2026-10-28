package dev.spud.shadowslave.smithing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The hearth's slots: a two by two crucible, fuel, and the billet over the
 * fire. The Fuse button asks the server to fuse the crucible into a billet;
 * the server decides everything.
 */
public class ForgeHearthMenu extends AbstractContainerMenu {
    public static final int BUTTON_FUSE = 0;
    public static final int[][] CRUCIBLE_POS = {{26, 22}, {44, 22}, {26, 40}, {44, 40}};
    public static final int FUEL_X = 116;
    public static final int FUEL_Y = 58;
    public static final int BILLET_X = 116;
    public static final int BILLET_Y = 22;
    public static final int INVENTORY_Y = 114;

    private final Container hearth;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Client side. */
    public ForgeHearthMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(ForgeHearthBlockEntity.SIZE), new SimpleContainerData(5), ContainerLevelAccess.NULL);
    }

    public ForgeHearthMenu(int id, Inventory inventory, Container hearth, ContainerData data, ContainerLevelAccess access) {
        super(SmithingRegistry.FORGE_HEARTH_MENU.get(), id);
        this.hearth = hearth;
        this.data = data;
        this.access = access;
        for (int i = 0; i < ForgeHearthBlockEntity.CRUCIBLE_SLOTS; i++) {
            addSlot(new Slot(hearth, i, CRUCIBLE_POS[i][0], CRUCIBLE_POS[i][1]));
        }
        addSlot(new Slot(hearth, ForgeHearthBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getBurnTime(RecipeType.SMELTING) > 0;
            }
        });
        addSlot(new Slot(hearth, ForgeHearthBlockEntity.BILLET_SLOT, BILLET_X, BILLET_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(SmithingRegistry.BILLET.get());
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
        addDataSlots(data);
    }

    public int temperature() {
        return data.get(0);
    }

    public int billetHeat() {
        return data.get(1);
    }

    /** How much of the current fuel is left, 0 to 1. */
    public float burnLeft() {
        int total = data.get(3);
        return total <= 0 ? 0.0F : data.get(2) / (float) total;
    }

    public boolean bellows() {
        return data.get(4) > 0;
    }

    public ItemStack billet() {
        return hearth.getItem(ForgeHearthBlockEntity.BILLET_SLOT);
    }

    /** What lies in the crucible, as item id to total amount. */
    public Map<ResourceLocation, Integer> crucibleAmounts() {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        for (int i = 0; i < ForgeHearthBlockEntity.CRUCIBLE_SLOTS; i++) {
            ItemStack stack = hearth.getItem(i);
            if (!stack.isEmpty()) {
                amounts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount(), Integer::sum);
            }
        }
        return amounts;
    }

    Container hearth() {
        return hearth;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_FUSE && player instanceof ServerPlayer serverPlayer) {
            SmithingService.fuse(serverPlayer, this);
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
        int hearthSlots = ForgeHearthBlockEntity.SIZE;
        if (index < hearthSlots) {
            if (!moveItemStackTo(stack, hearthSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.has(SmithingRegistry.BILLET.get())) {
            if (!moveItemStackTo(stack, ForgeHearthBlockEntity.BILLET_SLOT, ForgeHearthBlockEntity.BILLET_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getBurnTime(RecipeType.SMELTING) > 0 && moveItemStackTo(stack, ForgeHearthBlockEntity.FUEL_SLOT,
                ForgeHearthBlockEntity.FUEL_SLOT + 1, false)) {
            // Fuel goes to the fire first; what doesn't fit falls through to the crucible below.
        } else if (!moveItemStackTo(stack, 0, ForgeHearthBlockEntity.CRUCIBLE_SLOTS, false)) {
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
        return stillValid(access, player, SmithingRegistry.FORGE_HEARTH.get());
    }
}
