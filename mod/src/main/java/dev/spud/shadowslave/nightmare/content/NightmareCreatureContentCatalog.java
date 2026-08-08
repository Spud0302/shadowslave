package dev.spud.shadowslave.nightmare.content;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN content for composing varied Nightmare encounters without
 * making one bespoke Java entity class the canonical scenario definition.
 *
 * <p>Rank/Class names follow the accepted lore vocabulary. Sensory,
 * locomotion, pressure, environment and counterplay tags are Minecraft DESIGN
 * descriptors and are not claimed to be a canonical creature taxonomy.</p>
 */
public final class NightmareCreatureContentCatalog {
    private NightmareCreatureContentCatalog() {
    }

    public enum Rank {
        DORMANT,
        AWAKENED,
        FALLEN
    }

    public enum CreatureClass {
        BEAST,
        MONSTER,
        DEMON,
        DEVIL
    }

    public enum Sense {
        VISION,
        SCENT,
        SOUND,
        VIBRATION,
        HEAT,
        ESSENCE
    }

    public enum Locomotion {
        GROUND,
        CLIMB,
        BURROW,
        SWIM,
        GLIDE
    }

    public enum Pressure {
        AMBUSH,
        PURSUIT,
        AREA_DENIAL,
        PACK_COORDINATION,
        DECEPTION,
        ATTRITION,
        DISPLACEMENT
    }

    public record CreatureProfile(
            String id,
            String displayName,
            Rank rank,
            CreatureClass creatureClass,
            Set<Sense> senses,
            Set<Locomotion> locomotion,
            Set<Pressure> pressures,
            Set<String> environmentTags,
            Set<String> counterplayTags,
            Set<String> appraisalEvidenceTags,
            String presentationCue
    ) {
        public CreatureProfile {
            id = stableId(id);
            displayName = text(displayName, "displayName");
            rank = Objects.requireNonNull(rank, "rank");
            creatureClass = Objects.requireNonNull(creatureClass, "creatureClass");
            senses = nonEmptyCopy(senses, "senses");
            locomotion = nonEmptyCopy(locomotion, "locomotion");
            pressures = nonEmptyCopy(pressures, "pressures");
            environmentTags = tags(environmentTags, "environmentTags");
            counterplayTags = tags(counterplayTags, "counterplayTags");
            appraisalEvidenceTags = tags(appraisalEvidenceTags, "appraisalEvidenceTags");
            presentationCue = text(presentationCue, "presentationCue");
            if (counterplayTags.isEmpty()) {
                throw new IllegalArgumentException("Every creature profile must expose authored counterplay");
            }
        }
    }

