# Dream Realm story content — wave 1

**Status:** player-facing DESIGN content foundation.  
**Architecture:** Java owns stable content identity; future NeoForge/UI/NPC adapters may present or execute it but must remain removable.  
**Lore rule:** `docs/LORE-SOURCE-POLICY.md` controls. No faction, settlement, NPC, reputation or quest-generation formula in this wave is claimed canonical.

## Scope

Wave 1 adds ten authored settlement/faction/story modules, one for each existing DESIGN frontier region in `DreamRealmRegionContentCatalog`:

- Ashen Watch / Cinder Rest / Grey Lanterns;
- Chainward Station / Anchor House / Iron Pilgrims;
- Mirror Exchange / Dullglass Market / Veiled Ledger;
- Blackwater Ferry / Rope Harbour / Stillwater Hands;
- Thornward Commune / Stone Ring / Briar Keepers;
- Mist Cairn House / Cairnhouse / True Markers;
- Bonefield Post / Rib Shelter / White Trackers;
- Causeway Archive / Milestone Vault / Road Rememberers;
- Storm Belfry Town / High Bell / Cliff Wardens;
- Red Canopy Camp / Root Market / Raincutters.

Every module supplies services, NPC archetypes, local tensions, independent story hooks, an arrival cue and a qualitative standing rule. These are content definitions only: they award no Rank, Aspect, Flaw, Attribute, Memory, Echo or Soul progression.

## Primary chapter evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and checked primary chapter text plus later examples.

- **Chapter 468 — Desecrated Grove:** human-controlled Dream Realm territory has established routes, patrols, Citadels and dangerous wilderness outside those routes. This supports player-facing route guards, guides, scouts and settlements as broad worldbuilding possibilities.
- **Chapter 752 — Solid Foundation:** Citadels and Gateways act as meaningful human anchors/bases in the Dream Realm and their geography materially affects travel and local safety.
- **Chapters 1306–1307 — Paths of Ascension / Before the Nightmare Spell:** Dream Realm regions/Citadels develop local human traditions, while clans help keep human enclaves from collapsing into lawlessness. This supports differentiated communities and social organizations without implying one universal faction structure.
- **Chapter 2273 — Shadow Clan:** a later clan can be based in the Dream Realm, operate from a Citadel, travel widely and use organized teams/identities. This is a later clarification that human organizations can have practical operational roles beyond direct combat.
- **Chapter 2263 — Beginning of the End:** later material explicitly describes a human settlement paired with a Dream Realm Citadel under organized leadership, reinforcing that settlements can carry political and logistical functions.

## Evidence boundary

- **CANON:** humans establish Citadels/enclaves and settlements in the Dream Realm; routes can be patrolled; organizations/clans can maintain order, travel, logistics and territorial activity; regions and Citadels can develop distinct local practices.
- **INFERRED:** a reusable game-content layer can represent settlement services, local NPC roles, tensions and story opportunities separately from runtime NPC/entity implementations.
- **DESIGN:** all ten settlement names, faction names, faction roles, service combinations, NPC archetypes, tensions, story hooks, arrival cues and standing rules in this wave.
- **UNKNOWN:** any universal canonical settlement/faction taxonomy, reputation system, quest-generation formula, service list, leadership structure, population model, prices, refresh cadence or relationship-score formula.
- **COMPATIBILITY:** stable story-module identity lives in Java. Future client presentation, entity AI, dialogue, structures, economics and quest execution may be adapters but cannot become the canonical source of player progression state.

## Design constraints

The modules intentionally avoid copying canon factions or claiming these frontier regions occupy canonical map positions. They also avoid universal `quest giver -> fixed reward` assumptions. Story hooks are authored prompts for later deterministic composition; a future generator may combine them with region hazards, creatures, Memories and Echoes, but must persist resolved choices rather than rerolling established content after restart.

Qualitative `standingRule` text is deliberately not a numeric reputation formula. A later reputation or relationship system must be separately justified and classified.

## Validation

`DreamRealmStoryContentCatalogTest` checks:

- exactly ten unique story modules covering every current frontier region exactly once;
- unique settlement and faction names;
- all eight authored faction-role families and all ten service families;
- at least three services, NPC archetypes, tensions and hooks per module;
- at least 28 distinct NPC archetypes and 30 distinct story hooks across the wave;
- non-trivial arrival/standing presentation;
- no direct progression-award hooks.

No local Gradle/JUnit/client/server execution is claimed from a connector-only environment. Hosted CI is evidence only if a workflow registers for the exact PR head.
