package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntentionRulesTest {
    private static final GenerationEvidence NO_EVIDENCE =
            PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(0, 0, 0, 0));
    private static final IntentionData EMBER_BOTH = new IntentionData("ember", "Blaze Powder", "burn");

    @Test
    void noIntentionLeavesEvenOdds() {
        assertEquals(List.of(25, 25, 25, 25), IntentionRules.odds(IntentionRules.apply(NO_EVIDENCE, IntentionData.empty())));
    }

    @Test
    void offeringAndLongingForOneNatureTakeItToAboutFortyFourPercent() {
        GenerationEvidence leaned = IntentionRules.apply(NO_EVIDENCE, EMBER_BOTH);
        assertEquals(4, leaned.weightFor("ember"));
        assertEquals(List.of(44, 19, 19, 19), IntentionRules.odds(leaned));
    }

    @Test
    void splitIntentionsLeanTwoWays() {
        GenerationEvidence leaned = IntentionRules.apply(NO_EVIDENCE, new IntentionData("stone", "Iron Ingot", "return"));
        assertEquals(List.of(19, 19, 31, 31), IntentionRules.odds(leaned));
    }

    @Test
    void playstyleAndIntentionAreCappedSoNothingIsCertain() {
        GenerationEvidence heavyEmber = PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(10_000, 0, 0, 0));
        GenerationEvidence leaned = IntentionRules.apply(heavyEmber, EMBER_BOTH);
        assertEquals(IntentionRules.MAX_TAG_WEIGHT, leaned.weightFor("ember"));
        assertTrue(IntentionRules.odds(leaned).get(0) < 50);
    }

    @Test
    void theRollItselfFollowsTheLeanedOdds() {
        GenerationEvidence leaned = IntentionRules.apply(NO_EVIDENCE, EMBER_BOTH);
        int ember = 0;
        int rolls = 2000;
        for (long seed = 0; seed < rolls; seed++) {
            if (AspectRoller.roll(seed, leaned).kit() == AspectKitCatalog.EMBER) {
                ember++;
            }
        }
        double share = (double) ember / rolls;
        assertTrue(share > 0.38 && share < 0.50, "Ember came up " + share);
    }
}
