package dev.spud.shadowslave.migration;

/** Result of one explicit live datapack migration attempt. */
public record DatapackMigrationOutcome(Status status, String detail) {
    public enum Status {
        NO_LEGACY_STATE,
        ALREADY_MIGRATED,
        MIGRATED_CARRIER,
        MIGRATED_DREAMER
    }
}
