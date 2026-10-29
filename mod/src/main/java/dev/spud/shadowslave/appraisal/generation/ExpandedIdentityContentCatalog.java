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

    /**
     * The first wave plus the backfill Natures built for the Gates and rank-up
     * passes (Warden, Seer, Reaper, Wellspring) and the utility Natures of
     * the Weaving pass (Forge, Delve, Mason, Verdant) and the Dream Realm
     * Natures (Wayfarer, Lull) and the Awakening-inspired Natures (Gloam, Rime,
     * Fury, Threshold, Anchor, Hourglass) and the Citadel Natures (Herald,
     * Keeper), their Dormant abilities and
     * Flaws. The Dream Candle roll draws on this; the older appraisal flows
     * keep {@link #waveOne()} so their generated identities stay stable.
     */
    public static IdentityPrimitiveCatalog kitWave() {
        IdentityPrimitiveCatalog wave = waveOne();
        return new IdentityPrimitiveCatalog(
                concat(wave.natures(), List.of(
                        nature("warden", "Warden", Set.of("hunt", "pursuit", "warning"), 3),
                        nature("seer", "Seer", Set.of("foresight", "perception", "fate"), 3),
                        nature("reaper", "Reaper", Set.of("hunger", "soul", "harvest"), 3),
                        nature("wellspring", "Wellspring", Set.of("giving", "essence", "renewal"), 3),
                        nature("forge", "Forge", Set.of("forge", "craft", "heat"), 3),
                        nature("delve", "Delve", Set.of("delve", "depth", "ore"), 3),
                        nature("mason", "Mason", Set.of("mason", "building", "order"), 3),
                        nature("verdant", "Verdant", Set.of("verdant", "harvest_field", "tending"), 3),
                        nature("wayfarer", "Wayfarer", Set.of("wayfarer", "journey", "path"), 3),
                        nature("lull", "Lull", Set.of("lull", "slumber", "dream"), 3),
                        nature("gloam", "Gloam", Set.of("gloam", "darkness", "shade"), 3),
                        nature("rime", "Rime", Set.of("rime", "frost", "chill"), 3),
                        nature("fury", "Fury", Set.of("fury", "rage", "wrath"), 3),
                        nature("threshold", "Threshold", Set.of("threshold", "pocket", "doorway"), 3),
                        nature("anchor", "Anchor", Set.of("anchor", "weight", "gravity"), 3),
                        nature("hourglass", "Hourglass", Set.of("hourglass", "time", "stillness"), 3),
                        nature("herald", "Herald", Set.of("herald", "command", "banner"), 3),
                        nature("keeper", "Keeper", Set.of("keeper", "citadel", "ward"), 3)
                )),
                wave.archetypes(),
                concat(wave.abilities(), List.of(
                        ability("mark_prey", "Mark Prey", Set.of("hunt"), Set.of("duty", "warning"), 5),
                        ability("foresight", "Foresight", Set.of("foresight"), Set.of("perception", "witness"), 5),
                        ability("soul_siphon", "Soul Siphon", Set.of("hunger"), Set.of("resolve", "sacrifice"), 5),
                        ability("essence_gift", "Essence Gift", Set.of("giving"), Set.of("duty", "connection"), 5),
                        ability("quench", "Quench", Set.of("forge"), Set.of("precision", "craft"), 5),
                        ability("tunnel_strike", "Tunnel Strike", Set.of("delve"), Set.of("resolve", "force"), 5),
                        ability("raise_wall", "Raise Wall", Set.of("mason"), Set.of("stability", "duty"), 5),
                        ability("bloom", "Bloom", Set.of("verdant"), Set.of("growth", "connection"), 5),
                        ability("lullaby", "Lullaby", Set.of("lull"), Set.of("mercy", "control"), 5),
                        ability("gloam_edge", "Gloam Edge", Set.of("gloam"), Set.of("concealment", "force"), 5),
                        ability("ice_lance", "Ice Lance", Set.of("rime"), Set.of("precision", "control"), 5),
                        ability("provoke", "Provoke", Set.of("fury"), Set.of("resolve", "sacrifice"), 5),
                        ability("pocket", "Pocket", Set.of("threshold"), Set.of("preservation", "concealment"), 5),
                        ability("shift_weight", "Shift Weight", Set.of("anchor"), Set.of("stability", "adaptation"), 5),
                        ability("stasis", "Stasis", Set.of("hourglass"), Set.of("control", "precision"), 5),
                        ability("war_banner", "War Banner", Set.of("herald"), Set.of("resolve", "mercy"), 5),
                        ability("sanctuary", "Sanctuary", Set.of("keeper"), Set.of("preservation", "stability"), 5)
                )),
                concat(wave.flaws(), List.of(
                        flaw("quarry_lock", "Quarry Lock", Set.of("hunt", "pursuit"),
                                Set.of("compulsion", "conditional"), Set.of(), 3),
                        flaw("clouded_eyes", "Clouded Eyes", Set.of("foresight", "perception"),
                                Set.of("sensory", "conditional"), Set.of(), 3),
                        flaw("hunger", "Hunger", Set.of("hunger", "soul"),
                                Set.of("resource", "compulsion"), Set.of(), 3),
                        flaw("brimming_vessel", "Brimming Vessel", Set.of("giving", "essence"),
                                Set.of("resource", "physical"), Set.of(), 3),
                        flaw("cold_hearth", "Cold Hearth", Set.of("forge", "heat"),
                                Set.of("physical", "conditional"), Set.of(), 3),
                        flaw("open_sky", "Open Sky", Set.of("delve", "depth"),
                                Set.of("physical", "conditional"), Set.of(), 3),
                        flaw("far_from_home", "Far From Home", Set.of("mason", "building"),
                                Set.of("resource", "conditional"), Set.of(), 3),
                        flaw("rootbound", "Rootbound", Set.of("verdant", "tending"),
                                Set.of("resource", "conditional"), Set.of(), 3),
                        flaw("wanderlust", "Wanderlust", Set.of("wayfarer", "journey"),
                                Set.of("resource", "conditional"), Set.of(), 3),
                        flaw("insomnia", "Insomnia", Set.of("lull", "slumber"),
                                Set.of("physical", "compulsion"), Set.of(), 3),
                        flaw("sunstruck", "Sunstruck", Set.of("gloam", "darkness"),
                                Set.of("resource", "conditional"), Set.of(), 3),
                        flaw("thin_blood", "Thin Blood", Set.of("rime", "frost"),
                                Set.of("physical", "conditional"), Set.of(), 3),
                        flaw("red_haze", "Red Haze", Set.of("fury", "rage"),
                                Set.of("physical", "compulsion"), Set.of(), 3),
                        flaw("unmoored", "Unmoored", Set.of("threshold", "pocket"),
                                Set.of("physical", "conditional"), Set.of(), 3),
                        flaw("leaden", "Leaden", Set.of("anchor", "weight"),
                                Set.of("physical", "compulsion"), Set.of(), 3),
                        flaw("borrowed_time", "Borrowed Time", Set.of("hourglass", "time"),
                                Set.of("resource", "conditional"), Set.of(), 3),
                        flaw("burden_of_command", "Burden of Command", Set.of("herald", "command"),
                                Set.of("physical", "conditional"), Set.of(), 3),
                        flaw("homebound", "Homebound", Set.of("keeper", "citadel"),
                                Set.of("resource", "conditional"), Set.of(), 3)
                ))
        );
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> all = new java.util.ArrayList<>(first);
        all.addAll(second);
        return List.copyOf(all);
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
