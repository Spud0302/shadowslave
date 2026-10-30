package dev.spud.shadowslave.spell;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.spell.truename.TrueNameBuilder;
import dev.spud.shadowslave.spell.truename.TrueNameDeed;
import dev.spud.shadowslave.spell.truename.TrueNameSettings;
import dev.spud.shadowslave.spell.voice.VoiceLines;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrueNameTest {
    private static final List<String> DEEDS = List.of("treachery", "protector", "reckless", "defiant", "merciful", "alone",
            "steadfast", "first_great", "tyrant_alone", "citadel_alone");

    @Test
    void everyBehaviourTheJudgeReadsHasADeed() throws Exception {
        for (SpellJudge.Behaviour behaviour : SpellJudge.Behaviour.values()) {
            boolean answered = false;
            for (String id : DEEDS) {
                answered |= deed(id).matchesAppraisal(behaviour.id());
            }
            assertTrue(answered, behaviour.id());
        }
        assertTrue(deed("steadfast").matchesAppraisal("treachery"), "the steadfast deed answers any appraisal, at the lowest priority");
        assertTrue(deed("treachery").priority() >= deed("steadfast").priority());
    }

    @Test
    void extraordinaryDeedsMatchOnlyWhatTheyAskFor() throws Exception {
        TrueNameDeed first = deed("first_great");
        assertTrue(first.matchesFirst("kill_great_beast"));
        assertTrue(first.matchesFirst("kill_unholy_titan"));
        assertFalse(first.matchesFirst("kill_fallen_demon"));
        assertFalse(first.matchesAppraisal("alone"));

        TrueNameDeed tyrant = deed("tyrant_alone");
        assertTrue(tyrant.matchesKill(CreatureRank.AWAKENED, CreatureClass.TYRANT, true, true));
        assertFalse(tyrant.matchesKill(CreatureRank.AWAKENED, CreatureClass.TYRANT, true, false), "someone stood beside them");
        assertFalse(tyrant.matchesKill(CreatureRank.AWAKENED, CreatureClass.TYRANT, false, true), "not above their rank");
        assertFalse(tyrant.matchesKill(CreatureRank.GREAT, CreatureClass.DEVIL, true, true), "a Devil is not a Tyrant");

        TrueNameDeed raid = deed("citadel_alone");
        assertTrue(raid.matchesRaid(true), "a Citadel held alone");
        assertFalse(raid.matchesRaid(false), "held with others beside them");
        assertFalse(raid.matchesKill(CreatureRank.AWAKENED, CreatureClass.TYRANT, true, true));
        assertFalse(tyrant.matchesRaid(true), "a kill deed never answers a raid");
    }

    @Test
    void namesAreBuiltFromTheDeedsWordsInTheVoicesForms() throws Exception {
        VoiceLines lines = voice();
        for (String id : DEEDS) {
            TrueNameDeed deed = deed(id);
            Set<String> words = new HashSet<>();
            deed.words().values().forEach(words::addAll);
            for (long seed = 0; seed < 40; seed++) {
                String name = TrueNameBuilder.build(id, deed, Map.of("nature", "Ember"), seed, 4, lines::pick, lines::pick, taken -> false)
                        .orElseThrow(() -> new AssertionError("no name for " + id));
                assertTrue(TrueNameBuilder.valid(name).isPresent(), name);
                assertTrue(words.stream().anyMatch(name::contains), id + " made " + name);
            }
        }
    }

    @Test
    void aNameAlreadyBorneIsNeverGivenAgain() throws Exception {
        VoiceLines lines = voice();
        TrueNameDeed deed = deed("alone");
        Set<String> borne = new HashSet<>();
        for (int i = 0; i < 12; i++) {
            Optional<String> name = TrueNameBuilder.build("alone", deed, Map.of(), 99, 40, lines::pick, lines::pick,
                    candidate -> borne.stream().anyMatch(each -> TrueNameBuilder.same(each, candidate)));
            assertTrue(name.isPresent(), "try " + i);
            assertTrue(borne.add(name.get().toLowerCase()), "a second " + name.get());
        }
        assertTrue(TrueNameBuilder.build("alone", deed, Map.of(), 99, 3, lines::pick, lines::pick, candidate -> true).isEmpty(),
                "with every name taken the Spell gives up for now");
    }

    @Test
    void anUnfitNameFromAVoiceFallsBackToTheWrittenForms() throws Exception {
        VoiceLines lines = voice();
        TrueNameDeed deed = deed("protector");
        String name = TrueNameBuilder.build("protector", deed, Map.of(), 5, 1,
                request -> Optional.of("The 3rd Shield, who will get 40 boons!"), lines::pick, taken -> false).orElseThrow();
        assertTrue(TrueNameBuilder.valid(name).isPresent(), name);
        assertEquals(Optional.of("Ever-Watchful Lantern"), TrueNameBuilder.valid("  ever-Watchful   Lantern."));
        assertTrue(TrueNameBuilder.valid("x").isEmpty());
        assertTrue(TrueNameBuilder.valid("Song of the Fallen Who Sing Loudly").isEmpty(), "too many words");
        assertTrue(TrueNameBuilder.same("Lone Wolf", " lone  wolf "));
    }

    @Test
    void settingsParseWithTheGloriousChanceOpeningFirst() throws Exception {
        TrueNameSettings settings = TrueNameSettings.CODEC.parse(JsonOps.INSTANCE, json("true_name/settings.json")).getOrThrow();
        assertEquals(0.0F, settings.chanceFor("Excellent"), "below Glorious the chance never opens");
        assertTrue(settings.chanceFor("Glorious") > 0.0F);
        assertEquals(1.0F, settings.chanceFor("Impossible"));
        assertTrue(settings.drainResist() > 0.0F && settings.drainResist() < 1.0F);
        assertEquals(24000, settings.senseCooldown(), "once a day");
    }

    @Test
    void theVoiceHasEveryTrueNameLine() throws Exception {
        VoiceLines lines = voice();
        Map<String, String> facts = Map.of("true_name", "Lone Wolf", "deed", "alone");
        for (String key : List.of("granted", "reveal", "learned")) {
            assertTrue(lines.pick(VoiceRequest.of(TrueNameBuilder.TOPIC, key, facts, 3)).isPresent(), key);
        }
        VoiceLines callings = VoiceLines.merge(Map.of("calling", List.of(
                VoiceLines.FILE_CODEC.parse(JsonOps.INSTANCE, json("spell_voice/calling.json")).getOrThrow())));
        assertTrue(callings.pick(VoiceRequest.of("calling", "name_spoken", Map.of(), 3)).isPresent());
        String named = callings.pick(VoiceRequest.of("calling", "idle", Map.of("named", "yes", "true_name", "Lone Wolf"), 3)).orElseThrow();
        assertTrue(named.contains("Lone Wolf"), "a bearer's Callings say a little more: " + named);
    }

    static TrueNameDeed deed(String id) throws Exception {
        return TrueNameDeed.CODEC.parse(JsonOps.INSTANCE, json("true_name/deed/" + id + ".json")).getOrThrow();
    }

    static VoiceLines voice() throws Exception {
        return VoiceLines.merge(Map.of(TrueNameBuilder.TOPIC, List.of(
                VoiceLines.FILE_CODEC.parse(JsonOps.INSTANCE, json("spell_voice/true_name.json")).getOrThrow())));
    }

    private static JsonElement json(String path) throws Exception {
        var stream = TrueNameTest.class.getResourceAsStream("/data/shadowslave/" + path);
        if (stream == null) {
            throw new AssertionError("missing " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
}
