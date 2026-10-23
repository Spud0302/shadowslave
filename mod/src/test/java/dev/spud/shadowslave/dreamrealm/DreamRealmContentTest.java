package dev.spud.shadowslave.dreamrealm;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamRealmContentTest {
    @Test
    void coordinatesScaleLikeTheNether() {
        assertEquals(new BlockPos(80, 64, -16), DreamRealm.toOverworld(new BlockPos(10, 64, -2), 8));
        assertEquals(new BlockPos(1, 64, -1), DreamRealm.toRealm(new BlockPos(15, 64, -1), 8));
        assertEquals(new BlockPos(-2, 64, 0), DreamRealm.toRealm(new BlockPos(-9, 64, 7), 8));
        BlockPos realm = new BlockPos(-37, 70, 123);
        assertEquals(realm, DreamRealm.toRealm(DreamRealm.toOverworld(realm, 8), 8));
    }

    @Test
    void bundledFilesParse() throws Exception {
        DreamRegion region = DreamRegion.CODEC.parse(JsonOps.INSTANCE, json("dream_realm/region/ashen_expanse.json")).getOrThrow();
        assertEquals("The Ashen Expanse", region.displayName());
        assertEquals(100, region.seedForecast().odds().values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(region.contains(0) && region.contains(1_000_000));
        DreamSeedSettings seeds = DreamSeedSettings.CODEC.parse(JsonOps.INSTANCE, json("dream_realm/seeds.json")).getOrThrow();
        assertEquals(DreamSeedSettings.DEFAULT, seeds);
    }

    @Test
    void badForecastsAreRejected() {
        var json = JsonParser.parseString("{\"display_name\":\"x\",\"seed_forecast\":{\"1\":50,\"2\":40},\"cleanse_loot\":\"a:b\"}");
        assertTrue(DreamRegion.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent(), "odds must add to 100");
        var range = JsonParser.parseString("{\"display_name\":\"x\",\"seed_forecast\":{\"9\":100},\"cleanse_loot\":\"a:b\"}");
        assertTrue(DreamRegion.CODEC.parse(JsonOps.INSTANCE, range).error().isPresent(), "Category must exist");
    }

    @Test
    void theFurthestRingThatCoversAPlaceWins() {
        DreamRegion near = region("Near", 0, Integer.MAX_VALUE);
        DreamRegion far = region("Far", 500, 2000);
        DreamRealmContent content = new DreamRealmContent(DreamSeedSettings.DEFAULT, Map.of(
                ResourceLocation.parse("t:near"), near, ResourceLocation.parse("t:far"), far));
        assertEquals("Near", content.regionAt(new BlockPos(100, 0, 100)).orElseThrow().getValue().displayName());
        assertEquals("Far", content.regionAt(new BlockPos(600, 0, 0)).orElseThrow().getValue().displayName());
        assertEquals("Near", content.regionAt(new BlockPos(3000, 0, 0)).orElseThrow().getValue().displayName());
    }

    @Test
    void seedsShowFourStagesAndRewardTheEarlyCleanse() {
        DreamSeedSettings settings = DreamSeedSettings.DEFAULT;
        long growth = settings.growthTicks();
        assertEquals(72000, growth);
        assertEquals(0, settings.stage(0));
        assertEquals(1, settings.stage(growth / 4));
        assertEquals(3, settings.stage(growth - 1));
        assertEquals(3, settings.stage(growth * 5));
        for (int stage = 1; stage < DreamSeedSettings.STAGES; stage++) {
            assertTrue(settings.cleanseRolls(stage) <= settings.cleanseRolls(stage - 1), "earlier is never worse");
        }
    }

    private static DreamRegion region(String name, int min, int max) {
        return new DreamRegion(name, min, max, dev.spud.shadowslave.gate.GateForecast.certain(1),
                ResourceLocation.parse("t:loot"), "");
    }

    private static com.google.gson.JsonElement json(String path) throws Exception {
        try (var stream = DreamRealmContentTest.class.getResourceAsStream("/data/shadowslave/" + path)) {
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }
}
