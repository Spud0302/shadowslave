package dev.spud.shadowslave.smithing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The hearth's fire, crucible and heating slot. Four crucible slots hold a
 * pattern's materials, one holds fuel, one holds the billet being heated.
 * The fire burns only while a billet sits in it.
 */
public final class ForgeHearthBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CRUCIBLE_SLOTS = 4;
    public static final int FUEL_SLOT = 4;
    public static final int BILLET_SLOT = 5;
    public static final int SIZE = 6;
    /** How long one pump of the bellows lasts. */
    static final int BELLOWS_TICKS = 60;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int temperature = SmithingRules.AMBIENT;
    private int burn;
    private int burnTotal;
    private int bellows;

    /** temperature, billet heat, burn left, burn total, bellows left. */
    final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> temperature;
                case 1 -> billetHeat();
                case 2 -> burn;
                case 3 -> burnTotal;
                case 4 -> bellows;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> temperature = value;
                case 2 -> burn = value;
                case 3 -> burnTotal = value;
                case 4 -> bellows = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public ForgeHearthBlockEntity(BlockPos pos, BlockState state) {
        super(SmithingRegistry.FORGE_HEARTH_ENTITY.get(), pos, state);
    }

    public int temperature() {
        return temperature;
    }

    private int billetHeat() {
        BilletData billet = items.get(BILLET_SLOT).get(SmithingRegistry.BILLET.get());
        return billet == null || level == null ? 0 : billet.heatAt(level.getGameTime());
    }

    void pumpBellows() {
        bellows = BELLOWS_TICKS;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgeHearthBlockEntity hearth) {
        hearth.tick(level, pos, state);
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        ItemStack billetStack = items.get(BILLET_SLOT);
        BilletData billet = billetStack.get(SmithingRegistry.BILLET.get());
        boolean wantsFire = billet != null && !billet.cold();
        if (burn <= 0 && wantsFire) {
            ItemStack fuel = items.get(FUEL_SLOT);
            int time = fuel.getBurnTime(RecipeType.SMELTING);
            if (time > 0) {
                burn = burnTotal = time;
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty()) {
                    items.set(FUEL_SLOT, remainder);
                }
            }
        }
        boolean burning = burn > 0;
        if (burning) {
            burn -= bellows > 0 ? 2 : 1;
        }
        if (bellows > 0) {
            bellows--;
        }
        temperature = SmithingRules.hearthStep(temperature, burning, bellows > 0 && burning);
        if (billet != null) {
            long now = level.getGameTime();
            int heat = SmithingRules.billetStep(billet.heatAt(now), temperature);
            BilletData next = SmithingRules.burning(heat, billet.heatMax()) ? billet.burning(heat, now) : billet.measured(heat, now, 100);
            billetStack.set(SmithingRegistry.BILLET.get(), next);
        }
        if (state.getValue(ForgeHearthBlock.LIT) != burning) {
            level.setBlock(pos, state.setValue(ForgeHearthBlock.LIT, burning), 3);
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.shadowslave.forge_hearth");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ForgeHearthMenu(id, inventory, this, data, ContainerLevelAccess.create(player.level(), worldPosition));
    }

    // Container

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(items, slot, amount);
        setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("temperature", temperature);
        tag.putInt("burn", burn);
        tag.putInt("burn_total", burnTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        temperature = tag.contains("temperature") ? tag.getInt("temperature") : SmithingRules.AMBIENT;
        burn = tag.getInt("burn");
        burnTotal = tag.getInt("burn_total");
    }
}
