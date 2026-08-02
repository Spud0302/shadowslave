# Nightmare and Seed completion roadmap

**Status:** binding future-planning contract after `0.1.0-preview.1`  
**Scope:** First Nightmares, later Seeds of Nightmare, scenario resolution, challenger outcomes and appraisal  
**Lore rule:** re-check primary novel evidence before implementing each phase; never promote this roadmap's DESIGN choices to canon

This file records the project's current understanding so future work does not reduce every Nightmare to a boss kill, timer or objective-block click.

## 1. Working lore model

Important claims keep the project classifications from `docs/JAVA-LORE-ALIGNMENT.md`.

| Claim | Classification | Project rule |
| --- | --- | --- |
| A Nightmare reconstructs a historical situation and assigns challengers roles within it | **CANON** | Scenario data begins with role, situation and conflict—not arena geometry or a boss entity. |
| A Nightmare has a central conflict that can reach a terminal resolution | **CANON / INFERRED boundary** | Re-check the exact primary-text wording before generalising the implementation. |
| A challenger normally succeeds by surviving until the Nightmare reaches its end | **CANON / INFERRED boundary** | Survival/eligibility is evaluated separately from which action ended the conflict. |
| Killing the strongest creature is not a universal completion rule | **CANON-compatible constraint** | Combat can resolve a scenario, but only when that scenario's conflict says it does. |
| Appraisal is distinct from basic completion | **CANON-compatible architecture** | First determine whether the challenger completed the Nightmare; then judge how they performed. |
| A Seed is the entry/lifecycle manifestation associated with a contained Nightmare | **WORKING INTERPRETATION—VERIFY AGAIN** | Do not treat the external Seed as a block that must be clicked or destroyed to win. Conquering the contained Nightmare drives the Seed outcome. |
| First Nightmare and later Dream Realm Seeds are related but not identical entry cases | **CANON** | Keep common scenario/resolution machinery without forcing both through one inaccurate trigger model. |
| Multiple challengers may occupy different roles or sides in later Nightmares | **CANON-compatible requirement** | Resolution and outcome must be per scenario and per challenger, not a single global winner flag. |

Where primary evidence remains incomplete, use **UNKNOWN** rather than inventing a lore rule.

## 2. Completion is two decisions, not one trigger

The engine must answer two separate questions.

### 2.1 Did the reconstructed Nightmare end?

A scenario ends only when its **central conflict reaches a declared terminal resolution**.

Examples of valid terminal resolutions may include:

- a warning is delivered or suppressed;
- a person or group escapes, is captured, survives or dies;
- a city, object, seal or route is preserved, lost, opened or destroyed;
- one faction wins;
- a sacrifice changes the outcome;
- the reconstructed event reaches its unavoidable conclusion;
- a creature is defeated where that defeat actually resolves the conflict.

A button press, boss death or timer expiry is merely an event. The scenario's resolution logic decides whether that event completes the conflict.

### 2.2 What happened to each challenger?

After the scenario resolves, evaluate each challenger independently:

```text
scenario reached terminal resolution
        +
challenger remains alive and eligible at the end
        ↓
challenger completed/conquered the Nightmare
        ↓
teardown and return
        ↓
appraisal/progression
```

A challenger who dies before resolution ordinarily fails even if their earlier action later causes the conflict to end. Exact edge cases must be checked against primary lore before implementation.

## 3. Seed outcome is a consequence of Nightmare resolution

The future Seed model must separate:

- **Seed discovery and entry eligibility**;
- **the Nightmare definition contained/represented by that Seed**;
- **active Nightmare instances and challengers**;
- **terminal scenario resolution**;
- **per-challenger completion or failure**;
- **the Seed's post-resolution lifecycle**.

The project must not implement:

```text
find Seed block -> damage/click block -> Seed conquered
```

The intended architecture is:

```text
enter through Seed
-> inhabit assigned role
-> participate in reconstructed conflict
-> conflict reaches a valid terminal resolution
-> surviving challengers complete the Nightmare
-> contained Nightmare/Seed is resolved
```

The exact visual collapse, removal, reward timing and failed-Seed consequences remain **UNKNOWN until primary-text verification**.

## 4. Required domain boundaries

Future Java work should keep these as separate concepts even if some begin as small records.

### `SeedRecord`

Owns discovery, location, rank/eligibility information, entry state and relationship to a Nightmare definition. It does not own scenario scratch state.

### `NightmareDefinition`

Owns the reconstructed history, available roles, central conflict, resolution graph and scenario-specific content.

### `NightmareInstance`

Owns current participants, assigned roles/bodies, evidence, temporary inventory/state, spawned entities, recovery metadata and current conflict state.

### `ResolutionGraph`

Owns named conflict states, meaningful events, branches and terminal resolutions. It replaces universal `bossKilled` or `objectiveClicked` booleans.

### `ChallengerOutcome`

Records whether each challenger survived, remained eligible, completed, failed or exited technically. It is not the same object as the global scenario resolution.

