package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Pure Weaving rules (DESIGN): points, branch levels, deeds, respec, caps,
 * pattern matching and the hints a failed attempt leaves.
 *
 * <p>Branch level caps the Memory Tier a branch can weave and Soul Rank caps
 * the Memory Rank, the novel's two-part grading (ch. 74). A player with no
 * ranked core can still weave Dormant Memories, so crafting is open to all.</p>
 */
public final class WeavingRules {
    public static final int MATERIAL_SLOTS = 4;

    private WeavingRules() {
    }

    // Points and levels

    public static int pointsEarned(WeavingContent content, WeavingData data) {
        return data.totalXp() / content.settings().xpPerPoint();
    }

    /** XP gathered toward the next point. */
    public static int xpTowardPoint(WeavingContent content, WeavingData data) {
        return data.totalXp() % content.settings().xpPerPoint();
    }

    public static int pointsSpent(WeavingContent content, WeavingData data) {
        int spent = 0;
        for (Map.Entry<ResourceLocation, List<Integer>> entry : data.bought().entrySet()) {
            Optional<WeavingBranch> branch = content.branch(entry.getKey());
            if (branch.isEmpty()) {
                continue;
            }
            for (int level : entry.getValue()) {
                if (level >= 2 && level <= branch.get().maxLevel()) {
                    spent += branch.get().level(level).cost();
                }
            }
        }
        return spent;
    }

    public static int unspent(WeavingContent content, WeavingData data) {
        return Math.max(0, pointsEarned(content, data) - pointsSpent(content, data));
    }

    /** The highest level held: every level up to it is bought or earned. */
    public static int heldLevel(WeavingContent content, WeavingData data, ResourceLocation branchId) {
        Optional<WeavingBranch> branch = content.branch(branchId);
        if (branch.isEmpty()) {
            return 0;
        }
        int held = 1;
        for (int number = 2; number <= branch.get().maxLevel(); number++) {
            WeavingBranch.Level level = branch.get().level(number);
            boolean has = level.deed().isPresent() ? data.hasDeed(branchId, number) : data.bought(branchId).contains(number);
            if (!has) {
                break;
            }
            held = number;
        }
        return held;
    }

    /** What stands between a branch and its next level. {@code level} is 0 once the branch is maxed. */
    public record NextStep(int level, int cost, Optional<WeavingBranch.Deed> deed, int progress) {
        static final NextStep MAXED = new NextStep(0, 0, Optional.empty(), 0);

        public boolean maxed() {
            return level == 0;
        }
    }

    public static NextStep next(WeavingContent content, WeavingData data, ResourceLocation branchId) {
        Optional<WeavingBranch> branch = content.branch(branchId);
        int held = heldLevel(content, data, branchId);
        if (branch.isEmpty() || held >= branch.get().maxLevel()) {
            return NextStep.MAXED;
        }
        int number = held + 1;
        WeavingBranch.Level level = branch.get().level(number);
        return new NextStep(number, level.cost(), level.deed(), level.deed().isPresent() ? data.deedProgress(branchId, number) : 0);
    }

    public static boolean canRaise(WeavingContent content, WeavingData data, ResourceLocation branchId) {
        NextStep next = next(content, data, branchId);
        return !next.maxed() && next.deed().isEmpty() && unspent(content, data) >= next.cost();
    }

    /** Buys the next level of a branch with points. Empty if it is a deed level or the points are short. */
    public static Optional<WeavingData> raise(WeavingContent content, WeavingData data, ResourceLocation branchId) {
        if (!canRaise(content, data, branchId)) {
            return Optional.empty();
        }
        return Optional.of(data.withBought(branchId, next(content, data, branchId).level()));
    }

    public static boolean canRespec(WeavingData data) {
        return data.bought().values().stream().anyMatch(levels -> !levels.isEmpty());
    }

    /** Refunds every point; deeds stay earned. Empty if nothing is bought. */
    public static Optional<WeavingData> respec(WeavingData data) {
        return canRespec(data) ? Optional.of(data.respecced()) : Optional.empty();
    }

    // Patterns

    public static boolean knows(WeavingData data, ResourceLocation id, WeavingRecipe recipe) {
        return recipe.unlock().kind() == WeavingRecipe.Unlock.Kind.STARTER || data.learned().contains(id);
    }

