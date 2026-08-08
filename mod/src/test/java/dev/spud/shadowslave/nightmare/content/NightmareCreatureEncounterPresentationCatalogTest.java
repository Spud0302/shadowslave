package dev.spud.shadowslave.nightmare.content;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCreatureEncounterPresentationCatalogTest {
    private final NightmareCreatureEncounterPresentationCatalog catalog =
            NightmareCreatureEncounterPresentationCatalog.waveOne();

    @Test
    void coversEveryExistingCreatureExactly() {
        Set<String> sourceIds = NightmareCreatureContentCatalog.waveOne().stream()
                .map(NightmareCreatureContentCatalog.CreatureProfile::id)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(sourceIds, catalog.creatureIds());
        assertEquals(12, catalog.creatureIds().size());
    }

    @Test
    void preservesAuthoritativeCreatureIdentityAndRankClass() {
        for (var source : NightmareCreatureContentCatalog.waveOne()) {
            var result = catalog.compose(17L, source.id());
            assertEquals(source.id(), result.creatureId());
            assertEquals(source.displayName(), result.displayName());
            assertEquals(source.rank(), result.rank());
            assertEquals(source.creatureClass(), result.creatureClass());
            assertEquals(source.presentationCue(), result.firstSign());
            assertEquals(source.counterplayTags(), result.sourceCounterplayTags());
            assertEquals("DESIGN", result.provenance());
            assertEquals(NightmareCreatureEncounterPresentationCatalog.PRESENTATION_VERSION,
                    result.presentationVersion());
            assertFalse(result.threatRead().isBlank());
            assertFalse(result.counterplayHint().isBlank());
            assertFalse(result.boundary().isBlank());
        }
    }

    @Test
    void sameSeedAndIdentityAreDeterministic() {
        var first = catalog.compose(9001L, "hollow_mimic");
        var second = catalog.compose(9001L, "hollow_mimic");
        assertEquals(first, second);
    }

    @Test
    void seedMayVaryHintButCannotVaryCreatureState() {
        var source = NightmareCreatureContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals("veil_stalker"))
                .findFirst().orElseThrow();
        Set<String> hints = new HashSet<>();

        for (long seed = 0; seed < 256; seed++) {
            var result = catalog.compose(seed, source.id());
            hints.add(result.counterplayHint());
            assertEquals(source.id(), result.creatureId());
            assertEquals(source.displayName(), result.displayName());
            assertEquals(source.rank(), result.rank());
            assertEquals(source.creatureClass(), result.creatureClass());
            assertEquals(source.counterplayTags(), result.sourceCounterplayTags());
        }

        assertEquals(source.counterplayTags().size(), hints.size());
    }

    @Test
    void everyCreatureCanSurfaceEveryAuthoredCounterplayHint() {
        for (var source : NightmareCreatureContentCatalog.waveOne()) {
            Set<String> hints = new HashSet<>();
            for (long seed = 0; seed < 512; seed++) {
                hints.add(catalog.compose(seed, source.id()).counterplayHint());
            }
            assertEquals(source.counterplayTags().size(), hints.size(), source.id());
        }
    }

    @Test
    void highRiskPresentationKeepsExplicitBoundaries() {
        assertTrue(catalog.compose(1L, "hollow_mimic").boundary().contains("perfect lie detection"));
        assertTrue(catalog.compose(1L, "gutter_choir").boundary().contains("not Devil-class guarantees"));
        assertTrue(catalog.compose(1L, "thorn_matron").boundary().contains("not claim Fallen Devils"));
        assertTrue(catalog.compose(1L, "pale_ferryman").boundary().contains("does not establish a canonical"));
    }

    @Test
    void unknownCreatureFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> catalog.compose(1L, "not_a_creature"));
    }
}