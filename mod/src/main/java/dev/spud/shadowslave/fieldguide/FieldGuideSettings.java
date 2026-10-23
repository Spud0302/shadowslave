package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tunables for the Field Guide, read from {@code data/<ns>/field_guide/rules.json}
 * (DESIGN, Jack 2026-10-23: every number is "for now"). Missing keys keep
 * their defaults.
 *
 * @param giveOnFirstJoin   hand every player a guide the first time they join
 * @param exploreRadius     chunks around the player the map uncovers
 * @param maxChunks         explored chunks remembered per dimension; past this the oldest fall back into fog
 * @param sightingRange     how far a spyglass can make out a creature
 * @param sightingHoldTicks how long a creature must stay in the spyglass to count
 * @param sightingCooldown  ticks before the same kind of creature counts as a new sighting
 * @param seerBonus         extra sightings a Seer gets from each look
 * @param placeRange        how close you must come to a Gate to log it
 * @param seedRange         how close you must come to a Seed to log it
 * @param dangerCell        width in blocks of one danger-rated area
 * @param deathStep         ranks a creature-caused death adds to an area's rating
 */
public record FieldGuideSettings(boolean giveOnFirstJoin, int exploreRadius, int maxChunks, int sightingRange,
                                 int sightingHoldTicks, int sightingCooldown, int seerBonus, int placeRange,
                                 int seedRange, int dangerCell, int deathStep) {
    public static final FieldGuideSettings DEFAULT = new FieldGuideSettings(true, 2, 16384, 64, 16, 600, 1, 48, 24, 64, 1);

    public static final Codec<FieldGuideSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("give_on_first_join", DEFAULT.giveOnFirstJoin).forGetter(FieldGuideSettings::giveOnFirstJoin),
            Codec.intRange(0, 8).optionalFieldOf("explore_radius", DEFAULT.exploreRadius).forGetter(FieldGuideSettings::exploreRadius),
            Codec.intRange(64, 1 << 20).optionalFieldOf("max_chunks", DEFAULT.maxChunks).forGetter(FieldGuideSettings::maxChunks),
            Codec.intRange(4, 256).optionalFieldOf("sighting_range", DEFAULT.sightingRange).forGetter(FieldGuideSettings::sightingRange),
            Codec.intRange(0, 200).optionalFieldOf("sighting_hold_ticks", DEFAULT.sightingHoldTicks).forGetter(FieldGuideSettings::sightingHoldTicks),
            Codec.intRange(0, 72000).optionalFieldOf("sighting_cooldown_ticks", DEFAULT.sightingCooldown).forGetter(FieldGuideSettings::sightingCooldown),
            Codec.intRange(0, 10).optionalFieldOf("seer_bonus", DEFAULT.seerBonus).forGetter(FieldGuideSettings::seerBonus),
            Codec.intRange(0, 512).optionalFieldOf("place_range", DEFAULT.placeRange).forGetter(FieldGuideSettings::placeRange),
            Codec.intRange(0, 512).optionalFieldOf("seed_range", DEFAULT.seedRange).forGetter(FieldGuideSettings::seedRange),
            Codec.intRange(16, 1024).optionalFieldOf("danger_cell", DEFAULT.dangerCell).forGetter(FieldGuideSettings::dangerCell),
            Codec.intRange(0, 6).optionalFieldOf("death_step", DEFAULT.deathStep).forGetter(FieldGuideSettings::deathStep)
    ).apply(instance, FieldGuideSettings::new));
}
