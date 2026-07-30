# Datapack release checklist

<!-- completed-release-gate-status -->
> **DATAPACK GATE COMPLETED.** `datapack-v1.0.0` is released and frozen. Use this checklist only
> when rebuilding the frozen release or accepting a genuine datapack maintenance change. Java and
> modpack releases have separate gates; current Java verification is Issue #16 and `TESTING.md`.

This is the gate for the completed pre-Java datapack. A release number is a consequence of the evidence below, not a substitute for it.

## Every release

Run from the repository root:

```bash
python3 shadowslave/tools/validate.py
cd testserver && npm test
cd .. && python3 shadowslave/tools/build_release.py
```

Required:

- validator: clean;
- `npm test`: both the main lifecycle harness and deterministic Flaw-family harness clean;
- release builder: succeeds and prints the ZIP SHA-256;
- `pack.mcmeta`, load banner and `test/selfcheck` version agree after Claude stamps the release;
- CHANGELOG records what actually shipped and any known limitations discovered while testing.

Do not weaken an assertion merely to make a release pass. This project has repeatedly had tests that were wrong while the pack was right.

## Human checks after gameplay/presentation changes

Use the generated ZIP in a fresh Minecraft **1.21.1** world.

### Fresh install

- pack appears in `/datapack list`;
- `/reload` has no datapack errors;
- `/function shadowslave:test/selfcheck` succeeds;
- Shadow Slave advancement tab renders;
- custom Nightmare dimension can be entered.

### Natural player loop — no skip commands

1. Start Mundane; `/trigger soul` says Mundane.
2. Sleep once; become Carrier without entering the Nightmare on that same sleep.
3. `/trigger soul` says Carrier/marked.
4. Observe at least one normal Carrier call.
5. Enter by normal sleep or deliberate bed interaction.
6. Let the 90-second countdown run naturally at least once.
7. Confirm the presentation beats fire once and are readable rather than intrusive.
8. Fight the Nightmare Creature normally.
9. Win and return to the correct Overworld location.
10. Confirm the appraisal text matches the earned Flaw family rather than looking like an unrelated random roll.
11. `/trigger soul` shows Sleeper (Dormant), generated Aspect identity and generated Flaw identity.
12. Confirm the Aspect and Flaw mechanics remain active during ordinary post-Nightmare play.
13. Sleep as a Sleeper; do not re-enter the First Nightmare.

### Failure paths

- Reach the low-health ejection path at least once: return alive, gear intact, Cast Out presentation visible, re-entry blocked until recovery sleep.
- Die in the Nightmare at least once: no Cast Out advancement/presentation, return/respawn sane, dropped belongings recover at the bed as documented.
- Recovery sleep clears the cooldown and does not itself pull the player back in.

### Flaw generation

Automated test hooks prove each selected family's mechanics. Human testing still owns the classifier itself.

At minimum before `1.0.0`:

- observe baseline family from a successful trial with no strong signal;
- observe bloodied classification by reaching the tracked near-collapse window during the creature phase and then winning;
- observe fled classification by opening the tracked distance before the leash and then winning;
- exercise hunger classification deliberately at least once; if producing the six-point food delta in a natural fight is too awkward, record that as a balance/usability finding rather than silently accepting an effectively unreachable family;
- verify multi-signal precedence remains fled > hungry > bloodied.

The formal name may differ between runs; the family/mechanical burden is what the observed behaviour selects.

## Pre-`1.0.0` document audit

These must describe the same current reality:

- `README.md`
- `CHANGELOG.md`
- `ISSUES.md`
- `TESTING.md`
- `docs/PRE1-ROADMAP.md`
- `docs/JAVA-HANDOFF.md`
- `docs/ENGINEERING-NOTES.md`

Historical specs/reviews are not rewritten; their status banners must make supersession obvious.

## `0.9.x` release-candidate freeze

After the RC begins:

- no Memories;
- no Dream Realm / Awakening stage;
- no Soul Core system;
- no Gates;
- no custom GUI;
- no custom AI/entity rewrite;
- no elaborate command-based multiplayer instancing;
- no new Aspect/Flaw catalogue architecture.

Only bug fixes, balance corrections, presentation corrections, test improvements, documentation and packaging belong after the freeze.

## `1.0.0` PROUD gate

The PROUD bump is justified when:

- all automated gates pass repeatedly;
- the fresh-world natural loop passes;
- failure/recovery paths pass;
- generated identity feels coherent in play;
- the Flaw classifier has been exercised rather than only force-tested;
- no known Phase-1 blocker remains;
- all remaining limitations are explicitly Java-bound rather than accidental unfinished datapack work;
- the Java handoff contract accurately describes the final stored/runtime state.

At that point, build and archive the `1.0.0` ZIP, freeze datapack feature development, and begin the Java port from that tagged behaviour.
