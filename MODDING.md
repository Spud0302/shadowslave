# From datapack to mod

The completed datapack remains the frozen behavioural reference. Java-era development is a two-track experiment:

- **Nightmare Spell modpack:** selected existing mods provide generic content and mechanics; the custom Shadow Slave core fills the gaps and owns canonical Soul/Nightmare state.
- **Standalone Shadow Slave mod:** the same core contracts are implemented directly as a purpose-built NeoForge mod.

Start here:

1. [Two-track transition plan](docs/MOD-TRANSITION-PLAN.md)
2. [Shared vertical-slice acceptance specification](shared-test-spec/VERTICAL-SLICE.md)
3. [Standalone Java track](mod/README.md)
4. [Nightmare Spell modpack track](modpack/README.md)
5. [Datapack-to-Java persistence contract](docs/JAVA-HANDOFF.md)

## Product-qualified release tags

Historical tags already occupy ordinary `v1.x` names, so releases use product-qualified tags:

```text
datapack-v1.0.0
mod-v0.1.0
modpack-v0.1.0
```

The GitHub workflow at `.github/workflows/package-datapack.yml` validates and packages the datapack. A `datapack-v*` tag additionally creates or updates a GitHub Release containing the ZIP and `SHA256SUMS.txt`.

## Current ordering

```text
finish/push frozen datapack
        ↓
publish datapack-v1.0.0
        ↓
scaffold shared NeoForge Soul/Nightmare core
        ↓
Nightmare Spell modpack prototype
        +
standalone mod prototype
        ↓
run the same vertical-slice tests
        ↓
choose modpack-led, standalone-led, or hybrid
```
