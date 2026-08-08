package dev.spud.shadowslave.content.presentation;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog.EnchantmentProfile;
import dev.spud.shadowslave.content.memory.MemoryContentCatalog.MemoryProfile;
import dev.spud.shadowslave.echo.content.EchoContentCatalog.EchoProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Pure Java player-facing presentation composition for already-resolved Memory and Echo content.
 *
 * <p>This class does not award, mutate, persist, summon or execute Memories/Echoes. It turns
 * Java-owned catalogue identities into immutable display cards that removable presentation
 * adapters may render. Exact copy, field order and formatting are project DESIGN.</p>
 */
public final class MemoryEchoPresentationComposer {
    private static final String PROVENANCE = "design/memory-echo-presentation-wave1";

    private MemoryEchoPresentationComposer() {
    }

    public enum SubjectKind {
        MEMORY,
        ECHO
    }

    public enum EventKind {
        ACQUIRED,
        INSPECTED
    }

    public record PresentationLine(String label, String value) {
        public PresentationLine {
            label = requireText(label, "label");
            value = requireText(value, "value");
        }
    }

    public record PresentationCard(
            String subjectId,
            SubjectKind subjectKind,
            EventKind eventKind,
            String title,
            List<PresentationLine> lines,
            String provenance
    ) {
        public PresentationCard {
            subjectId = requireText(subjectId, "subjectId");
            subjectKind = Objects.requireNonNull(subjectKind, "subjectKind");
            eventKind = Objects.requireNonNull(eventKind, "eventKind");
            title = requireText(title, "title");
            lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
            if (lines.isEmpty()) {
                throw new IllegalArgumentException("presentation card must contain at least one line");
            }
            provenance = requireText(provenance, "provenance");
        }
    }

    public static PresentationCard memoryAcquired(MemoryProfile memory) {
        MemoryProfile checked = Objects.requireNonNull(memory, "memory");
        return card(
                checked.id().toString(),
                SubjectKind.MEMORY,
                EventKind.ACQUIRED,
                "Memory acquired: " + checked.formalName(),
                List.of(
                        line("Rank", display(checked.rank().name())),
                        line("Tier", Integer.toString(checked.tier())),
                        line("Type", display(checked.type().name()))
                )
        );
    }

    public static PresentationCard memoryInspection(MemoryProfile memory) {
        MemoryProfile checked = Objects.requireNonNull(memory, "memory");
        ArrayList<PresentationLine> lines = new ArrayList<>();
        lines.add(line("Rank", display(checked.rank().name())));
        lines.add(line("Tier", Integer.toString(checked.tier())));
        lines.add(line("Type", display(checked.type().name())));
        lines.add(line("Themes", joined(checked.themeTags())));

        List<EnchantmentProfile> enchantments = new ArrayList<>(checked.enchantments());
        enchantments.sort(Comparator.comparing(enchantment -> enchantment.id().toString()));
        for (EnchantmentProfile enchantment : enchantments) {
            lines.add(line(
                    "Enchantment · " + display(lastPathSegment(enchantment.id().getPath())),
                    display(enchantment.role().name()) + " — " + enchantment.gameplayHook()
            ));
        }

        return card(
                checked.id().toString(),
                SubjectKind.MEMORY,
                EventKind.INSPECTED,
                checked.formalName(),
                lines
        );
    }

    public static PresentationCard echoAcquired(EchoProfile echo) {
        EchoProfile checked = Objects.requireNonNull(echo, "echo");
        return card(
                echoSubjectId(checked),
                SubjectKind.ECHO,
                EventKind.ACQUIRED,
                "Echo acquired: " + checked.displayName(),
                List.of(
                        line("Origin", display(checked.originKind().name())),
                        line("Roles", joinedEnums(checked.roles())),
                        line("Field Use", checked.tacticalUse())
                )
        );
    }

    public static PresentationCard echoInspection(EchoProfile echo) {
        EchoProfile checked = Objects.requireNonNull(echo, "echo");
        ArrayList<PresentationLine> lines = new ArrayList<>();
        lines.add(line("Origin", display(checked.originKind().name())));

        checked.sourceCreatureId().ifPresent(source -> lines.add(line("Creature Source", display(source))));
        checked.sourceRank().ifPresent(rank -> lines.add(line("Source Rank", display(rank.name()))));
        checked.sourceClass().ifPresent(creatureClass -> lines.add(line("Source Class", display(creatureClass.name()))));

        lines.add(line("Roles", joinedEnums(checked.roles())));
        lines.add(line("Commands", joinedEnums(checked.commandModes())));
        lines.add(line("Utility", joined(checked.utilityTags())));
        lines.add(line("Field Use", checked.tacticalUse()));
        lines.add(line("Summon Cue", checked.presentationCue()));

        return card(
                echoSubjectId(checked),
                SubjectKind.ECHO,
                EventKind.INSPECTED,
                checked.displayName(),
                lines
        );
    }

    private static PresentationCard card(
            String subjectId,
            SubjectKind subjectKind,
            EventKind eventKind,
            String title,
            List<PresentationLine> lines
    ) {
        return new PresentationCard(subjectId, subjectKind, eventKind, title, lines, PROVENANCE);
    }

    private static PresentationLine line(String label, String value) {
        return new PresentationLine(label, value);
    }

    private static String echoSubjectId(EchoProfile echo) {
        return "shadowslave:echo/" + echo.id();
    }

    private static String joined(Collection<String> values) {
        List<String> ordered = values.stream()
                .map(MemoryEchoPresentationComposer::requireDisplayToken)
                .sorted()
                .map(MemoryEchoPresentationComposer::display)
                .toList();
        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("display collection cannot be empty");
        }
        return String.join(", ", ordered);
    }

    private static String joinedEnums(Collection<? extends Enum<?>> values) {
        List<String> ordered = values.stream()
                .map(Enum::name)
                .sorted()
                .map(MemoryEchoPresentationComposer::display)
                .toList();
        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("enum display collection cannot be empty");
        }
        return String.join(", ", ordered);
    }

    private static String lastPathSegment(String path) {
        int separator = path.lastIndexOf('/');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private static String display(String raw) {
        String normalized = requireDisplayToken(raw).toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder result = new StringBuilder(normalized.length());
        boolean capitalize = true;
        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            if (capitalize && Character.isLetter(character)) {
                result.append(Character.toUpperCase(character));
                capitalize = false;
            } else {
                result.append(character);
            }
            if (character == ' ' || character == '-') {
                capitalize = true;
            }
        }
        return result.toString();
    }

    private static String requireDisplayToken(String value) {
        return requireText(value, "display token");
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
