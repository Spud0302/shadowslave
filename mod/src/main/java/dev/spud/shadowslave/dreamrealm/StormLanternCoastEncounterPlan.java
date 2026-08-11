package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;

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

    /**
     * Coarse execution band derived from Java-owned Soul Rank. The exact encounter
     * tuning attached to these bands is project DESIGN, not a canonical Rank formula.
     */
    public enum ProgressionBand {
        UNRANKED,
        DORMANT,
        AWAKENED_OR_HIGHER
    }

    /** Coarse safe-settlement distance used by the encounter budget. */
    public enum SettlementDistanceBand {
        NEAR,
        FRONTIER,
        REMOTE
    }

    public record EncounterContext(ProgressionBand progressionBand, int nearestSettlementBlocks) {
        public EncounterContext {
            progressionBand = Objects.requireNonNull(progressionBand, "progressionBand");
            if (nearestSettlementBlocks < 0) {
                throw new IllegalArgumentException("nearestSettlementBlocks cannot be negative");
            }
        }

        public SettlementDistanceBand settlementDistanceBand() {
            if (nearestSettlementBlocks <= 96) return SettlementDistanceBand.NEAR;
            if (nearestSettlementBlocks <= 192) return SettlementDistanceBand.FRONTIER;
            return SettlementDistanceBand.REMOTE;
        }

        /** Compatibility default matching the current Cinder Rest -> Storm Lantern fixture spacing. */
        public static EncounterContext developmentFixture() {
            return new EncounterContext(ProgressionBand.DORMANT, 224);
        }
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

    public record Plan(long encounterSeed, EncounterContext context, List<Encounter> encounters) {
        public Plan {
            context = Objects.requireNonNull(context, "context");
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

    /**
     * Compatibility overload for pure callers that predate explicit ecology inputs.
     * Physical runtime should pass the current Java-owned player/world context.
     */
    public static Plan forSite(StormLanternCoastSitePlan.Plan sitePlan) {
        return forSite(sitePlan, EncounterContext.developmentFixture());
    }

    public static Plan forSite(StormLanternCoastSitePlan.Plan sitePlan, EncounterContext context) {
        Objects.requireNonNull(sitePlan, "sitePlan");
        EncounterContext checkedContext = Objects.requireNonNull(context, "context");
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

        // The historic ruin remains a deliberately dangerous landmark even when its
        // Chainback is above the current player's band. Rank disparity is therefore a
        // readable exploration problem, while optional pressure is band-limited below.
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

        // Optional pressure responds to why a location matters and now also to how far
        // the site lies from the nearest physical safe settlement plus the player's
        // coarse progression band. This is bounded DESIGN ecology, not a spawn-table or
        // canonical Rank formula. Selection remains without replacement and deterministic.
        ArrayList<OptionalPressure> optional = new ArrayList<>(List.of(
                candidate(sitePlan, "coast_watch_0", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(sitePlan, "coast_watch_1", EcologyContext.HIGH_EXPOSURE, 4, 7, 1),
                candidate(sitePlan, "salvage_ledge", EcologyContext.RESOURCE_EDGE, 5, 5, 1),
                candidate(sitePlan, "storm_shelter", EcologyContext.SHELTER_MARGIN, 1, 3, 1)
        ));

        int optionalCapacity = optionalCapacity(checkedContext);
        int extra = optionalCapacity == 0 ? 0 : random.nextInt(optionalCapacity + 1);
        List<String> optionalCreatures = optionalCreatureIds(checkedContext.progressionBand());
        for (int i = 0; i < extra; i++) {
            OptionalPressure selected = takeWeighted(random, optional);
            String creature = optionalCreatures.get(random.nextInt(optionalCreatures.size()));
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

        return new Plan(encounterSeed, checkedContext, encounters);
    }

    static int optionalCapacity(EncounterContext context) {
        int progressionCapacity = switch (context.progressionBand()) {
            case UNRANKED -> 0;
            case DORMANT -> 1;
            case AWAKENED_OR_HIGHER -> 2;
        };
        int settlementModifier = switch (context.settlementDistanceBand()) {
            case NEAR -> -1;
            case FRONTIER -> 0;
            case REMOTE -> 1;
        };
        return Math.max(0, Math.min(2, progressionCapacity + settlementModifier));
    }

    private static List<String> optionalCreatureIds(ProgressionBand progressionBand) {
        List<String> candidates = EXECUTABLE_AFFINITIES.stream()
                .filter(id -> optionalRankAllowed(creatureProfile(id).rank(), progressionBand))
                .sorted()
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No executable Storm Lantern creature fits progression band " + progressionBand);
        }
        return candidates;
    }

    private static boolean optionalRankAllowed(NightmareCreatureContentCatalog.Rank rank, ProgressionBand progressionBand) {
        return switch (progressionBand) {
            case UNRANKED, DORMANT -> rank == NightmareCreatureContentCatalog.Rank.DORMANT;
            case AWAKENED_OR_HIGHER -> rank == NightmareCreatureContentCatalog.Rank.DORMANT
                    || rank == NightmareCreatureContentCatalog.Rank.AWAKENED;
        };
    }

    private static NightmareCreatureContentCatalog.CreatureProfile creatureProfile(String creatureId) {
        return NightmareCreatureContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals(creatureId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Java creature profile " + creatureId));
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
