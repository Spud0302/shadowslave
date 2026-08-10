package dev.spud.shadowslave.content.spell;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Java-owned player-facing presentation primitives for Spell-like messages.
 *
 * <p>The novel establishes that the Nightmare Spell communicates through a voice
 * and rune-like status presentation, including Nightmare appraisal, Memory/Echo
 * acquisition and identity information. The exact copy, grouping, timing and UI
 * choreography in this catalogue are project DESIGN, not a claimed canonical
 * notification protocol.</p>
 *
 * <p>This type is deliberately presentation-only. It never mutates Soul,
 * Nightmare, Memory, Echo, Aspect, Flaw or Attribute state. External adapters may
 * render {@link PresentationLine} values in chat, overlays, narration or another
 * removable surface while Java remains the canonical state owner.</p>
 */
public record SpellPresentationCatalog(List<Template> templates) {
    private static final String NAMESPACE = "shadowslave";

    public SpellPresentationCatalog {
        ArrayList<Template> canonical = new ArrayList<>(Objects.requireNonNull(templates, "templates"));
        canonical.sort(Comparator.comparing(template -> template.id().toString()));
        HashSet<ResourceLocation> ids = new HashSet<>();
        HashSet<EventKind> kinds = new HashSet<>();
        for (Template template : canonical) {
            Objects.requireNonNull(template, "template");
            if (!ids.add(template.id())) {
                throw new IllegalArgumentException("Duplicate Spell presentation template id: " + template.id());
            }
            if (!kinds.add(template.kind())) {
                throw new IllegalArgumentException("Duplicate Spell presentation event kind: " + template.kind());
            }
        }
        templates = List.copyOf(canonical);
    }

    public static SpellPresentationCatalog waveOne() {
        return new SpellPresentationCatalog(List.of(
                template("nightmare_resolved", EventKind.NIGHTMARE_RESOLVED, Surface.VOICE,
                        "The Nightmare has ended.", Set.of()),
                template("appraisal_begin", EventKind.APPRAISAL_BEGIN, Surface.VOICE,
                        "Appraisal begins...", Set.of()),
                template("appraisal_summary", EventKind.APPRAISAL_SUMMARY, Surface.VOICE,
                        "{summary}", Set.of("summary")),
                template("appraisal_deed", EventKind.APPRAISAL_DEED, Surface.VOICE,
                        "{deed}", Set.of("deed")),
                template("appraisal_verdict", EventKind.APPRAISAL_VERDICT, Surface.VOICE,
                        "Final appraisal: {appraisal}.", Set.of("appraisal")),
                template("memory_received", EventKind.MEMORY_RECEIVED, Surface.VOICE,
                        "Memory received: {name}.", Set.of("name")),
                template("echo_received", EventKind.ECHO_RECEIVED, Surface.VOICE,
                        "Echo received: {name}.", Set.of("name")),
                template("attribute_revealed", EventKind.ATTRIBUTE_REVEALED, Surface.RUNES,
                        "Attribute: {name}.", Set.of("name")),
                template("attribute_evolution_ready", EventKind.ATTRIBUTE_EVOLUTION_READY, Surface.RUNES,
                        "{name} is ready to evolve.", Set.of("name")),
                template("attribute_evolved", EventKind.ATTRIBUTE_EVOLVED, Surface.RUNES,
                        "Attribute evolved: {name}.", Set.of("name")),
                template("aspect_revealed", EventKind.ASPECT_REVEALED, Surface.RUNES,
                        "Aspect: {name}.", Set.of("name")),
                template("flaw_revealed", EventKind.FLAW_REVEALED, Surface.RUNES,
                        "Flaw: {name}.", Set.of("name")),
                template("memory_inspection", EventKind.MEMORY_INSPECTION, Surface.RUNES,
                        "Memory: {name} — Rank {rank}, Tier {tier}, Type {type}.", Set.of("name", "rank", "tier", "type")),
                template("echo_inspection", EventKind.ECHO_INSPECTION, Surface.RUNES,
                        "Echo: {name} — {provenance}.", Set.of("name", "provenance"))
        ));
    }

