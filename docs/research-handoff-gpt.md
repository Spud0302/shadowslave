# Research brief — Shadow Slave lore, for a Minecraft mod

Paste everything below the line into ChatGPT. It is written to be self-contained.

---

## Your role

You are doing **lore research**, not game design. I am building an unofficial fan Minecraft
mod based on the webnovel _Shadow Slave_ by Guiltythree. Phase 1 is built and playable. I need
accurate canon detail to design Phases 2–6 so the mod stops guessing and starts matching the
book.

Answer as a careful researcher: cite where each fact comes from, say when the wiki is vague or
contradicts itself, and never fill a gap with invention. **"The wiki doesn't say" is a genuinely
useful answer.** A confident wrong detail costs me more than an admitted gap, because I will
build a system on it.

## Hard rules

1. **Sources, in order of preference:**
   - `https://shadowslave.miraheze.org/wiki/...` — the community wiki. This is the main source
     and it responds to automated fetching.
   - `shadowslave.fandom.com` — an older mirror that **blocks automated fetching**. If you can
     read it, treat it as secondary and note where it disagrees with Miraheze.
   - Official serialisation on Webnovel for verifying a specific claim.
   - **Do not** use pirated full-text mirrors of the novel, and do not reproduce chapters.
2. **Summarise; do not reproduce.** Facts, mechanics, definitions, lists, relationships. Quote
   only where the exact wording of a definition matters, and keep quotes to a sentence or two
   with a citation. I do not want novel prose pasted into my design docs — I want the rules
   underneath it.
3. **Mark every answer with a confidence level:**
   - `CANON` — stated outright in the novel or quoted from it on the wiki.
   - `WIKI` — the wiki asserts it but I could not trace it to a passage.
   - `INFERRED` — your reading across sources. Say what it is built on.
   - `UNKNOWN` — the sources do not cover it.
4. **Flag contradictions explicitly** rather than silently picking one. The wiki is
   community-maintained and inconsistent in places; where it disagrees with itself or with
   Fandom, I want both readings and which is better supported.
5. **Spoilers are fine.** Go as deep into the story as the sources allow.

## What already exists — don't re-derive this

Phase 1, "The First Nightmare", ships as a vanilla Minecraft datapack:

- Sleeping marks you (a Carrier), sleeping again pulls you into a dark **Nightmare dimension**.
- A countdown, then a **Nightmare Creature** spawns. Kill it and you wake **Awakened** with an
  **Aspect** and a **Flaw**. Drop too low and you are cast out instead.
- A `/trigger soul` readout shows rank, Aspect, Flaw and attributes.

Two things I already know are wrong and am fixing, so **prioritise research that feeds them**:

- Phase 1 shipped **four fixed Aspects** (Shadow, Flame, Bone, Wind) and **four fixed Flaws**.
  The novel is explicit that every Awakened's Aspect is unique, and that Flaws are never random
  but grow out of the person. Both are placeholders.
- The trial is currently "survive a timer, then kill a boss". The real Nightmares are more
  varied than that.

## Roadmap I am researching for

| Phase | Adds                                                                                      |
| ----- | ----------------------------------------------------------------------------------------- |
| 2     | **Memories** — soulbound tiered gear from the Dream Realm, Memory ranks, a soul inventory |
| 3     | **Soul Cores & Ascension** — absorbing cores, rank progression, attribute growth          |
| 4     | **Echoes** — binding defeated shadows and summoning them                                  |
| 5     | **The waking world** — Nightmare Creatures breaching reality, Gates, later Nightmares     |
| 6     | Multiplayer — Divine Sight on other players, Clans                                        |

---

## Research questions

Work through these in order. Depth matters more than covering everything — a thorough answer to
Section A is worth more than thin coverage of all six.

### A. Aspects and Flaws — highest priority

I want to **generate** Aspects procedurally rather than ship a fixed list, so I need the shape
of the naming and the range of what an Aspect can be.

1. Collect **every named Aspect** you can find, with its owner and what it actually does.
2. From that set, describe the **naming patterns**. Are they single words, compounds, "X of Y"
   forms? Is the name descriptive of the power, of the person, or neither?
3. How is an Aspect **determined**? Is it shaped by the person, the Nightmare, the Creature
   killed, chance, or something else? Does canon ever explain the mechanism?
4. Can an Aspect **change, grow, or gain new expressions** as its owner ascends? What is the
   relationship between Aspect and Aspect Ability, Legacy, and Relic?
