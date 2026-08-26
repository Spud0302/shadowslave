---
uid: 20260826T051740Z-claude-code-animation-timing-validator
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code
task_id: 20260826T051740Z-claude-code-animation-timing-validator
created: 2026-08-26
updated: 2026-08-26
branch: claude/shadow-slave-github-connect-x6nqra
worktree: primary
base_commit: 888a6ee2b98084fd01cbbff3bae76892def22129
lease_until: 2026-08-26T09:17:40Z
targets:
  - mod/tools/validate_animation_timing.py
  - mod/tools/animation-timing.json
  - mod/tools/test_validate_animation_timing.py
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Animation/action timing validator

## Owner intent

Owner request, 2026-08-26: build the animation/action timing validator proposed
in `brain/inbox/combat-reference-study`.

## Exact scope

Combat Core owns action timing in ticks; GeckoLib owns clip length in seconds.
Nothing connected the two, so an animation could finish before the hit it was
supposed to telegraph with no build, test or lint noticing. That desync is
invisible in source review and surfaces only as combat that does not read.

New Python tool, its binding file and its tests. No change to any Java source,
any animation asset, or any workflow.

The tool cannot guess which clip telegraphs which action, so bindings are
declared explicitly. An action that is deliberately not animated is declared
`unbound` with a reason, which keeps a real content gap visible instead of
silently passing.

Tick counts are read from the Java sources and deliberately not duplicated in
the binding file — duplicating them would let the two drift, which is the exact
bug the tool exists to catch.

## Acceptance criteria

- Resolves named int constants across files (the Glass Road shape, where the
  constants live in a different class from the constructor call).
- Ignores commented-out definitions and Combat Core's own test fixtures.
- Detects the known Chainback mismatch.
- Tests pass and each rule is covered by a test that fails without it.

## Findings

Three shipped actions were parsed:

| Action | ticks | seconds | animation |
| --- | --- | --- | --- |
| `shadowslave:chainback_displacement` | 12/1/8 = 21 | 1.05 | `attack.strike` at 0.50s |
| `shadowslave:glass_road/clean_edge` | 10/1/16 = 27 | 1.35 | none authored |
| `basic_melee` (Combat Core) | 4/1/6 = 11 | 0.55 | vanilla swing, out of scope |

**Chainback is off by 11 ticks.** Its clip ends roughly two ticks before the
active window opens, so the creature completes its whole swing, then stands
still through the hit and all eight recovery ticks. The telegraph does not
telegraph the thing it exists to telegraph.

**Glass Road commits the player for 1.35s with nothing shown on the player**,
which reads as input lag. Declared `unbound` with that reason rather than
passed over.

Also observed while working, outside this claim: all three entity models render
with vanilla placeholder textures (`spider.png`, `silverfish.png` twice) and the
mod has no `textures/` directory at all. Named `PLACEHOLDER_TEXTURE` in source,
so this is honest rather than broken.

## Explicit exclusions

- **Not wired into CI.** The tool exits 1 on the current tree because the
  Chainback mismatch is real, so adding it to a workflow now would ship red CI.
  Wiring is a separate change, after the mismatch is resolved. Precedent for
  where it goes exists: `java-core.yml` already runs Python unittest discovery.
- **The Chainback animation was not retimed.** Rescaling a 0.5s strike to 1.05s
  makes it play at less than half speed, which is a visual authoring judgement
  belonging to the owner and to Blockbench, not to a tool that cannot see the
  result. Reporting it is in scope; silently changing how it looks is not.
- No Java source, animation asset, model or texture was modified.

## Dependencies and overlaps

Follows `brain/inbox/combat-reference-study`. No other active claim names these
paths. `mod/tools/` is a new directory; no README, matching `modpack/tools/`.

## Coordination notes

Verified only that the tool reports correctly. Nothing here is physical proof
about how any animation looks or feels in a running client.

## Closure

Closed 2026-08-26 by claude-code. Tool, bindings and 18 tests delivered and
passing; the Chainback mismatch is reported, not fixed. Two decisions are left
with the owner: retime the clip (or author per-phase clips), and then wire the
check into CI.
