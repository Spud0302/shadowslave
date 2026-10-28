package dev.spud.shadowslave.smithing;

import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.weaving.WeavingRecipe;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The pure Smithing rules: heat, the hammer's marker, scoring and grades. */
class SmithingRulesTest {
    private static final ResourceLocation SWORD = ResourceLocation.fromNamespaceAndPath("shadowslave", "iron_sword");
    private static final WeavingRecipe.Forging BLADE = new WeavingRecipe.Forging(900, 1250, 6, true, 24);

    private static BilletData billet(int clean, int rough, int misses, int inBand, boolean burned, boolean quench) {
        BilletData fresh = BilletData.lay(SWORD, "Iron Sword", new WeavingRecipe.Forging(900, 1250, clean + rough, quench, 24),
                0L, 70, 7, false, "Jack");
        return new BilletData(fresh.pattern(), fresh.name(), 1100, 0L, 100, fresh.heatMin(), fresh.heatMax(), fresh.blows(),
                fresh.quench(), fresh.quenchTicks(), fresh.zone(), fresh.seed(), clean, rough, misses, inBand, 0, burned, false, "Jack");
    }

    @Test
    void heatGlowsThroughItsColoursAndCoolsToTheAir() {
        assertEquals(SmithingRules.Glow.COLD, SmithingRules.glow(20));
        assertEquals(SmithingRules.Glow.CHERRY, SmithingRules.glow(800));
        assertEquals(SmithingRules.Glow.ORANGE, SmithingRules.glow(1000));
        assertEquals(SmithingRules.Glow.YELLOW, SmithingRules.glow(1200));
        assertEquals(SmithingRules.Glow.WHITE, SmithingRules.glow(1350));
        assertEquals(1100 - 30, SmithingRules.cooled(1100, 10, 100), "3 degrees a tick");
        assertEquals(1100 - 21, SmithingRules.cooled(1100, 10, 70), "Thrifty Hands cools slower");
        assertEquals(SmithingRules.AMBIENT, SmithingRules.cooled(1100, 100_000, 100), "never below the air");
    }

    @Test
    void aHearthNeedsTheBellowsToReachYellow() {
        int fire = SmithingRules.AMBIENT;
        for (int i = 0; i < 400; i++) {
            fire = SmithingRules.hearthStep(fire, true, false);
        }
        assertEquals(SmithingRules.HEARTH_TEMPERATURE, fire);
        assertEquals(SmithingRules.Glow.ORANGE, SmithingRules.glow(fire));
        for (int i = 0; i < 400; i++) {
            fire = SmithingRules.hearthStep(fire, true, true);
        }
        assertEquals(SmithingRules.BELLOWS_TEMPERATURE, fire);
        assertTrue(SmithingRules.burning(fire, BLADE.heatMax()), "a billet left at bellows heat burns");
        assertEquals(fire - SmithingRules.HEARTH_FALL, SmithingRules.hearthStep(fire, false, false), "out of fuel it cools");
    }

    @Test
    void theMarkerSweepsBackAndForthAndBlowsAreJudgedByDistance() {
        for (int t = 0; t < 200; t++) {
            float marker = SmithingRules.marker(t, 3);
            assertTrue(marker >= 0.0F && marker <= 1.0F, "inside the bar at " + t);
        }
        float a = SmithingRules.marker(0, 0);
        float b = SmithingRules.marker(SmithingRules.SWEEP_TICKS * 2, 0);
        assertEquals(a, b, 1.0E-5F, "it returns after two sweeps");
        for (int blow = 0; blow < 50; blow++) {
            float target = SmithingRules.target(12345, blow);
            assertTrue(target >= 0.2F && target <= 0.8F, "the mark stays off the ends");
        }
        assertEquals(SmithingRules.Blow.CLEAN, SmithingRules.judge(0.55F, 0.5F, 70));
        assertEquals(SmithingRules.Blow.ROUGH, SmithingRules.judge(0.65F, 0.5F, 70));
        assertEquals(SmithingRules.Blow.MISS, SmithingRules.judge(0.9F, 0.5F, 70));
        assertEquals(80, SmithingRules.zone(2, false), "each Forging level widens the mark");
        assertEquals(140, SmithingRules.zone(1, true), "a Masterwork doubles it");
    }

