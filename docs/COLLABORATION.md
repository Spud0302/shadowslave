# Collaboration protocol — Claude and GPT via git

Two AI agents work on this repository in separate sessions that **cannot see each other**. There is
no shared chat, no messaging, no memory between us. The repository is the only channel.

That is the premise everything below follows from: **if it isn't committed, it wasn't said.**

Proposed by GPT in `docs/reviews/2026-07-30-gpt-code-review-followup.md`, accepted and extended by
Claude in `docs/reviews/2026-07-30-claude-reply-to-gpt-followup.md`, at Andrew's instruction to use
git as the channel between agents.

---

## Who owns what

|            |                                                                                                                                                             |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Andrew** | Owns the project and the decisions. Playtests. Both agents check with him rather than deciding for him — see the list at the end of `ENGINEERING-NOTES.md`. |
| **Claude** | Works on `main`. Reviews and merges `gpt/*` branches. Stamps versions. Runs the validator and the harness.                                                  |
| **GPT**    | Works on `gpt/*` branches. Does not write to `main` unless Andrew says otherwise.                                                                           |

Branch prefix `gpt/` is how GPT's work is identified. `main` is Claude's working branch and the
release branch.

---

## The rules

### 1. Reviews and disagreements are artifacts, not messages

When either agent disagrees with implementation, architecture, tests, canon, or the other's
decision, write it down in the repo — `docs/reviews/` for code, the relevant research or design doc
otherwise. Classify it as one of:

- **bug** — behaviour is wrong
- **prototype compromise** — knowingly imperfect, with a named ceiling
- **canon deviation** — disagrees with `docs/lore-research/`
- **design preference** — neither is wrong

That classification is not bureaucracy. It is the difference between "fix this" and "leave this
alone", and conflating them is how deliberate compromises get "fixed" into new bugs.

Include evidence or a reproduction where one exists.

### 2. State the baseline commit in every artifact

Not just the version — the commit SHA, and the branch it describes.

Concurrent agents make repository state expire fast. In one two-day stretch, a GPT review's `v1.4.8`
baseline went **three releases stale** before its follow-up landed. A document that names its
baseline announces its own staleness; one that doesn't quietly misleads.

Corollary, for the reader: **never assume a baseline is still current.** Compare refs before
substantial work and before stating anything about repository state.

### 3. Attribute your commits

GPT's commits arrive through Andrew's GitHub account as `author=Spud0302` with no trailers, so git
cannot distinguish them from Andrew's own. Once a branch is merged and deleted, the branch prefix —
the only remaining signal — is gone too.

- **GPT:** add `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- **Claude:** adds `Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>` (already does).

A channel whose value is a durable record of who concluded what needs to record who concluded it.

### 4. Nothing merges without the validator and the harness

```bash
python3 shadowslave/tools/validate.py     # static: structure, references, project rules
cd testserver && node harness.mjs         # behavioural: 25 assertions on a real server
```

Both exist because **almost every way a datapack breaks is silent** — see `ENGINEERING-NOTES.md`.
A branch that looks correct has told you nothing.

GPT is not expected to run these (no server in the connector environment). Say so in the branch, and
Claude runs them at review. GPT's first review branch did exactly this and it worked well.

### 5. Claude records merges where GPT will read them

Claude's obligation, because Claude merges. On merging a `gpt/*` branch:

- annotate the branch's own review doc with the outcome and the released version, **in place**
- keep the merge commit message specific about what was accepted, changed, or deferred

This is so "was my branch taken, and is my baseline current?" is answerable from the repository
instead of from a conversation GPT cannot see.

### 6. Version numbers are stamped at merge, by Claude

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`, tag first then bump SHAME per
round of fixes. The next number depends on what else landed first, which a branch author cannot know.

**GPT: leave the version files alone.** A branch arriving pre-stamped just has to be re-stamped.
Three files must agree and the validator enforces it — see `ENGINEERING-NOTES.md`.

### 7. Mark historical, don't rewrite — and annotate in place

Superseded documents are labelled, not edited. They record what was true when written, and rewriting
them falsifies the record.

But a follow-up the reader may never open does not fix a misleading original. When a document's
status goes stale, **annotate the stale line in place** with a pointer to the resolution. Both
review documents in `docs/reviews/` show the pattern.

### 8. Deferred concerns get recorded once, with the reason

A known ceiling that keeps being rediscovered wastes both agents' time. When a concern is
deliberately not fixed, record the decision and _why_ — see the table in
`2026-07-30-claude-reply-to-gpt-followup.md` and the compromises table in `ENGINEERING-NOTES.md`.

Mark shortcuts in code with `ponytail:` (Claude) or `PROTOTYPE-LIMIT:` (GPT). Either is fine.
Always name the ceiling **and** the upgrade path — never just "temporary".

---

## Where to look first

| You want                        | Read                                                                                                       |
| ------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Why the code is shaped this way | **`docs/ENGINEERING-NOTES.md`** — every convention with the bug that caused it. Read before changing code. |
| Current GPT session state       | `GPT_HANDOFF.md` (root) — GPT's living checkpoint                                                          |
| Canon                           | `docs/lore-research/` — with per-answer confidence labels                                                  |
| What shipped and why            | `CHANGELOG.md` — every version mapped to the issue it fixed                                                |
| Known issues and compromises    | `ISSUES.md`                                                                                                |
| Current behaviour               | `README.md` — the only doc that must always be current                                                     |
| Test plans                      | `TESTING.md` — live section at the bottom; earlier sections marked HISTORICAL                              |

---

## The point

> The aim is not for GPT and Claude to agree automatically. The aim is for disagreements to become
> durable, reviewable engineering decisions.
>
> — GPT, `2026-07-30-gpt-code-review-followup.md`

Kept because it is right. The cross-review has already produced two findings the other agent had
missed or got wrong: an uninfected player could enter a nightmare (reported by Andrew in the very
first play session, and unfixed for nine releases), and Claude's absent-score validator check was
presented as Minecraft syntax truth when it is a project policy. A channel that only produced
agreement would have surfaced neither.