    /** Why a pattern can't be woven yet, or empty if it can. */
    public static Optional<String> lock(WeavingContent content, WeavingData data, WeavingRecipe recipe,
                                        Optional<SoulRank> soulRank) {
        return lock(content, data, recipe, soulRank, Boons.NONE);
    }

    /** As {@link #lock(WeavingContent, WeavingData, WeavingRecipe, Optional)}, with a Masterwork's extra Tier counted. */
    public static Optional<String> lock(WeavingContent content, WeavingData data, WeavingRecipe recipe,
                                        Optional<SoulRank> soulRank, Boons boons) {
        MemoryContentCatalog.MemoryProfile profile = content.profile(recipe);
        int held = heldLevel(content, data, recipe.branch()) + boons.tierBonus();
        if (profile.tier() > held) {
            return Optional.of("Needs " + content.branchName(recipe.branch()) + " level " + profile.tier());
        }
        if (profile.rank().ordinal() > soulRank.map(Enum::ordinal).orElse(0)) {
            return Optional.of("Needs " + article(rankName(profile.rank())) + " " + rankName(profile.rank()) + " soul");
        }
        return Optional.empty();
    }

    /** The pattern these exact materials make, if any. */
    public static Optional<ResourceLocation> match(WeavingContent content, Map<ResourceLocation, Integer> materials) {
        for (Map.Entry<ResourceLocation, WeavingRecipe> entry : content.recipes().entrySet()) {
            if (entry.getValue().amounts().equals(materials)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public enum Outcome {
        /** The table is empty. */
        EMPTY,
        /** A known pattern was woven. */
        WOVEN,
        /** A pattern was found by trial and woven at once. */
        DISCOVERED,
        /** A pattern was found by trial but is still beyond the player's level or Rank. */
        DISCOVERED_LOCKED,
        /** A known pattern is beyond the player's level or Rank. */
        LOCKED,
        /** The materials match a pattern only the Spell can show. */
        UNSHOWN,
        /** No pattern matched; the closest unfound one gave up a hint. */
        HINTED,
        /** No pattern matched and nothing was learned. */
        NOTHING
    }

    /**
     * What one press of Weave did. {@code data} is the player's progress
     * afterwards; {@code lock} says why a matched pattern can't be woven;
     * {@code deeds} lists deed levels it earned.
     */
    public record Attempt(Outcome outcome, Optional<ResourceLocation> recipe, WeavingData data, int xp,
                          Optional<String> lock, List<String> deeds) {
        public boolean wove() {
            return outcome == Outcome.WOVEN || outcome == Outcome.DISCOVERED;
        }
    }

    /**
     * What a player's Aspect adds at the table: a Forge's Masterwork lets one
     * weave reach {@code tierBonus} Tiers above the branch level, and Thrifty
     * Hands earns {@code xpPercent} of the usual XP in {@code quickBranch}.
     */
    public record Boons(int tierBonus, Optional<ResourceLocation> quickBranch, int xpPercent) {
        public static final Boons NONE = new Boons(0, Optional.empty(), 100);

        int xp(ResourceLocation branch, int xp) {
            return quickBranch.filter(branch::equals).isPresent() ? xp * xpPercent / 100 : xp;
        }
    }

    public static Attempt attempt(WeavingContent content, WeavingData data, Map<ResourceLocation, Integer> materials,
                                  Optional<SoulRank> soulRank) {
        return attempt(content, data, materials, soulRank, Boons.NONE);
    }

    public static Attempt attempt(WeavingContent content, WeavingData data, Map<ResourceLocation, Integer> materials,
                                  Optional<SoulRank> soulRank, Boons boons) {
        if (materials.isEmpty()) {
            return new Attempt(Outcome.EMPTY, Optional.empty(), data, 0, Optional.empty(), List.of());
        }
        Optional<ResourceLocation> matched = match(content, materials);
        if (matched.isEmpty()) {
            return hint(content, data, materials);
        }
        ResourceLocation id = matched.get();
        WeavingRecipe recipe = content.recipes().get(id);
        boolean known = knows(data, id, recipe);
        if (!known && recipe.unlock().kind() == WeavingRecipe.Unlock.Kind.FIRST_KILL) {
            return new Attempt(Outcome.UNSHOWN, matched, data, 0, Optional.empty(), List.of());
        }
        WeavingData next = known ? data : data.withLearned(id);
        Optional<String> lock = lock(content, next, recipe, soulRank, boons);
        if (lock.isPresent()) {
            return new Attempt(known ? Outcome.LOCKED : Outcome.DISCOVERED_LOCKED, matched, next, 0, lock, List.of());
        }
        int xp = boons.xp(recipe.branch(), recipe.xp() * (next.woven(id) == 0 ? content.settings().firstWeaveMultiplier() : 1));
        next = next.withWoven(id).withXp(recipe.branch(), xp);
        List<String> deeds = new ArrayList<>();
        next = advanceDeeds(content, next, recipe, deeds);
        return new Attempt(known ? Outcome.WOVEN : Outcome.DISCOVERED, matched, next, xp, Optional.empty(), deeds);
    }

    private static WeavingData advanceDeeds(WeavingContent content, WeavingData data, WeavingRecipe recipe, List<String> earned) {
        WeavingBranch branch = content.branch(recipe.branch()).orElseThrow();
        int tier = content.tier(recipe);
        for (int number = 2; number <= branch.maxLevel(); number++) {
            Optional<WeavingBranch.Deed> deed = branch.level(number).deed();
            if (deed.isEmpty() || data.hasDeed(recipe.branch(), number)
                    || deed.get().kind() != WeavingBranch.Deed.Kind.WEAVE || tier < deed.get().minTier()) {
                continue;
            }
            int progress = data.deedProgress(recipe.branch(), number) + 1;
            boolean done = progress >= deed.get().count();
            data = data.withDeedProgress(recipe.branch(), number, progress, done);
            if (done) {
                earned.add(branch.name() + " level " + number + ": " + deed.get().description());
            }
        }
        return data;
    }

    /**
     * A failed attempt reveals which of its materials belong to the closest
     * pattern the player could still find by trial: the one sharing the most
     * kinds of material, lowest Tier first.
     */
    private static Attempt hint(WeavingContent content, WeavingData data, Map<ResourceLocation, Integer> materials) {
        ResourceLocation best = null;
        int bestShared = 0;
        for (Map.Entry<ResourceLocation, WeavingRecipe> entry : content.recipes().entrySet()) {
            WeavingRecipe recipe = entry.getValue();
            if (recipe.unlock().kind() != WeavingRecipe.Unlock.Kind.DISCOVER || data.learned().contains(entry.getKey())) {
                continue;
            }
            int shared = (int) recipe.amounts().keySet().stream().filter(materials::containsKey).count();
            if (shared > bestShared) {
                best = entry.getKey();
                bestShared = shared;
            }
        }
        if (best == null) {
            return new Attempt(Outcome.NOTHING, Optional.empty(), data, 0, Optional.empty(), List.of());
        }
        LinkedHashSet<ResourceLocation> revealed = new LinkedHashSet<>(data.hint(best));
        content.recipes().get(best).amounts().keySet().stream().filter(materials::containsKey).forEach(revealed::add);
        return new Attempt(Outcome.HINTED, Optional.of(best), data.withHint(best, List.copyOf(revealed)), 0,
                Optional.empty(), List.of());
    }

    /** Patterns the Spell grants for slaying this entity that the player does not know yet. */
    public static List<ResourceLocation> grantsForKill(WeavingContent content, WeavingData data, ResourceLocation entity) {
        List<ResourceLocation> granted = new ArrayList<>();
        content.recipes().forEach((id, recipe) -> {
            if (recipe.unlock().kind() == WeavingRecipe.Unlock.Kind.FIRST_KILL
                    && recipe.unlock().entity().filter(entity::equals).isPresent()
                    && !data.learned().contains(id)) {
                granted.add(id);
            }
        });
        return granted;
    }

    // Names

    public static String rankName(SoulRank rank) {
        String name = rank.serializedName();
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    public static String tierName(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            default -> Integer.toString(tier);
        };
    }

    private static String article(String word) {
        return "AEIOU".indexOf(word.charAt(0)) >= 0 ? "an" : "a";
    }
}
