package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static dev.spud.shadowslave.appraisal.PreviewAppraisalService.Reconciliation.ALREADY_APPLIED;
import static dev.spud.shadowslave.appraisal.PreviewAppraisalService.Reconciliation.APPLY_BOTH;
import static dev.spud.shadowslave.appraisal.PreviewAppraisalService.Reconciliation.APPLY_IDENTITY_ONLY;
import static dev.spud.shadowslave.appraisal.PreviewAppraisalService.Reconciliation.APPLY_SOUL_ONLY;
import static dev.spud.shadowslave.appraisal.PreviewAppraisalService.Reconciliation.CONFLICT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewAppraisalServiceTest {
    private static final SoulData ASPIRANT = SoulData.uninfected().asCarrier().asAspirant();

    @Test
    void freshAspirantNeedsBothAppraisalSurfaces() {
        assertEquals(APPLY_BOTH, PreviewAppraisalService.classify(ASPIRANT, SoulIdentityData.empty()));
    }

    @Test
    void identitySavedFirstNeedsOnlySoulTransition() {
        assertEquals(
                APPLY_SOUL_ONLY,
                PreviewAppraisalService.classify(ASPIRANT, PreviewAppraisalService.expectedIdentity())
        );
    }

    @Test
    void soulSavedFirstNeedsOnlyIdentityRepair() {
        assertEquals(
                APPLY_IDENTITY_ONLY,
                PreviewAppraisalService.classify(expectedDreamerSoul(), SoulIdentityData.empty())
        );
    }

    @Test
    void exactAppliedStateIsIdempotent() {
        assertEquals(
                ALREADY_APPLIED,
                PreviewAppraisalService.classify(
                        expectedDreamerSoul(),
                        PreviewAppraisalService.expectedIdentity()
                )
        );
    }

    @Test
    void unrelatedIdentityFailsClosed() {
        assertEquals(CONFLICT, PreviewAppraisalService.classify(ASPIRANT, conflictingIdentity()));
    }

    @Test
    void unrelatedDreamerProgressionFailsClosed() {
        SoulData unrelatedDreamer = ASPIRANT.asDreamer(
                id("other/aspect"),
                SoulRank.DORMANT,
                id("other/flaw")
        );
        assertEquals(CONFLICT, PreviewAppraisalService.classify(unrelatedDreamer, SoulIdentityData.empty()));
        assertFalse(PreviewAppraisalService.hasExpectedSoul(unrelatedDreamer));
    }

    @Test
    void currentMultiAbilityIdentityShapeIsPartOfExactExpectedState() {
        SoulIdentityData expected = PreviewAppraisalService.expectedIdentity();
        AspectInstanceData aspect = expected.aspect().orElseThrow();

        assertEquals(1, aspect.abilitySet().abilities().size());
        assertEquals(PreviewAppraisalService.ABILITY_ID, aspect.abilitySet().abilities().getFirst().abilityId());
        assertTrue(PreviewAppraisalService.hasExpectedSoul(expectedDreamerSoul()));
    }

    private static SoulData expectedDreamerSoul() {
        return ASPIRANT.asDreamer(
                PreviewAppraisalService.ASPECT_ID,
                SoulRank.AWAKENED,
                PreviewAppraisalService.FLAW_ID
        );
    }

    private static SoulIdentityData conflictingIdentity() {
        SoulIdentityData expected = PreviewAppraisalService.expectedIdentity();
        AspectInstanceData aspect = expected.aspect().orElseThrow();
        FlawInstanceData flaw = expected.flaw().orElseThrow();
        AspectInstanceData conflictingAspect = new AspectInstanceData(
                aspect.instanceId(),
                aspect.formalName(),
                aspect.aspectRank(),
                aspect.natureId(),
                aspect.abilitySet(),
                "different_provenance"
        );
        return new SoulIdentityData(Optional.of(conflictingAspect), Optional.of(flaw));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
