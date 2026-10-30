package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * How far a Citadel's Gateway has gone hungry (DESIGN, Run C). Neglect comes
 * in stages and only ever takes things away for a while: the wards drop first,
 * then the workshops slow, then the Guardian's echo stops defending. A
 * Citadel is never lost to neglect, and one meal sets it back to Fed.
 */
public enum Neglect implements StringRepresentable {
    /** The Gateway has provisions left. */
    FED("Fed"),
    /** Out of provisions: the wards drop. */
    HUNGRY("Hungry"),
    /** Hungry for a while: the workshops slow. */
    STARVING("Starving"),
    /** Starving for long: the Guardian's echo no longer defends it. */
    FORSAKEN("Forsaken");

    public static final Codec<Neglect> CODEC = StringRepresentable.fromEnum(Neglect::values);

    private final String displayName;

    Neglect(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Neglect byOrdinal(int ordinal) {
        Neglect[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }
}
