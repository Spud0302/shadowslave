package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Each Nature's ladder past Dormant (DESIGN, from the lore-to-gameplay doc):
 * at Awakening the player chooses one of two abilities that deepen the same
 * nature, the way Sunny's Shadow Step grew out of Shadow Control.
 */
public final class KitLadder {
    private static final String NAMESPACE = "shadowslave";

    public static final AbilityVariant CARRY_THE_FLAME = variant("carry_the_flame", "Carry the Flame",
            "Pass your fire to an ally you look at (or yourself): regeneration and fire resistance.", "Support");
    public static final AbilityVariant EMBER_WARD = variant("ember_ward", "Ember Ward",
            "A ring of fire around you for a few seconds; hostile mobs that touch it are thrown back burning.", "Control");
    public static final AbilityVariant THROUGH_THE_HAZE = variant("through_the_haze", "Through the Haze",
            "See every creature and player nearby through walls for a few seconds.", "Sight");
    public static final AbilityVariant VEIL = variant("veil", "Veil",
            "A dome of mist: hostile mobs inside are blinded and lose track of their prey.", "Control");
    public static final AbilityVariant ROOT_IN_STONE = variant("root_in_stone", "Root in Stone",
            "Raise a stone wall in front of you; it crumbles after a while.", "Shelter");
    public static final AbilityVariant QUAKE = variant("quake", "Quake",
            "Stomp: nearby hostile mobs are hurt, thrown up and slowed.", "Force");
    public static final AbilityVariant LOW_TIDE = variant("low_tide", "Low Tide",
            "Surge forward on a wave that shoves mobs aside.", "Movement");
    public static final AbilityVariant UNDERTOW = variant("undertow", "Undertow",
            "Drag nearby mobs toward the spot you look at and slow them.", "Control");
    public static final AbilityVariant SNARE_SIGIL = variant("snare_sigil", "Snare Sigil",
            "Draw a rune trap where you look; the first hostiles to cross it are rooted in place.", "Control");
    public static final AbilityVariant RELENTLESS_HUNT = variant("relentless_hunt", "Relentless Hunt",
            "Dash straight to your marked prey and strike faster for a few seconds.", "Movement");
    public static final AbilityVariant FATE_THREAD = variant("fate_thread", "Fate Thread",
            "Snap back to where you stood three seconds ago.", "Escape");
    public static final AbilityVariant SHARED_SIGHT = variant("shared_sight", "Shared Sight",
            "For a minute, players near you see your warnings too, and nearby hostiles glow.", "Support");
    public static final AbilityVariant HARVEST = variant("harvest", "Harvest",
            "Drain every hostile creature around you at once.", "Area");
    public static final AbilityVariant REAPING_ARC = variant("reaping_arc", "Reaping Arc",
            "A wide scythe sweep in front of you that finishes off badly wounded creatures.", "Force");
    public static final AbilityVariant SOUL_WARD = variant("soul_ward", "Soul Ward",
            "An essence shield on the ally you look at, or on yourself.", "Shelter");
    public static final AbilityVariant OVERFLOW = variant("overflow", "Overflow",
            "An aura for 20 seconds: you and allies near you regenerate health and essence.", "Support");
    public static final AbilityVariant MASTERWORK = variant("masterwork", "Masterwork",
            "For a minute, the next billet you fuse may reach one Tier above your Forging level, with a wider hammer mark and one Quality grade higher.", "Craft");
    public static final AbilityVariant MOLTEN_EDGE = variant("molten_edge", "Molten Edge",
            "Your blows burn for 15 seconds: each hit sets the target alight and sears it.", "Force");
    public static final AbilityVariant VEIN_PULL = variant("vein_pull", "Vein Pull",
            "Tear out the whole vein of the ore you look at and pull it into your pack.", "Craft");
    public static final AbilityVariant STONEWALK = variant("stonewalk", "Stonewalk",
            "Step straight through up to eight blocks of stone to the open air beyond.", "Escape");
    public static final AbilityVariant BLUEPRINT = variant("blueprint", "Blueprint",
            "Press to learn the five by five shape in front of you; press again to build it where you look, from your pack.", "Craft");
    public static final AbilityVariant SCAFFOLD = variant("scaffold", "Scaffold",
            "For ten seconds, footing appears under you wherever you step over air.", "Movement");
    public static final AbilityVariant LIVING_WALL = variant("living_wall", "Living Wall",
            "Raise a thorny hedge ahead of you that cuts and slows hostile creatures.", "Shelter");
    public static final AbilityVariant WILD_CALL = variant("wild_call", "Wild Call",
            "Tame the animal you look at, and rouse your pets nearby to fight harder.", "Support");

