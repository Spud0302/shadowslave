# Resolution and challenger outcome engine

**Status:** bounded pure-domain foundation; not wired into The Last Signal yet.

## Purpose

This slice replaces the future assumption `objective event -> global victory` with two independent decisions:

1. did the reconstructed scenario reach a named terminal resolution;
2. what outcome applies to each challenger after that shared resolution.

## Evidence checked

- **CANON:** Chapter 742 presents a reconstructed Nightmare reaching its end after the decisive historical conflict changes state.
- **CANON:** Chapter 743 places appraisal after the Nightmare is over and judges the challenger's performance rather than deciding whether the world ended.
- **CANON:** Chapter 1581 describes a Nightmare collapsing and expelling the surviving challengers, while the missing appraisal space is exceptional.
- **CANON:** Chapter 459 describes later Seeds as cohort challenges rather than necessarily individual trials.
- **CANON:** Chapter 2157 later confirms group-oriented Second Nightmares.
- **INFERRED:** a reusable implementation should represent shared scenario resolution separately from per-challenger survival and eligibility.
- **DESIGN:** named states, deterministic event edges, rejection reasons and the exact outcome enum are Minecraft domain choices.
- **UNKNOWN:** unusual cases where a dead, absent or invalidated challenger might still receive a special result. The current evaluator deliberately fails closed rather than inventing exceptions.

## Current contract

`ResolutionGraph` validates:

- one named initial state;
- deterministic `(state, event)` edges;
- known source and target states;
- named terminal resolution IDs.

Offering an event can:

- advance to another non-terminal state;
- reach one named terminal resolution;
- be rejected because prerequisites are missing;
- be rejected because the scenario already ended.

`ChallengerOutcomeEvaluator` then records one independent outcome per challenger. Technical recovery and administrator abort remain explicit technical outcomes and never masquerade as Spell success.

## Integration boundary

This PR does not:

- change the live campfire objective;
- persist graph state inside `NightmareInstance`;
- invoke appraisal;
- add a second playable ending;
- implement later Seed entry or lifecycle rules.

The next integration slice should persist one resolution state behind The Last Signal, translate the campfire interaction into an event, and preserve PR #39's durable completion receipt as the only success transaction.
