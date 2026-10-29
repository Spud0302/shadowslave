package dev.spud.shadowslave.spell.voice;

import java.util.Map;

/**
 * What the game asks the Spell to say: a topic ("appraisal_grade", "calling"),
 * a key within it ("glorious", "new_region") and a few structured facts
 * ("behaviour", "region", "nature"). Facts are game data only, never player
 * names or chat, so the same request can later go to an AI voice as is. The
 * game has already decided everything; the voice only phrases it.
 *
 * @param seed picks among written variants, so the same request reads the same way
 */
public record VoiceRequest(String topic, String key, Map<String, String> facts, long seed) {
    public VoiceRequest {
        facts = Map.copyOf(facts);
    }

    public static VoiceRequest of(String topic, String key, Map<String, String> facts, long seed) {
        return new VoiceRequest(topic, key, facts, seed);
    }

    public String fact(String name) {
        return facts.getOrDefault(name, "");
    }
}
