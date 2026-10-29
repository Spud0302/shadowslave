package dev.spud.shadowslave.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.spell.calling.CallingFrequency;

import java.util.Map;

/**
 * Tunables for the Spell's Recorder and Callings, read from
 * {@code data/<ns>/spell/settings.json} (DESIGN, every number is "for now").
 * The Judge's grade ladder and scoring live with the Nightmares, in
 * {@code nightmare_trial/settings.json}.
 *
 * @param recordCap       events the Recorder keeps before forgetting the oldest (tallies and firsts are kept for good)
 * @param checkTicks      how often each player's Callings are checked
 * @param coreNearlyFull  core saturation, as a share of its limit, that calls "nearly full"
 * @param coreReset       the share it must fall back under before that Calling can come again
 * @param seedRange       how close a Seed of your next Category must grow to call
 * @param citadelRange    how close an unclaimed Citadel must stand to call
 * @param idleMinutes     minutes of play without progress before the Spell gently calls
 * @param callingsKept    past Callings kept for the Field Guide's Callings page
 * @param gapSeconds      the least time between two Callings, for each client frequency
 */
public record SpellSettings(int recordCap, int checkTicks, float coreNearlyFull, float coreReset, int seedRange,
                            int citadelRange, int idleMinutes, int callingsKept, Map<CallingFrequency, Integer> gapSeconds) {
    public static final SpellSettings DEFAULT = new SpellSettings(2000, 40, 0.9F, 0.5F, 48, 96, 90, 64,
            Map.of(CallingFrequency.RARE, 600, CallingFrequency.NORMAL, 120, CallingFrequency.OFTEN, 30));

    public static final Codec<SpellSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(16, 1 << 20).optionalFieldOf("record_cap", DEFAULT.recordCap).forGetter(SpellSettings::recordCap),
            Codec.intRange(1, 1200).optionalFieldOf("check_ticks", DEFAULT.checkTicks).forGetter(SpellSettings::checkTicks),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("core_nearly_full", DEFAULT.coreNearlyFull).forGetter(SpellSettings::coreNearlyFull),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("core_reset", DEFAULT.coreReset).forGetter(SpellSettings::coreReset),
            Codec.intRange(0, 1024).optionalFieldOf("seed_range", DEFAULT.seedRange).forGetter(SpellSettings::seedRange),
            Codec.intRange(0, 1024).optionalFieldOf("citadel_range", DEFAULT.citadelRange).forGetter(SpellSettings::citadelRange),
            Codec.intRange(0, 10000).optionalFieldOf("idle_minutes", DEFAULT.idleMinutes).forGetter(SpellSettings::idleMinutes),
            Codec.intRange(1, 1024).optionalFieldOf("callings_kept", DEFAULT.callingsKept).forGetter(SpellSettings::callingsKept),
            Codec.unboundedMap(CallingFrequency.CODEC, Codec.intRange(0, 86400)).optionalFieldOf("gap_seconds", DEFAULT.gapSeconds)
                    .forGetter(SpellSettings::gapSeconds)
    ).apply(instance, SpellSettings::new));

    public SpellSettings {
        gapSeconds = Map.copyOf(gapSeconds);
    }

    /** The least ticks between two Callings at a frequency; never for {@link CallingFrequency#OFF}. */
    public long gapTicks(CallingFrequency frequency) {
        return gapSeconds.getOrDefault(frequency, DEFAULT.gapSeconds.getOrDefault(frequency, 120)) * 20L;
    }
}
