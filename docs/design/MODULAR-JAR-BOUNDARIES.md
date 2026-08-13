# Modular JAR and WIP extraction boundaries

Status: owner direction / architecture guardrail. This document does not by itself authorize broad runtime refactors.

## Goal

Keep `shadow-slave.jar` small enough to remain the stable authority for Shadow Slave progression while allowing systems that benefit from independent iteration, larger content pools, or substantially deeper implementation to live in separate JARs/modules.

The split is not primarily about file size. It is about preventing one feature lane from forcing the base mod to develop far beyond the minimum coherent playable experience.

## Core rule

**Shadow Slave owns grammar and authority; optional providers add vocabulary or replaceable execution.**

Canonical player/world state must remain readable and authoritative without an optional generator/content JAR owning that state.

Resolved permanent results are persisted as resolved records. Installing, updating or removing a generator/provider must not silently reroll an existing Aspect, Flaw, Attribute, Memory, Echo or completed Nightmare result.

## Base `shadow-slave.jar` owns

The base mod should retain:

- Soul/progression state and persistence;
- Nightmare Spell lifecycle, First Nightmare infection/trial flow and appraisal transaction authority;
- later-Nightmare Seed/Gate framework and provider admission/safety checks;
- provider-independent technical recovery metadata sufficient to recover participants when optional Nightmare content disappears;
- Aspect/Flaw/Attribute resolved identity state and a small dependable fallback generator;
- Memory identity/ownership/manifestation authority plus provider APIs;
- Echo identity/ownership/command/persistence authority plus provider APIs;
- Nightmare Creature identity contracts and provider APIs;
- Dream Realm identity, region/site/provider registries, persistence and discovery contracts;
- one small representative playable vertical slice for each critical system;
- compatibility/version/provenance contracts for optional modules.

The base mod should not need every official expansion installed to remain playable.

## Optional module families

### `combat-core.jar`

A Shadow-Slave-agnostic combat fundamentals module may own generic action timing, attack geometry, commitment/recovery, player/mob executor seams and generic combat hooks.

It must not know about Souls, Aspects, Flaws, Memories, Echoes, Nightmare Creatures, Rank, appraisal or Shadow Slave progression. Shadow Slave translates authored supernatural content into generic combat actions.

### Region/content expansion JARs

Official content should generally be grouped as coherent Dream Realm regions/eras rather than one JAR per feature.

A region provider may contribute together:

- biome/terrain/structure vocabulary;
- settlements, ruins, resources and historical sites;
- creatures and their Echo-capable definitions;
- Memories associated with the region/content;
- encounters/ecology/discovery content;
- Nightmare scenarios or historical reconstructions where appropriate;
- compatible identity-generation primitives where justified.

This keeps generated content thematically coherent instead of combining unrelated global pools.

### Advanced identity-generation JAR

An optional advanced generator may improve interpretation and composition of Aspects/Flaws/Attributes by using richer evidence, compatibility solving and larger primitive pools.

The base mod still owns the final resolved identity and persists it. Removing or updating the generator cannot mutate existing resolved characters.

The base must retain a smaller deterministic fallback generator so the core progression loop is never dependent on the advanced generator JAR.

### Future WIP/provider JARs

Experimental work that has become deeper than the base alpha requires should be preserved in a WIP module/JAR instead of continuing to widen `shadow-slave.jar`.

A WIP JAR is not automatically a pack dependency or release commitment. It is a holding/integration surface for useful work that should not dictate base-mod scope yet.

## Extraction decision rule

Before deepening a feature inside the base mod, ask:

1. Is this required for the minimum coherent Shadow Slave progression loop?
2. Does canonical authority need to live in the base, or only the provider/API contract?
3. Does the feature substantially increase variety/quality only when a large catalogue or sophisticated algorithm is present?
4. Can the base keep one representative implementation while the deeper implementation moves behind a provider boundary?
5. Would removal of this code make existing saves undecodable or existing resolved state ambiguous?

If the answer is “not required for the minimum loop”, “provider contract is enough”, and existing saves remain safe, prefer WIP/provider extraction over continued base-mod development.

## Current work classification

### Keep/integrate in base

