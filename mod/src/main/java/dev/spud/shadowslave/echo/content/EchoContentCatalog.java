package dev.spud.shadowslave.echo.content;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Authored DESIGN content describing reusable Echo identities and practical roles.
 *
 * <p>Echo state remains Java-owned. Entity models, navigation, animation and AI
 * execution are removable provider concerns and must not become the canonical
 * definition of an Echo.</p>
 */
public final class EchoContentCatalog {
    private EchoContentCatalog() {
    }

    public enum OriginKind {
        CREATURE_DERIVED,
        ARTIFICIAL
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

    public enum Role {
        MOUNT,
        CARRIER,
        SCOUT,
        GUARD,
        TRACKER,
        PURSUER,
        SCREEN,
        DISRUPTOR,
        AREA_CONTROL,
        ESCORT,
        LABOUR
    }

    public enum CommandMode {
        FOLLOW,
        HOLD,
        MOVE_TO,
        CARRY,
        MOUNT,
        SEARCH,
        INTERCEPT,
        SCREEN,
        GUARD_POINT,
        WITHDRAW
    }

    public record EchoProfile(
            String id,
            String displayName,
            OriginKind originKind,
            Optional<String> sourceCreatureId,
            Optional<Rank> sourceRank,
            Optional<CreatureClass> sourceClass,
            Set<Role> roles,
            Set<CommandMode> commandModes,
            Set<String> utilityTags,
            String tacticalUse,
            String presentationCue
    ) {
        public EchoProfile {
            id = stableId(id);
            displayName = text(displayName, "displayName");
            originKind = Objects.requireNonNull(originKind, "originKind");
            sourceCreatureId = Objects.requireNonNull(sourceCreatureId, "sourceCreatureId").map(EchoContentCatalog::stableId);
            sourceRank = Objects.requireNonNull(sourceRank, "sourceRank");
            sourceClass = Objects.requireNonNull(sourceClass, "sourceClass");
            roles = nonEmptyCopy(roles, "roles");
            commandModes = nonEmptyCopy(commandModes, "commandModes");
            utilityTags = tags(utilityTags, "utilityTags");
            tacticalUse = text(tacticalUse, "tacticalUse");
            presentationCue = text(presentationCue, "presentationCue");

            if (originKind == OriginKind.CREATURE_DERIVED) {
                if (sourceCreatureId.isEmpty() || sourceRank.isEmpty() || sourceClass.isEmpty()) {
                    throw new IllegalArgumentException("Creature-derived Echoes require source identity, Rank and Class");
                }
            } else if (sourceCreatureId.isPresent() || sourceRank.isPresent() || sourceClass.isPresent()) {
                throw new IllegalArgumentException("Artificial Echo templates must not invent a creature source identity");
            }
        }
    }

