package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.nightmare.content.NightmareHistoricalSiteCatalog;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareDivergenceAppraisalTest {
    @Test
    void reproducingOriginalHistoryScoresZero() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        NightmareDivergenceAppraisal.Result result = NightmareDivergenceAppraisal.score(site, Map.of(
                "warning_bell", "silent",
                "quarry_route", "sealed",
                "sea_gate", "failed",
                "lower_village", "inundated",
                "drowned_listener", "survived"
        ));

        assertEquals(0, result.score());
        assertEquals(0.0, result.ratio());
        assertEquals(site.originalHistory().keySet(), result.unchangedAxes());
    }

    @Test
    void changingMoreOriginalFateCannotLowerDivergenceScore() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        NightmareDivergenceAppraisal.Result oneChange = NightmareDivergenceAppraisal.score(site, Map.of(
                "warning_bell", "sounded"
        ));
        NightmareDivergenceAppraisal.Result threeChanges = NightmareDivergenceAppraisal.score(site, Map.of(
                "warning_bell", "sounded",
                "quarry_route", "opened",
                "lower_village", "evacuated"
        ));
        NightmareDivergenceAppraisal.Result allChanges = NightmareDivergenceAppraisal.score(site, Map.of(
                "warning_bell", "sounded",
                "quarry_route", "opened",
                "sea_gate", "diverted",
                "lower_village", "preserved",
                "drowned_listener", "buried"
        ));

        assertTrue(threeChanges.score() > oneChange.score());
        assertTrue(allChanges.score() > threeChanges.score());
        assertEquals(allChanges.maximumScore(), allChanges.score());
        assertEquals(1.0, allChanges.ratio());
    }

    @Test
    void unresolvedAxesGrantNoAssumedDeviationCredit() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        NightmareDivergenceAppraisal.Result result = NightmareDivergenceAppraisal.score(site, Map.of(
                "warning_bell", "sounded"
        ));

        assertEquals(site.originalHistory().get("warning_bell").weight(), result.score());
        assertEquals(site.originalHistory().size() - 1, result.unknownAxes().size());
    }

    @Test
    void unknownFateAxesFailClosed() {
        NightmareHistoricalSiteCatalog.Site site = NightmareHistoricalSiteCatalog.drownedBell();

        assertThrows(IllegalArgumentException.class, () -> NightmareDivergenceAppraisal.score(site, Map.of(
                "invented_axis", "changed"
        )));
    }
}
