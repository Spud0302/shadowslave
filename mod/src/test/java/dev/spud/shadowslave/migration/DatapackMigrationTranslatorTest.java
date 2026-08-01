package dev.spud.shadowslave.migration;

import dev.spud.shadowslave.soul.AwakeningPath;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SpellState;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatapackMigrationTranslatorTest {
    private static final UUID PLAYER_ID = UUID.fromString("12345678-1234-5678-1234-567812345678");

    @Test
    void untouchedPlayerDoesNotCreateAJavaMigration() {
        assertTrue(DatapackMigrationTranslator.translate(snapshot(false, 0, 0, 0, Set.of(), false, 0)).isEmpty());
    }

    @Test
    void carrierImportsWithoutInventingSoulRankOrIdentity() {
        DatapackMigrationPlan plan = DatapackMigrationTranslator.translate(
                snapshot(true, 0, 0, 0, Set.of("ss_carrier"), false, 0)
        ).orElseThrow();

        assertEquals(SpellState.CARRIER, plan.soulData().spellState());
        assertEquals(AwakeningPath.NIGHTMARE_SPELL, plan.soulData().awakeningPath());
        assertTrue(plan.soulData().soulRank().isEmpty());
        assertTrue(plan.aspect().isEmpty());
        assertTrue(plan.flaw().isEmpty());
        assertTrue(plan.soulData().importedFromDatapack());
        assertEquals(DatapackMigrationTranslator.CURRENT_MIGRATION, plan.soulData().migrationVersion());
    }

    @Test
    void generatedCompletedIdentityPreservesNamesFamiliesAndSeparateRanks() {
        DatapackMigrationPlan plan = DatapackMigrationTranslator.translate(
                snapshot(
                        true,
                        1,
                        12,
                        43,
                        Set.of("ss_carrier", "ss_aspect_shadow", "ss_flaw_weightless"),
                        false,
                        0
                )
        ).orElseThrow();

        ImportedAspect aspect = plan.aspect().orElseThrow();
        ImportedFlaw flaw = plan.flaw().orElseThrow();

        assertEquals(SpellState.DREAMER, plan.soulData().spellState());
        assertEquals(SoulRank.DORMANT, plan.soulData().soulRank().orElseThrow());
        assertEquals("Veiled Bearer", aspect.formalName());
        assertEquals("veiled", aspect.legacyNature());
        assertEquals("bearer", aspect.legacyArchetype());
        assertEquals(SoulRank.DORMANT, aspect.aspectRank());
        assertEquals("Shackled Pace", flaw.formalName());
        assertEquals("burdened_movement", flaw.legacyFamily());
        assertFalse(aspect.legacyPrototype());
        assertFalse(flaw.legacyPrototype());
        assertEquals(aspect.instanceId(), plan.soulData().aspectId().orElseThrow());
        assertEquals(flaw.instanceId(), plan.soulData().flawId().orElseThrow());
    }

    @Test
    void oldOneToFourScoresRequireAndPreserveCompatibilityTags() {
        Set<String> tags = Set.of(
                "ss_carrier",
                "ss_aspect_flame",
                "ss_flaw_weightless"
        );
        DatapackMigrationPlan plan = DatapackMigrationTranslator.translate(
                snapshot(true, 1, 2, 4, tags, false, 0)
        ).orElseThrow();

        assertEquals("Flame", plan.aspect().orElseThrow().formalName());
        assertEquals("Leadbound", plan.flaw().orElseThrow().formalName());
        assertTrue(plan.aspect().orElseThrow().legacyPrototype());
        assertTrue(plan.flaw().orElseThrow().legacyPrototype());

        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(true, 1, 2, 4, Set.of("ss_carrier"), false, 0)
                )
        );
    }

    @Test
    void generatedScoresRequireTheirMechanicsTags() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(true, 1, 12, 43, Set.of("ss_carrier"), false, 0)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(
                                true,
                                1,
                                12,
                                43,
                                Set.of("ss_carrier", "ss_aspect_shadow", "ss_flaw_ravenous"),
                                false,
                                0
                        )
                )
        );
    }

    @Test
    void completedPlayerRequiresCarrierEvidence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(
                                false,
                                1,
                                12,
                                43,
                                Set.of("ss_aspect_shadow", "ss_flaw_weightless"),
                                false,
                                0
                        )
                )
        );
    }

    @Test
    void activeNightmareFailsSafeBeforeAnyPlanExists() {
        assertThrows(
                IllegalStateException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(true, 0, 0, 0, Set.of("ss_carrier", "ss_in_nightmare"), true, 0)
                )
        );
    }

    @Test
    void incompleteOrUnsupportedStateIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(true, 1, 0, 11, Set.of("ss_carrier"), false, 0)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DatapackMigrationTranslator.translate(
                        snapshot(true, 2, 11, 11, Set.of("ss_carrier"), false, 0)
                )
        );
    }

    @Test
    void completedMigrationIsIdempotentAndDeterministic() {
        LegacyDatapackSnapshot source = snapshot(
                true,
                1,
                44,
                31,
                Set.of("ss_carrier", "ss_aspect_wind", "ss_flaw_ravenous"),
                false,
                0
        );
        DatapackMigrationPlan first = DatapackMigrationTranslator.translate(source).orElseThrow();
        DatapackMigrationPlan second = DatapackMigrationTranslator.translate(source).orElseThrow();

        assertEquals(first, second);
        assertEquals(
                Optional.empty(),
                DatapackMigrationTranslator.translate(
                        snapshot(
                                true,
                                1,
                                44,
                                31,
                                Set.of("ss_carrier", "ss_aspect_wind", "ss_flaw_ravenous"),
                                false,
                                DatapackMigrationTranslator.CURRENT_MIGRATION
                        )
                )
        );
    }

    private static LegacyDatapackSnapshot snapshot(
            boolean carrier,
            int rank,
            int aspect,
            int flaw,
            Set<String> tags,
            boolean activeNightmare,
            int migrationVersion
    ) {
        return new LegacyDatapackSnapshot(
                PLAYER_ID,
                carrier,
                rank,
                aspect,
                flaw,
                tags,
                activeNightmare,
                migrationVersion
        );
    }
}
