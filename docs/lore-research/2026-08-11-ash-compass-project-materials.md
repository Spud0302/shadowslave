# Ash Compass project material textures — 2026-08-11

## Scope

This slice retires the remaining bundled-vanilla material placeholders from the already-merged Ash Compass 3D item presentation. It changes only item rendering resources and focused presentation tests.

`MemoryOwnershipData`, the existing Ash Compass runtime item, `MemoryContentCatalog`, appraisal/reward identity, Nightmare lifecycle, progression, and persistence are unchanged.

## Repository / overlap check

Current `main` was checked at `812644e6c141cdcbbbfc1b02dfe1008179eb83cf` together with open PRs/issues and the required status, handoff, lore, Nightmare-roadmap, and third-party dependency policy files.

Active branches already own Ash Compass runtime direction/threat execution (#220/#221), Bellglass/Red Thread Memory behavior (#224/#226/#229), Ash Burrower Echo behavior (#214/#216), Drowned Listener sensing/presentation (#217/#222/#225), Watch Captain execution/presentation (#227), and restart correctness (#223/#228). This branch is based directly on current main and changes only Ash Compass presentation assets/tests so it does not duplicate those runtime lanes.

PR #200 is merged and `docs/THIRD-PARTY-DEPENDENCY-POLICY.md` is present on main, so no successor-policy fallback is required.

## Source-policy boundary

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. The merged Ash Compass presentation note already records that the item's exact appearance/materials are not established by canon. No new lore mechanic or canonical appearance claim is introduced here, so this slice does not manufacture a new novel proposition.

- **CANON:** unchanged Memory ownership/summon/dismiss boundaries from the existing Java content; no canonical Ash Compass appearance or material claim is made.
- **INFERRED:** an already-manifested Memory benefits from a stable, recognizable presentation subordinate to Java-owned identity.
- **DESIGN:** Ash Compass itself; the existing 3D geometry; charcoal/ash case texture, warm worn-metal needle texture, ember-fissure face texture, 16x16 atlas choices, and exact pixel art.
- **UNKNOWN:** canonical Ash Compass existence, appearance, materials, colors, glow, needle motion, summon/dismiss effects, sound, and final art direction.
- **COMPATIBILITY:** Java remains authority for Memory identity, ownership and behavior. The three PNGs and model texture bindings are replaceable presentation assets with no save migration or canonical-state consequences.

## Asset provenance

The following PNGs are original project assets created specifically for this repository and are not copied or adapted from a content mod, novel, adaptation, texture pack, or other third-party artwork:

- `textures/item/ash_compass_case.png` — 16x16, SHA-256 `14eca52ddfc0176b31a05c55526b40b037b09aa10aa1a0ae0259a660730405ff`;
- `textures/item/ash_compass_needle.png` — 16x16, SHA-256 `90067925082c4c168ab4abf2a6d84b539bbe64f6077415874ec3c62511c3b217`;
- `textures/item/ash_compass_ember.png` — 16x16, SHA-256 `6de0b40ae561a2b3a049f4714a690e33d39e7a0b60ddd5fa0e6d7e747055518e`.

The model now references only `shadowslave:item/ash_compass_*` material textures and no longer references polished blackstone, oxidized copper, or magma.

## Dependency decision

No external dependency is added. GeckoLib remains the approved model/animation infrastructure when animation is needed, but static project textures on an already-working vanilla item model do not justify adding or widening a runtime dependency. SmartBrainLib, Curios, Veil, and TerraBlender are not adopted by this slice.

## Player-visible result

The Ash Compass keeps its existing recessed 3D compass body but now has project-owned material identity rather than looking like assembled vanilla block textures: a dark ashen case, a worn warm-metal needle, and a restrained ember-fissure face/tip treatment.

## Test boundary

`AshCompassPresentationResourcesTest` now requires all three project texture bindings, rejects the former vanilla material paths, and decodes every PNG as an exact 16x16 image.

Hosted compile/unit/package, physical NeoForge client boot, dedicated-server boot, and frozen-datapack compatibility remain required before the exact head is called green.

## Remaining presentation gaps

- no animated needle or response-driven model state;
- no emissive texture/render layer;
- no summon/dismiss animation, particles, custom audio, or Spell HUD treatment;
- exact palette/pixel art remain replaceable DESIGN.
