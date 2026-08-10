# Ashen Expanse resource inspection completion

## Scope

This slice completes physical inspection coverage for the three resource hooks already authored and already placed in the Ashen Expanse preview: `bone_char`, `ruin_metal`, and `dry_fungus`. It does not add a resource catalogue, harvesting system, inventory type, economy, reward table, crafting rule, food system, healing rule, respawn model, or progression mechanic.

`DreamRealmRegionContentCatalog` remains the Java authority for the Ashen Expanse and its resource-hook identities. `DreamRealmVerticalSliceDefinition` remains the Java binding that decides where those hooks exist in the current preview. NeoForge only recognizes the corresponding physical cluster and presents bounded inspection text.

## Evidence classification

- **CANON:** no new Shadow Slave canon proposition is introduced by this runtime integration.
- **INFERRED:** an already-authored Java-owned world hook can be made physically inspectable without claiming a canonical yield, value, supernatural property, or progression consequence.
- **DESIGN:** the exact `bone_char`, `ruin_metal`, and `dry_fungus` names; their current Minecraft placeholder blocks and coordinates; the three-block cluster shape; inspection wording; and information-only interaction.
- **UNKNOWN:** canonical material identity, abundance, extraction, item form, edibility, healing, toxicity, crafting use, durability, trade value, supernatural properties, ownership, depletion/respawn, and relationship to Soul Shards, Memories, Echoes, or progression.
- **COMPATIBILITY:** Java owns region/resource identity. NeoForge block events may execute/present that resolved state but cannot create canonical resource ownership or gameplay rewards from the placeholder blocks.

## Runtime contract

An inspection succeeds only when all of the following are true:

1. execution is server-side;
2. the player is inside the bundled Dream Realm preview dimension;
3. the clicked position belongs to exactly one authored resource cluster; and
4. the clicked block still matches that hook's expected temporary Minecraft placeholder.

The current placeholder mapping is `bone_char -> bone block`, `ruin_metal -> raw iron block`, and `dry_fungus -> brown mushroom block`. These are removable DESIGN executors, not lore claims.

The interaction deliberately produces information only. A later harvesting/economy slice requires independent authoritative definitions and lore evidence before it may mutate Java-owned inventory, progression, or resource state.
