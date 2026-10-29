package dev.spud.shadowslave.trial;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.spell.SpellJudge;
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
        assertEquals("Poor", settings.gradeFor(0).name());
        assertEquals("Adequate", settings.gradeFor(50).name());
        assertEquals("Excellent", settings.gradeFor(169).name());
        assertEquals("Glorious", settings.gradeFor(170).name());
        assertEquals("Impossible", settings.gradeFor(10_000).name());
        assertEquals(4, settings.standing("glorious"));
        assertEquals(-1, settings.standing("Failed"));
    }

    @Test
    void killDeedsReadLikeTheSpell() {
        assertEquals("You have slain an Awakened Monster, Chainback.", SpellJudge.killDeed("Awakened Monster, Chainback", 1));
        assertEquals("You have slain a Dormant Beast, Ash Burrower.", SpellJudge.killDeed("Dormant Beast, Ash Burrower", 1));
        assertEquals("You have slain 3 Dormant Beasts, Ash Burrower.", SpellJudge.killDeed("Dormant Beast, Ash Burrower", 3));
    }

    @Test
    void theJudgeAddsEndingKillsRoleAndSolo() {
        NightmarePack.Ending vigil = new NightmarePack.Ending("vigil", NightmarePack.EndingKind.LIGHT, 150, List.of("a"), 90,
                "You lit the lamps.", "The Watch opened its eyes.", Optional.of("lamplighter"), 25, "A Lamplighter kept the lamps.");
        Map<String, Integer> kills = new LinkedHashMap<>();
        kills.put("Dormant Beast, Ash Burrower", 3);

        SpellJudge.Judgement solo = SpellJudge.judge(vigil, SpellJudge.Run.simple(kills, 6, "lamplighter", 1, 0), TrialSettings.DEFAULT);
        // 90 for the ending, 6 for the kills, 25 for the role's fate, 25 for going in alone.
        assertEquals(146, solo.score());
        assertEquals("Excellent", solo.grade().name());
        assertEquals(SpellJudge.Behaviour.ALONE, solo.behaviour());
        assertEquals(List.of("You have slain 3 Dormant Beasts, Ash Burrower.", "You lit the lamps.", "A Lamplighter kept the lamps.",
                "You entered the Nightmare alone."), solo.deeds());

        SpellJudge.Judgement cohort = SpellJudge.judge(vigil, SpellJudge.Run.simple(Map.of(), 0, "watch_slave", 3, 1), TrialSettings.DEFAULT);
        assertEquals(90, cohort.score(), "small cohort bonus, less one fallen; no role bonus for another role");
        assertTrue(cohort.deeds().contains("One of your cohort fell."));
        assertEquals(SpellJudge.Behaviour.MERCIFUL, cohort.behaviour(), "no kills at all reads as mercy");
    }

    @Test
    void theJudgeReadsHowYouPlayed() {
        NightmarePack.Ending dawn = new NightmarePack.Ending("dawn", NightmarePack.EndingKind.SURVIVE, 480, List.of(), 25,
                "", "", Optional.empty(), 0, "");
        Map<String, Integer> kills = Map.of("Awakened Monster, Chainback", 1);
        SpellJudge.Run traitor = new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 4, 0, 1, 3, 4);
        SpellJudge.Judgement judged = SpellJudge.judge(dawn, traitor, TrialSettings.DEFAULT);
        assertEquals(SpellJudge.Behaviour.TREACHERY, judged.behaviour(), "treachery outweighs everything else");
        assertTrue(judged.deeds().contains("You struck one of your own."));
        // 25 + 10 + unbroken 10 - treachery 30.
        assertEquals(15, judged.score());
        assertEquals(SpellJudge.Behaviour.PROTECTOR,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 4, 0, 0, 2, 4)));
        assertEquals(SpellJudge.Behaviour.RECKLESS,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 4, 0, 0, 0, 3)));
        assertEquals(SpellJudge.Behaviour.DEFIANT,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 4, 0, 0, 0, 0)));
        assertEquals(SpellJudge.Behaviour.STEADFAST,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 0, "watch_slave", 4, 0, 0, 0, 0)));
    }

    @Test
    void killsAreWorthRankTimesCores() {
        TrialSettings settings = TrialSettings.DEFAULT;
        assertEquals(5, SpellJudge.killPoints(settings, CreatureRank.AWAKENED, CreatureClass.BEAST, false, false));
        assertEquals(10, SpellJudge.killPoints(settings, CreatureRank.AWAKENED, CreatureClass.MONSTER, false, false));
        assertEquals(25 + 20 + 10, SpellJudge.killPoints(settings, CreatureRank.AWAKENED, CreatureClass.TYRANT, true, true));
        assertTrue(SpellJudge.unique(CreatureClass.TYRANT, false));
        assertTrue(SpellJudge.unique(CreatureClass.BEAST, true));
        assertTrue(SpellJudge.aboveRank(CreatureRank.AWAKENED, Optional.empty()));
        assertTrue(!SpellJudge.aboveRank(CreatureRank.AWAKENED, Optional.of(SoulRank.AWAKENED)));
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
