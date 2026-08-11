# Ash Burrower ambush combat evidence — 2026-08-11

## Scope

This note supports one bounded physical executor change for the already-authored `ash_burrower` Nightmare Creature. It does not claim that an Ash Burrower exists in the novel.

## Primary evidence checked

- **Chapter 370 — `Exploration Report`**: Sunny treats geography/environment/landmarks and individual Nightmare Creature powers, behavior, and weaknesses as practically valuable exploration knowledge.
- **Chapter 266 — `The Devil You Know`**: Sunny's preparation against the Black Knight depends on learning its strengths, weaknesses, behavior patterns, and supernatural power before fighting it.

Official WebNovel publication was cross-checked for both chapter identities. The owner-designated NovelFull access layer remains the working full-chapter research source under `docs/LORE-SOURCE-POLICY.md`.

## Classification

- **CANON:** Nightmare Creatures can have materially distinct powers, behavior, patterns, and weaknesses; learning those properties can materially improve survival/combat decisions.
- **INFERRED:** an already-authored creature pressure such as `AMBUSH` should produce observable, learnable physical counterplay instead of collapsing into instantaneous generic melee.
- **DESIGN:** Ash Burrower identity; vibration-to-ambush combat loop; 2.8-block commit range; 10-tick wind-up; ash/gravel telegraph; 3.1-block connect range; 9-tick recovery after connection; 22-tick recovery after evasion; 34-tick cooldown; movement suppression during commitment/recovery.
- **UNKNOWN:** canonical Ash Burrower existence/anatomy/Rank/Class; literal burrowing mechanism; exact sensory mechanism; whether any canonical creature exposes these timings; whether missed attacks canonically cause recovery; final audio/VFX/animation.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains authority for creature identity/descriptors. SmartBrainLib remains replaceable sensing/path scheduling; NeoForge movement/damage/timing and GeckoLib animation remain replaceable execution/presentation. No reward, appraisal, progression, Aspect, Memory, Echo, or Nightmare-resolution authority is moved into the entity.

## Combat design consequence

The intended physical rhythm is:

`movement/vibration reveals player -> Burrower pursues -> close-range ambush commits -> player escapes or is hit -> missed commitment creates a longer punish window -> pursuit can resume`

This is intentionally different from Chainback displacement and Drowned Listener listening counterplay. It advances the shared combat principle that serious enemies should expose readable commitments and creature-specific answers rather than merely trade vanilla melee hits.