    public Template template(EventKind kind) {
        Objects.requireNonNull(kind, "kind");
        return templates.stream()
                .filter(template -> template.kind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Spell presentation template for " + kind));
    }

    public PresentationLine line(EventKind kind, Map<String, String> values) {
        return template(kind).render(values);
    }

    /**
     * Builds a deterministic player-facing appraisal sequence from already-owned
     * appraisal content. This method does not calculate the appraisal itself.
     */
    public List<PresentationLine> appraisal(String summary, List<String> deeds, String verdict) {
        String checkedSummary = requireText(summary, "summary");
        String checkedVerdict = requireText(verdict, "verdict");
        List<String> checkedDeeds = List.copyOf(Objects.requireNonNull(deeds, "deeds"));

        ArrayList<PresentationLine> lines = new ArrayList<>();
        lines.add(line(EventKind.NIGHTMARE_RESOLVED, Map.of()));
        lines.add(line(EventKind.APPRAISAL_BEGIN, Map.of()));
        lines.add(line(EventKind.APPRAISAL_SUMMARY, Map.of("summary", checkedSummary)));
        for (String deed : checkedDeeds) {
            lines.add(line(EventKind.APPRAISAL_DEED, Map.of("deed", requireText(deed, "deed"))));
        }
        lines.add(line(EventKind.APPRAISAL_VERDICT, Map.of("appraisal", checkedVerdict)));
        return List.copyOf(lines);
    }

    public enum EventKind {
        NIGHTMARE_RESOLVED,
        APPRAISAL_BEGIN,
        APPRAISAL_SUMMARY,
        APPRAISAL_DEED,
        APPRAISAL_VERDICT,
        MEMORY_RECEIVED,
        ECHO_RECEIVED,
        ATTRIBUTE_REVEALED,
        ATTRIBUTE_EVOLUTION_READY,
        ATTRIBUTE_EVOLVED,
        ASPECT_REVEALED,
        FLAW_REVEALED,
        MEMORY_INSPECTION,
        ECHO_INSPECTION
    }

    public enum Surface {
        VOICE,
        RUNES
    }

    public record Template(
            ResourceLocation id,
            EventKind kind,
            Surface surface,
            String pattern,
            Set<String> requiredFields,
            String provenance
    ) {
        public Template {
            id = Objects.requireNonNull(id, "id");
            kind = Objects.requireNonNull(kind, "kind");
            surface = Objects.requireNonNull(surface, "surface");
            pattern = requireText(pattern, "pattern");
            requiredFields = normalizedFields(requiredFields);
            provenance = requireText(provenance, "provenance");
            for (String field : requiredFields) {
                if (!pattern.contains("{" + field + "}")) {
                    throw new IllegalArgumentException("Template " + id + " does not contain required field {" + field + "}");
                }
            }
        }

        public PresentationLine render(Map<String, String> suppliedValues) {
            Map<String, String> supplied = Map.copyOf(Objects.requireNonNull(suppliedValues, "suppliedValues"));
            if (!supplied.keySet().equals(requiredFields)) {
                throw new IllegalArgumentException(
                        "Template " + id + " requires exactly " + requiredFields + " but received " + supplied.keySet()
                );
            }

            String rendered = pattern;
            LinkedHashMap<String, String> canonicalValues = new LinkedHashMap<>();
            requiredFields.stream().sorted().forEach(field -> {
                canonicalValues.put(field, requireText(supplied.get(field), field));
            });
            for (Map.Entry<String, String> entry : canonicalValues.entrySet()) {
                rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return new PresentationLine(id, kind, surface, rendered, Map.copyOf(canonicalValues));
        }
    }

    public record PresentationLine(
            ResourceLocation templateId,
            EventKind kind,
            Surface surface,
            String text,
            Map<String, String> fields
    ) {
        public PresentationLine {
            templateId = Objects.requireNonNull(templateId, "templateId");
            kind = Objects.requireNonNull(kind, "kind");
            surface = Objects.requireNonNull(surface, "surface");
            text = requireText(text, "text");
            fields = Map.copyOf(Objects.requireNonNull(fields, "fields"));
        }
    }

    private static Template template(String path, EventKind kind, Surface surface, String pattern, Set<String> fields) {
        return new Template(
                id("spell_presentation/" + path),
                kind,
                surface,
                pattern,
                fields,
                "design/spell-presentation-wave1/" + path
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static Set<String> normalizedFields(Set<String> source) {
        HashSet<String> normalized = new HashSet<>();
        for (String value : Objects.requireNonNull(source, "requiredFields")) {
            String checked = requireText(value, "requiredField");
            if (!checked.matches("[a-z][a-z0-9_]*")) {
                throw new IllegalArgumentException("Invalid template field: " + checked);
            }
            if (!normalized.add(checked)) {
                throw new IllegalArgumentException("Duplicate template field: " + checked);
            }
        }
        return Set.copyOf(normalized);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
