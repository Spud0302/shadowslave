package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.appraisal.generation.ExpandedIdentityContentCatalog;
import dev.spud.shadowslave.appraisal.generation.IdentityPrimitiveCatalog;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure rules of the Warden, Seer, Reaper and Wellspring kits. */
class BackfillKitRulesTest {
    @Test
    void theKitWaveHoldsEveryKitsNatureAbilityAndFlawWhileWaveOneStaysAsItWas() {
        IdentityPrimitiveCatalog kits = ExpandedIdentityContentCatalog.kitWave();
        for (AspectKit kit : AspectKitCatalog.all()) {
            assertTrue(kits.natures().stream().anyMatch(nature -> nature.id().equals(kit.natureId())), kit.natureName());
            assertTrue(kits.abilities().stream().anyMatch(ability -> ability.id().equals(kit.abilityId())), kit.abilityName());
            assertTrue(kits.flaws().stream().anyMatch(flaw -> flaw.id().equals(kit.flawId())), kit.flawName());
        }
        assertEquals(12, ExpandedIdentityContentCatalog.waveOne().natures().size());
        assertEquals(32, kits.natures().size());
    }

    @Test
    void everyKitAbilityIsActivatable() {
        for (AspectKit kit : AspectKitCatalog.all()) {
            assertTrue(AbilityRegistry.isActivatable(kit.abilityId()), kit.abilityName());
        }
    }

    @Test
    void hungerStopsTheRefillExceptWhileMeditating() {
        assertEquals(0.0F, EssenceService.kitRegenFactor(true, false, false, false));
        assertEquals(0.0F, EssenceService.kitRegenFactor(true, false, false, true));
        assertEquals(1.0F, EssenceService.kitRegenFactor(true, false, true, false));
        assertEquals(1.0F, EssenceService.kitRegenFactor(false, false, false, false));
    }

    @Test
    void deepWellFillsFasterWhileMeditatingOrGathering() {
        assertEquals(3.0F, EssenceService.kitRegenFactor(false, true, true, false));
        assertEquals(3.0F, EssenceService.kitRegenFactor(false, true, true, true));
        assertEquals(2.0F, EssenceService.kitRegenFactor(false, true, false, true));
        assertEquals(1.0F, EssenceService.kitRegenFactor(false, true, false, false));
    }

    @Test
    void soulFeastGrowsWithCoresAndRankButIsCapped() {
        assertEquals(14, KitPassives.feastFor(1, 0));
        assertEquals(32, KitPassives.feastFor(2, 1));
        assertTrue(KitPassives.feastFor(5, 1) > KitPassives.feastFor(2, 1));
        assertEquals(KitPassives.FEAST_MAX, KitPassives.feastFor(7, 6));
    }

    @Test
    void reapingArcCutsInFrontAndReapsOnlyTheBadlyWounded() {
        Vec3 origin = Vec3.ZERO;
        Vec3 north = new Vec3(0, 0, -1);
        assertTrue(ReapingArcAbility.inArc(origin, north, new Vec3(0, 0, -3)));
        assertTrue(ReapingArcAbility.inArc(origin, north, new Vec3(2, 0, -2)));
        assertFalse(ReapingArcAbility.inArc(origin, north, new Vec3(0, 0, 3)));
        assertFalse(ReapingArcAbility.inArc(origin, north, new Vec3(0, 0, -6)));

        assertTrue(ReapingArcAbility.reapable(5.0F, 20.0F));
        assertFalse(ReapingArcAbility.reapable(10.0F, 20.0F));
        assertFalse(ReapingArcAbility.reapable(10.0F, 200.0F));
    }

    @Test
    void fateThreadReturnsToThePointThreeSecondsBack() {
        Deque<FateThreadAbility.Point> trail = new ArrayDeque<>();
        assertNull(FateThreadAbility.pointBefore(trail, 100));
        for (long time = 40; time <= 100; time += 5) {
            trail.addLast(new FateThreadAbility.Point(time, null, new Vec3(time, 0, 0)));
        }
        assertEquals(40.0, FateThreadAbility.pointBefore(trail, 40).position().x);
        assertEquals(40.0, FateThreadAbility.pointBefore(trail, 10).position().x);
        assertEquals(60.0, FateThreadAbility.pointBefore(trail, 62).position().x);
    }
}
