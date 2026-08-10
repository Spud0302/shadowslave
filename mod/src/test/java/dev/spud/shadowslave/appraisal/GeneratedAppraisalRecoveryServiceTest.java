package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.echo.EchoOwnershipData;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.nightmare.NightmareCompletionReceiptData;
import dev.spud.shadowslave.nightmare.NightmareInstance;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedAppraisalRecoveryServiceTest {
    @Test
    void untouchedAspirantConvergesToExactStoredAwardWithoutGeneratorReplay() {
        GeneratedAppraisalRecoverySnapshot snapshot = snapshot("flood_diverted");
        GeneratedAppraisalRecoveryService.PlayerState current = emptyAspirant();

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(current, snapshot);

        assertFalse(plan.alreadyComplete());
        assertEquals(snapshot.identity(), plan.target().identity());
        assertEquals(List.of(snapshot.attribute()), plan.target().attributes().attributes());
        assertEquals(List.of(snapshot.memory()), plan.target().memories().memories());
        assertEquals(snapshot.echo(), plan.target().echoes().echoes().getFirst());
        assertEquals(snapshot.identity().aspect().orElseThrow().instanceId(), plan.target().soul().aspectId().orElseThrow());
        assertEquals(snapshot.identity().flaw().orElseThrow().instanceId(), plan.target().soul().flawId().orElseThrow());
    }

    @Test
    void partiallyCommittedAttachmentsConvergeWithoutDuplicatingOwnership() {
        GeneratedAppraisalRecoverySnapshot snapshot = snapshot("tower_held");
        GeneratedAppraisalRecoveryService.PlayerState current = new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                snapshot.identity(),
                new AttributeOwnershipData(List.of(snapshot.attribute())),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(current, snapshot);

        assertFalse(plan.alreadyComplete());
        assertEquals(1, plan.target().attributes().attributes().size());
        assertEquals(1, plan.target().memories().memories().size());
        assertEquals(1, plan.target().echoes().echoes().size());
    }

    @Test
    void fullyCommittedMatchingDreamerIsIdempotent() {
        GeneratedAppraisalRecoverySnapshot snapshot = snapshot("villagers_evacuated");
        SoulData aspirant = aspirantSoul();
        var aspect = snapshot.identity().aspect().orElseThrow();
        var flaw = snapshot.identity().flaw().orElseThrow();
        SoulData dreamer = aspirant.asDreamer(aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
        GeneratedAppraisalRecoveryService.PlayerState current = new GeneratedAppraisalRecoveryService.PlayerState(
                dreamer,
                snapshot.identity(),
                new AttributeOwnershipData(List.of(snapshot.attribute())),
                new MemoryOwnershipData(List.of(snapshot.memory())),
                new EchoOwnershipData(List.of(snapshot.echo()))
        );

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(current, snapshot);

        assertTrue(plan.alreadyComplete());
        assertEquals(current, plan.target());
    }

    @Test
    void contradictoryPartialStateFailsClosed() {
        GeneratedAppraisalRecoverySnapshot expected = snapshot("flood_diverted");
        GeneratedAppraisalRecoverySnapshot contradictory = snapshot("quarry_collapsed");
        GeneratedAppraisalRecoveryService.PlayerState current = new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                contradictory.identity(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );

        assertThrows(IllegalStateException.class,
                () -> GeneratedAppraisalRecoveryService.plan(current, expected));
    }

    @Test
    void unrelatedSpellStateCannotConsumeCompletionAuthority() {
        GeneratedAppraisalRecoverySnapshot snapshot = snapshot("tower_held");
        GeneratedAppraisalRecoveryService.PlayerState current = new GeneratedAppraisalRecoveryService.PlayerState(
                SoulTransitions.infect(SoulData.uninfected()),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );

        assertThrows(IllegalStateException.class,
                () -> GeneratedAppraisalRecoveryService.plan(current, snapshot));
    }

    @Test
    void matchingActiveNightmareIsSelectedForSuccessfulReplayTeardown() {
        NightmareInstance instance = instance("flood_diverted", new UUID(211L, 223L), new UUID(227L, 229L));
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, "flood_diverted")
        );
        NightmareCompletionReceiptData.Receipt receipt = new NightmareCompletionReceiptData.Receipt(instance, snapshot);

        assertEquals(
                Optional.of(instance),
                GeneratedAppraisalRecoveryService.activeInstanceForReplay(Optional.of(instance), receipt)
        );
        assertTrue(GeneratedAppraisalRecoveryService.activeInstanceForReplay(Optional.empty(), receipt).isEmpty());
    }

    @Test
    void contradictoryActiveNightmareCannotBeConsumedByReceiptReplay() {
        NightmareInstance expected = instance("tower_held", new UUID(233L, 239L), new UUID(241L, 251L));
        NightmareInstance contradictory = instance("tower_held", new UUID(257L, 263L), expected.playerId());
        GeneratedAppraisalRecoverySnapshot snapshot = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(expected, "tower_held")
        );
        NightmareCompletionReceiptData.Receipt receipt = new NightmareCompletionReceiptData.Receipt(expected, snapshot);

        assertThrows(
                IllegalStateException.class,
                () -> GeneratedAppraisalRecoveryService.activeInstanceForReplay(Optional.of(contradictory), receipt)
        );
    }

    private static GeneratedAppraisalRecoveryService.PlayerState emptyAspirant() {
        return new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                SoulIdentityData.empty(),
                AttributeOwnershipData.empty(),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );
    }

    private static SoulData aspirantSoul() {
        return SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
    }

    private static GeneratedAppraisalRecoverySnapshot snapshot(String resolutionId) {
        NightmareInstance instance = instance(resolutionId, new UUID(211L, 223L), new UUID(227L, 229L));
        return GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, resolutionId)
        );
    }

    private static NightmareInstance instance(String resolutionId, UUID instanceId, UUID playerId) {
        return new NightmareInstance(
                instanceId,
                playerId,
                2,
                "drowned_bell",
                "cistern_keeper",
                ResourceLocation.parse("minecraft:overworld"),
                12.5,
                70.0,
                -4.5,
                15.0F,
                0.0F,
                new BlockPos(0, 64, 0),
                new BlockPos(3, 64, 3),
                Optional.empty(),
                100L,
                Optional.of("resolved"),
                Optional.of(resolutionId)
        );
    }
}
