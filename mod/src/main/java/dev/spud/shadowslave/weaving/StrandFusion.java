package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure rules for strands of a Weaver's own making (DESIGN, plan 2b: copy,
 * tune, create; after ch. 2048, where Sunny first copies, then tunes, then
 * creates enchantments of his own). Two learned strands that suit the same
 * kind of piece are fused into one named strand that takes a single thread
 * and does both, each part at the Weaver's fusing share. Once made it is
 * known like a studied strand, traced like one, and can be studied out of
 * any piece that holds it.
 */
public final class StrandFusion {
    /** Essence it takes to work out a fusion, before any of it is traced. */
    public static final int CREATE_ESSENCE = 40;
    /** The most strands of their own one Weaver can keep. */
    public static final int MAX_CREATED = 12;
    public static final int NAME_LENGTH = 24;
    /** Points taken from each part's pattern for the fused one: the first part leads, the second follows. */
    static final int FIRST_POINTS = 4;
    static final int SECOND_POINTS = 3;

    private StrandFusion() {
    }

    /** The id a fusion of {@code a} and {@code b} at {@code share} is known by, the same for anyone who makes it. */
    public static ResourceLocation id(ResourceLocation a, ResourceLocation b, int share) {
        return ResourceLocation.fromNamespaceAndPath(dev.spud.shadowslave.ShadowSlaveMod.MOD_ID,
                "fused/" + a.getNamespace() + "." + a.getPath().replace('/', '.') + "--"
                        + b.getNamespace() + "." + b.getPath().replace('/', '.') + "-" + share);
    }

    /** Why {@code a} and {@code b} can't be fused, or empty if they can. */
    public static Optional<String> check(WeavingContent content, ResourceLocation a, ResourceLocation b) {
        Optional<StrandPattern> first = content.strand(a);
        Optional<StrandPattern> second = content.strand(b);
        if (first.isEmpty() || second.isEmpty()) {
            return Optional.of("Only strands you have studied can be fused.");
        }
        if (a.equals(b)) {
            return Optional.of("Choose two different strands.");
        }
        for (StrandPattern part : List.of(first.get(), second.get())) {
            if (part.effect().kind().foundational()) {
                return Optional.of(part.name() + " is what a piece is, not a power, and can't be fused.");
            }
        }
        if (fits(first.get(), second.get()).isEmpty()) {
            return Optional.of(first.get().name() + " and " + second.get().name() + " never suit the same piece.");
        }
        return Optional.empty();
    }

    private static List<StrandFit> fits(StrandPattern a, StrandPattern b) {
        return a.fits().stream().filter(b.fits()::contains).toList();
    }

    /** "Dragging Links" and "Echoing Cut" make "Dragging Cut": the first word of one, the last of the other. */
    public static String defaultName(String a, String b) {
        String[] first = a.trim().split("\\s+");
        String[] second = b.trim().split("\\s+");
        String name = first[0] + " " + second[second.length - 1];
        return name.equals(a.trim()) || name.equals(b.trim()) ? a.trim() + " and " + b.trim() : name;
    }

    /** A Weaver's chosen name, trimmed to plain printable text, or the default if nothing is left. */
    public static String name(String chosen, String fallback) {
        StringBuilder clean = new StringBuilder();
        for (char c : chosen.strip().toCharArray()) {
            if ((Character.isLetterOrDigit(c) || c == ' ' || c == '\'' || c == '-') && clean.length() < NAME_LENGTH) {
                clean.append(c);
            }
        }
        String name = clean.toString().strip().replaceAll("\\s+", " ");
        return name.isEmpty() ? fallback : name;
    }

