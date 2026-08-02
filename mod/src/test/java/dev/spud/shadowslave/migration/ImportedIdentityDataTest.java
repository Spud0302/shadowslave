package dev.spud.shadowslave.migration;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportedIdentityDataTest {
    private static final ImportedAspect ASPECT = new ImportedAspect(
            id("import/datapack/test/aspect_12"),
            "Veiled Bearer",
            SoulRank.DORMANT,
            "veiled",
            "bearer",
            id("prototype/aspect_root/shadow"),
            12,
            false
    );
    private static final ImportedFlaw FLAW = new ImportedFlaw(
            id("import/datapack/test/flaw_43"),
            "Shackled Pace",
            "burdened_movement",
            id("prototype/flaw_effect/burdened"),
            43,
            false
    );

    @Test
    void codecRoundTripsFullImportedIdentity() {
        ImportedIdentityData original = new ImportedIdentityData(Optional.of(ASPECT), Optional.of(FLAW));

        JsonElement encoded = ImportedIdentityData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        ImportedIdentityData decoded = ImportedIdentityData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
    }

    @Test
    void halfStoredImportedIdentityReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "aspect": {
                    "instance_id": "shadowslave:import/datapack/test/aspect_12",
                    "formal_name": "Veiled Bearer",
                    "aspect_rank": "dormant",
                    "legacy_nature": "veiled",
                    "legacy_archetype": "bearer",
                    "legacy_mechanical_root": "shadowslave:prototype/aspect_root/shadow",
                    "source_score": 12,
                    "legacy_prototype": false
                  }
                }
                """);

        DataResult<ImportedIdentityData> result = assertDoesNotThrow(
                () -> ImportedIdentityData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void nonPositiveStoredImportedAspectScoreReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:import/datapack/test/aspect_12",
                  "formal_name": "Veiled Bearer",
                  "aspect_rank": "dormant",
                  "legacy_nature": "veiled",
                  "legacy_archetype": "bearer",
                  "legacy_mechanical_root": "shadowslave:prototype/aspect_root/shadow",
                  "source_score": 0,
                  "legacy_prototype": false
                }
                """);

        DataResult<ImportedAspect> result = assertDoesNotThrow(
                () -> ImportedAspect.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void nonPositiveStoredImportedFlawScoreReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:import/datapack/test/flaw_43",
                  "formal_name": "Shackled Pace",
                  "legacy_family": "burdened_movement",
                  "effect_id": "shadowslave:prototype/flaw_effect/burdened",
                  "source_score": 0,
                  "legacy_prototype": false
                }
                """);

        DataResult<ImportedFlaw> result = assertDoesNotThrow(
                () -> ImportedFlaw.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void halfAnImportedIdentityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportedIdentityData(Optional.of(ASPECT), Optional.empty())
        );
    }

    @Test
    void nestedConstructorsStillRejectNonPositiveScores() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportedAspect(
                        ASPECT.instanceId(),
                        ASPECT.formalName(),
                        ASPECT.aspectRank(),
                        ASPECT.legacyNature(),
                        ASPECT.legacyArchetype(),
                        ASPECT.legacyMechanicalRoot(),
                        0,
                        ASPECT.legacyPrototype()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportedFlaw(
                        FLAW.instanceId(),
                        FLAW.formalName(),
                        FLAW.legacyFamily(),
                        FLAW.effectId(),
                        0,
                        FLAW.legacyPrototype()
                )
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
