package dev.spud.shadowslave.awakening;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * The Natures the Dream Candle ritual can roll: Ember, Mist, Stone and Tide
 * from the first slice, then Warden, Seer, Reaper and Wellspring, the
 * backfill Natures for the Gates and rank-up passes. Ability and Flaw ids
 * match the procedural catalogue so existing mechanics (Kindle, Cold Ash)
 * keep working.
 */
public final class AspectKitCatalog {
    private static final String NAMESPACE = "shadowslave";

    public static final AspectKit EMBER = new AspectKit(
            id("generation/nature/ember"), "Ember",
            id("kit/innate/ember_warmth"), "Ember's Warmth",
            "Fire and lava hurt you half as much.",
            id("generation/ability/kindle"), "Kindle",
            "A burst of light: night vision and speed, and nearby hostile mobs catch fire.",
            id("generation/flaw/cold_ash"), id("generation/flaw_effect/cold_ash"), "Cold Ash",
            "Weakness while you stand in water or rain."
    );

    public static final AspectKit MIST = new AspectKit(
            id("generation/nature/mist"), "Mist",
            id("kit/innate/mist_hush"), "Mist's Hush",
            "Sneaking makes no sound.",
            id("generation/ability/mist_passage"), "Mist Passage",
            "Blink a short way where you look and turn briefly invisible.",
            id("generation/flaw/borrowed_breath"), id("generation/flaw_effect/borrowed_breath"), "Borrowed Breath",
            "Every Mist Passage costs hunger, and you run out of air faster."
    );

    public static final AspectKit STONE = new AspectKit(
            id("generation/nature/stone"), "Stone",
            id("kit/innate/stone_footing"), "Stone's Footing",
            "No knockback while you stand still.",
            id("generation/ability/holdfast"), "Holdfast",
            "Brace: heavy damage resistance and no knockback for a few seconds, but no sprinting.",
            id("generation/flaw/stone_sleep"), id("generation/flaw_effect/stone_sleep"), "Stone Sleep",
            "You wake slow and heavy, and you sink in water."
    );

    public static final AspectKit TIDE = new AspectKit(
            id("generation/nature/tide"), "Tide",
            id("kit/innate/tide_gills"), "Tide's Breath",
            "You breathe underwater.",
            id("generation/ability/returning_tide"), "Returning Tide",
            "Mark where you stand, then be pulled back there.",
            id("generation/flaw/tidal_debt"), id("generation/flaw_effect/tidal_debt"), "Tidal Debt",
            "Touch water once each day, or grow weak until you do."
    );

    public static final AspectKit WARDEN = new AspectKit(
            id("generation/nature/warden"), "Warden",
            id("kit/innate/warden_gate_sense"), "Gate Sense",
            "You feel the pull of the nearest Nightmare Gate, and you can read a creature's Rank and Class on sight.",
            id("generation/ability/mark_prey"), "Mark Prey",
            "Mark the creature you look at: it glows and takes half again as much damage from everyone.",
            id("generation/flaw/quarry_lock"), id("generation/flaw_effect/quarry_lock"), "Quarry Lock",
            "While your mark is up, your damage to anything else is halved."
    );

    public static final AspectKit SEER = new AspectKit(
            id("generation/nature/seer"), "Seer",
            id("kit/innate/seer_forewarning"), "Forewarning",
            "A flash of warning just before a creature near you lands its big attack.",
            id("generation/ability/foresight"), "Foresight",
            "Clear sight for 30 seconds: nearby hostiles glow, and a Gate within reach shows you its Category and roster.",
            id("generation/flaw/clouded_eyes"), id("generation/flaw_effect/clouded_eyes"), "Clouded Eyes",
            "Your view is short and hazy whenever Foresight isn't active."
    );

    public static final AspectKit REAPER = new AspectKit(
            id("generation/nature/reaper"), "Reaper",
            id("kit/innate/reaper_soul_feast"), "Soul Feast",
            "Every kill restores essence; stronger creatures restore more.",
            id("generation/ability/soul_siphon"), "Soul Siphon",
            "A beam that drains the creature you look at for 3 seconds, feeding its essence to you.",
            id("generation/flaw/hunger"), id("generation/flaw_effect/hunger"), "Hunger",
            "Your essence never refills on its own. Fight, or meditate at the Dream Candle."
    );

