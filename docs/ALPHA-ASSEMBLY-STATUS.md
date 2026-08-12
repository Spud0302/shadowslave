# Alpha Assembly Status

**Integration base:** `main@4b17902603abe46803fc672d43700fec64ded110`

This file tracks the current assembly candidate rather than the older preview-era root status files. It is intentionally scoped to integration evidence; it does not redefine lore or progression authority.

## Current integration lanes

- Gameplay interaction consolidation: Ash Compass runtime (#236), Chainback displacement counterplay (#256), Drowned Listener vibration counterplay (#260), and Stonewake Shield `settle` execution (#264), re-rooted together onto exact current main after Glass Road #270 merged.
- Glass Road compatibility: the four shared Memory surfaces (`ModItems`, `MemoryCommands`, `MemoryManifestationService`, and `PreviewResetService`) retain merged Glass Road registration/manifestation/reset state while adding Stonewake alongside it.
- Echo integration: #276 remains the reviewed GUARD + CARRY Ash Burrower Echo candidate but still predates #270 and requires current-main reconciliation before it can be called current.
- Dream Realm world integration: #275 remains the cumulative Drowned Bell / Storm Lantern candidate but also predates #270; its prior hosted runner-allocation blocker remains recorded.
- Nightmare recovery: #278 is re-rooted on current main; genuine networked `ServerPlayer` recovery across two dedicated-server JVMs is still unproven under #34.
- Better Combat: #277 remains a physical-admission experiment and is not promoted without interactive evidence.

## Alpha checklist

- [x] Nightmare entry represented by merged/current integration lineage.
- [x] Nightmare completion represented by merged/current integration lineage.
- [ ] Nightmare recovery: server-side/FakePlayer and restart fixtures exist, but genuine two-JVM network reconnect evidence is still required by #34.
- [x] Scenario selection represented by merged/current integration lineage.
- [x] Generated identity award represented by merged/current integration lineage.
- [x] Memory/Echo ownership baseline represented by merged/current integration lineage.
- [x] At least one real creature represented by merged/current integration lineage.
- [x] One Dream Realm vertical slice represented by merged/current integration lineage; #275 is the cumulative native-world candidate.
- [x] UI/presentation baseline represented by merged/current integration lineage.
- [x] Client/server packaging baseline represented by merged/current integration lineage.

## Gameplay consolidation evidence

The four source PRs were independently reviewed/hosted-green before this consolidation. Their gameplay contracts remain isolated from Glass Road except for four shared Memory integration files. This re-root deliberately keeps the newer generic Glass Road command/manifestation API, adds Stonewake to that API, retains Glass Road's transient combat-state cleanup on preview reset, and preserves the existing source blobs for Ash Compass, Chainback, Drowned Listener, Stonewake execution, and focused tests.

Do not mark #236, #256, #260, or #264 superseded until this exact re-rooted current-main head executes hosted CI successfully and final containment confirms each source contract is present. Do not retry #275 or #278 unchanged solely to chase runner allocation; resume those blockers only under their recorded conditions.
