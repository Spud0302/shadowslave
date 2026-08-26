---
uid: ss-idea-combat-reference-study
record_kind: idea
authority: proposal
lore_class: "N/A"
state: proposed
owner: claude-code
created: 2026-08-26
updated: 2026-08-26
sources:
  - https://github.com/Antikythera-Studios/epicfight
  - https://github.com/ZsoltMolnarrr/BetterCombat
  - https://github.com/ZsoltMolnarrr/CombatRoll
  - https://github.com/KosmX/minecraftPlayerAnimator
  - https://github.com/bernie-g/geckolib
  - https://github.com/Tslat/SmartBrainLib
  - https://github.com/FTBTeam/FTB-Quests
related: []
tags:
  - idea
  - inbox
  - combat
  - licensing
---

# Mods worth studying for combat feel, and a code-provenance policy

Research note. Authorises nothing. No dependency is proposed here.

## Idea

Our combat gap is not the state machine — `combat-core` already implements
`IDLE -> WINDUP -> ACTIVE -> RECOVERY`, commitment, cancellation-before-commit,
one-resolution-per-window and movement reservation. The gap is **presentation**:
animation, camera and animation-driven hurtboxes, all of which sit on
`brain/design/deferred-scope`. Combat *feel* comes mostly from that half.

So the useful move is to read how existing mods solved the presentation
problem and implement our own against `combat-core`'s existing phases.

Owner framing, 2026-08-26: *"we aren't copying the code or rewriting it, we are
looking at how they have done something then writing our own."* That is the
INSPIRED category below, which carries no licence obligation.

## What we can and cannot take

Copyright protects **expression**, not **ideas**. A mechanic, a state
machine shape, a tick count and a design approach are not protected. The
specific source text is.

Practical consequence: reading a GPL-3.0 mod and then writing our own
implementation of the same mechanic is fine and is how this ecosystem works.
The risk is *substantial similarity* — reproducing another project's class
decomposition, method breakdown and naming closely enough that ours reads as a
transcription. Retyping rather than pasting does not change that.

Working discipline that keeps us clearly on the right side:

1. read the source and take notes on **behaviour and numbers** — phase
   durations, transition conditions, curve shapes, what cancels what;
2. close the file;
3. implement from the notes against our own `combat-core` seams.

Parameters are facts. "Wind-up is four ticks" is not copyrightable.

### Provenance classes

Mirrors `docs/LORE-SOURCE-POLICY.md`, which already stops us laundering an
assumption into canon. Same apparatus, applied to code:

- **OWN** — written here, no external reference.
- **INSPIRED** — behaviour learned by reading another project; implementation
  written from notes. No obligation. Record what was read anyway.
- **ADAPTED** — source actually derived from another project. Requires a
  compatible licence plus attribution. Needs owner approval before it lands.
- **DEPENDENCY** — linked as a JAR, not copied. Governed by the licence for
  *redistribution*, which matters because we bundle JARs.

Most of what follows should end up INSPIRED.

## Verified licences

Read from each project's own repository on 2026-08-26. Not from search
summaries — one search result asserted Better Combat was MIT, and the
repository says otherwise. Verify before relying on any row.

| Project | Licence | Notes |
| --- | --- | --- |
| Epic Fight | **GPL-3.0 (code)**, **All Rights Reserved (assets)** | README splits them explicitly. Assets under `src/main/resources/assets` may not be redistributed. Read freely; never vendor code or assets. |
| Better Combat (ZsoltMolnarrr) | **NOASSERTION / custom** | GitHub reports "Other" — a custom notice, not MIT. Treat as All Rights Reserved for copying. |
| Combat Roll (ZsoltMolnarrr) | **GPL-3.0** | Read-only for us. |
| PlayerAnimator (KosmX) | **MIT** | The one permissive animation option. |
| GeckoLib | **MIT** | Already our dependency; manifest row correct. |
| SmartBrainLib | **MPL-2.0** | Already our dependency; manifest row correct. File-level copyleft. |
| FTB Quests | LGPL-family, **unverified** | Search indicated LGPL-2.1; not confirmed against the repo. Confirm before relying on it. |
| L_Ender's Cataclysm | **unverified** | No LICENSE file found via the API at `AuraSwordsmithRush/l-enders-cataclysm`. Absence of a licence means All Rights Reserved by default. |

