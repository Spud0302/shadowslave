package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A player's Weaving progress, persisted with the player. Nothing here is
 * trusted from the client.
 *
 * @param xp           Weaving XP earned in each branch
 * @param bought       the point levels bought in each branch; a respec clears these
 * @param deeds        deed levels earned, as {@code branch#level}; a respec never clears these
 * @param deedProgress progress toward deeds not yet earned, keyed like {@code deeds}
 * @param learned      patterns found by trial or granted by the Spell (starter patterns are always known)
 * @param woven        how many times each pattern has been woven
 * @param hints        for patterns not yet found, the materials a failed attempt has revealed
 * @param respecs      how many times the player has respecced
 * @param studied      strands learned by studying a weave
 * @param laid         how many times each strand has been laid into a piece
 * @param created      strands of the player's own making, fused from two learned ones, or copied from a weave that held one
 */
public record WeavingData(
        Map<ResourceLocation, Integer> xp,
        Map<ResourceLocation, List<Integer>> bought,
        Set<String> deeds,
        Map<String, Integer> deedProgress,
        Set<ResourceLocation> learned,
        Map<ResourceLocation, Integer> woven,
        Map<ResourceLocation, List<ResourceLocation>> hints,
        int respecs,
        Set<ResourceLocation> studied,
        Map<ResourceLocation, Integer> laid,
        Map<ResourceLocation, WovenStrands.Fused> created
) {
    public static final MapCodec<WeavingData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("xp", Map.of()).forGetter(WeavingData::xp),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT.listOf()).optionalFieldOf("bought", Map.of()).forGetter(WeavingData::bought),
            Codec.STRING.listOf().<Set<String>>xmap(Set::copyOf, List::copyOf).optionalFieldOf("deeds", Set.of()).forGetter(WeavingData::deeds),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("deed_progress", Map.of()).forGetter(WeavingData::deedProgress),
            ResourceLocation.CODEC.listOf().<Set<ResourceLocation>>xmap(Set::copyOf, List::copyOf).optionalFieldOf("learned", Set.of()).forGetter(WeavingData::learned),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("woven", Map.of()).forGetter(WeavingData::woven),
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC.listOf()).optionalFieldOf("hints", Map.of()).forGetter(WeavingData::hints),
            Codec.INT.optionalFieldOf("respecs", 0).forGetter(WeavingData::respecs),
            ResourceLocation.CODEC.listOf().<Set<ResourceLocation>>xmap(Set::copyOf, List::copyOf).optionalFieldOf("studied", Set.of()).forGetter(WeavingData::studied),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("laid", Map.of()).forGetter(WeavingData::laid),
            Codec.unboundedMap(ResourceLocation.CODEC, WovenStrands.Fused.CODEC).optionalFieldOf("created", Map.of()).forGetter(WeavingData::created)
    ).apply(instance, WeavingData::new));

    public WeavingData {
        xp = Map.copyOf(xp);
        HashMap<ResourceLocation, List<Integer>> boughtCopy = new HashMap<>();
        bought.forEach((branch, levels) -> boughtCopy.put(branch, List.copyOf(levels)));
        bought = Map.copyOf(boughtCopy);
        deeds = Set.copyOf(deeds);
        deedProgress = Map.copyOf(deedProgress);
        learned = Set.copyOf(learned);
        woven = Map.copyOf(woven);
        HashMap<ResourceLocation, List<ResourceLocation>> hintsCopy = new HashMap<>();
        hints.forEach((recipe, items) -> hintsCopy.put(recipe, List.copyOf(items)));
        hints = Map.copyOf(hintsCopy);
        respecs = Math.max(0, respecs);
        studied = Set.copyOf(studied);
        laid = Map.copyOf(laid);
        created = Map.copyOf(created);
    }

    public static WeavingData empty() {
        return new WeavingData(Map.of(), Map.of(), Set.of(), Map.of(), Set.of(), Map.of(), Map.of(), 0, Set.of(), Map.of(), Map.of());
    }

    static String deedKey(ResourceLocation branch, int level) {
        return branch + "#" + level;
    }

    public int xp(ResourceLocation branch) {
        return xp.getOrDefault(branch, 0);
    }

    public int totalXp() {
        return xp.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<Integer> bought(ResourceLocation branch) {
        return bought.getOrDefault(branch, List.of());
    }

    public boolean hasDeed(ResourceLocation branch, int level) {
        return deeds.contains(deedKey(branch, level));
    }

    public int deedProgress(ResourceLocation branch, int level) {
        return deedProgress.getOrDefault(deedKey(branch, level), 0);
    }

    public int woven(ResourceLocation recipe) {
        return woven.getOrDefault(recipe, 0);
    }

    public List<ResourceLocation> hint(ResourceLocation recipe) {
        return hints.getOrDefault(recipe, List.of());
    }

    WeavingData withXp(ResourceLocation branch, int amount) {
        HashMap<ResourceLocation, Integer> next = new HashMap<>(xp);
        next.merge(branch, amount, Integer::sum);
        return new WeavingData(next, bought, deeds, deedProgress, learned, woven, hints, respecs, studied, laid, created);
    }

    WeavingData withBought(ResourceLocation branch, int level) {
        HashMap<ResourceLocation, List<Integer>> next = new HashMap<>(bought);
        ArrayList<Integer> levels = new ArrayList<>(bought(branch));
        if (!levels.contains(level)) {
            levels.add(level);
        }
        next.put(branch, levels);
        return new WeavingData(xp, next, deeds, deedProgress, learned, woven, hints, respecs, studied, laid, created);
    }

    WeavingData withDeedProgress(ResourceLocation branch, int level, int progress, boolean earned) {
        HashMap<String, Integer> nextProgress = new HashMap<>(deedProgress);
        HashSet<String> nextDeeds = new HashSet<>(deeds);
        String key = deedKey(branch, level);
        if (earned) {
            nextDeeds.add(key);
            nextProgress.remove(key);
        } else {
            nextProgress.put(key, progress);
        }
        return new WeavingData(xp, bought, nextDeeds, nextProgress, learned, woven, hints, respecs, studied, laid, created);
    }

    WeavingData withLearned(ResourceLocation recipe) {
        HashSet<ResourceLocation> next = new HashSet<>(learned);
        next.add(recipe);
        HashMap<ResourceLocation, List<ResourceLocation>> nextHints = new HashMap<>(hints);
        nextHints.remove(recipe);
        return new WeavingData(xp, bought, deeds, deedProgress, next, woven, nextHints, respecs, studied, laid, created);
    }

    WeavingData withWoven(ResourceLocation recipe) {
        HashMap<ResourceLocation, Integer> next = new HashMap<>(woven);
        next.merge(recipe, 1, Integer::sum);
        return new WeavingData(xp, bought, deeds, deedProgress, learned, next, hints, respecs, studied, laid, created);
    }

    WeavingData withHint(ResourceLocation recipe, List<ResourceLocation> revealed) {
        HashMap<ResourceLocation, List<ResourceLocation>> next = new HashMap<>(hints);
        next.put(recipe, revealed);
        return new WeavingData(xp, bought, deeds, deedProgress, learned, woven, next, respecs, studied, laid, created);
    }

    public int laid(ResourceLocation strand) {
        return laid.getOrDefault(strand, 0);
    }

    WeavingData withStudied(ResourceLocation strand) {
        HashSet<ResourceLocation> next = new HashSet<>(studied);
        next.add(strand);
        return new WeavingData(xp, bought, deeds, deedProgress, learned, woven, hints, respecs, next, laid, created);
    }

    WeavingData withLaid(ResourceLocation strand) {
        HashMap<ResourceLocation, Integer> next = new HashMap<>(laid);
        next.merge(strand, 1, Integer::sum);
        return new WeavingData(xp, bought, deeds, deedProgress, learned, woven, hints, respecs, studied, next, created);
    }

    WeavingData withCreated(ResourceLocation id, WovenStrands.Fused fused) {
        HashMap<ResourceLocation, WovenStrands.Fused> next = new HashMap<>(created);
        next.put(id, fused);
        return new WeavingData(xp, bought, deeds, deedProgress, learned, woven, hints, respecs, studied, laid, next);
    }

    WeavingData respecced() {
        return new WeavingData(xp, Map.of(), deeds, deedProgress, learned, woven, hints, respecs + 1, studied, laid, created);
    }
}
