# Shadow Slave — current issues and limitations

**Canonical project status:** `PROJECT-STATUS.md`  
**Current main:** `96a3422c2469a832f9a977a4521cc3f3b62edc5b`  
**Current correctness edge:** PR #178 / head `a802a68cd1f0c007cef586c38a92683de87a63dc`

This file tracks current blockers/evidence gaps. Historical preview findings remain in review notes, Git history, GitHub issues and `docs/history/`.

## Issue #34 — persistence/restart recovery

Issue #34 remains the primary correctness tracker. The active lineage now includes replayable successful completion and explicit persistence verification at demonstrated first/final recovery-authority boundaries for canonical death, technical/admin exit and preview reset.

Corrected PR #178 head `a802a68cd1f0c007cef586c38a92683de87a63dc` passed **Preview Gates #164 / run `31368518381`**. Its only inline review finding is resolved; the final verifier now requires every surviving persisted Nightmare instance to pass the same production `NightmareInstance.load(...)` reconstruction that restart uses before the canonical-death marker may be consumed.

### Still unproven

- real process-kill/restart convergence at the newly guarded persistence boundaries;
- physical fsync/power-loss guarantees below readable persisted file images;
- post-verification storage corruption/failure behavior;
- complete real-player recovery matrix evidence where Issue #34 still requires it.

Do not add persistence barriers mechanically. A new checkpoint needs a demonstrated failure model in which losing the **last meaningful replay authority** can leave an unrecoverable persisted split.

## BLOCKED — prepared Nightmare world/chunk durability (#158)

The prepared-world/chunk durability audit is blocked. `SavedDataPersistence.saveAndWait(...)` does not itself prove Nightmare-dimension chunk/entity durability, while forcing a naive full save can create the opposite stale-preparation problem if rollback later occurs only in memory.

Resume only with at least one new condition: process-free reconstruction of the split/convergence target; live same-world process-kill evidence; stronger primary Minecraft/NeoForge save-path evidence; owner choice of rebuild-vs-durable-cleanup policy; dependency/code change; or another credible transaction design. Do not retry automatically without new evidence.

## Hosted frozen-datapack stall

The Nightmare-dimension generation/observation stall has repeated in hosted runs while Java jobs remained healthy. Do not blind-rerun indefinitely or increase watchdog/observation budgets again without fresh server-lag/protocol evidence. This is separate from #170's already-addressed Mineflayer transport watchdog mismatch.

## Active gameplay/content ownership

PRs #179, #181 and #182 currently own runtime historical-role assignment, the playable Drowned Bell integration, and physical Nightmare Creature execution. Correctness/documentation work must not duplicate those slices.

## Current open evidence gaps

1. Real process-kill/restart evidence for Issue #34 where still required.
2. Complete interactive Java playthrough against a selected integrated correctness/gameplay candidate.
3. Real relog persistence across Carrier, active Nightmare and completed Dreamer stages beyond synthetic reconstruction tests.
4. Two-player simultaneous Java Nightmare-instance verification.
5. Backed-up real frozen-datapack migration plus idempotent second invocation.
6. Player-facing completion/return-position verification.
7. Ordinary Nightmare death cleanup/wording and mature corpse/Gate behavior where canon evidence is still incomplete.
8. Dependency-order review/merge of the stacked correctness lineage.

## Lore/evidence rules

Follow `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md` and the relevant roadmap/research note before lore-sensitive changes.

- **CANON:** direct novel constraints only.
- **INFERRED:** reasoned synthesis, never promoted to canon for convenience.
- **DESIGN:** Minecraft/recovery implementation choices.
- **UNKNOWN:** unresolved mechanics/evidence gaps.
- **COMPATIBILITY:** technical recovery remains distinct from ordinary in-world Spell behavior; older saves remain supported unless an explicit migration says otherwise.

## Reporting/retry rule

Record defects/blockers with exact branch/commit, reproduction steps, expected vs observed behavior and logs/evidence. After two consecutive no-progress attempts on one blocker, record both attempts and the exact condition needed to resume, mark/comment it blocked, and move to the next unblocked slice.
