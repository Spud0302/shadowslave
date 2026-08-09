# Nightmare faction agreement / commitment presentation — evidence note

**Status:** player-facing DESIGN slice; no canonical agreement generator, persuasion system, contract system, or appraisal formula is claimed.

## Repository boundary checked

Before implementation, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the active Nightmare NPC/faction/investigation content branches were reviewed. The active faction stack already covers pressure (#156), negotiation response (#157), and exchange beats (#159). This slice deliberately does not duplicate those concerns and is based directly on `main`.

The roadmap remains authoritative for architecture: scenario events and terminal resolution are Java-owned, challenger outcome is separate, and appraisal happens only after completion. Presentation cannot manufacture accepted `ResolutionGraph` events or progression.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Primary First-Nightmare material shows decisive action depending on bounded asymmetric situational knowledge and manipulation. The Nightmare then ends before appraisal begins. This supports a strict separation between local social/information presentation and authoritative scenario resolution; it does **not** establish a canonical contract, bargain, reputation, or faction-agreement system.

Working access: NovelFull chapter text. Official publication identity is also available through WebNovel.

### Chapter 737 — `Self-Reflection`

Later Nightmare material shows nominal allies can retain uncertain or potentially conflicting intentions. Cooperation therefore must not be rendered as proof of trust, loyalty, or permanent allegiance. This supports keeping agreement state narrower than relationship truth.

Working access: NovelFull; official WebNovel chapter identity/text cross-check available.

### Chapter 743 — `Appraisal`

The Nightmare is explicitly already over before appraisal. The appraisal recounts broad deeds, including intellectual/social conflict, before giving its verdict. Sunny then develops a theory about divergence from fate; this remains character interpretation and is not promoted into a project scoring formula.

This supports keeping fulfilled/broken/expired agreement presentation separate from terminal Nightmare resolution, appraisal, reward, and progression.

### Chapter 3006 — `Boons of a Nightmare`

Official WebNovel currently lists Chapter 3006 under that title, providing a later locator for the Mictlan Nightmare material used by adjacent faction research. During this run the owner-designated access layer did not surface a reliable direct body for that chapter through search, so **no fine-grained commitment rule depends on its wording**. It is retained only as later contextual corroboration that reconstructed Nightmares can contain substantive competing historical sides.

## Evidence classification

### CANON

- Nightmare action can materially depend on bounded/asymmetric information and social manipulation.
- Nominal cooperation inside a Nightmare does not necessarily settle intent or future opposition.
- Nightmare resolution precedes appraisal.
- Appraisal can recount broad deeds, including intellectual/social conflict, without supplying a universal social-action formula.

### INFERRED

- A bounded faction commitment can be presented separately from pressure, negotiation dialogue, allegiance, truth, resources, world mutation, and terminal scenario resolution.
- `PROPOSED`, `ACCEPTED`, `FULFILLED`, `BROKEN`, and `EXPIRED` are useful presentation states **only when Java supplies the authoritative state**.
- Fulfillment can be displayed as completion of one scoped obligation without implying overall Nightmare success, trust, fairness, or appraisal value.
- Broken or expired commitments can be displayed without inferring motive, blame, hostility, guilt, or punishment.

### DESIGN

- The exact five-state taxonomy: `PROPOSED / ACCEPTED / FULFILLED / BROKEN / EXPIRED`.
- All 20 exact authored primitives, wording, prompts, player responses, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque caller-owned scenario/faction/agreement/commitment IDs.
- Positive-evidence preference with magnitude deliberately ignored.
- Deterministic SHA-256 selection and generator version `nightmare-faction-agreement-commitment-v1`.

### UNKNOWN

- Any canonical Nightmare contract/agreement/negotiation state machine or generation formula.
- Persuasion, leverage, trust, reputation, intimidation, hostility, allegiance, or faction-relation mathematics.
- Resource valuation, debt, escrow, transfer timing, ownership, scarcity, or fair-exchange rules.
- Territorial legitimacy, permission enforcement, route safety, access-token rules, or automatic AI ceasefire behavior.
- Truthfulness, bluff detection, bad-faith detection, motive inference, collective blame, guilt, punishment, or betrayal scoring.
- Canonical expiry timers, retry/cooldown rules, offer persistence, or probability of renewed terms.
- Any commitment-to-`ResolutionGraph`, appraisal, reward, rank, progression, or fate-divergence formula.

### COMPATIBILITY

Java remains canonical authority for:

- scenario, faction, agreement, and commitment identity;
- exact commitment state and any state transition;
- membership, allegiance, reputation, trust, hostility, and NPC/AI state;
- resources, transfers, inventory, access legality, route/world mutation, and actor movement;
- accepted scenario events and `ResolutionGraph` transitions;
- terminal Nightmare resolution and per-challenger outcome;
- appraisal inputs/verdict, rewards, progression, and persistence.

Dialogue, HUD, NPC, map, structure, prop, audio, animation, and other external adapters may render already-resolved commitment state but must remain removable and must not infer canonical state from presentation text.

## Freshness / source limitation

Official WebNovel was checked during this run and currently reports 3,131 chapters in the fetched title page, with Chapter 3006 explicitly listed in Volume 12. The owner-designated NovelFull index remains the project reading access layer; because its search result did not reliably expose the newest listing/body needed for a fresh terminal-chapter count in this run, no exact NovelFull latest-chapter number is asserted here.

No implementation rule in this slice depends on material later than Chapter 743; Chapter 3006 is contextual corroboration only.
