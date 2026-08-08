# Nightmare historical-role content — wave 1

## Scope

This slice adds reusable player-facing historical-role content for reconstructed Nightmares. It is deliberately independent of persistence, runtime spawning, appraisal mutation, and the large correctness stack.

Wave one contains 12 authored roles:

- Watch Apprentice
- Wounded Courier
- Caravan Scout
- Cistern Keeper
- Healer's Aide
- Quarry Runner
- Border Levy
- Archive Novice
- Hostage Interpreter
- Pilgrim Guide
- Temple Attendant
- Ferry Deckhand

Each role combines authored primitives for occupation, social position, obligations, relationships, physical/social conditions, useful knowledge, alignment pressure, evidence affinities, a starting constraint, a hidden opportunity, and an arrival cue. These are intended to make a Nightmare role affect what the player knows, owes, fears, and can attempt rather than acting as a cosmetic class name.

## Lore evidence

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked early primary material together with later clarification.

- **Chapter 2 — Slave Caravan:** Sunny enters his First Nightmare in the body/role of a physically constrained temple slave within a reconstructed caravan. The role carries concrete social status, bodily condition, local history and relationships to the situation rather than being a free character loadout.
- **Chapters 3–14:** the reconstructed role's shackles, social position, knowledge available in the situation, surrounding actors and physical limitations materially shape the choices Sunny can make. His successful path is not simply performing an assigned quest objective.
- **Chapter 15 — Shadow Slave:** appraisal occurs after the Nightmare resolves and evaluates what the challenger actually did; role assignment and appraisal therefore remain separate concerns.
- **Chapter 217 — Origin:** later material adds history to the nameless temple slave body, reinforcing that the inhabited role can have a pre-existing identity/history inside the reconstruction.
- **Chapter 2029 — Fortune Telling:** much later clarification states that some of Sunny's First-Nightmare Attributes came from the nameless temple slave whose body he inhabited. This is strong evidence that the historical body/role is not merely presentation layered over the challenger's ordinary body.

## Evidence classification

**CANON**

- A First Nightmare can place a challenger into another person's historical body/role inside a reconstructed situation.
- That role can carry physical condition, social position and pre-existing identity/history relevant to the trial.
- The role can materially constrain the challenger's starting circumstances.
- Appraisal is distinct from role assignment and occurs after resolution.

**INFERRED**

- Occupation, social position, obligations, relationships, conditions and contextual knowledge are useful separable content concerns for representing role texture in Java.
- Authored role affordances should expose both constraints and opportunities instead of prescribing a single correct action.

**DESIGN**

- All 12 new role names and their exact details.
- `SocialPosition`, `Condition`, `Relationship`, `Knowledge`, and `AlignmentPressure` as authoring taxonomies.
- Evidence-affinity tags, base/matching weights, seed mixing, deterministic weighted selection and matched-evidence reporting.
- The rule that negative evidence is invalid input.

**UNKNOWN**

- The Nightmare Spell's actual role-selection principle or probability distribution.
- Whether every First Nightmare uses precisely the same kind of historical-body assignment.
- Any universal relationship between challenger biography, appraisal history, personality, fate, Attributes, Aspect, scenario difficulty, and assigned role.
- Whether a role's original fate or intended historical actions directly determine appraisal scoring.

No deterministic matcher in this slice is claimed to reproduce the Spell.

**COMPATIBILITY**

- Stable role IDs and resolved role identity remain Java-owned content/state concerns.
- Scenario builders may consume a selected role, but removable dialogue, model, NPC, structure or UI adapters must not become canonical role authority.
- The matcher does not award or mutate Soul Rank, Aspect, Flaw, Attribute, Memory, Echo or appraisal state.
- A later persistent Nightmare instance should save the resolved role ID rather than rerolling it after restart.

## Validation

`NightmareRoleContentCatalogTest` validates:

- exactly 12 unique authored roles;
- coverage of all authored social-position, condition, relationship, knowledge and alignment-pressure primitives;
- non-empty obligations, affinities, constraints, opportunities and presentation cues;
- deterministic output independent of evidence-map iteration order;
- evidence materially biases compatible authored roles without becoming a claimed canon formula;
- broad role diversity across a seed sweep;
- matched-evidence reporting only includes positive compatible evidence;
- negative evidence fails closed.

## Integration boundary

This slice is definitions plus deterministic DESIGN matching only. It does not modify `NightmareInstance`, persistence, role assignment during `tryEnter`, resolution, appraisal, or external presentation. A later integration can resolve a role before instance creation and persist its stable ID using Java-owned state.