5. Collect **every named Flaw**, its owner, its effect, and — critically — **what about that
   person produced it**. This is the part I most need: I want to derive a player's Flaw from how
   they actually played the trial, so I need the causal logic canon uses.
6. Is a Flaw ever **removed, cured, or turned into an advantage**? Under what conditions?
7. How do Aspect and Flaw **relate to each other** for the same person? Opposed, complementary,
   independent?

### B. Ranks and progression

1. The **full rank ladder**, in order, with the canon name of each.
2. What **changes at each rank** — attributes, abilities, lifespan, status, anything measurable.
3. What is **required to ascend** between ranks? Soul cores, Nightmares survived, something else?
4. How do the **attributes** on the Spell Status Page work (the readout showing Vitality,
   Endurance and so on)? Full list, what each governs, typical values at each rank, and what
   makes them go up.
5. What is **Divine Sight**, and what exactly can one Awakened read about another?

### C. Memories — Phase 2

1. What **is** a Memory, in-world? Where do they come from?
2. The **rank/tier system** for Memories, and how a Memory's rank relates to its owner's rank.
3. How are Memories **obtained**? Are they earned, dropped, found, awarded on surviving a
   Nightmare?
4. What does **soulbound** mean here mechanically — can Memories be traded, lost, stolen,
   destroyed, upgraded?
5. Are there **categories** (weapon, armour, tool, utility)? Named examples with effects.
6. How do Memories **improve**? Is there an enchantment or awakening system for them?
7. How does an Awakened **carry and access** Memories? Is there a canon "soul inventory", and
   are there limits on how many can be held or equipped?

### D. Nightmares themselves — feeds Phase 5 and a Phase 1 rework

1. The **structure of a Nightmare**: how you enter, what determines its content, how it ends.
2. Is there a canon set of **Nightmare types or trials**? The wiki mentions six — enumerate them
   with what each demands of the participant.
3. **Win conditions other than killing a boss.** This matters a lot: my trial is currently
   survive-then-fight, and I want the real variety. Escape? Reaching a place? Solving something?
   Surviving a duration? Protecting something?
4. What happens on **failure**? Death, ejection, something worse? Are there canon consequences
   for failing that I should model?
5. **Nightmare Creatures**: the classification system (ranks and classes), what distinguishes
   each, and named examples with abilities.
6. **Gates and breaches** — how Nightmare Creatures reach the waking world, and what an
   incursion looks like.
7. Is the **First Nightmare** meaningfully different from later ones in rules or stakes?

### E. Soul Cores and Echoes — Phases 3 and 4

1. What is a **Soul Core**, where does it come from, and what does absorbing one do?
2. **Corruption** — what causes it, what it does, how it is resisted or cleansed.
3. What is an **Echo**? How is one created and bound?
4. What **limits** exist on Echoes — how many, what can be bound, do they persist or expire, do
   they grow?
5. How do Echoes **behave** when summoned? Do they retain the abilities they had in life?

### F. Texture and naming

Small but high-value for making the mod feel right rather than generic:

1. The **vocabulary** the world uses — what do people call the Nightmares, the Awakened, the
   creatures, the un-awakened?
2. Names of **factions, cities, and institutions** relevant to an Awakened's life.
3. The **tone** of the Spell's own messages, if the Spell ever "speaks" or displays anything.
4. Any **recurring imagery** — colours, materials, phenomena — associated with Nightmares,
   Aspects, or the Dream Realm.

---

## Output format

Structure your answer as markdown, under the section headings above. For each numbered question:

```
**A3. How is an Aspect determined?**
CONFIDENCE: WIKI

<your answer, 1-2 paragraphs or a list>

Sources: <urls>
Notes: <contradictions, gaps, anything I should not rely on>
```

At the very end, add two lists:

- **Open questions** — things I asked that the sources genuinely do not answer. Be specific;
  this tells me where I am free to invent without contradicting canon.
- **Things I did not ask about that matter.** You will have read more than my questions cover.
  If something in the source material would obviously change how I build this, say so. This is
  the most valuable part of the reply.

## What not to do

- Don't design Minecraft mechanics, suggest blocks or items, or propose implementations. I will
  do that. Give me the canon; the translation is my job.
- Don't smooth over gaps to give a complete-looking answer.
- Don't reproduce chapter text.
- Don't rank or review the novel. I only want its rules.
