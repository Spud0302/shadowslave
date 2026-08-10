# Ash Burrower vibration/ambush execution — evidence boundary

**Scope:** physical Minecraft execution only for the already-authored `ash_burrower` Nightmare Creature profile.

## Primary material checked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked primary chapter text plus official publication identity.

- **Chapter 370 — Exploration Report:** Dream Realm field knowledge explicitly treats individual Nightmare Creatures as having learnable powers, behavior, and weaknesses. This supports preserving creature-specific behavioral/counterplay identity instead of reducing every executor to identical vanilla hostile AI.
- **Chapter 183 — Learning New Tricks:** later field-hunting material again treats observing a creature's behavior and weaknesses before engagement as meaningful survival practice. This reinforces the broad behavioral/counterplay boundary; it does not establish any Ash Burrower-specific sense or threshold.
- Official WebNovel was cross-checked for Chapter 370 identity/publication.

## Classification

- **CANON:** individual Nightmare Creatures can have distinct powers, behavior, and exploitable weaknesses; observing those differences can matter to survival.
- **INFERRED:** a Minecraft executor should expose meaningful creature-specific counterplay when the Java-owned content profile already authorizes such descriptors.
- **DESIGN:** Ash Burrower itself; `VIBRATION`, `SCENT`, `BURROW`, `AMBUSH`, `DISPLACEMENT`, `bait_vibration`, and the Ashen Expanse affinity are existing project-authored DESIGN content. The 12-block sampling range, four-tick sample cadence, movement threshold, crouch suppression, 2.75-block proximity override, 60-tick pursuit memory, 1.35 pursuit speed, and vanilla Silverfish body/navigation are Minecraft execution choices.
- **UNKNOWN:** any canonical Ash Burrower appearance, anatomy, exact sensing mechanism, scent model, underground travel, displacement attack, statistics, ecology, spawn frequency, rewards, or whether crouching would canonically defeat a vibration sense.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains authority for creature identity/descriptors. NeoForge entity AI, sampled movement, target selection, thresholds, renderer, animation, sound, and coordinates remain removable execution. Detection cannot award progression, resolve a Nightmare, alter appraisal, or create Memory/Echo/Soul authority.

## Player-facing behavior in this slice

The registered hostile Ash Burrower no longer acquires every nearby player through vanilla nearest-target AI. Every four server ticks it samples nearby player positions. Meaningful movement within the bounded detection radius can trigger a short pursuit burst; ordinary crouch movement suppresses that ranged detection, while standing almost on top of the creature still triggers proximity aggression. Attacking the creature can still provoke it through the retained hurt-by-target goal.

This is deliberately a first physical executor for the existing `VIBRATION` + `AMBUSH` descriptors, not a claim that the final creature is complete. `SCENT`, real subterranean movement, displacement attacks, bespoke art/audio, and mature encounter ecology remain unimplemented.
