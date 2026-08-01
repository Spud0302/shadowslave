package dev.spud.shadowslave.soul;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoulDataTest {
    private static final ResourceLocation ASPECT =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/veiled_witness");
    private static final ResourceLocation FLAW =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/heavy_step");

    @Test
    void defaultsToUninfectedWithoutInventingRankZero() {
        SoulData soul = SoulData.uninfected();

        assertEquals(SoulData.CURRENT_SCHEMA, soul.schemaVersion());
        assertEquals(SpellState.UNINFECTED, soul.spellState());
        assertEquals(AwakeningPath.UNDECIDED, soul.awakeningPath());
        assertTrue(soul.soulRank().isEmpty());
        assertTrue(soul.aspectId().isEmpty());
        assertTrue(soul.aspectRank().isEmpty());
        assertTrue(soul.flawId().isEmpty());
        assertFalse(soul.importedFromDatapack());
    }

    @Test
    void firstNightmareHasCarrierAspirantAndDreamerBoundaries() {
        SoulData carrier = SoulTransitions.infect(SoulData.uninfected());
        assertEquals(SpellState.CARRIER, carrier.spellState());
        assertEquals(AwakeningPath.NIGHTMARE_SPELL, carrier.awakeningPath());
        assertTrue(carrier.soulRank().isEmpty());

        SoulData aspirant = SoulTransitions.beginFirstNightmare(carrier);
        assertEquals(SpellState.ASPIRANT, aspirant.spellState());
        assertEquals(SoulRank.DORMANT, aspirant.soulRank().orElseThrow());
        assertTrue(aspirant.aspectId().isEmpty());
        assertTrue(aspirant.flawId().isEmpty());

        SoulData dreamer = SoulTransitions.completeFirstNightmare(
                aspirant,
                ASPECT,
                SoulRank.DIVINE,
                FLAW
        );
        assertEquals(SpellState.DREAMER, dreamer.spellState());
        assertEquals(SoulRank.DORMANT, dreamer.soulRank().orElseThrow());
        assertEquals(ASPECT, dreamer.aspectId().orElseThrow());
        assertEquals(SoulRank.DIVINE, dreamer.aspectRank().orElseThrow());
        assertEquals(FLAW, dreamer.flawId().orElseThrow());

        assertThrows(
                IllegalStateException.class,
                () -> SoulTransitions.completeFirstNightmare(carrier, ASPECT, SoulRank.DORMANT, FLAW)
        );
    }

    @Test
    void codecRoundTripsImportedIdentity() {
        SoulData original = completedDreamer().markImported(1);

        JsonElement encoded = SoulData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        SoulData decoded = SoulData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
    }

    @Test
    void schemaOneSleeperMigratesToDreamerWithSeparateAspectRank() {
        JsonElement legacy = JsonParser.parseString("""
                {
                  "schema_version": 1,
                  "spell_state": "sleeper",
                  "soul_rank": "dormant",
                  "aspect_id": "shadowslave:prototype/veiled_witness",
                  "flaw_id": "shadowslave:prototype/heavy_step",
                  "imported_from_datapack": true,
                  "migration_version": 1
                }
                """);

        SoulData migrated = SoulData.CODEC.codec()
                .parse(JsonOps.INSTANCE, legacy)
                .getOrThrow();

        assertEquals(SoulData.CURRENT_SCHEMA, migrated.schemaVersion());
        assertEquals(SpellState.DREAMER, migrated.spellState());
        assertEquals(AwakeningPath.NIGHTMARE_SPELL, migrated.awakeningPath());
        assertEquals(SoulRank.DORMANT, migrated.soulRank().orElseThrow());
        assertEquals(SoulRank.DORMANT, migrated.aspectRank().orElseThrow());
        assertTrue(migrated.importedFromDatapack());
    }

    @Test
    void schemaOneMundaneRankMigratesToNoRank() {
        JsonElement legacy = JsonParser.parseString("""
                {
                  "schema_version": 1,
                  "spell_state": "mundane",
                  "soul_rank": "mundane"
                }
                """);

        SoulData migrated = SoulData.CODEC.codec()
                .parse(JsonOps.INSTANCE, legacy)
                .getOrThrow();

        assertEquals(SoulData.CURRENT_SCHEMA, migrated.schemaVersion());
        assertEquals(SpellState.UNINFECTED, migrated.spellState());
        assertEquals(AwakeningPath.UNDECIDED, migrated.awakeningPath());
        assertTrue(migrated.soulRank().isEmpty());
    }

    @Test
    void malformedStoredSoulReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "schema_version": 2,
                  "spell_state": "carrier",
                  "awakening_path": "nightmare_spell",
                  "soul_rank": "dormant"
                }
                """);

        DataResult<SoulData> result = assertDoesNotThrow(
                () -> SoulData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void invalidAndFutureStoredSchemasAreRejected() {
        for (int schema : new int[]{-5, 0, SoulData.CURRENT_SCHEMA + 1}) {
            JsonElement stored = JsonParser.parseString("""
                    {
                      "schema_version": %d,
                      "spell_state": "uninfected"
                    }
                    """.formatted(schema));

            DataResult<SoulData> result = assertDoesNotThrow(
                    () -> SoulData.CODEC.codec().parse(JsonOps.INSTANCE, stored)
            );
            assertTrue(result.error().isPresent(), "schema " + schema + " must be rejected");
        }
    }

    @Test
    void schemaTwoDoesNotSilentlyApplySchemaOneRepairs() {
        JsonElement malformedCurrent = JsonParser.parseString("""
                {
                  "schema_version": 2,
                  "spell_state": "dreamer",
                  "awakening_path": "nightmare_spell",
                  "soul_rank": "dormant",
                  "aspect_id": "shadowslave:prototype/veiled_witness",
                  "flaw_id": "shadowslave:prototype/heavy_step"
                }
                """);

        DataResult<SoulData> result = assertDoesNotThrow(
                () -> SoulData.CODEC.codec().parse(JsonOps.INSTANCE, malformedCurrent)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void everyLaterNightmareSpellStageRetainsAppraisedIdentity() {
        SpellState[] postFirstNightmare = {
                SpellState.DREAMER,
                SpellState.AWAKENED,
                SpellState.MASTER,
                SpellState.SAINT,
                SpellState.SOVEREIGN,
                SpellState.SPIRIT,
                SpellState.GOD
        };

        for (SpellState state : postFirstNightmare) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new SoulData(
                            SoulData.CURRENT_SCHEMA,
                            state,
                            AwakeningPath.NIGHTMARE_SPELL,
                            state.requiredSoulRank(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            false,
                            SoulData.NO_MIGRATION
                    ),
                    state + " must not lose its appraised identity"
            );
        }

        assertDoesNotThrow(() -> new SoulData(
                SoulData.CURRENT_SCHEMA,
                SpellState.AWAKENED,
                AwakeningPath.NIGHTMARE_SPELL,
                SpellState.AWAKENED.requiredSoulRank(),
                Optional.of(ASPECT),
                Optional.of(SoulRank.AWAKENED),
                Optional.of(FLAW),
                false,
                SoulData.NO_MIGRATION
        ));
    }

    private static SoulData completedDreamer() {
        return SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(
                        SoulTransitions.infect(SoulData.uninfected())
                ),
                ASPECT,
                SoulRank.DORMANT,
                FLAW
        );
    }
}
