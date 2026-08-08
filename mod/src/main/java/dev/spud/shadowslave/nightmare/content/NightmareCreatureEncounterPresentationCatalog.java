package dev.spud.shadowslave.nightmare.content;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Player-facing encounter reads for already-resolved Nightmare Creature profiles.
 *
 * <p>The source catalogue owns creature identity, Rank/Class and authored behavior
 * tags. This layer only turns that already-resolved Java state into readable cues.
 * It never selects, spawns, ranks, rewards, persists or executes a creature.</p>
 *
 * <p>All exact phases, wording and hint-selection rules are Minecraft DESIGN, not
 * a claimed canonical Nightmare Creature behavior taxonomy.</p>
 */
public final class NightmareCreatureEncounterPresentationCatalog {
    public static final String PROVENANCE = "DESIGN";
    public static final String PRESENTATION_VERSION = "nightmare-creature-encounter-presentation-v1";

    public enum EncounterPhase {
        FIRST_SIGN,
        THREAT_READ,
        COUNTERPLAY
    }

    public record EncounterPresentation(
            String creatureId,
            String displayName,
            NightmareCreatureContentCatalog.Rank rank,
            NightmareCreatureContentCatalog.CreatureClass creatureClass,
            String firstSign,
            String threatRead,
            String counterplayHint,
            Set<String> sourceCounterplayTags,
            String boundary,
            String provenance,
            String presentationVersion
    ) {
        public EncounterPresentation {
            creatureId = text(creatureId, "creatureId");
            displayName = text(displayName, "displayName");
            rank = Objects.requireNonNull(rank, "rank");
            creatureClass = Objects.requireNonNull(creatureClass, "creatureClass");
            firstSign = text(firstSign, "firstSign");
            threatRead = text(threatRead, "threatRead");
            counterplayHint = text(counterplayHint, "counterplayHint");
            sourceCounterplayTags = Set.copyOf(Objects.requireNonNull(sourceCounterplayTags, "sourceCounterplayTags"));
            boundary = text(boundary, "boundary");
            provenance = text(provenance, "provenance");
            presentationVersion = text(presentationVersion, "presentationVersion");
        }
    }

    private record ProfileText(String id, String threatRead, Map<String, String> counterplayHints, String boundary) {
        private ProfileText {
            id = text(id, "id");
            threatRead = text(threatRead, "threatRead");
            counterplayHints = Map.copyOf(Objects.requireNonNull(counterplayHints, "counterplayHints"));
            boundary = text(boundary, "boundary");
            if (counterplayHints.isEmpty()) {
                throw new IllegalArgumentException("counterplayHints cannot be empty");
            }
        }
    }

    private final Map<String, NightmareCreatureContentCatalog.CreatureProfile> creatures;
    private final Map<String, ProfileText> presentation;

    private NightmareCreatureEncounterPresentationCatalog(
            List<NightmareCreatureContentCatalog.CreatureProfile> creatures,
            List<ProfileText> presentation
    ) {
        this.creatures = indexCreatures(creatures);
        this.presentation = indexPresentation(presentation);
        if (!this.creatures.keySet().equals(this.presentation.keySet())) {
            throw new IllegalArgumentException("Encounter presentation must cover the creature catalogue exactly");
        }
        for (var entry : this.presentation.entrySet()) {
            var creature = this.creatures.get(entry.getKey());
            if (!creature.counterplayTags().equals(entry.getValue().counterplayHints().keySet())) {
                throw new IllegalArgumentException("Counterplay presentation must cover source tags exactly for " + entry.getKey());
            }
        }
    }

