package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;
import dev.spud.shadowslave.smithing.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Pure rules for Weaving strands (DESIGN, after ch. 2048 and 3024): how many
 * strands a piece holds, how high a Tier a Weaver can reach, what a weave
 * costs and how the essence is found, and how a traced pattern is judged.
 * The server decides every outcome with these.
 */
public final class StrandRules {
    public static final int MAX_TIER = 3;
    public static final int MIN_POINTS = 3;
    public static final int MAX_POINTS = 7;
    /** The most trace samples a client may send. */
    public static final int TRACE_LIMIT = 256;
    /** Ticks of study to pick a pattern out of a weave, and the essence it burns. */
    public static final int STUDY_TICKS = 60;
    public static final int STUDY_ESSENCE = 10;
    /** A trace scoring this or better lays the strand at full strength; below {@code LOOSE} it fizzles. */
    public static final float CLEAN = 0.85F;
    public static final float LOOSE = 0.5F;
    /** A tuned strand works this much harder while its condition holds. */
    public static final float TUNED = 1.5F;
    /** Essence an unravelling costs per Tier of the strand pulled out. */
    public static final int UNRAVEL_ESSENCE = 15;
    /** The share of a strand's potency kept when it is unravelled by a Forger below level 2. */
    public static final int UNRAVEL_KEEP = 75;
    /** Essence it takes to lay a ready thread into a piece. */
    public static final int LAY_ESSENCE = 10;
    /** Essence it takes to summon a Memory out of the soul (lore: rebuilt from essence, ch. 3024). */
    public static final int SUMMON_ESSENCE = 5;
    /** Wellspring pays this share of Weaving's essence costs. */
    public static final int WELLSPRING_PERCENT = 75;
    /** Ticks between each mend of a dismissed Memory, and the share of its durability each mend restores, in percent. */
    public static final int MEND_TICKS = 40;
    public static final int MEND_PERCENT = 2;
    /** The trace tolerance at Patterns level 1, in anchor radii, what each level adds, and how each Tier tightens it. */
    static final float BASE_TOLERANCE = 0.16F;
    static final float TOLERANCE_PER_LEVEL = 0.02F;
    private static final float[] TIER_TOLERANCE = {1.0F, 0.8F, 0.65F};
    /** A pattern point this many tolerances from the trace counts for nothing. */
    static final float MISS_SPAN = 3.0F;

    private StrandRules() {
    }

    // Threads

    /** How many strands a piece holds: its forged grade's threads, or one for any plain piece a strand fits. */
    public static int threads(Optional<Quality> forged, boolean fits) {
        if (!fits) {
            return 0;
        }
        return forged.map(Quality::threads).orElse(1);
    }

    // Caps

    /** The highest Tier a soul can weave: Tier I for a Sleeper or unranked soul, II Awakened, III Ascended and up. */
    public static int soulCap(Optional<SoulRank> soul) {
        int ordinal = soul.map(Enum::ordinal).orElse(SoulRank.DORMANT.ordinal());
        return Mth.clamp(ordinal - SoulRank.DORMANT.ordinal() + 1, 1, MAX_TIER);
    }

    /** The anchor's Rank caps the strand's: a Dormant shard holds Tier I, Awakened II, Fallen and up III. */
    public static int anchorCap(Optional<ShardRank> anchor) {
        return anchor.map(rank -> Math.min(MAX_TIER, rank.tier() + 1)).orElse(0);
    }

    /** The highest Tier this Weaver may lay with this anchor; 0 means none at all. */
    public static int tierCap(Optional<SoulRank> soul, int patternsLevel, Optional<ShardRank> anchor) {
        return Math.min(Math.min(soulCap(soul), Math.max(0, patternsLevel)), anchorCap(anchor));
    }

    // Paying

    /** Essence one shard gives when a weave burns it; a faint shard from an ordinary creature gives little. */
    public static int shardEssence(ShardRank rank, boolean faint) {
        if (faint) {
            return 3;
        }
        return switch (rank) {
            case DORMANT -> 10;
            case AWAKENED -> 40;
            case FALLEN -> 160;
            case CORRUPTED -> 480;
        };
    }

    /** How a cost is met: from the Weaver's own essence first, then by burning shards (lore: Valor's Weavers, ch. 2048). */
    public record Payment(int essence, int shards) {
    }

    /**
     * Pays {@code cost} from {@code essence} and up to {@code spareShards}
     * shards worth {@code shardValue} each. Empty if both together fall short.
     */
    public static Optional<Payment> pay(int cost, int essence, int spareShards, int shardValue) {
        int fromPool = Math.min(Math.max(0, essence), cost);
        int rest = cost - fromPool;
        if (rest == 0) {
            return Optional.of(new Payment(fromPool, 0));
        }
        if (shardValue <= 0) {
            return Optional.empty();
        }
        int shards = (rest + shardValue - 1) / shardValue;
        return shards <= spareShards ? Optional.of(new Payment(fromPool, shards)) : Optional.empty();
    }

    // Patterns and tracing

