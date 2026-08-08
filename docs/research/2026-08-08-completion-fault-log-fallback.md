# Completion fault evidence log fallback

**Scope:** Issue #34 physical successful-completion fault harness.

## Finding

`run-completion-fault-row.sh fault` deliberately accepted the exact intentional-halt marker from either Gradle's captured console or Minecraft's retained `latest.log`. The later `verify` stage, however, required that marker specifically in `fault-console.log`.

That made the evidence contract internally inconsistent: a row could pass the fault-collection stage and later fail verification without any gameplay, persistence, or restart defect, solely because ModDev routed the Minecraft child log line to `latest.log`.

## Change

Verification now accepts the exact fault marker from the same two retained sources as collection. The synthetic self-test places the marker only in `fault-latest.log`, while appraisal/teardown markers remain in the console evidence, proving the fallback path is intentional rather than dead code.

The exactly-once appraisal/teardown counters are unchanged and continue to count only the Gradle console captures, avoiding double-counting the same Minecraft log line when `latest.log` mirrors console output.

## Evidence classification

- **CANON:** unchanged; no Nightmare, appraisal, progression, death, or return mechanic changes.
- **INFERRED:** none added.
- **DESIGN:** accepting one exact process-fault marker from either retained ModDev output surface is test-harness behavior.
- **UNKNOWN:** no physical process-kill row is newly proven by this change; real dedicated-server/player restart evidence remains outstanding.
- **COMPATIBILITY:** rows whose marker is already present in `fault-console.log` behave exactly as before. Rows previously accepted from `fault-latest.log` now remain verifiable instead of failing at a later harness stage.

No novel rule is introduced or generalized.
