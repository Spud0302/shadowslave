package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tuning shared by every Long Nightmare, read from the {@code long_nightmares}
 * object of {@code nightmare_trial/settings.json} (DESIGN, Jack 2026-10-29).
 *
 * <ul>
 *   <li>{@code abandon_after_days}: a Long Nightmare nobody has walked for this many
 *       server days fades; whoever still lingers in it wakes as they entered, unjudged.</li>
 *   <li>{@code lingerer_health}: a challenger who logs out inside stays as a lingering
 *       figure with this share of the health they had; if it is slain, they have fallen.</li>
 *   <li>{@code rejoin_keeps_role}: a challenger who comes back into the same Long
 *       Nightmare keeps the role the Spell first gave them.</li>
 * </ul>
 */
public record LongNightmareSettings(int abandonAfterDays, float lingererHealth, boolean rejoinKeepsRole) {
    public static final LongNightmareSettings DEFAULT = new LongNightmareSettings(30, 1.0F, true);

    public static final MapCodec<LongNightmareSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 3650).optionalFieldOf("abandon_after_days", DEFAULT.abandonAfterDays)
                    .forGetter(LongNightmareSettings::abandonAfterDays),
            Codec.floatRange(0.05F, 4.0F).optionalFieldOf("lingerer_health", DEFAULT.lingererHealth)
                    .forGetter(LongNightmareSettings::lingererHealth),
            Codec.BOOL.optionalFieldOf("rejoin_keeps_role", DEFAULT.rejoinKeepsRole).forGetter(LongNightmareSettings::rejoinKeepsRole)
    ).apply(instance, LongNightmareSettings::new));

    /** Reads the {@code long_nightmares} object out of the settings file; the rest of the file is ignored. */
    public static final Codec<LongNightmareSettings> IN_SETTINGS = MAP_CODEC.codec().optionalFieldOf("long_nightmares", DEFAULT).codec();
}
