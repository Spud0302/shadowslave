package dev.spud.shadowslave.migration;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable evidence read from the frozen datapack before any legacy value is
 * modified. A later live reader will construct this record from tags and
 * scoreboard objectives; translation remains pure and independently tested.
 */
public record LegacyDatapackSnapshot(
        UUID playerId,
        boolean carrier,
        int rankScore,
        int aspectScore,
        int flawScore,
        Set<String> tags,
        boolean activeNightmare,
        int completedMigrationVersion
) {
    public LegacyDatapackSnapshot {
        playerId = Objects.requireNonNull(playerId, "playerId");
        tags = Set.copyOf(Objects.requireNonNull(tags, "tags"));
        if (rankScore < 0 || aspectScore < 0 || flawScore < 0) {
            throw new IllegalArgumentException("Legacy scores cannot be negative");
        }
        if (completedMigrationVersion < 0) {
            throw new IllegalArgumentException("completedMigrationVersion cannot be negative");
        }
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean hasAnyLegacyState() {
        return carrier || rankScore > 0 || aspectScore > 0 || flawScore > 0 || activeNightmare;
    }
}