- Nightmare lifecycle, appraisal persistence/recovery and resolved identity persistence;
- Memory/Echo ownership and manifestation/command authority;
- Seed/Gate framework with provider-gated activation and provider-independent participant/Gate recovery;
- provider/registry contracts;
- one representative First Nightmare and Dream Realm vertical slice;
- one representative Memory, Echo and Nightmare Creature execution path;
- stable historical-site identity linking Nightmare reconstruction and later Dream Realm geography.

### Stop deepening in base; preserve as WIP/module candidates

- generic combat/movement framework beyond the current playable proof -> `combat-core`;
- advanced stability/stagger, limb injury, broad damage/target layers, advanced movement and build graphs -> future combat WIP after the combat-core MVP proves a need;
- sophisticated Aspect/Flaw/Attribute generation beyond the dependable fallback -> advanced identity-generation WIP JAR;
- Storm Lantern Coast macro-region/native-worldgen breadth beyond the minimum era-linked proof -> region expansion WIP JAR;
- large authored Memory runtime breadth beyond representative base examples -> region/content provider packs rather than continued base catalogue growth;
- large Echo/creature breadth and region-specific presentation/ecology -> the same coherent region/content providers where possible;
- additional region landmarks, ecology weights, clue families and presentation polish that do not prove a new base API -> region WIP rather than base-alpha scope.

Existing useful work is not to be deleted merely because it crossed this line. Preserve it, stop extending it in the base, and migrate/port it behind an optional module boundary when that can be done without destabilizing current-main integration.

## Storm Lantern / Drowned Bell boundary

The base may keep the Drowned Bell / Storm Lantern relationship as the representative proof that a Nightmare historical site and later Dream Realm ruin can share stable identity.

The base does not need to grow the entire Storm Lantern Coast into a mature region before alpha. Further native landmarks, ecology breadth, terrain vocabulary, settlement depth and region content should be treated as an official region-expansion WIP unless a small piece is specifically required to prove a provider API.

## Memory and Echo boundary

Base Shadow Slave owns what a Memory/Echo is, how ownership and persistence work, and how providers register executable content.

The base should retain only enough authored examples to prove those systems. Additional finished Memories/Echoes should preferably arrive through coherent content providers so their source creatures, regions, encounters and themes remain aligned.

## Seed/Gate installation safety

Later-Nightmare Seed/Gate semantics remain base concepts, but a persistent escalating threat must not activate unless the current installation contains a legitimate resolvable provider path for that tier/content.

Provider removal safety applies to **all unresolved lifecycle phases**, not only immature Seeds:

- an unresolved Seed whose provider disappears becomes unavailable/dormant and cannot mature further;
- an already-bloomed linked Gate must be suspended or safely contained while remaining explicitly unresolved, so missing content cannot leave an unwinnable active breach or be mistaken for successful closure;
- a participant already inside a provider-owned Nightmare must have a base-owned technical abort/recovery route back to safe base-known state even when the base cannot reconstruct the missing provider's structures or resolution graph;
- provider disappearance must never itself award Nightmare success, appraisal, rewards or progression;
- restoration or explicit migration may resume/resolve the retained state once a valid authority path exists.

The detailed lifecycle contract is pinned in `docs/design/NIGHTMARE-SEED-EXPANSION-SAFETY.md`.

## Loop/automation guardrail

Repository automation should treat this document as a scope gate:

- alpha assembly work integrates authority, provider seams and minimum representative slices;
- combat-core work stays in the standalone combat module and stops after its MVP acceptance criteria;
- region/world work should not resume broad Storm Lantern expansion inside the base mod;
- advanced identity-generation work belongs in an optional WIP module after the base fallback/provider seam is stable;
- design-only advanced combat documents remain future constraints, not authorization to implement them in `shadow-slave.jar`;
- when a lane becomes substantially deeper than the base alpha needs, preserve the work and move its next development step to a WIP/provider JAR instead of continuing scope growth in base.

## Migration principle

Do not perform a destructive mass extraction simply to make the directory tree look modular. Prefer this order:

1. pin ownership/provider interfaces;
2. stop new deep work in the base lane;
3. prove one optional module can build and load independently;
4. port the overdeveloped implementation behind that boundary;
5. leave compatibility shims only where needed;
6. delete duplicate base implementation only after equivalent-or-stronger module integration is proven.

This protects current playable work while changing the direction of future development.
