package dev.spud.shadowslave.trial;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.spell.SpellJudge;
import dev.spud.shadowslave.trial.content.AppraisalSettings;
import dev.spud.shadowslave.trial.content.EchoSite;
import dev.spud.shadowslave.trial.content.FirstNightmareSettings;
import dev.spud.shadowslave.trial.content.LongNightmareSettings;
import dev.spud.shadowslave.trial.content.NightmarePack;
import dev.spud.shadowslave.trial.content.ScarSettings;
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
    void runESettingsAndTheBriarCloisterParse() throws Exception {
        JsonElement file = json("nightmare_trial/settings.json");
        ScarSettings scar = ScarSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, file).getOrThrow();
        assertEquals(ScarSettings.DEFAULT, scar);
        assertEquals(LongNightmareSettings.DEFAULT, LongNightmareSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, file).getOrThrow());
        assertEquals(ScarSettings.DEFAULT, ScarSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow(),
                "a settings file without a scar keeps the defaults");
        TrialSettings settings = TrialSettings.CODEC.parse(JsonOps.INSTANCE, file).getOrThrow();
        EchoSite site = EchoSite.CODEC.parse(JsonOps.INSTANCE, json("dream_realm/echo_site/briar_cloister.json")).getOrThrow();
        assertEquals("shadowslave:long_thorn", site.nightmare().toString());
        assertEquals(3, settings.categoryFor(site.weight()), "the Briar Cloister is heavy enough for a Category 3 Seed");
        assertTrue(Math.hypot(site.x(), site.z()) > 1048, "it lies past the Thornwake Basin's wobbling border");
    }

    @Test
    void theFirstNightmareSettingsNameABundledPack() throws Exception {
        FirstNightmareSettings first = FirstNightmareSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, json("nightmare_trial/settings.json"))
                .getOrThrow();
        assertEquals(List.of("shadowslave:chain_road"), first.nightmares().stream().map(Object::toString).toList());
        assertTrue(first.bloom().size() >= 1 && !first.stirs().isEmpty() && !first.withered().isEmpty());
        assertEquals(3.0, first.quietDays(), "a fallen carrier's Seed stays quiet a few days");
        assertTrue(first.quiet().contains("{time}"));
        for (var id : first.nightmares()) {
            JsonElement pack = json("nightmare_trial/nightmare/" + id.getPath() + ".json");
            assertTrue(pack.getAsJsonObject().has("breach"), "a First Nightmare says what follows a fallen carrier out");
        }
        assertEquals(FirstNightmareSettings.DEFAULT,
                FirstNightmareSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow(),
                "a settings file without a first_nightmare block keeps the defaults");
    }

    @Test
    void aLongRunCountsItsDays() {
        NightmarePack.LongRun run = new NightmarePack.LongRun(600, "Day {day}.", 5, "");
        assertEquals(1, run.dayAt(0));
        assertEquals(1, run.dayAt(600 * 20 - 1));
        assertEquals(2, run.dayAt(600 * 20));
        assertEquals(4, run.dayAt(1800 * 20));
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
        assertEquals("Glorious", settings.gradeFor(10_000).name(), "glorious is the top grade; the impossible is a comment (ch. 15)");
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
    void theJudgeMeasuresHowFarTheRunStrayedFromFate() {
        AppraisalSettings appraisal = AppraisalSettings.DEFAULT;
        NightmarePack.Ending vigil = new NightmarePack.Ending("vigil", NightmarePack.EndingKind.LIGHT, 150, List.of("a"), 3, 0,
                "You lit the lamps.", "The Watch opened its eyes.", Optional.of("lamplighter"), 25, "A Lamplighter kept the lamps.",
                false, Optional.of(90));
        Map<String, Integer> kills = new LinkedHashMap<>();
        kills.put("Dormant Beast, Ash Burrower", 3);

        SpellJudge.Judgement solo = SpellJudge.judge(vigil, SpellJudge.Run.simple(kills, 6, "lamplighter", 1, 0), TrialSettings.DEFAULT,
                appraisal);
        // 90 for how far the ending strayed, 25 for the role's part, 25 for going in alone; the kills are listed, not counted.
        assertEquals(140, solo.score());
        assertEquals("Excellent", solo.grade().name());
        assertEquals("Your achievements are remarkable!", solo.acclaim(), "ch. 743");
        assertEquals(SpellJudge.Behaviour.ALONE, solo.behaviour());
        assertEquals(List.of("You have slain 3 Dormant Beasts, Ash Burrower.", "You lit the lamps.", "A Lamplighter kept the lamps.",
                "You entered the Nightmare alone."), solo.deeds());
        AppraisalSettings counted = new AppraisalSettings(1.0F, 210, "You have achieved the impossible!", Map.of());
        assertEquals(146, SpellJudge.judge(vigil, SpellJudge.Run.simple(kills, 6, "lamplighter", 1, 0), TrialSettings.DEFAULT, counted)
                .score(), "a server can still count kills");

        SpellJudge.Judgement cohort = SpellJudge.judge(vigil, SpellJudge.Run.simple(Map.of(), 0, "watch_slave", 3, 1), TrialSettings.DEFAULT,
                appraisal);
        assertEquals(90, cohort.score(), "small cohort bonus, less one fallen; no role bonus for another role");
        assertTrue(cohort.deeds().contains("One of your cohort fell."));
        assertEquals(SpellJudge.Behaviour.MERCIFUL, cohort.behaviour(), "no kills at all reads as mercy");
    }

    @Test
    void aSlaveWhoChangesFateAchievesTheImpossible() {
        AppraisalSettings appraisal = AppraisalSettings.DEFAULT;
        NightmarePack.Ending drover = new NightmarePack.Ending("drover", NightmarePack.EndingKind.SLAY_TYRANT, 0, List.of(), 3, 0,
                "", "", Optional.of("road_slave"), 30, "", false, Optional.of(140));
        NightmarePack.Ending dawn = new NightmarePack.Ending("dawn", NightmarePack.EndingKind.SURVIVE, 0, List.of(), 3, 0,
                "", "", Optional.empty(), 0, "", true, Optional.of(60));
        SpellJudge.Run slave = new SpellJudge.Run(Map.of(), 0, 0, 0, "road_slave", 40, 1, 0, 0, 0, 0);

        SpellJudge.Judgement slew = SpellJudge.judge(drover, slave, TrialSettings.DEFAULT, appraisal);
        // 140 strayed, 40 more for a slave doing it, 30 for the role's part, 25 alone.
        assertEquals(235, slew.score());
        assertEquals("Glorious", slew.grade().name());
        assertTrue(slew.impossible());
        assertEquals("You have achieved the impossible!", slew.acclaim());

        SpellJudge.Judgement endured = SpellJudge.judge(dawn, slave, TrialSettings.DEFAULT, appraisal);
        assertEquals(0, dawn.deviation(), "the fated ending strays not at all, whatever it says");
        assertEquals(25, endured.score(), "only the solo bonus: the role counts for nothing when fate held");
        assertEquals("Poor", endured.grade().name());
        assertTrue(!endured.impossible() && endured.acclaim().isEmpty());

        SpellJudge.Run guard = new SpellJudge.Run(Map.of(), 0, 0, 0, "sworn_guard", 0, 1, 0, 0, 0, 0);
        SpellJudge.Judgement glorious = SpellJudge.judge(new NightmarePack.Ending("herald", NightmarePack.EndingKind.SLAY_TYRANT, 0,
                List.of(), 3, 0, "", "", Optional.empty(), 0, "", false, Optional.of(150)), guard, TrialSettings.DEFAULT, appraisal);
        assertEquals("Glorious", glorious.grade().name());
        assertTrue(!glorious.impossible(), "glorious, but short of the impossible");
    }

    @Test
    void everyBundledNightmareHasOneHiddenFate() throws Exception {
        for (String id : List.of("last_vigil", "chain_road", "long_thorn")) {
            var pack = json("nightmare_trial/nightmare/" + id + ".json").getAsJsonObject();
            long fated = pack.getAsJsonArray("endings").asList().stream()
                    .filter(ending -> ending.getAsJsonObject().has("fated") && ending.getAsJsonObject().get("fated").getAsBoolean())
                    .count();
            assertEquals(1, fated, id + " names its fated ending");
            for (String line : pack.getAsJsonArray("opening").asList().stream().map(JsonElement::getAsString).toList()) {
                assertTrue(!line.toLowerCase(java.util.Locale.ROOT).contains("fate"), id + " never states its fate on entry");
            }
        }
        AppraisalSettings bundled = AppraisalSettings.IN_SETTINGS.parse(JsonOps.INSTANCE, json("nightmare_trial/settings.json")).getOrThrow();
        assertEquals(AppraisalSettings.DEFAULT, bundled);
    }

    @Test
    void theJudgeReadsHowYouPlayed() {
        NightmarePack.Ending dawn = new NightmarePack.Ending("dawn", NightmarePack.EndingKind.SURVIVE, 480, List.of(), 3, 25,
                "", "", Optional.empty(), 0, "");
        Map<String, Integer> kills = Map.of("Awakened Monster, Chainback", 1);
        SpellJudge.Run traitor = new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 0, 4, 0, 1, 3, 4);
        SpellJudge.Judgement judged = SpellJudge.judge(dawn, traitor, TrialSettings.DEFAULT, AppraisalSettings.DEFAULT);
        assertEquals(SpellJudge.Behaviour.TREACHERY, judged.behaviour(), "treachery outweighs everything else");
        assertTrue(judged.deeds().contains("You struck one of your own."));
        // 25 + unbroken 10 - treachery 30; the kill is listed, not counted.
        assertEquals(5, judged.score());
        assertEquals(SpellJudge.Behaviour.PROTECTOR,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 0, 4, 0, 0, 2, 4)));
        assertEquals(SpellJudge.Behaviour.RECKLESS,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 0, 4, 0, 0, 0, 3)));
        assertEquals(SpellJudge.Behaviour.DEFIANT,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 1, "watch_slave", 0, 4, 0, 0, 0, 0)));
        assertEquals(SpellJudge.Behaviour.STEADFAST,
                SpellJudge.behaviour(new SpellJudge.Run(kills, 10, 0, 0, "watch_slave", 0, 4, 0, 0, 0, 0)));
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
