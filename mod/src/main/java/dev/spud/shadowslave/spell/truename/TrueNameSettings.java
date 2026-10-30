package dev.spud.shadowslave.spell.truename;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/**
 * Tunables for True Names, read from {@code data/<ns>/true_name/settings.json}
 * (DESIGN, Run B; every number is "for now").
 *
 * @param gradeChance      the chance a conquered Nightmare grants a True Name, by grade; grades not named grant none
 *                         (the chance only opens at Glorious, per the Spell Callings section)
 * @param drainResist      the share of soul and essence drains an Anchored Soul shrugs off
 * @param knownDamageBonus extra damage, as a share, from anyone who knows the bearer's True Name
 * @param senseCooldown    ticks before one knower can sense the same bearer again (one in-game day)
 * @param revealRange      how close a player must stand to hear a True Name spoken to them
 * @param callingGapFactor how much a bearer's least gap between Callings shrinks
 * @param callingReach     how much further a bearer's Callings reach for Seeds and Citadels
 * @param bindEssence      essence it takes to weave your True Name into a Memory
 * @param nameAttempts     tries at a name nobody else bears before the Spell gives up for now
 */
public record TrueNameSettings(Map<String, Float> gradeChance, float drainResist, float knownDamageBonus, int senseCooldown,
                               int revealRange, float callingGapFactor, float callingReach, int bindEssence, int nameAttempts) {
    public static final TrueNameSettings DEFAULT = new TrueNameSettings(Map.of("Glorious", 0.35F, "Impossible", 1.0F),
            0.5F, 0.1F, 24000, 8, 0.5F, 1.5F, 40, 12);

    public static final Codec<TrueNameSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.floatRange(0.0F, 1.0F)).optionalFieldOf("grade_chance", DEFAULT.gradeChance)
                    .forGetter(TrueNameSettings::gradeChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("drain_resist", DEFAULT.drainResist).forGetter(TrueNameSettings::drainResist),
            Codec.floatRange(0.0F, 4.0F).optionalFieldOf("known_damage_bonus", DEFAULT.knownDamageBonus)
                    .forGetter(TrueNameSettings::knownDamageBonus),
            Codec.intRange(0, 24000 * 30).optionalFieldOf("sense_cooldown_ticks", DEFAULT.senseCooldown)
                    .forGetter(TrueNameSettings::senseCooldown),
            Codec.intRange(1, 256).optionalFieldOf("reveal_range", DEFAULT.revealRange).forGetter(TrueNameSettings::revealRange),
            Codec.floatRange(0.05F, 1.0F).optionalFieldOf("calling_gap_factor", DEFAULT.callingGapFactor)
                    .forGetter(TrueNameSettings::callingGapFactor),
            Codec.floatRange(1.0F, 8.0F).optionalFieldOf("calling_reach", DEFAULT.callingReach).forGetter(TrueNameSettings::callingReach),
            Codec.intRange(0, 10000).optionalFieldOf("bind_essence", DEFAULT.bindEssence).forGetter(TrueNameSettings::bindEssence),
            Codec.intRange(1, 100).optionalFieldOf("name_attempts", DEFAULT.nameAttempts).forGetter(TrueNameSettings::nameAttempts)
    ).apply(instance, TrueNameSettings::new));

    public TrueNameSettings {
        gradeChance = Map.copyOf(gradeChance);
    }

    /** The chance a grade grants a True Name; 0 for a grade the settings do not name. */
    public float chanceFor(String grade) {
        return gradeChance.getOrDefault(grade, 0.0F);
    }
}
