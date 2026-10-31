package dev.spud.shadowslave.fieldguide;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The Field Notes pinned to a public Field Guide board, oldest first, each with who pinned it. */
public final class FieldBoardBlockEntity extends BlockEntity {
    public record Posted(UUID poster, String posterName, ItemStack note) {
    }

    private final List<Posted> notes = new ArrayList<>();

    public FieldBoardBlockEntity(BlockPos pos, BlockState state) {
        super(FieldGuideRegistry.FIELD_BOARD_ENTITY.get(), pos, state);
    }

    public List<Posted> notes() {
        return List.copyOf(notes);
    }

    public void post(Posted posted) {
        notes.add(posted);
        changed();
    }

    /** Takes back the newest note this player pinned, or with {@code anyone}, the newest note of all. */
    public Optional<Posted> takeBack(UUID player, boolean anyone) {
        for (int i = notes.size() - 1; i >= 0; i--) {
            if (anyone || notes.get(i).poster().equals(player)) {
                Posted taken = notes.remove(i);
                changed();
                return Optional.of(taken);
            }
        }
        return Optional.empty();
    }

    public List<ItemStack> takeAll() {
        List<ItemStack> all = notes.stream().map(Posted::note).toList();
        notes.clear();
        changed();
        return all;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            // The world's state, not the cached one: while the board is being broken it is already gone.
            BlockState state = level.getBlockState(worldPosition);
            int shown = Math.min(notes.size(), FieldBoardBlock.MAX_SHOWN);
            if (state.getBlock() instanceof FieldBoardBlock && state.getValue(FieldBoardBlock.NOTES) != shown) {
                level.setBlock(worldPosition, state.setValue(FieldBoardBlock.NOTES, shown), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Posted posted : notes) {
            CompoundTag entry = new CompoundTag();
            UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, posted.poster()).result().ifPresent(id -> entry.put("poster", id));
            entry.putString("poster_name", posted.posterName());
            entry.put("note", posted.note().save(registries));
            list.add(entry);
        }
        tag.put("notes", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        notes.clear();
        for (Tag each : tag.getList("notes", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) each;
            Optional<ItemStack> note = ItemStack.parse(registries, entry.getCompound("note"));
            UUID poster = entry.contains("poster") ? UUIDUtil.CODEC.parse(NbtOps.INSTANCE, entry.get("poster")).result().orElse(new UUID(0, 0))
                    : new UUID(0, 0);
            note.ifPresent(stack -> notes.add(new Posted(poster, entry.getString("poster_name"), stack)));
        }
    }
}
