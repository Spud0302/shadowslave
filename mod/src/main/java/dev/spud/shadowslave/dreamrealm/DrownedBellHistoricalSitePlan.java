package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.content.NightmareHistoricalSiteCatalog;

import java.util.List;
import java.util.Objects;

/** Pure historical-era execution plan for the Drowned Bell cliff settlement. */
public final class DrownedBellHistoricalSitePlan {
    private DrownedBellHistoricalSitePlan() {}

    public record Piece(String anchorId, String pieceFamily, int x, int y, int z) {
        public Piece {
            anchorId = requireText(anchorId, "anchorId");
            pieceFamily = requireText(pieceFamily, "pieceFamily");
        }
    }

    public record Plan(
            NightmareHistoricalSiteCatalog.Site site,
            DrownedBellScenarioDefinition.ScenarioContent scenario,
            List<Piece> pieces
    ) {
        public Plan {
            site = Objects.requireNonNull(site, "site");
            scenario = Objects.requireNonNull(scenario, "scenario");
            pieces = List.copyOf(Objects.requireNonNull(pieces, "pieces"));
            if (!site.id().equals(StormLanternCoastSitePlan.SITE_ID)) throw new IllegalArgumentException("wrong site");
            if (!scenario.id().equals(DrownedBellScenarioDefinition.SCENARIO_ID)) throw new IllegalArgumentException("wrong scenario");
            if (pieces.size() != 4) throw new IllegalArgumentException("historical plan must expose four recognizable anchors");
        }
    }

    /**
     * Uses the same local anchor geography as the later Storm Lantern Coast plan.
     * Geometry/palette changes by era; landmark identity and relative placement do not.
     */
    public static Plan drownedBell() {
        return new Plan(
                NightmareHistoricalSiteCatalog.drownedBell(),
                DrownedBellScenarioDefinition.content(),
                List.of(
                        new Piece("bell_tower", "intact_bell_tower", -20, 3, -17),
                        new Piece("sea_gate", "working_sea_gate", 18, 0, 18),
                        new Piece("quarry_tunnels", "open_quarry_tunnels", 24, 5, -20),
                        new Piece("lower_village", "inhabited_harbour_terraces", -10, -2, 20)
                )
        );
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }
}
