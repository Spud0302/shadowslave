package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Pure deterministic encounter budget for the later-era Storm Lantern Coast preview.
 * Creature identity comes from the Java-owned region profile; Minecraft entities are
 * only physical executors in {@link StormLanternCoastPreviewService}.
 */
public final class StormLanternCoastEncounterPlan {
    private static final Set<String> EXECUTABLE_AFFINITIES = Set.of("drowned_listener", "chainback");

    private StormLanternCoastEncounterPlan() {
    }

    public enum Pressure {
        FLOOD_EDGE,
        RUIN_GUARD,
        EXPOSED_ROUTE
    }

    /**
     * World-context reason an encounter was budgeted. This is deterministic placement
     * metadata, not creature identity, progression state, or a persistent ecology simulation.
     */
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
            StormLanternCoastSitePlan.Piece piece,
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

    public static Plan forSite(StormLanternCoastSitePlan.Plan sitePlan) {
        Objects.requireNonNull(sitePlan, "sitePlan");
        DreamRealmRegionContentCatalog.RegionProfile region = sitePlan.region();
        if (!StormLanternCoastSitePlan.REGION_ID.equals(region.id())) {
            throw new IllegalArgumentException("wrong region");
        }
        if (!region.creatureAffinityIds().containsAll(EXECUTABLE_AFFINITIES)) {
            throw new IllegalStateException("Storm Lantern Coast no longer authorizes the executable encounter affinities");
        }

        long encounterSeed = mix(sitePlan.siteSeed(), "encounters");
        SplittableRandom random = new SplittableRandom(encounterSeed);
        ArrayList<Encounter> encounters = new ArrayList<>();

        // The drowned terraces are the strongest flood-edge pressure point and always
        // receive the region-affine Listener executor, but the exact offset varies.
        StormLanternCoastSitePlan.Piece terraces = piece(sitePlan, "drowned_harbour_terraces");
        encounters.add(new Encounter(
                "drowned_listener",
                terraces.anchorId(),
                Pressure.FLOOD_EDGE,
                EcologyContext.FLOOD_MARGIN,
                terraces.x() + jitter(random, 4),
                terraces.y() + 3,
                terraces.z() + jitter(random, 4)
        ));

        // Quarry/belfry pressure varies by seed so the same region identity does not
        // collapse into one guaranteed encounter coordinate.
        StormLanternCoastSitePlan.Piece ruin = random.nextBoolean()
                ? piece(sitePlan, "collapsed_quarry_cut")
                : piece(sitePlan, "storm_belfry");
        encounters.add(new Encounter(
                "chainback",
                ruin.anchorId(),
                Pressure.RUIN_GUARD,
                EcologyContext.HISTORIC_RUIN,
                ruin.x() + jitter(random, 6),
                ruin.y() + 2,
                ruin.z() + jitter(random, 6)
        ));

        // Optional pressure now responds to why a location matters, rather than only
        // choosing another arbitrary route coordinate. High cliff routes and valuable
        // wreckage are attractive danger locations; the collapsed shelter is weighted
        // lower and pressure is kept outside its immediate interior so shelter retains
        // practical value. Selection is without replacement and deterministic.
        ArrayList<OptionalPressure> optional = new ArrayList<>(List.of(
                candidate(sitePlan, "coast_watch_0", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(sitePlan, "coast_watch_1", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(sitePlan, "salvage_ledge", EcologyContext.RESOURCE_EDGE, 5, 5, 1),
                candidate(sitePlan, "storm_shelter", EcologyContext.SHELTER_MARGIN, 1, 3, 1)
        ));

        int extra = random.nextInt(3); // 0-2, preserving a total budget of 2-4.
        for (int i = 0; i < extra; i++) {
            OptionalPressure selected = takeWeighted(random, optional);
            String creature = random.nextBoolean() ? "chainback" : "drowned_listener";
            int x;
            int z;
            if (selected.ecologyContext() == EcologyContext.SHELTER_MARGIN) {
                // Keep danger on an approach margin, not directly inside the shelter.
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

    private static OptionalPressure candidate(StormLanternCoastSitePlan.Plan plan, String anchorId,
                                              EcologyContext context, int weight, int jitterRadius, int yOffset) {
        return new OptionalPressure(piece(plan, anchorId), context, weight, jitterRadius, yOffset);
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

    private static StormLanternCoastSitePlan.Piece piece(StormLanternCoastSitePlan.Plan plan, String anchorId) {
        return plan.pieces().stream()
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
