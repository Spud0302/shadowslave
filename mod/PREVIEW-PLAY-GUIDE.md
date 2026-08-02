# Shadow Slave Java playable preview

**Build line:** `0.1.0-preview.2`  
**Minecraft:** Java Edition 1.21.1  
**Loader:** NeoForge 21.1.244  
**Status:** merged and Claude-verified development preview; Andrew play feedback pending; not a public release

This build exists so Andrew can install the Java mod in an ordinary local game and feel the current systems. It is a coherent vertical slice, not a complete Shadow Slave game.

## Install

1. Install Minecraft Java Edition **1.21.1**.
2. Install NeoForge **21.1.244** for that instance.
3. Copy `shadowslave-0.1.0-preview.2.jar` into the instance's `mods` folder.
4. Remove the older `preview.1` JAR if present; do not load both.
5. Do not install the vanilla Shadow Slave datapack for a fresh preview world. The JAR includes its own Java systems and Nightmare dimension.
6. Start Minecraft and create a new test world. A separate disposable world is strongly recommended.

The mod has no required third-party gameplay dependencies.

Verified JAR checksum:

```text
48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

## First play loop

1. Join the world.
2. Press **O** to open the read-only Soul screen.
3. Run:

   ```text
   /shadowslave preview_begin
   ```

   This is a clearly labelled development shortcut. It grants Carrier state and begins the preview First Nightmare; it is **not** presented as the canonical cause of infection.
4. You enter **The Last Signal** as the **last watchkeeper** of a ruined road.
5. Follow the road toward the far watch while a corrupted pursuer closes in.
6. Find the unlit soul campfire and **right-click it**. Killing the pursuer is optional; resolving the historical conflict is the objective.
7. You return to your original location as a Dreamer/Sleeper with Dormant Soul Rank.
8. Press **O** again. The screen should show:
   - Aspect: **Last Light**;
   - Aspect Rank: **Awakened**;
   - Ability: **Kindle**;
   - Flaw: **Cold Ash**.
9. Run:

   ```text
   /shadowslave kindle
   ```

   Kindle grants a short burst of visibility and speed, then enters a server-owned cooldown.
10. Enter water, rain, or a bubble column to feel **Cold Ash** apply Weakness.

## Player commands

| Command | Purpose |
| --- | --- |
| `/shadowslave soul` | Detailed server text readout |
| `/shadowslave soul_screen` | Open the Soul screen without the key |
| `/shadowslave preview_begin` | Development onboarding: infect if fresh, then enter The Last Signal |
| `/shadowslave nightmare_enter` | Enter the preview Nightmare when already a Carrier |
| `/shadowslave nightmare_status` | Show active instance, role, and slot |
| `/shadowslave nightmare_recover` | Technical recovery from a broken/stuck instance; not in-world mercy |
| `/shadowslave kindle` | Activate the Last Light preview ability |
| `/shadowslave preview_reset` | Abort active preview state and return to Uninfected |

Operator-only commands remain available for architecture and migration testing.

## Frozen datapack migration

On a copied world containing the released datapack's legacy scores/tags, an operator may run:

```text
/shadowslave migrate_datapack
```

The importer:

- reads legacy evidence without changing it;
- rejects explicit zero or inconsistent scores;
- requires completed players and generated identity scores to retain the tags the frozen datapack writes with them;
- translates through the tested pure mapping layer;
- writes Java Soul, Aspect, and Flaw records provisionally;
- reads them back and verifies exact identity;
- writes the migration marker only after verification;
- rolls Java state back on failure;
- retains every legacy score and tag.

Always test migration on a backup. This preview does not perform legacy cleanup.

## Recovery and death

Ordinary First-Nightmare failure is treated in the domain as death. Minecraft still presents its normal respawn flow because this is a development preview; the mod explicitly labels that as an accommodation, not a safe ejection granted by the Spell.

`/shadowslave nightmare_recover` is an administrative recovery path for technical faults. It returns the player to Carrier state and is deliberately described out of world.

## What is canon and what is designed

- The progression boundaries, assigned historical role, reconstructed conflict, appraisal boundary, and separation of canonical death from technical recovery follow the accepted lore gate.
- **The Last Signal**, its watchkeeper, road, soul campfire, and pursuer are project **DESIGN**.
- **Last Light**, **Kindle**, **Cold Ash**, and the fixed preview appraisal are project **DESIGN**.
- The project does not claim that canon provides an algorithm that would produce those identities.

See `docs/PREVIEW-LORE-DECISIONS.md` for the full ledger.

## Machine evidence already passed

Claude verified the merged result:

- validator clean;
- datapack lifecycle 32/32;
- Flaw suite 39/39;
- disconnect/reconnect trial-lock regression passed and exited cleanly twice;
- Java build 35 tests, 0 failures;
- physical client and dedicated server smokes passed.

That evidence proves startup, persistence codecs, lifecycle invariants, and test harnesses. It does not decide whether the game feels good.

## Known limitations

- One handcrafted First Nightmare only.
- The environment is a generated development arena inside a bundled dimension, not a complete historical settlement or procedural Nightmare system.
- The pursuer is a vanilla Husk placeholder with vanilla AI.
- No temporary historical body, role inventory, provisional Aspect, or bespoke creature AI yet.
- No natural infection/exhaustion trigger yet; preview onboarding is a command.
- No corpse Gate is created after failure.
- One fixed Aspect and Flaw outcome; appraisal is not procedural.
- Kindle uses vanilla effects as a temporary execution layer.
- Imported identities are preserved, but full mechanics for every imported Aspect/Flaw are not yet reimplemented in Java.
- Issue #29 remains a low-severity corrupt-save codec follow-up; normal gameplay cannot create its negative cooldown input.
- The complete loop has not yet been judged by Andrew in a real client.

## Feedback checklist

Record concrete observations rather than only “good” or “bad”:

- Did **O** open a readable Soul screen at each progression stage?
- Was the role/conflict understandable without reading repository documentation?
- Did reaching and lighting the signal feel like resolving a situation rather than killing a boss?
- Was the pursuer threatening, irritating, or irrelevant?
- Did return position and state feel reliable?
- Did Last Light/Kindle feel like one coherent supernatural nature?
- Was Cold Ash mechanically noticeable and thematically connected enough?
- Did the distinction between development shortcut, technical recovery, and in-world outcome remain clear?
- Did state survive quitting and re-entering the world?
- What should be kept, replaced, or expanded before the next preview?
