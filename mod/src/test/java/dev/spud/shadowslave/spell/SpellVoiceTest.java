package dev.spud.shadowslave.spell;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.spell.voice.SpellVoice;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceLines;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellVoiceTest {
    private static final String[] TOPICS = {"appraisal_grade", "appraisal_behaviour", "appraisal_memory", "calling", "first"};

    @AfterEach
    void reset() {
        SpellVoices.setLines(VoiceLines.EMPTY);
        SpellVoices.voices().stream().filter(voice -> !voice.trusted()).forEach(SpellVoices::unregister);
    }

    @Test
    void theMostSpecificMatchingLineWins() {
        VoiceLines lines = VoiceLines.merge(Map.of("appraisal_grade", List.of(Map.of("glorious", List.of(
                new VoiceLines.Line("Glorious.", Map.of()),
                new VoiceLines.Line("Glorious. Your treachery knows no bounds.", Map.of("behaviour", "treachery")))))));
        assertEquals("Glorious. Your treachery knows no bounds.",
                lines.pick(VoiceRequest.of("appraisal_grade", "glorious", Map.of("behaviour", "treachery"), 1)).orElseThrow());
        assertEquals("Glorious.", lines.pick(VoiceRequest.of("appraisal_grade", "glorious", Map.of("behaviour", "alone"), 1)).orElseThrow());
        assertTrue(lines.pick(VoiceRequest.of("appraisal_grade", "poor", Map.of(), 1)).isEmpty());
    }

    @Test
    void factsFillInAndMissingFactsSkipTheLine() {
        VoiceLines lines = VoiceLines.merge(Map.of("calling", List.of(Map.of("new_region", List.of(
                new VoiceLines.Line("You have entered {region}.", Map.of()))))));
        assertEquals("You have entered the Ashen Expanse.",
                lines.pick(VoiceRequest.of("calling", "new_region", Map.of("region", "the Ashen Expanse"), 3)).orElseThrow());
        assertTrue(lines.pick(VoiceRequest.of("calling", "new_region", Map.of(), 3)).isEmpty());
    }

    @Test
    void anotherVoiceSpeaksFirstButCannotStateNumbers() {
        SpellVoices.setLines(VoiceLines.merge(Map.of("calling", List.of(Map.of("idle", List.of(new VoiceLines.Line("Written.", Map.of())))))));
        SpellVoice numbers = new TestVoice("You have 3 days left.");
        SpellVoices.register(numbers);
        assertEquals("Written.", SpellVoices.say(VoiceRequest.of("calling", "idle", Map.of(), 0)).orElseThrow(),
                "an untrusted line with a number falls back to the written one");
        SpellVoices.unregister(numbers);
        SpellVoice ai = new TestVoice("The stars turn.\nSlowly.");
        SpellVoices.register(ai);
        assertEquals("The stars turn. Slowly.", SpellVoices.say(VoiceRequest.of("calling", "idle", Map.of(), 0)).orElseThrow());
        SpellVoices.unregister(ai);
    }

    @Test
    void bundledLinesParseAndCoverEveryGradeBehaviourAndCalling() throws Exception {
        Map<String, List<Map<String, List<VoiceLines.Line>>>> files = new HashMap<>();
        for (String topic : TOPICS) {
            files.put(topic, List.of(VoiceLines.FILE_CODEC.parse(JsonOps.INSTANCE, json("spell_voice/" + topic + ".json")).getOrThrow()));
        }
        VoiceLines lines = VoiceLines.merge(files);
        Map<String, String> facts = Map.of("region", "the Ashen Expanse", "category", "Second", "citadel", "Greywatch",
                "what", "a Fallen Demon", "danger", "Dormant", "nature", "Ember", "next_category", "Second", "enemy", "Ironveil");
        for (String grade : List.of("failed", "poor", "adequate", "good", "excellent", "glorious")) {
            assertTrue(lines.pick(VoiceRequest.of("appraisal_grade", grade, facts, 7)).isPresent(), grade);
        }
        for (SpellJudge.Behaviour behaviour : SpellJudge.Behaviour.values()) {
            assertTrue(lines.pick(VoiceRequest.of("appraisal_behaviour", behaviour.id(), facts, 7)).isPresent(), behaviour.id());
        }
        for (String key : List.of("core_nearly_full", "core_full", "seed_next_category", "new_region", "unclaimed_citadel", "idle",
                "war_declared", "war_declared_on", "war_begins", "war_assault_announced", "war_assault_on_seat", "war_assault_begins",
                "war_assault_begins_defend", "war_abandon_countdown", "war_seat_abandoned", "war_won", "war_lost")) {
            assertTrue(lines.pick(VoiceRequest.of("calling", key, facts, 7)).isPresent(), key);
        }
        for (String key : List.of("first_conquest", "after_fleeing", "after_failing", "rose", "fell", "same_grade", "again_failed", "again_fled")) {
            assertTrue(lines.pick(VoiceRequest.of("appraisal_memory", key, facts, 7)).isPresent(), key);
        }
        Map<String, String> treachery = new HashMap<>(facts);
        treachery.put("behaviour", "treachery");
        assertEquals("Glorious. Your treachery truly knows no bounds.",
                lines.pick(VoiceRequest.of("appraisal_grade", "glorious", treachery, 7)).orElseThrow(), "ch. 15");
        assertFalse(lines.pick(VoiceRequest.of("appraisal_memory", "again_behaviour", Map.of("behaviour", "steadfast"), 7)).isPresent());
    }

    private record TestVoice(String line) implements SpellVoice {
        @Override
        public Optional<String> speak(VoiceRequest request) {
            return Optional.of(line);
        }

        @Override
        public String name() {
            return "test";
        }
    }

    private static JsonElement json(String path) throws Exception {
        var stream = SpellVoiceTest.class.getResourceAsStream("/data/shadowslave/" + path);
        if (stream == null) {
            throw new AssertionError("missing " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
}
