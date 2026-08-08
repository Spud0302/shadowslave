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

### Chapters 15, 81 and 110 — Memory acquisition from defeated creatures

**CANON:** Sunny receives Memories associated with defeated Nightmare Creatures. Chapter 15 identifies the Puppeteer's Shroud as belonging to the Mountain King; Chapter 81 presents another direct Memory acquisition; Chapter 110 retrospectively links several Memories to the beings/events that produced them. This supports a creature-defeat acquisition context without supporting any universal drop probability or deterministic reward-selection formula.

### Chapters 104 and 263 — Memory transfer / trade

**CANON:** a Memory can pass deliberately between owners. Chapter 104 identifies the Endless Spring as a farewell gift from Cassie. Chapter 263 explicitly describes Sunny trading soul shards for Memories from other cohort members. Present ownership therefore does not uniquely identify acquisition history.

### Chapters 263 and 1373 — Echo gifts / transfer

**CANON:** Echo ownership can be deliberately given to another person. Chapter 263 states that two of Cassie's Echoes were given to her by Nephis and Kai; Chapter 1373 later reiterates that Nephis regularly gave away earned Echoes and that two of Cassie's three came from her. An Echo's present owner therefore need not be the person who originally earned it.

### Chapters 1373 and 1609 — artificial Echoes

**CANON:** artificial Echoes exist. Chapter 1373 shows a steel mannequin artificial Echo associated with Valor enchanters; Chapter 1609 later describes Nephis's steel stallion as an artificial Echo created by Great Clan Valor enchanters. Artificial Echo provenance must not invent a slain-creature Rank/Class/source.

### Chapter 1960 — Master Weaver

**CANON:** Sunny creates an entirely original Memory through weaving. Player-created Memories therefore exist independently of creature-drop acquisition.

## Evidence boundary

- **CANON:** Memories and Echoes can be received in connection with defeated Nightmare Creatures; Memories and Echoes can be transferred; Memories can be artificially created through weaving; artificial Echoes exist.
- **INFERRED:** acquisition provenance is useful separable Java content from the owned Memory/Echo identity itself, because present ownership does not uniquely determine how the object was obtained.
- **DESIGN:** the exact source enum, visibility enum, stable context IDs, wording, evidence tags, scenario-authored discovery contexts, deterministic presentation selection and generator version.
- **UNKNOWN:** canonical Memory/Echo drop probabilities; reward-selection formulas; whether every creature can produce either reward; universal transfer timing/UI; artificial-Echo construction method; universal Memory-crafting recipe; provenance information exposed by the Spell; any generic scenario-discovery reward rule.
- **COMPATIBILITY:** Java-owned state supplies the resolved acquisition source and subject identity. NeoForge/client/chat/audio/entity adapters may display the resulting context but may not choose or mutate ownership/progression.

No canonical generation, drop or reward formula is claimed.

## Validation expectations

The focused unit suite requires:

- every source family for both subject kinds has authored coverage;
- authoritative subject/source cannot be changed by evidence or seed;
- identical seed + authoritative source + evidence produces identical presentation;
- evidence map order and positive magnitude do not alter composition;
- negative evidence fails closed;
- returned matched-evidence tags are traceable to the selected authored primitive;
- unknown provenance remains hidden and does not imply a creature/crafter source;
- artificial Echo presentation explicitly avoids creature provenance claims;
- all context records retain explicit `DESIGN` evidence classification.

## Integration boundary

This slice does not depend on PR #105. After both land, the Java-owned acquisition record can compose an acquisition context and #105 can render the already-resolved Memory/Echo identity. A later persistence layer should store the authoritative acquisition source if provenance is intended to survive restart; presentation should never recompute ownership history from catalogue identity alone.
