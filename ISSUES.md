# Shadow Slave — known issues

**Status date:** 2026-10-22

GitHub Issues is empty; this file tracks the known problems. The August list (recovery proofs,
Nightmare durability, and issues numbered in the previous repository history) is archived in
[docs/history/ISSUES-2026-08-13.md](docs/history/ISSUES-2026-08-13.md).

## CI

- **The datapack harness is occasionally flaky.** The `datapack` job in Preview gates failed once
  in its last 26 runs: the two-player regression (`testserver/regression_issue20.mjs`) read the
  Nightmare trial lock as released right after a player disconnected. Earlier failures came from
  the test server falling behind ("Can't keep up"). Re-run the failed job, then re-run All checks,
  which finishes before the re-run does. The datapack is frozen, so this is not being fixed unless
  it gets worse.

## Documentation

- `mod/README.md`, `mod/PREVIEW-PLAY-GUIDE.md`, `mod/IMPLEMENTATION-STATUS.md` and parts of `docs/`
  still describe the August preview (Kindle, Last Light, the First Nightmare). Use
  [PLAYTEST.md](PLAYTEST.md) and [PROJECT-STATUS.md](PROJECT-STATUS.md) for the current state.

## Gameplay limits

- Only four of the twelve Natures have kits.
- Awakening is the only rank-up; later ranks are not implemented.
- Shards, essence and refinement values are first-pass numbers awaiting playtest feedback.
