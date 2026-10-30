package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The Nightmare Scar, read from the {@code scar} object of
 * {@code nightmare_trial/settings.json} (DESIGN; Jack 2026-10-29 left it open,
 * so it is a server option, {@code shadowslaveNightmareScar}, off by default).
 *
 * <p>With the option on, each death inside a Nightmare cuts one scar into the
 * soul, up to {@code max_stacks}. Every scar costs {@code health_per_stack}
 * max health and {@code damage_per_stack} of your attack damage (a fraction),
 * and all of them close {@code minutes} after the last one was cut. With
 * {@code cleared_heals}, conquering a Nightmare closes one scar.</p>
 */
public record ScarSettings(int minutes, int maxStacks, double healthPerStack, double damagePerStack, boolean clearedHeals) {
    public static final ScarSettings DEFAULT = new ScarSettings(30, 3, 2.0, 0.05, true);

    public static final MapCodec<ScarSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 24 * 60).optionalFieldOf("minutes", DEFAULT.minutes).forGetter(ScarSettings::minutes),
            Codec.intRange(1, 10).optionalFieldOf("max_stacks", DEFAULT.maxStacks).forGetter(ScarSettings::maxStacks),
            Codec.doubleRange(0.0, 10.0).optionalFieldOf("health_per_stack", DEFAULT.healthPerStack).forGetter(ScarSettings::healthPerStack),
            Codec.doubleRange(0.0, 0.3).optionalFieldOf("damage_per_stack", DEFAULT.damagePerStack).forGetter(ScarSettings::damagePerStack),
            Codec.BOOL.optionalFieldOf("cleared_heals", DEFAULT.clearedHeals).forGetter(ScarSettings::clearedHeals)
    ).apply(instance, ScarSettings::new));

    /** Reads the {@code scar} object out of the settings file; the rest of the file is ignored. */
    public static final Codec<ScarSettings> IN_SETTINGS = MAP_CODEC.codec().optionalFieldOf("scar", DEFAULT).codec();
}
