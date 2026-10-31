package dev.spud.shadowslave.trial.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.gate.GateCategory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tuning shared by every Nightmare, read from
 * {@code data/<ns>/nightmare_trial/settings.json} (DESIGN).
 *
 * <ul>
 *   <li>{@code category_weights}: the lowest echo-site weight for each Category.</li>
 *   <li>{@code gather_seconds} and {@code join_range}: after a Seed is challenged it
 *       wakes this many seconds later, and everyone this close is drawn in.</li>
 *   <li>{@code site_notice_range} and {@code site_seed_chance}: an echo site raises its
 *       ruin and may grow a Seed when a dreamer comes this close.</li>
 *   <li>{@code kill_points}: what each kill is worth, which only counts toward the grade as
 *       far as the {@code appraisal} object's {@code kill_weight} says (none by default).</li>
 *   <li>{@code solo_bonus} and {@code unbroken_bonus}: what going alone and losing nobody are
 *       worth in the appraisal, beside how far the ending strayed from fate.</li>
 *   <li>{@code unique_bonus}, {@code above_rank_bonus}: extra for each unique creature (a Tyrant
 *       or stronger, or a named one) and for each kill of a creature above your own rank.</li>
 *   <li>{@code small_cohort_size} and {@code small_cohort_bonus}: a cohort this small or smaller
 *       (but not alone) earns this much.</li>
 *   <li>{@code loss_penalty} and {@code treachery_penalty}: taken off for each of your cohort who
 *       fell, and for each time you struck one of your own.</li>
 *   <li>{@code grades}: the appraisal's grades for survivors, each from a minimum score; the
 *       Judge's ladder runs Poor up to Glorious, the top (ch. 15, 743). "You have achieved the
 *       impossible!" is a comment on the best glorious runs, set in {@link AppraisalSettings}.
 *       {@code failed_grade} is what a challenger who died inside is judged.</li>
 * </ul>
 *
 * <p>Each kill is worth its rank's {@code kill_points} times the creature's soul cores
 * (one for a Beast, five for a Tyrant).</p>
 */
