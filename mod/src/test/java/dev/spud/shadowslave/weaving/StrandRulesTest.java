package dev.spud.shadowslave.weaving;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.smithing.Quality;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;
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

/** The pure strand rules and the strand data in the mod's resources. */
class StrandRulesTest {
    private static final Path STRANDS = Path.of("src/main/resources/data/shadowslave/weaving/strand");
    private static final Path LANG = Path.of("src/main/resources/assets/shadowslave/lang/en_us.json");
    private static final ResourceLocation LINKS = ResourceLocation.fromNamespaceAndPath("shadowslave", "dragging_links");

    private static Map<ResourceLocation, StrandPattern> strands() throws IOException {
        Map<ResourceLocation, StrandPattern> strands = new HashMap<>();
        try (Stream<Path> files = Files.list(STRANDS)) {
            for (Path file : files.toList()) {
                strands.put(ResourceLocation.fromNamespaceAndPath("shadowslave", file.getFileName().toString().replace(".json", "")),
                        StrandPattern.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow());
            }
        }
        return strands;
    }

    private static float[] through(List<StrandPattern.Point> points, float offset) {
        List<Float> samples = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            StrandPattern.Point to = points.get(i);
            StrandPattern.Point from = i == 0 ? to : points.get(i - 1);
            for (int step = 1; step <= 8; step++) {
                samples.add(from.x() + (to.x() - from.x()) * step / 8.0F + offset);
                samples.add(from.y() + (to.y() - from.y()) * step / 8.0F);
            }
        }
        float[] trace = new float[samples.size()];
        for (int i = 0; i < trace.length; i++) {
            trace[i] = samples.get(i);
        }
        return trace;
    }

    @Test
    void everyStrandLoadsWithANameAndAShape() throws IOException {
        Map<ResourceLocation, StrandPattern> strands = strands();
        assertEquals(12, strands.size());
        String lang = Files.readString(LANG);
        strands.forEach((id, strand) -> {
            assertTrue(lang.contains("\"strand.shadowslave." + id.getPath() + "\""), "lang names " + id);
            List<StrandPattern.Point> points = StrandRules.points(id, strand);
            assertTrue(points.size() >= StrandRules.MIN_POINTS && points.size() <= StrandRules.MAX_POINTS, id + " has a traceable shape");
            assertEquals(points, StrandRules.points(id, strand), "the same shape every time");
            assertEquals(points.size(), points.stream().distinct().count(), id + " never repeats a point");
        });
        assertEquals(SoulRank.AWAKENED, strands.get(ResourceLocation.fromNamespaceAndPath("shadowslave", "echoing_cut")).minRank());
        assertFalse(strands.get(ResourceLocation.fromNamespaceAndPath("shadowslave", "whetted_edge")).tunable(), "attribute strands stay untuned");
    }

    @Test
    void aTraceIsJudgedByHowCloselyItPassesThePointsInOrder() throws IOException {
        StrandPattern strand = strands().get(LINKS);
        List<StrandPattern.Point> points = StrandRules.points(LINKS, strand);
        float tolerance = StrandRules.tolerance(1, 1);
        assertEquals(1.0F, StrandRules.score(points, through(points, 0.0F), tolerance), 1.0E-4F);
        assertEquals(StrandRules.Trace.CLEAN, StrandRules.judge(StrandRules.score(points, through(points, tolerance * 0.5F), tolerance)));
        float loose = StrandRules.score(points, through(points, tolerance * 2.0F), tolerance);
        assertEquals(StrandRules.Trace.LOOSE, StrandRules.judge(loose), "two tolerances off is loose: " + loose);
        assertEquals(Math.round(loose * 100), StrandRules.potency(loose));
        List<StrandPattern.Point> backwards = new ArrayList<>(points);
        java.util.Collections.reverse(backwards);
        assertTrue(StrandRules.score(points, through(backwards, 0.0F), tolerance) < StrandRules.CLEAN, "order matters");
        assertEquals(StrandRules.Trace.FIZZLE, StrandRules.judge(StrandRules.score(points, new float[]{1.5F, 1.5F, 1.6F, 1.6F}, tolerance)));
        assertEquals(0.0F, StrandRules.score(points, new float[0], tolerance));
        // A pattern that crosses back over its first point: the first pass must still count.
        List<StrandPattern.Point> crossing = List.of(new StrandPattern.Point(0.0F, 0.0F), new StrandPattern.Point(0.0F, 0.8F),
                new StrandPattern.Point(0.0F, -0.8F), new StrandPattern.Point(0.8F, 0.0F));
        // The trace starts a little off the centre and passes exactly through it on the way back down.
        float[] wobbly = through(List.of(new StrandPattern.Point(tolerance * 0.3F, 0.0F), new StrandPattern.Point(0.0F, 0.8F),
                new StrandPattern.Point(0.0F, -0.8F), new StrandPattern.Point(0.8F, 0.0F)), 0.0F);
        assertEquals(StrandRules.Trace.CLEAN, StrandRules.judge(StrandRules.score(crossing, wobbly, tolerance)), "crossing back stays clean");
        assertTrue(StrandRules.tolerance(3, 1) > StrandRules.tolerance(1, 1), "Patterns levels steady the hand");
        assertTrue(StrandRules.tolerance(1, 3) < StrandRules.tolerance(1, 1), "higher Tiers are finer work");
    }

    @Test
    void threadsComeFromQualityAndTiersFromSoulBranchAndAnchor() {
        assertEquals(1, StrandRules.threads(Optional.empty(), true), "plain gear takes one strand");
        assertEquals(0, StrandRules.threads(Optional.empty(), false), "a stick takes none");
        assertEquals(4, StrandRules.threads(Optional.of(Quality.MASTERWORK), true));
        assertEquals(1, StrandRules.tierCap(Optional.empty(), 3, Optional.of(ShardRank.FALLEN)), "an unranked soul weaves Tier I");
        assertEquals(2, StrandRules.tierCap(Optional.of(SoulRank.AWAKENED), 3, Optional.of(ShardRank.AWAKENED)));
        assertEquals(1, StrandRules.tierCap(Optional.of(SoulRank.AWAKENED), 3, Optional.of(ShardRank.DORMANT)), "the anchor caps it");
        assertEquals(1, StrandRules.tierCap(Optional.of(SoulRank.AWAKENED), 1, Optional.of(ShardRank.AWAKENED)), "so does Patterns");
        assertEquals(0, StrandRules.tierCap(Optional.of(SoulRank.AWAKENED), 3, Optional.empty()), "no anchor, no strand");
    }

    @Test
    void essenceComesFirstThenShards() {
        assertEquals(Optional.of(new StrandRules.Payment(30, 0)), StrandRules.pay(30, 100, 0, 10));
        assertEquals(Optional.of(new StrandRules.Payment(5, 3)), StrandRules.pay(30, 5, 3, 10), "25 short: three Dormant shards");
        assertEquals(Optional.empty(), StrandRules.pay(30, 5, 2, 10), "not enough of either");
        assertEquals(Optional.of(new StrandRules.Payment(0, 1)), StrandRules.pay(30, 0, 1, StrandRules.shardEssence(ShardRank.AWAKENED, false)));
        assertEquals(3, StrandRules.shardEssence(ShardRank.DORMANT, true), "a faint shard burns weakly");
    }

    @Test
    void strengthFollowsTierPotencyAndTuning() throws IOException {
        StrandPattern strand = strands().get(LINKS);
        WovenStrands.Laid whole = new WovenStrands.Laid(LINKS, 2, 100, Optional.empty(), "Jack");
        assertEquals(strand.effect().values().get(1), StrandRules.strength(strand, whole), 1.0E-6F);
        assertEquals(strand.effect().values().get(1) * 0.5F, StrandRules.strength(strand, whole.withPotency(50)), 1.0E-6F);
        WovenStrands.Laid tuned = new WovenStrands.Laid(LINKS, 2, 100, Optional.of(StrandRestriction.NIGHT), "Jack");
        assertEquals(strand.effect().values().get(1) * StrandRules.TUNED, StrandRules.strength(strand, tuned), 1.0E-6F);
        assertEquals(StrandRules.UNRAVEL_KEEP, StrandRules.unravelled(100, 1));
        assertEquals(100, StrandRules.unravelled(100, 2), "a practised Forger keeps it whole");
    }

    @Test
    void laidStrandsRoundTripThroughTheirCodec() {
        WovenStrands strands = new WovenStrands(List.of(new WovenStrands.Laid(LINKS, 1, 88, Optional.of(StrandRestriction.DEEP), "Jack"),
                new WovenStrands.Laid(LINKS, 3, 100, Optional.empty(), "")));
        assertEquals(strands, WovenStrands.CODEC.parse(JsonOps.INSTANCE, WovenStrands.CODEC.encodeStart(JsonOps.INSTANCE, strands).getOrThrow())
                .getOrThrow());
        assertEquals(1, strands.without(0).size());
    }
}
