package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.nightmare.content.DrownedBellScenarioDefinition;
import dev.spud.shadowslave.nightmare.content.NightmareHistoricalSiteCatalog;

import java.util.Map;
import java.util.Objects;

/**
 * Converts one committed Drowned Bell terminal resolution into the fate axes that
 * the runtime has actually proven changed from the authored original history.
 *
 * <p>Unmentioned axes deliberately remain unknown. A terminal resolution does not
 * receive credit for historical changes that its accepted event path did not prove.</p>
 */
public final class DrownedBellFateOutcome {
    private DrownedBellFateOutcome() {
    }

    public static NightmareDivergenceAppraisal.Result appraise(String resolutionId) {
        return NightmareDivergenceAppraisal.score(
                NightmareHistoricalSiteCatalog.drownedBell(),
                resolvedHistory(resolutionId)
        );
    }

    public static Map<String, String> resolvedHistory(String resolutionId) {
        String checked = Objects.requireNonNull(resolutionId, "resolutionId").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("resolutionId cannot be blank");
        }
        if (!DrownedBellScenarioDefinition.content().resolutions().containsKey(checked)) {
            throw new IllegalArgumentException("Unknown Drowned Bell resolution " + checked);
        }

        return switch (checked) {
            case "tower_held" -> Map.of(
                    "warning_bell", "sounded",
                    "lower_village", "warned"
            );
            case "villagers_evacuated" -> Map.of(
                    "quarry_route", "opened",
                    "lower_village", "evacuated"
            );
            case "flood_diverted" -> Map.of(
                    "sea_gate", "diverted",
                    "lower_village", "spared"
            );
            case "creature_buried" -> Map.of(
                    "warning_bell", "used_as_lure",
                    "drowned_listener", "buried"
            );
            default -> throw new IllegalStateException("Unhandled Drowned Bell resolution " + checked);
        };
    }
}
