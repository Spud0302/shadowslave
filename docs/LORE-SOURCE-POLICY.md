# Lore source policy

**Status:** mandatory research rule for every lore-sensitive design, implementation, review and migration decision.

**Owner direction recorded:** 2026-08-02  
**Applies to:** GPT, Claude and any future contributor or agent working on Shadow Slave systems.

## Read this before lore-sensitive work

Do not implement, rename, generalise or document a Shadow Slave mechanic from memory.

The project owner has designated the following full-chapter access source for current novel research:

- <https://novelfull.com/shadow-slave.html>

Treat it as the working access layer for reading the novel through the latest available chapters. Check its current chapter listing at the start of a research task rather than copying a supposedly current chapter number into project documentation.

## Source hierarchy

Use sources in this order:

1. **Novel chapter text** — authority for mechanics, terminology, progression, metaphysics and chronology. Use the owner-designated NovelFull archive above as the working full-chapter access layer.
2. **Official WebNovel publication** — use to verify official wording, chapter identity or publication details where practical.
3. **Officially released adaptation material** — visual and staging reference only where it does not contradict the novel.
4. **Miraheze and Fandom wikis** — indexes, cross-references and research aids. Trace important claims back to chapters before building on them.
5. **Repository lore decisions** — accepted interpretations and project constraints, provided they remain compatible with stronger evidence.
6. **Project design** — invention chosen for a playable Minecraft system and explicitly labelled as such.

The host is not the authority; the novel text is. A community summary must not override a directly supported chapter reading.

## Copyright and quotation rule

Use the chapter archive for research, not redistribution.

- Summarise facts, rules, relationships and relevant events.
- Do not copy or commit chapters or long passages.
- Quote only a short phrase or sentence when exact wording is necessary.
- Prefer chapter references and paraphrase over pasted prose.
- Do not add downloaded novel files or scraped chapter collections to this repository.

## Evidence labels

Important lore statements in implementation plans, data definitions and reviews must be labelled or clearly treated as one of:

- **CANON** — directly supported by novel chapter text;
- **INFERRED** — reasoned synthesis from identified canon evidence;
- **DESIGN** — a Minecraft implementation choice not supplied by canon;
- **UNKNOWN** — deliberately unresolved because the available evidence does not answer it.

Do not turn an inference into canon merely because it is convenient for an implementation.

## Required workflow

Before changing a lore-sensitive system:

1. identify the exact mechanic or terminology being changed;
2. search the full chapter archive for the relevant appearances and later clarifications;
3. use the wikis to locate additional names, chapters or contradictions;
4. verify the decisive claim against chapter text;
5. record chapter numbers or a compact evidence note in the relevant design/review artifact;
6. separate canon constraints from Minecraft design decisions;
7. preserve `UNKNOWN` when the text does not establish an answer.

Later chapters can clarify or overturn an early interpretation. Research must therefore include relevant later material, not only the first chapter where a term appears.

## Conflict rule

When sources conflict:

1. prefer direct novel evidence;
2. prefer later explicit clarification over an early character assumption;
3. record the contradiction rather than silently choosing the convenient reading;
4. keep the implementation flexible when canon remains genuinely ambiguous.

## Current repository entry points

Read these after this policy:

- `docs/JAVA-LORE-ALIGNMENT.md` — architecture constraints derived from lore;
- `docs/PREVIEW-LORE-DECISIONS.md` — canon/inference/design ledger for the current preview;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — future Nightmare scenario boundaries;
- `docs/OPEN-QUESTIONS.md` — unresolved lore and design questions;
- `docs/lore-research/minecraft-implementation-brainstorm.md` — historical research-driven brainstorm; not automatically authoritative.

If one of those files conflicts with verified chapter evidence, do not quietly preserve the stale claim. Record the evidence and update the current authority document through the normal review workflow.
