---
uid: 20261022T024241Z-claude-code-dream-candle-roll
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T023123Z-claude-code-dream-candle-roll
created: 2026-10-22
updated: 2026-10-22
branch: claude/dream-candle-roll
captured_commit: b4ccf430038bdb5b9bdef3556ead2169c7afe146
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Dream Candle ritual and roll verification

## Claim tested

The Dream Candle ritual rolls an Aspect from the four kits on waking, only when a lit Dream Candle is near the bed, and only once.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/dream-candle-roll before commit (dirty worktree on base b4ccf43).

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, `TIMEOUT_SECONDS=240 ./mod/verify-smoke.sh server-restart`, and `xvfb-run -a ./mod/gradlew -p mod runClientSmoke`.

## Observed result

build: BUILD SUCCESSFUL, 249 unit tests, 0 failures (AspectRollerTest 4, PlaystyleEvidenceTest 2, SoulRitualTransitionTest 3 new). Game tests: "All 4 required tests passed :)" (lit candle rolls and is consumed; unlit candle and no candle leave sleep vanilla; a second ritual does not reroll). Server smoke: "SMOKE GATE PASSED". Client smoke reached blocks and gui atlases with no model or texture errors logged for shadowslave.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. The game test sets the sleep counter by reflection instead of waiting a real night. The candle textures were not looked at in a rendered world.

## Conclusion

The roll trigger and roller behave as specified at this worktree state.
