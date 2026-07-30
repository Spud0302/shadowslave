# Shared vertical-slice acceptance specification

This specification is implementation-neutral. The Nightmare Spell modpack track and standalone Java mod track must both pass it before the architecture comparison is scored.

The datapack `1.0.0` is the behavioural reference where this document is silent. This document may deliberately require stronger Java-era behaviour, especially explicit instance ownership and multiplayer isolation.

## Test identities

- **Player A:** fresh profile, no imported datapack state.
- **Player B:** fresh profile used for multiplayer isolation.
- **Imported Carrier:** datapack player with `ss_carrier` and no completed rank.
- **Imported Sleeper:** datapack player with completed rank, generated Aspect and generated Flaw.

## VS-01 — untouched player remains Mundane

**Given** Player A has no Soul state  
**When** the server runs normally without the player sleeping  
**Then** no infection, rank, Aspect or Flaw is silently assigned  
**And** the Soul interface identifies the player as Mundane/untouched.

## VS-02 — first sleep creates a Carrier

**Given** Player A is Mundane  
**When** Player A completes the first qualifying ordinary sleep  
**Then** the server creates persistent Soul data  
**And** marks the player as a Carrier  
**And** does not assign a completed First-Nightmare Aspect or Flaw  
**And** the state survives logout and server restart.

## VS-03 — Carrier enters a First Nightmare

**Given** Player A is a Carrier and passes every entry guard  
**When** Player A completes the later sleep/bed interaction  
**Then** one Nightmare instance is created  
**And** Player A is recorded as its participant  
**And** the return location is captured  
**And** the chosen scenario is recorded by an ID  
**And** all spawned objective entities are owned by that instance.

## VS-04 — duplicate entry is rejected at the service boundary

**Given** Player A already has an active Nightmare instance  
**When** any caller requests another entry  
**Then** the central Nightmare service rejects it  
**And** no second instance, return location or objective is created.

## VS-05 — trial evidence is captured per participant

**Given** Player A is in an active First Nightmare  
**When** the player reaches configured evidence conditions such as near-collapse, hunger consumption or sustained retreat  
**Then** evidence is appended to the active instance's participant record  
**And** it is not stored as an unrelated global flag  
**And** evidence from Player B cannot affect Player A.

## VS-06 — victory produces a Sleeper

**Given** Player A completes the scenario's central conflict  
**When** the objective reports victory  
**Then** the instance exits through the shared teardown path  
**And** Player A returns safely  
**And** becomes a Sleeper/Dreamer with Dormant Soul Rank  
**And** receives one Aspect identity  
**And** receives one Flaw identity derived from the recorded trial evidence  
**And** all identity data persists across restart.

## VS-07 — Aspect and Flaw are instances, not display-only classes

**Given** Player A is a Sleeper  
**When** the Soul state is read  
**Then** the Aspect has a stable instance ID, formal name, rank and ability references  
**And** the Flaw has a stable instance ID, formal name, family and parameters  
**And** the active mechanics resolve from those records  
**And** removing a third-party integration does not erase the identity records.

## VS-08 — Flaw has a real reliable cost

**Given** Player A has a generated Flaw  
**When** the relevant gameplay condition is exercised  
**Then** the drawback is observable and server-authoritative  
**And** it does not silently fail while the UI still claims it is active  
**And** its cleanup occurs only through defined progression, migration or administrative behaviour.

## VS-09 — ejection uses shared teardown

**Given** Player A is in an active Nightmare  
**When** an ejection condition is reached  
**Then** the same exit service used by victory performs teardown  
**And** the exit reason is EJECTION  
**And** the player is returned without receiving victory progression  
**And** owned entities, temporary effects and instance state are cleaned up  
**And** retry/cooldown state is explicit and persistent as designed.

## VS-10 — death and reconnect recover safely

**Given** Player A is in an active Nightmare  
**When** the player dies, disconnects, or the server restarts  
**Then** the recovery policy produces one valid state  
**And** the player is never simultaneously inside and outside an instance  
**And** items are not stranded in an unreachable abandoned instance  
**And** duplicate objective rewards cannot be claimed.

## VS-11 — simultaneous players are isolated

**Given** Player A and Player B are both eligible  
**When** they enter separate Nightmares  
**Then** each receives a distinct instance ID  
**And** each has independent objective state, entities, boss presentation and return location  
**And** victory or failure in one instance does not modify the other.

## VS-12 — Soul interface is server-synchronised

**Given** Player A opens the Soul interface  
**When** the client requests the screen  
**Then** the server sends an owning-player snapshot  
**And** the snapshot contains only required display/action data  
**And** the client cannot set its own rank, Aspect, Flaw, resources or cooldowns  
**And** stale snapshots are replaced after authoritative mutations.

## VS-13 — datapack Carrier import

**Given** Imported Carrier has legacy datapack state  
**When** Java migration runs  
**Then** it constructs valid Soul data in memory first  
**And** validates it  
**And** persists the Java record  
**And** marks migration complete only afterward  
**And** preserves Carrier progression without inventing an Aspect or Flaw.

## VS-14 — datapack Sleeper import

**Given** Imported Sleeper has generated datapack identity  
**When** Java migration runs  
**Then** the player remains a Sleeper/Dreamer with Dormant rank  
**And** the generated Aspect name and legacy root are retained  
**And** the generated Flaw name and semantic family are retained  
**And** historical internal tag names are not shown as formal names  
**And** no identity is rerolled.

## VS-15 — migration is idempotent

**Given** a player has already migrated successfully  
**When** migration detection runs again  
**Then** it does not duplicate history, abilities, items, instances or identity  
**And** it does not reinterpret obsolete datapack scratch scores.

## VS-16 — dedicated server boot

**Given** the client is absent  
**When** a dedicated NeoForge server starts with the prototype installed  
**Then** no client-only class is loaded  
**And** registrations and data resources complete  
**And** the server can host the full shared slice.

## VS-17 — clean uninstall/dependency failure behaviour

**Given** an optional integration mod is absent  
**When** the server loads a Soul containing an ability adapter from that integration  
**Then** the canonical Soul and identity still load  
**And** the missing adapter becomes unavailable with a clear diagnostic  
**And** the save is not deleted or silently rerolled.

## VS-18 — administrative teardown

**Given** an instance is stuck or a player requires recovery  
**When** an authorised administrator invokes teardown/recovery  
**Then** the operation is logged  
**And** uses the normal service boundary  
**And** leaves the player and global instance registry consistent.

## Evidence required

For each path, record:

- build identifier and exact dependency manifest;
- automated test output;
- dedicated-server log;
- save/restart test result;
- two-player isolation result;
- import fixture result;
- observed gameplay notes;
- defects and workarounds;
- implementation time for the slice;
- any acceptance case skipped and the reason.

A path has not passed because its developer believes the code should work. It passes when the behaviour above has been observed or asserted through a trustworthy test.
