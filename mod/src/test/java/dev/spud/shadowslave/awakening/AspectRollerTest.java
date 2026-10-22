package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.appraisal.generation.GenerationEvidence;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilityKind;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectRollerTest {
    private static final GenerationEvidence NO_EVIDENCE =
            PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(0, 0, 0, 0));

    @Test
    void sameSeedAndEvidenceRollTheSameAspect() {
        assertEquals(AspectRoller.roll(42L, NO_EVIDENCE), AspectRoller.roll(42L, NO_EVIDENCE));
    }

    @Test
    void everyRollIsExactlyOneHandMadeKit() {
        for (long seed = 0; seed < 200; seed++) {
            AspectRoller.Roll roll = AspectRoller.roll(seed, NO_EVIDENCE);
            AspectKit kit = roll.kit();
            AspectInstanceData aspect = roll.identity().aspect().orElseThrow();
            FlawInstanceData flaw = roll.identity().flaw().orElseThrow();

            assertEquals(kit.natureId(), aspect.natureId());
            assertEquals(SoulRank.DORMANT, aspect.aspectRank());
            assertTrue(aspect.displayedName().contains(kit.natureName()), aspect.displayedName());
            List<AspectAbilityData> abilities = aspect.abilitySet().abilities();
            assertEquals(2, abilities.size());
            assertEquals(AspectAbilityData.innate(kit.innateId(), abilities.get(0).provenance()), abilities.get(0));
            assertEquals(kit.abilityId(), abilities.get(1).abilityId());
            assertEquals(AspectAbilityKind.RANK_GRANTED, abilities.get(1).kind());
            assertEquals(kit.flawEffectId(), flaw.effectId());
            assertEquals(kit.flawName(), flaw.displayedName());
            assertEquals(kit.natureId(), roll.candidate().aspect().natureId());
        }
    }

    @Test
    void allFourNaturesAreReachableWithoutEvidence() {
        Map<AspectKit, Integer> counts = distribution(NO_EVIDENCE, 2000);
        assertEquals(4, counts.size());
        counts.values().forEach(count -> assertTrue(count > 350 && count < 650, counts.toString()));
    }

    @Test
    void playstyleTipsTheOddsButNeverDecides() {
        GenerationEvidence swimmer = PlaystyleEvidence.fromTotals(
                new PlaystyleEvidence.Totals(0, 0, 0, PlaystyleEvidence.TIDE_HIGH_CM));
        Map<AspectKit, Integer> counts = distribution(swimmer, 2000);
        int tide = counts.getOrDefault(AspectKitCatalog.TIDE, 0);
        assertTrue(tide > 600 && tide < 850, counts.toString());
        assertEquals(4, counts.size());
    }

    private static Map<AspectKit, Integer> distribution(GenerationEvidence evidence, int rolls) {
        Map<AspectKit, Integer> counts = new HashMap<>();
        for (long seed = 0; seed < rolls; seed++) {
            counts.merge(AspectRoller.roll(seed * 7919L + 13L, evidence).kit(), 1, Integer::sum);
        }
        return counts;
    }
}
