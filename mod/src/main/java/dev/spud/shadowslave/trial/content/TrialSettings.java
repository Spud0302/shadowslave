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
 *   <li>{@code kill_points}, {@code solo_bonus} and {@code unbroken_bonus}: what deeds are
 *       worth in the appraisal, beside the ending's own score.</li>
 *   <li>{@code grades}: the appraisal's grades, each from a minimum score.</li>
 * </ul>
 */
public record TrialSettings(Map<Integer, Integer> categoryWeights, int gatherSeconds, int joinRange,
                            int siteNoticeRange, float siteSeedChance, Map<CreatureRank, Integer> killPoints,
                            int soloBonus, int unbrokenBonus, List<Grade> grades) {
    public record Grade(String name, int min) {
        public static final Codec<Grade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Grade::name),
                Codec.INT.fieldOf("min").forGetter(Grade::min)
        ).apply(instance, Grade::new));
    }

    public static final TrialSettings DEFAULT = new TrialSettings(
            Map.of(1, 0, 2, 30, 3, 60, 4, 80, 5, 95), 15, 12, 96, 0.5F,
            Map.of(CreatureRank.DORMANT, 2, CreatureRank.AWAKENED, 5, CreatureRank.FALLEN, 10, CreatureRank.CORRUPTED, 20),
            25, 10,
            List.of(new Grade("Passable", 0), new Grade("Commendable", 50), new Grade("Admirable", 90), new Grade("Glorious", 130)));

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
            Grade.CODEC.listOf(1, 16).optionalFieldOf("grades", DEFAULT.grades).forGetter(TrialSettings::grades)
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
}
