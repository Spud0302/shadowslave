package dev.spud.shadowslave.content.acquisition;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.AcquisitionSource;
import static dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.ProvenanceVisibility;
import static dev.spud.shadowslave.content.acquisition.MemoryEchoAcquisitionContextCatalog.SubjectKind;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryEchoAcquisitionContextCatalogTest {
    @Test
    void waveOneCoversEveryAuthoredSourceForBothSubjectKinds() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        assertEquals(10, catalog.contexts().size());

        Set<String> ids = new HashSet<>();
        for (SubjectKind subjectKind : SubjectKind.values()) {
            Set<AcquisitionSource> covered = EnumSet.noneOf(AcquisitionSource.class);
            catalog.contexts().stream()
                    .filter(context -> context.subjectKind() == subjectKind)
                    .forEach(context -> covered.add(context.source()));
            assertEquals(EnumSet.allOf(AcquisitionSource.class), covered);
        }

        catalog.contexts().forEach(context -> {
            assertTrue(ids.add(context.id()));
            assertEquals("DESIGN", context.evidenceClassification());
            assertTrue(context.evidenceTags().size() >= 3);
            assertTrue(context.acquisitionCue().length() >= 30);
            assertTrue(context.provenanceLine().length() >= 30);
        });
    }

    @Test
    void authoritativeSourceCannotBeChangedByEvidenceOrSeed() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();

        for (long seed = 0; seed < 256; seed++) {
            var memory = catalog.compose(seed, SubjectKind.MEMORY, AcquisitionSource.TRANSFER,
                    Map.of("combat", 1, "creature", 1, "crafted", 1));
            assertEquals(SubjectKind.MEMORY, memory.subjectKind());
            assertEquals(AcquisitionSource.TRANSFER, memory.source());
            assertEquals("memory_transfer_gift", memory.contextId());

            var echo = catalog.compose(seed, SubjectKind.ECHO, AcquisitionSource.ARTIFICIAL_CREATION,
                    Map.of("creature", 1, "spell_reward", 1));
            assertEquals(SubjectKind.ECHO, echo.subjectKind());
            assertEquals(AcquisitionSource.ARTIFICIAL_CREATION, echo.source());
            assertEquals("echo_artificial_forge", echo.contextId());
        }
    }

    @Test
    void compositionIsDeterministicIndependentOfMapOrderAndPositiveMagnitude() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        Map<String, Integer> first = new HashMap<>();
        first.put("gift", 1);
        first.put("transfer", 7);
        first.put("social", 999);

        Map<String, Integer> reordered = new HashMap<>();
        reordered.put("social", 1);
        reordered.put("gift", 55);
        reordered.put("transfer", 2);

        var a = catalog.compose(4815162342L, SubjectKind.MEMORY, AcquisitionSource.TRANSFER, first);
        var b = catalog.compose(4815162342L, SubjectKind.MEMORY, AcquisitionSource.TRANSFER, reordered);

        assertEquals(a, b);
        assertEquals(Set.of("gift", "transfer", "social"), a.matchedEvidence());
    }

    @Test
    void negativeEvidenceFailsClosedAndUnknownEvidenceInventsNothing() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        assertThrows(IllegalArgumentException.class, () -> catalog.compose(
                1L, SubjectKind.MEMORY, AcquisitionSource.SLAIN_CREATURE, Map.of("combat", -1)));

        var resolved = catalog.compose(
                1L, SubjectKind.ECHO, AcquisitionSource.TRANSFER, Map.of("not_an_authored_tag", 1));
        assertTrue(resolved.matchedEvidence().isEmpty());
        assertEquals(AcquisitionSource.TRANSFER, resolved.source());
    }

    @Test
    void matchedEvidenceAlwaysComesFromSelectedAuthoredPrimitive() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        var resolved = catalog.compose(
                99L,
                SubjectKind.MEMORY,
                AcquisitionSource.SLAIN_CREATURE,
                Map.of("combat", 3, "creature", 4, "spell_reward", 5, "irrelevant", 8));
        var source = catalog.find(resolved.contextId());

        assertTrue(source.evidenceTags().containsAll(resolved.matchedEvidence()));
        assertEquals(Set.of("combat", "creature", "spell_reward"), resolved.matchedEvidence());
    }

    @Test
    void hiddenUnknownProvenanceDoesNotGuessCreatureTransferOrCrafting() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();

        for (SubjectKind kind : SubjectKind.values()) {
            var resolved = catalog.compose(7L, kind, AcquisitionSource.UNKNOWN, Map.of("mystery", 1));
            assertEquals(ProvenanceVisibility.HIDDEN, resolved.visibility());
            assertEquals(AcquisitionSource.UNKNOWN, resolved.source());
            assertTrue(resolved.provenanceLine().contains("UNKNOWN"));
        }
    }

    @Test
    void artificialEchoContextExplicitlyRejectsCreatureProvenance() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        var context = catalog.find("echo_artificial_forge");

        assertEquals(SubjectKind.ECHO, context.subjectKind());
        assertEquals(AcquisitionSource.ARTIFICIAL_CREATION, context.source());
        assertEquals(ProvenanceVisibility.KNOWN, context.visibility());
        assertTrue(context.provenanceLine().contains("Do not invent creature Rank, Class or source identity"));
    }

    @Test
    void unknownContextIdsFailClosed() {
        MemoryEchoAcquisitionContextCatalog catalog = MemoryEchoAcquisitionContextCatalog.waveOne();
        assertThrows(IllegalArgumentException.class, () -> catalog.find("missing_context"));
    }
}
