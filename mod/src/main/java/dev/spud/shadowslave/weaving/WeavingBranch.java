package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

    /** A level past the first: a point cost, or a deed to earn instead. */
    public record Level(int cost, Optional<Deed> deed) {
        public static final Codec<Level> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 99).optionalFieldOf("cost", 0).forGetter(Level::cost),
                Deed.CODEC.optionalFieldOf("deed").forGetter(Level::deed)
        ).apply(instance, Level::new));

        public static Level free() {
            return new Level(0, Optional.empty());
        }

        public static Level points(int cost) {
            return new Level(cost, Optional.empty());
        }

        public static Level deed(Deed deed) {
            return new Level(0, Optional.of(deed));
        }
    }

    /**
     * Something to do rather than points to spend. The only kind so far,
     * {@code weave}, counts Memories of at least {@code minTier} woven in this
     * branch. Earned deeds are never taken back by a respec.
     */
    public record Deed(Kind kind, int minTier, int count, String description) {
        public static final Codec<Deed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Kind.CODEC.fieldOf("type").forGetter(Deed::kind),
                Codec.intRange(1, 7).optionalFieldOf("min_tier", 1).forGetter(Deed::minTier),
                Codec.intRange(1, 10_000).optionalFieldOf("count", 1).forGetter(Deed::count),
                Codec.STRING.fieldOf("description").forGetter(Deed::description)
        ).apply(instance, Deed::new));

        public enum Kind {
            WEAVE;

            public static final Codec<Kind> CODEC = Codec.STRING.comapFlatMap(
                    value -> value.equals("weave") ? DataResult.success(WEAVE)
                            : DataResult.error(() -> "Unknown deed type: " + value),
                    kind -> kind.name().toLowerCase(java.util.Locale.ROOT));
        }
    }
}
