package dev.spud.shadowslave.appraisal;

import dev.spud.shadowslave.content.spell.SpellPresentationCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstNightmareSpellPresentationTest {
    @Test
    void rendersResolvedRuntimeStateInLifecycleOrder() {
        FirstNightmareSpellPresentation.ResolvedView view = new FirstNightmareSpellPresentation.ResolvedView(
                "The Drowned Bell",
                "Ferry Deckhand",
                "The Flood Diverted",
                "Ember-Wake",
                "Brittle Promise",
                Optional.of("Tide Listener"),
                "Ash Compass",
                "Ash Burrower"
        );

        List<SpellPresentationCatalog.PresentationLine> lines = FirstNightmareSpellPresentation.render(view);

        assertEquals(List.of(
                SpellPresentationCatalog.EventKind.NIGHTMARE_RESOLVED,
                SpellPresentationCatalog.EventKind.APPRAISAL_BEGIN,
                SpellPresentationCatalog.EventKind.APPRAISAL_SUMMARY,
                SpellPresentationCatalog.EventKind.APPRAISAL_DEED,
                SpellPresentationCatalog.EventKind.APPRAISAL_VERDICT,
                SpellPresentationCatalog.EventKind.ASPECT_REVEALED,
                SpellPresentationCatalog.EventKind.FLAW_REVEALED,
                SpellPresentationCatalog.EventKind.ATTRIBUTE_REVEALED,
                SpellPresentationCatalog.EventKind.MEMORY_RECEIVED,
                SpellPresentationCatalog.EventKind.ECHO_RECEIVED
        ), lines.stream().map(SpellPresentationCatalog.PresentationLine::kind).toList());

        String rendered = lines.stream().map(SpellPresentationCatalog.PresentationLine::text)
                .reduce("", (left, right) -> left + "\n" + right);
        assertTrue(rendered.contains("The Drowned Bell"));
        assertTrue(rendered.contains("Ferry Deckhand"));
        assertTrue(rendered.contains("The Flood Diverted"));
        assertTrue(rendered.contains("Ember-Wake"));
        assertTrue(rendered.contains("Brittle Promise"));
        assertTrue(rendered.contains("Tide Listener"));
        assertTrue(rendered.contains("Ash Compass"));
        assertTrue(rendered.contains("Ash Burrower"));
        assertFalse(rendered.contains("Last Light"));
        assertFalse(rendered.contains("Cold Ash"));
    }

    @Test
    void obscuredAttributeIsNotFalselyRevealed() {
        FirstNightmareSpellPresentation.ResolvedView view = new FirstNightmareSpellPresentation.ResolvedView(
                "The Last Signal",
                "Watch Apprentice",
                "Signal Restored",
                "Ember-Wake",
                "Brittle Promise",
                Optional.empty(),
                "Ash Compass",
                "Ash Burrower"
        );

        List<SpellPresentationCatalog.PresentationLine> lines = FirstNightmareSpellPresentation.render(view);
        assertFalse(lines.stream().anyMatch(line -> line.kind() == SpellPresentationCatalog.EventKind.ATTRIBUTE_REVEALED));
        assertEquals(9, lines.size());
    }

    @Test
    void surfacesRemainCatalogueOwned() {
        List<SpellPresentationCatalog.PresentationLine> lines = FirstNightmareSpellPresentation.render(
                new FirstNightmareSpellPresentation.ResolvedView(
                        "The Last Signal", "Watch Apprentice", "Signal Restored",
                        "Ember-Wake", "Brittle Promise", Optional.of("Watcher's Mark"),
                        "Ash Compass", "Ash Burrower"
                )
        );

        assertTrue(lines.stream()
                .filter(line -> line.kind() == SpellPresentationCatalog.EventKind.ASPECT_REVEALED
                        || line.kind() == SpellPresentationCatalog.EventKind.FLAW_REVEALED
                        || line.kind() == SpellPresentationCatalog.EventKind.ATTRIBUTE_REVEALED)
                .allMatch(line -> line.surface() == SpellPresentationCatalog.Surface.RUNES));
        assertTrue(lines.stream()
                .filter(line -> line.kind() == SpellPresentationCatalog.EventKind.NIGHTMARE_RESOLVED
                        || line.kind() == SpellPresentationCatalog.EventKind.APPRAISAL_BEGIN
                        || line.kind() == SpellPresentationCatalog.EventKind.MEMORY_RECEIVED
                        || line.kind() == SpellPresentationCatalog.EventKind.ECHO_RECEIVED)
                .allMatch(line -> line.surface() == SpellPresentationCatalog.Surface.VOICE));
    }
}
