package dev.spud.shadowslave.soul.identity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectAbilitySetDataTest {
    @Test
    void codecRoundTripsInnateAndRankGrantedAbilitiesInStableOrder() {
        AspectAbilitySetData original = new AspectAbilitySetData(List.of(
                AspectAbilityData.innate(id("ability/shadow_bond"), "canon_identity"),
                AspectAbilityData.rankGranted(id("ability/shadow_control"), SoulRank.DORMANT, "canon_identity"),
                AspectAbilityData.rankGranted(id("ability/shadow_step"), SoulRank.AWAKENED, "canon_identity")
        ));

        JsonElement encoded = AspectAbilitySetData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        AspectAbilitySetData decoded = AspectAbilitySetData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(original, decoded);
        assertTrue(decoded.contains(id("ability/shadow_step")));
    }

    @Test
    void duplicateAbilityIdsAreRejected() {
        AspectAbilityData ability = AspectAbilityData.innate(id("ability/shared"), "test");
        assertThrows(IllegalArgumentException.class, () -> new AspectAbilitySetData(List.of(ability, ability)));
    }

    @Test
    void abilityKindAndRankMustAgree() {
        assertThrows(IllegalArgumentException.class, () -> new AspectAbilityData(
                id("ability/invalid"), AspectAbilityKind.INNATE, Optional.of(SoulRank.DORMANT), "test"
        ));
        assertThrows(IllegalArgumentException.class, () -> new AspectAbilityData(
                id("ability/invalid"), AspectAbilityKind.RANK_GRANTED, Optional.empty(), "test"
        ));
    }

    @Test
    void malformedStoredAbilityReturnsDataError() {
        JsonElement malformed = JsonParser.parseString("""
                {
                  "abilities": [{
                    "ability_id": "shadowslave:ability/invalid",
                    "kind": "rank_granted",
                    "provenance": "test"
                  }]
                }
                """);

        DataResult<AspectAbilitySetData> result = assertDoesNotThrow(
                () -> AspectAbilitySetData.CODEC.parse(JsonOps.INSTANCE, malformed)
        );
        assertTrue(result.error().isPresent());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
