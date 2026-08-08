# Nightmare deed/evidence catalogue — wave 1

## Purpose

This slice adds reusable player-facing deed language for Nightmare appraisal presentation without inventing a canonical appraisal-scoring formula.

The Java core may record scenario-specific evidence and later ask this catalogue for authored narrative deed primitives. The catalogue does not decide whether a Nightmare ended, whether a challenger completed it, what appraisal verdict they receive, what progression is awarded, or how the Nightmare Spell itself evaluates fate.

## Primary lore checked

Research follows `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 15 — Shadow Slave:** after the First Nightmare has ended, the Spell appraises Sunny by narrating what he did during the trial, including enemies defeated, the temple/altar outcome, and the Shadow God's blessing, before issuing a final appraisal. This supports a distinction between recorded deeds and the later verdict.
- **Chapter 743 — Appraisal:** after the Second Nightmare ends, the Spell recounts a broad history of Sunny's actions before the final verdict. The narration includes escape, sorcery, endurance, allies, war, deaths, freeing Hope, combat achievements, and a battle of wits. This is strong evidence that appraisal-relevant narration is not limited to creature kills.
- **Chapter 744 — Ascension:** Sunny theorizes that appraisal depends on divergence from fate rather than raw magnitude of feats. This remains a character theory, not a canonical scoring formula.
- **Chapter 1581 — Shadows and Dust:** later material again separates Nightmare collapse from the appraisal/rank-up sequence for ordinary surviving challengers, while Sunny's exceptional Spell-disconnected case lacks that appraisal.

## Evidence classification

**CANON**

- Nightmare appraisal occurs after the Nightmare/trial has ended.
- The Spell can narrate a challenger's deeds before giving a final appraisal.
- The narrated history can include more than kills: survival, escape, learned sorcery, allies, large-scale consequences, and contests of wit all appear in the Second Nightmare appraisal.

**INFERRED**

- A reusable Java content layer may safely separate `recorded scenario evidence` from `authored deed presentation`, because canon visibly separates the actions being recounted from the final verdict.
- Non-combat scenario evidence such as discovery, warning, preservation, negotiation, deception, sacrifice, adaptation, and counterplay is appropriate raw material for authored appraisal narration where a scenario actually records those facts.

**DESIGN**

- The 16 deed names and seven deed families in `NightmareDeedContentCatalog`.
- The evidence-tag vocabulary and which tags are attached to each deed.
- Deterministic seeded tie-breaking between equally compatible deed primitives.
- Ranking candidate presentation primitives by count of matching positive evidence tags.
- Returning a bounded number of narrative deed entries.

**UNKNOWN**

- The Nightmare Spell's universal appraisal-scoring formula, if one exists.
- Whether the Spell internally classifies deeds into reusable categories.
- How it weights kills, discoveries, sacrifices, relationships, deviation from fate, role expectations, difficulty, or historical consequences.
- Whether all Nightmares receive the same style or amount of deed narration.
- Whether Sunny's fate-divergence theory in Chapters 743–744 is completely correct.

**COMPATIBILITY**

- Canonical completion, challenger outcome, appraisal verdict, progression, and persistent Nightmare state remain Java-core responsibilities.
- The catalogue consumes already-recorded evidence; it cannot mutate `NightmareInstance`, Soul state, identity, rewards, or completion state.
- External HUD/chat/narrator/audio mods may render composed deed lines, but removing them must not erase or alter authoritative evidence or appraisal state.

## Composition boundary

`compose(seed, evidence, limit)` intentionally ignores evidence magnitude except for whether a tag is positive. A weight of `1` and a weight of `999` therefore produce the same eligible narrative primitives for the same seed.

That constraint is deliberate: existing scenario definitions already carry DESIGN evidence weights for local content purposes, but this wave must not accidentally turn those numbers into a claimed appraisal score.

Unknown evidence tags produce no invented deed. Negative evidence fails closed. The returned value records the source deed ID, matched evidence tags, generator version, and seed so a later persistent appraisal/presentation record can save resolved identity rather than rerolling it.

## Current scenario compatibility

The merged **Drowned Bell** resolution evidence already maps naturally into this catalogue:

- `duty`, `warning`, `preservation`, `sacrifice`, `resolve` can narrate warning/preservation/sacrifice/endurance deeds;
- `guidance`, `movement`, `social`, `adaptation` can narrate rescue/adaptation/social deeds;
- `precision`, `retaliation`, `warning`, `resolve` can narrate direct conflict/counterplay choices.

**The Last Signal** can adopt the same primitives when its richer resolution evidence is integrated.

Open PR #102, **The Hollow Treaty**, was reviewed for overlap but is intentionally not imported because this branch is based directly on `main`. Its social/deception/evidence endings are covered by generic primitives such as `negotiation`, `deception`, `exposure`, `destruction`, `discovery`, and `investigation`, allowing a clean later rebase/integration after #102 merges.
