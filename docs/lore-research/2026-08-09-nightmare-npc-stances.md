# Nightmare NPC stance / response primitives — evidence note

**Date:** 2026-08-09  
**Scope:** player-facing response stances for already-resolved Nightmare NPC/role contexts  
**Implementation:** `NightmareNpcStanceCatalog`

## Repository boundary checked first

Before authoring this slice, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current Nightmare scenario/content work, and active PR #141 NPC-motive work were reviewed.

PR #141 already authors motive/relationship primitives. This branch therefore does **not** duplicate or import those motives. It is based directly on `main` and defines a separate response/presentation concern that can later consume a resolved motive without becoming dependent on that unmerged branch.

## Primary / later chapter checks

### Chapter 2 — `Slave Caravan`

Sunny's First Nightmare reconstructs an older situation and puts him into a substantive historical body/social position. The scene has other people with their own immediate interests, status and reactions rather than treating everyone as neutral objective dispensers.

**Use here:** supports the broad constraint that reconstructed social context and other actors can matter to a Nightmare.

### Chapter 737 — `Self-Reflection`

During the Second Nightmare, Sunny explicitly treats Mordret as an uncertain ally whose actual intentions may diverge from the nominal alliance. The conflict is not reducible to a binary friendly/hostile flag.

**Use here:** supports keeping cooperation, refusal and changing interpersonal posture distinct from factual allegiance or guaranteed intent.

### Chapter 743 — `Appraisal`

After the Nightmare is already over, the Spell recounts a broad range of deeds and consequences, including Sunny's battle of wits against Mordret, before issuing the final appraisal. Sunny then theorizes about divergence from fate; that theory is not treated as an implementation formula here.

**Use here:** supports interpersonal/non-combat action being narratively meaningful while keeping scenario resolution and appraisal separate from dialogue presentation.

### Chapter 2029 — `Fortune Telling`

Much later discussion explicitly clarifies that traits gained from Sunny's First Nightmare can be tied to the historical body/person he inhabited. This reinforces that reconstructed role/body identity can be substantive rather than cosmetic.

**Use here:** supports accepting an already-resolved actor/role context as meaningful input without inventing a canonical NPC-generation system.

## Freshness check

The owner-designated NovelFull access layer currently lists through **Chapter 3116 — Princess of the Underworld**. Official WebNovel currently reports **3,131 chapters**. NovelFull is therefore treated as a reading-access layer, not publication authority. No claim in this slice depends on material later than Chapter 2029.

## Evidence classification

### CANON

- Nightmares can reconstruct substantive historical/social circumstances rather than neutral arenas.
- People inside a Nightmare can have uncertain or conflicting intentions even when they are nominally allied.
- Nightmare resolution precedes appraisal, and appraisal can narrate broad non-combat/social/intellectual deeds.
- Historical body/role identity in a First Nightmare can contribute substantive traits.

### INFERRED

- An NPC's immediate conversational/behavioral stance is a useful concern to author separately from deeper motive, allegiance, truthfulness and final scenario outcome.
- Cooperation, refusal, bargaining, warning, conditional help and withdrawal can be represented as player-readable local response shapes without asserting hidden truth.
- Scenario authors should provide the allowed stance families after actor/role identity is already resolved.

### DESIGN

- The exact six-family taxonomy: `COOPERATION`, `REFUSAL`, `BARGAINING`, `WARNING`, `CONDITIONAL_HELP`, `WITHDRAWAL`.
- All 24 exact stance primitives, wording, spoken hooks, player levers, affinity tags, presentation cues and anti-overclaim boundaries.
- Positive evidence tags may prefer compatible authored presentation; evidence magnitude is ignored.
- SHA-256 deterministic tie-breaking and generator version `nightmare-npc-stance-v1`.
- The generic `actorContextId` input seam so this main-based layer can later compose with scenario-local NPC IDs and/or a resolved motive ID.

### UNKNOWN

- Any canonical NPC personality, motive or dialogue generation formula.
- Whether the Spell reconstructs exact historical psychology or only functionally relevant behavior.
- Truthfulness, lie probability, allegiance changes, betrayal probability, trust/reputation values, persuasion thresholds or social-AI rules.
- Whether cooperation/refusal/bargaining states have any universal canonical taxonomy or frequency.
- Any relationship between conversation outcomes and Nightmare difficulty, fate, appraisal, rewards or progression.

### COMPATIBILITY

- Java remains authoritative for scenario ID, role/NPC identity, accepted `ResolutionGraph` events, terminal resolution, appraisal inputs and any future persistent relationship state.
- A future Java-owned composition layer may first resolve a motive (for example from PR #141 after merge) and then constrain the allowed stance families/evidence supplied here.
- Dialogue, NPC AI, animation, voice, HUD and other external adapters may render a resolved stance but must not infer truth, allegiance, persuasion success or scenario completion from presentation.

## Anti-overclaim rule

A displayed stance is **not an accepted scenario event**. `Open Cooperation` does not mean the NPC is truthful or loyal; `Flat Refusal` does not mean persuasion is impossible; `Temporary Truce` does not alter faction state; `Immediate Warning` does not prove danger; `Help After Proof` does not define a quest gate; `Withdraw to Observe` does not predict future allegiance.

No canonical generation, persuasion, allegiance, trust, probability, appraisal or reward formula is claimed.