    /** First reusable Echo content wave. All names, roles and commands are DESIGN. */
    public static List<EchoProfile> waveOne() {
        List<EchoProfile> profiles = List.of(
                derived("ash_burrower", "Ash Burrower", Rank.DORMANT, CreatureClass.BEAST,
                        Set.of(Role.CARRIER, Role.LABOUR, Role.GUARD),
                        Set.of(CommandMode.FOLLOW, CommandMode.CARRY, CommandMode.HOLD, CommandMode.GUARD_POINT),
                        Set.of("heavy_load", "loose_ground", "camp_work"),
                        "Moves supplies and braces a camp while retaining enough mass to block narrow approaches.",
                        "Ash rolls from its shell as it settles beside the assigned load."),
                derived("bell_eater", "Bell-Eater", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Role.TRACKER, Role.DISRUPTOR, Role.GUARD),
                        Set.of(CommandMode.FOLLOW, CommandMode.SEARCH, CommandMode.INTERCEPT, CommandMode.HOLD),
                        Set.of("sound_tracking", "resonance", "warning"),
                        "Tracks noisy targets and can be positioned where its sensitivity to ringing becomes an early-warning tool.",
                        "Its plated throat opens toward the loudest metallic vibration."),
                derived("chainback", "Chainback", Rank.AWAKENED, CreatureClass.MONSTER,
                        Set.of(Role.CARRIER, Role.ESCORT, Role.SCREEN),
                        Set.of(CommandMode.FOLLOW, CommandMode.CARRY, CommandMode.SCREEN, CommandMode.WITHDRAW),
                        Set.of("cliff_route", "load_bearing", "formation_screen"),
                        "Carries bulky loads across broken routes and screens slower companions during withdrawal.",
                        "Loose iron drags behind its shell in a moving barrier."),
                derived("drowned_listener", "Drowned Listener", Rank.DORMANT, CreatureClass.MONSTER,
                        Set.of(Role.SCOUT, Role.TRACKER, Role.ESCORT),
                        Set.of(CommandMode.FOLLOW, CommandMode.SEARCH, CommandMode.MOVE_TO, CommandMode.WITHDRAW),
                        Set.of("flooded_route", "sound_search", "water_crossing"),
                        "Searches flooded passages by sound and helps identify safer routes through waterlogged terrain.",
                        "Water dimples around its head as it turns toward distant noise."),
                derived("glasswing", "Glasswing", Rank.AWAKENED, CreatureClass.MONSTER,
                        Set.of(Role.SCOUT, Role.SCREEN, Role.PURSUER),
                        Set.of(CommandMode.SEARCH, CommandMode.MOVE_TO, CommandMode.SCREEN, CommandMode.INTERCEPT),
                        Set.of("aerial_scout", "open_ground", "visual_signal"),
                        "Provides fast elevated reconnaissance and can pressure exposed targets without committing the owner to pursuit.",
                        "A brief flash of reflected light marks its turn overhead."),
                derived("gutter_choir", "Gutter Choir", Rank.FALLEN, CreatureClass.DEVIL,
                        Set.of(Role.DISRUPTOR, Role.AREA_CONTROL, Role.GUARD),
                        Set.of(CommandMode.HOLD, CommandMode.GUARD_POINT, CommandMode.SCREEN, CommandMode.WITHDRAW),
                        Set.of("voice_pressure", "urban_defence", "denial"),
                        "Best used to make a defended route difficult to approach rather than as a simple pursuit creature.",
                        "Several mismatched voices answer from nearby openings at once."),
                derived("hollow_mimic", "Hollow Mimic", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Role.SCOUT, Role.DISRUPTOR, Role.GUARD),
                        Set.of(CommandMode.SEARCH, CommandMode.MOVE_TO, CommandMode.HOLD, CommandMode.WITHDRAW),
                        Set.of("deception", "interior_scout", "false_signal"),
                        "Supports misdirection and cautious scouting in structures where direct line-of-sight is dangerous.",
                        "A familiar voice sounds from where no ally is standing."),
                derived("mire_runner", "Mire Runner", Rank.DORMANT, CreatureClass.BEAST,
                        Set.of(Role.MOUNT, Role.SCOUT, Role.PURSUER),
                        Set.of(CommandMode.FOLLOW, CommandMode.MOUNT, CommandMode.SEARCH, CommandMode.WITHDRAW),
                        Set.of("fast_mount", "marsh", "shallow_water"),
                        "A lightweight travel Echo for wet ground, reconnaissance and rapid disengagement.",
                        "Its feet barely break the water before it accelerates."),
                derived("pale_ferryman", "Pale Ferryman", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Role.ESCORT, Role.CARRIER, Role.GUARD),
                        Set.of(CommandMode.FOLLOW, CommandMode.CARRY, CommandMode.GUARD_POINT, CommandMode.MOVE_TO),
                        Set.of("river_crossing", "fog", "escort"),
                        "Useful around crossings and shoreline travel where protecting passengers matters more than chasing enemies.",
                        "The pale figure waits motionless at the waterline until commanded."),
                derived("stone_maw", "Stone Maw", Rank.DORMANT, CreatureClass.MONSTER,
                        Set.of(Role.GUARD, Role.AREA_CONTROL, Role.LABOUR),
                        Set.of(CommandMode.HOLD, CommandMode.GUARD_POINT, CommandMode.MOVE_TO, CommandMode.WITHDRAW),
                        Set.of("ground_denial", "excavation", "fixed_defence"),
                        "Excels at holding ground, obstructing approaches and rough excavation rather than long-distance travel.",
                        "A ring of hairline cracks follows it beneath the floor."),
                derived("thorn_matron", "Thorn Matron", Rank.FALLEN, CreatureClass.DEVIL,
                        Set.of(Role.AREA_CONTROL, Role.GUARD, Role.ESCORT),
                        Set.of(CommandMode.HOLD, CommandMode.GUARD_POINT, CommandMode.SCREEN, CommandMode.WITHDRAW),
                        Set.of("living_barrier", "camp_defence", "route_control"),
                        "Shapes a dangerous defensive perimeter and protects a retreat route when mobility is secondary.",
                        "Fresh briars bend toward the point it has been ordered to hold."),
                derived("veil_stalker", "Veil Stalker", Rank.AWAKENED, CreatureClass.DEMON,
                        Set.of(Role.SCOUT, Role.TRACKER, Role.PURSUER),
                        Set.of(CommandMode.FOLLOW, CommandMode.SEARCH, CommandMode.INTERCEPT, CommandMode.WITHDRAW),
                        Set.of("mist_scout", "night_tracking", "ambush_screen"),
                        "Tracks through obscured terrain and intercepts threats before they reach slower companions.",
                        "Mist folds around a moving absence before its outline resolves."),
                artificial("steel_courser", "Steel Courser",
                        Set.of(Role.MOUNT, Role.ESCORT),
                        Set.of(CommandMode.FOLLOW, CommandMode.MOUNT, CommandMode.MOVE_TO, CommandMode.WITHDRAW),
                        Set.of("artificial", "fast_mount", "formation_travel"),
                        "A deliberately narrow artificial-Echo template focused on reliable mounted travel rather than creature provenance.",
                        "Ethereal sparks knit themselves into a steel stallion ready to run." )
        );
        validateUniqueIds(profiles);
        return profiles;
    }

    private static EchoProfile derived(
            String sourceCreatureId,
            String displayName,
            Rank sourceRank,
            CreatureClass sourceClass,
            Set<Role> roles,
            Set<CommandMode> commandModes,
            Set<String> utilityTags,
            String tacticalUse,
            String presentationCue
    ) {
        return new EchoProfile(
                sourceCreatureId,
                displayName,
                OriginKind.CREATURE_DERIVED,
                Optional.of(sourceCreatureId),
                Optional.of(sourceRank),
                Optional.of(sourceClass),
                roles,
                commandModes,
                utilityTags,
                tacticalUse,
                presentationCue
        );
    }

    private static EchoProfile artificial(
            String id,
            String displayName,
            Set<Role> roles,
            Set<CommandMode> commandModes,
            Set<String> utilityTags,
            String tacticalUse,
            String presentationCue
    ) {
        return new EchoProfile(
                id,
                displayName,
                OriginKind.ARTIFICIAL,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                roles,
                commandModes,
                utilityTags,
                tacticalUse,
                presentationCue
        );
    }

    private static void validateUniqueIds(List<EchoProfile> profiles) {
        HashSet<String> ids = new HashSet<>();
        for (EchoProfile profile : profiles) {
            if (!ids.add(profile.id())) {
                throw new IllegalArgumentException("Duplicate Echo profile id: " + profile.id());
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
