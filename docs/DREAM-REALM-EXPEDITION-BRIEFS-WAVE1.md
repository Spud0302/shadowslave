# Dream Realm expedition briefs — wave 1

**Status:** player-facing DESIGN content layered on the merged Dream Realm region catalogue.

## Scope

This slice adds pre-departure planning briefs for already-resolved Dream Realm regions. It does not generate regions, creatures, rewards, travel time, supply use, encounter frequency, safe routes, or progression.

The Java caller supplies the authoritative region ID. `DreamRealmExpeditionBriefCatalog` can only choose between two authored briefs belonging to that region, or compose a caller-selected brief that is verified to belong to that region. A seed varies only authored brief/cue presentation.

Each brief combines only primitives already authored in `DreamRealmRegionContentCatalog`: one hazard, traversal mode, opportunity, landmark hook, and creature-affinity ID. Exact runtime world state remains a separate concern.

## Player-facing content

Wave 1 contains 20 briefs, two per merged region, spanning five DESIGN preparation families:

- `ROUTE_RECON`
- `HAZARD_PREPARATION`
- `LANDMARK_ORIENTATION`
- `THREAT_AWARENESS`
- `OPPORTUNITY_PLANNING`

Examples include:

- planning line-of-sight breaks and tested footing before crossing the Ashen Expanse;
- verifying anchors and reversible high-ground routes in Chainfall Reach;
- using physical reference points instead of reflection/resonance assumptions on Glassmere Flats;
- observing water conditions immediately before Blackwater Steps crossings;
- treating food opportunities in Thornwake Basin as unverified rather than automatic supplies;
- moving between verified physical markers in Mistwound Pass;
- planning shelter-to-shelter movement on Bonewhite March;
- marking repeating Hollow Causeway junctions twice;
- treating Storm Lantern Coast bells and water marks as observations rather than prophecy or a universal forecast system;
- selecting Red Canopy travel layer deliberately and preserving a dry exit from water routes.

Every brief contains preparation checks, departure questions, two presentation cues, and an explicit anti-overclaim boundary.

## Lore evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md`. The decisive claims use chapter text rather than host summaries.

### Chapter 370 — *Exploration Report*

Sunny compiles geography, environment, landmarks, Nightmare Creature powers/behavior/weaknesses, and other accumulated field knowledge into a report specifically intended to make Dream Realm survival easier for someone entering without that information.

This is direct support for player-facing pre-departure information being useful, but it does not supply a universal expedition-planning schema.

### Chapter 468 — *Desecrated Grove*

Sunny travels using an established route, leaves it for more dangerous wilderness, and deliberately avoids places where dangerous Nightmare Creatures are known to dwell. Detailed map/route knowledge and known danger locations materially affect route choice.

This supports route reconnaissance and threat-awareness presentation without establishing encounter probabilities or guaranteed safety.

### Chapter 1608 — *Death Zone*

Much later material again shows local Dream Realm conditions materially controlling whether a cohort should move at all: the Fire Keepers remain still through a lethal environmental state, then prepare once conditions change. The same region's rapidly changing environment and creature life make timing and local observation strategically meaningful.

This later evidence reinforces that environment-aware preparation remains relevant; it does not create a universal travel-time, weather, rest, or encounter formula.

### Publication freshness

On 2026-08-09, official WebNovel search results exposed the work at roughly chapter 3130/3131 depending on the indexed page snapshot. The owner-designated NovelFull access layer remains a chapter-reading access source rather than publication authority. No claim in this slice depends on material newer than Chapter 1608.

## Evidence classification

**CANON**

- Dream Realm geography, environment, landmarks, creature behavior, and known weaknesses can be actionable survival information.
- Routes, maps, known danger locations, and established travel paths can materially change travel decisions.
- Local environmental conditions can make waiting, detouring, or changing movement strategy preferable to pressing forward.

**INFERRED**

- Pre-departure route reconnaissance, hazard preparation, landmark orientation, threat awareness, and opportunity planning are useful separable presentation concerns for a game adaptation.
- A player-facing brief can summarize already-known regional information without becoming authority for current world state.

**DESIGN**

- All 20 exact briefs, five brief families, headings, situation reads, preparation checks, departure questions, hazard/traversal/opportunity/landmark/creature-affinity pairings, presentation cues, anti-overclaim text, seed mixing, and generator version `dream-realm-expedition-brief-v1`.
- Exactly two briefs per current authored region.

**UNKNOWN**

- Any canonical expedition-planning or route-selection algorithm.
- Encounter/spawn probabilities, creature density, migration, or guaranteed presence.
- Travel time, route distance, stamina/exhaustion, climbing/swimming speed, or supply-consumption equations.
- Exact weather/flood/rockfall/resonance timing or forecasting rules.
- Safe-zone guarantees, shelter duration, food/water safety, material quantities/rarity, rewards, appraisal effects, or progression consequences.
- Whether the Nightmare Spell or Dream Realm provides a built-in planning interface resembling this catalogue.

**COMPATIBILITY**

- `DreamRealmRegionContentCatalog` remains Java authority for stable region and primitive identity.
- A future Java-owned expedition or travel instance may persist region + resolved brief ID + presentation seed plus authoritative runtime outcomes if replay matters.
- HUD, map, book, NPC briefing, sound, structure, weather, entity, and world adapters may render or execute already-resolved content but cannot own region identity, progression, rewards, encounter authority, or persistence.

No canonical generation, probability, travel-time, supply, forecast, reward, or safety formula is claimed.

## Validation contract

`DreamRealmExpeditionBriefCatalogTest` checks:

- exactly 20 unique briefs and exactly two per merged region;
- coverage of all five DESIGN families;
- every hazard, traversal mode, opportunity, landmark, and creature affinity belongs to the source region;
- non-trivial preparation checks, departure questions, presentation cues, and anti-overclaim boundaries;
- deterministic same-seed composition;
- a 2,048-seed sweep per region preserving caller-supplied region identity and source-region primitive validity;
- both authored briefs and both presentation cues per brief are reachable in every region;
- explicit caller-selected brief composition cannot cross region boundaries;
- unknown region/brief IDs fail closed;
- anti-overclaim text explicitly preserves qualitative planning rather than silently defining probabilities, guarantees, forecasts, or travel-time equations.

## Integration boundary

This slice is intentionally independent of open travel-event, shelter/camp, landmark, resource-site, ecology, creature-role, story, and correctness branches. It summarizes already-merged region primitives before departure and does not import those open PR APIs.

A later layer may present a resolved brief through an NPC, journal, map, or expedition-planning screen. That adapter must consume Java-resolved content rather than calculating canonical region state from presentation text.