    /**
     * The strand a fusion behaves as at the table: it suits what both parts
     * suit, reaches the lower of their top Tiers, costs both their essence
     * and materials, needs the higher Rank, and traces the start of the first
     * part's pattern into the start of the second's. Empty if a part is no
     * longer loaded.
     */
    public static Optional<StrandPattern> pattern(WeavingContent content, ResourceLocation id, WovenStrands.Fused fused) {
        if (fused.parts().size() != 2) {
            return Optional.empty();
        }
        ResourceLocation a = fused.parts().get(0);
        ResourceLocation b = fused.parts().get(1);
        Optional<StrandPattern> first = content.strand(a);
        Optional<StrandPattern> second = content.strand(b);
        if (first.isEmpty() || second.isEmpty()) {
            return Optional.empty();
        }
        StrandPattern one = first.get();
        StrandPattern two = second.get();
        List<StrandFit> fits = fits(one, two);
        if (fits.isEmpty()) {
            return Optional.empty();
        }
        int tiers = Math.min(one.maxTier(), two.maxTier());
        boolean tunable = one.tunable() && two.tunable();
        List<Float> values = new ArrayList<>();
        List<Integer> essence = new ArrayList<>();
        List<Integer> firstCounts = new ArrayList<>();
        for (int tier = 1; tier <= tiers; tier++) {
            values.add(tunable ? 1.0F : 0.0F);
            essence.add(one.essence().get(tier - 1) + two.essence().get(tier - 1));
            firstCounts.add(one.material().count(tier));
        }
        List<StrandPattern.Material> extra = new ArrayList<>();
        extra.add(new StrandPattern.Material(two.material().item(), counts(two.material(), tiers)));
        for (StrandPattern part : List.of(one, two)) {
            for (StrandPattern.Material material : part.extraMaterials()) {
                extra.add(new StrandPattern.Material(material.item(), counts(material, tiers)));
            }
        }
        List<StrandPattern.Point> points = new ArrayList<>();
        StrandRules.points(a, one).stream().limit(FIRST_POINTS).forEach(points::add);
        for (StrandPattern.Point point : StrandRules.points(b, two)) {
            if (points.size() >= FIRST_POINTS + SECOND_POINTS) {
                break;
            }
            // Two points in the same place would be one stop for the trace; skip a repeat.
            if (points.stream().noneMatch(existing -> existing.distance(point.x(), point.y()) < 0.05)) {
                points.add(point);
            }
        }
        while (points.size() < StrandRules.MIN_POINTS) {
            points.add(new StrandPattern.Point(0.0F, 0.0F));
        }
        SoulRank rank = one.minRank().ordinal() >= two.minRank().ordinal() ? one.minRank() : two.minRank();
        return Optional.of(new StrandPattern(fused.name(), "Your own strand, fused from " + one.name() + " and " + two.name()
                + ". Each works at " + fused.share() + "% in a single thread.", fits,
                new StrandPattern.Effect(StrandPattern.Kind.FUSED, values, Optional.empty(), Optional.empty()), List.of(),
                new StrandPattern.Material(one.material().item(), firstCounts), essence, rank, points,
                one.xp() + two.xp(), false, StrandService.PATTERNS, extra));
    }

    private static List<Integer> counts(StrandPattern.Material material, int tiers) {
        return material.counts().subList(0, tiers);
    }

    /** One part of a fused strand at work: the part's own strand, laid at the fused strand's Tier and potency. */
    public record Part(StrandPattern strand, WovenStrands.Laid laid, float share) {
    }

    /**
     * What a laid strand is made of: itself, or for a fused strand each part,
     * laid at the fused Tier (or the part's highest, if lower) and potency.
     */
    public static List<Part> parts(WeavingContent content, WovenStrands.Laid laid) {
        if (laid.fused().isEmpty()) {
            return content.strand(laid.id()).map(strand -> List.of(new Part(strand, laid, 1.0F))).orElse(List.of());
        }
        WovenStrands.Fused fused = laid.fused().get();
        List<Part> parts = new ArrayList<>();
        for (ResourceLocation id : fused.parts()) {
            content.strand(id).filter(strand -> !strand.effect().kind().foundational()).ifPresent(strand -> parts.add(new Part(strand,
                    new WovenStrands.Laid(id, Math.min(laid.tier(), strand.maxTier()), laid.potency(), laid.restriction(), laid.weaver()),
                    fused.share() / 100.0F)));
        }
        return parts;
    }

    /** The strands a Weaver knows how to trace: the data-pack ones they have studied or start with, and their own. */
    public static boolean known(WeavingContent content, WeavingData data, ResourceLocation id) {
        return content.strand(id).map(strand -> StrandRules.knows(data.studied(), id, strand)).orElse(false)
                || data.created().containsKey(id);
    }

    /** The strand {@code id} as this Weaver sees it at the table: from data, or one of their own. */
    public static Optional<StrandPattern> resolve(WeavingContent content, WeavingData data, ResourceLocation id) {
        Optional<StrandPattern> loaded = content.strand(id);
        if (loaded.isPresent()) {
            return loaded;
        }
        WovenStrands.Fused fused = data.created().get(id);
        return fused == null ? Optional.empty() : pattern(content, id, fused);
    }

    /** Every strand of their own a Weaver keeps, in id order. */
    public static Map<ResourceLocation, WovenStrands.Fused> created(WeavingData data) {
        java.util.TreeMap<ResourceLocation, WovenStrands.Fused> sorted = new java.util.TreeMap<>(java.util.Comparator.comparing(ResourceLocation::toString));
        sorted.putAll(data.created());
        return sorted;
    }
}
