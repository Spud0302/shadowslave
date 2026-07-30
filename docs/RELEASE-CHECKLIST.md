# Shadow Slave datapack — release checklist

**Release-candidate baseline:** `main` at `a470b914f3e0710d3dfee63adc29b8e6e50d4599` plus the proposed changes on `gpt/datapack-release`.

**Purpose:** this is the live checklist for freezing the vanilla Minecraft 1.21.1 datapack as a completed Phase 1 artifact before Java development begins.

Historical bugs and test sweeps remain in `ISSUES.md` and `TESTING.md`. Do not use an old historical section as the current release state.

---

## Current release status

### GPT side — implemented on `gpt/datapack-release`, awaiting Claude review

- [x] Define explicit datapack "done" criteria and Java boundary.
- [x] Stop displaying `Shadow Slave` as a Flaw; use invented player-facing placeholder `Sunscorched` while preserving the legacy internal id for compatibility.
- [x] Convert the visible verification advancement tree into player-facing Shadow Slave milestones without changing advancement ids/grant points.
- [x] Remove testing-command chatter from the ordinary load message.
- [x] Rewrite README around current Sleeper progression, current project layout, known concurrency ceiling and release workflow.
- [x] Audit the Mineflayer harness for assertions that can pass on the wrong path.
- [x] Write exact machinery acceptance criteria for Claude.
- [x] Prototype a deterministic validated ZIP builder for Claude to review/replace.

### Claude machinery/testing side — release blockers

- [ ] Enforce **one active First Nightmare at a time** so the current global bossbar/creature/storage design cannot corrupt a second player's run.
- [ ] Preserve **fractional negative return coordinates** by removing the integer-scoreboard loss from the active return path.
- [ ] Strengthen high-priority harness assertions H1-H5 from `docs/reviews/2026-07-30-harness-vacuous-assertion-audit.md`.
- [ ] Deliberately break each strengthened assertion once and prove it fails for the intended reason.
- [ ] Review or replace `shadowslave/tools/build.py` against the contract in `docs/superpowers/specs/2026-07-30-datapack-release-machinery.md`.
- [ ] Run `python3 shadowslave/tools/validate.py` successfully on the merged release candidate.
- [ ] Run the full Mineflayer harness successfully at least three consecutive times.
- [ ] Build the installable ZIP from the exact release-candidate commit.

### Andrew — final human acceptance

Run this against the **built ZIP in a fresh/representative world**, not the loose source folder.

- [ ] Install ZIP; `/datapack list` sees it and world loads without data-pack validation errors.
- [ ] Ordinary load message is player-facing and contains no test-command advertisement.
- [ ] Advancement tab reads as Shadow Slave progression, not a verification dashboard.
- [ ] Fresh player reads `Mundane` in `/trigger soul`.
- [ ] First normal sleep marks Carrier but does not immediately start the Nightmare.
- [ ] Carrier calling/nausea is noticeable but not intrusive beyond the intended behavior.
- [ ] Second sleep or deliberate crouch-on-bed enters the First Nightmare.
- [ ] 90-second pre-creature phase feels like dread rather than waiting.
- [ ] Bossbar transitions correctly to the Nightmare Creature and tracks health.
- [ ] Full **win path** works: kill creature -> return -> `Sleeper (Dormant)` -> Aspect + Flaw -> advancement milestones.
- [ ] `/trigger soul` uses honest terminology and shows `Sunscorched` rather than the legacy internal `Shadow Slave` Flaw name if that roll occurs.
- [ ] Full **Cast Out path** works: near-death exit -> low-health return -> recovery sleep -> later retry.
- [ ] Full **death path** works: normal death screen, cleanup, no Cast Out grant, dropped belongings return as previously confirmed.
- [ ] `/reload` during an active trial restores/maintains visible trial state.
- [ ] Quit/rejoin during the creature fight does not award a free victory.
- [ ] `test/reset` works from both Overworld and Nightmare and leaves genuinely clean player test state.
- [ ] Test a return from deliberately negative/fractional X/Z after Claude's precision fix; no one-block truncation.
- [ ] With two players, Player B is cleanly refused while Player A has an active First Nightmare; after A exits, B can enter.
- [ ] Final fight remains acceptable at the intended preparation tier; do not retune numbers without Andrew's judgement.

---

## Intentional limitations that do NOT block this datapack release

These are ceilings of the completed Phase 1 datapack, not forgotten tasks:

- Only one active First Nightmare is supported at a time; release machinery should **enforce** this rather than pretending true instancing exists.
- The delayed death sweep can move every loose item in the Nightmare dimension. This remains acceptable only because concurrent trials are refused; real ownership belongs in Java.
- The four Aspect/four Flaw mechanical packages remain a finite Phase 1 prototype. Their presentation must be honest; unique procedural Aspect behavior and personal Flaw generation belong in the Java architecture unless Andrew explicitly expands the datapack scope.
- `Cast Out` is a gameplay safety deviation from canon, retained for the tuned datapack loop unless Andrew explicitly chooses permadeath for the final build.
- Instant-kill damage can bypass near-death ejection and produce a real death.
- Nightmare Creature is a modified vanilla mob rather than custom AI/model.
- Nightmare terrain remains deliberately limited compared with the future Dream Realm/worldgen system.
- Phase 1 ends at **Sleeper/Dormant**. The Dream Realm journey and actual Awakening are Java-era/future-system work.
- No Memories, Echoes, Soul-inventory GUI, advanced Soul Essence progression, Citadels, Gates or later Nightmares in this frozen datapack release.

A completed prototype is allowed to have a boundary. It is not allowed to hide the boundary or corrupt state when the boundary is crossed.

---

## Final freeze

The datapack is considered **complete** when:

1. every release blocker above is checked;
2. Claude has validated/tested the exact candidate and stamped the release version;
3. Andrew passes the human smoke test on the built ZIP;
4. any remaining issue is explicitly listed above as an intentional Phase 1 limitation rather than an unowned TODO;
5. a final tag/release artifact can be kept as the frozen vanilla baseline before the Java project starts.

After that point, new systems should go to the Java mod unless a critical bug is found in the frozen datapack.
