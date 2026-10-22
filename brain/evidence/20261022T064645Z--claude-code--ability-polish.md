---
uid: 20261022T064645Z-claude-code-ability-polish
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T064644Z-claude-code-ability-polish
created: 2026-10-22
updated: 2026-10-22
branch: claude/ability-polish
captured_commit: 7548028ed9fb5c5c7989964cfeaedd2e0986b68e
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Ability icons, casting poses and effects

## Claim tested

Each kit ability shows its icon, plays its casting pose for the caster and onlookers, and has visible effects; the Flaw tab shows the Flaw icon.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/ability-polish, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that checked PAL loaded the animations, gave the player each kit in a fresh world, used each ability in third person and took screenshots, then opened the Soul screen tabs.

## Observed result

build: BUILD SUCCESSFUL, 272 unit tests, 0 failures (new: AbilityAssetsTest for icon files and pose names; ready-cue and icon assertions). Game tests: "All 11 required tests passed :)". Headless client: PAL listed shadowslave:cast_kindle, cast_mist_passage, cast_holdfast and cast_returning_tide; screenshots show the Kindle arms-up burst with the flame ring, the Mist trail, Holdfast's crossed-arm brace, Returning Tide's reach, the Tide icon in the HUD slot and on the Abilities tab, and the Tidal Debt icon on the Flaw tab.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. Sounds were not heard (no audio device). Other players seeing the pose was not tested with two clients; it uses the same payload path the caster's own client used.

## Conclusion

Icons, casting poses and effects work in a real client.
