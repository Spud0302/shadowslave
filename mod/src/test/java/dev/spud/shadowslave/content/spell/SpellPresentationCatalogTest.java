package dev.spud.shadowslave.content.spell;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellPresentationCatalogTest {
    @Test
    void waveOneCoversEveryDeclaredEventKindWithStableIdentity() {
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();

        assertEquals(EnumSet.allOf(SpellPresentationCatalog.EventKind.class),
                catalog.templates().stream()
                        .map(SpellPresentationCatalog.Template::kind)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(SpellPresentationCatalog.EventKind.class))));
        assertEquals(SpellPresentationCatalog.EventKind.values().length, catalog.templates().size());
        assertEquals(catalog.templates().size(), catalog.templates().stream().map(template -> template.id().toString()).distinct().count());
        assertTrue(catalog.templates().stream().allMatch(template -> template.provenance().startsWith("design/spell-presentation-wave1/")));
    }

    @Test
    void catalogueSeparatesVoiceEventsFromRuneInspection() {
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();

        Set<SpellPresentationCatalog.EventKind> voice = catalog.templates().stream()
                .filter(template -> template.surface() == SpellPresentationCatalog.Surface.VOICE)
                .map(SpellPresentationCatalog.Template::kind)
                .collect(Collectors.toSet());
        Set<SpellPresentationCatalog.EventKind> runes = catalog.templates().stream()
                .filter(template -> template.surface() == SpellPresentationCatalog.Surface.RUNES)
                .map(SpellPresentationCatalog.Template::kind)
                .collect(Collectors.toSet());

        assertTrue(voice.containsAll(Set.of(
                SpellPresentationCatalog.EventKind.NIGHTMARE_RESOLVED,
                SpellPresentationCatalog.EventKind.APPRAISAL_BEGIN,
                SpellPresentationCatalog.EventKind.APPRAISAL_SUMMARY,
                SpellPresentationCatalog.EventKind.APPRAISAL_DEED,
                SpellPresentationCatalog.EventKind.APPRAISAL_VERDICT,
                SpellPresentationCatalog.EventKind.MEMORY_RECEIVED,
                SpellPresentationCatalog.EventKind.ECHO_RECEIVED
        )));
        assertTrue(runes.containsAll(Set.of(
                SpellPresentationCatalog.EventKind.ATTRIBUTE_REVEALED,
                SpellPresentationCatalog.EventKind.ATTRIBUTE_EVOLUTION_READY,
                SpellPresentationCatalog.EventKind.ATTRIBUTE_EVOLVED,
                SpellPresentationCatalog.EventKind.ASPECT_REVEALED,
                SpellPresentationCatalog.EventKind.FLAW_REVEALED,
                SpellPresentationCatalog.EventKind.MEMORY_INSPECTION,
                SpellPresentationCatalog.EventKind.ECHO_INSPECTION
        )));
    }

    @Test
    void renderingIsStrictAndDoesNotSilentlyLoseFields() {
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();

        SpellPresentationCatalog.PresentationLine memory = catalog.line(
                SpellPresentationCatalog.EventKind.MEMORY_INSPECTION,
                Map.of("name", "Ash Compass", "rank", "Dormant", "tier", "II", "type", "Tool")
        );
        assertEquals(SpellPresentationCatalog.Surface.RUNES, memory.surface());
        assertTrue(memory.text().contains("Ash Compass"));
        assertTrue(memory.text().contains("Dormant"));
        assertTrue(memory.text().contains("II"));
        assertTrue(memory.text().contains("Tool"));

        assertThrows(IllegalArgumentException.class, () -> catalog.line(
                SpellPresentationCatalog.EventKind.MEMORY_INSPECTION,
                Map.of("name", "Ash Compass", "rank", "Dormant", "tier", "II")
        ));
        assertThrows(IllegalArgumentException.class, () -> catalog.line(
                SpellPresentationCatalog.EventKind.MEMORY_RECEIVED,
                Map.of("name", "Ash Compass", "unexpected", "ignored")
        ));
        assertThrows(IllegalArgumentException.class, () -> catalog.line(
                SpellPresentationCatalog.EventKind.ECHO_RECEIVED,
                Map.of("name", "   ")
        ));
    }

    @Test
    void appraisalPresentationHasCanonicalShapeWithoutCalculatingVerdict() {
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();

        List<SpellPresentationCatalog.PresentationLine> lines = catalog.appraisal(
                "The bell-keeper preserved the warning and led the trapped villagers to safety.",
                List.of(
                        "You restored the warning tower under pressure.",
                        "You chose rescue over pursuit."
                ),
                "remarkable"
        );

        assertEquals(List.of(
                        SpellPresentationCatalog.EventKind.NIGHTMARE_RESOLVED,
                        SpellPresentationCatalog.EventKind.APPRAISAL_BEGIN,
                        SpellPresentationCatalog.EventKind.APPRAISAL_SUMMARY,
                        SpellPresentationCatalog.EventKind.APPRAISAL_DEED,
                        SpellPresentationCatalog.EventKind.APPRAISAL_DEED,
                        SpellPresentationCatalog.EventKind.APPRAISAL_VERDICT
                ),
                lines.stream().map(SpellPresentationCatalog.PresentationLine::kind).toList());
        assertEquals("Final appraisal: remarkable.", lines.get(lines.size() - 1).text());
    }

    @Test
    void acquisitionAndIdentityMessagesConsumeResolvedNamesRatherThanOwningIdentity() {
        SpellPresentationCatalog catalog = SpellPresentationCatalog.waveOne();

        assertEquals("Memory received: Bellglass Token.", catalog.line(
                SpellPresentationCatalog.EventKind.MEMORY_RECEIVED,
                Map.of("name", "Bellglass Token")
        ).text());
        assertEquals("Echo received: Glasswing.", catalog.line(
                SpellPresentationCatalog.EventKind.ECHO_RECEIVED,
                Map.of("name", "Glasswing")
        ).text());
        assertEquals("Aspect: Hollow Lantern.", catalog.line(
                SpellPresentationCatalog.EventKind.ASPECT_REVEALED,
                Map.of("name", "Hollow Lantern")
        ).text());
        assertEquals("Flaw: Borrowed Voice.", catalog.line(
                SpellPresentationCatalog.EventKind.FLAW_REVEALED,
                Map.of("name", "Borrowed Voice")
        ).text());
        assertEquals("Attribute evolved: Living Ember.", catalog.line(
                SpellPresentationCatalog.EventKind.ATTRIBUTE_EVOLVED,
                Map.of("name", "Living Ember")
        ).text());
    }
}