    public static final AbilityVariant ANCHOR_RECALL = variant("anchor_recall", "Anchor Recall",
            "Channel for three seconds to return to your Dream Anchor, or your bed in the waking world; a hit breaks it.", "Escape");
    public static final AbilityVariant TRAILBLAZE = variant("trailblaze", "Trailblaze",
            "For fifteen seconds your sprint leaves a trail that hastens you and every ally who runs it.", "Movement");
    public static final AbilityVariant DREAMFALL = variant("dreamfall", "Dreamfall",
            "Every creature around you of your rank or lower falls asleep for a few seconds.", "Control");
    public static final AbilityVariant SLEEPWALKER = variant("sleepwalker", "Sleepwalker",
            "Steer a sleeping creature you look at: for ten seconds it walks, still dreaming, wherever you look.", "Control");

    public static final AbilityVariant SHADE_STEP = variant("shade_step", "Shade Step",
            "Step through the shadows to a dark spot you look at, up to twenty-four blocks away.", "Movement");
    public static final AbilityVariant SNUFF = variant("snuff", "Snuff",
            "Put out every torch, lantern, candle and fire around you for thirty seconds; hostiles in the dark lose sight of you.", "Control");
    public static final AbilityVariant ENCASE = variant("encase", "Encase",
            "Freeze the creature you look at inside a shell of real ice for a few seconds, if its rank is no higher than yours.", "Control");
    public static final AbilityVariant RIMEPATH = variant("rimepath", "Rimepath",
            "For twenty seconds water freezes under your feet and you run faster on ice.", "Movement");
    public static final AbilityVariant GROUND_SLAM = variant("ground_slam", "Ground Slam",
            "Slam the ground: hostiles around you are hurt and thrown up, harder the more rage you hold; it spends half of it.", "Force");
    public static final AbilityVariant SECOND_WIND = variant("second_wind", "Second Wind",
            "Burn all your rage to heal and run: the more rage, the more you heal.", "Escape");

    public static final AbilityVariant DOORWAY = variant("doorway", "Doorway",
            "Open a door where you stand, then its twin somewhere else; for thirty seconds you and your allies step from one to the other.", "Movement");
    public static final AbilityVariant BANISH = variant("banish", "Banish",
            "Drop the creature you look at out of the world for five seconds; it comes back where it was.", "Control");
    public static final AbilityVariant CRUSH = variant("crush", "Crush",
            "For ten seconds, landing from a height sends out a shockwave that grows with the fall, and the fall cannot hurt you.", "Force");
    public static final AbilityVariant GRAVITY_WELL = variant("gravity_well", "Gravity Well",
            "Drag the hostiles around the spot you look at into it and pin them there, heavy, for four seconds.", "Control");
    public static final AbilityVariant REWIND = variant("rewind", "Rewind",
            "Snap back to where you stood three seconds ago, with the health you had then if it was more.", "Escape");
    public static final AbilityVariant STILL_MOMENT = variant("still_moment", "Still Moment",
            "Hold every hostile around you out of time for two seconds.", "Control");

    public static final AbilityVariant RALLY_CALL = variant("rally_call", "Rally Call",
            "Sound the call: allies who answer it by sneaking are drawn to your side, and your pets come at once.", "Movement");
    public static final AbilityVariant OATH_OF_ARMS = variant("oath_of_arms", "Oath of Arms",
            "Bind the ally you look at: for twenty seconds you take half of every blow that falls on them.", "Support");
    public static final AbilityVariant BULWARK = variant("bulwark", "Bulwark",
            "Raise a great dome: for ten seconds you and your allies inside take less harm, and arrows from outside fly back.", "Shelter");
    public static final AbilityVariant GATEWAY_STEP = variant("gateway_step", "Gateway Step",
            "At a lit Dream Candle, step straight through to your clan's Citadel Gateway.", "Movement");