public record TrialSettings(Map<Integer, Integer> categoryWeights, int gatherSeconds, int joinRange,
                            int siteNoticeRange, float siteSeedChance, Map<CreatureRank, Integer> killPoints,
                            int soloBonus, int unbrokenBonus, int uniqueBonus, int aboveRankBonus, int smallCohortSize,
                            int smallCohortBonus, int lossPenalty, int treacheryPenalty, List<Grade> grades, String failedGrade) {
    public record Grade(String name, int min) {
        public static final Codec<Grade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Grade::name),
                Codec.INT.fieldOf("min").forGetter(Grade::min)
        ).apply(instance, Grade::new));
    }

    public static final TrialSettings DEFAULT = new TrialSettings(
            Map.of(1, 0, 2, 30, 3, 60, 4, 80, 5, 95), 15, 12, 96, 0.5F,
            Map.of(CreatureRank.DORMANT, 2, CreatureRank.AWAKENED, 5, CreatureRank.FALLEN, 10, CreatureRank.CORRUPTED, 20),
            25, 10, 20, 10, 3, 10, 10, 30,
            List.of(new Grade("Poor", 0), new Grade("Adequate", 40), new Grade("Good", 80), new Grade("Excellent", 120),
                    new Grade("Glorious", 170)),
            "Failed");

    private static final Codec<Map<Integer, Integer>> CATEGORY_WEIGHTS = Codec.unboundedMap(Codec.STRING, Codec.intRange(0, 100))
            .comapFlatMap(raw -> {
                Map<Integer, Integer> weights = new TreeMap<>();
                for (Map.Entry<String, Integer> entry : raw.entrySet()) {
                    int category;
                    try {
                        category = Integer.parseInt(entry.getKey());
                    } catch (NumberFormatException exception) {
                        return DataResult.error(() -> "Not a Category: " + entry.getKey());
                    }
                    if (category < GateCategory.MIN || category > GateCategory.MAX) {
                        return DataResult.error(() -> "Category out of range: " + category);
                    }
                    weights.put(category, entry.getValue());
                }
                return weights.isEmpty() ? DataResult.error(() -> "category_weights is empty") : DataResult.success(weights);
            }, weights -> {
                Map<String, Integer> raw = new TreeMap<>();
                weights.forEach((category, weight) -> raw.put(Integer.toString(category), weight));
                return raw;
            });

    public static final Codec<TrialSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CATEGORY_WEIGHTS.optionalFieldOf("category_weights", DEFAULT.categoryWeights).forGetter(TrialSettings::categoryWeights),
            Codec.intRange(0, 600).optionalFieldOf("gather_seconds", DEFAULT.gatherSeconds).forGetter(TrialSettings::gatherSeconds),
            Codec.intRange(1, 128).optionalFieldOf("join_range", DEFAULT.joinRange).forGetter(TrialSettings::joinRange),
            Codec.intRange(8, 1024).optionalFieldOf("site_notice_range", DEFAULT.siteNoticeRange).forGetter(TrialSettings::siteNoticeRange),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("site_seed_chance", DEFAULT.siteSeedChance).forGetter(TrialSettings::siteSeedChance),
            Codec.unboundedMap(CreatureRank.CODEC, Codec.intRange(0, 10000)).optionalFieldOf("kill_points", DEFAULT.killPoints)
                    .forGetter(TrialSettings::killPoints),
            Codec.intRange(0, 10000).optionalFieldOf("solo_bonus", DEFAULT.soloBonus).forGetter(TrialSettings::soloBonus),
            Codec.intRange(0, 10000).optionalFieldOf("unbroken_bonus", DEFAULT.unbrokenBonus).forGetter(TrialSettings::unbrokenBonus),
            Codec.intRange(0, 10000).optionalFieldOf("unique_bonus", DEFAULT.uniqueBonus).forGetter(TrialSettings::uniqueBonus),
            Codec.intRange(0, 10000).optionalFieldOf("above_rank_bonus", DEFAULT.aboveRankBonus).forGetter(TrialSettings::aboveRankBonus),
            Codec.intRange(1, 64).optionalFieldOf("small_cohort_size", DEFAULT.smallCohortSize).forGetter(TrialSettings::smallCohortSize),
            Codec.intRange(0, 10000).optionalFieldOf("small_cohort_bonus", DEFAULT.smallCohortBonus).forGetter(TrialSettings::smallCohortBonus),
            Codec.intRange(0, 10000).optionalFieldOf("loss_penalty", DEFAULT.lossPenalty).forGetter(TrialSettings::lossPenalty),
            Codec.intRange(0, 10000).optionalFieldOf("treachery_penalty", DEFAULT.treacheryPenalty).forGetter(TrialSettings::treacheryPenalty),
            Grade.CODEC.listOf(1, 16).optionalFieldOf("grades", DEFAULT.grades).forGetter(TrialSettings::grades),
            Codec.STRING.optionalFieldOf("failed_grade", DEFAULT.failedGrade).forGetter(TrialSettings::failedGrade)
    ).apply(instance, TrialSettings::new));

    public TrialSettings {
        categoryWeights = Map.copyOf(categoryWeights);
        killPoints = Map.copyOf(killPoints);
        grades = grades.stream().sorted(Comparator.comparingInt(Grade::min)).toList();
    }

    /** The Category of a Seed at an echo site of this weight: the highest one whose minimum weight it reaches. */
    public int categoryFor(int weight) {
        return categoryWeights.entrySet().stream()
                .filter(entry -> weight >= entry.getValue())
                .mapToInt(Map.Entry::getKey)
                .max()
                .orElse(GateCategory.MIN);
    }

    /** The grade a score earns: the highest one whose minimum it reaches. */
    public Grade gradeFor(int score) {
        Grade best = grades.get(0);
        for (Grade grade : grades) {
            if (score >= grade.min()) {
                best = grade;
            }
        }
        return best;
    }

    public int pointsFor(CreatureRank rank) {
        return killPoints.getOrDefault(rank, 1);
    }

    /** A grade's key for line tables and saved records: its name in lower case, spaces as underscores. */
    public static String key(String grade) {
        return grade.toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
    }

    /** How high a grade stands on the ladder: its place, from 0; -1 for the failed grade or one not on it. */
    public int standing(String grade) {
        for (int i = 0; i < grades.size(); i++) {
            if (key(grades.get(i).name()).equals(key(grade))) {
                return i;
            }
        }
        return -1;
    }
}
