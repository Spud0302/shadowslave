# Nightmare NPC motive / relationship primitives — evidence note

Date: 2026-08-09

## Scope

This slice adds authored player-facing NPC motive primitives for already-resolved Nightmare historical roles. It does **not** select a Nightmare, role, NPC identity, allegiance, truth value, persuasion result, scenario resolution, appraisal result, reward, or persistent relationship state.

Research followed `docs/LORE-SOURCE-POLICY.md`, current `main`, open PRs/issues, `docs/NIGHTMARE-SEED-ROADMAP.md`, current scenario content, and active role/pressure work before implementation. No active branch already owned this NPC-motive layer.

## Primary and later chapter checks

- **Chapter 2 — Slave Caravan:** Sunny's First Nightmare places him in a substantive reconstructed body and social situation. Physical condition, social status, nearby people, existing tensions and constraints are part of the scenario rather than cosmetic staging.
- **Chapter 737 — Self-Reflection:** inside a later Nightmare, allies can remain uncertain or potentially adversarial; relationships, intentions and strategic trust are not reducible to a fixed ally/enemy flag.
- **Chapter 743 — Appraisal:** after the Nightmare is over, the Spell recounts broad deeds and consequences, including social/intellectual conflict, before appraisal. This supports keeping player/NPC choices and consequences richer than combat while not supplying a motive or appraisal formula.
- **Chapter 2029 — Fortune Telling:** much later clarification explicitly ties important First-Nightmare identity traits to the historical body Sunny inhabited, reinforcing that historical identity can be substantive rather than presentation-only.

Freshness check: the owner-designated NovelFull access layer currently lists through **Chapter 3116 — Princess of the Underworld**. Official WebNovel currently reports **3,131 chapters** (with public catalogue snapshots around Chapter 3130/3131). NovelFull is therefore used as the project-designated reading access layer, not current-publication authority. No claim in this slice depends on material later than Chapter 2029.

## Evidence classification

### CANON

- A Nightmare can place a challenger into a substantive reconstructed historical body/role and social situation.
- Other people and their relationships/intentions can materially complicate a Nightmare; apparent alliance does not guarantee stable trust or identical goals.
- Nightmare appraisal is separate from the ended trial and can recount broad deeds and consequences, not only combat.
- Later text confirms that inherited First-Nightmare body identity can contribute substantive traits.

### INFERRED

- Motive, relationship pressure, dialogue opportunity and observable behavior are useful separable authoring concerns for NPCs inside an already-resolved reconstructed scenario.
- A player-facing motive cue should express uncertainty and leverage without becoming authoritative truth about what an NPC will do.
- Historical-role identity and NPC motive presentation should remain separate so a scenario can author several coherent people around one role without turning role selection into a personality formula.

### DESIGN

- The seven motive families: `DUTY`, `FEAR`, `CONCEALMENT`, `RIVALRY`, `OBLIGATION`, `DESPERATION`, and `CONFLICTING_LOYALTY`.
- All 28 exact motive primitives, titles, motive reads, dialogue hooks, behavior hooks, affinity tags, presentation cues, and anti-overclaim boundaries.
- Deterministic compatible-only composition under generator version `nightmare-npc-motive-v1`.
- Positive evidence tags may prefer compatible authored motives; evidence magnitude is deliberately ignored.
- The caller must supply an already-resolved historical role ID and allowed motive families.

### UNKNOWN

- The Nightmare Spell's actual NPC/personality/motive generation principle, if any.
- Any universal motive taxonomy, motive frequency, probability, weighting, or relationship-generation formula.
- Whether historical people are reconstructed with exact original psychology, partial reconstruction, scenario-relevant behavior, or another mechanism.
- NPC truthfulness, allegiance changes, persuasion thresholds, trust/reputation math, social AI, emotional state machines, loyalty switching, betrayal probability, or dialogue success formulas.
- Any relationship between NPC motive, challenger role, fate, difficulty, appraisal, Rank/Class, rewards, or progression.

### COMPATIBILITY

Java remains the only authority for resolved Nightmare/scenario/role identity, accepted scenario events, terminal resolution, appraisal inputs, rewards/progression, and any persistent relationship state that may later be added. Dialogue, NPC AI, animation, voice, HUD, structures, schedules and other external/platform adapters may consume a resolved motive primitive but cannot use presentation text or behavior hooks as canonical state authority.

## Implementation boundary

`NightmareNpcMotiveCatalog.compose(...)` accepts:

1. deterministic presentation seed;
2. already-resolved historical role ID;
3. scenario-author-approved motive families;
4. non-negative local evidence tags.

It may choose only among primitives in the supplied families. Positive evidence magnitude `1` and `999` are equivalent. The seed cannot reroll the role or authorize a scenario event. Presentation cues are descriptive possibilities, not proof that an NPC is lying, loyal, guilty, persuaded, allied, hostile, or destined to act in a particular way.

No canonical generation, personality, persuasion, allegiance, relationship, appraisal, or reward formula is claimed.
