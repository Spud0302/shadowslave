# Playable preview build provenance

**Artifact:** `shadowslave-0.1.0-preview.1.jar`  
**Status:** development preview / pre-Claude-tested / not a public release

## Source

- repository: `Spud0302/shadowslave`;
- branch: `gpt/live-datapack-import`;
- pull request: #19;
- source commit: `460cd31f135ae7e98f66890b6bbf60414772d57b`;
- stable base: `main@5f8acf2b2e3b04198166592568dd885431a2a09f`.

## GitHub Actions

- workflow: `Java core`;
- run number: `33`;
- run ID: `30555343642`;
- conclusion: **success**;
- completed gates:
  - Gradle wrapper validation;
  - compile and unit tests;
  - JAR packaging;
  - physical NeoForge client startup marker;
  - dedicated NeoForge server ready marker;
  - artifact upload.

## Workflow artifact

- artifact name: `shadow-slave-java-core`;
- artifact ID: `8764632229`;
- archive size: `98,618` bytes;
- created: `2026-07-30T15:13:37Z`;
- scheduled expiry: `2026-10-28T15:10:11Z`;
- archive SHA-256:

  ```text
  dd6315fd25ad50bbba09c53433e8b1840a2f70b344b18a425533c4856da3a8e8
  ```

## Extracted JAR

- filename: `shadowslave-0.1.0-preview.1.jar`;
- size: `107,795` bytes;
- SHA-256:

  ```text
  600fa2143879f8f269aec6d048a0fa4b3150f808a091c1527fe34067d9cdd867
  ```

The downloaded archive contained exactly one file with that name. Both archive and JAR checksums were
recalculated after download and passed.

## Evidence boundary

This provenance proves the source, automated build, startup smokes, packaging and downloaded bytes. It
does **not** prove the complete gameplay interaction loop, visual quality, balance, multiplayer feel or
real logout/reload experience.

Andrew has not yet played the artifact. Claude has not yet bulk-reviewed PR #19. Those remain explicit
follow-up evidence, not silently assumed results.
