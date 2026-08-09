# Nightmare NPC conversation exchanges — lore/evidence note

**Date:** 2026-08-09  
**Scope:** bounded player-facing question/answer/counter-question/verification/disengagement modules after scenario and actor context are already resolved.  
**Generator:** `nightmare-npc-conversation-exchange-v1`

## Repository checks

Before authoring this slice, current `main` (`e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`), open PRs/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, the current Nightmare content catalogues, and active NPC motive/stance PRs #141/#142 were reviewed.

This branch is deliberately based directly on `main`. It does **not** import #141 motives or #142 stances. A future Java-owned composer may use a resolved motive/stance to constrain the allowed exchange families, but this catalogue remains independently reviewable and rebaseable.

## Primary/later chapter checks

### Chapter 2 — *Slave Caravan*

Sunny enters a reconstructed historical/social situation with a substantive inherited body, social position and relationships. Conversation among the caravan members communicates local history, danger, status and competing attitudes, but it does not function as an omniscient truth channel.

Relevant constraint: social information inside a Nightmare can matter while remaining situated in the reconstructed people and circumstances.

### Chapter 14 — *Child of Shadows*

Sunny wins decisive advantage through incomplete information, selective disclosure, deception and situational knowledge rather than through a universal announced social or combat rule. What characters know and what they infer are materially different.

Relevant constraint: conversation/information can alter what actors do without becoming a universal truth or persuasion mechanic.

### Chapter 737 — *Self-Reflection*

During the Second Nightmare, Sunny deliberately uses a long conversation, misleading questions and information asymmetry to occupy Mordret and create a false impression while another actor completes the important task. Nominal alliance does not make intentions transparent.

Relevant constraint: questions, answers and conversational framing can be strategic actions; apparent cooperation does not expose hidden truth or allegiance.

### Chapter 743 — *Appraisal*

The Nightmare is already over before appraisal. The Spell later narrates a broad history of deeds, including a battle of wits, before giving a verdict. Sunny's subsequent theory about fate divergence remains character interpretation rather than a formula.

Relevant constraint: social/intellectual deeds can be consequential, but this catalogue must not calculate scenario completion or appraisal from conversational content.

### Chapter 2029 — *Fortune Telling*

Much later material explicitly explains that some of Sunny's First-Nightmare Attributes and initial Aspect came from the historical temple slave whose body he inhabited.

Relevant constraint: inherited First-Nightmare body/role identity can be substantive rather than cosmetic, supporting the architecture where an actor context is resolved before player-facing dialogue modules are composed.

## Freshness check

At research time, the owner-designated NovelFull access layer listed through **Chapter 3116 — Princess of the Underworld**. Official WebNovel reported **3,131 chapters**. NovelFull is therefore treated only as the repository-designated chapter-reading access layer, not as publication authority. No claim in this slice depends on material later than Chapter 2029.

## Evidence classification

### CANON

- Nightmares can reconstruct substantive historical/social circumstances and historical body/role identity.
- Information, deception, uncertainty and interpersonal intent can materially affect action inside a Nightmare.
- Nominal allies can retain uncertain or conflicting intentions.
- Nightmare resolution precedes appraisal.
- Appraisal narration can include non-combat/intellectual/social accomplishments.

### INFERRED

- It is useful to separate an already-resolved actor/scenario identity from a local conversation exchange.
- Question, answer, counter-question, verification and disengagement are useful authoring dimensions for representing bounded information conflict.
- Verification should constrain a local claim without automatically turning presentation into omniscient truth.

### DESIGN

- The exact five `ExchangeFamily` values.
- All 20 exact exchange primitives, titles, reads, NPC lines, three player options, affinity tags, two presentation cues and anti-overclaim boundaries.
- Positive-evidence preference, evidence-magnitude independence, SHA-256 deterministic selection and generator version `nightmare-npc-conversation-exchange-v1`.
- The rule that the caller supplies scenario ID, actor-context ID and allowed families before composition.

### UNKNOWN

- Any canonical NPC-dialogue or conversation-generation formula.
- Canonical persuasion, trust, reputation, leverage, interrogation, deception-detection or truthfulness mechanics.
- Whether the Nightmare Spell reconstructs exact historical psychology or dialogue.
- Any universal probability for cooperation, refusal, lying, bargaining, disclosure or disengagement.
- Any mapping from conversational behavior to allegiance changes, scenario events, appraisal, rewards or progression.

### COMPATIBILITY

- Java remains authority for scenario identity, actor/historical-role identity, accepted `ResolutionGraph` events, terminal resolution, appraisal inputs and future persistent relationship/knowledge state.
- This catalogue emits immutable presentation content only.
- Dialogue UI, voice, NPC AI, animation, subtitles, structures and other external adapters may render or execute an already-resolved exchange but cannot infer truth, mutate allegiance, accept scenario events, award progression or own persistence.
- A later composer may combine #141 motive and/or #142 stance with these exchanges by narrowing `allowedFamilies`; neither branch needs to become state authority.

## Validation intent

`NightmareNpcConversationExchangeCatalogTest` requires:

- exactly 20 unique primitives / four per family;
- exactly three authored player options and two cues per primitive;
- deterministic same-input output independent of evidence-map order;
- evidence magnitude independence (`1` and `999` are equivalent positive context);
- positive compatible evidence may prefer matching authored content without inferring truth;
- a 4,096-seed sweep cannot change caller-owned scenario ID, actor context or allowed family;
- a 16,384-seed neutral sweep reaches all 20 primitives and all 40 primitive/cue pairs;
- common truth/guilt/allegiance/persuasion/scenario/canon overclaims remain explicitly bounded;
- blank scenario/actor IDs, empty family sets and negative evidence fail closed.

No canonical generation, dialogue, truth, persuasion, allegiance, probability, appraisal or reward formula is claimed.
