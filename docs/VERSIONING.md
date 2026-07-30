# Versioning policy — datapack completion and Java handoff

**Decision owner:** Andrew  
**Recorded by:** GPT  
**Date:** 2026-07-30  
**Baseline:** `main` at `a470b914f3e0710d3dfee63adc29b8e6e50d4599`

## Decision

Keep **Pride Versioning** (`PROUD.DEFAULT.SHAME`). The policy change is not a move away from PrideVer; it is a correction to what the `PROUD` field means for this project.

The datapack is still the unfinished implementation of the initial idea. Therefore, while it remains in active framework/prototype development, **PROUD stays at `0`**.

- `0.DEFAULT.0` — a meaningful datapack development milestone we are happy with, but which is still part of completing the initial idea.
- `0.DEFAULT.SHAME` — fixes to that development milestone.
- **`1.0.0` — the first release we are proud to call the completed datapack framework.** This is the datapack-to-Java handoff baseline.

The intended meaning of `1.0.0` is concrete: the datapack is a good enough complete, playable vertical slice that its behaviour can serve as the reference implementation/specification for turning Shadow Slave into an actual Java mod.

## Example progression

```text
0.5.0  New Aspect/Flaw framework
0.5.1  Fix generated names
0.5.2  Fix Flaw behaviour tracking

0.6.0  Nightmare presentation overhaul
0.6.1  Fix Spell message timing

...

0.9.0  Datapack release candidate
0.9.1  Playtest fixes
0.9.2  Playtest fixes

1.0.0  Completed datapack framework / Java handoff baseline
```

The exact DEFAULT numbers are not a promised roadmap. Skip numbers or combine milestones when appropriate. The important rule is that ordinary substantial datapack development increments DEFAULT while PROUD remains `0`; fixes increment SHAME; the completed datapack earns `1.0.0`.

## What “completed datapack” means

`1.0.0` does **not** mean every eventual Java-mod feature has been forced into commands first. It means the datapack has proved the initial playable framework well enough to translate into proper mod architecture.

At minimum, the reference loop should be coherent and polished:

```text
Mundane
  -> infection / Carrier
  -> Spell interaction
  -> First Nightmare entry
  -> playable trial, failure and victory
  -> Sleeper progression
  -> personal Aspect framework
  -> personal/behaviour-influenced Flaw framework
  -> persistent status and usable post-Nightmare gameplay
```

Systems whose real implementation benefits from Java — full Dream Realm progression, advanced Nightmare instancing, Memories, Echoes, Gates, later ranks, sophisticated multiplayer ownership, and similar expansion — are not prerequisites merely to make the datapack number reach `1.0.0`.

The datapack should establish the **behavioural contracts** Java must preserve, rather than trying to make command implementations that will immediately be thrown away.

## Existing `1.0.0` through `1.4.9` history

Do **not** rewrite, delete, or retag the existing history. Those releases remain valid historical prototype/development artifacts produced under the earlier interpretation of the version policy.

Going forward, the project deliberately resets the active development line below `1.0.0`. The changelog/release notes should make this explicit so the numerical decrease cannot be mistaken for a rollback.

Suggested wording:

> **Versioning policy reset:** earlier `1.x` builds were prototype/development releases and did not represent completion of the initial datapack concept. The project remains on Pride Versioning, but the `PROUD` field is reset to `0` until the datapack is complete enough to become the Java mod's reference implementation. Historical releases and tags are preserved unchanged.

This is a policy correction, not an attempt to falsify history.

## Relationship to the existing collaboration rules

Claude still owns version stamping, packaging and releases on `main`. GPT still leaves the three runtime version files alone on `gpt/*` branches unless Andrew explicitly changes that workflow.

The only superseded part of the old policy is the implicit assumption that the current `1.x` line represents a completed/proper `PROUD=1` generation. It does not.

When Claude accepts this decision, the next release should begin the new `0.x.x` development line at the DEFAULT value appropriate to the next meaningful datapack milestone. Andrew's examples used `0.5.0` for the Aspect/Flaw framework, but the exact next DEFAULT number is secondary to the semantics above.

## Why

The version number should communicate project maturity as well as release history. Calling the current prototype `1.x` makes `1.0.0` meaningless as a milestone even though the original datapack idea has not yet reached its finished playable state.

Under this interpretation of PrideVer, `1.0.0` becomes useful again: **we are proud to call the datapack complete, freeze it as the reference implementation, and begin the Java transition.**
