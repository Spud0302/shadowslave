package dev.spud.shadowslave.spell;

import net.minecraft.nbt.CompoundTag;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * One notable thing the Spell saw happen, as the Recorder keeps it (DESIGN,
 * Jack 2026-10-29). Structured data only: what happened, to whom, about what,
 * and a short detail. The Judge reads it, the Voice may recall it, and a later
 * Chronicler can turn it into history.
 *
 * @param time    game time it happened at
 * @param kind    what happened
 * @param player  who it happened to, if anyone
 * @param name    their name when it happened, kept for history; never sent to a voice
 * @param subject what it was about: a creature kind, a Nightmare id, a Citadel name, a grade
 * @param detail  extra words: the rank and class of a kill, a Category, a first's key
 */
public record SpellEvent(long time, Kind kind, Optional<UUID> player, String name, String subject, String detail) {

    public enum Kind {
        /** A Nightmare Creature slain by a player. */
        KILL,
        /** A Tyrant or stronger, or a named creature such as a Guardian, slain by a player. */
        UNIQUE_KILL,
        /** A Nightmare conquered and appraised. */
        SEED_CLEARED,
        /** A challenger who died inside a Nightmare and was judged. */
        SEED_FAILED,
        /** A challenger who left a Nightmare some other way. */
        SEED_FLED,
        CITADEL_CLAIMED,
        CITADEL_LOST,
        /** A Seed bloomed near a Citadel and raided it, or the raid was lost. */
        CITADEL_RAIDED,
        /** A player helped hold a Citadel against a raid. */
        CITADEL_DEFENDED,
        /** A player's death in the waking world or the Dream Realm. */
        DEATH,
        /** A player's soul rose a rank. */
        RANK_UP,
        /** The first time anyone under the Spell did something. */
        FIRST,
        /** The Spell granted a player a True Name. */
        TRUE_NAME,
        /** A player learned another's True Name. */
        NAME_LEARNED,
        /** A player spoke a True Name they know and sensed where its bearer stood. */
        NAME_SENSED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Optional<Kind> byId(String id) {
            for (Kind kind : values()) {
                if (kind.id().equals(id)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }
    }

    public SpellEvent {
        name = name == null ? "" : name;
        subject = subject == null ? "" : subject;
        detail = detail == null ? "" : detail;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("time", time);
        tag.putString("kind", kind.id());
        player.ifPresent(id -> tag.putUUID("player", id));
        tag.putString("name", name);
        tag.putString("subject", subject);
        tag.putString("detail", detail);
        return tag;
    }

    static Optional<SpellEvent> load(CompoundTag tag) {
        return Kind.byId(tag.getString("kind")).map(kind -> new SpellEvent(tag.getLong("time"), kind,
                tag.hasUUID("player") ? Optional.of(tag.getUUID("player")) : Optional.empty(),
                tag.getString("name"), tag.getString("subject"), tag.getString("detail")));
    }

    /** One line for logs and the record command. */
    public String describe() {
        String who = name.isEmpty() ? "" : name + " ";
        String about = subject.isEmpty() ? "" : " " + subject;
        String extra = detail.isEmpty() ? "" : " (" + detail + ")";
        return who + kind.id() + about + extra;
    }
}
