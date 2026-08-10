# Nightmare role-to-scenario compatibility — wave 1

**Status:** bounded player-facing content DESIGN  
**Stack:** depends only on PR #97's authored historical-role catalogue  
**Architecture:** Java owns resolved role identity; dialogue, NPCs, structures, models and UI remain removable adapters

## Player-facing content

This wave adds scenario-specific historical-role variants for the two current authored First-Nightmare families:

- **The Last Signal** — Watch Apprentice, Wounded Courier, Caravan Scout, Border Levy, Archive Novice and Pilgrim Guide.
- **The Drowned Bell** — Watch Apprentice, Cistern Keeper, Quarry Runner, Healer's Aide, Pilgrim Guide and Ferry Deckhand.

Each variant adds three authored pieces of scenario texture:

1. an **entry hook** explaining why that historical person is present;
2. a **conflict pressure** giving the role a concrete obligation or dilemma inside the reconstructed situation;
3. **leverage** describing useful knowledge, access or relationships that make the role play differently.

The compatibility catalogue is deliberately narrower than the role catalogue. A role may be coherent in one reconstructed conflict and inappropriate in another.

## Selection model

The Java helper first filters to a scenario's authored compatible variants, then performs deterministic weighted DESIGN selection. Positive evidence matching the role's existing authored affinities may bias the result.

This is a project content tool, not a model of the Nightmare Spell.

```text
scenario compatibility module
+ authored role variants
+ optional recorded evidence
+ deterministic seed
-> one compatible role variant
```

The resolved role should later be persisted by stable role ID and not rerolled after restart.

## Lore evidence and policy boundary

Research follows `docs/LORE-SOURCE-POLICY.md` and the existing `docs/JAVA-LORE-ALIGNMENT.md` requirement that a Nightmare begins with role + historical situation + central conflict + possible resolutions.

Primary-material references already verified by the immediately adjacent role/scenario content work were re-audited for this slice:

- **Chapter 2 — Slave Caravan:** a First Nightmare places Sunny into a concrete historical body/role whose social and physical circumstances constrain the trial.
- **Chapters 3–14:** the inherited circumstances, surrounding people and local situation materially shape available choices.
- **Chapter 15:** appraisal follows resolution rather than defining the historical role.
- **Chapter 217 — Origin:** later material gives the inherited body pre-existing history.
- **Chapter 743:** later appraisal discussion supports retaining what the challenger actually did without supplying a universal role-selection formula.
- **Chapter 2029 — Fortune Telling:** later clarification ties some First-Nightmare Attributes to the historical body Sunny inhabited, reinforcing that the assigned role/body is substantive rather than cosmetic.

A fresh live chapter-host fetch was attempted during this run, but both the web research path and direct network resolution were temporarily unavailable. No new canon proposition in this slice depends on material beyond the already chapter-grounded role/scenario findings above. The access failure is therefore recorded as a verification limitation rather than silently claiming a fresh source read.

### Evidence classification

**CANON**

- First Nightmares can place challengers into another person's historical role/body.
- The role can carry meaningful social, physical and historical circumstances.
- Nightmare resolution/appraisal remains distinct from role assignment.

**INFERRED**

- A game implementation can represent role coherence as compatibility between a reconstructed conflict and authored occupation/knowledge/relationship primitives.
- Different coherent roles can expose materially different starting pressures and opportunities inside one scenario.

**DESIGN**

- all twelve role-to-scenario variants;
- all entry hooks, conflict pressures and leverage text;
- base compatibility weights;
- evidence multipliers;
- seed mixing and deterministic selection;
- the exact set of roles compatible with The Last Signal or The Drowned Bell.

**UNKNOWN**

- the Nightmare Spell's real role-selection algorithm or probabilities;
- whether similarity, fate, personality, biography, Aspect, Attributes or prior actions universally influence role assignment;
- any canonical number of possible roles for one reconstructed conflict;
- whether every theoretically coherent historical role would actually be available to the Spell.

**COMPATIBILITY**

- Java remains the canonical owner of resolved scenario/role identity.
- external mods may render bodies, dialogue, NPC behavior, structures or presentation, but removing them must not alter the saved resolved role.

## Validation contract

`NightmareRoleScenarioCompatibilityCatalogTest` requires:

- exactly the two current scenario families are covered;
- at least six authored role variants per scenario;
- every referenced role exists in the PR #97 catalogue;
- role IDs are unique within each scenario;
- every variant has non-empty entry, pressure and leverage content;
- deterministic output is independent of evidence-map iteration order;
- selection never escapes the scenario's compatibility set across 2,048 seeds;
- positive compatible evidence changes selection distribution without being described as canon;
- neutral generation reaches multiple variants;
- unknown scenarios and negative evidence fail closed.

## Deliberate limits

This wave does not alter `NightmareInstance`, `tryEnter`, role persistence, appraisal, Soul state or scenario resolution. The Last Signal runtime still uses its fixed preview `last_watchkeeper` role until a later Java-owned integration explicitly resolves and stores one catalogue role. The Drowned Bell remains an authored content definition rather than a spawned runtime scenario.

The next integration must resolve a compatible role exactly once before instance activation and persist the stable role ID. It must not reroll the role on login or generator-version change.
