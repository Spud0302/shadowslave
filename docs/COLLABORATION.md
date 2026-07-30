# Collaboration protocol — Claude and GPT via git

Two AI agents work on this repository in separate sessions that **cannot see each other**. There is
no shared chat, no messaging, no memory between us. The repository is the only channel.

That is the premise everything below follows from: **if it isn't committed, it wasn't said.**

Proposed by GPT in `docs/reviews/2026-07-30-gpt-code-review-followup.md`, accepted and extended by
Claude in `docs/reviews/2026-07-30-claude-reply-to-gpt-followup.md`, at Andrew's instruction to use
git as the channel between agents.

---

## Who owns what

**Revised 2026-07-30 by Andrew.** The split follows a practical constraint, not a judgement about
capability: Andrew is **not token-limited with GPT**, so design conversation happens there, where it
is cheap to iterate. Claude's budget is spent on implementation and verification instead — which is
also the half GPT cannot do, since the connector environment has no Minecraft server.

|            |                                                                                                                                              |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| **Andrew** | Owns the project and every decision. Playtests. **Gives most instructions to GPT.**                                                          |
| **GPT**    | Design, specs, canon research, code review, **and writing code** — see the split below. Works on `gpt/*` branches. Does not write to `main`. |
| **Claude** | Implementation, **and testing everything either agent writes**. Works on `main`. Reviews and merges `gpt/*`. Stamps versions. Ships builds.  |

Branch prefix `gpt/` identifies GPT's work. `main` is Claude's working branch and the release branch.

### GPT writes code too — split by file, not by role

**Andrew's call, 2026-07-30, with a reason worth recording: GPT has the better grasp of the novel.**
That is true and it matters, because a large part of what remains to build _is_ canon — Aspects,
Flaws, ranks, Memories, the wording the Spell uses. An agent that has read the source should write
that content rather than describe it to one that hasn't.

An earlier draft of this document said GPT should stop writing pack code. That was overruled, and the
concern behind it — two agents editing the same files — is better solved by splitting the **files**
than the roles:

| Belongs to GPT                                                                  | Belongs to Claude                                                                        |
| ------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Lore-derived content: Aspect and Flaw definitions, names, effects, descriptions | The state machine: `nightmare/enter`, `leave`, `eject`, `survive`, `tick`, `tick_player` |
| `prototype/roll_aspect_flaw.mcfunction` and whatever replaces it                | Guards, choke points, teardown ordering, cooldown and threshold logic                    |
| Player-facing copy and the Spell's voice                                        | `testserver/harness.mjs`, `shadowslave/tools/validate.py`                                |
| Progression semantics (`progression/`), rank meaning, canon naming              | Dimension, worldgen, macro/storage plumbing                                              |
| Canon research and design docs                                                  | Version stamping, packaging, release                                                     |

Neither list is a fence. It is about where each agent's advantage lies, and it exists so that a
change arriving from the other side is a surprise about _content_, not a merge conflict in a file
someone else was mid-way through.

**Two things do not move, regardless of who is writing:**

1. **Claude runs the validator and the harness before anything merges.** Not a status thing — GPT's
   environment has no Minecraft server, so it cannot test what it writes. This has already mattered:
   GPT's review branch was correct, but verifying it turned up a harness assertion that had been
   passing for the wrong reason.
2. **The invariants in `ENGINEERING-NOTES.md` bind both agents.** Guard at the choke point; absent
   scores fail `matches`; player NBT is read-only; an assertion you have never seen fail is not an
   assertion. Those came from bugs, not preference, and three separate bugs have come from the
   choke-point rule alone.

**Reviewing remains GPT's job as well.** It found a bug from Andrew's very first play session that
had survived nine releases.

### Practical conflict avoidance

- Check `origin/main` before starting and say which commit you branched from — see rule 2.
- Prefer small, focused branches over one branch that touches everything.
- Name the files you touched in the branch description, so Claude can tell a content change from a
  machinery change without reading the whole diff.
- If work genuinely needs a file from the other agent's column, say so explicitly in the branch
  rather than editing it quietly.

### The risk this introduces, and the mitigation

Andrew's instructions now reach Claude **second-hand**. Where GPT writes the code, that is fine —
GPT heard the conversation. Where GPT writes a brief for Claude to build, the ambiguities that would
have been a ten-second question become guesses instead, and a confident wrong guess in this codebase
is invisible: see `ENGINEERING-NOTES.md`.

Two mitigations, both obligations rather than suggestions:

1. **A brief must carry intent, not just requirements** — see the next section.
2. **`docs/OPEN-QUESTIONS.md`** is where Claude logs questions that block or shape implementation,
   because Claude's questions need a route back that does not depend on being relayed through a chat.
   GPT and Andrew answer there.

There is a second-order version of this now that both agents write code: **whoever did not write a
change cannot see the reasoning behind it.** That is what the commenting standard is for. A comment
saying what a line does is close to worthless; one saying what it protects against, or what was tried
first and failed, is the only durable way to stop the other agent "simplifying" a guard whose purpose
is invisible. It has already happened once — a rule against absent-score filters was written in the
README _and_ two lines above the offending selector, and the bug still shipped.

---

## What a brief needs for Claude to implement it without guessing

Applies when GPT is specifying work for Claude rather than writing it directly. Not a template to
fill in mechanically — these are the things whose absence has caused rework.

- **The intent behind the requirement.** _Why_ Andrew wants it, in a sentence. This is the single
  most useful line in any brief: it is what lets an implementer resolve an ambiguity correctly
  instead of picking a plausible reading. When the countdown was cut from five minutes to ninety
  seconds, the reason — "it read as waiting rather than dread" — mattered more than the number.
- **Exact values.** Numbers, strings, thresholds, message copy. If a value is Claude's call, say so
  explicitly; otherwise Claude will treat an omission as an oversight and ask.
- **Acceptance criteria** — how to know it works, stated observably. Ideally what a harness
  assertion should check, since that is the gate on every release.
- **What must NOT change.** Named files, behaviours, or invariants to leave alone. This repo has a
  list of deliberate compromises in `ENGINEERING-NOTES.md` and a position on each deferred concern in
  `docs/reviews/2026-07-30-claude-reply-to-gpt-followup.md`; a spec that would overturn one of those
  should say so on purpose rather than by implication.
- **The baseline commit** the spec was written against — rule 2 below.

Specs go in `docs/superpowers/specs/` (existing convention) or `docs/lore-research/` when they are
canon-derived design.

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
| Questions awaiting an answer    | `docs/OPEN-QUESTIONS.md` — two open at `v1.4.9`                                                            |
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
