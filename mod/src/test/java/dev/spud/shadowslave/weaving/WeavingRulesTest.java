package dev.spud.shadowslave.weaving;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.client.SoulPages;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Forging data in the mod's resources, run through the pure Weaving rules. */
class WeavingRulesTest {
    private static final Path DATA = Path.of("src/main/resources/data/shadowslave/weaving");
    private static final Path PATTERNS = Path.of("src/main/resources/data/shadowslave/smithing/pattern");
    private static final Path ASSETS = Path.of("src/main/resources/assets/shadowslave");
    private static final ResourceLocation FORGING = id("forging");
    private static final Optional<SoulRank> DORMANT = Optional.of(SoulRank.DORMANT);
    private static final Optional<SoulRank> AWAKENED = Optional.of(SoulRank.AWAKENED);

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }

    private static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static <T> T parse(Path file, Codec<T> codec) throws IOException {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow();
    }

    private static <T> Map<ResourceLocation, T> folder(Path folder, Codec<T> codec) throws IOException {
        Map<ResourceLocation, T> values = new HashMap<>();
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.toList()) {
                values.put(id(file.getFileName().toString().replace(".json", "")), parse(file, codec));
            }
        }
        return values;
    }

    private static WeavingContent content() throws IOException {
        List<String> problems = new ArrayList<>();
        WeavingContent content = WeavingContent.build(parse(DATA.resolve("rules.json"), WeavingSettings.CODEC),
                folder(DATA.resolve("branch"), WeavingBranch.CODEC), folder(PATTERNS, WeavingRecipe.CODEC), problems::add);
        assertEquals(List.of(), problems);
        return content;
    }

    private static Map<ResourceLocation, Integer> amounts(Object... pairs) {
        Map<ResourceLocation, Integer> amounts = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            amounts.put((ResourceLocation) pairs[i], (Integer) pairs[i + 1]);
        }
        return amounts;
    }

    private static WeavingData weave(WeavingContent content, WeavingData data, String recipe, Optional<SoulRank> rank) {
        WeavingRules.Attempt attempt = WeavingRules.attempt(content, data, content.recipes().get(id(recipe)).amounts(), rank);
        assertTrue(attempt.wove(), recipe + " should weave, got " + attempt.outcome() + " " + attempt.lock());
        return attempt.data();
    }

    @Test
    void forgingDataLoadsWithAssetsForEveryPattern() throws IOException {
        WeavingContent content = content();
        assertEquals(3, content.branch(FORGING).orElseThrow().maxLevel());
        assertEquals(16, content.recipes().size(), "eight creature patterns and eight plain metal ones");
        for (WeavingRecipe recipe : content.recipes().values()) {
            assertTrue(content.tier(recipe) <= 3, recipe.result() + " fits under Forging level 3");
            if (recipe.memory().isEmpty()) {
                assertEquals("minecraft", recipe.result().getNamespace(), "plain patterns make vanilla gear: " + recipe.result());
                continue;
            }
            assertTrue(Files.exists(ASSETS.resolve("models/item/" + recipe.result().getPath() + ".json")), recipe.result() + " model");
            assertTrue(Files.exists(ASSETS.resolve("textures/item/" + recipe.result().getPath() + ".png")), recipe.result() + " texture");
            if (recipe.unlock().kind() == WeavingRecipe.Unlock.Kind.DISCOVER) {
                assertFalse(recipe.hint().isBlank(), recipe.result() + " leaves a hint");
            }
        }
        String lang = Files.readString(ASSETS.resolve("lang/en_us.json"));
        content.recipes().values().stream().filter(recipe -> recipe.memory().isPresent()).forEach(recipe -> assertTrue(lang.contains("\"item.shadowslave." + recipe.result().getPath() + "\""),
                recipe.result() + " has a name"));
    }

    @Test
    void firstWeavesOfTheStartersEarnThePointForLevelTwo() throws IOException {
        WeavingContent content = content();
        WeavingData data = WeavingData.empty();
        assertEquals(1, WeavingRules.heldLevel(content, data, FORGING));
        assertFalse(WeavingRules.canRaise(content, data, FORGING));
        data = weave(content, data, "shard_knife", Optional.empty());
        data = weave(content, data, "sleepers_hood", Optional.empty());
        data = weave(content, data, "wanderers_boots", Optional.empty());
        assertEquals(150, data.xp(FORGING), "each first weave pays five times");
        assertEquals(1, WeavingRules.unspent(content, data));
        data = WeavingRules.raise(content, data, FORGING).orElseThrow();
        assertEquals(2, WeavingRules.heldLevel(content, data, FORGING));
        assertEquals(0, WeavingRules.unspent(content, data));
        assertTrue(WeavingRules.raise(content, data, FORGING).isEmpty(), "level 3 is a deed, not points");
        data = weave(content, data, "shard_knife", Optional.empty());
        assertEquals(160, data.xp(FORGING), "a repeat weave pays its plain XP");
    }

    @Test
    void branchLevelCapsTierAndSoulRankCapsRank() throws IOException {
        WeavingContent content = content();
        Map<ResourceLocation, Integer> cleaver = content.recipes().get(id("linked_cleaver")).amounts();

        WeavingRules.Attempt found = WeavingRules.attempt(content, WeavingData.empty(), cleaver, AWAKENED);
        assertEquals(WeavingRules.Outcome.DISCOVERED_LOCKED, found.outcome());
        assertEquals(Optional.of("Needs Forging level 2"), found.lock());
        assertTrue(found.data().learned().contains(id("linked_cleaver")), "found even though it can't be woven yet");

        WeavingData levelTwo = found.data().withXp(FORGING, 100).withBought(FORGING, 2);
        WeavingRules.Attempt dormant = WeavingRules.attempt(content, levelTwo, cleaver, DORMANT);
        assertEquals(WeavingRules.Outcome.LOCKED, dormant.outcome());
        assertEquals(Optional.of("Needs an Awakened soul"), dormant.lock());

        assertEquals(WeavingRules.Outcome.WOVEN, WeavingRules.attempt(content, levelTwo, cleaver, AWAKENED).outcome());
    }

    @Test
    void aMasterworkReachesOneTierHigherAndThriftyHandsLearnsFaster() throws IOException {
        WeavingContent content = content();
        Map<ResourceLocation, Integer> cleaver = content.recipes().get(id("linked_cleaver")).amounts();
        WeavingRules.Boons masterwork = new WeavingRules.Boons(1, Optional.empty(), 100);
        WeavingRules.Attempt found = WeavingRules.attempt(content, WeavingData.empty(), cleaver, AWAKENED, masterwork);
        assertEquals(WeavingRules.Outcome.DISCOVERED, found.outcome(), "a Tier II pattern at Forging level 1");

        WeavingRules.Boons thrifty = new WeavingRules.Boons(0, Optional.of(FORGING), 150);
        Map<ResourceLocation, Integer> knife = content.recipes().get(id("shard_knife")).amounts();
        WeavingRules.Attempt plain = WeavingRules.attempt(content, WeavingData.empty(), knife, DORMANT);
        WeavingRules.Attempt quick = WeavingRules.attempt(content, WeavingData.empty(), knife, DORMANT, thrifty);
        assertEquals(plain.xp() * 3 / 2, quick.xp());
    }

    @Test
    void theLevelThreeDeedSurvivesARespecButThePointDoesNot() throws IOException {
        WeavingContent content = content();
        WeavingData data = WeavingData.empty().withXp(FORGING, 100).withLearned(id("linked_cleaver"));
        data = WeavingRules.raise(content, data, FORGING).orElseThrow();
        for (int i = 0; i < 4; i++) {
            data = weave(content, data, "linked_cleaver", AWAKENED);
        }
        assertEquals(2, WeavingRules.heldLevel(content, data, FORGING));
        assertEquals(4, WeavingRules.next(content, data, FORGING).progress());
        WeavingRules.Attempt fifth = WeavingRules.attempt(content, data, content.recipes().get(id("linked_cleaver")).amounts(), AWAKENED);
        assertEquals(1, fifth.deeds().size(), "the fifth Tier II weave earns the deed");
        data = fifth.data();
        assertEquals(3, WeavingRules.heldLevel(content, data, FORGING));
        assertTrue(WeavingRules.next(content, data, FORGING).maxed());

        data = WeavingRules.respec(data).orElseThrow();
        assertEquals(1, WeavingRules.heldLevel(content, data, FORGING), "level 3 hangs on level 2");
        assertTrue(data.hasDeed(FORGING, 3), "the deed stays earned");
        assertTrue(WeavingRules.unspent(content, data) >= 1, "the point comes back");
        data = WeavingRules.raise(content, data, FORGING).orElseThrow();
        assertEquals(3, WeavingRules.heldLevel(content, data, FORGING), "rebuying level 2 restores level 3");
        assertTrue(WeavingRules.respec(WeavingData.empty()).isEmpty(), "nothing to respec");
    }

    @Test
    void aFailedWeaveRevealsWhatItSharedWithTheClosestUnfoundPattern() throws IOException {
        WeavingContent content = content();
        WeavingRules.Attempt first = WeavingRules.attempt(content, WeavingData.empty(),
                amounts(id("chainback_link"), 1, mc("stick"), 1), AWAKENED);
        assertEquals(WeavingRules.Outcome.HINTED, first.outcome());
        assertEquals(Optional.of(id("linked_cleaver")), first.recipe(), "lowest Tier first on a tie");
        assertEquals(List.of(id("chainback_link")), first.data().hint(id("linked_cleaver")));

        WeavingRules.Attempt second = WeavingRules.attempt(content, first.data(),
                amounts(id("chainback_link"), 1, mc("iron_ingot"), 3), AWAKENED);
        assertEquals(List.of(id("chainback_link"), mc("iron_ingot")), second.data().hint(id("linked_cleaver")));

        assertEquals(WeavingRules.Outcome.NOTHING, WeavingRules.attempt(content, WeavingData.empty(),
                amounts(mc("dirt"), 4), AWAKENED).outcome());
        assertEquals(WeavingRules.Outcome.EMPTY, WeavingRules.attempt(content, WeavingData.empty(), Map.of(), AWAKENED).outcome());
    }

    @Test
    void chainbackMailIsOnlyShownByTheSpell() throws IOException {
        WeavingContent content = content();
        Map<ResourceLocation, Integer> mail = content.recipes().get(id("chainback_mail")).amounts();
        assertEquals(WeavingRules.Outcome.UNSHOWN, WeavingRules.attempt(content, WeavingData.empty(), mail, AWAKENED).outcome());
        assertEquals(List.of(id("chainback_mail")), WeavingRules.grantsForKill(content, WeavingData.empty(), id("chainback")));
        assertEquals(List.of(), WeavingRules.grantsForKill(content, WeavingData.empty(), id("ash_burrower")));
        WeavingData granted = WeavingData.empty().withLearned(id("chainback_mail"));
        assertEquals(List.of(), WeavingRules.grantsForKill(content, granted, id("chainback")), "only the first kill");
    }

    @Test
    void progressRoundTripsThroughItsCodec() {
        WeavingData data = WeavingData.empty().withXp(FORGING, 70).withBought(FORGING, 2).withLearned(id("hollow_edge"))
                .withHint(id("hollow_visage"), List.of(id("mimic_husk"))).withDeedProgress(FORGING, 3, 2, false).withWoven(id("shard_knife"));
        WeavingData back = WeavingData.CODEC.codec().parse(JsonOps.INSTANCE,
                WeavingData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow();
        assertEquals(data, back);
    }

    @Test
    void theSoulScreenShowsHintsWithTheirMissingMaterials() throws IOException {
        WeavingContent content = content();
        WeavingData data = WeavingData.empty().withHint(id("hollow_edge"), List.of(id("mimic_husk")));
        WeavingStatusPayload status = WeavingService.status(content, data, AWAKENED, "");
        List<String> texts = SoulPages.page(SoulPages.Tab.WEAVING, null, null, 0.0F, List.of(), null, status,
                item -> item.substring(item.indexOf(':') + 1)).stream().map(SoulPages.Entry::text).toList();
        assertTrue(texts.contains("Forging, Tier III: mimic_husk, ?, ?, ?"), texts.toString());
        assertTrue(texts.contains("Patterns you know: 11"), texts.toString());
        assertTrue(texts.stream().anyMatch(text -> text.startsWith("Level 2 costs 1 point")), texts.toString());
    }
}
