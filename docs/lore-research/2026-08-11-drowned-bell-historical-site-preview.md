# Drowned Bell historical-site physical preview

## Scope

This slice adds the HISTORICAL physical half of the first era-linked site established by PR #248 and physically begun by PR #249. It does not change Nightmare resolution, appraisal, progression, persistence, or the later Dream Realm world state.

The development command `/shadowslave_dreamrealm enter_drowned_bell_history` builds an intact cliff settlement representing The Drowned Bell's historical era. The Bell Tower, Sea Gate, quarry approach and lower village occupy the exact same local anchor coordinates used by the corresponding later Storm Lantern Coast ruins.

## Player-visible comparison

Historical era:

- intact Bell Tower with bell and light;
- working masonry Sea Gate;
- open/lit quarry approach;
- inhabited lower harbour terraces with simple houses and lanterns.

Later Dream Realm era from PR #249:

- storm-battered belfry;
- broken Sea Gate;
- collapsed quarry cut;
- drowned harbour terraces.

The two executors live at separate development origins in the same Dream Realm dimension so a tester can visit both without one rebuild destroying the other.

## Lore boundary

### CANON

Nightmares reconstruct events/conflicts from the ancient history of the Dream Realm. The Drowned Bell itself remains project-authored scenario content.

### INFERRED

A reconstructed historical location can remain recognisable in later Dream Realm geography, making the same-place/multiple-eras design a lore-compatible exploration mechanic.

### DESIGN

All exact coordinates, settlement architecture, palettes, tower dimensions, gate geometry, quarry dimensions, terrace houses, lighting, historical origin and command surface are project DESIGN.

### UNKNOWN

Exact reconstruction fidelity, elapsed time, geographic drift, whether all Nightmare sites remain recognisable, and the precise physical appearance of any canonical historical settlement.

### COMPATIBILITY

`NightmareHistoricalSiteCatalog` owns the stable scenario/site/landmark correspondence. The Minecraft blocks and development origins are removable physical execution. A player's altered Nightmare result never feeds either historical layout or the later shared Dream Realm layout.

## Dependency decision

No dependency added. Native NeoForge/Minecraft block execution is sufficient for this bounded proof. TerraBlender remains deferred until actual biome placement/blending becomes a demonstrated bottleneck; GeckoLib and SmartBrainLib are unrelated to this static site-layout slice.

## Validation target

`DrownedBellHistoricalSitePlanTest` pins:

- the exact authored scenario/site identity;
- four historical physical piece families;
- exact Bell Tower/belfry coordinate equality;
- exact Sea Gate coordinate equality;
- exact quarry/collapsed-cut coordinate equality;
- exact lower-village/drowned-terrace coordinate equality.

Fresh exact-head Preview Gates are required before this slice is called green.

## Next step

The next high-impact world slice is no longer another bounded preview room. Move the shared site plan toward true chunk/structure placement, or add the first deterministic Storm Lantern Coast encounter budget if the structure-worldgen seam is blocked. The fixed Ashen Expanse remains a regression fixture until those generated systems cover its interactions.
