# Dream Realm landmark micro-modules — lore/evidence note

**Scope:** player-facing interaction content for the 30 landmark hooks already present in `DreamRealmRegionContentCatalog`.

**Source-policy check:** `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current `main`, open PRs/issues, and current Dream Realm region/creature/story work were reviewed before implementation. The owner-designated NovelFull index currently lists through Chapter 3116, while official WebNovel reports 3,131 chapters; NovelFull is therefore treated as a reading-access layer rather than current-publication authority.

## Primary and later chapter material checked

- **Chapter 370 — Exploration Report:** Sunny treats Dream Realm geography, environment, notable landmarks, creature behavior/weaknesses, and personally gathered field information as practical survival knowledge. This is the primary basis for making landmarks readable and informational rather than decorative props.
- **Chapter 468 — Desecrated Grove:** a detailed map with known dangers makes travel safer; established routes, patrols, dangerous wilderness, and known creature locations materially affect route choice. This supports observation, verification, detour, and route-planning gameplay around landmarks.
- **Chapter 752 — Solid Foundation:** Citadel/Gateway geography can sharply constrain safe access and the usefulness of a location. This reinforces that a landmark's value can be contextual rather than an intrinsic loot/objective guarantee.
- **Chapter 1608 — Death Zone:** much later material again shows movement timing, local environmental danger, and tactical waiting/route choice interacting materially with survival in the Dream Realm.
- **Chapter 2278 — Field Reports:** later operational reporting continues to treat travel conditions, logistics, route clearing, and environmental constraints as information worth communicating, without establishing a universal landmark interaction system.

## Evidence classification

### CANON

- Dream Realm geography, environments, notable landmarks, routes, and known dangers can be meaningful survival information.
- Maps and accumulated field knowledge can make traversal materially safer.
- Local environmental conditions can change whether movement, waiting, shelter, or a particular route is prudent.
- Human explorers can observe and report uncertain field information rather than possessing automatic omniscient knowledge.

### INFERRED

- A reusable game-content layer can separate a landmark's stable identity from the player-facing decision it creates at a particular visit.
- Observation, traversal, recovery, avoidance, and information verification are useful authoring concerns for turning landmarks into gameplay without requiring every landmark to be a quest objective or combat arena.
- Explicit negative boundaries are useful wherever an evocative ruin could otherwise be mistaken for prophecy, guaranteed loot, hidden-objective revelation, or canonical history.

### DESIGN

- All 30 exact landmark micro-modules in `DreamRealmLandmarkMicroModuleCatalog`.
- The five `InteractionFamily` values.
- Every approach cue, decision prompt, decision option, hazard/opportunity pairing, and anti-overclaim boundary.
- The rule requiring every module to use a hazard and opportunity already authored for its source region.
- `dream-realm-landmark-micro-module-v1` and deterministic approach-cue selection.

### UNKNOWN

- Any canonical landmark-generation, placement, density, respawn, discovery, or interaction formula.
- Whether any project-authored landmark has a supernatural function, historical identity, faction ownership, hidden objective, guaranteed resource, Memory, Echo, Soul Shard, or other reward.
- Universal safe-route rules, environmental timing formulas, loot tables, trade inventories, map-reveal mechanics, ruin history, or archaeology rules.
- Whether the Dream Realm or Nightmare Spell procedurally composes landmark encounters in any way resembling this catalogue.

### COMPATIBILITY

- `DreamRealmRegionContentCatalog` remains Java authority for stable region identity and the existing landmark/hazard/opportunity primitives.
- This catalogue accepts an already-resolved region + landmark pair and may vary only presentation of an authored approach cue.
- It does not place structures, generate terrain, award resources, reveal objectives, mutate progression, spawn entities, or persist canonical state.
- Future structure, sound, particle, HUD, map, dialogue, loot-container, or interaction adapters may render/execute a resolved module, but remain removable and cannot become region/landmark/progression authority.

No canonical landmark generation, world-placement, archaeology, reward, loot, map-reveal, or interaction formula is claimed.
