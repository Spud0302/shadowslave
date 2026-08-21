---
uid: ss-context-release-qa
record_kind: context
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/implementation/authority-drift-register.md
  - modpack/manifest.json
  - .github/workflows/modpack-shell.yml
  - .github/workflows/java-core.yml
tags:
  - context
  - qa
  - release
---

# Context packet — Release and modpack QA

## Goal

Prove that the exact distributed archive is dependency-complete, reproducible, bootable, and tied to an exact source revision.

## Must read

- [[brain/implementation/authority-drift-register]]
- [[PROJECT-STATUS]]
- [[docs/CURRENT-PREVIEW-SUMMARY]]
- [[modpack/README]]
- modpack/manifest.json
- modpack/tools and modpack/tests
- .github/workflows/modpack-shell.yml
- .github/workflows/java-core.yml

## Highest-priority gap

The Shadow Slave mod requires standalone combat_core 0.0.4-wip, but the manifest currently declares only GeckoLib and SmartBrainLib. The modpack workflow assembles a literal fixture rather than the real core and does not boot the archive.

## Required proof

1. Build exact Shadow Slave and Combat Core JARs.
2. Resolve and hash the complete required dependency closure.
3. Assemble the deterministic package from those exact artifacts.
4. Verify archive layout and hashes.
5. Extract the package.
6. Run NeoForge GameTests with both namespaces.
7. Boot the extracted pack as a client and dedicated server.
8. Archive logs, crash reports, hashes, and provenance.
9. Run a version/status consistency check.

## Non-claims

- A deterministic ZIP is not proof that dependencies are complete.
- A fixture package is not proof the released pack boots.
- A Gradle task exit code is not proof Minecraft reached readiness.
- A historical green workflow does not verify a new head.

