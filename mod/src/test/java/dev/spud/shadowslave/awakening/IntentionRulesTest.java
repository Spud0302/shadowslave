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
        assertEquals(List.of(13, 13, 13, 13, 13, 13, 13, 13), IntentionRules.odds(IntentionRules.apply(NO_EVIDENCE, IntentionData.empty())));
    }

    @Test
    void offeringAndLongingForOneNatureDoubleItsChance() {
        GenerationEvidence leaned = IntentionRules.apply(NO_EVIDENCE, EMBER_BOTH);
        assertEquals(4, leaned.weightFor("ember"));
        assertEquals(List.of(25, 11, 11, 11, 11, 11, 11, 11), IntentionRules.odds(leaned));
    }

    @Test
    void splitIntentionsLeanTwoWays() {
        GenerationEvidence leaned = IntentionRules.apply(NO_EVIDENCE, new IntentionData("stone", "Iron Ingot", "return"));
        assertEquals(List.of(11, 11, 18, 18, 11, 11, 11, 11), IntentionRules.odds(leaned));
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
        assertTrue(share > 0.20 && share < 0.30, "Ember came up " + share);
    }

    @Test
    void everyNatureHasALonging() {
        for (AspectKit kit : AspectKitCatalog.all()) {
            assertTrue(java.util.Arrays.stream(Longing.values()).anyMatch(longing -> longing.natureName().equals(kit.natureName())),
                    kit.natureName());
        }
    }
}
