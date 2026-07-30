# Datapack release-completion audit

**Baseline:** `main` at `70d3548089e0ef3503ba260a9021cb23d4ccbacd`

**Working branch:** `gpt/datapack-release`

**Goal:** finish the vanilla 1.21.1 datapack as a polished, honest, releasable Phase 1 artifact before development moves to the Java mod. "Complete" here means the datapack has no known correctness bug or release-facing footgun that can reasonably be solved in a datapack. It does **not** mean forcing Java-era systems into commands.

---

## Definition of done

The datapack is ready to call complete when all of the following are true:

1. **Core loop is mechanically reliable.** Mundane -> Carrier -> First Nightmare -> Sleeper works through normal play, death, ejection, retry, reload/rejoin and test/reset paths.
2. **Known datapack-solvable bugs are gone.** Do not defer a real command-level correctness bug merely because Java is next.
3. **Known Java-only ceilings are safe and explicit.** A limitation may remain, but it must not silently corrupt another player's run or pretend to be canon.
4. **Public-facing terminology is honest.** Prototype mechanics must not be presented as canon systems when research says otherwise.
5. **The release UI is player-facing.** QA instrumentation may remain internally, but the player should not be dropped into a developer verification dashboard.
6. **Packaging is deterministic.** One documented command validates and builds the installable ZIP with `pack.mcmeta` at archive root.
7. **README describes the current release.** Historical docs stay historical; current installation, gameplay, limitations and credits are accurate.
8. **Known-issues documentation distinguishes current issues from history.** Fixed bugs must not look open.
9. **Automated verification passes.** `validate.py` and the Mineflayer harness pass on Claude's review environment. Per `docs/COLLABORATION.md`, GPT does not claim these passed without Claude running them.
10. **A final human release smoke test passes.** The full normal player loop is played once from a clean state on the built ZIP, plus the small set of visual/feel checks a bot cannot judge.

---

## Release work to do in the datapack

### A. Enforce the single-active-Nightmare ceiling

**Classification:** bug / unsafe prototype compromise.

The pack uses one global bossbar, global `ss_creature` selectors, global return storage during teardown, and a cleanup that kills every Nightmare Creature. Engineering notes correctly defer *true* per-player instances to Java, but the datapack currently relies on "single player at a time" as a convention rather than enforcing it.

**Release target:** a second player attempting to enter while another First Nightmare is active is refused at `nightmare/enter.mcfunction`, the same choke point that owns all other entry invariants. This converts cross-player corruption into an explicit temporary limitation.

Do **not** build an owner-tag/macro instance framework in the datapack.

### B. Preserve return-position precision

**Classification:** bug.

`ISSUES.md` records negative-coordinate truncation because entry stores `Pos[]` through integer scoreboard objectives. Player NBT cannot be written, but it can be read/copied to command storage.

**Release target:** preserve the player's real return coordinates in storage rather than reducing them to integer scoreboards. Add a behavioral regression that starts from fractional negative coordinates and proves the return is close to the original position.

### C. Make the advancement tab player-facing

**Classification:** release polish.

The existing visible `Shadow Slave — Verification` tree is excellent instrumentation but reads like QA tooling. Keep its IDs/grant points so harness checks remain stable, but rewrite display text into a normal Shadow Slave progression/achievement tab. Debug meaning belongs in tests/docs, not the player's advancement screen.

### D. Rename the player-facing placeholder Flaw `Shadow Slave`

**Classification:** canon deviation / release polish.

`Shadow Slave` is Sunny's formal Divine Aspect, not a Flaw. Keep the internal `ss_flaw_shadow_slave` id/tag for save and test compatibility, but give the invented sunlight placeholder a clearly invented display name. Do not silently imply it is canon.

### E. Remove developer chatter from ordinary load UX

**Classification:** release polish.

The current `/reload`/world-load message advertises testing commands to every player. Keep the version/Spell announcement; testing commands remain documented and available to operators.

### F. Deterministic release build

**Classification:** release engineering.

Add a standard-library build script that:

- runs the static validator first;
- reads the version from `pack.mcmeta` rather than duplicating it;
- creates an ignored `dist-shadowslave-<version>.zip`;
- places the *contents* of `shadowslave/` at archive root, especially `pack.mcmeta`;
- excludes development-only Python cache/artifacts;
- fails loudly on validation or malformed version metadata.

The build script does not replace the live Mineflayer harness; Claude still runs that before merge/release.

### G. Current release documentation

**Classification:** documentation correctness.

- Fix README structure after the progression/prototype split.
- Document the enforced one-active-Nightmare limitation.
- Update the lore/credit wording to reflect `docs/lore-research/` rather than the old wiki-only source-of-truth wording.
- Add a current release acceptance section to `TESTING.md`; keep older test plans marked historical.
- Put a current-status summary at the top of `ISSUES.md` and mark already-resolved stale entries as resolved without rewriting the historical record.

---

## Deliberately deferred to Java

These are **not** blockers for declaring the datapack complete, provided the limitations remain explicit:

- real simultaneous Nightmare instances and ownership;
- exact ownership of death drops (the current all-loose-item sweep remains documented and safe only because concurrent trials are refused);
- procedural unique Aspect generation;
- personal/derived Flaw generation;
- real Dream Realm / Sleeper -> Awakened journey;
- Memories and Soul inventory GUI;
- Soul Essence/core systems beyond the Phase 1 readout;
- Echoes/custom summoned entities;
- bespoke Nightmare Creature AI/models;
- full Dream Realm worldgen and region systems.

Do not build a large datapack framework that will immediately be replaced by `SoulData`, Java networking, menus, item data components and Nightmare instance ownership.

---

## Release decisions still owned by Andrew

Per `ENGINEERING-NOTES.md`, these should not be silently decided by an agent:

- final balance changes to countdown, health thresholds, cooldown and creature stats;
- any change that materially alters the feel of the First Nightmare;
- public distribution/licensing choices;
- whether the final release should keep the current non-canon `Cast Out` safety mechanic as the default.

Everything else above can be prepared and reviewed on this branch.
