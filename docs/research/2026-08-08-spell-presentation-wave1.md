# Spell presentation content — wave 1

**Status:** bounded player-facing content/design slice  
**Scope:** reusable presentation primitives for Nightmare appraisal, Memory/Echo acquisition, identity revelation/evolution, and Memory/Echo inspection  
**Architecture:** Java owns canonical state and stable presentation content; external UI/audio/chat adapters are removable consumers only

## Repository context checked

Before authoring this slice, current `main`, open pull requests/issues, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, `docs/JAVA-LORE-ALIGNMENT.md`, the Memory/Echo content catalogues, PR #89's Attribute work, and the roadmap/research material from PR #36 were reviewed.

The active large correctness stack (#86 and its predecessors) remains deliberately untouched. PR #89 already owns the Attribute catalogue/generation slice, so this branch does not duplicate Attribute definitions or persistence.

## Primary lore evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md`. The decisive chapter material was rechecked through the owner-designated full-chapter access layer.

- **Chapter 15:** the First Nightmare ends before the Spell finishes appraisal; Memory acquisition is communicated directly; the Spell then presents the First-Nightmare appraisal/reward sequence.
- **Chapter 74:** a transferred Memory produces a direct receipt message, after which rune presentation exposes Memory name, Rank and Type; the surrounding chapter also establishes Tier as a separate Memory concept.
- **Chapter 85:** rune presentation exposes an Attribute by name/description, reports that an Attribute is ready to evolve, accepts a choice, and then presents the evolved Attribute identity.
- **Chapter 103:** the Spell directly reports acquisition of an Echo after a Nightmare Creature kill, supporting Echo acquisition as its own presentation event.
- **Chapter 743:** later Nightmare appraisal begins after the Nightmare ends, narrates the challenger's deeds, then gives a final appraisal verdict. The chapter also shows that the Spell's narrative appraisal can be extensive rather than a single numeric score.
- **Chapter 744:** Sunny theorizes that appraisal relates to divergence from the expected flow of fate; this remains a character theory rather than a universal generation formula.
- **Chapter 1581:** much later material again describes the intended sequence as Nightmare collapse/expulsion followed by appraisal and rank progression in the void, while also demonstrating an exceptional case where that normal presentation does not occur.
- **Chapter 2029:** later clarification confirms that naturally awakened people are not automatically presented an Aspect/Flaw by the Spell in the same way, reinforcing that these presentation events must be driven by already-resolved canonical state rather than assumed for every awakening path.

No chapter text is reproduced here beyond short terminology labels.

## Evidence classification

### CANON

- The Nightmare Spell communicates player-facing information through an audible/mental voice and rune-like status presentation.
- A Nightmare ending and appraisal are distinct events; appraisal can include a narrative account of deeds followed by a final verdict.
- Memory and Echo acquisition can be announced as distinct events.
- Memory inspection exposes stable identity information such as name, Rank, Tier and Type where known.
- Attributes can be presented by name and can be shown as ready to evolve / evolved.
- Aspect, Flaw and Attribute identity can be represented through rune/status presentation when those identities are actually known to the character/Spell path.

### INFERRED

- A reusable Java presentation contract should consume already-resolved canonical identity/appraisal values instead of owning or recalculating them.
- Voice-like announcements and rune-like inspection are useful separate presentation surfaces because the novel repeatedly distinguishes those modes.
- Appraisal presentation should accept an ordered narrative/deed list from the appraisal system rather than flattening appraisal into one score.

### DESIGN

Everything in `SpellPresentationCatalog` beyond the broad canon constraints is project-authored design, including:

- the exact English copy used by wave-one templates;
- the `EventKind` taxonomy;
- `VOICE` versus `RUNES` as adapter-facing enum names;
- the exact ordering produced by `appraisal(...)`;
- strict placeholder validation;
- Memory/Echo inspection formatting;
- stable `shadowslave:spell_presentation/*` IDs;
- use of presentation provenance strings.

The template wording intentionally paraphrases the novel instead of trying to reproduce canonical prose verbatim.

### UNKNOWN

The project does **not** claim to know:

- a universal canonical UI layout, font, animation, sound, timing or duration;
- a universal appraisal grading scale or scoring formula;
- a canonical formula deciding which deeds are narrated or how they are weighted;
- universal acquisition-message wording for every era/awakening path;
- whether every Attribute/Aspect/Flaw revelation uses exactly the same presentation sequence;
- how exceptional Spell-disconnected cases should be rendered;
- whether Memory/Echo inspection always exposes the same fields in every circumstance.

### COMPATIBILITY

- Java remains the canonical state owner.
- `SpellPresentationCatalog` only turns supplied values into immutable presentation lines.
- It does not mutate Soul, Nightmare, Aspect, Flaw, Attribute, Memory or Echo state.
- A NeoForge overlay, chat renderer, narrator, sound layer, shader, animation mod or modpack integration may consume the stable presentation lines and can be removed without changing canonical saves or progression.
- The catalogue is independent of PR #89 and the large correctness stack, so it can be reviewed/merged/rebased separately.

## Player-facing wave-one content

Wave one adds 14 reusable event templates covering:

1. Nightmare resolved;
2. appraisal begin;
3. appraisal summary;
4. appraisal deed narration;
5. final appraisal verdict;
6. Memory received;
7. Echo received;
8. Attribute revealed;
9. Attribute evolution ready;
10. Attribute evolved;
11. Aspect revealed;
12. Flaw revealed;
13. Memory inspection;
14. Echo inspection.

`appraisal(...)` provides a deterministic sequence builder only. It consumes a summary, ordered deed list and verdict already produced by another Java-owned system. It deliberately contains no appraisal formula.

## Validation

`SpellPresentationCatalogTest` checks:

- every declared event kind has exactly one stable wave-one template;
- template IDs are unique and DESIGN provenance is explicit;
- voice and rune surfaces remain distinct;
- required placeholders are strict and fail closed on missing/extra/blank fields;
- Memory inspection preserves supplied name/rank/tier/type values;
- appraisal presentation preserves the expected end -> begin -> narrative/deeds -> verdict order without calculating the verdict;
- acquisition/revelation events consume supplied resolved names rather than defining canonical identity.

## Integration boundary

This slice intentionally does not wire runtime chat, HUD, narrator audio, player persistence or reward mutation. The next integration step can map existing Java-owned appraisal and stable content IDs into these presentation lines, while keeping any actual NeoForge/client rendering adapter removable.

A later content slice can add **NPC/faction/story modules for Dream Realm frontier regions**, using the already-merged region, creature, Memory and Echo catalogues without depending on the correctness stack.
