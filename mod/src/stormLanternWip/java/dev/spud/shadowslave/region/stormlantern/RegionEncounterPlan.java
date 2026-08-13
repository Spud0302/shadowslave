package dev.spud.shadowslave.region.stormlantern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Deterministic encounter-budget vocabulary ported from the reviewed Storm Lantern
 * integration lineage. This belongs to the optional region provider, not base Shadow Slave.
 */
public final class RegionEncounterPlan {
    private static final Set<String> EXECUTABLE_AFFINITIES = Set.of("drowned_listener", "chainback");

    private RegionEncounterPlan() {
    }

    public enum Pressure {
        FLOOD_EDGE,
        RUIN_GUARD,
        EXPOSED_ROUTE
    }

    public enum EcologyContext {
        FLOOD_MARGIN,
        HISTORIC_RUIN,
        HIGH_EXPOSURE,
        RESOURCE_EDGE,
        SHELTER_MARGIN
    }

    public record Encounter(
            String creatureId,
            String anchorId,
            Pressure pressure,
            EcologyContext ecologyContext,
            int x,
            int y,
            int z
    ) {
        public Encounter {
            creatureId = requireText(creatureId, "creatureId");
            anchorId = requireText(anchorId, "anchorId");
            pressure = Objects.requireNonNull(pressure, "pressure");
            ecologyContext = Objects.requireNonNull(ecologyContext, "ecologyContext");
            if (!EXECUTABLE_AFFINITIES.contains(creatureId)) {
                throw new IllegalArgumentException("encounter creature is not physically executable: " + creatureId);
            }
        }
    }

    public record Plan(long encounterSeed, List<Encounter> encounters) {
        public Plan {
            encounters = List.copyOf(Objects.requireNonNull(encounters, "encounters"));
            if (encounters.size() < 2 || encounters.size() > 4) {
                throw new IllegalArgumentException("Storm Lantern encounter budget must contain 2-4 encounters");
            }
        }
    }

    private record OptionalPressure(
            RegionLayout.Piece piece,
            EcologyContext ecologyContext,
            int weight,
            int jitterRadius,
            int yOffset
    ) {
        private OptionalPressure {
            Objects.requireNonNull(piece, "piece");
            Objects.requireNonNull(ecologyContext, "ecologyContext");
            if (weight <= 0) throw new IllegalArgumentException("weight must be positive");
            if (jitterRadius < 0) throw new IllegalArgumentException("jitterRadius cannot be negative");
        }
    }

    public static Plan create(long worldSeed) {
        List<RegionLayout.Piece> pieces = RegionLayout.create(worldSeed);
        long encounterSeed = mix(worldSeed, "encounters");
        SplittableRandom random = new SplittableRandom(encounterSeed);
        ArrayList<Encounter> encounters = new ArrayList<>();

        RegionLayout.Piece terraces = piece(pieces, "harbour_terraces");
        encounters.add(new Encounter(
                "drowned_listener",
                terraces.anchorId(),
                Pressure.FLOOD_EDGE,
                EcologyContext.FLOOD_MARGIN,
                terraces.x() + jitter(random, 4),
                terraces.y() + 3,
                terraces.z() + jitter(random, 4)
        ));

        RegionLayout.Piece ruin = random.nextBoolean()
                ? piece(pieces, "collapsed_quarry_cut")
                : piece(pieces, "storm_belfry");
        encounters.add(new Encounter(
                "chainback",
                ruin.anchorId(),
                Pressure.RUIN_GUARD,
                EcologyContext.HISTORIC_RUIN,
                ruin.x() + jitter(random, 6),
                ruin.y() + 2,
                ruin.z() + jitter(random, 6)
        ));

        ArrayList<OptionalPressure> optional = new ArrayList<>(List.of(
                candidate(pieces, "coast_watch_0", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(pieces, "coast_watch_1", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(pieces, "salvage_ledge", EcologyContext.RESOURCE_EDGE, 5, 5, 1),
                candidate(pieces, "storm_shelter", EcologyContext.SHELTER_MARGIN, 1, 3, 1)
        ));

        int extra = random.nextInt(3);
        for (int i = 0; i < extra; i++) {
            OptionalPressure selected = takeWeighted(random, optional);
            String creature = random.nextBoolean() ? "chainback" : "drowned_listener";
            int x;
            int z;
            if (selected.ecologyContext() == EcologyContext.SHELTER_MARGIN) {
                x = selected.piece().x() + (random.nextBoolean() ? 8 : -8);
                z = selected.piece().z() + jitter(random, selected.jitterRadius());
            } else {
                x = selected.piece().x() + jitter(random, selected.jitterRadius());
                z = selected.piece().z() + jitter(random, selected.jitterRadius());
            }
            encounters.add(new Encounter(
                    creature,
                    selected.piece().anchorId(),
                    Pressure.EXPOSED_ROUTE,
                    selected.ecologyContext(),
                    x,
                    selected.piece().y() + selected.yOffset(),
                    z
            ));
        }

        return new Plan(encounterSeed, encounters);
    }

    private static OptionalPressure candidate(List<RegionLayout.Piece> pieces, String anchorId,
                                              EcologyContext context, int weight, int jitterRadius, int yOffset) {
        return new OptionalPressure(piece(pieces, anchorId), context, weight, jitterRadius, yOffset);
    }

    private static OptionalPressure takeWeighted(SplittableRandom random, ArrayList<OptionalPressure> candidates) {
        int totalWeight = candidates.stream().mapToInt(OptionalPressure::weight).sum();
        int roll = random.nextInt(totalWeight);
        for (int i = 0; i < candidates.size(); i++) {
            OptionalPressure candidate = candidates.get(i);
            if (roll < candidate.weight()) {
                candidates.remove(i);
                return candidate;
            }
            roll -= candidate.weight();
        }
        throw new IllegalStateException("weighted Storm Lantern pressure selection exhausted unexpectedly");
    }

    private static RegionLayout.Piece piece(List<RegionLayout.Piece> pieces, String anchorId) {
        return pieces.stream()
                .filter(piece -> piece.anchorId().equals(anchorId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Storm Lantern site anchor " + anchorId));
    }

    private static int jitter(SplittableRandom random, int radius) {
        return random.nextInt(radius * 2 + 1) - radius;
    }

    private static long mix(long seed, String stableId) {
        long value = seed ^ 0xD6E8FEB86659FD93L;
        for (int i = 0; i < stableId.length(); i++) {
            value ^= stableId.charAt(i) * 0x9E3779B97F4A7C15L;
            value = Long.rotateLeft(value, 17) * 0x94D049BB133111EBL;
        }
        value ^= value >>> 31;
        return value;
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }
}
