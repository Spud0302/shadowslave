package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * First player-facing content expansion for the procedural identity foundation.
 *
 * <p>These primitives are Minecraft DESIGN content. They are constrained by the
 * accepted lore model, but their names, tags, weights and effect identifiers are
 * not claimed to be a canonical Nightmare Spell generation formula.</p>
 */
public final class ExpandedIdentityContentCatalog {
    private static final String NAMESPACE = "shadowslave";

    private ExpandedIdentityContentCatalog() {
    }

    /**
     * Broad first-wave catalogue intended to make generated First-Nightmare
     * identities feel meaningfully less repetitive while keeping every mechanic
     * behind stable reusable primitive IDs.
     */
    public static IdentityPrimitiveCatalog waveOne() {
        return new IdentityPrimitiveCatalog(
                List.of(
                        nature("ash", "Ash", Set.of("ash", "endurance", "aftermath"), 3),
                        nature("bell", "Bell", Set.of("sound", "warning", "resonance"), 3),
                        nature("ember", "Ember", Set.of("ember", "light", "preservation"), 5),
                        nature("glass", "Glass", Set.of("glass", "reflection", "precision"), 3),
                        nature("hollow", "Hollow", Set.of("hollow", "absence", "concealment"), 2),
                        nature("mist", "Mist", Set.of("mist", "concealment", "perception"), 3),
                        nature("road", "Road", Set.of("path", "movement", "guidance"), 4),
                        nature("signal", "Signal", Set.of("signal", "warning", "perception"), 4),
                        nature("stone", "Stone", Set.of("stone", "endurance", "stability"), 4),
                        nature("thorn", "Thorn", Set.of("thorn", "growth", "retaliation"), 3),
                        nature("thread", "Thread", Set.of("thread", "connection", "precision"), 3),
                        nature("tide", "Tide", Set.of("water", "rhythm", "adaptation"), 3)
                ),
                List.of(
                        archetype("bearer", "Bearer of %s", Set.of("duty", "endurance"), 3),
                        archetype("keeper", "Keeper of %s", Set.of("duty", "preservation"), 4),
                        archetype("last", "Last %s", Set.of("resolve", "sacrifice"), 2),
                        archetype("pilgrim", "%s Pilgrim", Set.of("path", "resolve"), 3),
                        archetype("seeker", "%s Seeker", Set.of("perception", "curiosity"), 3),
                        archetype("sentinel", "%s Sentinel", Set.of("warning", "duty"), 4),
                        archetype("voice", "Voice of %s", Set.of("signal", "social"), 2),
                        archetype("wanderer", "%s Wanderer", Set.of("movement", "escape"), 3),
                        archetype("weaver", "Weaver of %s", Set.of("connection", "precision"), 2),
                        archetype("witness", "%s Witness", Set.of("witness", "perception"), 3)
                ),
                List.of(
                        ability("ashen_guard", "Ashen Guard", Set.of("ash"), Set.of("endurance", "duty"), 5),
                        ability("carry_the_flame", "Carry the Flame", Set.of("ember"), Set.of("duty", "sacrifice"), 3),
                        ability("chime_warning", "Chime Warning", Set.of("sound"), Set.of("warning", "perception"), 5),
                        ability("cut_the_thread", "Cut the Thread", Set.of("thread"), Set.of("precision", "severance"), 3),
                        ability("endure_the_ruin", "Endure the Ruin", Set.of("ash"), Set.of("preservation", "aftermath"), 3),
                        ability("glass_edge", "Glass Edge", Set.of("glass"), Set.of("precision", "retaliation"), 4),
                        ability("hollow_step", "Hollow Step", Set.of("hollow"), Set.of("absence", "movement"), 4),
                        ability("hear_the_call", "Hear the Distant Call", Set.of("signal"), Set.of("witness", "warning"), 3),
                        ability("holdfast", "Holdfast", Set.of("stone"), Set.of("stability", "preservation"), 5),
                        ability("kindle", "Kindle", Set.of("ember"), Set.of("preservation", "resolve"), 5),
                        ability("low_tide", "Low Tide", Set.of("water"), Set.of("adaptation", "concealment"), 3),
                        ability("mirror_glimpse", "Mirror Glimpse", Set.of("glass"), Set.of("reflection", "perception"), 4),
                        ability("mist_passage", "Mist Passage", Set.of("mist"), Set.of("concealment", "movement"), 5),
                        ability("resonant_mark", "Resonant Mark", Set.of("sound"), Set.of("resonance", "guidance"), 3),
                        ability("returning_tide", "Returning Tide", Set.of("water"), Set.of("rhythm", "adaptation"), 5),
                        ability("root_in_stone", "Root in Stone", Set.of("stone"), Set.of("endurance", "stability"), 3),
                        ability("shorten_the_road", "Shorten the Road", Set.of("path"), Set.of("escape", "movement"), 3),
                        ability("still_presence", "Still Presence", Set.of("hollow"), Set.of("absence", "concealment"), 3),
                        ability("thorn_lash", "Thorn Lash", Set.of("thorn"), Set.of("retaliation", "growth"), 5),
                        ability("through_the_haze", "Through the Haze", Set.of("mist"), Set.of("perception", "guidance"), 3),
                        ability("warning_pulse", "Warning Pulse", Set.of("signal"), Set.of("warning", "perception"), 5),
                        ability("waymark", "Waymark", Set.of("path"), Set.of("guidance", "movement"), 5),
                        ability("weave_link", "Weave Link", Set.of("thread"), Set.of("connection", "guidance"), 5),
                        ability("wicked_bloom", "Wicked Bloom", Set.of("thorn"), Set.of("growth", "preservation"), 3)
                ),
                List.of(
                        flaw(
                                "bell_without_silence",
                                "Bell Without Silence",
                                Set.of("sound", "warning"),
                                Set.of("sensory", "social"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "borrowed_breath",
                                "Borrowed Breath",
                                Set.of("mist", "concealment"),
                                Set.of("resource", "conditional"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "brittle_oath",
                                "Brittle Oath",
                                Set.of("glass", "precision"),
                                Set.of("compulsion", "conditional"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "burden_of_the_last",
                                "Burden of the Last",
                                Set.of("sacrifice", "aftermath"),
                                Set.of("resource", "long_horizon"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "cold_ash",
                                "Cold Ash",
                                Set.of("ember", "preservation"),
                                Set.of("environmental", "water_vulnerability"),
                                Set.of(),
                                4
                        ),
                        flaw(
                                "echoing_pain",
                                "Echoing Pain",
                                Set.of("resonance", "warning"),
                                Set.of("sensory", "pain"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "empty_seat",
                                "Empty Seat",
                                Set.of("absence", "witness"),
                                Set.of("social", "attachment"),
                                Set.of(),
                                2
                        ),
                        flaw(
                                "glass_heart",
                                "Glass Heart",
                                Set.of("reflection", "precision"),
                                Set.of("emotional", "conditional"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "narrow_path",
                                "Narrow Path",
                                Set.of("path", "guidance"),
                                Set.of("compulsion", "conditional"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "open_flame",
                                "Open Flame",
                                Set.of("witness", "warning"),
                                Set.of("social", "disclosure"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "rooted_step",
                                "Rooted Step",
                                Set.of("duty", "endurance"),
                                Set.of("immobility", "physical"),
                                Set.of("movement"),
                                3
                        ),
                        flaw(
                                "stone_sleep",
                                "Stone Sleep",
                                Set.of("stone", "stability"),
                                Set.of("physical", "sluggishness"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "thorned_mercy",
                                "Thorned Mercy",
                                Set.of("growth", "retaliation"),
                                Set.of("pain", "conditional"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "tidal_debt",
                                "Tidal Debt",
                                Set.of("water", "rhythm"),
                                Set.of("resource", "cyclical"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "unanswered_call",
                                "Unanswered Call",
                                Set.of("signal", "duty"),
                                Set.of("compulsion", "social"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "uncut_thread",
                                "Uncut Thread",
                                Set.of("thread", "connection"),
                                Set.of("attachment", "long_horizon"),
                                Set.of(),
                                3
                        ),
                        flaw(
                                "witness_burden",
                                "Witness's Burden",
                                Set.of("perception", "witness"),
                                Set.of("disclosure", "psychological"),
                                Set.of(),
                                3
                        )
                )
        );
    }

    private static IdentityPrimitiveCatalog.Nature nature(
            String path,
            String name,
            Set<String> tags,
            int weight
    ) {
        return new IdentityPrimitiveCatalog.Nature(id("generation/nature/" + path), name, tags, weight);
    }

    private static IdentityPrimitiveCatalog.Archetype archetype(
            String path,
            String pattern,
            Set<String> tags,
            int weight
    ) {
        return new IdentityPrimitiveCatalog.Archetype(id("generation/archetype/" + path), pattern, tags, weight);
    }

    private static IdentityPrimitiveCatalog.Ability ability(
            String path,
            String name,
            Set<String> requiredNatureTags,
            Set<String> affinityTags,
            int weight
    ) {
        return new IdentityPrimitiveCatalog.Ability(
                id("generation/ability/" + path),
                name,
                requiredNatureTags,
                affinityTags,
                weight
        );
    }

    private static IdentityPrimitiveCatalog.Flaw flaw(
            String path,
            String name,
            Set<String> affinityTags,
            Set<String> traitTags,
            Set<String> incompatibleNatureTags,
            int weight
    ) {
        return new IdentityPrimitiveCatalog.Flaw(
                id("generation/flaw/" + path),
                name,
                id("generation/flaw_effect/" + path),
                affinityTags,
                traitTags,
                incompatibleNatureTags,
                weight
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
