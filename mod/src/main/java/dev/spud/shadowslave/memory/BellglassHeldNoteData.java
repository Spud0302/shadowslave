package dev.spud.shadowslave.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/** Persistent Java-owned payload for Bellglass Token's authored held_note enchantment. */
public record BellglassHeldNoteData(Optional<String> instrument, Optional<Integer> note) {
    private static final MapCodec<BellglassHeldNoteData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("instrument").forGetter(BellglassHeldNoteData::instrument),
            Codec.INT.optionalFieldOf("note").forGetter(BellglassHeldNoteData::note)
    ).apply(instance, BellglassHeldNoteData::new));

    public static final MapCodec<BellglassHeldNoteData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new BellglassHeldNoteData(value.instrument(), value.note()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid BellglassHeldNoteData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public BellglassHeldNoteData {
        instrument = Objects.requireNonNull(instrument, "instrument");
        note = Objects.requireNonNull(note, "note");
        if (instrument.isPresent() != note.isPresent()) {
            throw new IllegalArgumentException("Bellglass held note must contain both instrument and note or neither");
        }
        instrument = instrument.map(value -> {
            String checked = value.trim();
            if (checked.isEmpty()) throw new IllegalArgumentException("instrument cannot be blank");
            return checked;
        });
        note.ifPresent(value -> {
            if (value < 0 || value > 24) throw new IllegalArgumentException("note must be between 0 and 24");
        });
    }

    public static BellglassHeldNoteData empty() {
        return new BellglassHeldNoteData(Optional.empty(), Optional.empty());
    }

    public static BellglassHeldNoteData captured(String instrument, int note) {
        return new BellglassHeldNoteData(Optional.of(Objects.requireNonNull(instrument, "instrument")), Optional.of(note));
    }

    public boolean hasNote() {
        return instrument.isPresent();
    }

    public BellglassHeldNoteData clear() {
        return empty();
    }
}
