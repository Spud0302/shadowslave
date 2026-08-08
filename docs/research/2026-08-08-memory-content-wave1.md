# Memory content wave 1

**Status:** player-facing content slice; not wired into loot, persistence or Minecraft item registration.  
**Classification:** authored Minecraft **DESIGN** constrained by primary Shadow Slave Memory mechanics.

## Primary evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked chapter text rather than relying on wiki summaries.

- **Chapter 74 — Midnight Shard:** explicitly presents Memory Rank and Tier as separate properties and states that there are seven Tiers corresponding to Nightmare Creature Classes. It also cautions that a higher Tier is not an absolute guarantee of a stronger Memory.
- **Chapter 104 — Soul Arsenal:** shows a practical inventory containing weapons and tools with very different enchantment purposes, including a returning thrown weapon, a sound-repeating tool, and a water-producing utility Memory.
- **Chapter 204 — Mysterious Key:** shows one Memory carrying multiple distinct enchantments, including a powerful effect with a meaningful resource/risk interaction.
- **Chapter 261 — Moon Shard:** gives a shield as `Memory Type: Weapon`, demonstrating that the Spell-facing type vocabulary is not identical to ordinary Minecraft equipment-slot categories.
- **Chapter 695 — Sonorous Silver Bell:** shows an enchantment being copied/transplanted into an existing Memory, reinforcing that enchantment identity can be considered separately from the host Memory.
- **Chapter 869 — Sin of Solace:** later high-Rank evidence continues to use Rank, Tier and Type as independent Memory descriptors.
- **Chapter 2038 — Soul of a Poet:** much later material still shows weapons, armor and tools with authored descriptions and varied practical functions, including Memories created by an Awakened rather than only directly bestowed from kills.

## Evidence classification

**CANON**

- Memories have Rank and Tier as separate properties.
- Seven Tiers exist and are associated with the seven Nightmare Creature Classes for naturally obtained Memories.
- Memory Types include at least Weapon, Armor, Tool and Charm in the novel's broader usage.
- A Memory may possess multiple enchantments with qualitatively different effects.
- Memory enchantments can provide combat, utility, mobility, information, resource and other effects rather than one universal stat template.
- Later sorcery can create and modify Memories, so player-facing Memory content should not assume every Memory must come directly from one Spell kill.

**INFERRED**

- A reusable Minecraft content model should keep stable Memory identity, Rank, Tier, Type and enchantment identities separate so execution providers can change without changing canonical content identity.
- Authored Memories benefit from explicit gameplay-role metadata because novel enchantments vary strongly in purpose.

**DESIGN**

- All twelve Wave-1 Memory names, exact Rank/Tier assignments, theme tags, enchantment names, roles and gameplay hooks.
- `EnchantmentRole` is a Minecraft/content-authoring taxonomy, not Nightmare Spell terminology.
- `design/memory-wave1/...` provenance is repository metadata.

**UNKNOWN**

- No universal Spell drop probability, Memory-generation formula or enchantment-count formula is asserted.
- Tier is not treated as a deterministic count of enchantments in this content slice.
- No universal rule is asserted for translating Rank/Tier into Minecraft damage, armor points, cooldowns or essence costs.
- Exact compatibility rules for transplanting arbitrary enchantments remain outside this slice.

**COMPATIBILITY**

- Java remains the sole canonical owner of Shadow Slave identity and progression state.
- This slice has no item registry, loot table, inventory attachment, save codec or execution provider.
- Future external-mod adapters may render or execute these stable IDs but must remain removable and may not become the authority for ownership or progression.

## Wave-1 content

The catalogue contains twelve deliberately varied concepts:

- **Ash Compass** — navigation/survival tool;
- **Bellglass Token** — warning/communication charm;
- **Blackwater Hook** — mobility/control weapon;
- **Borrowed Dawn** — recovery charm with a repeated-use tradeoff;
- **Glass Road** — precision/mobility weapon;
- **Last Watch Mantle** — defensive/rescue armor;
- **Mirewalker Boots** — terrain mobility armor;
- **Pale Ferryman's Lantern** — guidance tool with escalating essence burden;
- **Red Thread Bracelet** — companion rescue/communication charm;
- **Stonewake Shield** — defensive/control weapon;
- **Thorn Mercy** — restraint weapon whose authored identity discourages indiscriminate lethal use;
- **Veil-Stitch Case** — repair/concealment tool.

The slice intentionally includes non-combat utility and costs/conditions so future loot does not collapse into linear damage upgrades.

## Integration boundary

A later implementation should introduce persistent Memory ownership and execution only after deciding the authoritative Java storage model. Spell-bestowed rewards, crafted Memories and scenario-authored rewards should all resolve to stable persisted Memory instances rather than being reconstructed from mutable external providers.

No live reward path is changed here.
