package dev.spud.shadowslave.smithing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The one piece lying on an anvil: a billet being shaped, or a finished piece waiting to be taken. */
public final class SmithingAnvilBlockEntity extends BlockEntity {
    private ItemStack piece = ItemStack.EMPTY;

    public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(SmithingRegistry.SMITHING_ANVIL_ENTITY.get(), pos, state);
    }

    public ItemStack piece() {
        return piece;
    }

    public void setPiece(ItemStack stack) {
        piece = stack;
        changed();
    }

    public ItemStack take() {
        ItemStack taken = piece;
        piece = ItemStack.EMPTY;
        changed();
        return taken;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!piece.isEmpty()) {
            tag.put("piece", piece.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        piece = tag.contains("piece") ? ItemStack.parse(registries, tag.getCompound("piece")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
