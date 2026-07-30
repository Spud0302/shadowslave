package dev.spud.shadowslave.soul.identity;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
