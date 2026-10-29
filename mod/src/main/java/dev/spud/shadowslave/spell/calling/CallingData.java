package dev.spud.shadowslave.spell.calling;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One player's Callings: the ones they have heard (for the Field Guide), what
 * has already been called so it is not called again, and the client frequency
 * they chose. Kept through death.
 */
public final class CallingData {
    /** How many Seeds and Citadels are remembered as already called. */
    static final int REMEMBERED = 128;

    /** One Calling as it was heard: what about, the words, and the day it came. */
    public record Entry(String calling, String text, long day) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("calling").forGetter(Entry::calling),
                Codec.STRING.fieldOf("text").forGetter(Entry::text),
                Codec.LONG.fieldOf("day").forGetter(Entry::day)
        ).apply(instance, Entry::new));
    }

    public static final Codec<CallingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CallingFrequency.CODEC.optionalFieldOf("frequency", CallingFrequency.NORMAL).forGetter(data -> data.frequency),
            Entry.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(data -> data.history),
            UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("seeds", List.of()).forGetter(data -> data.seeds),
            UUIDUtil.STRING_CODEC.listOf().optionalFieldOf("citadels", List.of()).forGetter(data -> data.citadels),
            Codec.STRING.listOf().optionalFieldOf("regions", List.of()).forGetter(data -> List.copyOf(data.regions)),
            Codec.BOOL.optionalFieldOf("core_called", false).forGetter(data -> data.coreCalled),
            Codec.INT.optionalFieldOf("idle_ticks", 0).forGetter(data -> data.idleTicks),
            Codec.LONG.optionalFieldOf("last_calling", 0L).forGetter(data -> data.lastCalling),
            Codec.STRING.optionalFieldOf("last_rank", "?").forGetter(data -> data.lastRank),
            Codec.INT.optionalFieldOf("last_saturation", 0).forGetter(data -> data.lastSaturation)
    ).apply(instance, CallingData::new));

    CallingFrequency frequency;
    final List<Entry> history;
    final List<UUID> seeds;
    final List<UUID> citadels;
    final Set<String> regions;
    boolean coreCalled;
    int idleTicks;
    long lastCalling;
    /** The soul rank last seen, so a rise is noticed; "?" until first seen. */
    String lastRank;
    int lastSaturation;

    private CallingData(CallingFrequency frequency, List<Entry> history, List<UUID> seeds, List<UUID> citadels, List<String> regions,
                        boolean coreCalled, int idleTicks, long lastCalling, String lastRank, int lastSaturation) {
        this.frequency = frequency;
        this.history = new ArrayList<>(history);
        this.seeds = new ArrayList<>(seeds);
        this.citadels = new ArrayList<>(citadels);
        this.regions = new HashSet<>(regions);
        this.coreCalled = coreCalled;
        this.idleTicks = idleTicks;
        this.lastCalling = lastCalling;
        this.lastRank = lastRank;
        this.lastSaturation = lastSaturation;
    }

    public static CallingData empty() {
        return new CallingData(CallingFrequency.NORMAL, List.of(), List.of(), List.of(), List.of(), false, 0, 0L, "?", 0);
    }

    public CallingFrequency frequency() {
        return frequency;
    }

    public List<Entry> history() {
        return List.copyOf(history);
    }

    void remember(List<UUID> list, UUID id) {
        list.add(id);
        while (list.size() > REMEMBERED) {
            list.remove(0);
        }
    }

    void add(Entry entry, int kept) {
        history.add(entry);
        while (history.size() > kept) {
            history.remove(0);
        }
    }
}
