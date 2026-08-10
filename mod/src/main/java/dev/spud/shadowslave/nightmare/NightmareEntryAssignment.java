package dev.spud.shadowslave.nightmare;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.content.NightmareRoleScenarioCompatibilityCatalog;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Resolves the Java-owned authored identity used when a playable Nightmare instance is created. */
public final class NightmareEntryAssignment {
    private NightmareEntryAssignment() {
    }

    public record Assignment(
            String scenarioId,
            NightmareRoleScenarioCompatibilityCatalog.ScenarioRoleMatch roleMatch
    ) {
        public Assignment {
            scenarioId = requireText(scenarioId, "scenarioId");
            roleMatch = Objects.requireNonNull(roleMatch, "roleMatch");
            if (!scenarioId.equals(roleMatch.scenarioId())) {
                throw new IllegalArgumentException("role assignment must belong to the resolved scenario");
            }
        }

        public String historicalRoleId() {
            return roleMatch.role().id();
        }
    }

    /** Resolves one of the physically implemented First Nightmares and a compatible authored role exactly once. */
    public static Assignment resolveFirstNightmare(UUID playerId, long gameTime) {
        Objects.requireNonNull(playerId, "playerId");
        if (gameTime < 0) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }

        long seed = mix(playerId, gameTime);
        String scenarioId = (seed & 1L) == 0L
                ? LastSignalScenario.SCENARIO_ID
                : DrownedBellScenarioDefinition.SCENARIO_ID;
        NightmareRoleScenarioCompatibilityCatalog.ScenarioRoleMatch roleMatch =
                NightmareRoleScenarioCompatibilityCatalog.match(scenarioId, Long.rotateLeft(seed, 17), Map.of());
        return new Assignment(scenarioId, roleMatch);
    }

    private static long mix(UUID playerId, long gameTime) {
        long value = playerId.getMostSignificantBits() ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 23);
        value ^= gameTime * 0x9E3779B97F4A7C15L;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
