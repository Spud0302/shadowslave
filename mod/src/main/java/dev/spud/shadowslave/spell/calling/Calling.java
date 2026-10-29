package dev.spud.shadowslave.spell.calling;

import java.util.Locale;
import java.util.Optional;

/**
 * What the Spell calls to a player about (DESIGN, Spell Callings section):
 * short runic lines over its starry thread-web, nudging them on their way.
 */
public enum Calling {
    /** Their soul core is nearly saturated. */
    CORE_NEARLY_FULL("The Core"),
    /** A Seed of the Category they need next grows nearby. */
    SEED_NEXT_CATEGORY("A Seed"),
    /** They entered a region of the Dream Realm for the first time. */
    NEW_REGION("A New Region"),
    /** An unclaimed Citadel stands nearby. */
    UNCLAIMED_CITADEL("A Citadel"),
    /** A long time has passed without progress; the Spell calls gently. */
    IDLE("Stillness");

    private final String title;

    Calling(String title) {
        this.title = title;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The heading the Field Guide's Callings page gives it. */
    public String title() {
        return title;
    }

    public static Optional<Calling> byId(String id) {
        for (Calling calling : values()) {
            if (calling.id().equals(id)) {
                return Optional.of(calling);
            }
        }
        return Optional.empty();
    }
}
