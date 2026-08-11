# Cinder Rest lantern-ring physical presentation — 2026-08-11

## Scope

This slice makes one already-authored Cinder Rest arrival cue physically present and inspectable in the bounded Dream Realm preview. It does not add settlement authority, standing mutation, service execution, progression, rewards, Nightmare resolution, or persistence.

## Repository authority checked

`DreamRealmStoryContentCatalog.ashen_watch` already authors Cinder Rest, the Grey Lanterns, the SHELTER/RUMORS/SCOUTING service set, the standing rule, and the arrival cue describing a ring of hooded lamps behind a scavenged black-stone wall. `DreamRealmStoryNpcExecutionBinding` already resolves those values into a removable execution binding. The new lantern binding consumes those existing Java-owned values rather than duplicating or deriving story state from blocks.

`DreamRealmPreviewService` already builds the bounded Cinder Rest shelter and places the Watch Captain. The five new soul-lantern positions are therefore local physical presentation coordinates only.

## Primary/later lore check

Research followed `docs/LORE-SOURCE-POLICY.md`.

- Chapter 468, **Desecrated Grove**, was rechecked through the owner-designated NovelFull access layer. It describes human-tamed Dream Realm territory, an established route to a Citadel, and that route being patrolled by organized human forces.
- Chapter 2263, **Beginning of the End**, was rechecked through official WebNovel. It explicitly refers to human territories in the Dream Realm and to a settlement/Citadel in Godgrave under organized leadership.
- Official WebNovel's catalogue was used to cross-check the later chapter identity/publication context.

These sources support organized human-controlled locations/routes in the Dream Realm. They do **not** establish Cinder Rest, Grey Lanterns, a universal warning-lamp convention, this geometry, or these materials.

## Evidence boundary

- **CANON:** humans can establish controlled Dream Realm locations/Citadels and organized routes/patrols; later human territories and settlements in the Dream Realm exist.
- **INFERRED:** an already-authored frontier settlement may receive readable physical boundary/warning infrastructure without making that infrastructure story-state authority.
- **DESIGN:** Cinder Rest, Grey Lanterns, the `ashen_watch` module, the hooded warning-lamp ring, five exact lamp coordinates, polished-blackstone-brick-wall posts, soul-lantern material, inspection wording, and the use of the existing service/standing text at the landmark.
- **UNKNOWN:** canonical Cinder Rest existence, Grey Lanterns, exact settlement architecture, warning-light practices, materials, colors, lamp count/placement, service interaction, standing/reputation mechanics, patrol simulation, custom models/audio/VFX, and final art direction.
- **COMPATIBILITY:** `DreamRealmStoryContentCatalog` / `DreamRealmStoryNpcExecutionBinding` remain Java authority for story identity and authored labels. The block geometry, NeoForge right-click event, vanilla materials, coordinates, and chat presentation are removable execution/presentation only and cannot grant services, mutate standing, resolve scenarios, award progression, or own persistence.

## Dependency / asset decision

No dependency is added. Minecraft/NeoForge native block placement and interaction events solve this bounded landmark cleanly. GeckoLib remains the approved model/animation lane but provides no material benefit for a static five-post landmark. SmartBrainLib, Curios, Veil, and TerraBlender are not adopted.

No third-party content-mod, novel, adaptation, model, texture, sound, or structure asset is copied. The current soul lantern and polished-blackstone-brick wall are bundled vanilla resources used as clearly replaceable presentation materials.

## Player-visible result

Entering the current Dream Realm preview builds five soul lanterns on dark wall posts around the Cinder Rest refuge front. Right-clicking one of those five lamps identifies Cinder Rest / Grey Lanterns, presents the Java-authored service labels, and repeats the Java-authored standing rule. The pre-existing interior soul lantern is intentionally not part of this interaction.

## Deliberate limits

- no service execution, trade, shelter state, scouting state, rumor generation, reputation/standing mutation, quest/event acceptance, rewards, progression, or persistence;
- no custom block/model/texture/audio/VFX;
- no dynamic lamp state, route-warning state, patrol AI, redstone behavior, or settlement simulation;
- exact geometry/materials/text remain replaceable DESIGN.

Fresh exact-head compile/unit/package, physical NeoForge client boot, dedicated-server boot, and frozen-datapack compatibility gates are required before this slice is called green.
