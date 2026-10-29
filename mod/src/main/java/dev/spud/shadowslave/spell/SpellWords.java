package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceRequest;
import dev.spud.shadowslave.dreamrealm.DreamCrossing;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** Small helpers for the words the Spell uses: ordinals, and sending one voiced line to a player. */
public final class SpellWords {
    private static final String[] ORDINALS = {"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh"};

    private SpellWords() {
    }

    /** "Second" for 2; the Spell speaks of Nightmares by their ordinal. */
    public static String ordinal(int n) {
        return n >= 1 && n <= ORDINALS.length ? ORDINALS[n - 1] : Integer.toString(n);
    }

    /** "an Awakened Monster", "a Fallen Demon". */
    public static String withArticle(String noun) {
        boolean vowel = !noun.isEmpty() && "AEIOU".indexOf(Character.toUpperCase(noun.charAt(0))) >= 0;
        return (vowel ? "an " : "a ") + noun;
    }

    /** Asks the voice for a line and, if it has one, speaks it to the player in chat as the Spell. */
    public static boolean tell(ServerPlayer player, String topic, String key, Map<String, String> facts, ChatFormatting colour) {
        VoiceRequest request = VoiceRequest.of(topic, key, facts, player.getRandom().nextLong());
        return SpellVoices.say(request).map(line -> {
            DreamCrossing.spell(player, Component.literal(line).withStyle(colour));
            return true;
        }).orElse(false);
    }
}
