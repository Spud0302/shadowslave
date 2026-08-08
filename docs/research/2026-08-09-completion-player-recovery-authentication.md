# Completion player recovery evidence authentication — 2026-08-09

## Problem

Issue #34's physical runner already authenticates the selected fault boundary, child exit-86 evidence, source HEAD, world identity, successful recovery-process exit, exactly-once appraisal, and exactly-once teardown. The remaining row contract still depended on a human manually recording the same player identity, recovered Soul/appraisal state, absence of active Nightmare ownership, and a second relog.

That leaves a false-evidence gap: plausible process logs can satisfy the shell verifier without machine-authenticating that the same player converged twice after restart.

## Change

Successful-completion login recovery now emits one structured server-log marker after coordinator replay:

`COMPLETION RECOVERY EVIDENCE nightmare=<uuid> player_uuid=<uuid> appraisal_applied=<bool> active_present=<bool> in_nightmare=<bool>`

`appraisal_applied=true` is the existing `PreviewAppraisalService.isApplied(...)` predicate, so it jointly observes the expected Dreamer Soul state and exact fixed-preview identity rather than merely checking one field.

`mod/verify-completion-player-recovery.sh` requires exactly two markers for the requested Nightmare instance, the same player UUID in both, and fully converged state in both. In the physical procedure those two observations correspond to the initial reconnect and the required second relog.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, return, progression, death, Aspect, Flaw, Memory, Echo, Attribute, or Seed mechanic changes.
- **INFERRED:** none added.
- **DESIGN:** structured server-side recovery evidence and the requirement that the same technical recovery state be observed on two logins are test infrastructure.
- **UNKNOWN:** no real process-kill row is proven by this change alone; a human still has to trigger the original Nightmare completion and actually perform the two recovery logins. Filesystem/power-loss behavior beyond the existing joined persistence boundary remains unproven.
- **COMPATIBILITY:** gameplay and persistence schemas are unchanged. The retained successful-completion receipt already causes login replay; this adds only an INFO evidence marker after that replay.

## Test boundary

`NightmareCompletionRecoveryEvidenceTest` covers the exact marker format and converged-state predicate. The shell verifier has a process-free `self-test` covering the valid two-login case, mismatched player identity, and an incomplete second recovery state.

The next physical row remains `after_terminal_registry_save`. Its recovery log can now machine-authenticate the remaining same-player/state/relog observations instead of relying on handwritten notes.