    /** The strand's anchor points: from its file, or drawn from its id so every strand has its own shape. */
    public static List<StrandPattern.Point> points(ResourceLocation id, StrandPattern strand) {
        if (!strand.pattern().isEmpty()) {
            return strand.pattern();
        }
        int count = Mth.clamp(MIN_POINTS + strand.maxTier(), MIN_POINTS, MAX_POINTS);
        List<StrandPattern.Point> ring = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8 - Math.PI / 2;
            ring.add(new StrandPattern.Point((float) (Math.cos(angle) * 0.8), (float) (Math.sin(angle) * 0.8)));
        }
        ring.add(new StrandPattern.Point(0.0F, 0.0F));
        Random random = new Random(id.toString().hashCode());
        List<StrandPattern.Point> chosen = new ArrayList<>();
        while (chosen.size() < count) {
            StrandPattern.Point next = ring.remove(random.nextInt(ring.size()));
            chosen.add(next);
        }
        return chosen;
    }

    /** How far off a trace may pass a point and still hit it cleanly, in anchor radii. */
    public static float tolerance(int patternsLevel, int tier) {
        float base = BASE_TOLERANCE + TOLERANCE_PER_LEVEL * Math.max(0, patternsLevel - 1);
        return base * TIER_TOLERANCE[Mth.clamp(tier, 1, MAX_TIER) - 1];
    }

    /**
     * How well a trace follows the pattern, 0 to 1. The trace must pass the
     * points in order: each point is matched to a later sample than the
     * point before it, choosing the matches that score most. A point within {@code tolerance} scores fully, and
     * nothing from {@link #MISS_SPAN} tolerances away. {@code trace} holds
     * x, y pairs in anchor radii.
     */
    public static float score(List<StrandPattern.Point> points, float[] trace, float tolerance) {
        int samples = trace.length / 2;
        if (points.isEmpty() || samples == 0) {
            return 0.0F;
        }
        // best[i]: the most the points so far can score with the last one matched at or before sample i.
        // Each point takes a later sample than the one before it.
        // Matching each point to its nearest sample greedily fails patterns that cross back over an
        // earlier point, so the samples are matched in order to whichever assignment scores most.
        float[] best = new float[samples];
        for (int p = 0; p < points.size(); p++) {
            StrandPattern.Point point = points.get(p);
            float[] next = new float[samples];
            float running = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < samples; i++) {
                float off = (float) ((point.distance(trace[i * 2], trace[i * 2 + 1]) - tolerance) / (tolerance * (MISS_SPAN - 1.0F)));
                float before = p == 0 ? 0.0F : i == 0 ? Float.NEGATIVE_INFINITY : best[i - 1];
                float here = Mth.clamp(1.0F - off, 0.0F, 1.0F) + before;
                running = Math.max(running, here);
                next[i] = running;
            }
            best = next;
        }
        return Math.max(0.0F, best[samples - 1] / points.size());
    }

    public enum Trace {
        CLEAN, LOOSE, FIZZLE
    }

    public static Trace judge(float score) {
        return score >= CLEAN ? Trace.CLEAN : score >= LOOSE ? Trace.LOOSE : Trace.FIZZLE;
    }

    /** The potency a traced strand is laid at: full when clean, its score when loose. */
    public static int potency(float score) {
        return judge(score) == Trace.CLEAN ? 100 : Mth.clamp(Math.round(score * 100), 1, 99);
    }

    // Strength

    /** What a laid strand gives: its Tier's value, scaled by potency, and half again if tuned. */
    public static float strength(StrandPattern strand, WovenStrands.Laid laid) {
        int tier = Mth.clamp(laid.tier(), 1, strand.maxTier());
        float value = strand.effect().values().get(tier - 1) * laid.potency() / 100.0F;
        return laid.restriction().isPresent() ? value * TUNED : value;
    }

    /** Whether a Weaver knows {@code strand}: every Weaver knows a starter strand, and the rest are studied. */
    public static boolean knows(Set<ResourceLocation> studied, ResourceLocation id, StrandPattern strand) {
        return strand.starter() || studied.contains(id);
    }

    /** An essence cost at {@code percent} of itself, rounded up so nothing becomes free by rounding. */
    public static int discounted(int cost, int percent) {
        return (cost * percent + 99) / 100;
    }

    /** How many Memories a soul can hold dismissed: three to an unranked or Dormant soul, two more for each Rank above. */
    public static int soulCapacity(Optional<SoulRank> rank) {
        return 3 + 2 * rank.map(value -> Math.max(0, value.ordinal() - SoulRank.DORMANT.ordinal())).orElse(0);
    }

    /** Durability one mend restores to a piece that can take {@code maxDamage}: a small share, never less than one. */
    public static int mend(int maxDamage) {
        return Math.max(1, maxDamage * MEND_PERCENT / 100);
    }

    /** XP for laying a strand: its XP times its Tier, five times over the first time (as patterns at the forge). */
    public static int xp(StrandPattern strand, int tier, boolean first, int firstMultiplier) {
        return strand.xp() * tier * (first ? firstMultiplier : 1);
    }

    /**
     * Credits one strand laid: XP in {@code branch} (five times over the
     * first time this strand is laid), the count, and any deeds it earns.
     */
    public static WeavingRules.Credit credit(WeavingContent content, WeavingData data, ResourceLocation id, StrandPattern strand,
                                             int tier, ResourceLocation branch) {
        int xp = xp(strand, tier, data.laid(id) == 0, content.settings().firstWeaveMultiplier());
        WeavingData next = data.withLaid(id).withXp(branch, xp);
        List<String> deeds = new ArrayList<>();
        next = WeavingRules.advanceDeeds(content, next, branch, tier, deeds);
        return new WeavingRules.Credit(next, xp, deeds);
    }

    /** The potency a strand keeps when unravelled into a thread. */
    public static int unravelled(int potency, int forgeryLevel) {
        return forgeryLevel >= 2 ? potency : Math.max(1, potency * UNRAVEL_KEEP / 100);
    }
}
