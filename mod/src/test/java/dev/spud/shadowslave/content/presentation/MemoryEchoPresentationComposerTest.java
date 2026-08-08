package dev.spud.shadowslave.content.presentation;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog.MemoryProfile;
import dev.spud.shadowslave.content.presentation.MemoryEchoPresentationComposer.EventKind;
import dev.spud.shadowslave.content.presentation.MemoryEchoPresentationComposer.PresentationCard;
import dev.spud.shadowslave.content.presentation.MemoryEchoPresentationComposer.PresentationLine;
import dev.spud.shadowslave.content.presentation.MemoryEchoPresentationComposer.SubjectKind;
import dev.spud.shadowslave.echo.content.EchoContentCatalog;
import dev.spud.shadowslave.echo.content.EchoContentCatalog.EchoProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryEchoPresentationComposerTest {
    @Test
    void everyAuthoredMemoryProducesAcquisitionAndInspectionCards() {
        MemoryContentCatalog catalog = MemoryContentCatalog.waveOne();

        assertEquals(12, catalog.memories().size());
        for (MemoryProfile memory : catalog.memories()) {
            PresentationCard acquired = MemoryEchoPresentationComposer.memoryAcquired(memory);
            PresentationCard inspected = MemoryEchoPresentationComposer.memoryInspection(memory);

            assertEquals(memory.id().toString(), acquired.subjectId());
            assertEquals(memory.id().toString(), inspected.subjectId());
            assertEquals(SubjectKind.MEMORY, acquired.subjectKind());
            assertEquals(EventKind.ACQUIRED, acquired.eventKind());
            assertEquals(EventKind.INSPECTED, inspected.eventKind());
            assertTrue(acquired.title().contains(memory.formalName()));
            assertEquals(memory.formalName(), inspected.title());
            assertTrue(inspected.lines().size() >= 5);
            assertEquals("design/memory-echo-presentation-wave1", acquired.provenance());
        }
    }

    @Test
    void memoryInspectionPreservesResolvedIdentityAndAuthoredEnchantmentHooks() {
        MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.formalName().equals("Last Watch Mantle"))
                .findFirst()
                .orElseThrow();

        PresentationCard card = MemoryEchoPresentationComposer.memoryInspection(memory);
        Map<String, String> lines = indexed(card.lines());

        assertEquals("Awakened", lines.get("Rank"));
        assertEquals("5", lines.get("Tier"));
        assertEquals("Armor", lines.get("Type"));
        assertEquals("Guard, Rescue, Vigilance", lines.get("Themes"));
        assertEquals(3, card.lines().stream().filter(line -> line.label().startsWith("Enchantment · ")).count());
        assertTrue(card.lines().stream().anyMatch(line -> line.value().contains("fixed position")));
        assertTrue(card.lines().stream().anyMatch(line -> line.value().contains("nearby ally")));
    }

    @Test
    void everyAuthoredEchoProducesAcquisitionAndInspectionCards() {
        List<EchoProfile> echoes = EchoContentCatalog.waveOne();

        assertEquals(13, echoes.size());
        for (EchoProfile echo : echoes) {
            PresentationCard acquired = MemoryEchoPresentationComposer.echoAcquired(echo);
            PresentationCard inspected = MemoryEchoPresentationComposer.echoInspection(echo);

            assertEquals("shadowslave:echo/" + echo.id(), acquired.subjectId());
            assertEquals("shadowslave:echo/" + echo.id(), inspected.subjectId());
            assertEquals(SubjectKind.ECHO, acquired.subjectKind());
            assertEquals(EventKind.ACQUIRED, acquired.eventKind());
            assertEquals(EventKind.INSPECTED, inspected.eventKind());
            assertTrue(acquired.title().contains(echo.displayName()));
            assertEquals(echo.displayName(), inspected.title());
            assertTrue(inspected.lines().size() >= 5);
            assertEquals("design/memory-echo-presentation-wave1", inspected.provenance());
        }
    }

    @Test
    void creatureDerivedEchoInspectionPreservesSourceRankClassCommandsAndUtility() {
        EchoProfile echo = EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals("mire_runner"))
                .findFirst()
                .orElseThrow();

        Map<String, String> lines = indexed(MemoryEchoPresentationComposer.echoInspection(echo).lines());

        assertEquals("Mire Runner", lines.get("Creature Source"));
        assertEquals("Dormant", lines.get("Source Rank"));
        assertEquals("Beast", lines.get("Source Class"));
        assertEquals("Mount, Pursuer, Scout", lines.get("Roles"));
        assertEquals("Follow, Mount, Search, Withdraw", lines.get("Commands"));
        assertEquals("Fast Mount, Marsh, Shallow Water", lines.get("Utility"));
        assertTrue(lines.get("Field Use").contains("wet ground"));
    }

    @Test
    void artificialEchoDoesNotInventCreatureProvenance() {
        EchoProfile echo = EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals("steel_courser"))
                .findFirst()
                .orElseThrow();

        PresentationCard card = MemoryEchoPresentationComposer.echoInspection(echo);
        Map<String, String> lines = indexed(card.lines());

        assertEquals("Artificial", lines.get("Origin"));
        assertFalse(lines.containsKey("Creature Source"));
        assertFalse(lines.containsKey("Source Rank"));
        assertFalse(lines.containsKey("Source Class"));
        assertTrue(lines.get("Utility").contains("Artificial"));
    }

    @Test
    void presentationOrderingIsDeterministicForSetBackedCatalogueFields() {
        EchoProfile echo = EchoContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals("bell_eater"))
                .findFirst()
                .orElseThrow();

        PresentationCard first = MemoryEchoPresentationComposer.echoInspection(echo);
        PresentationCard second = MemoryEchoPresentationComposer.echoInspection(echo);

        assertEquals(first, second);
        Map<String, String> lines = indexed(first.lines());
        assertEquals("Disruptor, Guard, Tracker", lines.get("Roles"));
        assertEquals("Follow, Hold, Intercept, Search", lines.get("Commands"));
        assertEquals("Resonance, Sound Tracking, Warning", lines.get("Utility"));
    }

    private static Map<String, String> indexed(List<PresentationLine> lines) {
        return lines.stream().collect(Collectors.toMap(
                PresentationLine::label,
                PresentationLine::value,
                (first, second) -> first
        ));
    }
}
