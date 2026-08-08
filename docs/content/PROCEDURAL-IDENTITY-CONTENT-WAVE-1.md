# Procedural identity content — wave 1

**Status:** player-facing content slice stacked on PR #38.  
**Runtime status:** catalogue is generation-ready but not wired into permanent appraisal.  
**Classification:** original Minecraft **DESIGN** content constrained by verified Shadow Slave lore architecture.

## Purpose

The procedural generator foundation deliberately started with a very small Last Signal catalogue. This content wave broadens the space enough that generated identities can vary in theme, naming structure, ability expression and Flaw family without requiring a finite list of completed Aspects.

The generator rules are unchanged. This PR adds content only.

## Content totals

| Family | PR #38 prototype | Wave 1 | Added |
| --- | ---: | ---: | ---: |
| Aspect natures | 4 | 12 | 8 |
| naming archetypes | 4 | 10 | 6 |
| Dormant ability expressions | 8 | 24 | 16 |
| Flaw primitives | 5 | 17 | 12 |

The original Ash, Ember, Road and Signal families remain present.

## Nature families

Wave 1 supports:

- Ash — endurance and aftermath;
- Bell — warning, sound and resonance;
- Ember — light and preservation;
- Glass — reflection and precision;
- Hollow — absence and concealment;
- Mist — concealment and perception;
- Road — movement and guidance;
- Signal — warning and perception;
- Stone — endurance and stability;
- Thorn — growth and retaliation;
- Thread — connection and precision;
- Tide — rhythm and adaptation.

These are project themes, not a claim that canon divides Aspects into twelve elemental categories.

## Naming archetypes

Generated names can now use patterns such as Keeper, Bearer, Sentinel, Seeker, Witness, Wanderer, Pilgrim, Weaver, Voice and Last. The pattern is combined with the chosen nature token, so the generator still produces a resolved identity rather than choosing one authored completed Aspect.

Examples of possible generated names include forms such as `Keeper of Bell`, `Glass Sentinel`, `Weaver of Thread` and `Mist Wanderer`. These are illustrative DESIGN outputs, not canon characters or canon Aspects.

## Ability expressions

Each nature has at least two compatible Dormant ability expressions. Examples include:

- Bell: `Chime Warning`, `Resonant Mark`;
- Glass: `Glass Edge`, `Mirror Glimpse`;
- Hollow: `Hollow Step`, `Still Presence`;
- Mist: `Mist Passage`, `Through the Haze`;
- Stone: `Holdfast`, `Root in Stone`;
- Thorn: `Thorn Lash`, `Wicked Bloom`;
- Thread: `Cut the Thread`, `Weave Link`;
- Tide: `Low Tide`, `Returning Tide`.

The existing Ash, Ember, Road and Signal expressions remain available.

Only stable identity, affinity and compatibility metadata is added here. Actual executable effects remain a later provider/core integration task.

## Flaw content

The Flaw pool deliberately avoids becoming a list of interchangeable percentage penalties. It includes different design families such as:

- compulsion and constrained behaviour;
- social disclosure;
- attachment and long-horizon burdens;
- environmental vulnerability;
- pain and sensory consequences;
- resource/cyclical constraints;
- physical or mobility constraints;
- psychological consequences.

New examples include `Bell Without Silence`, `Borrowed Breath`, `Brittle Oath`, `Echoing Pain`, `Empty Seat`, `Glass Heart`, `Narrow Path`, `Stone Sleep`, `Thorned Mercy`, `Tidal Debt`, `Uncut Thread` and `Witness's Burden`.

The exact effects behind these stable IDs are deliberately not implemented in this content slice. Future effect implementations must preserve the resolved primitive identity rather than rerolling the Flaw.

## Lore evidence boundary

Primary chapter material was rechecked before adding the content families.

### CANON

- Chapter 15 distinguishes the Aspect itself and a separately named Innate Ability during First-Nightmare appraisal.
- Chapter 354 grants an additional Aspect Ability on Awakening.
- Chapter 744 grants another Aspect Ability on Ascension and also shows an existing ability evolving.
- Chapter 784 demonstrates a behavioural/information constraint Flaw: Sunny cannot simply refuse to answer questions.
- Chapter 53 demonstrates a very different Flaw relationship: using Nephis's power causes extreme pain.

Together these examples support keeping Aspect identity, individual abilities and Flaws as separate persistent concepts, and they show that Flaws should not be modelled as one universal numeric-debuff template.

### INFERRED

- A broad generator should preserve thematic coherence between a resolved Aspect and its abilities without assuming that all powers are reducible to one element.
- A broad Flaw pool benefits from distinct constraint families rather than one mechanical shape.

### DESIGN

All Wave 1 nature names, archetypes, ability names, Flaw names, affinity tags, compatibility rules, weights and effect IDs are project-authored DESIGN.

The deterministic weighted-generation algorithm from PR #38 is also DESIGN.

### UNKNOWN

Canon does not provide:

- a deterministic Aspect/Flaw generation formula;
- a universal taxonomy of Aspect natures;
- a universal mechanical relationship between an Aspect and its Flaw;
- a canonical maximum number of possible Aspect themes;
- a rule that every generated identity must resemble these project categories.

This PR does not invent those as canon rules.

### COMPATIBILITY

- the original PR #38 primitive IDs are retained;
- generated results remain stable-ID based and persistence-ready;
- no existing fixed preview identity is changed;
- no player save is mutated;
- no external mod becomes a canonical-state owner.

## Validation

`ExpandedIdentityContentCatalogTest` verifies:

- the exact content-family totals;
- at least two compatible ability expressions for every nature;
- broad Flaw trait-family coverage;
- retention of all original Last Signal nature IDs;
- a 128-seed generation sweep produces valid resolved candidates and explores a broad portion of the catalogue.

## Integration dependency

This content remains intentionally unwired until the procedural foundation and restart-safe appraisal stack are integrated. The eventual live path should persist the chosen generated identity before committing permanent appraisal progression; it must never regenerate an existing player's result after a restart or catalogue update.
