package dev.spud.shadowslave.spell.voice;

import dev.spud.shadowslave.ShadowSlaveMod;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Where the game asks for the Spell's lines. Voices registered here are asked
 * first, newest first; the written tables always answer last, so the mod never
 * depends on any other voice. Untrusted lines are cleaned and checked: one
 * line of plain text, at most {@link #MAX_LENGTH} characters, and no numbers,
 * since grades, rewards and counts are the game's to state.
 */
public final class SpellVoices {
    public static final int MAX_LENGTH = 200;
    private static final Pattern DIGITS = Pattern.compile("\\d");
    private static final List<SpellVoice> VOICES = new CopyOnWriteArrayList<>();
    private static volatile VoiceLines lines = VoiceLines.EMPTY;
    private static final WrittenVoice WRITTEN = new WrittenVoice(() -> lines);

    private SpellVoices() {
    }

    /** Puts a voice in front of the others, e.g. an optional AI module. */
    public static void register(SpellVoice voice) {
        VOICES.add(0, voice);
    }

    public static void unregister(SpellVoice voice) {
        VOICES.remove(voice);
    }

    public static void setLines(VoiceLines loaded) {
        lines = loaded;
    }

    public static VoiceLines lines() {
        return lines;
    }

    /** The voices asked, in order, ending with the written one. */
    public static List<SpellVoice> voices() {
        List<SpellVoice> all = new java.util.ArrayList<>(VOICES);
        all.add(WRITTEN);
        return all;
    }

    /** The Spell's line for a request, or empty if no voice and no table has one. */
    public static Optional<String> say(VoiceRequest request) {
        for (SpellVoice voice : voices()) {
            Optional<String> line;
            try {
                line = voice.speak(request);
            } catch (RuntimeException exception) {
                ShadowSlaveMod.LOGGER.warn("Spell voice {} failed on {}/{}", voice.name(), request.topic(), request.key(), exception);
                continue;
            }
            Optional<String> clean = line.flatMap(text -> clean(text, voice.trusted()));
            if (clean.isPresent()) {
                return clean;
            }
        }
        return Optional.empty();
    }

    /** The line, or {@code fallback} if nothing answers. */
    public static String say(VoiceRequest request, String fallback) {
        return say(request).orElse(fallback);
    }

    static Optional<String> clean(String text, boolean trusted) {
        String flat = text.replace('\n', ' ').replace('\r', ' ').replace("§", "").trim();
        if (flat.isEmpty()) {
            return Optional.empty();
        }
        // A line may open with a filled-in fact ("{region}. Few have come this far.").
        flat = Character.toUpperCase(flat.charAt(0)) + flat.substring(1);
        if (trusted) {
            return Optional.of(flat);
        }
        if (DIGITS.matcher(flat).find()) {
            return Optional.empty();
        }
        return Optional.of(flat.length() <= MAX_LENGTH ? flat : flat.substring(0, MAX_LENGTH - 1).trim() + "…");
    }
}
