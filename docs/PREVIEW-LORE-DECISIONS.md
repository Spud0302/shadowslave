# Playable preview lore decisions

This ledger applies to the Java preview built under `docs/PLAYABLE-PREVIEW-DIRECTIVE.md`. It prevents
Minecraft implementation choices from being mistaken for novel canon.

Source authority remains `docs/JAVA-LORE-ALIGNMENT.md`: novel mechanics first, official adaptation as a
compatible visual/staging reference, access translations as aids, community sources as indexes only,
and project design labelled honestly.

| Topic | Classification | Decision |
| --- | --- | --- |
| Uninfected, Carrier, Aspirant and Dreamer are distinct states | **CANON** | The Java domain keeps all four boundaries. Carrier has no Soul Rank; Aspirant and Dreamer have Dormant Soul Rank. |
| Infection before the First Nightmare | **CANON** | The preview never claims ordinary sleep causes infection. `/shadowslave preview_begin` is labelled a development shortcut. |
| Assigned historical role | **CANON** | The active `NightmareInstance` owns a temporary role rather than storing it in permanent `SoulData`. |
| Reconstructed situation and central conflict | **CANON / INFERRED boundary** | A Nightmare is modelled as role + situation + conflict + resolution, not universally arena + timer + boss. |
| Individual First Nightmare ownership | **CANON** | Every player has a separate persistent instance and play-space slot. |
| Ordinary failure is death | **CANON** | The domain records canonical death rather than safe low-health ejection. Minecraft respawn remains a labelled development accommodation. |
| Crash/admin recovery | **DESIGN required by software** | Recovery is technical, explicit and never described as mercy from the Spell. |
| The Last Signal scenario | **DESIGN** | A ruined road, last watchkeeper, dead signal fire and corrupted pursuer provide one testable historical conflict. No claim is made that this event exists in the novel. |
| Lighting the signal rather than killing the pursuer | **DESIGN aligned with canon constraints** | Combat is optional. The player resolves the conflict by restoring the warning signal. |
| Fixed `Last Light` Aspect | **DESIGN** | A preview identity chosen to demonstrate a coherent nature and ability. It is not a canonical character Aspect. |
| Awakened Aspect Rank on a Dormant Dreamer | **CANON-compatible DESIGN** | Aspect Rank is independent of Soul Rank; the chosen rank is a preview value, not an appraisal formula. |
| `Kindle` ability | **DESIGN** | Night Vision and Speed are temporary vanilla-effect execution for the preview. Future implementation may replace them while preserving identity. |
| Fixed `Cold Ash` Flaw | **DESIGN** | Weakness in water/rain is a mechanically real preview drawback. No claim is made that trial statistics canonically generate it. |
| Appraisal algorithm | **UNKNOWN in canon; fixed DESIGN in preview** | The preview uses one deterministic result only to close the playable loop. The service boundary is replaceable. |
| Nightmare terrain and visual palette | **DESIGN with adaptation guidance permitted** | The bundled dark dimension and deepslate watch are staging choices, not mechanical authority. |
| Vanilla Husk pursuer | **DESIGN placeholder** | It proves owned-entity lifecycle and pressure. It is not named or presented as a canonical Nightmare Creature. |
| Imported datapack identities | **COMPATIBILITY** | Existing players retain earned names/ranks/effects. Datapack generation tables are not treated as the complete Java universe. |

## Rules for the next implementation pass

- Do not add natural infection until the exhaustion/trigger sequence has been checked against primary
  lore notes again.
- Do not generalise Last Signal into a universal Nightmare template.
- Do not derive permanent Flaws from one visible metric and call the formula canonical.
- Do not force Aspect Rank to equal Soul Rank.
- Do not add safe retry as default lore behaviour; any accessibility option must be labelled.
- Keep temporary role, conflict evidence, owned entities and recovery data on `NightmareInstance`.
- Keep permanent revealed identity in `SoulIdentityData` and authoritative progression in `SoulData`.
