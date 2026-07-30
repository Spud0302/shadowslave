# Pre-1.0 datapack completion roadmap

**Baseline:** `main@9f0cca9a0342f340cbdd9a72514363129f2156eb` (`0.5.0`)

**Owner decision:** the datapack remains pre-1.0 until it is a complete, playable reference implementation of the initial idea and is good enough to become the behavioural baseline for the Java mod. Pride Versioning remains `PROUD.DEFAULT.SHAME`; `1.0.0` is the first completed datapack release we are proud to freeze and translate.

This roadmap is a completion boundary, not a promise to consume every minor number. DEFAULT releases are milestones that actually shipped; SHAME releases are fixes discovered while verifying them.

## `0.6.0` target — make the First Nightmare a finished experience

Intent: the Phase 1 loop already works mechanically. This release makes the loop read as a game and gives the future Java implementation the right Nightmare abstraction instead of teaching it that every Nightmare is intrinsically `timer -> boss -> kill`.

Changes prepared on `gpt/v0.6-experience`:

- keep the current First Nightmare scenario and balance intact;
- move its win-condition machinery behind `nightmare/objective_tick`, a deliberately small scenario/objective seam;
- keep entry and teardown choke points unchanged;
- add countdown presentation at meaningful thresholds without changing the 90-second timer;
- improve the First-Nightmare completion/appraisal copy so the generated Flaw feels like a consequence of the trial rather than a random debuff;
- do not add Memories, Dream Realm progression, GUI, custom AI, or Java-bound ownership machinery.

Acceptance:

1. static validator clean;
2. existing full harness clean with no assertion weakened to accommodate the refactor;
3. human playthrough confirms warnings fire once, do not spam, and improve rather than obstruct the trial;
4. killing the creature still follows the exact existing survive -> leave -> become Sleeper path;
5. test countdown skip (`ss_timer 1`) still works.

## `0.7.0` target — verification and Java handoff hardening

Intent: close the known `0.5.0` coverage gap and make the datapack state model explicit enough that the Java port can import it instead of reverse-engineering it.

Changes prepared on `gpt/v0.7-hardening`:

- deterministic test-only entry points for each earned Flaw family;
- automated checks for generated score bands, exactly-one-family selection, persistent attribute burden application, and cleanup;
- `test/reset` clears short-lived pack effects as well as scores/tags/modifiers so test order cannot leak state;
- Java handoff contract maps datapack state to `SoulData`, `AspectInstance`, `FlawInstance`, and Nightmare services;
- release checklist states the exact evidence required before `1.0.0`.

Acceptance:

1. validator clean;
2. normal harness clean;
3. deterministic Flaw-family harness clean;
4. classification itself remains a human test: the test hooks prove what a selected family does, not that real gameplay selected the right family;
5. no runtime gameplay path calls a `test/` function.

## `0.8.x` — correction release only if earned

Do not invent a `0.8.0` feature just because the number is available.

Use a DEFAULT bump here only if the human playtest reveals a meaningful gameplay/presentation change that is larger than an embarrassing fix. Use SHAME on the current DEFAULT line for ordinary bugs.

Examples of legitimate work here:

- a warning is badly timed or unreadable;
- the generated identity reveal is confusing;
- a real Flaw family classifies behaviour poorly enough to need threshold/design changes;
- fresh-world onboarding misses a required instruction;
- a Java migration seam is ambiguous when checked against real state.

## `0.9.x` — release-candidate freeze

No new systems.

A `0.9.0` release is justified when the datapack is feature-complete but Andrew wants an explicit release-candidate build before the PROUD bump. After that, only SHAME fixes, documentation corrections, balance corrections, and release packaging changes are allowed.

Required RC evidence:

- fresh Minecraft 1.21.1 world install from the generated ZIP;
- validator passes;
- all automated harnesses pass repeatedly;
- full natural player loop passes without test commands;
- all four Flaw families have been manually observed or deliberately forced and mechanically verified;
- death/ejection/item-return checks pass;
- `/trigger soul` is accurate for Mundane, Carrier, and Sleeper;
- known Java-bound limitations are documented and are not disguised as completed datapack features;
- README, CHANGELOG, ISSUES, TESTING and Java handoff agree on current behaviour.

## `1.0.0` — completed datapack / Java baseline

`1.0.0` means:

> The vanilla datapack is a complete playable vertical slice of the initial Shadow Slave idea, stable enough to freeze as the behavioural reference implementation for the Java mod.

It does **not** mean the eventual Shadow Slave mod is feature-complete.

The datapack must prove these contracts:

- Mundane -> Carrier -> First Nightmare -> Sleeper (Dormant);
- centralised Nightmare entry eligibility;
- one shared teardown path for every exit reason;
- a Nightmare scenario/objective boundary rather than hard-coding future Java around boss kills;
- generated Aspect identity over finite executable mechanics;
- Flaw family derived from observed trial behaviour, with personal identity randomness layered afterward;
- persistent player identity and an honest Soul readout;
- deterministic test/reset tools and trustworthy automated gates;
- explicit migration mapping for every persistent prototype value the Java importer must understand.

At `1.0.0`, stop extending the datapack. Future systems — Dream Realm, actual Awakening, Memories, Soul/Core data, Gates, custom entities/AI, GUI, real multiplayer Nightmare instances — begin in Java.
