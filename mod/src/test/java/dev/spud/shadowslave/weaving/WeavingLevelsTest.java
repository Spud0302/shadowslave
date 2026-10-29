package dev.spud.shadowslave.weaving;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.smithing.Quality;
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

/** The higher branch levels, their deeds and perks, and strands of a Weaver's own making, from the mod's data. */
class WeavingLevelsTest {
    private static final Path DATA = Path.of("src/main/resources/data/shadowslave/weaving");
    private static final Path PATTERNS = Path.of("src/main/resources/data/shadowslave/smithing/pattern");
    private static final ResourceLocation FORGING = id("forging");
    private static final ResourceLocation PATTERNS_BRANCH = id("patterns");
    private static final ResourceLocation BINDING = id("binding");
    private static final ResourceLocation LINKS = id("dragging_links");
    private static final ResourceLocation ECHO = id("echoing_cut");
    private static final ResourceLocation WHETTED = id("whetted_edge");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("shadowslave", path);
    }

    private static <T> Map<ResourceLocation, T> folder(Path folder, Codec<T> codec) throws IOException {
        Map<ResourceLocation, T> values = new HashMap<>();
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.toList()) {
                values.put(id(file.getFileName().toString().replace(".json", "")),
                        codec.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow());
            }
        }
        return values;
    }

    private static WeavingContent content() throws IOException {
        List<String> problems = new ArrayList<>();
        WeavingSettings settings = WeavingSettings.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(DATA.resolve("rules.json")))).getOrThrow();
        WeavingContent content = WeavingContent.build(settings, folder(DATA.resolve("branch"), WeavingBranch.CODEC),
                folder(PATTERNS, WeavingRecipe.CODEC), folder(DATA.resolve("strand"), StrandPattern.CODEC), problems::add);
        assertEquals(List.of(), problems);
        return content;
    }

    /** Buys every point level and earns every deed of a branch up to {@code level}. */
    private static WeavingData reach(WeavingContent content, WeavingData data, ResourceLocation branch, int level) {
        WeavingBranch found = content.branch(branch).orElseThrow();
        for (int number = 2; number <= level; number++) {
            WeavingBranch.Level each = found.level(number);
            data = each.deed().isPresent() ? data.withDeedProgress(branch, number, 0, true) : data.withBought(branch, number);
        }
        return data;
    }

    @Test
    void forgingPatternsAndBindingClimbToLevelSevenWithDeedsAtThreeFiveAndSeven() throws IOException {
        WeavingContent content = content();
        for (ResourceLocation branch : List.of(FORGING, PATTERNS_BRANCH, BINDING)) {
            WeavingBranch found = content.branch(branch).orElseThrow();
            assertEquals(7, found.maxLevel(), branch + " has seven levels, one per Memory Tier");
            for (int number = 2; number <= 7; number++) {
                assertEquals(number % 2 == 1, found.level(number).deed().isPresent(), branch + " level " + number);
            }
        }
        assertEquals(BINDING, content.strand(MemoryWeave.MEMORY_WEAVE).orElseThrow().branch(), "the Memory weave is Binding work");
        assertEquals(BINDING, content.strand(MemoryWeave.BOUND).orElseThrow().branch());
        assertEquals(PATTERNS_BRANCH, content.strand(LINKS).orElseThrow().branch(), "other strands stay with Patterns");
    }

    @Test
    void perksAddUpAcrossHeldLevelsAndCreateTakesTheHighest() throws IOException {
        WeavingContent content = content();
        WeavingData data = WeavingData.empty();
        assertEquals(0, WeavingRules.perk(content, data, WeavingBranch.PerkKind.SOUL_CAPACITY));
        data = reach(content, data, BINDING, 7);
        assertEquals(4, WeavingRules.perk(content, data, WeavingBranch.PerkKind.SOUL_CAPACITY), "1 + 1 + 2 more Memories");
        assertEquals(100, WeavingRules.perk(content, data, WeavingBranch.PerkKind.MEND));
        assertEquals(50, WeavingRules.perk(content, data, WeavingBranch.PerkKind.SUMMON_DISCOUNT));
        assertEquals(7, StrandRules.soulCapacity(Optional.of(SoulRank.DORMANT), 4));
        assertEquals(0, WeavingRules.perk(content, data, WeavingBranch.PerkKind.CREATE), "fusing is a Patterns gift");
        assertEquals(70, WeavingRules.perk(content, reach(content, data, PATTERNS_BRANCH, 5), WeavingBranch.PerkKind.CREATE));
        assertEquals(85, WeavingRules.perk(content, reach(content, data, PATTERNS_BRANCH, 7), WeavingBranch.PerkKind.CREATE));
        assertEquals(1, WeavingRules.perk(content, reach(content, data, FORGING, 7), WeavingBranch.PerkKind.EXTRA_THREAD));
        assertEquals(5, StrandRules.threads(Optional.of(Quality.MASTERWORK), 1, true), "a master's Masterwork holds five");
        assertEquals(1, StrandRules.threads(Optional.empty(), 1, true), "plain gear still takes one");
    }

    @Test
    void deedFiltersCountOnlyWhatTheyAskFor() throws IOException {
        WeavingContent content = content();
        WeavingData data = reach(content, WeavingData.empty(), FORGING, 4);
        List<String> earned = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            data = WeavingRules.advanceDeeds(content, data, FORGING, WeavingBranch.Made.forged(1, Quality.FINE), earned);
        }
        assertTrue(earned.isEmpty(), "Fine pieces don't count toward a Superior deed");
        for (int i = 0; i < 3; i++) {
            data = WeavingRules.advanceDeeds(content, data, FORGING, WeavingBranch.Made.forged(1, i == 0 ? Quality.MASTERWORK : Quality.SUPERIOR), earned);
        }
        assertTrue(data.hasDeed(FORGING, 5), "three Superior or better: " + earned);
        assertTrue(data.hasDeed(FORGING, 7), "the Masterwork earns level 7's deed too");

        WeavingData binding = reach(content, WeavingData.empty(), BINDING, 2);
        StrandPattern memory = content.strand(MemoryWeave.MEMORY_WEAVE).orElseThrow();
        StrandPattern links = content.strand(LINKS).orElseThrow();
        binding = StrandRules.credit(content, binding, LINKS, links, 1, BINDING).data();
        assertEquals(0, binding.deedProgress(BINDING, 3), "only the Memory weave counts toward Binding 3");
        for (int i = 0; i < 3; i++) {
            binding = StrandRules.credit(content, binding, MemoryWeave.MEMORY_WEAVE, memory, 1, BINDING).data();
        }
        assertTrue(binding.hasDeed(BINDING, 3));

        WeavingData held = WeavingRules.holdDeeds(content, binding, 4, earned);
        assertFalse(held.hasDeed(BINDING, 5), "four held is not five");
        assertEquals(4, held.deedProgress(BINDING, 5));
        held = WeavingRules.holdDeeds(content, held, 5, earned);
        assertTrue(held.hasDeed(BINDING, 5));

        WeavingData patterns = reach(content, WeavingData.empty(), PATTERNS_BRANCH, 4);
        patterns = StrandRules.credit(content, patterns, LINKS, links, 1, PATTERNS_BRANCH, false).data();
        assertEquals(0, patterns.deedProgress(PATTERNS_BRANCH, 5), "an untuned strand doesn't count");
        patterns = StrandRules.credit(content, patterns, LINKS, links, 1, PATTERNS_BRANCH, true).data();
        assertEquals(1, patterns.deedProgress(PATTERNS_BRANCH, 5));
    }

    @Test
    void twoStrandsFuseIntoOneThatDoesBothAtTheSharedStrength() throws IOException {
        WeavingContent content = content();
        assertEquals(Optional.empty(), StrandFusion.check(content, LINKS, ECHO));
        assertTrue(StrandFusion.check(content, LINKS, LINKS).isPresent(), "a strand can't fuse with itself");
        assertTrue(StrandFusion.check(content, LINKS, MemoryWeave.MEMORY_WEAVE).isPresent(), "the Memory weave isn't a power");
        assertTrue(StrandFusion.check(content, LINKS, id("cinder_skin")).isPresent(), "a blade strand and a chest strand never meet");
        assertEquals("Dragging Cut", StrandFusion.defaultName("Dragging Links", "Echoing Cut"));
        assertEquals("Ash-born Edge", StrandFusion.name("  Ash-born   Edge§!", "x"));
        assertEquals("x", StrandFusion.name("  §§ ", "x"));

        ResourceLocation id = StrandFusion.id(LINKS, ECHO, 70);
        WovenStrands.Fused fused = new WovenStrands.Fused("Dragging Cut", List.of(LINKS, ECHO), 70);
        StrandPattern pattern = StrandFusion.pattern(content, id, fused).orElseThrow();
        StrandPattern links = content.strand(LINKS).orElseThrow();
        StrandPattern echo = content.strand(ECHO).orElseThrow();
        assertEquals(StrandPattern.Kind.FUSED, pattern.effect().kind());
        assertEquals(List.of(StrandFit.WEAPON), pattern.fits());
        assertEquals(SoulRank.AWAKENED, pattern.minRank(), "the higher Rank of the two");
        assertEquals(links.essence().get(1) + echo.essence().get(1), pattern.essence().get(1));
        assertEquals(Map.of(links.material().item(), links.material().count(2), echo.material().item(), echo.material().count(2)),
                pattern.materials(2), "it is spun from both parts' materials");
        assertTrue(pattern.tunable());
        List<StrandPattern.Point> points = StrandRules.points(id, pattern);
        assertTrue(points.size() >= StrandRules.MIN_POINTS && points.size() <= StrandRules.MAX_POINTS, "a traceable shape");
        assertEquals(StrandRules.points(LINKS, links).getFirst(), points.getFirst(), "the first part's pattern leads");

        WovenStrands.Laid laid = new WovenStrands.Laid(id, 2, 100, Optional.empty(), "Jack", Optional.of(fused));
        List<StrandFusion.Part> parts = StrandFusion.parts(content, laid);
        assertEquals(2, parts.size());
        assertEquals(StrandRules.strength(links, parts.getFirst().laid()) * 0.7F,
                StrandRules.strength(parts.getFirst().strand(), parts.getFirst().laid()) * parts.getFirst().share(), 1.0E-5F);

        WovenStrands.Fused withAttribute = new WovenStrands.Fused("Keen Links", List.of(WHETTED, LINKS), 70);
        assertFalse(StrandFusion.pattern(content, StrandFusion.id(WHETTED, LINKS, 70), withAttribute).orElseThrow().tunable(),
                "an always-on part can't be tied to a condition");

        WeavingData data = WeavingData.empty().withCreated(id, fused);
        assertTrue(StrandFusion.known(content, data, id));
        assertEquals("Dragging Cut", StrandFusion.resolve(content, data, id).orElseThrow().name());
        WeavingData back = WeavingData.CODEC.codec().parse(JsonOps.INSTANCE,
                WeavingData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow();
        assertEquals(data, back, "own strands are saved with the player");
        WovenStrands strands = new WovenStrands(List.of(laid));
        assertEquals(strands, WovenStrands.CODEC.parse(JsonOps.INSTANCE, WovenStrands.CODEC.encodeStart(JsonOps.INSTANCE, strands).getOrThrow()).getOrThrow(),
                "a fused strand carries its parts on the piece");
    }

    @Test
    void summonsAndMendsFollowTheBindingPerks() {
        assertEquals(StrandRules.SUMMON_ESSENCE, StrandRules.summonCost(100, 0));
        assertEquals(3, StrandRules.summonCost(100, 50));
        assertEquals(1, StrandRules.summonCost(100, 100), "never quite free");
        assertEquals(StrandRules.mend(1000), StrandRules.mend(1000, 0));
        assertEquals(StrandRules.mend(1000) * 2, StrandRules.mend(1000, 100));
    }

    @Test
    void forgingLevelFourOpensTheNetheritePatterns() throws IOException {
        WeavingContent content = content();
        WeavingRecipe sword = content.recipes().get(id("netherite_sword"));
        assertEquals(4, content.tier(sword));
        assertTrue(WeavingRules.lock(content, reach(content, WeavingData.empty(), FORGING, 3), sword, Optional.empty()).isPresent());
        assertEquals(Optional.empty(), WeavingRules.lock(content, reach(content, WeavingData.empty(), FORGING, 4), sword, Optional.empty()));
    }
}
