# Preview Gates PR-update trigger restoration

## Scope

This note records a bounded CI/testability correction for the active Issue #34 completion-recovery stack. It changes no gameplay or lore-facing mechanic.

## Finding

The repository's `Preview gates` workflow was configured with:

```yaml
pull_request:
  types:
    - ready_for_review
    - reopened
```

The active GPT pull requests are normally created directly as non-draft pull requests and then receive follow-up commits while review is ongoing. GitHub therefore has no matching `opened` trigger for the initial PR and no `synchronize` trigger for later head updates. This explains the repeated observation that exact new PR heads had no registered workflow run even though they changed `mod/**` or the workflow itself.

The completion-fault runner also contains a process-free `self-test` that checks point validation, stale evidence invalidation, retained log fallback, recovery-process authentication, exactly-once marker counting, and duplicate teardown rejection. Before this slice, that self-test was not part of the hosted Java gate.

## Correction

`Preview gates` now listens to:

- `opened`;
- `synchronize`;
- `ready_for_review`;
- `reopened`.

The Java job also executes:

```bash
bash mod/run-completion-fault-row.sh self-test
```

after Gradle build and before the physical NeoForge client/server boots.

This makes future new PRs and head updates eligible for the same compile/unit/package/client/server/datapack gate, while also preventing a broken evidence runner from silently riding through the Java job.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, progression, death, return, Aspect, Flaw, Attribute, Memory, Echo, or Seed rule changes.
- **INFERRED:** none added.
- **DESIGN:** CI event selection and inclusion of the process-free completion-fault self-test in the hosted gate are repository test infrastructure choices.
- **UNKNOWN:** this does not itself prove that GitHub-hosted runners are available or that a future workflow run will pass; the real six same-world process-kill/restart rows remain unexecuted physical evidence.
- **COMPATIBILITY:** push-to-`main`, manual dispatch, existing ready-for-review/reopened behavior, Gradle build, physical client/server smoke, and datapack gates remain intact. The change only adds missing PR lifecycle triggers and one process-free shell test step.

## Limitation

A green hosted workflow still does not substitute for Issue #34's physical restart matrix. The shell self-test validates evidence-processing logic only; it does not launch Minecraft or prove process-kill convergence.
