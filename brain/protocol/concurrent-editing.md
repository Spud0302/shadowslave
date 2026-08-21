---
uid: ss-protocol-concurrent-editing
record_kind: protocol
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Andrew
recorded_by: Claude
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/protocol/ai-collaboration.md
  - AGENTS.md
  - brain/inbox/multi-ai-vault-optimization.md
tags:
  - protocol
  - multi-ai
  - concurrency
---

# Concurrent editing — what is normal here

> [!important] Read this before concluding something has gone wrong
> **Several AI agents work in this vault at the same time. This is intended.**
> Another agent holding a claim on your file, a note changing while you read it,
> or an edit failing because the text moved are all *normal operating
> conditions*, not errors, not conflicts to escalate, and not evidence that
> anything is broken.
>
> Owner direction, 2026-08-21: *"since this is a multi-AI workflow there will be
> times where different AIs will be working on the same file concurrently and
> that's fine."*

Do not stop, ask for permission, revert anyone, or file an incident because you
encountered another agent. Read the table, act, continue.

## You are not in an error state

| What you observe | What it means | What to do |
| --- | --- | --- |
| An active claim names a file you need | Another agent is working there | Proceed. Co-edit under the rules below and note the overlap in your own claim |
| `agent_brief.py` reports concurrent work | Coordination information | Proceed. It exits 0 on purpose |
| A file changed since you read it | Someone is mid-task | Re-read, then make a targeted edit |
| Your edit failed — the old text no longer matches | Someone edited that region | Re-read and reapply. Do not force a whole-file write |
| A claim exists that your earlier check missed | Claims are not locks and cannot be | Proceed. Record the overlap |
| A lease has expired | The claim needs tidying | Set `state: expired` with a dated closure note, or ask the owner to extend |
| Validation is red and you did not cause it | Another agent is mid-flight | Re-run. If it persists, fix forward — do not revert their work |
| Another agent was given your exact task | Expected; Andrew dispatches in parallel | Append your own attributed section. Do not rewrite theirs |
| Another agent reached a different conclusion | Legitimate disagreement | Record both and raise it to Andrew. Do not silently overwrite |

## What is actually a problem

Short list, and none of it is about concurrency itself:

1. **Rewriting a shared file wholesale.** This is the only common action that
   destroys another agent's work, and it does so silently — no error, no
   conflict marker. Use targeted edits or append a section.
2. **Reflowing, renumbering, or reformatting a region you did not author.** Once
   written, this is indistinguishable from an overwrite.
3. **Editing another agent's one-writer record** — their claim, run log,
   evidence, handoff, or agent card.
4. **Reverting another agent's change** because you disagree with it.
5. **Renaming or moving notes during parallel work**, which breaks links and
   every in-flight edit at once.
6. **Editing a Canvas concurrently.** These are high-conflict JSON arrays where
   line-level merges do not work. One writer at a time.

## How to co-edit safely

- **Append an attributed section** headed with your agent name and the date,
  rather than interleaving into someone else's prose.
- **Re-read immediately before writing.** A copy read minutes ago may be stale.
- **Make the smallest edit that does the job.** A replaced paragraph merges; a
  regenerated document does not.
- **Claim the narrowest paths** that cover your work, so you do not hold files
  you never touch.
- **Record overlaps in your own claim** as you find them. That is how the next
  agent learns what happened.

## Claims are awareness, not locks

A claim announces who is working where and on what. That is what makes safe
co-editing possible — you can see who to coordinate with. It does not reserve
paths, and it cannot: a claim filed seconds before yours may not be visible when
you look. On 2026-08-21 two claims on the same file were filed thirteen seconds
apart and neither agent's check saw the other.

Where genuine sequencing matters, the earliest timestamp wins. It is the only
ordering every agent computes identically without coordination.

## If work is lost

Know this before it happens: **`brain/` is not currently tracked in git**, so
there is no commit history to recover an overwritten note from. Recovery options
are, in order:

1. **Obsidian File Recovery** — the core plugin is enabled and keeps periodic
   local snapshots of changed notes.
2. **OneDrive version history** — right-click the file in OneDrive.
3. **The run logs and handoffs** of whoever wrote the lost content, which often
   describe it well enough to reconstruct.

**Recommendation for Andrew:** committing `brain/` would give real history,
diffs, and merges for the vault. It is his call, and no agent should commit
without being asked.

## Why the vault is built this way

Serialising agents would be simpler and much slower. The design accepts
concurrent work and moves the safety into three places instead:

- **Write mode** — targeted edits merge; whole-file writes destroy.
- **One-writer records** — claims, logs, evidence, handoffs, and agent cards are
  never contended because each has exactly one author by construction.
- **Immutability with supersede chains** — corrections add records rather than
  editing history, so two agents can disagree without either losing.

Shared documents stay collaborative. Records stay owned. History stays additive.

## Related

- [[brain/protocol/ai-collaboration|AI collaboration protocol]]
- [[brain/protocol/authority-model|Authority model]]
- [[brain/ai/agents/README|Agent registry]]
- [[brain/inbox/multi-ai-vault-optimization|Vault optimization proposals]]
- `AGENTS.md` — the operational entry point
