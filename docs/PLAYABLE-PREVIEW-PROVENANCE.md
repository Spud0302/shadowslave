# Playable preview build provenance

**Artifact:** `shadowslave-0.1.0-preview.2.jar`  
**Status:** corrected development preview / pending Claude bulk verification / not a public release

## Source

- repository: `Spud0302/shadowslave`;
- branch: `gpt/live-datapack-import`;
- pull request: #19;
- runtime source commit: `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`;
- stable base: `main@a638efc60866ca9a390f3172c5e712753e5764c8`.

Documentation commits after the runtime source do not change the JAR bytes.

## GitHub Actions

- workflow: `Java core`;
- run number: `34`;
- run ID: `30686670446`;
- conclusion: **success**;
- completed gates:
  - Gradle wrapper validation;
  - compilation and expanded unit tests;
  - physical NeoForge client startup marker;
  - dedicated NeoForge server ready marker;
  - JAR packaging;
  - artifact upload.

## Workflow artifact

- artifact name: `shadow-slave-java-core`;
- artifact ID: `8814240590`;
- archive size reported by GitHub: `101,226` bytes;
- created: `2026-08-01T06:00:30Z`;
- scheduled expiry: `2026-10-30T05:52:57Z`;
- archive SHA-256:

  ```text
  a7ee670001042ee9c783ceb191e667fefdf043acd1b6fa498438434907291d79
  ```

## Extracted JAR

- filename: `shadowslave-0.1.0-preview.2.jar`;
- size: `110,652` bytes;
- SHA-256:

  ```text
  48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
  ```

The downloaded archive contained exactly one file with that name. Both archive and JAR checksums were recalculated after download and matched.

## Correction scope

This build includes the Java-side corrections from issues #22–#25 and the versioned documentation/hand-off state for the full #20–#26 batch. The branch also contains frozen-datapack changes for #20, #21, and #26, but the Java workflow does not run the deployed Minecraft/Mineflayer datapack gate.

## Evidence boundary

This provenance proves source linkage, Java compilation/tests, startup smokes, packaging, artifact upload, and downloaded bytes. It does **not** prove:

- deployed datapack lifecycle, Flaw, and concurrency regression results;
- complete gameplay interaction, visual quality, pacing, or balance;
- real logout/reload and active-instance restart behaviour;
- Claude's corrected-head bulk verdict;
- Andrew's play feedback.

The `0.1.0-preview.1` artifact is superseded and does not contain the correction batch.
