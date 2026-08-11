package dev.spud.shadowslave.dreamrealm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuinMetalPresentationResourcesTest {
    private static final Path MOD = Path.of("src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java");
    private static final Path BLOCKS = Path.of("src/main/java/dev/spud/shadowslave/world/block/ModBlocks.java");
    private static final Path PREVIEW = Path.of("src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmPreviewService.java");
    private static final Path RUNTIME = Path.of("src/main/java/dev/spud/shadowslave/dreamrealm/DreamRealmResourceInteractionRuntime.java");
    private static final Path BLOCKSTATE = Path.of("src/main/resources/assets/shadowslave/blockstates/ruin_metal.json");
    private static final Path MODEL = Path.of("src/main/resources/assets/shadowslave/models/block/ruin_metal.json");

    @Test
    void ruinMetalUsesARegisteredProjectBlockWithoutOwningResourceIdentity() throws IOException {
        var ruin = DreamRealmResourceInteractionBinding.ashenExpanseResources().stream()
                .filter(interaction -> interaction.resourceId().equals("ruin_metal"))
                .findFirst()
                .orElseThrow();

        assertEquals("shadowslave:ruin_metal", ruin.physicalBlockId());
        assertTrue(ruin.boundary().contains("no item"));
        assertTrue(ruin.boundary().contains("no item, currency, Soul Shard, progression, ownership"));

        String blocks = Files.readString(BLOCKS);
        String mod = Files.readString(MOD);
        String preview = Files.readString(PREVIEW);
        String runtime = Files.readString(RUNTIME);

        assertTrue(blocks.contains("BuiltInRegistries.BLOCK, ShadowSlaveMod.MOD_ID"));
        assertTrue(blocks.contains("\"ruin_metal\""));
        assertTrue(mod.contains("ModBlocks.register(modEventBus);"));
        assertTrue(preview.contains("case \"ruin_metal\" -> ModBlocks.RUIN_METAL.get().defaultBlockState();"));
        assertFalse(preview.contains("case \"ruin_metal\" -> Blocks.RAW_IRON_BLOCK.defaultBlockState();"));
        assertTrue(runtime.contains("case \"shadowslave:ruin_metal\" -> ModBlocks.RUIN_METAL.get();"));
    }

    @Test
    void projectModelIsNotTheVanillaRawIronCube() throws IOException {
        String blockstate = Files.readString(BLOCKSTATE);
        String model = Files.readString(MODEL);

        assertTrue(blockstate.contains("shadowslave:block/ruin_metal"));
        assertFalse(model.contains("\"parent\": \"minecraft:block/cube_all\""));
        assertEquals(3, occurrences(model, "\"from\""));
        assertEquals(3, occurrences(model, "\"to\""));

        // Material pixels are still a clearly bounded vanilla placeholder; geometry and registry identity are project-owned.
        assertTrue(model.contains("minecraft:block/raw_iron_block"));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = text.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
