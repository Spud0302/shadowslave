package dev.spud.shadowslave.spell.truename;

import dev.spud.shadowslave.spell.voice.VoiceRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Builds a True Name from a deed's word lists (DESIGN, Run B). The game picks
 * the words; the voice picks the form they take ({@code true_name/name} lines,
 * such as "{epithet} {noun}" or "{noun} of {place}"), so the Spell may phrase a
 * name creatively, as with Kai (ch. 378), while the game still decides whether
 * a name is granted at all. Pure, so it can be tested without a world.
 */
public final class TrueNameBuilder {
    public static final String TOPIC = "true_name";
    public static final String NAME_KEY = "name";
    /** Letters, spaces, hyphens and apostrophes; two to thirty-two characters; starting and ending on a letter. */
    private static final Pattern VALID = Pattern.compile("[A-Za-z][A-Za-z' -]{0,30}[A-Za-z]");
    private static final int MAX_WORDS = 5;

    private TrueNameBuilder() {
    }

    /**
     * A name for the deed that nobody else bears, or empty if every try was taken
     * or no form could be filled.
     *
     * @param facts   facts about the bearer the forms may use (nature, rank); never their player name
     * @param voice   asks the Spell's voices for a line (the first that answers)
     * @param written asks only the written tables, used when a voice's line is not a fit name
     * @param taken   whether a name is already borne by someone
     */
    public static Optional<String> build(String deedId, TrueNameDeed deed, Map<String, String> facts, long seed, int attempts,
                                         Function<VoiceRequest, Optional<String>> voice,
                                         Function<VoiceRequest, Optional<String>> written, Predicate<String> taken) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            Random random = new Random(seed + attempt * 0x9E3779B97F4A7C15L);
            Map<String, String> filled = new HashMap<>(facts);
            filled.put("deed", deedId);
            deed.words().forEach((slot, words) -> {
                if (!words.isEmpty()) {
                    filled.put(slot, words.get(random.nextInt(words.size())));
                }
            });
            VoiceRequest request = VoiceRequest.of(TOPIC, NAME_KEY, filled, random.nextLong());
            Optional<String> name = voice.apply(request).flatMap(TrueNameBuilder::valid)
                    .or(() -> written.apply(request).flatMap(TrueNameBuilder::valid));
            if (name.isPresent() && !taken.test(name.get())) {
                return name;
            }
        }
        return Optional.empty();
    }

    /** The line as a name, tidied, if it is one: plain words, no numbers or marks, and short. */
    public static Optional<String> valid(String line) {
        String name = line.trim().replaceAll("\\s+", " ");
        if (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }
        if (!VALID.matcher(name).matches() || name.split(" ").length > MAX_WORDS) {
            return Optional.empty();
        }
        return Optional.of(Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    /** Whether two names are the same name, as the Spell would hear them. */
    public static boolean same(String a, String b) {
        return normal(a).equals(normal(b));
    }

    static String normal(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    /** The words a slot can take, for tests and the deed command. */
    public static List<String> slot(TrueNameDeed deed, String slot) {
        return deed.words().getOrDefault(slot, List.of());
    }
}
