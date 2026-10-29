package dev.spud.shadowslave.spell.calling;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** How often a player wants the Spell to call to them: a client option, applied on the server. */
public enum CallingFrequency implements StringRepresentable {
    OFF("off", "Off"),
    RARE("rare", "Rare"),
    NORMAL("normal", "Normal"),
    OFTEN("often", "Often");

    public static final Codec<CallingFrequency> CODEC = StringRepresentable.fromEnum(CallingFrequency::values);

    private final String id;
    private final String displayName;

    CallingFrequency(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public CallingFrequency next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static CallingFrequency byOrdinal(int ordinal) {
        CallingFrequency[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NORMAL;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
