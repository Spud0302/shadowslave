# Shadow Slave datapack `1.0.0` release record

**Release product:** vanilla Minecraft Java Edition datapack  
**Minecraft:** `1.21.1` (`pack_format` 48)  
**Release tag:** `datapack-v1.0.0`  
**Package:** `shadowslave-v1.0.0.zip`

## Release decision

Andrew approved the first public datapack release on 30 July 2026 after reviewing the completed `0.7.3` baseline and confirming that it looked ready to ship.

`1.0.0` is a release stamp over the already-tested `0.7.3` gameplay. No gameplay commands, balance values, progression rules, dimensions, Aspects, Flaws or Nightmare lifecycle behaviour were changed while stamping the release.

## Recorded automated evidence

Claude's final verified deployment recorded:

- static validator clean;
- lifecycle harness: **32/32**;
- deterministic Flaw harness: **39/39**;
- combined result stable across three confirmed `0.7.3` deployments;
- Q4 closed by retiring the unreliable Weightless attribute implementation and replacing it with the retreat-derived Burdened/Slowness family.

The tag-triggered GitHub workflow must validate and build the `1.0.0` ZIP again before publishing it. A tag/package version mismatch fails the workflow.

## Completed behavioural baseline

The frozen datapack proves:

```text
Mundane
  -> first ordinary sleep
Carrier
  -> later sleep or deliberate bed interaction
First Nightmare
  -> central conflict resolved
Sleeper / Dormant
  -> generated Aspect identity
  -> behaviour-derived Flaw identity
  -> persistent Soul readout
```

It also provides centralised Nightmare entry checks, one teardown route, death/ejection handling, deterministic test/reset tools and an explicit datapack-to-Java migration contract.

## Installation contract

Players download `shadowslave-v1.0.0.zip` and place it unchanged at:

```text
<world>/datapacks/shadowslave-v1.0.0.zip
```

Then reopen the world or run `/reload`.

No mod loader and no client-side installation are required. Multiplayer clients may remain completely vanilla. Difficulty must be Easy or higher; cheats are needed only for testing commands.

## Deliberate limits

This release is the completed datapack vertical slice and Java behavioural baseline, not the complete future mod. The following begin in Java rather than being extended through fragile commands:

- real per-player/multiplayer Nightmare instances;
- Dream Realm progression and actual Awakening;
- typed Soul/Core state;
- Memories, Echoes and Gates;
- custom entities and AI;
- full Aspect and Flaw generation;
- dedicated Soul GUI and networking.

## Next phase

After this release, datapack work is limited to serious compatibility or release defects. Feature development moves to the two-track Java experiment:

1. Nightmare Spell modpack using selected existing mods plus the canonical Shadow Slave core;
2. standalone NeoForge Shadow Slave mod using the same core contracts.

Co-Authored-By: ChatGPT <gpt@openai.com>
