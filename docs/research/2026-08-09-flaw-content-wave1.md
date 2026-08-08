# Flaw content wave 1 — evidence and design boundary

**Date:** 2026-08-09  
**Status:** player-facing content research note  
**Implementation:** `FlawContentCatalog`

## Scope

This slice enriches the 17 already-authored procedural Flaw identities in `ExpandedIdentityContentCatalog.waveOne()` with player-facing descriptions of:

- a broad consequence family;
- an authored trigger;
- an authored consequence;
- multiple practical coping hooks;
- presentation tags;
- deterministic presentation variation after Flaw identity is already resolved.

It does **not** select, award, persist, reveal, suppress, balance, rank, evolve, or execute a Flaw. It does not change `DeterministicIdentityGenerator` and does not claim that the Nightmare Spell uses these families or rules.

## Source-policy check

Research followed `docs/LORE-SOURCE-POLICY.md`.

At research start, the owner-designated NovelFull listing showed **Chapter 3116 — Princess of the Underworld** as its latest indexed chapter. That listing is an access-layer freshness check only; chapter text remains the authority. Relevant later clarification for this slice is far earlier than the current index.

The decisive claims were checked against chapter text and, where practical, official WebNovel chapter pages.

## Primary evidence

### Chapter 17 — Three Simple Words

Sunny's post-First-Nightmare runes expose a named **Flaw**, `Clear Conscience`, with a concise description that he cannot lie. The immediate narrative establishes that a Flaw can be personally consequential rather than a generic stat penalty.

**Use here:** supports named, player-visible Flaw identity and qualitative consequence presentation.

### Chapter 20 — Outcast Once Again

Sunny experiments with the actual behavior of `Clear Conscience`: direct questions compel truthful answers, resistance builds pressure/pain, and he can still work within the constraint through careful wording.

**Use here:** supports a Flaw having a trigger/condition, a concrete consequence, and player-discoverable ways to cope or work around it. This does **not** imply every Flaw uses compulsion, pain, questions, or loopholes.

### Chapter 53 — Immortal Flame

Nephis' Flaw is demonstrated through severe pain when she uses her Aspect Ability. The trigger and consequence are completely different from Sunny's truth restriction.

**Use here:** strong evidence that Flaws are mechanically varied and can be tied to ability use and bodily pain rather than sharing one universal debuff shape.

### Chapter 118 — Mirror Image

Kai reveals that his Flaw allows him to detect when someone is lying. In context it can look beneficial, while later interactions show that its personal/social consequences are not reducible to a conventional negative status effect.

**Use here:** supports avoiding a design rule that every Flaw must be a simple numeric punishment.

### Chapter 211 — Game of Lies

Kai's lie-detection Flaw is directly useful in a social game. This is useful negative evidence against representing every Flaw as a combat weakness.

**Use here:** supports social/information consequence families alongside physical/resource/sensory ones.

### Chapter 353 — Light Bringer

Nephis' runes explicitly name `Pristine Soul` and describe the cost of using her power as suffering, matching the earlier demonstrated pain behavior.

**Use here:** supports named Flaw presentation and condition-linked consequences without implying a universal Aspect-to-Flaw formula.

### Chapter 1307 — Before the Nightmare Spell

Ananke's explanation of the natural path of ascension clarifies that imperfection/Flaws are more fundamental than the Nightmare Spell. The Spell helps people discover/unseal supernatural traits; it is not safe to model Flaws as arbitrary punishments manufactured by the Spell solely to balance an Aspect.

**Use here:** later clarification prevents the project from treating the procedural catalogue or its compatibility tags as a canonical Spell balancing algorithm.

## Evidence classification

### CANON

- Awakened can possess named Flaws that are player-visible through rune/identity presentation.
- Flaws can impose highly personal conditions and consequences.
- Known Flaws are mechanically diverse: truth compulsion, ability-linked suffering, and lie perception are not one shared debuff template.
- A person can learn to operate around a Flaw's constraints without removing the Flaw.
- Later clarification makes Flaws/imperfection more fundamental than the Nightmare Spell; the Spell should not be described as inventing a balancing curse by formula.

### INFERRED

- Trigger, consequence, and coping guidance are useful separable Java content concerns for presenting an already-resolved Flaw to a player.
- Broad presentation families such as sensory, social, physical, resource, emotional, environmental, attachment, and compulsion can help author varied gameplay as long as those families are not claimed as canon taxonomy.

### DESIGN

- All 17 exact Flaw descriptions in this wave.
- `FlawFamily` and every family assignment.
- Every trigger, consequence, coping hook, and presentation tag.
- Deterministically choosing which authored coping hook is surfaced first.
- `flaw-presentation-v1` generator/version identity.
- The rule that every current generated Flaw receives at least two coping hooks.

### UNKNOWN

- The canonical principle by which a person's Flaw is determined.
- Any universal Aspect-to-Flaw relationship or balancing equation.
- Whether the Spell internally categorizes Flaws at all.
- Relative severity, rarity, scaling, probability, or rank interaction of Flaws.
- Universal reveal timing outside the chapter-specific examples.
- Whether every Flaw has a discoverable loophole or mitigation strategy.

### COMPATIBILITY

The existing Java generator remains authoritative for the already-resolved Flaw identity. `FlawContentCatalog.compose(...)` consumes that stable ID and may vary only player-facing coping presentation. A future HUD/chat/rune/effect adapter may render or execute the result, but it must not become the owner of canonical Flaw identity or persistence.

## Validation contract

`FlawContentCatalogTest` requires:

1. exact one-to-one coverage of all 17 current generated Flaw IDs;
2. explicit `DESIGN` classification for every authored profile;
3. all eight design families represented;
4. non-trivial player summary, trigger, consequence, tags, and at least two coping hooks per Flaw;
5. deterministic presentation for identical seed + Flaw ID;
6. a 256-seed sweep can vary coping presentation while never changing identity, family, trigger, or consequence;
7. unknown Flaw IDs fail closed.

## Integration limit

This wave is descriptive content only. It does not implement the actual gameplay effects described here. If effects are added later, the Java core must own effect state and authoritative identity; removable platform/mod adapters may perform rendering or world execution only. Effect behavior should be integrated in small per-family slices rather than turning these descriptions into a broad unreviewed mechanic rewrite.
