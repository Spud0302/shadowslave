package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrownedBellNativeWorldgenWiringTest {
    private static final Path MOD = Path.of("src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java");
    private static final Path REGISTRY = Path.of("src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmWorldgenFeatures.java");
    private static final Path FEATURE = Path.of("src/main/java/dev/spud/shadowslave/dreamrealm/DrownedBellLaterAnchorFeature.java");
    private static final Path CONFIGURED = Path.of("src/main/resources/data/shadowslave/worldgen/configured_feature/drowned_bell_later_anchor.json");
    private static final Path PLACED = Path.of("src/main/resources/data/shadowslave/worldgen/placed_feature/drowned_bell_later_anchor.json");
    private static final Path BIOME = Path.of("src/main/resources/data/shadowslave/worldgen/biome/ashen_expanse.json");

    @Test
    void modRegistersTheNativeFeatureWithoutMovingSiteAuthorityIntoMinecraft() throws IOException {
        String mod = Files.readString(MOD);
        String registry = Files.readString(REGISTRY);
        String feature = Files.readString(FEATURE);

        assertTrue(mod.contains("DreamRealmWorldgenFeatures.register(modEventBus);"));
        assertTrue(registry.contains("Registries.FEATURE"));
        assertTrue(registry.contains("drowned_bell_later_anchor_feature"));
        assertTrue(feature.contains("StormLanternCoastNativePlacementPlan.drownedBellLater"));
        assertTrue(feature.contains("nativeSite.nativePieceForChunk"));
        assertTrue(feature.contains("NATIVE_SEA_GATE_ID"));
        assertTrue(feature.contains("buildBrokenSeaGate"));
        assertFalse(feature.contains("NightmareDivergenceAppraisal"));
        assertFalse(feature.contains("MemoryOwnership"));
        assertFalse(feature.contains("SoulData"));
    }

    @Test
    void datapackConfiguresAndPlacesTheFeatureAtSurfaceStructureStep() throws IOException {
        String configured = Files.readString(CONFIGURED);
        String placed = Files.readString(PLACED);
        String biome = Files.readString(BIOME);

        assertTrue(configured.contains("shadowslave:drowned_bell_later_anchor_feature"));
        assertTrue(placed.contains("shadowslave:drowned_bell_later_anchor"));
        assertTrue(placed.contains("\"placement\": []"));
        assertTrue(biome.contains("shadowslave:drowned_bell_later_anchor"));

        int features = biome.indexOf("\"features\"");
        int nativeAnchor = biome.indexOf("shadowslave:drowned_bell_later_anchor");
        assertTrue(features >= 0 && nativeAnchor > features);
    }

    @Test
    void nativeFeatureDoesNotClearOrRebuildTheCommandFixture() throws IOException {
        String feature = Files.readString(FEATURE);

        assertFalse(feature.contains("RADIUS"));
        assertFalse(feature.contains("clear("));
        assertFalse(feature.contains("StormLanternCoastPreviewService"));
        assertFalse(feature.contains("teleportTo"));
        assertTrue(feature.contains("WORLD_SURFACE_WG"));
    }
}
