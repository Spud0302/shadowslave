package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaystyleEvidenceTest {
    @Test
    void noActivityGivesNoEvidence() {
        GenerationEvidence evidence = PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(0, 0, 0, 0));
        assertTrue(evidence.weights().isEmpty());
    }

    @Test
    void eachPlaystyleMapsToItsNatureTagInTwoTiers() {
        GenerationEvidence evidence = PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(
                PlaystyleEvidence.EMBER_LOW,
                PlaystyleEvidence.MIST_HIGH_CM,
                PlaystyleEvidence.STONE_LOW - 1,
                PlaystyleEvidence.TIDE_HIGH_CM * 10));
        assertEquals(1, evidence.weightFor("ember"));
        assertEquals(2, evidence.weightFor("mist"));
        assertEquals(0, evidence.weightFor("stone"));
        assertEquals(2, evidence.weightFor("water"));
    }
}
