# Memory / Echo acquisition-context content — wave 1

**Status:** player-facing content DESIGN; does not award or persist Memories/Echoes.  
**Architecture:** Java supplies canonical resolved ownership/source identity; presentation and external adapters consume it only.  
**Source rule:** `docs/LORE-SOURCE-POLICY.md` remains controlling.

## Content added

`MemoryEchoAcquisitionContextCatalog` defines reusable provenance presentation for five acquisition-source families across Memories and Echoes:

- slain-creature reward;
- transfer from another owner;
- artificial creation;
- authored scenario discovery/handoff;
- deliberately unknown provenance.

The authoritative acquisition source is always supplied by the caller. The catalogue can only resolve a compatible presentation context for that already-known source. It cannot roll whether a creature dropped a Memory/Echo, decide that a transfer occurred, invent an artificial creator, or convert hidden provenance into a guessed origin.

`AUTHORED_DISCOVERY` is explicitly Minecraft **DESIGN** for story content. It does not claim the Nightmare Spell has a universal scenario-cache reward mechanic.

## Primary lore evidence checked

Research followed the repository source hierarchy and rechecked direct chapter material plus later clarification.

### Chapter 47 — Echo

**CANON:** Echoes are rare rewards that Awakened can receive after slaying Nightmare Creatures. The chapter also establishes that an Echo is represented in the owner's Soul Sea and is a distinct player-facing possession.

### Chapters 81 and 110 — Memory acquisition from slain creatures

**CANON:** Sunny receives Memories associated with slain Nightmare Creatures. Chapter 110 retrospectively identifies several Memories by the creatures/events that produced them. This supports a creature-slaying acquisition context without supporting any universal drop probability or deterministic reward-selection formula.

### Chapters 1529 / 1541 and later transfer examples

**CANON:** Memories can be deliberately transferred between owners. Later material repeatedly uses transfer for upgrade, gifting and redistribution. Physical/social mechanics beyond the observed transfer remain outside this content slice.

### Chapters 263 / 1362 / 1373 — Echo transfers

**CANON:** Echo ownership can be transferred/given to another person. This means an Echo's present owner need not be the person who originally received it from a slain creature.

### Chapter 1609 — Reclusive Saint

**CANON:** practical mount Echoes are deliberately hunted for, and Nephis uses a steel stallion described as an artificial Echo created by Great Clan Valor enchanters. Therefore artificial Echo provenance must not invent a slain-creature Rank/Class/source.

### Chapter 1960 — Master Weaver

**CANON:** Sunny creates an entirely original Memory through weaving. Player-created Memories therefore exist independently of creature-drop acquisition.

## Evidence boundary

- **CANON:** Memories and Echoes can be received in connection with slain Nightmare Creatures; Memories and Echoes can be transferred; Memories can be artificially created through weaving; artificial Echoes exist.
- **INFERRED:** acquisition provenance is useful separable Java content from the owned Memory/Echo identity itself, because present ownership does not uniquely determine how the object was obtained.
- **DESIGN:** the exact source enum, visibility enum, stable context IDs, wording, evidence tags, scenario-authored discovery contexts, deterministic presentation selection and generator version.
- **UNKNOWN:** canonical Memory/Echo drop probabilities; reward-selection formulas; whether every creature can produce either reward; universal transfer timing/UI; artificial-Echo construction method; universal Memory-crafting recipe; provenance information exposed by the Spell; any generic scenario-discovery reward rule.
- **COMPATIBILITY:** Java-owned state supplies the resolved acquisition source and subject identity. NeoForge/client/chat/audio/entity adapters may display the resulting context but may not choose or mutate ownership/progression.

No canonical generation, drop or reward formula is claimed.

## Validation expectations

The focused unit suite requires:

- every source family for both subject kinds has authored coverage where valid;
- incompatible source/subject combinations fail closed;
- identical seed + authoritative source + evidence produces identical presentation;
- evidence map order and positive magnitude do not alter selection;
- negative evidence fails closed;
- returned matched-evidence tags are traceable to the selected authored primitive;
- unknown provenance remains hidden and does not imply a creature/crafter source;
- artificial Echo presentation explicitly avoids creature provenance claims;
- all context records retain explicit `DESIGN` evidence classification.

## Integration boundary

This slice does not depend on PR #105. After both land, the Java-owned acquisition record can compose an acquisition context and #105 can render the already-resolved Memory/Echo identity. A later persistence layer should store the authoritative acquisition source if provenance is intended to survive restart; presentation should never recompute ownership history from catalogue identity alone.
