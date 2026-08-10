# Echo runtime evidence — 2026-08-10

## Scope

This note constrains the first Java-owned Echo ownership and summoning integration. It does not define a universal Echo acquisition formula, command system, death lifecycle, or Minecraft entity mapping.

## Primary evidence checked

- **Chapter 47 — Echo:** an Echo is received as an exceptionally rare reward after killing a Nightmare Creature; it exists as owned soul-associated state, can be summoned into a corporeal form, obey commands, fight, carry cargo and perform practical tasks. The chapter's Carapace Scavenger is used as transport immediately after summoning.
- **Chapter 107 — Growing Shadow:** ordinary Echoes are described as replicas that remain as they were, while Sunny's Shadows are separately capable of growth. This is the decisive boundary against treating ordinary Echoes as growth-capable companions.
- **Chapter 193 — The Catacombs:** Echoes are still directly summoned as physical partners during later combat, corroborating that summoning is not merely an early exposition device.
- **Chapter 1826 — Coming Storm:** much later material still treats an Echo as a summonable protector that can be dismissed from one location and summoned to another, confirming the basic runtime concept persists.

Working access followed `docs/LORE-SOURCE-POLICY.md`; official WebNovel material was also checked where indexed. No novel prose is reproduced here.

## Classification

**CANON**

- ordinary Echoes can be owned and summoned into physical service;
- Echoes can obey commands and perform combat or utility work;
- ordinary Echoes are replicas rather than independently growing Shadows;
- Echoes remain distinct from Sunny's Aspect-specific Shadows.

**INFERRED**

- persistent Java ownership and an ephemeral Minecraft manifestation are a useful software separation for representing soul-owned Echo identity without letting an entity UUID become the identity itself;
- a missing/stale physical manifestation can be reconciled without deleting canonical ownership.

**DESIGN**

- awarding the authored **Ash Burrower** from the current First-Nightmare appraisal preview;
- the exact command names `/shadowslave_echo summon ash_burrower` and `/shadowslave_echo dismiss ash_burrower`;
- using a vanilla Armadillo as the temporary physical executor for Ash Burrower;
- making that placeholder manifestation invulnerable while destruction semantics are not yet implemented;
- the exact acquisition/source/provenance strings and player-facing messages.

**UNKNOWN**

- any universal Echo drop probability or appraisal reward formula;
- the complete canonical transfer/destruction/recovery rules across all Echo types and eras;
- whether every Echo can be re-summoned after physical destruction under every circumstance;
- a universal command vocabulary or AI model.

**COMPATIBILITY**

- Java `EchoOwnershipData` owns stable identity, provenance and the current manifestation reference;
- NeoForge/Minecraft owns only physical spawning/despawning and presentation;
- the vanilla Armadillo executor is removable and must not become the canonical definition of Ash Burrower;
- future Echo creature models, AI, combat executors or external-mod adapters must consume Java-owned Echo identity rather than replace it.
