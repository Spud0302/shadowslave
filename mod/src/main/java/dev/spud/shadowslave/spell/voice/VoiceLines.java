package dev.spud.shadowslave.spell.voice;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Spell's written lines, read from {@code data/<ns>/spell_voice/<topic>.json}
 * (DESIGN). Each file maps keys to variants; a variant is plain text, or an
 * object with {@code text} and a {@code when} map that must match the
 * request's facts. Text can name facts as {@code {region}}. Files of the same
 * topic from several data packs add their lines together.
 *
 * <pre>{"lines": {"glorious": ["Glorious.", {"text": "Glorious. Your treachery knows no bounds.", "when": {"behaviour": "treachery"}}]}}</pre>
 */
public final class VoiceLines {
    private static final Pattern FACT = Pattern.compile("\\{([a-z_]+)}");

    public record Line(String text, Map<String, String> when) {
        private static final Codec<Line> OBJECT = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("text").forGetter(Line::text),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("when", Map.of()).forGetter(Line::when)
        ).apply(instance, Line::new));

        public static final Codec<Line> CODEC = Codec.either(Codec.STRING, OBJECT).xmap(
                either -> either.map(text -> new Line(text, Map.of()), line -> line),
                line -> line.when.isEmpty() ? Either.left(line.text) : Either.right(line));

        public Line {
            when = Map.copyOf(when);
        }

        boolean matches(Map<String, String> facts) {
            return when.entrySet().stream().allMatch(entry -> entry.getValue().equals(facts.get(entry.getKey())));
        }

        /** Fills in facts; empty if the line names a fact the request does not have. */
        java.util.Optional<String> fill(Map<String, String> facts) {
            Matcher matcher = FACT.matcher(text);
            StringBuilder out = new StringBuilder();
            while (matcher.find()) {
                String value = facts.get(matcher.group(1));
                if (value == null || value.isEmpty()) {
                    return java.util.Optional.empty();
                }
                matcher.appendReplacement(out, Matcher.quoteReplacement(value));
            }
            matcher.appendTail(out);
            return java.util.Optional.of(out.toString());
        }
    }

    /** One file: keys to their variants. */
    public static final Codec<Map<String, List<Line>>> FILE_CODEC =
            Codec.unboundedMap(Codec.STRING, Line.CODEC.listOf()).fieldOf("lines").codec();

    public static final VoiceLines EMPTY = new VoiceLines(Map.of());

    private final Map<String, Map<String, List<Line>>> topics;

    public VoiceLines(Map<String, Map<String, List<Line>>> topics) {
        Map<String, Map<String, List<Line>>> copy = new HashMap<>();
        topics.forEach((topic, keys) -> {
            Map<String, List<Line>> inner = new HashMap<>();
            keys.forEach((key, lines) -> inner.put(key, List.copyOf(lines)));
            copy.put(topic, Map.copyOf(inner));
        });
        this.topics = Map.copyOf(copy);
    }

    /** Merges files of the same topic, as several data packs may add lines to it. */
    public static VoiceLines merge(Map<String, List<Map<String, List<Line>>>> files) {
        Map<String, Map<String, List<Line>>> topics = new HashMap<>();
        files.forEach((topic, list) -> {
            Map<String, List<Line>> keys = topics.computeIfAbsent(topic, ignored -> new HashMap<>());
            for (Map<String, List<Line>> file : list) {
                file.forEach((key, lines) -> keys.computeIfAbsent(key, ignored -> new ArrayList<>()).addAll(lines));
            }
        });
        return new VoiceLines(topics);
    }

    public Map<String, Map<String, List<Line>>> topics() {
        return topics;
    }

    public int count() {
        return topics.values().stream().flatMap(keys -> keys.values().stream()).mapToInt(List::size).sum();
    }

    /**
     * The line for a request: among the key's variants whose {@code when} matches
     * and whose facts are all known, the most specific ones win, and the request's
     * seed picks among those. Falls back to the topic's {@code default} key.
     */
    public java.util.Optional<String> pick(VoiceRequest request) {
        Map<String, List<Line>> keys = topics.get(request.topic());
        if (keys == null) {
            return java.util.Optional.empty();
        }
        java.util.Optional<String> line = pick(keys.get(request.key()), request);
        return line.isPresent() ? line : pick(keys.get("default"), request);
    }

    private static java.util.Optional<String> pick(List<Line> lines, VoiceRequest request) {
        if (lines == null || lines.isEmpty()) {
            return java.util.Optional.empty();
        }
        List<String> best = new ArrayList<>();
        int bestConditions = -1;
        for (Line line : lines) {
            if (!line.matches(request.facts())) {
                continue;
            }
            java.util.Optional<String> filled = line.fill(request.facts());
            if (filled.isEmpty()) {
                continue;
            }
            if (line.when().size() > bestConditions) {
                best.clear();
                bestConditions = line.when().size();
            }
            if (line.when().size() == bestConditions) {
                best.add(filled.get());
            }
        }
        if (best.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(best.get(Math.floorMod(Long.hashCode(request.seed() * 0x9E3779B97F4A7C15L), best.size())));
    }
}
