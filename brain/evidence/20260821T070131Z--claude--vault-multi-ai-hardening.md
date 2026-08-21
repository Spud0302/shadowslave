---
uid: 20260821T070131Z-claude-vault-multi-ai-hardening
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T063734Z-claude-vault-multi-ai-hardening
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
captured_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
worktree_dirty: true
sources:
  - brain/tools/validate_vault.py
  - brain/tools/new_record.py
  - brain/tools/agent_brief.py
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Vault hardening for multi-AI workflows

## Claim tested

That the vault now mechanically enforces its own note schema and claim
lifecycle, and that records scaffolded by tooling satisfy that enforcement.

## Environment and method

- Windows 11, Python 3.9.13, no third-party packages installed.
- Branch `codex/combat-core-standalone`, head `7223e140d6253caa0d0a69635f76b57e3f94e0ed`.
- Worktree dirty throughout with unrelated Combat Core and Chainback work.

## Preconditions

The vault had no mechanical validation and no CI workflow referencing `brain/`.
The single `brain` match under `.github` was the substring inside
`SmartBrainLib`.

## Command or research procedure

```
python brain/tools/validate_vault.py
python brain/tools/test_validate_vault.py
python brain/tools/test_new_record.py
python brain/tools/agent_brief.py --paths brain/ai/agents/README.md
```

## Observed result

- `validate_vault.py` — 54 notes checked, 0 errors, 0 warnings, exit 0.
- `test_validate_vault.py` — 28 tests, OK, exit 0.
- `test_new_record.py` — 14 tests, OK, exit 0.
- `agent_brief.py` with a held path — collision reported, exit 1.

`test_every_kind_passes_validation` scaffolds all nine record kinds from the
migrated templates and validates the output, closing the loop between the two
tools.

## Artifacts, hashes, logs, or chapter references

No build artifacts. Tools and tests are the artifacts:
`brain/tools/validate_vault.py`, `new_record.py`, `agent_brief.py`, and the two
test modules. Session narrative including the corrections is in
[[brain/ai/logs/20260821T063734Z--claude--vault-multi-ai-hardening|the run log]].

## Defects found and fixed during this work

1. **Frontmatter block sequences parsed as empty.** A key such as `targets:`
   with its values on following `- ` lines read as `None`, so every block list in
   the vault was invisible to tooling. This made the claim collision check answer
   "clear to proceed" for a path an active claim held. Fixed, with a named
   regression test. This was the most serious defect in the work: a go/no-go
   tool returning a false all-clear is worse than no tool.
2. **Unsubstituted template scaffolding was not rejected.** A filed record could
   carry `base_commit: replace-full-sha`, naming no commit, and pass every check.
   Added a `PLACEHOLDER` error over frontmatter values.
3. **The evidence filename rule was too strict.** It flagged
   `brain/implementation/authority-drift-register.md`, which is correctly named.
   `record_kind: evidence` covers both task-scoped immutable observations, which
   carry `task_id`, and standing registers with stable names. The rule was
   corrected rather than the note renamed.

## Limitations and unperformed checks

- **Nothing here concerns runtime behaviour.** No Gradle, NeoForge, GameTest,
  client, or dedicated-server work was run. This evidence says nothing about
  Combat Core, Chainback, or preview.4.
- The P0 findings in [[brain/implementation/authority-drift-register]] —
  modpack dependency closure and the fixture-based modpack CI — are untouched.
- Validation proves schema conformance only. A note can pass every check and
  still be stale, wrong, or misclassified.
- The tools were exercised on this vault at one commit, on one Python version
  and one operating system.
- No CI job runs any of this. Enforcement is local and agent-invoked by design.

## Conclusion

The vault now rejects malformed records mechanically, scaffolds conforming ones,
and detects claim collisions and expired leases. Cross-tool compliance was
observed in practice rather than assumed: the agent `antigravity` completed a
full claim, log, evidence, and handoff cycle in this vault during this session,
and its records pass this validator.

An observation proves behaviour at one commit. It does not authorize a design,
merge, release, canon classification, or scope change.
