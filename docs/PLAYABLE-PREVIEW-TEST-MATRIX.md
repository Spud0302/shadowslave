# Playable preview bulk test matrix

**Target:** `0.1.0-preview.2` on PR #19  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Review mode:** accumulated Claude review plus Andrew play feedback  
**Human status:** corrected build not yet played; pending is not passed

## GPT Java checkpoint

GitHub Actions `Java core` run **34** / ID **30686670446** completed successfully.

| Area | Evidence | Result |
| --- | --- | --- |
| Wrapper | Gradle wrapper validation | passed |
| Java build and tests | `./mod/gradlew -p mod build --stacktrace` | passed |
| Physical client | Shadow Slave client + GUI-atlas startup markers | passed |
| Dedicated server | Shadow Slave load + server-ready marker | passed |
| Packaging | `shadowslave-0.1.0-preview.2.jar` | passed |
| Artifact upload | artifact ID `8814240590` | passed |

```text
archive SHA-256  a7ee670001042ee9c783ceb191e667fefdf043acd1b6fa498438434907291d79
JAR SHA-256      48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Claude must reproduce rather than trust the workflow:

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

## Required frozen-datapack gate — pending

The Java workflow does not boot the vanilla datapack harness. Run against a deployed Minecraft 1.21.1 server:

```bash
cd testserver
npm run deploy
npm test
```

Expected suites:

- lifecycle: 32/32;
- Flaw: 39/39;
- issue #20 serialization regression: Alice enters, Bob is refused cleanly, Alice completes, Bob enters after teardown.

Do not claim this gate passed merely because `regression_issue20.mjs` is wired into `npm test`.

## Correction regression matrix

| Issue | Required evidence |
| --- | --- |
| #20 | second frozen-datapack entrant is refused before `ss_in_nightmare`, shared creature, bossbar, or progression state; first trial completes; teardown releases slot |
| #21 | README and authoritative `ISSUES.md` state one active datapack First Nightmare and explain the former blocked-victory/trapped-player symptom |
| #22 | malformed Soul records return `DataResult.error`; parse call does not throw raw invariant exceptions |
| #23 | negative, zero, unknown, and future schemas reject; schema 1 migrates explicitly; schema 2 receives no schema-1 repair |
| #24 | every Nightmare-Spell state from Dreamer through God rejects missing Aspect/Flaw identity |
| #25 | completed player without Carrier tag rejects; modern two-digit Aspect/Flaw score without matching mechanics tag rejects |
| #26 | `test/reset` restores enough health for normal entry after max-health modifier removal and low-health setup |

## Existing automated domain coverage

| Rule | Test/evidence |
| --- | --- |
| Missing legacy score differs from explicit stored zero | `LegacyDatapackReaderTest` |
| Frozen mappings remain exact and fail closed | `DatapackMigrationTranslatorTest` |
| Imported metadata codec round-trip | `ImportedIdentityDataTest` |
| Wrong migration read-back rejects | `DatapackMigrationPersistenceVerifierTest` |
| Revealed identity paired presence | `SoulIdentityDataTest` |
| Bounded Soul snapshot and independent ranks | `SoulSnapshotTest` |
| Nightmare owner/role/return/layout/pursuer record round-trip | `NightmareInstanceTest` |
| Different instance slots use different origins | `NightmareInstanceTest` |
| Kindle cooldown codec and guard | `PreviewPowerDataTest` |
| Bundled dimension resources load | workflow run 34 dedicated-server smoke |

## Claude code-review matrix

### Soul persistence and schema

- `SoulData.CODEC` decodes into a non-throwing storage record first.
- Domain constructor exceptions become `DataResult.error`, never a silent reset.
- Stored schema is checked before migration.
- Schema 1 migration is explicit and cannot silently repair schema 2.
- Future schemas reject rather than being rewritten as current.
- Post-First-Nightmare Spell progression cannot lose permanent identity.

### Live migration

- Reader uses direct scoreboard API, not command/chat parsing.
- Missing objective/player score maps to absence deliberately; read failures do not become zero.
- Established Java Soul/identity is not overwritten.
- Provisional writes and exact read-back precede final marker.
- Soul, identity, and imported metadata roll back together.
- Legacy scores/tags remain.
- Completed state and every mechanics family tag are validated consistently.

### Java Nightmare lifecycle

- `NightmareService.tryEnter` is the only entry choke point.
- Overworld `SavedData` enforces one active instance per owner UUID.
- Role, scenario, layout, pursuer, return, and recovery state belong to `NightmareInstance`.
- Per-player slots and exact entity UUID cleanup prevent cross-player interference.
- Signal restoration resolves the preview conflict; pursuer death is not completion.
- Success, technical recovery, admin abort, and canonical death converge on shared teardown.
- Technical recovery is not presented as mercy from the Spell.

### Appraisal, identity, and powers

- Fixed preview appraisal remains explicitly DESIGN.
- Dreamer Soul Rank is Dormant while Last Light Aspect Rank is Awakened.
- Appraisal failure cannot leave half an identity.
- Kindle/cooldown and Cold Ash drawback are server-authoritative.
- Client payload cannot submit progression, identity, or cooldown values.

## Manual Java play matrix — not yet performed

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only `shadowslave-0.1.0-preview.2.jar` and confirm the mod is listed.
2. Join and press **O**; confirm Uninfected state.
3. Run `/shadowslave preview_begin`; confirm development-shortcut wording.
4. Confirm Carrier -> Aspirant/Dormant on entry.
5. Confirm The Last Signal builds without suffocation or void placement.
6. Confirm role and conflict are understandable.
7. Confirm the pursuer pressures but is not required for completion.
8. Right-click the unlit soul campfire; confirm exact return dimension/location and exactly-once completion.
9. Confirm Dreamer/Dormant, Last Light/Awakened, Kindle, and Cold Ash on the Soul screen.
10. Run `/shadowslave kindle`; confirm effect, cooldown refusal, and reuse.
11. Enter water/rain; confirm Weakness applies and expires after leaving.
12. Quit/reload at Carrier, Aspirant, and Dreamer points; confirm persistence.
13. Test `/shadowslave nightmare_recover` and technical wording.
14. Die in the Nightmare; confirm cleanup and explicit development accommodation wording.
15. Run `/shadowslave preview_reset`; confirm cleared state.
16. With two Java-preview players, confirm separate slots and independent completion.
17. On a backed-up legacy world, test migration, retained evidence, rejection of inconsistent saves, and idempotency.

## Known deferred risks

- No human has run the complete corrected interaction loop.
- No GameTest simulates real logout/login.
- Active-instance restart recovery is not end-to-end automated.
- The pursuer is a vanilla Husk placeholder.
- Full mechanics for every imported identity are not implemented.
- Visual layout, pacing, balance, and readability need Andrew's feedback.

## Verdict vocabulary

Claude should record exactly one:

- **verified**;
- **verified with fixes**;
- **blocked**;
- **verified machine-checkably; human feedback pending**.