    public static final AbilityVariant TWIST_FATE = variant("twist_fate", "Twist Fate",
            "For twenty seconds, the next attack aimed at you has an even chance to miss.", "Defense");
    public static final AbilityVariant BORROWED_LUCK = variant("borrowed_luck", "Borrowed Luck",
            "Mark the creature you look at: if it dies within thirty seconds, it drops twice as much.", "Utility");
    public static final AbilityVariant SILENCING_WORD = variant("silencing_word", "Silencing Word",
            "Speak to the Nightmare Creature you look at: for five seconds it cannot use its special abilities.", "Control");
    public static final AbilityVariant RALLYING_NAME = variant("rallying_name", "Rallying Name",
            "Speak the name of the ally you look at: their ailments are cleared and they run faster for a while.", "Support");

    public static final AbilityVariant PACK_TACTICS = variant("pack_tactics", "Pack Tactics",
            "Your summoned Echoes leap to flank the creature you look at, and fight harder for a while.", "Command");
    public static final AbilityVariant SECOND_BOND = variant("second_bond", "Second Bond",
            "Always on: your soul holds one Echo more than your rank allows.", "Passive");
    public static final AbilityVariant SWAP_PLACES = variant("swap_places", "Swap Places",
            "Trade places with your summoned Echo, wherever it stands nearby.", "Movement");
    public static final AbilityVariant SHARED_WOUNDS = variant("shared_wounds", "Shared Wounds",
            "For twenty seconds your summoned Echo takes two fifths of every blow that falls on you.", "Shelter");

    public static final AbilityVariant MIRROR_SWAP = variant("mirror_swap", "Mirror Swap",
            "Trade places with the creature or player you look at.", "Movement");
    public static final AbilityVariant STOLEN_VOICE = variant("stolen_voice", "Stolen Voice",
            "Throw a creature's own call to the spot you look at: its kind nearby forget what they hunt and go to it.", "Control");
    public static final AbilityVariant GRIT = variant("grit", "Grit",
            "Plant your feet for ten seconds: every blow you take where you stand hardens you against the next.", "Defense");
    public static final AbilityVariant DEFIANCE = variant("defiance", "Defiance",
            "Dare every creature nearby to fight you: they turn on you and cannot turn away for eight seconds.", "Command");

    public static final AbilityVariant CUT_THE_LINES = variant("cut_the_lines", "Cut the Lines",
            "Always on: you cut a Link Anchor twice as fast, and your Undermine charges burn in half the time.", "Passive");
    public static final AbilityVariant FALSE_TRAIL = variant("false_trail", "False Trail",
            "For a minute your steps leave no path on anyone's map, and a false trail runs off where you look; creatures hunting you follow it.", "Stealth");
    public static final AbilityVariant HOLD_THE_LINE = variant("hold_the_line", "Hold the Line",
            "Always on: you and your allies near your planted banner cannot be knocked back.", "Passive");
    public static final AbilityVariant LAST_RALLY = variant("last_rally", "Last Rally",
            "Always on: when your banner falls or is furled, you and your allies near it are healed four hearts.", "Passive");

