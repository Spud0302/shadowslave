package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.smithing.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One Weaving branch, read from {@code data/<ns>/weaving/branch/<name>.json}
 * (DESIGN). Its levels are listed in order from level 1; level N caps the
 * Memory Tier the branch can weave at N. Each level past the first is either a
 * point node (bought with Weaving points) or a deed node (earned by doing).
 */
public record WeavingBranch(String name, String description, List<Level> levels) {
    public static final Codec<WeavingBranch> CODEC = RecordCodecBuilder.<WeavingBranch>create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WeavingBranch::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(WeavingBranch::description),
            Level.CODEC.listOf().fieldOf("levels").forGetter(WeavingBranch::levels)
    ).apply(instance, WeavingBranch::new)).validate(WeavingBranch::validate);

    public WeavingBranch {
        name = Objects.requireNonNull(name, "name");
        description = Objects.requireNonNull(description, "description");
        levels = List.copyOf(levels);
    }

    private static DataResult<WeavingBranch> validate(WeavingBranch branch) {
        if (branch.levels.isEmpty()) {
            return DataResult.error(() -> "A branch needs at least level 1");
        }
        Level first = branch.levels.getFirst();
        if (first.cost() != 0 || first.deed().isPresent()) {
            return DataResult.error(() -> "Level 1 of " + branch.name + " must be free");
        }
        for (int i = 1; i < branch.levels.size(); i++) {
            Level level = branch.levels.get(i);
            if ((level.cost() > 0) == level.deed().isPresent()) {
                int number = i + 1;
                return DataResult.error(() -> "Level " + number + " of " + branch.name + " must have a cost or a deed, not both");
            }
        }
        return DataResult.success(branch);
    }

    public int maxLevel() {
        return levels.size();
    }

    /** The level with this number, counted from 1. */
    public Level level(int number) {
        return levels.get(number - 1);
    }

    /**
     * A level past the first: a point cost, or a deed to earn instead. Either
     * kind may carry {@code perks}, which work for as long as the level is held.
     */
    public record Level(int cost, Optional<Deed> deed, List<Perk> perks) {
        public static final Codec<Level> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 99).optionalFieldOf("cost", 0).forGetter(Level::cost),
                Deed.CODEC.optionalFieldOf("deed").forGetter(Level::deed),
                Perk.CODEC.listOf().optionalFieldOf("perks", List.of()).forGetter(Level::perks)
        ).apply(instance, Level::new));

        public Level {
            perks = List.copyOf(perks);
        }

        public Level(int cost, Optional<Deed> deed) {
            this(cost, deed, List.of());
        }

        public static Level free() {
            return new Level(0, Optional.empty());
        }

        public static Level points(int cost) {
            return new Level(cost, Optional.empty());
        }

        public static Level deed(Deed deed) {
            return new Level(0, Optional.of(deed));
        }

        public Level withPerks(Perk... perks) {
            return new Level(cost, deed, List.of(perks));
        }
    }

    /**
     * What a held level gives besides its Tier (DESIGN). Most kinds add up
     * across every level that grants them; {@code create} takes the highest.
     */
    public record Perk(PerkKind kind, int amount) {
        public static final Codec<Perk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PerkKind.CODEC.fieldOf("type").forGetter(Perk::kind),
                Codec.intRange(0, 1000).optionalFieldOf("amount", 1).forGetter(Perk::amount)
        ).apply(instance, Perk::new));
    }

    public enum PerkKind implements StringRepresentable {
        /** The soul holds {@code amount} more dismissed Memories. */
        SOUL_CAPACITY("soul_capacity"),
        /** Summoning a Memory costs {@code amount} percent less essence. */
        SUMMON_DISCOUNT("summon_discount"),
        /** Dismissed Memories mend {@code amount} percent more each time. */
        MEND("mend"),
        /** Two learned strands can be fused into one of your own; each part works at {@code amount} percent. */
        CREATE("create"),
        /** Pieces you forge hold {@code amount} more threads than their grade gives. */
        EXTRA_THREAD("extra_thread");

        public static final Codec<PerkKind> CODEC = StringRepresentable.fromEnum(PerkKind::values);

        private final String serializedName;

        PerkKind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    /**
     * Something to do rather than points to spend. Earned deeds are never
     * taken back by a respec.
     *
     * <p>{@code weave} counts pieces or strands of at least {@code minTier}
     * made in this branch; the optional filters narrow which count: a
     * particular {@code strand}, a forged grade of at least {@code quality},
     * a {@code tuned} strand, or a strand of the Weaver's own making
     * ({@code fused}). {@code hold} is earned by holding {@code count}
     * Memories in the soul at once.</p>
     */
    public record Deed(Kind kind, int minTier, int count, String description, Optional<ResourceLocation> strand,
                       Optional<Quality> quality, boolean tuned, boolean fused) {
        public static final Codec<Deed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Kind.CODEC.fieldOf("type").forGetter(Deed::kind),
                Codec.intRange(1, 7).optionalFieldOf("min_tier", 1).forGetter(Deed::minTier),
                Codec.intRange(1, 10_000).optionalFieldOf("count", 1).forGetter(Deed::count),
                Codec.STRING.fieldOf("description").forGetter(Deed::description),
                ResourceLocation.CODEC.optionalFieldOf("strand").forGetter(Deed::strand),
                Quality.CODEC.optionalFieldOf("quality").forGetter(Deed::quality),
                Codec.BOOL.optionalFieldOf("tuned", false).forGetter(Deed::tuned),
                Codec.BOOL.optionalFieldOf("fused", false).forGetter(Deed::fused)
        ).apply(instance, Deed::new));

        public Deed(Kind kind, int minTier, int count, String description) {
            this(kind, minTier, count, description, Optional.empty(), Optional.empty(), false, false);
        }

        /** Whether one piece or strand made in the branch counts toward this deed. */
        public boolean counts(Made made) {
            return kind == Kind.WEAVE && made.tier() >= minTier
                    && strand.map(id -> made.strand().filter(id::equals).isPresent()).orElse(true)
                    && quality.map(min -> made.quality().filter(grade -> grade.ordinal() >= min.ordinal()).isPresent()).orElse(true)
                    && (!tuned || made.tuned()) && (!fused || made.fused());
        }

        public enum Kind implements StringRepresentable {
            WEAVE("weave"),
            HOLD("hold");

            public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

            private final String serializedName;

            Kind(String serializedName) {
                this.serializedName = serializedName;
            }

            @Override
            public String getSerializedName() {
                return serializedName;
            }
        }
    }

    /** One thing made in a branch, as a deed sees it: a forged piece has a grade, a laid strand an id. */
    public record Made(int tier, Optional<ResourceLocation> strand, Optional<Quality> quality, boolean tuned, boolean fused) {
        public static Made tier(int tier) {
            return new Made(tier, Optional.empty(), Optional.empty(), false, false);
        }

        public static Made forged(int tier, Quality quality) {
            return new Made(tier, Optional.empty(), Optional.of(quality), false, false);
        }
    }
}
