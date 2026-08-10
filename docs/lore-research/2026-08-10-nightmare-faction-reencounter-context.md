# Nightmare faction re-encounter context — lore evidence

**Status:** bounded player-facing DESIGN content around already-resolved Java state.  
**Research date:** 2026-08-10  
**Source policy:** `docs/LORE-SOURCE-POLICY.md` is mandatory and was reread before implementation.

## Scope

This slice asks a narrow presentation question: when a Nightmare-local faction is encountered again and Java already knows the relevant prior-history record plus the current re-encounter context kind, what bounded context can be shown to the player without synthesizing a relationship simulator or inventing current faction state?

The implementation deliberately does **not** claim that the novel defines a re-encounter UI, relationship-history taxonomy, procedural faction generator, reputation system, or formula connecting prior contact to later behavior. The exact four context kinds and all authored copy are project DESIGN.

## Primary chapter checks

### Chapter 2 — `Slave Caravan`

The First Nightmare reconstructs a substantive historical situation rather than placing the challenger in an abstract arena. Sunny experiences a particular body and social position inside a slave caravan, with concrete people, hierarchy, environment and immediate circumstances.

**Use here:** supports keeping faction/social context tied to a specific reconstructed scenario and interaction rather than treating faction state as a universal out-of-world reputation meter.

### Chapter 14 — `Child of Shadows`

Sunny's decisive action depends on bounded asymmetric situational knowledge: he knows a crucial fact that another participant does not and acts around that information gap. The chapter ends with the Nightmare ending and appraisal beginning afterward.

**Use here:** supports preserving the difference between remembered information, current unknowns and later authoritative outcome. A past interaction can be useful context without becoming omniscience or appraisal authority.

### Chapter 737 — `Self-Reflection`

Inside a later Nightmare, nominal allies can still have uncertain or potentially conflicting future intentions. Sunny explicitly treats alliance as insufficient to settle whether another participant may become an adversary.

**Use here:** later clarification against converting prior cooperation, contact or nominal alignment into guaranteed present trust, allegiance or future behavior.

### Chapter 743 — `Appraisal`

The Spell begins appraisal only after the Nightmare is already over. Its retrospective account can encompass broad deeds, including intellectual/social conflict, while remaining distinct from the event that ended the Nightmare.

**Use here:** re-encounter presentation must not become terminal-resolution or appraisal state.

## Freshness / later-material check

At research time the owner-designated NovelFull access layer listed through **Chapter 3116 — `Princess of the Underworld`**. Official WebNovel pages were not perfectly synchronized: the title page exposed a newer snapshot than one catalogue result, with current indexed counts around **3,127–3,131 chapters** and title-page latest release around Chapter 3130. This mismatch is treated as publication/index volatility, not lore evidence.

No implementation rule in this slice depends on material later than Chapter 743. Later-material research was used to look for contradictions to the bounded social-state interpretation; none of the checked material establishes a canonical re-encounter taxonomy or formula.

## Evidence boundary

### CANON

- Nightmares can reconstruct substantive historical/social circumstances in which role, people and immediate situation matter.
- Bounded/asymmetric information can materially affect action inside a Nightmare.
- Nominal cooperation or alliance need not settle another participant's future intent.
- Nightmare resolution precedes appraisal; appraisal is a later concern.

### INFERRED

- Prior interaction history and current re-encounter context are useful separable implementation concerns.
- Remembered history can be shown as context without converting it into a present relationship verdict.
- When current circumstances differ from an earlier interaction, it is safer to re-check present state than to copy old assumptions forward.
- An unresolved matter can be carried forward as an unresolved matter without implying that old terms, offers or obligations remain valid unchanged.

### DESIGN

- `KNOWN_HISTORY / CHANGED_CIRCUMSTANCE / OPEN_BUSINESS / NO_CURRENT_COMMITMENT` as the four presentation kinds.
- All 16 exact primitives, titles, reads, prompts, responses, affinity tags, cues and anti-overclaim boundaries.
- Opaque scenario/faction/prior-history/re-encounter identifiers supplied by the caller.
- Positive evidence tags as presentation preference only, with magnitude ignored.
- Deterministic SHA-256 selection and generator version `nightmare-faction-reencounter-context-v1`.

### UNKNOWN

- Any canonical faction re-encounter/history UI or state taxonomy.
- Trust, reputation, allegiance, hostility, friendship, betrayal, goodwill or relationship-score mechanics.
- How prior cooperation, disputes, warnings, access or resource exchanges affect future faction behavior.
- Whether an old offer, permission, bargain, route, territorial claim or obligation remains current in any arbitrary scenario.
- Persuasion, leverage, intimidation, truthfulness, deception detection, guilt, legitimacy, ownership or social-AI formulas.
- Any probability, timer, cooldown or escalation system for faction re-encounters.
- Any formula mapping interaction history to accepted `ResolutionGraph` events, terminal Nightmare resolution, challenger outcome, appraisal, rewards or progression.

### COMPATIBILITY

Java remains the authority for scenario identity, faction identity, the prior-history record, the current re-encounter identity, exact context kind, current stance, membership/allegiance/reputation, active terms and commitments, access/territorial/resource/world/NPC state, accepted events, `ResolutionGraph` transitions, terminal resolution, challenger outcomes, appraisal, rewards, progression and persistence.

The catalogue only selects removable authored presentation for state already supplied by Java. Dialogue, HUD, journal, NPC, map, prop, audio, animation and other adapters may render the resolved context but must not derive or persist canonical state from the displayed prose.

## Implementation safeguards

- Authority identifiers are nonblank opaque strings and are preserved verbatim, including mixed-case or namespaced IDs.
- Only catalogue IDs and evidence tags use normalized stable-ID rules.
- Seeded selection cannot change the supplied context kind or any authority identity.
- Positive evidence changes preference only; `1` and `999` are equivalent and negative values fail closed.
- Generic primitive wording avoids inventing the specific fact that changed when only `CHANGED_CIRCUMSTANCE` is supplied.
- `KNOWN_HISTORY` never assumes that the history was cooperative, hostile or truthful.
- `OPEN_BUSINESS` carries only the existence of a bounded unresolved matter; it does not make an old offer or contract current.
- `NO_CURRENT_COMMITMENT` states only the supplied absence of a current bounded commitment; it does not imply neutrality, friendship, hostility or freedom from unrelated consequences.
- Player-facing fields exclude backend terminology such as `Java`, `caller-owned`, `authoritative`, and `ResolutionGraph`.

## Sources

- `docs/LORE-SOURCE-POLICY.md`
- `docs/NIGHTMARE-SEED-ROADMAP.md`
- Shadow Slave, Chapter 2 — `Slave Caravan`
- Shadow Slave, Chapter 14 — `Child of Shadows`
- Shadow Slave, Chapter 737 — `Self-Reflection`
- Shadow Slave, Chapter 743 — `Appraisal`
- NovelFull Shadow Slave chapter index (access-layer freshness check)
- Official WebNovel Shadow Slave publication/catalogue (chapter identity and publication freshness check)
