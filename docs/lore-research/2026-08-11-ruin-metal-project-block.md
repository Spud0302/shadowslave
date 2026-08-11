# Ashen Expanse ruin-metal project block — 2026-08-11

## Scope

This slice retires the vanilla `raw_iron_block` physical executor for the already-authored Ashen Expanse `ruin_metal` resource hook. It adds a registered `shadowslave:ruin_metal` block and project-authored three-piece block model while preserving inspection-only behavior. It does not create harvesting, drops, crafting, trade value, Soul Shards, Memories, ownership, progression, Nightmare resolution, or persistence.

## Repository authority checked

`DreamRealmVerticalSliceDefinition` / `DreamRealmRegionContentCatalog` already own the `ruin_metal` hook and SALVAGE opportunity. `DreamRealmResourceInteractionBinding` already owns the bounded inspection copy and exact three-block cluster footprint. The Minecraft block registry, model and placement are therefore replaceable presentation/execution only.

This branch is stacked on PR #233 so it consumes the active Cinder Rest world-presentation changes instead of racing `DreamRealmPreviewService` or `ShadowSlaveMod` from a competing main-based branch.

## Primary and later lore check

Research followed `docs/LORE-SOURCE-POLICY.md`.

- Chapter 1800, **Return to the Black Mountain**, was rechecked through the owner-designated NovelFull access layer. It describes the Dream Realm's devastated history and the destruction/erasure of much evidence from former human civilizations.
- Chapter 2046, **Spellsmiths of Valor**, was rechecked as a much later clarification of practical material use during conquest. It shows Awakened artisans processing locally available materials, especially harvested Nightmare Creature remains; it does not establish a generic ruin-metal resource category.
- Official WebNovel's publication page was cross-checked for the current official publication identity/context.

These sources support ruined former civilizations and practical material recovery/use in the Dream Realm. They do **not** establish this project's `ruin_metal`, its composition, visual appearance, value, yield, or supernatural properties.

## Evidence boundary

- **CANON:** the Dream Realm contains devastated remnants of former civilizations; materials encountered during Dream Realm operations can be practically processed and used.
- **INFERRED:** an already-authored Minecraft region hook may be physically represented as inspectable ruined material without assigning it a canonical yield or property.
- **DESIGN:** `ruin_metal`, the SALVAGE hook, `shadowslave:ruin_metal`, its exact three-piece geometry, metal block sound/strength, cluster coordinates, inspection wording, and current raw-iron material pixels.
- **UNKNOWN:** canonical `ruin_metal` existence/name, composition, color, corrosion, supernatural qualities, harvestability, drop form, quantity, crafting use, economy value, depletion/respawn, ownership, or relation to Memories/Soul Shards.
- **COMPATIBILITY:** Java region/resource definitions remain authoritative. The registered block, blockstate/model, physical coordinates and right-click validation can be replaced without migrating or changing canonical player/resource/progression state.

## Dependency and asset decision

No third-party dependency is added. A static registered block and block model are solved directly by Minecraft/NeoForge's registry and resource systems; GeckoLib, SmartBrainLib, Curios, Veil and TerraBlender provide no material benefit to this slice.

The three-piece model JSON is original project-authored geometry. No content-mod, novel, adaptation, resource-pack, model, sound or structure asset is copied. The model still references Minecraft's bundled `raw_iron_block` texture as a clearly marked temporary material placeholder; replacing those pixels with an original project texture is a later presentation slice.

## Player-visible result

The three `ruin_metal` blocks in the bounded Ashen Expanse preview now use the project namespace and a stepped three-piece salvage silhouette rather than rendering as ordinary full-cube raw-iron blocks. Right-click inspection continues to resolve the same Java-owned resource hook and still grants nothing.

## Deliberate limits

- material pixels remain the vanilla raw-iron texture placeholder;
- collision remains the default full-block shape even though the visual model is stepped;
- no BlockItem, loot table, harvesting system, depletion, respawn, economy, crafting, resource persistence, custom audio, particles or VFX;
- `bone_char` and `dry_fungus` still use vanilla block placeholders;
- exact geometry/material treatment remains replaceable DESIGN.

Fresh exact-head compile/unit/package, physical NeoForge client boot, dedicated-server boot and frozen-datapack compatibility evidence is required before this slice is called green.
