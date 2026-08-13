# Nightmare Seed expansion safety boundary

**Status:** design constraint; no runtime implementation authorized by this document.

## Lore boundary

Repository lore research already distinguishes the First Nightmare from later Nightmares:

- the First Nightmare's Seed grows inside an infected human soul and is an individual trial;
- from the Second Nightmare onward, Seeds grow in the Dream Realm and can be challenged by multiple participants;
- if a later Seed matures, it blooms into a Nightmare Gate in the waking world;
- killing Gate creatures or a Gate Guardian does not permanently resolve the underlying threat while the Seed remains unconquered.

See `docs/lore-research/section-d-nightmares.md`.

This document does not claim a new canon rule. It records the implementation consequence of those established constraints.

## Base-mod responsibility

`shadow-slave.jar` should own the generic Nightmare Seed/Gate framework because Seeds and Gates are fundamental Nightmare Spell/world concepts rather than content belonging to one later-Nightmare expansion.

The base may own generic concepts such as:

- later-Nightmare Seed identity and tier/category;
- discovery, persistence, maturation and Gate transition state;
- participant/cohort and entry contracts;
- scenario-provider registration and compatibility checks;
- provider-independent technical recovery metadata for active Nightmare participants;
- Gate/world-threat state that derives from a resolvable Seed;
- safe handling when optional content is absent or removed.

The base must not need to know the authored historical scenario, structures, NPCs, creatures, resolution graph or other content supplied by a particular expansion.

## Resolvability invariant

> **Never activate a persistent escalating world threat unless the current installation contains at least one legitimate player route to resolve it.**

For later Nightmares this means a Seed tier/provider may become active only when compatible Nightmare content is registered and can actually be entered/conquered under the current installation.

Conceptually:

```text
no compatible Nightmare provider
    -> no active Seed generation for that provider/tier
    -> no unavoidable Gate maturation from unavailable content

compatible provider installed
    -> eligible Seeds may appear
    -> players can discover/challenge them
    -> ignored Seeds may mature into Gates
    -> conquering the underlying Seed remains the durable resolution path
```

A later-Nightmare expansion therefore activates both sides of the system: **the escalating threat and the means of defeating it**.

## Addon removal / missing-content safety

Optional expansion removal must not doom an existing save.

If persisted unresolved state loses the provider needed to resolve it, the base framework must fail safe rather than merely stop creating new content. The default contract is:

1. retain the durable Seed/Gate/provider identity for restoration, migration and diagnosis;
2. mark the affected Seed/provider route unavailable and freeze further maturation/escalation;
3. if the Seed already bloomed, suspend or safely contain the linked Gate and its continuing hostile escalation while preserving that it is **unresolved** — missing content must never be interpreted as victory or permanent closure;
4. if players are already inside a provider-owned active Nightmare instance, use base-owned lifecycle/recovery metadata to abort or technically recover those participants to a safe base-known location before the provider-specific runtime is considered unavailable; retain enough recovery evidence to retry or diagnose failures, and do not award successful completion/appraisal merely because the provider disappeared;
5. do not manufacture a new Gate, encounter escalation or other persistent threat whose durable resolution path is unavailable;
6. resume the suspended Seed/Gate/instance only after a compatible provider is restored, or complete an explicit migration/administrative recovery that establishes a new valid authority path.

The recovery path must be content-independent: the base does not need to reconstruct a missing provider's structures or `ResolutionGraph` in order to return participants safely or suspend an active Gate. Provider-specific cleanup may improve presentation, but it cannot be the only way to avoid a stranded player or unwinnable persistent threat.

Exact UX, suspended-Gate presentation, migration semantics and administrator tooling remain DESIGN/UNKNOWN until implemented.

## Expansion boundary

A later-Nightmare content JAR should register authored content against the base framework rather than redefining Seed/Gate semantics. For example, a Second Nightmare expansion may provide:

- one or more historical scenarios;
- historical roles/body mappings;
- structures/world reconstruction;
- scenario-specific NPCs and Nightmare Creatures;
- central-conflict/resolution graphs;
- original-history baselines and appraisal evidence;
- tier-appropriate rewards/advancement presentation.

`shadow-slave.jar` remains authority for persistent player progression, Nightmare lifecycle contracts, technical participant recovery, Seed/Gate identity and cross-expansion compatibility.

## Broader addon rule

The same safety principle should be reused for other optional persistent systems: optional content may increase variety, difficulty and progression possibilities, but removing it must not leave base-owned persistent state in an unwinnable, permanently escalating, or player-stranding condition.

This is an architectural constraint, not permission to split existing runtime systems into separate JARs yet.
