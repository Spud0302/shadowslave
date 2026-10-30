package dev.spud.shadowslave.spell.truename;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.CreatureRank;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A deed the Spell may answer with a True Name, and the words a name for it is
 * built from (DESIGN, Run B), read from {@code data/<ns>/true_name/deed/<id>.json}.
 * The Judge (game code) decides whether the deed was done; the words and the
 * voice's forms only decide how the name reads.
 *
 * <pre>{"trigger": "appraisal", "behaviour": "treachery",
 *  "deed": "You broke faith with your own and won anyway.",
 *  "words": {"epithet": ["Smiling"], "noun": ["Knife"], "place": ["the Last Feast"], "whole": ["Viper"]}}</pre>
 *
 * @param trigger    what kind of moment it answers
 * @param behaviour  appraisal: how the challenger played, as the Judge read it; empty for any
 * @param first      first: a pattern the server first's key must match ("kill_great_.*")
 * @param minRank    kill: the least rank the creature must have
 * @param minClass   kill: the least class the creature must have
 * @param aboveRank  kill: the creature must stand above the slayer's own rank
 * @param aloneRange kill: no other player may stand this close; 0 to not ask
 * @param chance     the chance the Spell answers the deed when it is done (an appraisal's grade chance comes on top)
 * @param priority   when several deeds answer one moment, the highest wins
 * @param deed       the line the Spell says the name was granted for
 * @param words      word lists a name is built from: epithet, noun, place, whole, or any slot a form names
 */
public record TrueNameDeed(Trigger trigger, String behaviour, String first, Optional<CreatureRank> minRank,
                           Optional<CreatureClass> minClass, boolean aboveRank, int aloneRange, float chance, int priority,
                           String deed, Map<String, List<String>> words) {

    public enum Trigger implements StringRepresentable {
        /** A Nightmare's appraisal at a grade that opens the chance. */
        APPRAISAL,
        /** A server first, by its key in the Spell's record. */
        FIRST,
        /** Slaying a Nightmare Creature outside a Nightmare. */
        KILL;

        public static final Codec<Trigger> CODEC = StringRepresentable.fromEnum(Trigger::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<TrueNameDeed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Trigger.CODEC.fieldOf("trigger").forGetter(TrueNameDeed::trigger),
            Codec.STRING.optionalFieldOf("behaviour", "").forGetter(TrueNameDeed::behaviour),
            Codec.STRING.optionalFieldOf("first", "").forGetter(TrueNameDeed::first),
            CreatureRank.CODEC.optionalFieldOf("min_rank").forGetter(TrueNameDeed::minRank),
            CreatureClass.CODEC.optionalFieldOf("min_class").forGetter(TrueNameDeed::minClass),
            Codec.BOOL.optionalFieldOf("above_rank", false).forGetter(TrueNameDeed::aboveRank),
            Codec.intRange(0, 512).optionalFieldOf("alone_range", 0).forGetter(TrueNameDeed::aloneRange),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(TrueNameDeed::chance),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(TrueNameDeed::priority),
            Codec.STRING.fieldOf("deed").forGetter(TrueNameDeed::deed),
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).fieldOf("words").forGetter(TrueNameDeed::words)
    ).apply(instance, TrueNameDeed::new));

    public TrueNameDeed {
        words = Map.copyOf(words);
    }

    /** Whether a server first's key answers this deed. A malformed pattern answers nothing. */
    public boolean matchesFirst(String key) {
        if (trigger != Trigger.FIRST || first.isEmpty()) {
            return false;
        }
        try {
            return Pattern.compile(first).matcher(key).matches();
        } catch (PatternSyntaxException exception) {
            return false;
        }
    }

    /** Whether a kill answers this deed, given what was slain and how. */
    public boolean matchesKill(CreatureRank rank, CreatureClass creatureClass, boolean above, boolean alone) {
        return trigger == Trigger.KILL
                && minRank.map(least -> rank.ordinal() >= least.ordinal()).orElse(true)
                && minClass.map(least -> creatureClass.ordinal() >= least.ordinal()).orElse(true)
                && (!aboveRank || above)
                && (aloneRange <= 0 || alone);
    }

    /** Whether an appraisal that read {@code behaviourId} answers this deed. */
    public boolean matchesAppraisal(String behaviourId) {
        return trigger == Trigger.APPRAISAL && (behaviour.isEmpty() || behaviour.equals(behaviourId));
    }
}