    @Test
    void scoresAddHeatHammerAndQuenchIntoAGrade() {
        assertEquals(100, SmithingRules.score(billet(6, 0, 0, 6, false, true), 1.0F));
        assertEquals(Quality.MASTERWORK, SmithingRules.grade(100, false, false));
        // Half the blows rough, two misses, half struck too hot, an early quench.
        int middling = SmithingRules.score(billet(3, 3, 2, 3, false, true), 0.5F);
        assertEquals(Math.round(15 + 50 * 4.5F / 8 + 10), middling);
        assertEquals(Quality.FAIR, SmithingRules.grade(middling, false, false));
        assertEquals(Quality.FINE, SmithingRules.grade(middling, true, false), "a Masterwork lifts a grade");
        assertEquals(Quality.CRUDE, SmithingRules.grade(middling, false, true), "a crack drops one");
        assertEquals(Quality.CRUDE, SmithingRules.grade(5, false, true), "never below Crude");
        assertEquals(Quality.MASTERWORK, SmithingRules.grade(99, true, false), "never above Masterwork");
        assertEquals(90, SmithingRules.score(billet(6, 0, 0, 6, true, true), 1.0F), "burning costs ten");
        assertEquals(100, SmithingRules.score(billet(5, 0, 0, 5, false, false), 0.0F), "no quench scales the rest to 100");
    }

    @Test
    void aQuenchIsPerfectNearTheHissAndBrittleFarPastIt() {
        assertEquals(1.0F, SmithingRules.quench(24, 24));
        assertEquals(1.0F, SmithingRules.quench(28, 24));
        assertEquals(0.0F, SmithingRules.quench(4, 24));
        assertTrue(SmithingRules.quench(34, 24) > 0.0F && SmithingRules.quench(34, 24) < 1.0F);
        assertFalse(SmithingRules.cracked(44, 24));
        assertTrue(SmithingRules.cracked(45, 24));
    }

    @Test
    void gradesSetStatsAndThreads() {
        assertEquals(Quality.CRUDE, Quality.forScore(39));
        assertEquals(Quality.FAIR, Quality.forScore(40));
        assertEquals(Quality.SUPERIOR, Quality.forScore(96));
        assertEquals(Quality.MASTERWORK, Quality.forScore(97));
        assertEquals(100, Quality.FAIR.statPercent());
        assertEquals(4, Quality.MASTERWORK.threads());
    }

    @Test
    void aBilletRoundTripsThroughItsCodec() {
        BilletData billet = billet(3, 1, 2, 3, true, true);
        BilletData back = BilletData.CODEC.parse(JsonOps.INSTANCE, BilletData.CODEC.encodeStart(JsonOps.INSTANCE, billet).getOrThrow())
                .getOrThrow();
        assertEquals(billet, back);
        ForgedData forged = new ForgedData(Quality.FINE, 71, "Jack");
        assertEquals(forged, ForgedData.CODEC.parse(JsonOps.INSTANCE, ForgedData.CODEC.encodeStart(JsonOps.INSTANCE, forged).getOrThrow())
                .getOrThrow());
    }

    @Test
    void blowsCoolTheBilletAndCountTheBand() {
        BilletData hot = BilletData.lay(SWORD, "Iron Sword", BLADE, 0L, 70, 1, false, "Jack").measured(1300, 0L, 100);
        BilletData struck = hot.struck(SmithingRules.Blow.CLEAN, 1300, 0L, 100);
        assertEquals(1300 - SmithingRules.BLOW_COOL, struck.heat());
        assertEquals(0, struck.inBand(), "too hot: it lands but outside the band");
        BilletData banded = struck.struck(SmithingRules.Blow.ROUGH, 1200, 0L, 100);
        assertEquals(1, banded.inBand());
        assertEquals(2, banded.landed());
        BilletData missed = banded.struck(SmithingRules.Blow.MISS, 1180, 0L, 100);
        assertEquals(2, missed.landed(), "a miss shapes nothing");
        assertEquals(1, missed.misses());
        assertFalse(hot.measured(800, 0L, 100).workable(0L), "below the band it won't move");
    }
}
