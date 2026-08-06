package dev.spud.shadowslave.soul.identity;

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

class SoulIdentityDataTest {
    @Test
    void codecRoundTripsRevealedIdentity() {
        SoulIdentityData original = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        id("preview/aspect/last_light"),
                        "Last Light",
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        id("preview/ability/kindle"),
                        "preview_appraisal_design"
                )),
                Optional.of(new FlawInstanceData(
                        id("preview/flaw/cold_ash"),
                        "Cold Ash",
                        id("preview/flaw_effect/cold_ash"),
                        "preview_appraisal_design"
                ))
        );

        JsonElement encoded = SoulIdentityData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        SoulIdentityData decoded = SoulIdentityData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
    }

    @Test
    void codecRoundTripsIdentityBeforeFormalNamesAreRevealed() {
        SoulIdentityData original = new SoulIdentityData(
                Optional.of(new AspectInstanceData(
                        id("natural/aspect/rain"),
                        Optional.empty(),
                        SoulRank.SACRED,
                        id("nature/unresolved"),
                        id("ability/unresolved"),
                        "natural_awakening_observation"
                )),
                Optional.of(new FlawInstanceData(
                        id("natural/flaw/rain"),
                        Optional.empty(),
                        id("flaw_effect/unresolved"),
                        "natural_awakening_observation"
                ))
        );

        JsonElement encoded = SoulIdentityData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        SoulIdentityData decoded = SoulIdentityData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
        assertTrue(!encoded.toString().contains("formal_name"),
                "an unrevealed name must be absent rather than serialized as invented text");
    }

    @Test
    void legacyStoredNamesRemainReadable() {
        JsonElement stored = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:preview/aspect/last_light",
                  "formal_name": "Last Light",
                  "aspect_rank": "awakened",
                  "nature_id": "shadowslave:preview/nature/ember_resolve",
                  "ability_id": "shadowslave:preview/ability/kindle",
                  "provenance": "test"
                }
                """);

        AspectInstanceData decoded = AspectInstanceData.CODEC.codec()
                .parse(JsonOps.INSTANCE, stored)
                .getOrThrow();

        assertEquals(Optional.of("Last Light"), decoded.formalName());
    }

    @Test
    void halfStoredIdentityReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "aspect": {
                    "instance_id": "shadowslave:preview/aspect/last_light",
                    "formal_name": "Last Light",
                    "aspect_rank": "awakened",
                    "nature_id": "shadowslave:preview/nature/ember_resolve",
                    "ability_id": "shadowslave:preview/ability/kindle",
                    "provenance": "test"
                  }
                }
                """);

        DataResult<SoulIdentityData> result = assertDoesNotThrow(
                () -> SoulIdentityData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void blankStoredAspectNameReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:preview/aspect/last_light",
                  "formal_name": "   ",
                  "aspect_rank": "awakened",
                  "nature_id": "shadowslave:preview/nature/ember_resolve",
                  "ability_id": "shadowslave:preview/ability/kindle",
                  "provenance": "test"
                }
                """);

        DataResult<AspectInstanceData> result = assertDoesNotThrow(
                () -> AspectInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void blankStoredFlawProvenanceReturnsDataErrorInsteadOfThrowing() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:preview/flaw/cold_ash",
                  "formal_name": "Cold Ash",
                  "effect_id": "shadowslave:preview/flaw_effect/cold_ash",
                  "provenance": "   "
                }
                """);

        DataResult<FlawInstanceData> result = assertDoesNotThrow(
                () -> FlawInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, malformed)
        );

        assertTrue(result.error().isPresent());
    }

    @Test
    void aspectAndFlawMustBeRevealedTogether() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SoulIdentityData(
                        Optional.of(new AspectInstanceData(
                                id("preview/aspect/last_light"),
                                "Last Light",
                                SoulRank.AWAKENED,
                                id("preview/nature/ember_resolve"),
                                id("preview/ability/kindle"),
                                "test"
                        )),
                        Optional.empty()
                )
        );
    }

    @Test
    void nestedConstructorsStillRejectBlankText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AspectInstanceData(
                        id("preview/aspect/last_light"),
                        "   ",
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        id("preview/ability/kindle"),
                        "test"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FlawInstanceData(
                        id("preview/flaw/cold_ash"),
                        "Cold Ash",
                        id("preview/flaw_effect/cold_ash"),
                        "   "
                )
        );
    }

    @Test
    void compatibilityConstructorsRejectNullInsteadOfInventingUnrevealedState() {
        assertThrows(
                NullPointerException.class,
                () -> new AspectInstanceData(
                        id("preview/aspect/last_light"),
                        (String) null,
                        SoulRank.AWAKENED,
                        id("preview/nature/ember_resolve"),
                        id("preview/ability/kindle"),
                        "test"
                )
        );
        assertThrows(
                NullPointerException.class,
                () -> new FlawInstanceData(
                        id("preview/flaw/cold_ash"),
                        (String) null,
                        id("preview/flaw_effect/cold_ash"),
                        "test"
                )
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
