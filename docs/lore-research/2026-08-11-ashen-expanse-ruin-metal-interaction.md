# Ashen Expanse ruin-metal interaction boundary

## Scope

This slice executes one already-authored Java-owned Dream Realm resource hook, `ruin_metal`, as a physical right-click inspection in the Ashen Expanse preview. It does not add a new resource catalogue, material economy, reward table, crafting rule or progression mechanic.

The existing `DreamRealmRegionContentCatalog` already classifies the Ashen Expanse with the `SALVAGE` opportunity and the `ruin_metal` resource hook. The runtime therefore exposes only that bounded authored opportunity.

## Evidence classification

- **CANON:** no new canon claim is introduced. Dream Realm resource/economy specifics are not derived by this slice.
- **INFERRED:** a physical world adapter may make an already-authored region resource hook inspectable without assigning it canonical yield or value.
- **DESIGN:** the name `ruin_metal`, its three-block raw-iron placeholder cluster, location in the Ashen Expanse development slice, right-click inspection text and the decision to expose SALVAGE as information only.
- **UNKNOWN:** canonical material identity, abundance, extraction method, item form, durability, trade value, crafting use, supernatural properties, ownership, respawn behavior and relationship to Soul Shards or Memories.
- **COMPATIBILITY:** Java remains authority for the region/resource identity and opportunities. NeoForge block events only execute presentation. No item, currency, Soul Shard, Memory, progression state or accepted Nightmare event is granted by inspection.

## Runtime boundary

The executor activates only:

1. on the server side;
2. for a player inside the bundled Dream Realm preview dimension;
3. at one of the three physical blocks belonging to the authored `ruin_metal` cluster; and
4. while that block is still the expected raw-iron placeholder.

The interaction fails closed everywhere else. A later real salvage system can replace this adapter without migrating canonical player state because this slice stores none.
