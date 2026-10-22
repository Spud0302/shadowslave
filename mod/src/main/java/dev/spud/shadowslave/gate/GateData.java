package dev.spud.shadowslave.gate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/** The Gates open in one dimension, saved with the world so a restart resumes them. */
public final class GateData extends SavedData {
    private static final String NAME = "shadowslave_gates";
    final List<GateEvent> gates = new ArrayList<>();

    public static GateData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(GateData::new, GateData::load), NAME);
    }

    private static GateData load(CompoundTag tag, HolderLookup.Provider registries) {
        GateData data = new GateData();
        for (Tag gate : tag.getList("gates", Tag.TAG_COMPOUND)) {
            data.gates.add(GateEvent.load((CompoundTag) gate));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        gates.forEach(gate -> list.add(gate.save()));
        tag.put("gates", list);
        return tag;
    }

    public List<GateEvent> gates() {
        return List.copyOf(gates);
    }
}
