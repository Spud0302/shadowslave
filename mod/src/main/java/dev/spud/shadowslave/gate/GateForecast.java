package dev.spud.shadowslave.gate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * The Category forecast the warning shows before a Gate opens, in the
 * canon form "Category 2 (89%), 3 (10%), higher (undefined)" (ch. 484), and
 * the roll that picks the real Category when it opens.
 *
 * <p>For a Gate Lure the forecast comes from its pull, the shard value fed to
 * it (DESIGN): {@link #PULL_BANDS} split the pull into Categories, and the
 * further a pull sits into its band, the likelier the next Category up.</p>
 */
public record GateForecast(Map<Integer, Integer> odds) {
    /** Lowest pull of Categories 2, 3, 4 and 5; Category 1 starts at {@link #MIN_PULL}. */
    public static final int[] PULL_BANDS = {8, 24, 64, 160};
    /** A Lure needs at least this much pull to open anything. */
    public static final int MIN_PULL = 4;

    public GateForecast {
        odds = Map.copyOf(odds);
        int total = odds.values().stream().mapToInt(Integer::intValue).sum();
        if (total != 100 || odds.isEmpty()) {
            throw new IllegalArgumentException("Forecast odds must add up to 100: " + odds);
        }
    }

    /** A certain Category, e.g. for an operator command. */
    public static GateForecast certain(int category) {
        return new GateForecast(Map.of(GateCategory.clamp(category), 100));
    }

    /** Pull of one shard of a rank tier (0 Dormant .. 3 Corrupted): a quarter of that for a faint shard. */
    public static int pullQuarters(int rankTier, boolean faint) {
        int quarters = 4 << (2 * rankTier);
        return faint ? Math.max(1, quarters / 4) : quarters;
    }

    /** The forecast for a Lure holding {@code pull} points of shard value. */
    public static GateForecast fromPull(int pull) {
        int category = 1;
        int low = MIN_PULL;
        int high = PULL_BANDS[0];
        for (int i = 0; i < PULL_BANDS.length; i++) {
            if (pull >= PULL_BANDS[i]) {
                category = i + 2;
                low = PULL_BANDS[i];
                high = i + 1 < PULL_BANDS.length ? PULL_BANDS[i + 1] : PULL_BANDS[i] * 3;
            }
        }
        double into = Math.max(0.0, Math.min(1.0, (double) (pull - low) / (high - low)));
        Map<Integer, Integer> odds = new LinkedHashMap<>();
        int higher = category < GateCategory.MAX ? (int) Math.round(3 + 37 * into * into) : 0;
        int lower = category > GateCategory.MIN ? (int) Math.round(8 * (1.0 - into)) : 0;
        if (lower > 0) {
            odds.put(category - 1, lower);
        }
        odds.put(category, 100 - higher - lower);
        if (higher > 0) {
            odds.put(category + 1, higher);
        }
        return new GateForecast(odds);
    }

    /** The likeliest Category. */
    public int likeliest() {
        return odds.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(1);
    }

    /** Rolls the Category the Gate really opens at. */
    public int roll(IntUnaryOperator nextInt) {
        int pick = nextInt.applyAsInt(100);
        for (int category : sortedCategories()) {
            pick -= odds.get(category);
            if (pick < 0) {
                return category;
            }
        }
        return likeliest();
    }

    private List<Integer> sortedCategories() {
        List<Integer> categories = new ArrayList<>(odds.keySet());
        categories.sort(Integer::compareTo);
        return categories;
    }

    /** "Category 2 (89%), 3 (10%), higher (undefined)", likeliest first like the canon warning. */
    public String describe() {
        return describeOdds() + ", higher (undefined)";
    }

    /** "Category 2 (89%)": the likeliest Category alone, for a short line like the boss bar. */
    public String describeLikeliest() {
        int category = likeliest();
        return "Category " + category + " (" + odds.get(category) + "%)";
    }

    /** "Category 2 (89%), 3 (10%)": the odds without the canon tail, for the action bar. */
    public String describeOdds() {
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(odds.entrySet());
        entries.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());
        StringBuilder text = new StringBuilder("Category ");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(entries.get(i).getKey()).append(" (").append(entries.get(i).getValue()).append("%)");
        }
        return text.toString();
    }
}
