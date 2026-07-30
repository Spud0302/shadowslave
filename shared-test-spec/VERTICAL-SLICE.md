# Shared vertical-slice acceptance specification

<!-- shared-slice-current-status -->
> **CURRENT STATUS:** neither Path A nor Path B has completed this slice. The shared Java foundation
> covers persistence, snapshots, UI and pure migration translation; natural infection, real instance
> ownership, a playable historical conflict and appraisal remain outstanding. Issue #16 must close
> before new feature packages merge.

This specification is implementation-neutral. The Nightmare Spell modpack track and standalone Java mod track must both pass it before the architecture comparison is scored.

The frozen datapack remains a migration and regression reference, but Java-era behaviour follows `docs/JAVA-LORE-ALIGNMENT.md` where the datapack used a format-driven approximation.

## Test identities

- **Player A:** fresh profile, no imported state.
- **Player B:** fresh profile used for multiplayer isolation.
- **Imported Carrier:** datapack player with `ss_carrier` and no completed rank.
- **Imported Sleeper:** datapack player with completed rank, generated Aspect and generated Flaw.

## VS-01 — uninfected player has no invented Rank zero

**Given** Player A has no Soul state  
**When** the server loads or the Soul screen opens  
**Then** the player is identified as uninfected/Mundane  
**And** Soul Rank is absent rather than `Mundane`  
**And** no Aspect, Aspect Rank or Flaw is assigned.

## VS-02 — infection creates a Carrier

**Given** Player A is uninfected  
**When** the configured infection event marks the player  
**Then** the server creates persistent Soul data  
**And** marks the player as a Carrier on the Nightmare Spell path  
**And** does not assign Soul Rank, permanent Aspect or Flaw  
**And** the state survives logout and server restart.

Ordinary sleep may be used by a clearly labelled development shortcut, but it is not treated as the canonical cause of infection.

## VS-03 — First Nightmare trigger creates an Aspirant

**Given** Player A is a Carrier and passes every trigger guard  
**When** supernatural exhaustion/forced-sleep logic starts the First Nightmare  
**Then** the player becomes an Aspirant  
**And** gains a Dormant Soul Core  
**And** one individually owned Nightmare instance is created  
**And** the return/recovery state is captured  
**And** the chosen scenario is recorded by an ID.

## VS-04 — duplicate entry is rejected at the service boundary

**Given** Player A already has an active Nightmare instance  
**When** any caller requests another entry  
**Then** the central Nightmare service rejects it  
**And** no second instance, role, return state or objective is created.

## VS-05 — historical role is instance-owned, not permanent Soul identity

**Given** Player A enters a First Nightmare  
**When** the scenario assigns a historical role/body and provisional trial context  
**Then** those values belong to the Nightmare instance participant record  
**And** temporary role Attributes or abilities are not silently written as the final permanent Aspect  
**And** teardown cannot leave the waking player trapped in the historical body.

## VS-06 — central conflict is scenario-defined

**Given** Player A is inside a First Nightmare  
**When** the player interacts with its reconstructed historical situation  
**Then** progress is measured against a central conflict  
**And** the conflict may support more than one resolution where designed  
**And** a boss kill counts only when it actually resolves that conflict  
**And** scenario logic is not hard-coded into the player lifecycle service.

## VS-07 — meaningful evidence is captured without claiming a canon formula

**Given** Player A is in an active First Nightmare  
**When** the player makes significant choices or demonstrates sustained behaviour  
**Then** evidence is appended to the active instance's participant record  
**And** it is not stored as unrelated global flags  
**And** evidence from Player B cannot affect Player A  
**And** the appraisal engine labels its weighting as project design rather than a canon algorithm.

## VS-08 — victory and appraisal produce a Dreamer/Sleeper

**Given** Player A resolves the scenario's central conflict  
**When** the objective reports victory and appraisal completes  
**Then** the instance exits through the shared teardown path  
**And** Player A becomes a Dreamer, socially called a Sleeper  
**And** Soul Rank remains Dormant  
**And** one permanent Aspect identity and independent Aspect Rank are revealed  
**And** one permanent Flaw identity/effect is revealed  
**And** all identity data persists across restart.

## VS-09 — Aspect Rank is independent from Soul Rank

**Given** Player A is a Dormant Dreamer  
**When** the appraised Aspect has a Rank other than Dormant  
**Then** the Soul record preserves both values independently  
**And** UI, networking and ability resolution do not infer Aspect Rank from Soul Rank.

## VS-10 — Aspect and Flaw are instances, not fixed datapack classes

