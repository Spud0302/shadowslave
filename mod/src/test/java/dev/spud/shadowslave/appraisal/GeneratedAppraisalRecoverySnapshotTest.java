package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.appraisal.generation.GeneratedIdentityCandidate;
import dev.spud.shadowslave.echo.EchoInstanceData;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.AttributeInstanceData;
import dev.spud.shadowslave.soul.identity.FlawInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratedAppraisalRecoverySnapshotTest {
    @Test
    void exactCommittedAwardRoundTripsWithoutGeneratorOrCatalogueLookup() {
        NightmareInstance completed = instance(new UUID(41L, 73L), "drowned_bell", "cistern_keeper");
        FirstNightmareAppraisalResolver.Award award = FirstNightmareAppraisalResolver.resolve(completed, "flood_diverted");
        PreviewAppraisalService.CommittedAppraisal committed = committed(award, completed, "flood_diverted");

        GeneratedAppraisalRecoverySnapshot before = GeneratedAppraisalRecoverySnapshot.fromCommitted(committed);
        GeneratedAppraisalRecoverySnapshot after = GeneratedAppraisalRecoverySnapshot.load(before.save());

        assertEquals(before, after);
        assertEquals(committed.identity(), after.identity());
        assertEquals(committed.attribute(), after.attribute());
        assertEquals(committed.memory(), after.memory());
        assertEquals(committed.echo(), after.echo());
        assertEquals(award.identity().generatorVersion(), after.generatorVersion());
        assertEquals(award.identity().seed(), after.generatorSeed());
        assertEquals(award.identity().generationFingerprint(), after.generationFingerprint());
    }

    @Test
    void snapshotRetainsTheResolvedAwardWhenAnotherResolutionWouldGenerateDifferently() {
        NightmareInstance completed = instance(new UUID(101L, 203L), "drowned_bell", "ferry_deckhand");
        FirstNightmareAppraisalResolver.Award tower = FirstNightmareAppraisalResolver.resolve(completed, "tower_held");
        FirstNightmareAppraisalResolver.Award evacuation = FirstNightmareAppraisalResolver.resolve(completed, "villagers_evacuated");
        GeneratedAppraisalRecoverySnapshot stored = GeneratedAppraisalRecoverySnapshot.fromCommitted(
                committed(tower, completed, "tower_held")
        );

        assertNotEquals(tower.identity().generationFingerprint(), evacuation.identity().generationFingerprint());
        assertEquals(tower.identity().generationFingerprint(), stored.generationFingerprint());
        assertEquals(tower.identity().aspect().instanceId(), stored.identity().aspect().orElseThrow().instanceId());
        assertEquals(tower.identity().flaw().instanceId(), stored.identity().flaw().orElseThrow().instanceId());
    }

    @Test
    void malformedOrIncompleteRecoveryPayloadFailsClosed() {
        NightmareInstance completed = instance(new UUID(17L, 19L), "last_signal", "last_watchkeeper");
        FirstNightmareAppraisalResolver.Award award = FirstNightmareAppraisalResolver.resolve(completed, "signal_restored");
        GeneratedAppraisalRecoverySnapshot stored = GeneratedAppraisalRecoverySnapshot.fromCommitted(
                committed(award, completed, "signal_restored")
        );
        CompoundTag malformed = (CompoundTag) stored.save();
        malformed.remove("identity");

        assertThrows(RuntimeException.class, () -> GeneratedAppraisalRecoverySnapshot.load(malformed));
    }

    private static PreviewAppraisalService.CommittedAppraisal committed(
            FirstNightmareAppraisalResolver.Award award,
            NightmareInstance completed,
            String resolutionId
    ) {
        GeneratedIdentityCandidate generated = award.identity();
        GeneratedIdentityCandidate.Aspect generatedAspect = generated.aspect();
        GeneratedIdentityCandidate.Flaw generatedFlaw = generated.flaw();
        SoulIdentityData identity = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        generatedAspect.instanceId(),
                        generatedAspect.formalName(),
                        generatedAspect.aspectRank(),
                        generatedAspect.natureId(),
                        new AspectAbilitySetData(List.of(AspectAbilityData.legacyUnclassified(
                                generatedAspect.abilityId(),
                                "generated First-Nightmare Dormant ability; classification integration pending"
                        ))),
                        generated.provenance()
                )),
                Optional.of(new FlawInstanceData(
                        generatedFlaw.instanceId(),
                        generatedFlaw.formalName(),
                        generatedFlaw.effectId(),
                        generated.provenance()
                ))
        );
        AttributeInstanceData attribute = new AttributeInstanceData(
                award.attribute().id(),
                award.attribute().formalName(),
                award.attribute().origin().name().toLowerCase(Locale.ROOT),
                award.attribute().visibility().name().toLowerCase(Locale.ROOT),
                generated.provenance() + "/attribute-selection"
        );
        String provenance = "nightmare/" + completed.instanceId() + "/resolution/" + resolutionId;
        MemoryInstanceData memory = new MemoryInstanceData(
                ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/ash_compass"),
                "Ash Compass",
                "first_nightmare_appraisal_design",
                provenance
        );
        EchoInstanceData echo = new EchoInstanceData(
                ResourceLocation.fromNamespaceAndPath("shadowslave", "echo/ash_burrower"),
                "Ash Burrower",
                "first_nightmare_appraisal_design",
                provenance,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        return new PreviewAppraisalService.CommittedAppraisal(award, identity, attribute, memory, echo);
    }

    private static NightmareInstance instance(UUID id, String scenario, String role) {
        return new NightmareInstance(
                id,
                new UUID(91L, 92L),
                0,
                scenario,
                role,
                ResourceLocation.parse("minecraft:overworld"),
                0.5,
                64.0,
                0.5,
                0.0F,
                0.0F,
                BlockPos.ZERO,
                BlockPos.ZERO,
                Optional.empty(),
                1L
        );
    }
}
