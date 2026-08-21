---
uid: ss-brain-tools
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/protocol/note-schema.md
  - brain/protocol/ai-collaboration.md
tags:
  - multi-ai
  - tooling
  - validation
---

# Vault tools

## validate_vault.py

Mechanical enforcement of [[brain/protocol/note-schema|the note schema]] and the
lifecycle rules in [[brain/protocol/ai-collaboration|the collaboration protocol]].

```bash
python brain/tools/validate_vault.py
```

Zero third-party dependencies, Python 3.8+. Exit `0` clean, `1` on any error,
`2` on bad invocation.

| Option | Effect |
| --- | --- |
| `--changed-only` | validate only notes git reports changed or untracked |
| `--format json` | machine-readable findings, for agent use |
| `--strict` | treat warnings as errors |
| `--root PATH` | validate a vault other than the enclosing one |

### What it checks

**Errors** — required properties, enum values for `record_kind`, `authority`,
`lore_class`, and `state`, ISO date format and ordering, `uid` uniqueness,
filename conventions, claim placement, claim `lease_until` presence and expiry,
`supersedes` resolving to a real `uid`, unsubstituted template scaffolding, and
frontmatter this parser cannot represent.

**Warnings** — unresolved wikilinks, unknown commit hashes, `captured_commit`
behind HEAD on a *living* note, `derived_from` sources newer than the note that
summarises them, handoffs whose verification section names no command, oversized
context packets, broken Canvas node references, and advisory placement.

### The parser refuses to guess

`parse_frontmatter` handles the flat scalar and block-list subset this vault
uses. It returns an error, rather than a best guess, on a nested mapping or a
duplicate key.

This is deliberate. An earlier version parsed a block list as empty, and the
claim collision check consequently reported "clear to proceed" for a path an
active claim held. A safety tool that answers confidently and wrongly is worse
than one that refuses to answer, so anything ambiguous is now a hard failure.

### Freshness

Declare a summary's inputs in `derived_from`, using source uids. The validator
warns when a source's `updated` date is newer than the derived note's, which
turns the dependency chain into something checkable. Immutable records — closed
evidence, handoffs, accepted decisions — are exempt, because they describe a
moment and are not expected to track their sources.

The same rule governs `captured_commit`: only a living note can be stale.

### Why local rather than CI

`PROJECT-STATUS.md` records that recent Preview Gates runs failed before any
repository step executed because no hosted runner was allocated. A CI-only gate
could therefore sit unexecuted indefinitely. Every agent runs this directly
instead — before writing, and again before filing a handoff. Adding a CI job
later is compatible with this, not a replacement for it.

### Two things it deliberately does not do

- **It does not repair notes.** Findings are reported for a writer to resolve. A
  tool that silently rewrote records would break the immutability rules it exists
  to protect.
- **It does not judge content.** A note can pass every check and still be wrong,
  stale, or misclassified. Passing validation is not authority. See
  [[brain/protocol/authority-model|the authority model]].

## new_record.py

Scaffolds a conforming record from a template.

```bash
python brain/tools/new_record.py claim --agent claude --slug combat-tuning
```

| Kind | Destination | Filename |
| --- | --- | --- |
| `claim` `log` `handoff` `evidence` | `brain/ai/*`, `brain/evidence` | `YYYYMMDDTHHMMSSZ--agent--slug.md` |
| `decision` | `brain/decisions` | `ADR-YYYYMMDD-slug.md` |
| `idea` `lore` `feature` `context` | `brain/inbox`, `brain/lore`, `brain/design`, `brain/ai/context` | `slug.md` |

`--task-id` is required for `handoff`, `evidence`, and `log`, because those
records must name the claim they belong to. `--dry-run` prints instead of
writing, and an existing file is never overwritten.

The tool **derives uid, filename, dates, and git context itself rather than
trusting the template.** Templates still carry Obsidian `{{date}}` placeholders
that only expand inside Obsidian, and their uid patterns predate the conventions
the vault settled on — see
[[brain/decisions/ADR-20260821-template-placeholders|the proposed template ADR]].
Deriving the fields means this tool emits conforming records regardless of how
that decision lands.

It gets the mechanical fields right. Only a writer can get the content right, so
run the validator afterwards.

## test_validate_vault.py

```bash
python brain/tools/test_validate_vault.py
```

42 tests, each constructing a throwaway vault containing exactly one violation
and asserting the matching code is reported. Run this after changing validation
rules — a check that cannot be observed failing is not evidence that the vault is
clean.

Every check has both a positive and a negative case, because a validator that
only ever passes also passes a smoke test.

## test_new_record.py

```bash
python brain/tools/test_new_record.py
```

14 tests against a real temporary git repository. The load-bearing one is
`test_every_kind_passes_validation`: every record kind the scaffolder emits must
satisfy `validate_vault.py`. That closes the loop between the two tools, so a
template drifting out of conformance surfaces here rather than in a filed record.

## build_manifest.py

Generates a deterministic, sorted, provenance-stamped `brain/manifest.json` indexing all notes, metadata, titles, and summaries.

```bash
python brain/tools/build_manifest.py
python brain/tools/build_manifest.py --check
```

Runs `test_build_manifest.py` to verify sorting determinism and drift detection.

## query_vault.py

Fast CLI tool for AI agents and maintainers to query notes and inspect active claims without manually parsing YAML files.

```bash
python brain/tools/query_vault.py --kind design
python brain/tools/query_vault.py --tag combat-v1
python brain/tools/query_vault.py --active-claims
python brain/tools/query_vault.py --search "Chainback" --json
```

Runs `test_query_vault.py` for query test coverage.
