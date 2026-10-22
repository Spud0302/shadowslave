package dev.spud.shadowslave.soul.refinement;

import java.util.Locale;

/** Where a refinement point goes (DESIGN): the body, the soul's essence, or the Aspect itself. */
public enum RefinementStat {
    BODY("Body"),
    SOUL("Soul"),
    ASPECT("Aspect");

    private final String label;

    RefinementStat(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
