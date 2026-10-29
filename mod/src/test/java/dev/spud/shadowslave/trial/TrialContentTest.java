package dev.spud.shadowslave.trial;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.TrialSettings;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrialContentTest {
    @Test
    void bundledSettingsAndSiteParse() throws Exception {
        TrialSettings settings = TrialSettings.CODEC.parse(JsonOps.INSTANCE, json("nightmare_trial/settings.json")).getOrThrow();
        assertEquals(15, settings.gatherSeconds());
        assertEquals(5, settings.pointsFor(CreatureRank.AWAKENED));
        EchoSite site = EchoSite.CODEC.parse(JsonOps.INSTANCE, json("dream_realm/echo_site/ember_watch.json")).getOrThrow();
        assertEquals("shadowslave:last_vigil", site.nightmare().toString());
        assertEquals(2, settings.categoryFor(site.weight()), "the Ember Watch is heavy enough for a Category 2 Seed");
    }

    @Test
    void weightSetsTheCategory() {
        TrialSettings settings = TrialSettings.DEFAULT;
        assertEquals(1, settings.categoryFor(0));
        assertEquals(1, settings.categoryFor(29));
        assertEquals(2, settings.categoryFor(30));
        assertEquals(3, settings.categoryFor(60));
        assertEquals(5, settings.categoryFor(100));
    }

    @Test
    void gradesClimbWithTheScore() {
        TrialSettings settings = TrialSettings.DEFAULT;
        assertEquals("Passable", settings.gradeFor(0).name());
        assertEquals("Commendable", settings.gradeFor(50).name());
        assertEquals("Admirable", settings.gradeFor(129).name());
        assertEquals("Glorious", settings.gradeFor(10_000).name());
    }

    @Test
    void killDeedsReadLikeTheSpell() {
        assertEquals("You have slain an Awakened Monster, Chainback.", Appraisal.killDeed("Awakened Monster, Chainback", 1));
        assertEquals("You have slain a Dormant Beast, Ash Burrower.", Appraisal.killDeed("Dormant Beast, Ash Burrower", 1));
        assertEquals("You have slain 3 Dormant Beasts, Ash Burrower.", Appraisal.killDeed("Dormant Beast, Ash Burrower", 3));
    }

    @Test
    void theAppraisalAddsEndingKillsRoleAndSolo() {
        NightmarePack.Ending vigil = new NightmarePack.Ending("vigil", NightmarePack.EndingKind.LIGHT, 150, List.of("a"), 90,
                "You lit the lamps.", "The Watch opened its eyes.", Optional.of("lamplighter"), 25, "A Lamplighter kept the lamps.");
        Map<String, Integer> kills = new LinkedHashMap<>();
        kills.put("Dormant Beast, Ash Burrower", 3);
        Map<String, String> comments = Map.of("Glorious", "Glorious.", "Passable", "You survived.");

        Appraisal solo = Appraisal.of(comments, vigil, new Appraisal.Run(kills, 6, "lamplighter", 1, 0), TrialSettings.DEFAULT);
        // 90 for the ending, 6 for the kills, 25 for the role's fate, 25 for going in alone.
        assertEquals(146, solo.score());
        assertEquals("Glorious", solo.grade().name());
        assertEquals(List.of("You have slain 3 Dormant Beasts, Ash Burrower.", "You lit the lamps.", "A Lamplighter kept the lamps.",
                "You entered the Nightmare alone."), solo.deeds());

        Appraisal cohort = Appraisal.of(comments, vigil, new Appraisal.Run(Map.of(), 0, "watch_slave", 3, 1), TrialSettings.DEFAULT);
        assertEquals(90, cohort.score(), "no role bonus for another role, no unbroken bonus after a fall");
        assertTrue(cohort.deeds().contains("You lit the lamps."));
    }

    private static JsonElement json(String path) throws Exception {
        var stream = TrialContentTest.class.getResourceAsStream("/data/shadowslave/" + path);
        if (stream == null) {
            throw new AssertionError("missing " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
}
