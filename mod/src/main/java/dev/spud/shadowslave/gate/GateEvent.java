package dev.spud.shadowslave.gate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One Nightmare Gate, from warning to close (DESIGN, after ch. 484): a
 * countdown with the Category forecast, then waves of creatures pour out,
 * then the Guardian emerges, and killing it closes the Gate. This is plain
 * saved state; {@link GateService} runs it.
 */
public final class GateEvent {
    public enum Phase implements StringRepresentable {
        WARNING, OPEN, GUARDIAN, CLOSED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Phase byName(String name) {
            for (Phase phase : values()) {
                if (phase.getSerializedName().equals(name)) {
                    return phase;
                }
            }
            return CLOSED;
        }
    }

    /** Warning countdown before a Gate opens. */
    public static final int WARNING_TICKS = 30 * 20;

    final UUID id;
    final BlockPos center;
    final GateTrigger trigger;
    final GateForecast forecast;
    Phase phase = Phase.WARNING;
    int category;
    int phaseTicks;
    int wave;
    int nextWaveIn;
    UUID rift;
    UUID guardian;
    final List<UUID> spawned = new ArrayList<>();

    GateEvent(UUID id, BlockPos center, GateTrigger trigger, GateForecast forecast) {
        this.id = id;
        this.center = center.immutable();
        this.trigger = trigger;
        this.forecast = forecast;
        this.category = forecast.likeliest();
    }

    public UUID id() {
        return id;
    }

    public BlockPos center() {
        return center;
    }

    public Phase phase() {
        return phase;
    }

    /** The forecast's likeliest Category during the warning, the rolled one after. */
    public int category() {
        return category;
    }

    public GateForecast forecast() {
        return forecast;
    }

    public GateTrigger trigger() {
        return trigger;
    }

    public int wave() {
        return wave;
    }

    public int phaseTicks() {
        return phaseTicks;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.put("center", NbtUtils.writeBlockPos(center));
        tag.putString("trigger", trigger.getSerializedName());
        CompoundTag odds = new CompoundTag();
        forecast.odds().forEach((category, percent) -> odds.putInt(Integer.toString(category), percent));
        tag.put("odds", odds);
        tag.putString("phase", phase.getSerializedName());
        tag.putInt("category", category);
        tag.putInt("phase_ticks", phaseTicks);
        tag.putInt("wave", wave);
        tag.putInt("next_wave_in", nextWaveIn);
        if (rift != null) {
            tag.putUUID("rift", rift);
        }
        if (guardian != null) {
            tag.putUUID("guardian", guardian);
        }
        ListTag list = new ListTag();
        spawned.forEach(uuid -> list.add(NbtUtils.createUUID(uuid)));
        tag.put("spawned", list);
        return tag;
    }

    static GateEvent load(CompoundTag tag) {
        Map<Integer, Integer> odds = new LinkedHashMap<>();
        CompoundTag oddsTag = tag.getCompound("odds");
        for (String key : oddsTag.getAllKeys()) {
            odds.put(Integer.parseInt(key), oddsTag.getInt(key));
        }
        BlockPos center = NbtUtils.readBlockPos(tag, "center").orElse(BlockPos.ZERO);
        GateEvent event = new GateEvent(tag.getUUID("id"), center, GateTrigger.byName(tag.getString("trigger")),
                odds.isEmpty() ? GateForecast.certain(1) : new GateForecast(odds));
        event.phase = Phase.byName(tag.getString("phase"));
        event.category = tag.getInt("category");
        event.phaseTicks = tag.getInt("phase_ticks");
        event.wave = tag.getInt("wave");
        event.nextWaveIn = tag.getInt("next_wave_in");
        event.rift = tag.hasUUID("rift") ? tag.getUUID("rift") : null;
        event.guardian = tag.hasUUID("guardian") ? tag.getUUID("guardian") : null;
        for (Tag uuid : tag.getList("spawned", Tag.TAG_INT_ARRAY)) {
            event.spawned.add(NbtUtils.loadUUID(uuid));
        }
        return event;
    }
}
