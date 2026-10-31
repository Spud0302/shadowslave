package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * The First Nightmare, read from the {@code first_nightmare} object of
 * {@code nightmare_trial/settings.json} (DESIGN, Jack 2026-10-29 and 2026-10-31).
 * It only matters while the {@code shadowslaveFirstNightmare} game rule is on.
 *
 * <ul>
 *   <li>{@code nightmares}: the packs the Seed inside a carrier can bloom into; one is
 *       picked at random each time. Each is played solo, as Category 1.</li>
 *   <li>{@code bloom}: what the Spell says as the Seed blooms, before the pack's own opening.</li>
 *   <li>{@code stirs}: what the Spell says when a Dormant core saturates and the Seed is ready.</li>
 *   <li>{@code withered}: what the Spell says after a death inside.</li>
 *   <li>{@code quiet_days}: after a death inside, the Seed stays quiet this many in-game days
 *       before it can bloom again (Jack, 2026-10-31). CANON has no second try (ch. 1); a
 *       wait keeps the cost without barring Awakening for good.</li>
 *   <li>{@code quiet}: what the Spell says to a carrier who sleeps by the candle while the Seed
 *       is still quiet; {@code {time}} is how long is left.</li>
 * </ul>
 */
public record FirstNightmareSettings(List<ResourceLocation> nightmares, List<String> bloom, String stirs, String withered,
                                     double quietDays, String quiet) {
    public static final FirstNightmareSettings DEFAULT = new FirstNightmareSettings(List.of(),
            List.of("The Seed the Spell planted in you blooms."),
            "The Seed the Spell planted in you stirs. Sleep beside a lit Dream Candle to face your First Nightmare.",
            "The Seed in you withers. It will grow again, but not for some days.",
            3.0, "The Seed in you is quiet. It will stir again in {time}.");

    public static final MapCodec<FirstNightmareSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("nightmares", DEFAULT.nightmares).forGetter(FirstNightmareSettings::nightmares),
            Codec.STRING.listOf().optionalFieldOf("bloom", DEFAULT.bloom).forGetter(FirstNightmareSettings::bloom),
            Codec.STRING.optionalFieldOf("stirs", DEFAULT.stirs).forGetter(FirstNightmareSettings::stirs),
            Codec.STRING.optionalFieldOf("withered", DEFAULT.withered).forGetter(FirstNightmareSettings::withered),
            Codec.doubleRange(0.0, 365.0).optionalFieldOf("quiet_days", DEFAULT.quietDays).forGetter(FirstNightmareSettings::quietDays),
            Codec.STRING.optionalFieldOf("quiet", DEFAULT.quiet).forGetter(FirstNightmareSettings::quiet)
    ).apply(instance, FirstNightmareSettings::new));

    /** Reads the {@code first_nightmare} object out of the settings file; the rest of the file is ignored. */
    public static final Codec<FirstNightmareSettings> IN_SETTINGS = MAP_CODEC.codec().optionalFieldOf("first_nightmare", DEFAULT).codec();

    public FirstNightmareSettings {
        nightmares = List.copyOf(nightmares);
        bloom = List.copyOf(bloom);
    }
}
