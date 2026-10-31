package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/**
 * How the Spell's Judge weighs a run, read from the {@code appraisal} object of
 * {@code nightmare_trial/settings.json} (Jack, 2026-10-31).
 *
 * <p>CANON: the grade measures how far a challenger strayed from the
 * predestined outcome, not how many they slew (ch. 744). The appraisal lists
 * the kills, then a comment line, then the grade: "You have achieved the
 * impossible!" before a glorious grade (ch. 15), "Your achievements are
 * remarkable!" before an excellent one (ch. 743). So the impossible is a
 * comment on the best glorious runs, never a grade of its own.</p>
 *
 * <ul>
 *   <li>{@code kill_weight}: the share of kill points that counts toward the score;
 *       0 lists kills without scoring them.</li>
 *   <li>{@code impossible_min} and {@code impossible_line}: a run judged at the top grade
 *       with at least this score hears this line before its grade.</li>
 *   <li>{@code acclaim}: the line said before other grades, by grade name.</li>
 * </ul>
 */
public record AppraisalSettings(float killWeight, int impossibleMin, String impossibleLine, Map<String, String> acclaim) {
    public static final AppraisalSettings DEFAULT = new AppraisalSettings(0.0F, 210, "You have achieved the impossible!",
            Map.of("Excellent", "Your achievements are remarkable!"));

    public static final MapCodec<AppraisalSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("kill_weight", DEFAULT.killWeight).forGetter(AppraisalSettings::killWeight),
            Codec.intRange(0, 100000).optionalFieldOf("impossible_min", DEFAULT.impossibleMin).forGetter(AppraisalSettings::impossibleMin),
            Codec.STRING.optionalFieldOf("impossible_line", DEFAULT.impossibleLine).forGetter(AppraisalSettings::impossibleLine),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("acclaim", DEFAULT.acclaim).forGetter(AppraisalSettings::acclaim)
    ).apply(instance, AppraisalSettings::new));

    /** Reads the {@code appraisal} object out of the settings file; the rest of the file is ignored. */
    public static final Codec<AppraisalSettings> IN_SETTINGS = MAP_CODEC.codec().optionalFieldOf("appraisal", DEFAULT).codec();

    public AppraisalSettings {
        acclaim = Map.copyOf(acclaim);
    }
}
