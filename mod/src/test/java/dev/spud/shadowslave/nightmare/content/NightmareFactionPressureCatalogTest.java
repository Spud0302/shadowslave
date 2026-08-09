package dev.spud.shadowslave.nightmare.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NightmareFactionPressureCatalogTest {
    @Test
    void waveOneHasExactlyFourPrimitivesPerPressureFamily() {
        List<NightmareFactionPressureCatalog.Primitive> primitives = NightmareFactionPressureCatalog.waveOne();
        assertEquals(24, primitives.size());
        assertEquals(24, primitives.stream().map(NightmareFactionPressureCatalog.Primitive::id).distinct().count());
        for (NightmareFactionPressureCatalog.PressureFamily family : NightmareFactionPressureCatalog.PressureFamily.values()) {
            assertEquals(4, primitives.stream().filter(primitive -> primitive.family() == family).count(), family.name());
        }
    }

    @Test
    void everyPrimitiveHasBoundedPlayerFacingNegotiationContent() {
        for (NightmareFactionPressureCatalog.Primitive primitive : NightmareFactionPressureCatalog.waveOne()) {
            assertEquals(3, primitive.playerLevers().size(), primitive.id());
            assertEquals(2, primitive.presentationCues().size(), primitive.id());
            assertFalse(primitive.affinityTags().isEmpty(), primitive.id());
            assertTrue(primitive.pressureRead().length() >= 55, primitive.id());
            assertTrue(primitive.negotiationQuestion().length() >= 50, primitive.id());
            assertTrue(primitive.antiOverclaimBoundary().length() >= 55, primitive.id());
        }
    }

    @Test
    void compositionPreservesOpaqueJavaAuthorityAndAllowedFamiliesAcrossSeedSweep() {
        String scenario = "shadow:LanternBelow/V2";
        String faction = "Faction:OldWatch/ThirdCompany";
        Set<NightmareFactionPressureCatalog.PressureFamily> allowed = EnumSet.of(
                NightmareFactionPressureCatalog.PressureFamily.DUTY,
                NightmareFactionPressureCatalog.PressureFamily.RESCUE,
                NightmareFactionPressureCatalog.PressureFamily.SURVIVAL);

        for (long seed = 0; seed < 4096; seed++) {
            NightmareFactionPressureCatalog.Selection selection = NightmareFactionPressureCatalog.compose(
                    seed, scenario, faction, allowed, Map.of());
            assertEquals(scenario, selection.scenarioId());
            assertEquals(faction, selection.factionId());
            assertEquals(allowed, selection.allowedFamilies());
            assertTrue(allowed.contains(selection.primitive().family()));
            assertTrue(selection.primitive().presentationCues().contains(selection.presentationCue()));
        }
    }

    @Test
    void neutralSeedsReachEveryPrimitiveAndCuePair() {
        Set<String> primitives = new HashSet<>();
        Set<String> pairs = new HashSet<>();
        Set<NightmareFactionPressureCatalog.PressureFamily> all = EnumSet.allOf(NightmareFactionPressureCatalog.PressureFamily.class);
        for (long seed = 0; seed < 16384; seed++) {
            NightmareFactionPressureCatalog.Selection selection = NightmareFactionPressureCatalog.compose(
                    seed, "scenario", "faction", all, Map.of());
            primitives.add(selection.primitive().id());
            pairs.add(selection.primitive().id() + "|" + selection.presentationCue());
        }
        assertEquals(24, primitives.size());
        assertEquals(48, pairs.size());
    }

    @Test
    void allowedFamilySetOrderDoesNotAffectSelection() {
        Set<NightmareFactionPressureCatalog.PressureFamily> first = new java.util.LinkedHashSet<>();
        first.add(NightmareFactionPressureCatalog.PressureFamily.SECRECY);
        first.add(NightmareFactionPressureCatalog.PressureFamily.RESOURCE);
        Set<NightmareFactionPressureCatalog.PressureFamily> second = new java.util.LinkedHashSet<>();
        second.add(NightmareFactionPressureCatalog.PressureFamily.RESOURCE);
        second.add(NightmareFactionPressureCatalog.PressureFamily.SECRECY);

        NightmareFactionPressureCatalog.Selection a = NightmareFactionPressureCatalog.compose(
                81L, "Scenario:Opaque", "Faction:Opaque", first, Map.of());
        NightmareFactionPressureCatalog.Selection b = NightmareFactionPressureCatalog.compose(
                81L, "Scenario:Opaque", "Faction:Opaque", second, Map.of());
        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
    }

    @Test
    void evidenceMapOrderAndMagnitudeCannotCreateFactionAuthority() {
        Map<String, Integer> first = new HashMap<>();
        first.put("route", 1);
        first.put("access", 1);
        Map<String, Integer> second = new HashMap<>();
        second.put("access", 999);
        second.put("route", 999);
        Set<NightmareFactionPressureCatalog.PressureFamily> allowed = EnumSet.of(
                NightmareFactionPressureCatalog.PressureFamily.SECRECY,
                NightmareFactionPressureCatalog.PressureFamily.TERRITORIAL,
                NightmareFactionPressureCatalog.PressureFamily.RESCUE);

        NightmareFactionPressureCatalog.Selection a = NightmareFactionPressureCatalog.compose(
                77L, "Scenario:Opaque", "Faction:Opaque", allowed, first);
        NightmareFactionPressureCatalog.Selection b = NightmareFactionPressureCatalog.compose(
                77L, "Scenario:Opaque", "Faction:Opaque", allowed, second);

        assertEquals(a.primitive().id(), b.primitive().id());
        assertEquals(a.presentationCue(), b.presentationCue());
        assertEquals(a.matchedEvidenceTags(), b.matchedEvidenceTags());
        assertEquals("Scenario:Opaque", a.scenarioId());
        assertEquals("Faction:Opaque", a.factionId());
        assertEquals(allowed, a.allowedFamilies());
    }

    @Test
    void positiveEvidenceMayPreferCompatiblePressureWithoutChangingAuthority() {
        Set<NightmareFactionPressureCatalog.PressureFamily> allowed = Set.of(NightmareFactionPressureCatalog.PressureFamily.RESOURCE);
        for (long seed = 0; seed < 512; seed++) {
            NightmareFactionPressureCatalog.Selection selection = NightmareFactionPressureCatalog.compose(
                    seed, "scenario", "faction", allowed, Map.of("water", 1));
            assertEquals("resource_hold_water", selection.primitive().id());
            assertEquals(allowed, selection.allowedFamilies());
            assertTrue(selection.matchedEvidenceTags().contains("water"));
        }
    }

    @Test
    void boundariesExplicitlyRejectFactionAndResolutionAuthority() {
        String bargain = NightmareFactionPressureCatalog.byId("survival_bargain_passage").antiOverclaimBoundary().toLowerCase();
        String crossing = NightmareFactionPressureCatalog.byId("territorial_hold_crossing").antiOverclaimBoundary().toLowerCase();
        String secrecy = NightmareFactionPressureCatalog.byId("secrecy_conceal_failure").antiOverclaimBoundary().toLowerCase();
        String rescue = NightmareFactionPressureCatalog.byId("rescue_trade_time").antiOverclaimBoundary().toLowerCase();

        assertTrue(bargain.contains("allegiance"));
        assertTrue(crossing.contains("scenario"));
        assertTrue(secrecy.contains("reputation"));
        assertTrue(rescue.contains("appraisal"));
    }

    @Test
    void malformedAuthorityAndEvidenceFailClosed() {
        Set<NightmareFactionPressureCatalog.PressureFamily> duty = Set.of(NightmareFactionPressureCatalog.PressureFamily.DUTY);
        List<Runnable> invalid = new ArrayList<>();
        invalid.add(() -> NightmareFactionPressureCatalog.compose(1L, "", "faction", duty, Map.of()));
        invalid.add(() -> NightmareFactionPressureCatalog.compose(1L, "scenario", "", duty, Map.of()));
        invalid.add(() -> NightmareFactionPressureCatalog.compose(1L, "scenario", "faction", Set.of(), Map.of()));
        invalid.add(() -> NightmareFactionPressureCatalog.compose(1L, "scenario", "faction", duty, Map.of("duty", -1)));
        invalid.add(() -> NightmareFactionPressureCatalog.byId("unknown_pressure"));
        for (Runnable call : invalid) assertThrows(RuntimeException.class, call::run);
    }
}