    public static final AspectKit WELLSPRING = new AspectKit(
            id("generation/nature/wellspring"), "Wellspring",
            id("kit/innate/wellspring_deep_well"), "Deep Well",
            "Essence refills three times as fast while you meditate, and twice as fast while you sneak still.",
            id("generation/ability/essence_gift"), "Essence Gift",
            "Pour essence into the ally you look at; with no ally in sight, it mends your own wounds.",
            id("generation/flaw/brimming_vessel"), id("generation/flaw_effect/brimming_vessel"), "Brimming Vessel",
            "A heavy hit spills a quarter of the essence you hold."
    );
    public static final AspectKit FORGE = new AspectKit(
            id("generation/nature/forge"), "Forge",
            id("kit/innate/forge_thrifty_hands"), "Thrifty Hands",
            "Smelted goods sometimes come out doubled, and Forging at the Weaving table teaches you half again as fast.",
            id("generation/ability/quench"), "Quench",
            "Smelt up to eight of whatever you hold, in your bare hands.",
            id("generation/flaw/cold_hearth"), id("generation/flaw_effect/cold_hearth"), "Cold Hearth",
            "Snow and frost sicken you: slow and weak wherever it is cold enough to snow."
    );
    public static final AspectKit DELVE = new AspectKit(
            id("generation/nature/delve"), "Delve",
            id("kit/innate/delve_ore_sense"), "Ore Sense",
            "Sneak to feel the ores around you; they shine through the stone.",
            id("generation/ability/tunnel_strike"), "Tunnel Strike",
            "Break a tunnel three blocks wide, high and deep in one blow.",
            id("generation/flaw/open_sky"), id("generation/flaw_effect/open_sky"), "Open Sky",
            "Under the open sky you are weak, and essence comes back at half speed."
    );
    public static final AspectKit MASON = new AspectKit(
            id("generation/nature/mason"), "Mason",
            id("kit/innate/mason_long_reach"), "Long Reach",
            "You place and break blocks two blocks further away.",
            id("generation/ability/raise_wall"), "Raise Wall",
            "Raise a three by three wall ahead of you from the blocks in your hand.",
            id("generation/flaw/far_from_home"), id("generation/flaw_effect/far_from_home"), "Far From Home",
            "Abilities cost half again as much far from anything you have built."
    );
    public static final AspectKit VERDANT = new AspectKit(
            id("generation/nature/verdant"), "Verdant",
            id("kit/innate/verdant_green_thumb"), "Green Thumb",
            "Crops and saplings around you grow faster.",
            id("generation/ability/bloom"), "Bloom",
            "Everything growing around you surges, and nearby animals are mended.",
            id("generation/flaw/rootbound"), id("generation/flaw_effect/rootbound"), "Rootbound",
            "Essence only comes back while you stand on living ground: grass, dirt, moss or leaves."
    );
    public static final AspectKit WAYFARER = new AspectKit(
            id("generation/nature/wayfarer"), "Wayfarer",
            id("kit/innate/wayfarer_pathfinder"), "Pathfinder",
            "A faint pull draws you toward the nearest Seed and your Dream Anchor; in the waking world, toward your bed.",
            id("generation/ability/waymark"), "Waymark",
            "Set a marker where you stand (three at most) that you and those near you see through walls; sneak and press to snap back to your newest marker.",
            id("generation/flaw/wanderlust"), id("generation/flaw_effect/wanderlust"), "Wanderlust",
            "Essence comes back slower the longer you linger in one place; keep moving."
    );
    public static final AspectKit LULL = new AspectKit(
            id("generation/nature/lull"), "Lull",
            id("kit/innate/lull_drowsy_aura"), "Drowsy Aura",
            "Hostile creatures near you nod off between blows and attack a little slower.",
            id("generation/ability/lullaby"), "Lullaby",
            "The creature you look at sleeps for a few seconds, if its rank is no higher than yours; damage wakes it.",
            id("generation/flaw/insomnia"), id("generation/flaw_effect/insomnia"), "Insomnia",
            "Beds cannot carry you through the night, only a Dream Candle can, so the phantoms come for you."
    );

    private static final List<AspectKit> ALL = List.of(EMBER, MIST, STONE, TIDE, WARDEN, SEER, REAPER, WELLSPRING,
            FORGE, DELVE, MASON, VERDANT, WAYFARER, LULL);

    private AspectKitCatalog() {
    }

    public static List<AspectKit> all() {
        return ALL;
    }

    public static Optional<AspectKit> byNature(ResourceLocation natureId) {
        return ALL.stream().filter(kit -> kit.natureId().equals(natureId)).findFirst();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
