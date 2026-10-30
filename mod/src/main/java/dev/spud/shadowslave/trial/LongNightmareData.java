package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.trial.content.TrialContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Every Long Nightmare, saved with the world (DESIGN, Jack 2026-10-29): the
 * ones still running, and the ones that ended while a challenger slept inside
 * and has not yet come back to hear the Spell's appraisal. Its runs are the
 * same objects {@link TrialService} ticks, so marking it dirty saves them as
 * they stand.
 */
public final class LongNightmareData extends SavedData {
    private static final String NAME = "shadowslave_long_nightmares";

    final Map<UUID, TrialInstance> runs = new LinkedHashMap<>();
    /** Saved runs whose Nightmare could not be found on load, kept untouched in case the data pack comes back. */
    private final List<CompoundTag> orphans = new ArrayList<>();

    public static LongNightmareData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(LongNightmareData::new, LongNightmareData::load), NAME);
    }

    public List<TrialInstance> runs() {
        return List.copyOf(runs.values());
    }

    public Optional<TrialInstance> run(UUID id) {
        return Optional.ofNullable(runs.get(id));
    }

    void keep(TrialInstance instance) {
        runs.put(instance.id, instance);
        setDirty();
    }

    void forget(TrialInstance instance) {
        if (runs.remove(instance.id) != null) {
            setDirty();
        }
    }

    /** The run a player still belongs to: one they are inside, sleeping in, or owed an appraisal or a fall from. */
    public Optional<TrialInstance> runOf(UUID player) {
        return runs.values().stream()
                .filter(run -> run.member(player).filter(member -> member.inside || member.pending || member.fellAway).isPresent())
                .findFirst();
    }

    private static LongNightmareData load(CompoundTag tag, HolderLookup.Provider registries) {
        LongNightmareData data = new LongNightmareData();
        for (Tag entry : tag.getList("runs", Tag.TAG_COMPOUND)) {
            CompoundTag run = (CompoundTag) entry;
            Optional<TrialInstance> loaded = TrialInstance.load(run, TrialContent.get()::nightmare);
            if (loaded.isPresent()) {
                data.runs.put(loaded.get().id, loaded.get());
            } else {
                ShadowSlaveMod.LOGGER.warn("A Long Nightmare ({}) is missing from the data packs; it sleeps until it returns",
                        run.getString("nightmare"));
                data.orphans.add(run);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        runs.values().forEach(run -> list.add(run.save()));
        list.addAll(orphans);
        tag.put("runs", list);
        return tag;
    }
}
