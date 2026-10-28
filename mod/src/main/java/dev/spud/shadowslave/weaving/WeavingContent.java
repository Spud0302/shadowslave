package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * The branches, patterns and tuning loaded from data packs, joined with the
 * Memory catalogue. Rebuilt on every data reload; pure so rules can be tested
 * without a server.
 */
public record WeavingContent(
        WeavingSettings settings,
        Map<ResourceLocation, WeavingBranch> branches,
        Map<ResourceLocation, WeavingRecipe> recipes,
        Map<ResourceLocation, MemoryContentCatalog.MemoryProfile> profiles
) {
    public static final WeavingContent EMPTY = new WeavingContent(WeavingSettings.DEFAULT, Map.of(), Map.of(), Map.of());
    private static volatile WeavingContent current = EMPTY;

    public WeavingContent {
        branches = sorted(branches);
        profiles = Map.copyOf(profiles);
        // Patterns run in branch, Tier, then id order wherever they are listed.
        ArrayList<Map.Entry<ResourceLocation, WeavingRecipe>> entries = new ArrayList<>(recipes.entrySet());
        Map<ResourceLocation, MemoryContentCatalog.MemoryProfile> lookup = profiles;
        entries.sort(Comparator.<Map.Entry<ResourceLocation, WeavingRecipe>, String>comparing(entry -> entry.getValue().branch().toString())
                .thenComparingInt(entry -> entry.getValue().memory().filter(lookup::containsKey).map(memory -> lookup.get(memory).tier())
                        .orElse(entry.getValue().tier()))
                .thenComparing(entry -> entry.getKey().toString()));
        LinkedHashMap<ResourceLocation, WeavingRecipe> ordered = new LinkedHashMap<>();
        entries.forEach(entry -> ordered.put(entry.getKey(), entry.getValue()));
        recipes = java.util.Collections.unmodifiableMap(ordered);
    }

    public static WeavingContent current() {
        return current;
    }

    static void set(WeavingContent content) {
        current = content;
    }

    /**
     * Joins loaded files with the Memory catalogue, dropping (and reporting)
     * any pattern whose branch or Memory does not exist.
     */
    public static WeavingContent build(WeavingSettings settings, Map<ResourceLocation, WeavingBranch> branches,
                                       Map<ResourceLocation, WeavingRecipe> recipes, Consumer<String> problems) {
        Map<ResourceLocation, MemoryContentCatalog.MemoryProfile> profiles = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        MemoryContentCatalog.all().memories().forEach(profile -> profiles.put(profile.id(), profile));
        LinkedHashMap<ResourceLocation, WeavingRecipe> valid = new LinkedHashMap<>();
        recipes.forEach((id, recipe) -> {
            if (!branches.containsKey(recipe.branch())) {
                problems.accept("Weaving pattern " + id + " names unknown branch " + recipe.branch());
            } else if (recipe.memory().isPresent() && !profiles.containsKey(recipe.memory().get())) {
                problems.accept("Weaving pattern " + id + " names unknown Memory " + recipe.memory().get());
            } else {
                valid.put(id, recipe);
            }
        });
        return new WeavingContent(settings, branches, valid, profiles);
    }

    /** The Memory a pattern makes, if it makes one; a plain pattern (an iron sword) has none. */
    public Optional<MemoryContentCatalog.MemoryProfile> profile(WeavingRecipe recipe) {
        return recipe.memory().map(profiles::get);
    }

    public int tier(WeavingRecipe recipe) {
        return profile(recipe).map(MemoryContentCatalog.MemoryProfile::tier).orElse(recipe.tier());
    }

    /** The Soul Rank a pattern needs; plain patterns need none beyond Dormant. */
    public SoulRank rank(WeavingRecipe recipe) {
        return profile(recipe).map(MemoryContentCatalog.MemoryProfile::rank).orElse(SoulRank.DORMANT);
    }

    public String name(WeavingRecipe recipe) {
        return recipe.name().orElseGet(() -> profile(recipe).map(MemoryContentCatalog.MemoryProfile::formalName).orElse(recipe.result().getPath()));
    }

    public Optional<WeavingBranch> branch(ResourceLocation id) {
        return Optional.ofNullable(branches.get(id));
    }

    public String branchName(ResourceLocation id) {
        return branch(id).map(WeavingBranch::name).orElse(id.getPath());
    }

    /** Branch ids in their fixed order, so a menu button can name one by index. */
    public List<ResourceLocation> branchOrder() {
        return List.copyOf(branches.keySet());
    }

    private static Map<ResourceLocation, WeavingBranch> sorted(Map<ResourceLocation, WeavingBranch> branches) {
        TreeMap<ResourceLocation, WeavingBranch> sorted = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        sorted.putAll(branches);
        return java.util.Collections.unmodifiableMap(sorted);
    }
}
