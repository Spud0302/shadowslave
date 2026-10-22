package dev.spud.shadowslave.gate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** What a Gate Lure has been fed: its pull, in quarter points, and how many shards. */
public final class GateLureBlockEntity extends BlockEntity {
    /** A Lure holds this many shards at most. */
    public static final int CAPACITY = 32;

    private int pullQuarters;
    private int shards;

    public GateLureBlockEntity(BlockPos pos, BlockState state) {
        super(GateRegistry.GATE_LURE_ENTITY.get(), pos, state);
    }

    public int pull() {
        return pullQuarters / 4;
    }

    public int shards() {
        return shards;
    }

    public boolean full() {
        return shards >= CAPACITY;
    }

    void feed(int quarters) {
        pullQuarters += quarters;
        shards++;
        setChanged();
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        LureCharge charge = input.getOrDefault(GateRegistry.LURE_CHARGE.get(), LureCharge.EMPTY);
        pullQuarters = charge.pullQuarters();
        shards = charge.shards();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (shards > 0) {
            components.set(GateRegistry.LURE_CHARGE.get(), new LureCharge(pullQuarters, shards));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("pull_quarters");
        tag.remove("shards");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("pull_quarters", pullQuarters);
        tag.putInt("shards", shards);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pullQuarters = tag.getInt("pull_quarters");
        shards = tag.getInt("shards");
    }
}
