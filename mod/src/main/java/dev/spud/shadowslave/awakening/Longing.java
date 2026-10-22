package dev.spud.shadowslave.awakening;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * What a Dreamer tells the Spell they long for before it names their Aspect
 * (DESIGN). Each longing leans toward one Nature through its evidence tag.
 */
public enum Longing {
    BURN("Burn", "ember", "Ember"),
    HIDE("Hide", "mist", "Mist"),
    ENDURE("Endure", "stone", "Stone"),
    RETURN("Return", "water", "Tide");

    private final String label;
    private final String evidenceTag;
    private final String natureName;

    Longing(String label, String evidenceTag, String natureName) {
        this.label = label;
        this.evidenceTag = evidenceTag;
        this.natureName = natureName;
    }

    public String label() {
        return label;
    }

    public String evidenceTag() {
        return evidenceTag;
    }

    public String natureName() {
        return natureName;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<Longing> parse(String serialized) {
        return Arrays.stream(values()).filter(value -> value.serializedName().equals(serialized)).findFirst();
    }

    /** The Nature name for an evidence tag, for showing what an offering leans toward. */
    public static Optional<Longing> byTag(String evidenceTag) {
        return Arrays.stream(values()).filter(value -> value.evidenceTag.equals(evidenceTag)).findFirst();
    }
}
