# Shadow Slave lore research

Working evidence base for the Minecraft project. This directory is intentionally separate from the
existing lore-reference note until findings are sufficiently verified to promote into the systems bible.

## Research rules

- **Spoilers:** unrestricted. The reader is current with the novel.
- **Purpose:** establish the canon first, then derive Minecraft implementation/design proposals from it.
  Design proposals are allowed in this branch when they are clearly separated from canon claims.
- **Do not fill gaps:** `UNKNOWN` is preferable to a plausible invention.
- **Contradictions stay visible:** record both readings and explain which has stronger support.
- **Formal names stay separate from labels:** a nickname, title, wiki page label such as
  `Character's Flaw`, or an ability name is not treated as the formal Spell-given name unless the
  novel/status runes establish it.
- **Canon and design stay distinguishable:** implementation sections may be opinionated, but they must
  not silently turn a proposed mechanic into a lore claim.
- **Adaptation visuals stay secondary:** the manhwa may guide visual language and staging, but it does
  not overrule the novel's mechanics or terminology.

## Source order

1. **Novel text** — the primary authority. Andrew authorised the NovelFull mirror for this
   project's own research; treat that as a decision about this repo, not a general licence, and
   prefer the official release below when verifying a specific claim.
   https://novelfull.com/shadow-slave.html
2. **Official WebNovel** — especially useful for verification and chapters newer than mirrors/indexes:
   https://www.webnovel.com/book/shadow-slave_22196546206090805
3. **Released manhwa adaptation material** — secondary authority for visual interpretation, staging,
   clothing, architecture, environment, creature scale and status-screen composition. Translation or
   scanlation access pages are reading aids, not authority for exact English terminology. Current owner-
   supplied access page: https://vortexscans.org/series/shadow-slave
4. **Miraheze community wiki:** https://shadowslave.miraheze.org/wiki/
5. **Fandom community wiki:** https://shadowslave.fandom.com/wiki/

Community wikis are indexes and research aids, not authority over direct novel text. They can also be
unevenly updated: late-story claims must be checked against the novel instead of assuming either mirror
is current.

The adaptation can resolve how its artists chose to depict something, but that choice remains an
adaptation interpretation where the novel is ambiguous. It must not be promoted into a universal canon
mechanic without textual support.

## Confidence labels

- **CANON** — stated directly in the novel/status runes, or a wiki claim whose cited passage was verified.
- **WIKI** — asserted by a community wiki but the supporting passage has not yet been independently checked.
- **INFERRED** — synthesis from multiple canon facts; the inference and its basis must be stated.
- **DESIGN** — an explicit Minecraft implementation choice, not a claim about the novel.
- **UNKNOWN** — available sources do not answer the question.

## Research set

The project research brief prioritised depth over breadth. Sections A–F have now received a full working
pass, followed by a cross-system Minecraft design brainstorm.

1. [Section A — Aspects and Flaws](section-a-aspects-and-flaws.md)
   - [Verification pass 2](section-a-verification-pass-2.md) — later-lore corrections and stronger evidence.
   - [Verification pass 3](section-a-verification-pass-3.md) — closes most of the remaining Flaw-verification backlog.
2. [Section B — Ranks and progression](section-b-ranks-and-progression.md)
3. [Section C — Memories](section-c-memories.md)
4. [Section D — Nightmares, Seeds, Gates and Nightmare Creatures](section-d-nightmares.md)
5. [Section E — Soul Cores, Corruption and Echoes](section-e-soul-cores-corruption-echoes.md)
6. [Section F — Vocabulary, institutions, Spell voice and world texture](section-f-vocabulary-world-texture.md)
7. [Minecraft implementation brainstorm](minecraft-implementation-brainstorm.md) — architecture and migration ideas derived from Sections A–F and a fresh audit of the live datapack.
8. [Java lore-alignment gate](../JAVA-LORE-ALIGNMENT.md) — approved architecture constraints for Java-era implementation.

## Highest-impact corrections found

These are navigation notes, not substitutes for the evidence in the section files:

- First Nightmare completion produces a **Dormant Dreamer/Sleeper**, not an Awakened. Actual Awakening
  follows the first successful Dream Realm journey and return through a Gateway.
- Canon **Attributes** are named supernatural traits, not generic Vitality/Endurance-style statistics.
- Ordinary **Memories** are soul-stored but transferable; true Bound/Soulbound relics are exceptional.
- **Soul Shards** strengthen/saturate a core; they do not automatically promote Soul Rank.
- Ordinary **Echoes** are static soulless replicas. Sunny's growth-capable Shadows are a separate
  Aspect-specific system.
- A Nightmare resolves a **central conflict** rather than universally requiring a boss kill.
- First-Nightmare failure normally means death and a small Gate, not safe ejection and retry.
- Late canon establishes a **Seventh Nightmare** beyond the six Rank-advancement Nightmares.
- There is no verified universal `Divine Sight` mechanic that lets every Awakened inspect another
  person's full status.

## Promotion policy

The existing `Shadow Slave - Lore Reference` note remains the project's concise systems bible for now.
Research in this directory may disagree with it while verification is underway. When a finding is stable,
update the systems bible explicitly rather than silently allowing the two documents to drift.

The implementation brainstorm is **not** automatically approved design. It is a proposal set to evaluate
before code changes are made. The Java lore-alignment gate records the subset now accepted as architecture.
