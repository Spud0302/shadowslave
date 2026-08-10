# Runtime Spell/appraisal presentation integration — 2026-08-10

## Scope

This slice connects the already-merged `SpellPresentationCatalog` to the combined alpha gameplay line. It does not create another presentation catalogue and does not calculate appraisal, rewards, identity, Nightmare completion, or progression.

The concrete defect was presentation drift: the alpha runtime already committed generated Aspect, Flaw and Attribute identity plus Ash Compass Memory and Ash Burrower Echo state, while the completion path still displayed the obsolete fixed `Last Light / Cold Ash` compatibility result.

## Primary evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked primary chapter material through the owner-designated chapter access layer, with official WebNovel chapter identity/publication cross-checks where practical.

- **Chapter 15 — Shadow Slave:** after the First Nightmare ends, the Spell performs appraisal/reward presentation; a Memory acquisition is directly communicated and the trial is then described/evaluated before the final result. This supports distinct player-facing acquisition and appraisal presentation while leaving exact ordering non-universal.
- **Chapter 743 — Appraisal:** later Nightmare material again makes the boundary explicit: the Nightmare is already over before appraisal begins, and appraisal can recount deeds before a final verdict. This supports consuming already-resolved Nightmare state rather than allowing presentation to decide completion.
- The current publication/access indexes were also checked for freshness. No implementation rule in this slice depends on material later than the chapters above.

No novel text is reproduced here.

## Classification

### CANON

- Nightmare resolution and subsequent appraisal are distinct stages.
- The Spell can present an appraisal/deed account and final verdict after the Nightmare has ended.
- Memory acquisition can be directly communicated to the recipient.
- Existing adjacent research already establishes player-facing Aspect, Flaw, Attribute and Echo presentation events.

### INFERRED

- Runtime presentation should consume the exact scenario, historical role, terminal resolution and rewards already committed by Java rather than rerun generators or derive identity from chat/UI state.
- An obscured Attribute should not be rendered through an `ATTRIBUTE_REVEALED` event until authoritative state says it is revealed.

### DESIGN

- The exact sequence assembled by `FirstNightmareSpellPresentation`.
- The summary sentence describing scenario + historical role.
- Treating the named terminal resolution as one appraisal-deed line.
- The `Nightmare conquered` verdict wording.
- Chat colors used to distinguish existing `VOICE` and `RUNES` presentation surfaces.
- The explicit fallback sentence for an obscured Attribute.
- Keeping summon-command hints after acquisition presentation for the alpha runtime.

### UNKNOWN

- A universal canonical appraisal message order, timing, audio treatment, rune layout or animation.
- A universal rule for which deeds are narrated or how a final appraisal is worded.
- Whether every First-Nightmare reward/revelation follows an identical presentation sequence.
- Mature presentation for obscured Attributes and exceptional Spell-disconnected awakening paths.

### COMPATIBILITY

- `PreviewAppraisalService` commits canonical project state first and returns the exact committed reward objects.
- `FirstNightmareSpellPresentation` and `SpellPresentationCatalog` only render supplied state; they do not mutate progression or ownership.
- Minecraft chat remains a replaceable execution/presentation surface. Future HUD, narrator, sound or rune UI adapters must consume the same Java-owned values rather than becoming authority.
- Successful-completion crash atomicity remains tracked separately by issue #34 and is not claimed solved here.
