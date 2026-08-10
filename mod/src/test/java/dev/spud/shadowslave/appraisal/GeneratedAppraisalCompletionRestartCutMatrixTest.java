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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic reconstruction matrix for persisted cuts in the successful-completion transaction.
 *
 * <p>Each test reconstructs the appraisal snapshot through its NBT codec first. That deliberately
 * models a new process reading durable receipt authority rather than retaining an in-memory generator
 * result. The matrix then proves the recovery planner converges the exact stored award without
 * duplicating player-owned records, whether active Nightmare ownership survived the cut or teardown
 * had already reached durable storage.</p>
 */
class GeneratedAppraisalCompletionRestartCutMatrixTest {
    @Test
    void restartAfterReceiptWriteBeforeTeardownReplaysExactAwardAndSelectsOwnedTeardown() {
        NightmareInstance instance = instance("flood_diverted");
        NightmareCompletionReceiptData.Receipt receipt = reloadedReceipt(instance);

        assertEquals(
                Optional.of(instance),
                GeneratedAppraisalRecoveryService.activeInstanceForReplay(Optional.of(instance), receipt)
        );

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(
                emptyAspirant(), receipt.appraisal());
        assertExactSingleAward(plan.target(), receipt.appraisal());
        assertFalse(plan.alreadyComplete());
    }

    @Test
    void restartAfterTeardownBeforePlayerCommitReplaysExactAwardWithoutRequiringActiveOwnership() {
        NightmareInstance instance = instance("tower_held");
        NightmareCompletionReceiptData.Receipt receipt = reloadedReceipt(instance);

        assertTrue(GeneratedAppraisalRecoveryService.activeInstanceForReplay(Optional.empty(), receipt).isEmpty());

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(
                emptyAspirant(), receipt.appraisal());
        assertExactSingleAward(plan.target(), receipt.appraisal());
        assertFalse(plan.alreadyComplete());
    }

    @Test
    void restartAfterPartialPlayerWriteFillsOnlyMissingAwardState() {
        NightmareCompletionReceiptData.Receipt receipt = reloadedReceipt(instance("villagers_evacuated"));
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();
        GeneratedAppraisalRecoveryService.PlayerState partial = new GeneratedAppraisalRecoveryService.PlayerState(
                aspirantSoul(),
                snapshot.identity(),
                new AttributeOwnershipData(List.of(snapshot.attribute())),
                MemoryOwnershipData.empty(),
                EchoOwnershipData.empty()
        );

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(partial, snapshot);

        assertExactSingleAward(plan.target(), snapshot);
        assertFalse(plan.alreadyComplete());
    }

    @Test
    void restartAfterCompletePlayerWriteBeforeReceiptDeletionIsIdempotent() {
        NightmareCompletionReceiptData.Receipt receipt = reloadedReceipt(instance("quarry_collapsed"));
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();
        GeneratedAppraisalRecoveryService.PlayerState complete = completeDreamer(snapshot);

        GeneratedAppraisalRecoveryService.RecoveryPlan plan = GeneratedAppraisalRecoveryService.plan(complete, snapshot);

        assertTrue(plan.alreadyComplete());
        assertEquals(complete, plan.target());
        assertExactSingleAward(plan.target(), snapshot);
    }

    @Test
    void repeatedRestartPlanningCannotDuplicateGeneratedOwnership() {
        NightmareCompletionReceiptData.Receipt receipt = reloadedReceipt(instance("signal_preserved"));
        GeneratedAppraisalRecoverySnapshot snapshot = receipt.appraisal();

        GeneratedAppraisalRecoveryService.PlayerState once = GeneratedAppraisalRecoveryService.plan(
                emptyAspirant(), snapshot).target();
        GeneratedAppraisalRecoveryService.RecoveryPlan twice = GeneratedAppraisalRecoveryService.plan(once, snapshot);

        assertTrue(twice.alreadyComplete());
        assertEquals(once, twice.target());
        assertExactSingleAward(twice.target(), snapshot);
    }

    private static NightmareCompletionReceiptData.Receipt reloadedReceipt(NightmareInstance instance) {
        GeneratedAppraisalRecoverySnapshot prepared = GeneratedAppraisalRecoverySnapshot.fromPrepared(
                PreviewAppraisalService.prepareWithRewards(instance, instance.terminalResolutionId().orElseThrow())
        );
        GeneratedAppraisalRecoverySnapshot loaded = GeneratedAppraisalRecoverySnapshot.load(prepared.save());
        return new NightmareCompletionReceiptData.Receipt(instance, loaded);
    }

    private static void assertExactSingleAward(
            GeneratedAppraisalRecoveryService.PlayerState state,
            GeneratedAppraisalRecoverySnapshot snapshot
    ) {
        assertEquals(snapshot.identity(), state.identity());
        assertEquals(List.of(snapshot.attribute()), state.attributes().attributes());
        assertEquals(List.of(snapshot.memory()), state.memories().memories());
        assertEquals(List.of(snapshot.echo()), state.echoes().echoes());
        assertEquals(snapshot.identity().aspect().orElseThrow().instanceId(), state.soul().aspectId().orElseThrow());
        assertEquals(snapshot.identity().flaw().orElseThrow().instanceId(), state.soul().flawId().orElseThrow());
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

    private static GeneratedAppraisalRecoveryService.PlayerState completeDreamer(
            GeneratedAppraisalRecoverySnapshot snapshot
    ) {
        var aspect = snapshot.identity().aspect().orElseThrow();
        var flaw = snapshot.identity().flaw().orElseThrow();
        SoulData dreamer = aspirantSoul().asDreamer(aspect.instanceId(), aspect.aspectRank(), flaw.instanceId());
        return new GeneratedAppraisalRecoveryService.PlayerState(
                dreamer,
                snapshot.identity(),
                new AttributeOwnershipData(List.of(snapshot.attribute())),
                new MemoryOwnershipData(List.of(snapshot.memory())),
                new EchoOwnershipData(List.of(snapshot.echo()))
        );
    }

    private static SoulData aspirantSoul() {
        return SoulTransitions.beginFirstNightmare(SoulTransitions.infect(SoulData.uninfected()));
    }

    private static NightmareInstance instance(String resolutionId) {
        return new NightmareInstance(
                new UUID(401L, 409L),
                new UUID(419L, 421L),
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