    /** First broad encounter-content wave. All names and mechanics are DESIGN. */
    public static List<CreatureProfile> waveOne() {
        List<CreatureProfile> profiles = List.of(
                profile("ash_burrower", "Ash Burrower", Rank.DORMANT, CreatureClass.BEAST,
                        Set.of(Sense.VIBRATION, Sense.SCENT), Set.of(Locomotion.BURROW, Locomotion.GROUND),
                        Set.of(Pressure.AMBUSH, Pressure.DISPLACEMENT),
                        Set.of("ash", "ruins", "loose_ground"), Set.of("stone_floor", "bait_vibration", "high_ground"),
                        Set.of("awareness", "adaptation"), "A furrow of warm ash races toward the nearest footfall."),
                profile("bell_eater", "Bell-Eater", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Sense.SOUND, Sense.VIBRATION), Set.of(Locomotion.CLIMB, Locomotion.GROUND),
                        Set.of(Pressure.PURSUIT, Pressure.AREA_DENIAL),
                        Set.of("tower", "stone", "resonance"), Set.of("silence", "false_echo", "soft_ground"),
                        Set.of("restraint", "misdirection"), "Its plated throat opens whenever metal rings."),
                profile("chainback", "Chainback", Rank.AWAKENED, CreatureClass.MONSTER,
                        Set.of(Sense.VISION, Sense.SCENT), Set.of(Locomotion.GROUND, Locomotion.CLIMB),
                        Set.of(Pressure.PURSUIT, Pressure.DISPLACEMENT),
                        Set.of("cliffs", "bridges", "fortifications"), Set.of("narrow_gap", "cut_anchor", "vertical_escape"),
                        Set.of("mobility", "terrain_use"), "Loose lengths of iron drag from its shell and snag whatever it passes."),
                profile("drowned_listener", "Drowned Listener", Rank.DORMANT, CreatureClass.MONSTER,
                        Set.of(Sense.SOUND, Sense.VIBRATION), Set.of(Locomotion.SWIM, Locomotion.GROUND),
                        Set.of(Pressure.AMBUSH, Pressure.PURSUIT),
                        Set.of("flooded_caves", "harbour", "storm"), Set.of("decoy_sound", "dry_ground", "collapsed_route"),
                        Set.of("warning", "rescue", "misdirection"), "Water stills for a heartbeat before it turns toward a sound."),
                profile("glasswing", "Glasswing", Rank.AWAKENED, CreatureClass.MONSTER,
                        Set.of(Sense.VISION, Sense.HEAT), Set.of(Locomotion.GLIDE, Locomotion.CLIMB),
                        Set.of(Pressure.AMBUSH, Pressure.AREA_DENIAL),
                        Set.of("crystal", "canyon", "sunlight"), Set.of("shade", "smoke", "break_reflection"),
                        Set.of("observation", "preparation"), "Transparent wings flash only when they cross reflected light."),
                profile("gutter_choir", "Gutter Choir", Rank.FALLEN, CreatureClass.DEVIL,
                        Set.of(Sense.SOUND, Sense.ESSENCE), Set.of(Locomotion.GROUND, Locomotion.CLIMB),
                        Set.of(Pressure.DECEPTION, Pressure.ATTRITION, Pressure.AREA_DENIAL),
                        Set.of("city", "sewers", "crowds"), Set.of("isolation", "broken_line_of_hearing", "identify_source"),
                        Set.of("discernment", "resolve"), "Several stolen voices answer from drains before the body appears."),
                profile("hollow_mimic", "Hollow Mimic", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Sense.SOUND, Sense.ESSENCE), Set.of(Locomotion.GROUND, Locomotion.CLIMB),
                        Set.of(Pressure.DECEPTION, Pressure.AMBUSH),
                        Set.of("settlement", "darkness", "interiors"), Set.of("verification_phrase", "paired_watch", "bright_open_space"),
                        Set.of("discernment", "trust"), "It repeats a familiar voice perfectly, but never breathes between words."),
                profile("mire_runner", "Mire Runner", Rank.DORMANT, CreatureClass.BEAST,
                        Set.of(Sense.SCENT, Sense.HEAT), Set.of(Locomotion.GROUND, Locomotion.SWIM),
                        Set.of(Pressure.PURSUIT, Pressure.PACK_COORDINATION),
                        Set.of("marsh", "reeds", "shallow_water"), Set.of("deep_water", "fire", "mask_scent"),
                        Set.of("mobility", "resourcefulness"), "Reeds fold in a widening V before the pack breaks cover."),
                profile("pale_ferryman", "Pale Ferryman", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Sense.VISION, Sense.ESSENCE), Set.of(Locomotion.SWIM, Locomotion.GROUND),
                        Set.of(Pressure.DISPLACEMENT, Pressure.DECEPTION),
                        Set.of("river", "fog", "crossing"), Set.of("refuse_passage", "anchor_rope", "alternate_crossing"),
                        Set.of("caution", "choice"), "A white silhouette waits beside water that has no boat."),
                profile("stone_maw", "Stone Maw", Rank.DORMANT, CreatureClass.MONSTER,
                        Set.of(Sense.VIBRATION, Sense.HEAT), Set.of(Locomotion.BURROW),
                        Set.of(Pressure.AMBUSH, Pressure.AREA_DENIAL),
                        Set.of("quarry", "cave", "stone"), Set.of("timed_crossing", "cold_decoy", "reinforced_floor"),
                        Set.of("timing", "terrain_use"), "The floor cracks in a circle before rows of mineral teeth rise."),
                profile("thorn_matron", "Thorn Matron", Rank.FALLEN, CreatureClass.DEVIL,
                        Set.of(Sense.SCENT, Sense.ESSENCE), Set.of(Locomotion.GROUND, Locomotion.CLIMB),
                        Set.of(Pressure.AREA_DENIAL, Pressure.ATTRITION, Pressure.PACK_COORDINATION),
                        Set.of("forest", "overgrowth", "ruins"), Set.of("controlled_burn", "sever_vines", "stone_route"),
                        Set.of("sacrifice", "planning"), "Fresh briars knot themselves into corridors around its path."),
                profile("veil_stalker", "Veil Stalker", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Sense.ESSENCE, Sense.HEAT), Set.of(Locomotion.GLIDE, Locomotion.GROUND),
                        Set.of(Pressure.AMBUSH, Pressure.DECEPTION, Pressure.PURSUIT),
                        Set.of("mist", "night", "open_ground"), Set.of("cold_shelter", "crosswind", "shared_watch"),
                        Set.of("awareness", "cooperation"), "The mist bends around an absence before claws become visible.")
        );
        validateUniqueIds(profiles);
        return profiles;
    }

    private static CreatureProfile profile(
            String id,
            String name,
            Rank rank,
            CreatureClass creatureClass,
            Set<Sense> senses,
            Set<Locomotion> locomotion,
            Set<Pressure> pressures,
            Set<String> environment,
            Set<String> counterplay,
            Set<String> evidence,
            String cue
    ) {
        return new CreatureProfile(id, name, rank, creatureClass, senses, locomotion, pressures,
                environment, counterplay, evidence, cue);
    }

    private static void validateUniqueIds(List<CreatureProfile> profiles) {
        HashSet<String> ids = new HashSet<>();
        for (CreatureProfile profile : profiles) {
            if (!ids.add(profile.id())) {
                throw new IllegalArgumentException("Duplicate creature profile id: " + profile.id());
            }
        }
    }

    private static String stableId(String value) {
        String checked = text(value, "id").toLowerCase(Locale.ROOT);
        if (!checked.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("id must contain only lowercase letters, numbers and underscores");
        }
        return checked;
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private static Set<String> tags(Set<String> source, String name) {
        HashSet<String> result = new HashSet<>();
        for (String value : Objects.requireNonNull(source, name)) {
            result.add(stableId(value));
        }
        return Set.copyOf(result);
    }

    private static <T> Set<T> nonEmptyCopy(Set<T> source, String name) {
        Set<T> result = Set.copyOf(Objects.requireNonNull(source, name));
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return result;
    }
}
