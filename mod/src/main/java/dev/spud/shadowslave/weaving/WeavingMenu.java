package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.item.SoulShardItem;
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
 * The Weaving Table's slots (DESIGN, after Sunny's Weaving): the piece in the
 * centre, a soul-shard anchor beside it, four slots for the materials a strand
 * is spun from (or a Woven Thread to lay), and one a Woven Thread comes out
 * of when a strand is unravelled. Buttons ask the server to study, unravel,
 * lay a thread, raise a branch or respec; a traced weave arrives as its own
 * packet. The server decides everything.
 */
public class WeavingMenu extends AbstractContainerMenu {
    public static final int BUTTON_RESPEC = 1;
    public static final int BUTTON_LAY = 2;
    /** Binding the echo remnant in the centre into an Echo. */
    public static final int BUTTON_BIND = 3;
    /** Raising branch i is button {@code BUTTON_RAISE + i}, in {@link WeavingContent#branchOrder()} order. */
    public static final int BUTTON_RAISE = 10;
    /** Starting and finishing the study of strand i of what the centre piece teaches. */
    public static final int BUTTON_STUDY = 100;
    public static final int BUTTON_STUDIED = 200;
    /** Unravelling strand i out of the centre piece. */
    public static final int BUTTON_UNRAVEL = 300;
    public static final int MAX_LISTED = 16;

    public static final int CENTRE_SLOT = 0;
    public static final int ANCHOR_SLOT = 1;
    public static final int FIRST_MATERIAL = 2;
    public static final int OUTPUT_SLOT = FIRST_MATERIAL + WeavingRules.MATERIAL_SLOTS;
    public static final int TABLE_SLOTS = OUTPUT_SLOT + 1;

    public static final int CENTRE_X = 14;
    public static final int CENTRE_Y = 24;
    public static final int ANCHOR_X = 14;
    public static final int ANCHOR_Y = 58;
    public static final int[][] MATERIAL_POS = {{8, 98}, {26, 98}, {8, 116}, {26, 116}};
    public static final int OUTPUT_X = 152;
    public static final int OUTPUT_Y = 116;
    public static final int INVENTORY_Y = 152;

    private final ContainerLevelAccess access;
    private final Player player;
    final SimpleContainer table = new SimpleContainer(TABLE_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            WeavingMenu.this.slotsChanged(this);
        }
    };

    /** Client side. */
    public WeavingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public WeavingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(WeavingRegistry.WEAVING_MENU.get(), id);
        this.access = access;
        this.player = inventory.player;
        addSlot(new Slot(table, CENTRE_SLOT, CENTRE_X, CENTRE_Y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new Slot(table, ANCHOR_SLOT, ANCHOR_X, ANCHOR_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof SoulShardItem;
            }
        });
        for (int i = 0; i < WeavingRules.MATERIAL_SLOTS; i++) {
            addSlot(new Slot(table, FIRST_MATERIAL + i, MATERIAL_POS[i][0], MATERIAL_POS[i][1]));
        }
        addSlot(new Slot(table, OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
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

    public ItemStack centre() {
        return table.getItem(CENTRE_SLOT);
    }

    public ItemStack anchor() {
        return table.getItem(ANCHOR_SLOT);
    }

    public ItemStack output() {
        return table.getItem(OUTPUT_SLOT);
    }

    public Container table() {
        return table;
    }

    /** What lies in the material slots, as item id to total amount. */
    public Map<ResourceLocation, Integer> materialAmounts() {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        for (int i = FIRST_MATERIAL; i < OUTPUT_SLOT; i++) {
            ItemStack stack = table.getItem(i);
            if (!stack.isEmpty()) {
                amounts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount(), Integer::sum);
            }
        }
        return amounts;
    }

    /** Takes {@code count} of {@code item} from the material slots; the caller has checked they are there. */
    void takeMaterial(ResourceLocation item, int count) {
        for (int i = FIRST_MATERIAL; i < OUTPUT_SLOT && count > 0; i++) {
            ItemStack stack = table.getItem(i);
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item)) {
                int taken = Math.min(count, stack.getCount());
                stack.shrink(taken);
                count -= taken;
            }
        }
        table.setChanged();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu == this) {
            WeavingService.sync(serverPlayer);
            dev.spud.shadowslave.echoes.EchoService.sync(serverPlayer);
        }
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
        if (index < TABLE_SLOTS) {
            if (!moveItemStackTo(stack, TABLE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof SoulShardItem) {
            if (!moveItemStackTo(stack, ANCHOR_SLOT, ANCHOR_SLOT + 1, false)
                    && !moveItemStackTo(stack, FIRST_MATERIAL, OUTPUT_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        } else if ((StrandFit.of(stack).isPresent() || stack.is(dev.spud.shadowslave.echoes.EchoesRegistry.ECHO_REMNANT.get()))
                && !this.slots.get(CENTRE_SLOT).hasItem()) {
            if (!moveItemStackTo(stack, CENTRE_SLOT, CENTRE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, FIRST_MATERIAL, OUTPUT_SLOT, false)
                && !moveItemStackTo(stack, CENTRE_SLOT, CENTRE_SLOT + 1, false)) {
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
            clearContainer(player, table);
        }
    }
}
