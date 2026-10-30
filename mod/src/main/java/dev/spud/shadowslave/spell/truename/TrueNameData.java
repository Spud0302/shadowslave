package dev.spud.shadowslave.spell.truename;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * One player's True Name, if the Spell granted one, and the True Names of
 * others they have learned. Kept through death; a name is never taken back
 * and a name once learned is never forgotten.
 */
public final class TrueNameData {
    /** A True Name this player knows: whose it is, the name, and when they last sensed its bearer. */
    public record Known(UUID bearer, String bearerName, String name, long lastSensed) {
        public static final Codec<Known> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("bearer").forGetter(Known::bearer),
                Codec.STRING.fieldOf("bearer_name").forGetter(Known::bearerName),
                Codec.STRING.fieldOf("name").forGetter(Known::name),
                Codec.LONG.optionalFieldOf("last_sensed", Long.MIN_VALUE).forGetter(Known::lastSensed)
        ).apply(instance, Known::new));

        Known sensedAt(long time) {
            return new Known(bearer, bearerName, name, time);
        }
    }

    public static final Codec<TrueNameData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(data -> data.name),
            Codec.STRING.optionalFieldOf("deed", "").forGetter(data -> data.deed),
            Codec.STRING.optionalFieldOf("deed_line", "").forGetter(data -> data.deedLine),
            Codec.LONG.optionalFieldOf("day", 0L).forGetter(data -> data.day),
            Codec.BOOL.optionalFieldOf("shown", false).forGetter(data -> data.shown),
            Codec.BOOL.optionalFieldOf("pending_reveal", false).forGetter(data -> data.pendingReveal),
            Known.CODEC.listOf().optionalFieldOf("known", List.of()).forGetter(data -> data.known)
    ).apply(instance, TrueNameData::new));

    String name;
    String deed;
    String deedLine;
    long day;
    boolean shown;
    /** Granted while the screen could not open (inside a Nightmare); shown when it can. */
    boolean pendingReveal;
    final List<Known> known;

    private TrueNameData(String name, String deed, String deedLine, long day, boolean shown, boolean pendingReveal, List<Known> known) {
        this.name = name;
        this.deed = deed;
        this.deedLine = deedLine;
        this.day = day;
        this.shown = shown;
        this.pendingReveal = pendingReveal;
        this.known = new ArrayList<>(known);
    }

    public static TrueNameData empty() {
        return new TrueNameData("", "", "", 0L, false, false, List.of());
    }

    public boolean named() {
        return !name.isEmpty();
    }

    public String name() {
        return name;
    }

    public String deed() {
        return deed;
    }

    public String deedLine() {
        return deedLine;
    }

    public long day() {
        return day;
    }

    /** Whether the bearer shows it as a title. Hidden by default. */
    public boolean shown() {
        return shown;
    }

    public List<Known> known() {
        return List.copyOf(known);
    }

    /** The True Name of {@code bearer} as this player learned it, if they did. */
    public Optional<Known> knows(UUID bearer) {
        return known.stream().filter(entry -> entry.bearer().equals(bearer)).findFirst();
    }

    /** A known name, spoken as the player typed it. */
    public Optional<Known> knowsName(String spoken) {
        return known.stream().filter(entry -> TrueNameBuilder.same(entry.name(), spoken)).findFirst();
    }

    void grant(String name, String deed, String deedLine, long day) {
        this.name = name;
        this.deed = deed;
        this.deedLine = deedLine;
        this.day = day;
    }

    void clear() {
        grant("", "", "", 0L);
        shown = false;
        pendingReveal = false;
    }

    /** Adds a name; returns false if it was already known as it is. */
    boolean learn(UUID bearer, String bearerName, String name) {
        Optional<Known> was = knows(bearer);
        if (was.isPresent() && was.get().name().equals(name)) {
            return false;
        }
        was.ifPresent(known::remove);
        known.add(new Known(bearer, bearerName, name, Long.MIN_VALUE));
        return true;
    }

    void sensed(UUID bearer, long time) {
        for (int i = 0; i < known.size(); i++) {
            if (known.get(i).bearer().equals(bearer)) {
                known.set(i, known.get(i).sensedAt(time));
            }
        }
    }
}
