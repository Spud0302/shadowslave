# Watch Captain physical presentation — 2026-08-11

## Scope

This slice deepens the existing Cinder Rest / Grey Lanterns `watch_captain` Minecraft adapter. It adds no new region, settlement, faction, NPC archetype, service, relationship, reward, progression, Nightmare state or persistence authority.

`DreamRealmStoryNpcExecutionBinding` remains the source of module, region, settlement, faction, archetype, services, arrival cue and standing rule. Minecraft supplies only a replaceable body, props and interaction presentation.

## Lore check

Research followed `docs/LORE-SOURCE-POLICY.md`. Chapter references rechecked for the existing Dream Realm settlement boundary: 468 (`Desecrated Grove`), 2263 (`Beginning of the End`) and 2273 (`Shadow Clan`). Official publication was used as the chapter-identity cross-check for the later material.

- **CANON:** Dream Realm human organizations can maintain settlements, routes, patrols and practical organized work around controlled locations while dangerous wilderness remains relevant.
- **INFERRED:** an already-authored settlement role can be given a distinct physical body and bounded informational interaction without making that body story-state authority.
- **DESIGN:** Cinder Rest, Grey Lanterns, `watch_captain`, Pillager body, iron helmet, spyglass, soul lantern, invulnerable/no-AI state, normal service readout and sneak standing-rule readout.
- **UNKNOWN:** canonical appearance, uniform, equipment, dialogue, AI, patrol routine, service mechanics, reputation mechanics, combat role, animation, voice, audio and final project art direction.
- **COMPATIBILITY:** `DreamRealmStoryContentCatalog` and `DreamRealmStoryNpcExecutionBinding` own authored identity/labels. Entity type, equipment, tags, chat and commands are removable execution/presentation only and cannot own Soul/progression, Aspect, Flaw, Attribute, Memory, Echo, Nightmare Creature, scenario, reward, Nightmare lifecycle, relationships or persistence.

## Dependency and asset decision

No runtime dependency or external asset is added. GeckoLib remains the approved custom model/animation lane, but its active creature integration is still under separate review branches; importing that stack here would duplicate active work and enlarge this direct-to-main NPC slice. This implementation therefore uses bundled vanilla-compatible resources as an explicit placeholder.

No model, texture, sound, structure or other asset is copied from another content mod or adaptation.

## Player-visible result

The Cinder Rest Watch Captain uses a stationary Pillager silhouette with an iron helmet, spyglass and soul lantern instead of the previous ordinary Villager placeholder. Normal interaction presents the existing arrival cue and Java-authored service labels. Sneak-interaction presents the existing Java-authored standing rule.

The interaction is informational only: it does not trade, award, unlock, alter reputation, grant progression or manufacture story state.

## Validation boundary

`DreamRealmStoryNpcPresentationTest` pins the non-Villager presentation, explicit vanilla equipment cues, inert/invulnerable body, Java binding consumption, service presentation and sneak standing-rule presentation while rejecting obvious progression/reward mutation seams.

Fresh exact-head hosted compile/unit/package, physical NeoForge client boot and dedicated-server boot are required before this slice is called green. Physical client review remains necessary to judge whether the vanilla placeholder reads well enough in-game; replacement requires no Java-owned identity migration.
