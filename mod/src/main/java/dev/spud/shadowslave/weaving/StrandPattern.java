package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One strand a Weaver can learn and lay into a piece, read from
 * {@code data/<ns>/weaving/strand/<name>.json} (DESIGN, after Sunny's
 * Weaving, ch. 2048 and 3024). A strand is learned by studying a weave that
 * holds it ({@code study}), then traced onto a soul-shard anchor at the
 * Weaving Table, spending {@code essence} and {@code material} for the Tier
 * chosen. Its effect is one of a small set of kinds in code; the numbers
 * come from here, one per Tier.
 *
 * @param pattern the anchor points to trace, in order, each within the unit circle; empty means one is drawn from the id
 * @param starter known by every Weaver from the start, without study
 */
public record StrandPattern(String name, String description, List<StrandFit> fits, Effect effect, List<Source> study,
                            Material material, List<Integer> essence, SoulRank minRank, List<Point> pattern, int xp,
                            boolean starter) {
    public static final Codec<StrandPattern> CODEC = RecordCodecBuilder.<StrandPattern>create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(StrandPattern::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(StrandPattern::description),
            StrandFit.CODEC.listOf().fieldOf("fits").forGetter(StrandPattern::fits),
            Effect.CODEC.fieldOf("effect").forGetter(StrandPattern::effect),
            Source.CODEC.listOf().optionalFieldOf("study", List.of()).forGetter(StrandPattern::study),
            Material.CODEC.fieldOf("material").forGetter(StrandPattern::material),
            Codec.intRange(0, 10_000).listOf().fieldOf("essence").forGetter(StrandPattern::essence),
            SoulRank.CODEC.optionalFieldOf("min_rank", SoulRank.DORMANT).forGetter(StrandPattern::minRank),
            Point.CODEC.listOf().optionalFieldOf("pattern", List.of()).forGetter(StrandPattern::pattern),
            Codec.intRange(0, 10_000).optionalFieldOf("xp", 20).forGetter(StrandPattern::xp),
            Codec.BOOL.optionalFieldOf("starter", false).forGetter(StrandPattern::starter)
    ).apply(instance, StrandPattern::new)).validate(StrandPattern::validate);

    public StrandPattern {
        name = Objects.requireNonNull(name, "name");
        description = Objects.requireNonNull(description, "description");
        fits = List.copyOf(fits);
        study = List.copyOf(study);
        essence = List.copyOf(essence);
        pattern = List.copyOf(pattern);
    }

    private static DataResult<StrandPattern> validate(StrandPattern strand) {
        int tiers = strand.effect.values().size();
        if (tiers < 1 || tiers > StrandRules.MAX_TIER) {
            return DataResult.error(() -> strand.name + " needs one to " + StrandRules.MAX_TIER + " effect values, one per Tier");
        }
        if (strand.essence.size() != tiers || strand.material.counts().size() != tiers) {
            return DataResult.error(() -> strand.name + " needs an essence cost and a material count for each of its " + tiers + " Tiers");
        }
        if (strand.fits.isEmpty()) {
            return DataResult.error(() -> strand.name + " fits nothing");
        }
        if (!strand.pattern.isEmpty() && (strand.pattern.size() < StrandRules.MIN_POINTS || strand.pattern.size() > StrandRules.MAX_POINTS)) {
            return DataResult.error(() -> strand.name + " needs " + StrandRules.MIN_POINTS + " to " + StrandRules.MAX_POINTS + " pattern points");
        }
        if (strand.effect.kind() == Kind.ATTRIBUTE && strand.effect.attribute().isEmpty()) {
            return DataResult.error(() -> strand.name + " is an attribute strand with no attribute");
        }
        if (strand.effect.kind() == Kind.RESIST && strand.effect.damageTag().isEmpty()) {
            return DataResult.error(() -> strand.name + " resists nothing: it needs a damage_tag");
        }
        return DataResult.success(strand);
    }

    public int maxTier() {
        return effect.values().size();
    }

    public boolean fits(StrandFit fit) {
        return fits.contains(fit);
    }

    /** Attribute strands are always on, and the Memory weave and Bound are what a piece is, so none of them can be tied to a condition. */
    public boolean tunable() {
        return effect.kind() != Kind.ATTRIBUTE && !effect.kind().foundational();
    }

    /** What a strand does. Each kind's numbers mean something different; see {@link StrandEffects}. */
    public enum Kind implements StringRepresentable {
        /** An attribute bonus while worn or held: {@code values} is the amount. */
        ATTRIBUTE("attribute"),
        /** Hits and broken blocks sometimes cost no durability: {@code values} is the chance. */
        KEEN("keen"),
        /** A hit drags the foe toward you: {@code values} is the pull's strength. */
        PULL("pull"),
        /** A hit repeats faintly a moment later: {@code values} is the share of the hit. */
        ECHO("echo"),
        /** A heavy hit is softened, then the guard rests: {@code values} is the share taken off. */
        GUARD("guard"),
        /** Damage of one kind is softened: {@code values} is the share taken off. */
        RESIST("resist"),
        /** While sneaking, creatures notice you later: {@code values} is how much less you are seen. */
        HUSH("hush"),
        /** Sprinting tires you less: {@code values} is the share of hunger saved. */
        STRIDE("stride"),
        /** Lesser creatures hesitate before turning on you: {@code values} is seconds of hesitation. */
        DETER("deter"),
        /**
         * The Memory weave (lore: the foundational set, ch. 3024): the piece can be dismissed into the soul,
         * summoned back, mends while dismissed and stays with its holder through death. {@code values} is unused.
         */
        MEMORY("memory"),
        /** Bound (lore: rarer than a plain Memory): a Memory only its bonded owner can wield or wear. {@code values} is unused. */
        BOUND("bound");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        /** The Memory weave and Bound change what the piece is rather than adding a power. */
        public boolean foundational() {
            return this == MEMORY || this == BOUND;
        }
    }

    public record Effect(Kind kind, List<Float> values, Optional<ResourceLocation> attribute, Optional<ResourceLocation> damageTag) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Kind.CODEC.fieldOf("type").forGetter(Effect::kind),
                Codec.FLOAT.listOf().fieldOf("values").forGetter(Effect::values),
                ResourceLocation.CODEC.optionalFieldOf("attribute").forGetter(Effect::attribute),
                ResourceLocation.CODEC.optionalFieldOf("damage_tag").forGetter(Effect::damageTag)
        ).apply(instance, Effect::new));

        public Effect {
            values = List.copyOf(values);
        }
    }

    /** Something to study to learn the strand: an item, or any item carrying an enchantment. */
    public record Source(Optional<ResourceLocation> item, Optional<ResourceLocation> enchantment) {
        public static final Codec<Source> CODEC = RecordCodecBuilder.<Source>create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("item").forGetter(Source::item),
                ResourceLocation.CODEC.optionalFieldOf("enchantment").forGetter(Source::enchantment)
        ).apply(instance, Source::new)).validate(source -> source.item.isPresent() != source.enchantment.isPresent()
                ? DataResult.success(source) : DataResult.error(() -> "A study source names an item or an enchantment, not both"));

        public static Source item(ResourceLocation item) {
            return new Source(Optional.of(item), Optional.empty());
        }

        public static Source enchantment(ResourceLocation enchantment) {
            return new Source(Optional.empty(), Optional.of(enchantment));
        }
    }

    /** What the strand is spun from: so many of one item for each Tier. */
    public record Material(ResourceLocation item, List<Integer> counts) {
        public static final Codec<Material> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Material::item),
                Codec.intRange(0, 64).listOf().fieldOf("counts").forGetter(Material::counts)
        ).apply(instance, Material::new));

        public Material {
            counts = List.copyOf(counts);
        }

        public int count(int tier) {
            return counts.get(tier - 1);
        }
    }

    /** A point on the anchor, the unit circle centred on the shard. */
    public record Point(float x, float y) {
        public static final Codec<Point> CODEC = Codec.FLOAT.listOf().comapFlatMap(
                list -> list.size() == 2 && Math.hypot(list.get(0), list.get(1)) <= 1.0001
                        ? DataResult.success(new Point(list.get(0), list.get(1)))
                        : DataResult.error(() -> "A pattern point is [x, y] inside the unit circle"),
                point -> List.of(point.x, point.y));

        public double distance(float px, float py) {
            return Math.hypot(px - x, py - y);
        }
    }
}
