# Ashen Watch NPC execution evidence — 2026-08-10

## Scope

This slice turns one already-authored Dream Realm story NPC archetype from PR #94 into a physical Minecraft body and right-click presentation adapter. It does **not** add a new NPC/content catalogue, relationship simulation, trading economy, quest state, reward, or progression rule.

The executed identity is:

- story module: `ashen_watch` / **Ashen Watch**;
- region: `ashen_expanse`;
- settlement: **Cinder Rest**;
- faction: **Grey Lanterns**;
- authored NPC archetype: `watch_captain`.

All of those identities are resolved from `DreamRealmStoryContentCatalog`; the NeoForge adapter does not own them.

## Primary and later source checks

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked current primary chapter text plus official WebNovel chapter identity where available.

- **Chapter 468 — Desecrated Grove:** established routes, patrols, a Citadel, and human-controlled/tamed Dream Realm territory coexist with dangerous wilderness. Practical map and route knowledge materially changes safe travel.
- **Chapter 469 — Fire Keepers:** the destination shows direct signs of organized human presence and a camp in dangerous Dream Realm terrain, supporting the narrow proposition that human bodies/organizations can physically occupy and operate from Dream Realm locations.
- **Chapter 2263 — Beginning of the End:** later material explicitly discusses settlements and Citadels in Dream Realm human territories, as well as shelter/food/survival infrastructure required for large-scale habitation.
- **Chapter 2273 — Shadow Clan:** later material explicitly describes a Dream-Realm-based organization operating from a Citadel with members assigned to information gathering, threat elimination, logistics and construction.

No exact Ashen Watch, Grey Lanterns, Cinder Rest, watch-captain appearance, dialogue, trade inventory, population, relationship mechanic, or placement is claimed to come from the novel.

## Evidence boundary

### CANON

Humans and organized groups can occupy and operate from Dream Realm Citadels/camps/settlements; established routes and patrols can exist; organizations can perform practical information, threat, logistics and construction work in the Dream Realm.

### INFERRED

A physical NPC body and interaction surface are useful removable Minecraft execution concerns around an already-authored Java-owned settlement/faction/archetype identity. Presenting an authored settlement name, faction name, arrival cue and available service families does not require the body to own those facts.

### DESIGN

Ashen Watch, Cinder Rest, Grey Lanterns and the `watch_captain` are project-authored content from PR #94. The vanilla Villager body, fixed custom name, invulnerability, no-AI stance, scoreboard-style execution tags, `/shadowslave_storynpc` development command, exact chat formatting, and right-click interaction choreography are Minecraft implementation choices.

### UNKNOWN

Canonical settlement-generation or population rules; NPC appearance, profession matching and social AI; canonical dialogue; prices/trade inventories; reputation/standing arithmetic; faction membership; quest generation; resource ownership; service availability/refresh; combat behavior; persistence/respawn rules; and any relationship between settlement interaction and Soul progression, Memories, Echoes, appraisal, or Nightmare resolution.

### COMPATIBILITY

`DreamRealmStoryContentCatalog` remains the Java identity/content authority. Any future Java-owned settlement/NPC/relationship/quest record must own persistent social and world state. Villager bodies, entity tags, commands, chat, models, animations, audio, trading screens, structures, datapacks and third-party adapters may only execute or render already-authorized state. This slice therefore cancels vanilla Villager trading so the placeholder body cannot invent an economy that Java has not authorized.

## Physical implementation boundary

`DreamRealmStoryNpcExecutionBinding` is pure Java and validates that `watch_captain` is actually authored for `ashen_watch`, exposing only source-module identity and presentation fields.

`DreamRealmStoryNpcRuntime` creates an explicit **PLACEHOLDER** vanilla Villager body through a development command. The body is persistent, invulnerable and no-AI to avoid silently inventing combat/social behavior. Right-click resolves the binding and displays authored module/faction/settlement/arrival/service information. It cancels vanilla Villager interaction, so no vanilla trade inventory or reputation behavior leaks into project authority.
