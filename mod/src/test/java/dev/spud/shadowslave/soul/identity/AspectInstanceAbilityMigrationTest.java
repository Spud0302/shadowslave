package dev.spud.shadowslave.soul.identity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectInstanceAbilityMigrationTest {
    @Test
    void legacyAbilityIdDecodesWithoutInventingKindOrRank() {
        JsonElement legacy = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:aspect/last_light",
                  "formal_name": "Last Light",
                  "aspect_rank": "awakened",
                  "nature_id": "shadowslave:nature/last_light",
                  "ability_id": "shadowslave:ability/kindle",
                  "provenance": "fixed_preview"
                }
                """);

        AspectInstanceData decoded = AspectInstanceData.CODEC.codec()
                .parse(JsonOps.INSTANCE, legacy)
                .getOrThrow();

        AspectAbilityData migrated = decoded.abilitySet().abilities().getFirst();
        assertEquals(id("ability/kindle"), migrated.abilityId());
        assertEquals(AspectAbilityKind.LEGACY_UNCLASSIFIED, migrated.kind());
        assertTrue(migrated.acquisitionRank().isEmpty());
    }

    @Test
    void migratedEncodingUsesAbilitySetAndDropsLegacyField() {
        AspectInstanceData original = new AspectInstanceData(
                id("aspect/example"),
                "Example",
                SoulRank.ASCENDED,
                id("nature/example"),
                new AspectAbilitySetData(List.of(
                        AspectAbilityData.innate(id("ability/innate"), "test"),
                        AspectAbilityData.rankGranted(id("ability/dormant"), SoulRank.DORMANT, "test")
                )),
                "test"
        );

        JsonObject encoded = AspectInstanceData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow()
                .getAsJsonObject();

        assertTrue(encoded.has("abilities"));
        assertFalse(encoded.has("ability_id"));
        assertEquals(original, AspectInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void ambiguousOrMissingAbilityStorageFailsClosed() {
        JsonElement both = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:aspect/example",
                  "formal_name": "Example",
                  "aspect_rank": "awakened",
                  "nature_id": "shadowslave:nature/example",
                  "ability_id": "shadowslave:ability/legacy",
                  "abilities": [{
                    "ability_id": "shadowslave:ability/new",
                    "kind": "innate",
                    "provenance": "test"
                  }],
                  "provenance": "test"
                }
                """);
        JsonElement neither = JsonParser.parseString("""
                {
                  "instance_id": "shadowslave:aspect/example",
                  "formal_name": "Example",
                  "aspect_rank": "awakened",
                  "nature_id": "shadowslave:nature/example",
                  "provenance": "test"
                }
                """);

        DataResult<AspectInstanceData> bothResult = AspectInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, both);
        DataResult<AspectInstanceData> neitherResult = AspectInstanceData.CODEC.codec().parse(JsonOps.INSTANCE, neither);

        assertTrue(bothResult.error().isPresent());
        assertTrue(neitherResult.error().isPresent());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
