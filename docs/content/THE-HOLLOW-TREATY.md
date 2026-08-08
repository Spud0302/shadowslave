# The Hollow Treaty — First Nightmare content module

**Status:** authored player-facing content slice on the reusable resolution engine.  
**Runtime status:** pure content definition; not yet wired into world generation or player entry.  
**Scenario ID:** `the_hollow_treaty`  
**Historical role:** `hostage_interpreter`

## Player-facing premise

The challenger inhabits the role of a hostage interpreter during the last hours of a winter truce between two exhausted hill settlements. An envoy is dead, the signed treaty ledger is missing, both delegations believe the other side altered the agreement, and civilians are gathering at the only pass before a storm closes it.

The player's leverage is social rather than martial: they understand both treaty dialects, know how translated clauses can be distorted, and are one of the few people both factions reluctantly recognize as able to authenticate testimony. Their weakness is the same fact viewed from the other direction: they are being held as a guarantee of good faith and are distrusted by both sides.

The scenario is intentionally built so that no boss or creature kill is required to resolve it.

## Authored content

Locations:

- **Oath Hall** — neutral council chamber where public accusations can harden the conflict;
- **Scribe Loft** — treaty drafts, seal impressions and interpreter working notes;
- **Guest Quarters** — separated witnesses and minor delegates;
- **Snow Gate** — the closing pass where refugees from both sides are gathering;
- **Bell Court** — a public testimony ground where an exposed lie becomes politically difficult to conceal.

Named characters:

- **Sera** — senior interpreter who knows a disputed clause existed;
- **Halvek** — eastern escort captain demanding immediate blame;
- **Maelin** — western grain factor who knows who could reach the treaty seals;
- **Tovan** — page who saw the treaty wrapping replaced;
- **Elder Ress** — neutral gate keeper prioritising civilian passage over proving guilt.

Authored pressures:

- mutual distrust;
- translation asymmetry;
- forged evidence;
- hostage status;
- a closing mountain pass;
- civilian pressure.

## Resolution paths

The reusable `ResolutionGraph` exposes five terminal outcomes:

1. **Words Returned to Stone** — recover the documentary trail, secure the page's testimony, force a joint council and expose the forged clause;
2. **The Gate Before the Verdict** — leave the larger dispute unresolved but broker neutral civilian passage before the storm closes the ridge;
3. **A Peace Made of Paper** — fabricate a clean treaty copy and use it to stop immediate bloodshed, creating a peace founded on a deliberate lie;
4. **The Bell Answers Back** — challenge the central accusation publicly and expose the coercion behind the witness statement;
5. **No Words Left to Fight Over** — destroy or withhold the decisive documentary evidence so neither side can prove the claim it wants to use as a pretext for war.

These endings are intentionally morally different. The resolution engine treats them as ways the central conflict can reach a terminal state, not as a ranking from "correct" to "incorrect." Appraisal evidence is recorded separately so a later appraisal layer can distinguish truth, deception, mediation, sacrifice, preservation and other observed conduct without changing whether the conflict actually ended.

## Lore evidence

Research followed `docs/LORE-SOURCE-POLICY.md`; chapter text is the authority and the exact scenario remains project-authored design.

### CANON

- **Chapter 2 — Slave Caravan:** Sunny's First Nightmare places him in a concrete historical role with inherited social status, physical constraints, surrounding actors and situation-specific agency. The chapter also explains that other First-Nightmare roles can differ substantially in the agency and resources they provide.
- **Chapter 14 — Child of Shadows:** the First Nightmare ends when the reconstructed trial reaches its decisive resolution; the Spell does not present a universal boss-objective contract before the end.
- **Chapter 15 — Shadow Slave:** appraisal happens after the trial is over and recounts what the challenger actually did, keeping completion and appraisal separate.
- **Chapter 737 — Self-Reflection:** during the Second Nightmare, Sunny resolves a dangerous confrontation with Mordret through deception and manipulation rather than direct force. This is strong later evidence that meaningful Nightmare conflict is not limited to physical combat.
- **Chapter 743 — Appraisal:** the Spell's appraisal recounts a broad history of deeds from the Nightmare, including non-trivial conflict and a battle of wits, before issuing a verdict. Sunny's subsequent theory about fate divergence remains a character interpretation rather than a canonical scoring formula.
- **Chapter 2029 — Fortune Telling:** much later material explicitly states that some of Sunny's First-Nightmare traits came from the historical temple-slave body he inhabited, reinforcing that assigned historical role/body is substantive rather than cosmetic.

### INFERRED

- A reconstructed Nightmare conflict can be represented with social information, testimony, divided loyalties and deception as first-class scenario pressures instead of reducing every situation to creature combat.
- A historical interpreter/hostage role can coherently constrain what the challenger knows, whom they can influence and what personal risks attach to using that knowledge.
- Distinct terminal histories should feed appraisal evidence after completion rather than decide completion by moral alignment.

### DESIGN

Everything specific to **The Hollow Treaty** is Minecraft project design: the settlements, role, names, locations, political situation, forged clause, storm, choices, five endings, evidence weights, state graph and event IDs.

The following are also DESIGN:

- using a pure `ResolutionGraph` for the conflict;
- allowing deliberate deception or evidence destruction to reach terminal resolutions;
- treating the Snow Gate civilian evacuation as a terminal outcome without resolving the treaty dispute;
- the exact appraisal-evidence tags and weights.

None of these are claimed as Nightmare Spell formulas or copied novel events.

### UNKNOWN

- The Spell's actual algorithm for selecting a First-Nightmare historical role is unknown.
- The exact relationship between a reconstructed event's original history and the set of paths a challenger can create is unknown.
- The exact appraisal scoring/deed-selection formula is unknown; Chapter 743 does not supply one.
- Canon does not establish that every First Nightmare includes a Nightmare Creature, nor that a First Nightmare without a required creature fight follows this exact structure. This scenario therefore makes no universal claim from its combat-light design.
- Canon does not establish a universal faction, negotiation, testimony or deception subsystem for Nightmares.

### COMPATIBILITY

- canonical state remains owned by the Java core;
- this slice writes no player, Soul, identity, registry, progression or appraisal state;
- it depends only on the already-merged pure resolution engine;
- NPC dialogue, structures, animations, models, sounds and quest UI may later render/execute the authored content through removable adapters, but cannot own scenario completion;
- any future runtime integration should persist the resolved graph state/evidence in Java before permanent progression rather than reconstructing or rerolling the scenario after restart.

## Validation

`HollowTreatyScenarioDefinitionTest` checks:

- five locations, five named characters, six pressure primitives, twelve choices and five terminal resolutions;
- every declared choice is reachable through at least one accepted graph state;
- all five authored endings are reachable through explicit event sequences;
- no declared choice requires a kill/slay/boss event;
- decisive claims fail closed when their preparation has not happened;
- each ending carries positive, distinct appraisal-evidence weights;
- truthful, deceptive and evidence-destruction endings can all resolve the conflict without conflating completion with moral appraisal.

## Integration boundary

This slice deliberately does not spawn NPCs, implement dialogue, choose a live Nightmare scenario, persist graph state, assign the role to a player, award appraisal, or alter the active restart-correctness stack.

A later runtime integration should resolve one scenario and one historical role once, persist those stable IDs with the active Java-owned `NightmareInstance`, then let presentation/world adapters consume that state.