    public static NightmareCreatureEncounterPresentationCatalog waveOne() {
        return new NightmareCreatureEncounterPresentationCatalog(
                NightmareCreatureContentCatalog.waveOne(),
                List.of(
                        profile("ash_burrower", "The moving furrow marks an ambush route under loose ground; its vibration sense makes careless footwork part of the threat.",
                                hints("stone_floor", "Reach firm stone where tunnelling is easier to read.", "bait_vibration", "Throw a false vibration away from the route you need.", "high_ground", "Climb above the loose surface before it closes distance."),
                                "These cues describe this authored Ash Burrower only; they do not define universal burrowing-creature senses."),
                        profile("bell_eater", "Ringing metal feeds its pursuit and area pressure, so the soundscape itself can reveal why it is closing in.",
                                hints("silence", "Stop unnecessary ringing before trying to break pursuit.", "false_echo", "Create a false sound source to pull its attention off the real route.", "soft_ground", "Move over material that carries less sharp vibration."),
                                "Sound interaction is DESIGN for this creature, not a canonical rule for Demons or Awakened creatures."),
                        profile("chainback", "Dragging iron turns narrow terrain into a displacement hazard: the danger is being snagged or forced off a safe line.",
                                hints("narrow_gap", "Use a gap its shell and trailing chains cannot cross cleanly.", "cut_anchor", "Break or release an anchor point before the chain can control the route.", "vertical_escape", "Change elevation where its dragging reach is least useful."),
                                "Rank and Class are canon vocabulary; this exact chain behavior and counterplay are DESIGN."),
                        profile("drowned_listener", "The pause in the water is a warning that sound has become a targeting cue for an approaching ambusher.",
                                hints("decoy_sound", "Put a deliberate sound somewhere you do not intend to remain.", "dry_ground", "Force the encounter onto ground where its swimming approach loses value.", "collapsed_route", "Close a flooded approach after crossing it."),
                                "This does not imply all aquatic Nightmare Creatures hunt by sound."),
                        profile("glasswing", "Reflected light can betray its glide before the strike; open glare favors the creature more than shaded, visually broken terrain.",
                                hints("shade", "Move through shade to reduce clean reflected approach lines.", "smoke", "Break long sightlines with an obscuring screen.", "break_reflection", "Change or cover reflective surfaces that reveal and guide the encounter."),
                                "No universal weakness to shade, smoke or reflection is claimed for Nightmare Creatures."),
                        profile("gutter_choir", "Multiple stolen voices create an information threat before direct contact: identifying the real source matters as much as surviving attrition.",
                                hints("isolation", "Separate one suspicious source from the chorus before acting on it.", "broken_line_of_hearing", "Break continuous hearing contact when the voices are shaping decisions.", "identify_source", "Verify which physical source actually produced the call."),
                                "Voice theft and these counters are authored creature mechanics, not Devil-class guarantees."),
                        profile("hollow_mimic", "A familiar voice without breathing is a verification problem disguised as a rescue or social cue.",
                                hints("verification_phrase", "Ask for information the imitated voice should know but an observer may not.", "paired_watch", "Use two observers so one voice cannot define the whole situation.", "bright_open_space", "Move the contact into open visibility before trusting it."),
                                "This presentation never grants perfect lie detection or universal mimic detection."),
                        profile("mire_runner", "The widening wake in reeds announces coordinated pursuit; scent and heat pressure make a straight flight increasingly predictable.",
                                hints("deep_water", "Cross water deep enough to disrupt its ground-running pack line.", "fire", "Use controlled fire as a local barrier rather than assuming it is a universal weakness.", "mask_scent", "Reduce the scent trail before changing direction."),
                                "Pack behavior and these counters belong to this DESIGN profile only."),
                        profile("pale_ferryman", "Its waiting posture turns a crossing into a choice-and-displacement threat rather than an automatic combat encounter.",
                                hints("refuse_passage", "Treat refusing the offered crossing as a valid tactical choice.", "anchor_rope", "Secure your own line before entering water or fog.", "alternate_crossing", "Search for another route instead of accepting the presented one."),
                                "The encounter does not establish a canonical ferryman archetype or supernatural bargain rule."),
                        profile("stone_maw", "Circular cracking is the readable tell before an underground area-denial strike; timing and surface choice matter more than trading blows.",
                                hints("timed_crossing", "Cross exposed stone immediately after a failed emergence rather than during the warning tell.", "cold_decoy", "Place a cooler false target away from the intended route.", "reinforced_floor", "Use reinforced ground where an emergence is harder to conceal."),
                                "Heat/vibration sensing and timing windows are exact DESIGN, not Monster-class rules."),
                        profile("thorn_matron", "Growing briar corridors convert the battlefield over time, combining attrition with pack pressure and shrinking safe routes.",
                                hints("controlled_burn", "Burn only the route needed before growth closes it again.", "sever_vines", "Cut connecting growth to reopen one escape lane.", "stone_route", "Favor hard routes where new briars have less control over movement."),
                                "This does not claim Fallen Devils universally reshape terrain or command packs."),
                        profile("veil_stalker", "The mist bending around an absence is the warning; heat and essence pressure make isolated movement especially dangerous.",
                                hints("cold_shelter", "Use a cooler shelter to reduce the contrast of your position.", "crosswind", "Move where crosswind disturbs the mist enough to expose approach changes.", "shared_watch", "Have another observer cover the angle you cannot watch while moving."),
                                "Mist, heat and essence interactions are authored for this creature and are not universal stealth rules.")
                )
        );
    }

    public EncounterPresentation compose(long seed, String creatureId) {
        NightmareCreatureContentCatalog.CreatureProfile creature = creatures.get(creatureId);
        ProfileText text = presentation.get(creatureId);
        if (creature == null || text == null) {
            throw new IllegalArgumentException("Unknown creature id: " + creatureId);
        }
        List<String> tags = creature.counterplayTags().stream().sorted().toList();
        int index = Math.floorMod(Long.hashCode(seed ^ stableHash(creature.id())), tags.size());
        String tag = tags.get(index);
        return new EncounterPresentation(
                creature.id(), creature.displayName(), creature.rank(), creature.creatureClass(),
                creature.presentationCue(), text.threatRead(), text.counterplayHints().get(tag),
                creature.counterplayTags(), text.boundary(), PROVENANCE, PRESENTATION_VERSION
        );
    }

    public Set<String> creatureIds() {
        return Set.copyOf(creatures.keySet());
    }

    private static ProfileText profile(String id, String threatRead, Map<String, String> hints, String boundary) {
        return new ProfileText(id, threatRead, hints, boundary);
    }

    private static Map<String, String> hints(String... values) {
        if (values.length == 0 || values.length % 2 != 0) {
            throw new IllegalArgumentException("hints require tag/text pairs");
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            if (result.put(text(values[i], "hint tag"), text(values[i + 1], "hint text")) != null) {
                throw new IllegalArgumentException("Duplicate hint tag: " + values[i]);
            }
        }
        return result;
    }

    private static Map<String, NightmareCreatureContentCatalog.CreatureProfile> indexCreatures(List<NightmareCreatureContentCatalog.CreatureProfile> source) {
        Map<String, NightmareCreatureContentCatalog.CreatureProfile> result = new HashMap<>();
        for (var creature : source) {
            if (result.put(creature.id(), creature) != null) {
                throw new IllegalArgumentException("Duplicate creature id: " + creature.id());
            }
        }
        return Map.copyOf(result);
    }

    private static Map<String, ProfileText> indexPresentation(List<ProfileText> source) {
        Map<String, ProfileText> result = new HashMap<>();
        for (var profile : source) {
            if (result.put(profile.id(), profile) != null) {
                throw new IllegalArgumentException("Duplicate presentation id: " + profile.id());
            }
        }
        return Map.copyOf(result);
    }

    private static long stableHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}