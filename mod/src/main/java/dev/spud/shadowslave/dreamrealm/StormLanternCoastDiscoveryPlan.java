package dev.spud.shadowslave.dreamrealm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure exploration-information layer derived from the deterministic Storm Lantern
 * encounter plan. Clues deliberately reveal pressure class, not creature position.
 */
public final class StormLanternCoastDiscoveryPlan {
    private static final int ARRIVAL_X = 0;
    private static final int ARRIVAL_Z = -32;
    private static final int STANDOFF = 5;

    private StormLanternCoastDiscoveryPlan() {
    }

    public enum ClueKind {
        DISTURBED_FLOOD_EDGE,
        CHAIN_SCAR,
        EXPOSED_ROUTE_DAMAGE
    }

    public record Clue(
            ClueKind kind,
            StormLanternCoastEncounterPlan.Pressure pressure,
            String anchorId,
            int x,
            int y,
            int z
    ) {
        public Clue {
            kind = Objects.requireNonNull(kind, "kind");
            pressure = Objects.requireNonNull(pressure, "pressure");
            anchorId = Objects.requireNonNull(anchorId, "anchorId").trim();
            if (anchorId.isEmpty()) throw new IllegalArgumentException("anchorId cannot be blank");
        }
    }

    public record Plan(List<Clue> clues) {
        public Plan {
            clues = List.copyOf(Objects.requireNonNull(clues, "clues"));
            if (clues.isEmpty()) throw new IllegalArgumentException("Storm Lantern discovery plan cannot be empty");
        }
    }

    public static Plan fromEncounters(StormLanternCoastEncounterPlan.Plan encounters) {
        Objects.requireNonNull(encounters, "encounters");
        ArrayList<Clue> clues = new ArrayList<>();
        for (StormLanternCoastEncounterPlan.Encounter encounter : encounters.encounters()) {
            ClueKind kind = switch (encounter.pressure()) {
                case FLOOD_EDGE -> ClueKind.DISTURBED_FLOOD_EDGE;
                case RUIN_GUARD -> ClueKind.CHAIN_SCAR;
                case EXPOSED_ROUTE -> ClueKind.EXPOSED_ROUTE_DAMAGE;
            };
            clues.add(new Clue(
                    kind,
                    encounter.pressure(),
                    encounter.anchorId(),
                    toward(encounter.x(), ARRIVAL_X, STANDOFF),
                    encounter.y(),
                    toward(encounter.z(), ARRIVAL_Z, STANDOFF)
            ));
        }
        return new Plan(clues);
    }

    /**
     * Move a clue a bounded number of blocks from the encounter toward the arrival
     * route. This keeps the clue useful as warning information without marking the
     * exact hostile coordinate.
     */
    static int toward(int from, int target, int distance) {
        if (distance < 0) throw new IllegalArgumentException("distance cannot be negative");
        int delta = target - from;
        if (delta == 0 || distance == 0) return from;
        int step = Math.min(Math.abs(delta), distance);
        return from + Integer.signum(delta) * step;
    }
}
