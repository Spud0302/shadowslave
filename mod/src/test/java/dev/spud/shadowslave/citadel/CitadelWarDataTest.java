package dev.spud.shadowslave.citadel;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Clan wars' data file parses with the numbers locked with Jack, and fixed war hours read the clock right. */
class CitadelWarDataTest {
    private static final Path WAR = Path.of("src/main/resources/data/shadowslave/citadel/war.json");

    private static CitadelWarSettings read() throws IOException {
        return CitadelWarSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(WAR))).getOrThrow();
    }

    @Test
    void theBundledWarMatchesTheLockedDesign() throws IOException {
        CitadelWarSettings war = read();
        assertTrue(war.enabled(), "sieges are on by default");
        assertEquals(24, war.declare().noticeHours(), "24 hours' notice after a declaration");
        assertFalse(war.declare().cost().isEmpty(), "declaring costs shards");
        assertTrue(war.declare().cost().stream().allMatch(cost -> cost.item().filter(id -> id.getPath().endsWith("soul_shard")).isPresent()));
        assertEquals(12, war.assault().noticeHours(), "assaults are announced at least 12 hours ahead");
        assertTrue(war.assault().cooldownHours() >= 48, "a failed assault waits a few days");
        assertEquals(72, war.abandon().abandonedHours(), "3 days with no login turns a seat Abandoned");
        assertTrue(war.abandon().warnHours() < war.abandon().abandonedHours(), "the Spell warns before");
        assertTrue(war.abandon().forsakenOpen(), "a Forsaken seat can be assaulted with no notice");
        assertTrue(war.truceDays() > 0, "a lost war leaves a truce");
        assertTrue(war.seat().moveCooldownDays() > 0, "a seat moves with a long cooldown");
        assertFalse(war.warHours().enabled(), "announced assaults by default, not fixed hours");
        assertFalse(war.supply().repairCost().isEmpty(), "a cut anchor costs something to mend");
        assertTrue(war.supply().anchorDistance() > 21, "the anchor stands outside the largest Citadel's walls");
    }

    @Test
    void anEmptyFileKeepsEveryDefault() {
        CitadelWarSettings war = CitadelWarSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow();
        assertEquals(CitadelWarSettings.DEFAULT, war);
    }

    @Test
    void warHoursReadUtcAndWrapPastMidnight() {
        CitadelWarSettings.WarHours evening = new CitadelWarSettings.WarHours(true, 18, 22);
        assertTrue(evening.contains(at(19)));
        assertFalse(evening.contains(at(22)));
        assertFalse(evening.contains(at(3)));
        CitadelWarSettings.WarHours night = new CitadelWarSettings.WarHours(true, 22, 2);
        assertTrue(night.contains(at(23)));
        assertTrue(night.contains(at(1)));
        assertFalse(night.contains(at(12)));
    }

    @Test
    void spansReadInWords() {
        assertEquals("3 days", CitadelWars.span(CitadelWarSettings.hours(72)));
        assertEquals("14 hours", CitadelWars.span(CitadelWarSettings.hours(14)));
        assertEquals("an hour", CitadelWars.span(CitadelWarSettings.hours(1)));
        assertEquals("40 minutes", CitadelWars.span(40 * 60_000L));
    }

    private static long at(int hour) {
        return ZonedDateTime.of(2026, 10, 31, hour, 30, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}