**Given** Player A is a Dreamer  
**When** the Soul state is read  
**Then** the Aspect has stable identity, formal-name state, Rank, nature and ability references  
**And** the Flaw has stable identity, formal-name state and effect parameters  
**And** unknown formal names remain unknown rather than being invented from effect labels  
**And** removing a third-party integration does not erase either identity.

## VS-11 — Flaw has a real reliable cost

**Given** Player A has a revealed Flaw  
**When** the relevant gameplay condition is exercised  
**Then** the drawback is observable and server-authoritative  
**And** it does not silently fail while the UI still claims it is active  
**And** it is not casually removed like a temporary potion debuff.

## VS-12 — ordinary failure is lethal, not safe ejection

**Given** Player A fails or dies in a First Nightmare under canonical rules  
**When** failure resolves  
**Then** the player does not receive Dreamer progression  
**And** the death is treated as real  
**And** the configured First-Nightmare Gate consequence or its explicit prototype placeholder is recorded  
**And** the game does not describe the result as a normal retry granted by the Spell.

An optional accessibility mode may change this outcome, but must be labelled non-canon and tested separately.

## VS-13 — crash and administrative recovery are technical paths

**Given** Player A disconnects, the server restarts, or an instance becomes corrupt  
**When** recovery runs  
**Then** it produces one consistent technical state  
**And** the player is never simultaneously inside and outside an instance  
**And** items and owned entities are not stranded  
**And** recovery is logged as technical/administrative rather than ordinary lore ejection  
**And** duplicate rewards cannot be claimed.

## VS-14 — simultaneous players are isolated

**Given** Player A and Player B are eligible  
**When** they enter separate First Nightmares  
**Then** each receives a distinct instance ID, role, conflict state and return record  
**And** each has independent entities and presentation  
**And** victory, death or recovery in one instance does not modify the other.

## VS-15 — Soul interface is server-synchronised

**Given** Player A opens the Soul interface  
**When** the client requests the screen  
**Then** the server sends an owning-player snapshot  
**And** status, awakening path, Soul Rank and Aspect Rank are separate fields  
**And** absent Soul Rank renders as absent rather than Mundane  
**And** the client cannot set rank, Aspect, Flaw, evidence, resources or cooldowns  
**And** stale snapshots are replaced after authoritative mutations.

## VS-16 — datapack Carrier import

**Given** Imported Carrier has legacy datapack state  
**When** Java migration runs  
**Then** it constructs valid Soul data in memory first  
**And** maps the player to Carrier on the Nightmare Spell path with no Soul Rank  
**And** validates and persists the Java record before marking migration complete  
**And** does not invent an Aspect or Flaw.

## VS-17 — datapack Sleeper import

**Given** Imported Sleeper has generated datapack identity  
**When** Java migration runs  
**Then** the player becomes a Dreamer/Sleeper with Dormant Soul Rank  
**And** the generated Aspect name/root and legacy Dormant Aspect Rank are retained  
**And** the generated Flaw name and semantic family are retained  
**And** historical internal tag names are not shown as formal names  
**And** no identity is rerolled  
**And** the imported four-family prototype is not treated as the complete Java taxonomy.

## VS-18 — alpha schema migration is compatible

**Given** a development save stores alpha schema 1 values such as `mundane` or `sleeper`  
**When** schema 2 loads  
**Then** `mundane` maps to uninfected with absent Soul Rank  
**And** `sleeper` maps to Dreamer  
**And** legacy completed Aspect identity receives an explicit imported Aspect Rank  
**And** the migration is idempotent.

## VS-19 — dedicated server and physical client both boot

**Given** the prototype is installed  
**When** a dedicated NeoForge server starts without client classes  
**Then** registrations and data resources complete  
**And** the server reaches ready state  
**And** a physical client separately loads the Soul UI and networking without duplicate registration.

## VS-20 — optional dependency failure preserves canonical identity

**Given** an optional integration mod is absent  
**When** the server loads a Soul containing an ability adapter from that integration  
**Then** the canonical Soul, Aspect and Flaw still load  
**And** the missing adapter becomes unavailable with a clear diagnostic  
**And** the save is not deleted or silently rerolled.

## Evidence required

For each path, record:

- build identifier and exact dependency manifest;
- automated test output;
- physical-client and dedicated-server logs;
- save/restart test result;
- two-player isolation result;
- import and schema-migration fixture results;
- observed gameplay and lore-alignment notes;
- any non-canon accessibility/configuration choices;
- defects and workarounds;
- implementation time for the slice;
- any acceptance case skipped and the reason.

A path has not passed because its developer believes the code should work. It passes when the behaviour above has been observed or asserted through trustworthy evidence.
