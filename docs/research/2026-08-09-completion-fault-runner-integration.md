# Completion fault runner integration — 2026-08-09

## Scope

This slice ports the deterministic Issue #34 physical successful-completion evidence runner onto the current restart-recoverable runtime lineage after PR #106 activated terminal and login recovery.

It deliberately adds no new successful-completion transaction state and no new player-facing lore mechanic. The purpose is to make the existing six durability boundaries executable and auditable before further persistence design is attempted.

## Repository evidence checked

- current `main` status/handoff/issues documents;
- `docs/LORE-SOURCE-POLICY.md`;
- `docs/JAVA-LORE-ALIGNMENT.md`;
- `docs/NIGHTMARE-SEED-ROADMAP.md`;
- Issue #34 and its latest integration notes;
- PR #106 live terminal/login activation;
- historical runner PR #86;
- historical retained-`latest.log` correction PR #90;
- current `NightmareCompletionFaultInjector`, `PreviewAppraisalService`, `ServerNightmareCompletionOperations`, and `NightmareService` log contracts.

## Integration findings

### 1. Isolated ModDev run remains valid

`completionFaultServer` uses its own game directory and forwards `-PcompletionFault=<point>` to the Minecraft child as `shadowslave.completionFault=<point>`. Ordinary client/server/smoke/data runs remain unchanged.

### 2. Six points are now live

The current transaction exposes:

1. `after_terminal_registry_save`
2. `after_appraisal_player_save`
3. `after_appraisal_registry_save`
4. `after_return_player_save`
5. `after_return_registry_save`
6. `after_teardown_registry_save`

The terminal point was not part of the oldest physical matrix document but is now wired by the live Last Signal success path.

### 3. Retained latest.log fallback must remain symmetric

Historical PR #90 fixed a harness false negative: collection already accepted the exact intentional-halt marker from either Gradle console output or Minecraft `latest.log`, while verification checked console only. The current runner preserves the corrected two-source contract for the fault marker.

Appraisal and teardown counts intentionally remain console-only to avoid double-counting the same Minecraft line if `latest.log` mirrors console output.

### 4. Historical teardown marker was stale against the rebuilt runtime

Static integration found a new current-lineage mismatch before review. The historical runner counted:

```text
Nightmare <id> teardown completed
```

but the live `ServerNightmareCompletionOperations` successful path now logs:

```text
Nightmare <id> successful-completion teardown completed
```

Porting the historical pattern unchanged would make every otherwise-correct physical row fail verification. The integrated runner now counts the exact current successful-completion marker, and its synthetic duplicate-teardown test uses the same string.

This is a test-harness integration correction, not a gameplay change.

## Evidence classification

- **CANON:** unchanged. No Nightmare ending, appraisal, return, progression, death, Aspect, Flaw, Memory, Echo, Attribute or Seed rule changes.
- **INFERRED:** none added.
- **DESIGN:** isolated ModDev server run, six named process-fault boundaries, exit code 86 evidence handling, retained-log layout, exact log-marker counting and the synthetic shell self-test.
- **UNKNOWN:** no same-world physical process-kill/restart row is proven by committing this runner; launcher diagnostic wording may still differ in the actual ModDev environment; real player recovery state and second-relog idempotency remain operator evidence.
- **COMPATIBILITY:** ordinary game runs are unchanged; the fault property is forwarded only by the explicitly selected completion-fault run; verification now matches the current live successful-completion teardown log instead of the obsolete historical marker.

## Validation boundary

The shell self-test is designed to run without Minecraft and covers:

- one valid fault point;
- misspelled-point rejection;
- fault marker present only in retained `fault-latest.log`;
- one appraisal plus one current successful-completion teardown passing;
- duplicate successful-completion teardown failing.

This connector-only environment cannot execute the repository shell/Gradle commands locally. Hosted CI is evidence only if GitHub registers a run for the exact PR head.

## Next evidence slice

Do not add another speculative completion transaction layer first. Execute the first real physical row, preferably `after_terminal_registry_save`, because it simultaneously validates:

- ModDev property forwarding into the child JVM;
- the actual Gradle child-exit-86 diagnostic shape;
- same-world restart after `Runtime.halt(86)`;
- login precedence from retained terminal receipt;
- exactly-one appraisal and successful-completion teardown;
- second-relog idempotency.

If the row fails, preserve the exact console/latest.log evidence and change only the demonstrated defect. If execution is unavailable, keep Issue #34 blocked on physical evidence rather than treating the runner itself as a PASS.
