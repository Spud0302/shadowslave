# Dream Realm resource-site micro-modules — lore and design evidence

**Status:** player-facing content evidence note  
**Scope:** bounded presentation/decision modules for the 30 `resourceHooks` already authored in `DreamRealmRegionContentCatalog`  
**Architecture:** Java-owned stable region/resource identity; removable world/UI/loot adapters only

## Sources checked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked current `main`, open PRs/issues, the Nightmare/Seed roadmap, the existing Dream Realm region catalogue, and adjacent open landmark/ecology/story content before implementation.

Primary/later chapter material:

- **Chapter 370 — Exploration Report:** geography, environment, landmarks, Nightmare Creature behavior/weaknesses, Memories/Echoes, and accumulated exploration knowledge are all useful field information.
- **Chapter 380 — Above and Below:** environmental cycles materially constrain safe movement; killed creatures can be processed for soul shards; harvesting useful material can impose tactical delay/risk.
- **Chapter 1383 — A Pile of Soul Shards:** a large battle can yield many recoverable soul shards while yielding no new Memories or Echoes, strong negative evidence against treating ordinary resource recovery as guaranteed Memory/Echo acquisition.
- **Official WebNovel catalogue:** checked to confirm Chapter 1383 identity/publication listing.

## Evidence boundary

**CANON**

- Dream Realm exploration involves actionable knowledge about geography, environment, landmarks and threats.
- Material recovery from dangerous environments and defeated Nightmare Creatures can be a meaningful practical activity.
- Soul shards are recoverable resources in relevant creature contexts.
- Memories and Echoes are not guaranteed by a large number of creature kills.

**INFERRED**

- A player-facing resource interaction benefits from separating observation/verification, a bounded gathering or recovery decision, and the authoritative result applied later by gameplay systems.
- Leaving a site unchanged can be a meaningful option when local danger, uncertainty or carrying cost matters.

**DESIGN**

- All 30 exact resource-site micro-modules in `DreamRealmResourceSiteMicroModuleCatalog`.
- The `GATHERING`, `RECOVERY`, and `VERIFICATION` interaction families.
- Exact resource names, cues, decision prompts/options, anti-overclaim boundaries and deterministic approach-cue selection.
- Generator version `dream-realm-resource-site-micro-module-v1`.
- Existing resource hooks such as `stormglass`, `echo_stone`, `red_sap`, `fossil_resin`, etc. are project-authored content, not canonical Shadow Slave materials.

**UNKNOWN**

- Any canonical Dream Realm resource-site generation, placement, abundance, depletion, regeneration or respawn formula.
- Quantity, quality, rarity, trade value, crafting recipe, durability, toxicity, edibility, alchemical effect or supernatural property for these project-authored resources.
- Whether any such site can award a Memory, Echo, Soul Shard, Attribute, progression state or appraisal credit.
- Universal loot tables, gathering speeds, tool requirements, ownership rules, economy pricing, settlement demand, or resource-to-region probability.
- Any canonical procedural resource-generation system resembling this catalogue.

**COMPATIBILITY**

- `DreamRealmRegionContentCatalog` remains Java authority for stable region identity and its authored `resourceHooks`.
- This catalogue accepts only an already-resolved region/resource anchor and varies presentation only.
- A future Java-owned exploration/resource instance may persist the resolved module ID and presentation seed if exact narration must survive restart.
- Structure placement, block mutation, inventory changes, actual item stacks, depletion, regrowth, NPC economy, particles, sound, models and HUD remain removable execution/presentation adapters and must not become canonical progression authority.

## Implementation limits

This slice deliberately does **not**:

- create item definitions or loot tables;
- award Soul Shards, Memories, Echoes or progression;
- decide whether a resource site exists;
- assign spawn/placement frequency;
- mutate world blocks;
- persist depletion/regrowth state;
- assign prices, recipes or rarity;
- infer creature provenance from a resource;
- claim any canonical resource-generation formula.

The seed can only choose between two already-authored approach cues for the exact caller-supplied region/resource pair.