Note the pattern: **assets are consistently stricter than code**, including in
otherwise open projects. Animations, models and sounds are the category to be
most careful with — and animation is exactly what we need.

## Study list, mapped to our gaps

### Combat feel — the actual gap

- **Epic Fight** (GPL-3.0 code) — the closest thing to what we want:
  soulslike movesets, battle-mode camera, dodge roll. Study how it binds
  animation phases to combat state and how the third-person combat camera is
  driven. Do not vendor. NeoForge 1.21.1 builds exist, which also makes it a
  live *adopt-instead-of-build* option — but see the conflict below.
- **PlayerAnimator** (MIT) — a real candidate to *use*, not just read, since
  the licence is permissive and its whole purpose is animating the player
  without fighting other mods.
- **Better Combat** (custom licence) — read-only. Worth studying for swing
  arcs and multi-target reach, which is adjacent to our `MeleeGeometry`.
- **Combat Roll** (GPL-3.0) — small and focused. Best single reference for
  dodge i-frames and how invulnerability windows interact with commitment.

### Encounter design — the rank-ceiling problem

Making a Sovereign *feel* like one needs rank-matched opposition, not more
player power. Study boss mods for arena-scale encounter structure and
multi-phase state:

- **L_Ender's Cataclysm** — licence unverified; read only after confirming.
- **Bosses of Mass Destruction** — repository not located this pass. Worth a
  second look; strong reputation for readable, telegraphed boss patterns.

### The Spell's voice

- **FTB Quests** — a questbook is a diegetic text channel, and the Nightmare
  Spell already speaks in formal rune-text (`Rank: Aspirant`,
  `Dreamer Sunless`). Rendering progression as the Spell addressing the player
  is near-free fidelity and a solved pattern.

## Architectural conflict to decide first

`combat-core` owns action timing, commitment and recovery **by design**.
Epic Fight owns exactly the same territory. They cannot both own it.

`modpack/manifest.json` now declares `combat_core` a required component at an
exact version, with NeoForge refusing to load the pack without it. We have in
practice already chosen **build** over **adopt** — without recording it as a
decision. That should become an explicit decision record either way.

If build stands, combat feel is our content problem, and the next step is not
more state-machine work: it is making one Chainback exchange readable through
animation, using GeckoLib, which we already depend on.

## Repository gaps found while checking

1. **No `LICENSE` at the repository root.** Our own code therefore defaults to
   All Rights Reserved. Worth a deliberate choice. Note we can license our
   code but not the setting — the Shadow Slave IP is not ours.
2. **`validate_manifest.py` requires a licence string but never checks its
   value** (`require_text(component.get("license"), ...)`). `"license":
   "banana"` passes today. An allowlist of redistribution-compatible
   identifiers would turn a documentation field into a real gate, matching the
   fail-closed style of `check_dependency_closure.py`. This is the change that
   would catch an All-Rights-Reserved mod entering the bundled pack.
3. **`packaging.include` is only `README.md` and `manifest.json`.** No
   third-party licence notices ship in the archive. The bundled JARs carry
   their own notices internally so we are probably fine, but a generated
   `THIRD-PARTY-LICENSES.md` would make it unambiguous.

Gap 2 matters most: we *bundle* JARs, so redistribution — not code reuse — is
our real licence exposure. Both current third-party components (MIT, MPL-2.0)
are redistribution-friendly. That stops being true the first time an
All-Rights-Reserved mod is added.

## Why it may be valuable

Reading before building is cheaper than discovering the same constraints
through playtesting, and the provenance classes cost nothing to apply now
while being very hard to reconstruct later.

## Canon and terminology risks

None. No lore claim is made here.

## Open questions for the owner

1. Build or adopt for combat? The manifest has effectively answered "build";
   should that become a decision record?
2. What licence for our own code?
3. Implement the manifest licence allowlist?