    private static final Map<ResourceLocation, List<AbilityVariant>> AWAKENED = Map.ofEntries(
            Map.entry(AspectKitCatalog.EMBER.natureId(), List.of(CARRY_THE_FLAME, EMBER_WARD)),
            Map.entry(AspectKitCatalog.MIST.natureId(), List.of(THROUGH_THE_HAZE, VEIL)),
            Map.entry(AspectKitCatalog.STONE.natureId(), List.of(ROOT_IN_STONE, QUAKE)),
            Map.entry(AspectKitCatalog.TIDE.natureId(), List.of(LOW_TIDE, UNDERTOW)),
            Map.entry(AspectKitCatalog.WARDEN.natureId(), List.of(SNARE_SIGIL, RELENTLESS_HUNT)),
            Map.entry(AspectKitCatalog.SEER.natureId(), List.of(FATE_THREAD, SHARED_SIGHT)),
            Map.entry(AspectKitCatalog.REAPER.natureId(), List.of(HARVEST, REAPING_ARC)),
            Map.entry(AspectKitCatalog.WELLSPRING.natureId(), List.of(SOUL_WARD, OVERFLOW)),
            Map.entry(AspectKitCatalog.FORGE.natureId(), List.of(MASTERWORK, MOLTEN_EDGE)),
            Map.entry(AspectKitCatalog.DELVE.natureId(), List.of(VEIN_PULL, STONEWALK)),
            Map.entry(AspectKitCatalog.MASON.natureId(), List.of(BLUEPRINT, SCAFFOLD)),
            Map.entry(AspectKitCatalog.VERDANT.natureId(), List.of(LIVING_WALL, WILD_CALL)),
            Map.entry(AspectKitCatalog.WAYFARER.natureId(), List.of(ANCHOR_RECALL, TRAILBLAZE)),
            Map.entry(AspectKitCatalog.LULL.natureId(), List.of(DREAMFALL, SLEEPWALKER)),
            Map.entry(AspectKitCatalog.GLOAM.natureId(), List.of(SHADE_STEP, SNUFF)),
            Map.entry(AspectKitCatalog.RIME.natureId(), List.of(ENCASE, RIMEPATH)),
            Map.entry(AspectKitCatalog.FURY.natureId(), List.of(GROUND_SLAM, SECOND_WIND)),
            Map.entry(AspectKitCatalog.THRESHOLD.natureId(), List.of(DOORWAY, BANISH)),
            Map.entry(AspectKitCatalog.ANCHOR.natureId(), List.of(CRUSH, GRAVITY_WELL)),
            Map.entry(AspectKitCatalog.HOURGLASS.natureId(), List.of(REWIND, STILL_MOMENT)),
            Map.entry(AspectKitCatalog.HERALD.natureId(), List.of(RALLY_CALL, OATH_OF_ARMS)),
            Map.entry(AspectKitCatalog.KEEPER.natureId(), List.of(BULWARK, GATEWAY_STEP)),
            Map.entry(AspectKitCatalog.FORTUNE.natureId(), List.of(TWIST_FATE, BORROWED_LUCK)),
            Map.entry(AspectKitCatalog.CANTOR.natureId(), List.of(SILENCING_WORD, RALLYING_NAME)),
            Map.entry(AspectKitCatalog.SHEPHERD.natureId(), List.of(PACK_TACTICS, SECOND_BOND)),
            Map.entry(AspectKitCatalog.VESSEL.natureId(), List.of(SWAP_PLACES, SHARED_WOUNDS)),
            Map.entry(AspectKitCatalog.MASQUE.natureId(), List.of(MIRROR_SWAP, STOLEN_VOICE)),
            Map.entry(AspectKitCatalog.UNBROKEN.natureId(), List.of(GRIT, DEFIANCE)),
            Map.entry(AspectKitCatalog.SABOTEUR.natureId(), List.of(CUT_THE_LINES, FALSE_TRAIL)),
            Map.entry(AspectKitCatalog.STANDARD.natureId(), List.of(HOLD_THE_LINE, LAST_RALLY))
    );

    private KitLadder() {
    }

    /** The two abilities a Nature can grow on reaching {@code rank}; empty for ranks not built yet. */
    public static List<AbilityVariant> variants(ResourceLocation natureId, SoulRank rank) {
        if (rank != SoulRank.AWAKENED) {
            return List.of();
        }
        return AWAKENED.getOrDefault(natureId, List.of());
    }

    public static List<AbilityVariant> all() {
        return AWAKENED.values().stream().flatMap(List::stream).toList();
    }

    public static Optional<AbilityVariant> byAbility(ResourceLocation abilityId) {
        return all().stream().filter(variant -> variant.abilityId().equals(abilityId)).findFirst();
    }

    private static AbilityVariant variant(String path, String name, String description, String role) {
        return new AbilityVariant(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "generation/ability/" + path),
                name, description, role, SoulRank.AWAKENED);
    }
}