### `AppraisalRecord`

Consumes the completed scenario, role, choices and evidence after completion. It must not decide whether the scenario itself ended.

## 5. Upgrade path from The Last Signal preview

`0.1.0-preview.1` proves lifecycle wiring, not the finished completion model. Its campfire interaction is a development trigger standing in for a richer conflict.

### Phase A — make actions into events

Replace direct `campfire clicked -> victory` logic with scenario events such as:

- reached the ruined watch;
- learned who the warning concerns;
- recovered fuel or a signal component;
- confronted, evaded or redirected the pursuer;
- chose a signal message or chose not to signal;
- lit, corrupted or abandoned the beacon.

No individual event completes the Nightmare unless the resolution graph accepts it as terminal in the current state.

### Phase B — add multiple terminal resolutions

The Last Signal should eventually support at least two materially different endings, for example:

- warn the threatened settlement;
- send a false signal for another faction;
- preserve the watch at personal cost;
- abandon the post and escape with its knowledge.

These are **DESIGN examples**, not claimed novel events. Lore review may replace them.

### Phase C — separate completion from appraisal

All surviving challengers may complete when the conflict ends, but appraisal can differ based on:

- assigned role;
- contribution;
- choices and sacrifices;
- deviation from the role's expected fate;
- discovered evidence;
- relationship to the final resolution.

No scoring formula may be labelled canonical.

### Phase D — allow world-driven resolution

The conflict must be able to resolve because of:

- another challenger;
- an NPC or faction;
- a delayed consequence of an earlier action;
- the reconstructed world's own timeline.

The engine must not require the local player to personally activate the final trigger.

### Phase E — later Seed support

After the First Nightmare model is stable:

- add persistent Seed discovery and eligibility;
- support multiple challengers and role assignment;
- allow challengers to occupy opposing sides;
- resolve one shared scenario while producing separate outcomes;
- enforce rank/entry rules only after another primary lore check;
- define the Seed's post-conquest and failure lifecycle from verified evidence.

## 6. State-machine direction

A scenario-specific implementation may use different internal states, but every Nightmare should expose a common contract similar to:

```text
PREPARING
-> ACTIVE
-> RESOLVING
-> TERMINAL_RESOLUTION
-> OUTCOMES_RECORDED
-> TEARDOWN_COMPLETE
```

`TERMINAL_RESOLUTION` must carry a named resolution ID and evidence, not only `success=true`.

Per-player outcomes should distinguish at minimum:

- `COMPLETED`;
- `FAILED_DEATH`;
- `TECHNICAL_RECOVERY`;
- `ADMIN_ABORT`;
- `INELIGIBLE_OR_INVALIDATED` where lore and design require it.

Technical outcomes never masquerade as normal mercy from the Spell.

## 7. Required tests

Before calling the completion engine mature, automated tests must prove:

1. A boss can die without completing a conflict.
2. A non-combat event can complete a conflict.
3. An objective interaction can be rejected when prerequisites are missing.
4. Two different event paths can reach different terminal resolutions.
5. The scenario can resolve through another actor while an eligible player still completes.
6. A dead challenger fails even when the shared scenario later resolves, unless verified lore establishes an exception.
7. Appraisal runs only after challenger completion is recorded.
8. Appraisal differences do not alter whether the global conflict ended.
9. Every terminal resolution uses the single teardown path.
10. Seed state changes only after the contained Nightmare's outcomes are committed.
11. Restart/reload preserves the resolution state without duplicating rewards or teardown.
12. Multiplayer challengers receive independent outcomes from one shared resolution.

Manual testing must additionally judge whether the player can understand the conflict, choices and consequences without reading implementation notes.

## 8. Anti-patterns forbidden by this roadmap

Do not build future Nightmares around any universal assumption that:

- every Nightmare has a boss;
- the boss must die;
- the strongest creature is the boss;
- the challenger must personally perform the final action;
- only a morally good ending counts;
- one global `won` flag represents all challengers;
- clicking the Seed or an objective block equals conquest;
- appraisal and completion are the same calculation;
- technical recovery is an in-world success;
- The Last Signal is the template every Nightmare must copy.

## 9. Next implementation order after preview feedback

1. Re-check the novel evidence for Nightmare endings, challenger survival, appraisal timing and Seed consequences.
2. Record chapter-level evidence or an honest `UNKNOWN` in the lore ledger.
3. Introduce `ResolutionGraph` and named terminal resolutions behind the existing scenario service.
4. Refactor The Last Signal's campfire click into one event in a multi-step conflict.
5. Add at least two valid DESIGN endings and separate completion from appraisal.
6. Add persistence/restart and idempotent-resolution tests.
7. Add multiplayer shared-resolution/per-challenger-outcome tests.
8. Only then design later Dream Realm Seed discovery, entry and post-resolution lifecycle.

This roadmap does not authorise broad content expansion by itself. It defines how Nightmare and Seed completion must work when that expansion is approved.