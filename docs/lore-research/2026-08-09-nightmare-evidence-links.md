# Nightmare evidence-object and testimony-link primitives — lore evidence

**Date:** 2026-08-09  
**Scope:** player-facing evidence records, testimony, signals, route traces, damaged objects, and contradictions inside already-resolved Nightmare scenario/actor context  
**Implementation:** `NightmareEvidenceLinkCatalog` / generator version `nightmare-evidence-link-v1`

## Source-policy check

Research followed `docs/LORE-SOURCE-POLICY.md` before implementation. Current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, merged Nightmare content, and active NPC motive/stance/conversation PRs were checked first so this slice would not duplicate their concerns.

The owner-designated NovelFull access layer currently lists through **Chapter 3116 — Princess of the Underworld**. Official WebNovel currently reports **3,131 chapters**. NovelFull is therefore used only as the project-designated chapter-reading access layer, not as publication authority.

No claim in this slice depends on material later than Chapter 743.

## Primary chapter evidence checked

### Chapter 14 — `Child Of Shadows`

Sunny's decisive First-Nightmare action depends on a bounded piece of situational knowledge that Hero does not have: the Mountain King is blind and can be manipulated through sound. Sunny deliberately withholds information and controls what Hero can infer until the reconstructed conflict resolves.

This is useful evidence that **knowledge, uncertainty, selective disclosure, and interpretation of observable circumstances can materially affect Nightmare action**. It does not establish a universal evidence system, testimony mechanic, truth detector, or investigation formula.

### Chapter 737 — `Self-Reflection`

Inside the Second Nightmare, Sunny deliberately uses conversation, misleading questions, information asymmetry, and previously prepared knowledge to manipulate Mordret's understanding of the situation. The encounter is consequential even though the decisive advantage is informational rather than a direct contest of strength.

This supports treating **questions, claims, source uncertainty, and verification pressure** as meaningful player-facing scenario concerns. It does not establish a canonical dialogue tree, lie-detection system, persuasion score, or universal rule for how evidence changes Nightmare state.

### Chapter 743 — `Appraisal`

The Nightmare is already over before appraisal. The Spell recounts a broad history of deeds and consequences, including Sunny's battle of wits with Mordret, before producing a final appraisal.

This supports keeping **local evidence/information play separate from terminal resolution and later appraisal**. The later speculation about divergence from fate is Sunny's theory and is not promoted into a project formula.

## Evidence boundary

### CANON

- Nightmare action can materially depend on situational knowledge, incomplete information, deception, and what another actor does or does not know.
- A consequential conflict inside a Nightmare can involve manipulation and a battle of wits rather than only direct combat.
- Nightmare resolution precedes appraisal; appraisal can narrate broad deeds and consequences, including intellectual/social conflict.

### INFERRED

- Physical records, witness accounts, signals, route traces, damaged objects, and contradictions are useful separable authoring concerns for presenting scenario-local uncertainty.
- A player-facing evidence link can make a claim more testable without becoming authority for hidden truth.
- Scenario identity, actor context, evidence presentation, accepted `ResolutionGraph` events, terminal resolution, and appraisal are useful separate implementation concerns.

### DESIGN

- The six-family taxonomy: `PHYSICAL_RECORD`, `WITNESS_ACCOUNT`, `SIGNAL_TRACE`, `ROUTE_TRACE`, `DAMAGED_OBJECT`, and `CONTRADICTION`.
- All 24 exact primitives, their names, prose, questions, player responses, affinity tags, presentation cues, and anti-overclaim boundaries.
- Positive-evidence tag preference, evidence-magnitude independence, SHA-256 deterministic selection, and generator version `nightmare-evidence-link-v1`.
- Requiring a caller-supplied Java-owned scenario ID, actor-context ID, and explicitly allowed families before presentation composition.

### UNKNOWN

- Any canonical evidence-object or testimony-generation system used by the Nightmare Spell.
- Canonical truthfulness, lie detection, forgery detection, guilt inference, testimony reliability, contradiction resolution, interrogation, persuasion, leverage, reputation, or trust mechanics.
- Any probability/frequency taxonomy for records, witnesses, signals, damaged objects, or contradictory evidence.
- Any rule mapping evidence quantity or quality to accepted scenario events, terminal resolution, appraisal, rewards, progression, role assignment, or fate.
- Whether a given reconstructed historical scenario preserves physical evidence exactly as the original history did, or how much freedom the Spell has in reconstruction details.

### COMPATIBILITY

- Java remains authority for resolved scenario identity, actor/role context, accepted `ResolutionGraph` events, terminal resolution, appraisal inputs, and any future persistent knowledge/evidence state.
- This catalogue may only present an already-authorized local evidence link. Displaying a record, witness account, contradiction, or damaged object cannot itself establish truth, guilt, allegiance, persuasion success, scenario completion, appraisal, or progression.
- Dialogue, NPC AI, books, signs, particles, block entities, models, HUD, audio, and other external/platform adapters may render or execute removable presentation only. They must not become canonical state owners.

## Implementation limits

The selector deliberately ignores evidence magnitude. A positive tag value of `1` and `999` are equivalent for compatible-presentation preference. This prevents existing or future scenario evidence weights from silently becoming an invented canonical truth, difficulty, or appraisal equation.

The seed can select only among primitives whose family was explicitly allowed by the Java caller. It cannot change the supplied scenario ID or actor context and cannot create an accepted scenario event.

## Sources

- NovelFull, Chapter 14 — `Child Of Shadows`: https://novelfull.com/shadow-slave/chapter-14-child-of-shadows.html
- NovelFull, Chapter 737 — `Self-Reflection`: https://novelfull.com/shadow-slave/chapter-737-self-reflection.html
- NovelFull, Chapter 743 — `Appraisal`: https://novelfull.com/shadow-slave/chapter-743-appraisal.html
- NovelFull series index: https://novelfull.com/shadow-slave.html
- Official WebNovel / Guiltythree listing: https://www.webnovel.com/profile/4316552864
