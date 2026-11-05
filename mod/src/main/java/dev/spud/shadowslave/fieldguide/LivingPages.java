package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.network.payload.FieldGuideCopyRequestPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Field Notes written by a Scribe with Living Page: which note came from whose
 * guide, and what was asked of it, so the note can be written again from the
 * writer's guide as it stands whenever someone reads it. Saved with the
 * Overworld.
 */
public final class LivingPages extends SavedData {
    private static final String NAME = "shadowslave_living_pages";

    /** Who wrote a living note, and the copy request that wrote it. */
    public record Living(UUID author, FieldGuideCopyRequestPayload request) {
    }

    private final Map<UUID, Living> notes = new HashMap<>();

    public static LivingPages get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(LivingPages::new, LivingPages::load), NAME);
    }

    public void add(UUID note, UUID author, FieldGuideCopyRequestPayload request) {
        notes.put(note, new Living(author, request));
        setDirty();
    }

    public Optional<Living> living(UUID note) {
        return Optional.ofNullable(notes.get(note));
    }

    private static LivingPages load(CompoundTag tag, HolderLookup.Provider registries) {
        LivingPages data = new LivingPages();
        for (Tag each : tag.getList("notes", Tag.TAG_COMPOUND)) {
            CompoundTag note = (CompoundTag) each;
            if (!note.hasUUID("id") || !note.hasUUID("author")) {
                continue;
            }
            data.notes.put(note.getUUID("id"), new Living(note.getUUID("author"), new FieldGuideCopyRequestPayload(
                    note.getString("kind"), note.getString("target"), note.getString("dimension"), note.getInt("x"), note.getInt("z"))));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        notes.forEach((id, living) -> {
            CompoundTag note = new CompoundTag();
            note.putUUID("id", id);
            note.putUUID("author", living.author());
            note.putString("kind", living.request().kind());
            note.putString("target", living.request().target());
            note.putString("dimension", living.request().dimension());
            note.putInt("x", living.request().x());
            note.putInt("z", living.request().z());
            list.add(note);
        });
        tag.put("notes", list);
        return tag;
    }
}
