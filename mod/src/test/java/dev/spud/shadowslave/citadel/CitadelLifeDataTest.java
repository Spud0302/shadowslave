package dev.spud.shadowslave.citadel;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Citadel life's data files parse, and upkeep and upgrades read the way the Gateway shows them. */
class CitadelLifeDataTest {
    private static final Path DATA = Path.of("src/main/resources/data/shadowslave/citadel");
    private static final List<String> UPGRADES = List.of("wards", "workshops", "hall", "archive", "lanterns", "gateway_links");

    private static <T> T read(Path file, Codec<T> codec) throws IOException {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow();
    }

    @Test
    void upkeepScalesWithTheClanAndNeglectComesInStages() throws IOException {
        CitadelUpkeep upkeep = read(DATA.resolve("upkeep.json"), CitadelUpkeep.CODEC);
        assertTrue(upkeep.perDay(4) > upkeep.perDay(1), "a bigger clan eats more");
        assertEquals(upkeep.perDay(3) * upkeep.capacityDays(), upkeep.capacity(3), 1e-9);
        assertEquals(Neglect.FED, upkeep.stage(1, 0));
        assertEquals(Neglect.HUNGRY, upkeep.stage(0, 0));
        long day = CitadelUpkeep.DAY_TICKS;
        assertEquals(Neglect.STARVING, upkeep.stage(0, Math.round(upkeep.starvingDays() * day)));
        assertEquals(Neglect.FORSAKEN, upkeep.stage(0, Math.round(upkeep.forsakenDays() * day) + 1));
        assertEquals(Neglect.FORSAKEN, upkeep.stage(0, 1000 * day), "neglect stops at Forsaken; nothing is ever lost to it");
        assertTrue(upkeep.starvingDays() < upkeep.forsakenDays());
        assertFalse(upkeep.feed().isEmpty(), "something feeds a Gateway");
        assertTrue(upkeep.feed().stream().anyMatch(entry -> entry.item().filter(id -> id.getPath().endsWith("soul_shard")).isPresent()),
                "soul shards feed it");
    }

    @Test
    void everyUpgradeParsesWithThreeTiersAndGrows() throws IOException {
        try (Stream<Path> files = Files.list(DATA.resolve("upgrade"))) {
            assertEquals(UPGRADES.size(), files.count());
        }
        for (String id : UPGRADES) {
            CitadelUpgrade upgrade = read(DATA.resolve("upgrade/" + id + ".json"), CitadelUpgrade.CODEC);
            assertEquals(3, upgrade.maxTier(), id);
            upgrade.tiers().forEach(tier -> {
                assertFalse(tier.cost().isEmpty(), id + " costs something");
                assertFalse(tier.summary().isEmpty(), id + " says what it gives");
            });
        }
        CitadelUpgrade wards = read(DATA.resolve("upgrade/wards.json"), CitadelUpgrade.CODEC);
        assertEquals(0, wards.value(0, "radius"));
        assertTrue(wards.value(1, "radius") > 0 && wards.value(3, "radius") > wards.value(1, "radius"));
        assertEquals(1, wards.value(2, "alarm"), "a tier that does not name a value keeps the last tier's");
        assertFalse(wards.worksAt(Neglect.HUNGRY), "the wards drop first");
        CitadelUpgrade workshops = read(DATA.resolve("upgrade/workshops.json"), CitadelUpgrade.CODEC);
        assertTrue(workshops.worksAt(Neglect.HUNGRY) && !workshops.worksAt(Neglect.STARVING), "then the workshops slow");
        CitadelUpgrade hall = read(DATA.resolve("upgrade/hall.json"), CitadelUpgrade.CODEC);
        assertTrue(hall.worksAt(Neglect.FORSAKEN), "neglect never shrinks the Hall");
        assertTrue(hall.value(0, "anchors") > 0, "some members can anchor before the Hall is built");
        CitadelUpgrade links = read(DATA.resolve("upgrade/gateway_links.json"), CitadelUpgrade.CODEC);
        assertTrue(links.value(1, "essence") > links.value(3, "essence"), "higher Links cost less essence a trip");
    }

    @Test
    void raidSettingsParse() throws IOException {
        CitadelRaidSettings raids = read(DATA.resolve("raid.json"), CitadelRaidSettings.CODEC);
        assertTrue(raids.radius() > raids.breachRadius());
        assertEquals(Neglect.FORSAKEN, raids.echoFailsAt(), "the echo is the last thing neglect takes");
        assertTrue(raids.repairShare() > 0 && raids.repairShare() <= 1);
    }
}
