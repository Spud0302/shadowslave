package dev.spud.shadowslave.migration;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void halfAnImportedIdentityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ImportedIdentityData(Optional.of(ASPECT), Optional.empty())
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }
}
