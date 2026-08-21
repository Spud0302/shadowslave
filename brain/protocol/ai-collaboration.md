---
uid: ss-protocol-multi-ai
record_kind: protocol
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - docs/COLLABORATION.md
  - GPT_HANDOFF.md
  - docs/OPEN-QUESTIONS.md
  - AGENTS.md
  - brain/tools/README.md
tags:
  - protocol
  - multi-ai
  - git
---

# AI collaboration protocol

## The repository is the communication channel

Decisions, scope, evidence, limitations, conflicts, and handoffs must be written to versioned files or linked issues and pull requests. Chat context alone is not durable project state.

## Before an agent writes

1. Read [[brain/protocol/authority-model|the authority model]].
2. Run `python brain/tools/agent_brief.py`. It refreshes branch, head, and
   worktree status, lists active claims and what they hold, reports expired
   leases and stale derived notes, and validates the vault. Exit 1 means resolve
   something before writing.
3. Load the smallest suitable note from [[brain/ai/context/README|context packets]].
4. Run `python brain/tools/agent_brief.py --paths <paths you will touch>` to
   detect overlap with an active claim. Conceptual overlap still needs judgement;
   the tool only sees paths.
5. Run `python brain/tools/new_record.py claim --agent <slug> --slug <task>`,
   which produces the required `YYYYMMDDTHHMMSSZ--agent--short-slug.md` name,
   uid, dates, git context, and lease.
6. Record target paths, excluded paths, branch/worktree, lease, and acceptance criteria.

Claim the narrowest paths that cover the work. A claim over a whole directory
blocks agents who only need one file in it, and shared append-only indexes such
as [[brain/ai/agents/README|the agent registry]] are written by every agent that
registers.

Read-only research does not need a claim unless it may cause another agent to rely on a shared mutable output.

## Ownership and conflict rules

- One writer owns each claim, run log, evidence record, and handoff.
- One topic belongs in one Markdown note; avoid giant shared scratchpads.
- Do not edit another agent's active claim, log, evidence, or handoff.
- Do not rename or move notes during parallel work.
- Home, protocol notes, accepted decisions, and shared canvases are maintainer-owned.
- An agent that needs a correction creates a new record with supersedes rather than rewriting history.
- Check claimed file paths before editing; coordinate explicitly before overlapping them.
- Preserve unrelated dirty-worktree changes.
- Never put secrets, credentials, private account data, or substantial copied novel text in the vault.

## Concurrent editing

Several agents work in this vault at once. **Two agents editing one shared file
concurrently is expected and supported** — owner direction, 2026-08-21. The
protocol's job is to make that safe, not to serialise it.

[[brain/protocol/concurrent-editing]] is the reference for what is normal and
what is not. Send an agent there when it encounters another agent's work and
suspects something has gone wrong.

The hazard is the write mode, not the file. Targeted edits and appended sections
merge cleanly; a whole-file rewrite silently discards concurrent work, and
neither agent observes an error. OneDrive can also resolve a conflict by picking
a side.

For shared documents — indexes, inbox proposals, design, lore, and
implementation notes:

- Never rewrite the file wholesale. Edit a specific known region or append.
- Append your own attributed section rather than interleaving into another
  agent's prose.
- Edit only text you authored. Reflowing, renumbering, or reformatting another
  agent's region is indistinguishable from an overwrite once it lands.
- Re-read immediately before writing; a copy read minutes ago may be stale.

One-writer records are the exception and stay exclusive: claims, run logs,
evidence, handoffs, and agent cards. Canvas files are high-conflict JSON and keep
to one writer at a time.

## Claims and leases

**A claim is awareness, not a lock.** It tells other agents who is working where,
which is what makes safe co-editing possible. It does not reserve paths, and an
overlap is a coordination signal rather than a stop.

- `agent_brief.py --paths` reports overlaps. Proceed under the rules above and
  record the overlap in your own claim.
- The check cannot be a lock. A claim filed seconds before yours may not be
  visible when you look — this happened on 2026-08-21, thirteen seconds apart.
  Re-check immediately before the first write.
- Where genuine sequencing matters, the earliest timestamp wins.
- An expired lease means the claim needs tidying, not that its work was invalid.
  Set `state` to `expired` with a dated line in its Closure section naming who
  expired it and why. Change only state and closure note; scope, evidence, and
  history stay immutable.
- If the work is plausibly still in progress, ask the owner to extend instead.
- Expiring a claim does not adopt, validate, or inherit the unfinished work,
  which still needs its own evidence.

## While working

- Keep a task-specific append-only run log when work spans multiple sessions or agents.
- Record assumptions as assumptions.
- Link to exact source paths instead of duplicating large documents.
- Keep CANON, INFERRED, DESIGN, UNKNOWN, and COMPATIBILITY distinct.
- Do not broaden scope because an adjacent idea is interesting.
- Tie all pass/fail claims to exact commands and artifacts.

## Handoff contract

A handoff is a new immutable file containing:

- owner intent and exact scope;
- base and head commits, branch, and worktree state;
- files changed;
- observable acceptance criteria;
- verification performed and exact results;
- artifact or evidence links;
- unperformed manual checks;
- known correctness, lore, balance, presentation, and security risks;
- explicit deferred scope;
- the next safe action.

The receiving agent creates its own claim and later handoff. It does not rewrite the incoming handoff to mark it consumed.

## Questions and disagreements

Use [[docs/OPEN-QUESTIONS]] for owner-facing questions that must remain part of repository history. An agent may proceed under a reversible, documented assumption when safe. If different assumptions would materially change the result, stop and request owner direction.

## Canvas rule

Canvas files are views, not authority. They are high-conflict JSON arrays, so one maintainer edits a shared Canvas at a time. Knowledge belongs in linked Markdown notes where backlinks and line-level merges work.
