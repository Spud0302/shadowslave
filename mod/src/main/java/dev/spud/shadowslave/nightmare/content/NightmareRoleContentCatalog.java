package dev.spud.shadowslave.nightmare.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Authored DESIGN content for historical roles inside reconstructed Nightmares.
 *
 * <p>Canon establishes that challengers inhabit roles/bodies within reconstructed
 * history, but the Spell's role-selection formula is UNKNOWN. This catalogue
 * therefore exposes modular authored primitives plus a deterministic DESIGN
 * matcher for game content. It does not claim to reproduce the Spell.</p>
 */
public final class NightmareRoleContentCatalog {
    private NightmareRoleContentCatalog() {
    }

    public enum SocialPosition {
        BOUND,
        DEPENDENT,
        OUTSIDER,
        LABOURER,
        RETAINER,
        INITIATE,
        AUXILIARY,
        WITNESS
    }

    public enum Condition {
        HEALTHY,
        EXHAUSTED,
        WOUNDED,
        HUNGRY,
        RESTRAINED,
        ISOLATED,
        DISTRUSTED,
        UNDER_OBSERVATION
    }

    public enum Relationship {
        OWES_DUTY,
        PROTECTS_SOMEONE,
        DEPENDS_ON_SOMEONE,
        RIVALS_SOMEONE,
        FEARS_AUTHORITY,
        KNOWS_A_SECRET,
        IS_BEING_WATCHED,
        HAS_LOCAL_TIES
    }

    public enum Knowledge {
        ROUTES,
        RITUALS,
        MEDICINE,
        LEDGERS,
        SIGNALS,
        WATERWORKS,
        LANGUAGES,
        CREATURE_HABITS
    }

    public enum AlignmentPressure {
        AUTHORITY,
        CIVILIANS,
        FAMILY,
        FACTION,
        SELF_PRESERVATION,
        FAITH,
        TRUTH,
        SURVIVAL_GROUP
    }

    public record RoleProfile(
            String id,
            String displayName,
            String occupation,
            SocialPosition socialPosition,
            Set<String> obligations,
            Set<Relationship> relationships,
            Set<Condition> conditions,
            Set<Knowledge> knowledge,
            Set<AlignmentPressure> pressures,
            Set<String> evidenceAffinities,
            String startingConstraint,
            String hiddenOpportunity,
            String presentationCue
    ) {
        public RoleProfile {
            id = stableId(id);
            displayName = text(displayName, "displayName");
            occupation = text(occupation, "occupation");
            socialPosition = Objects.requireNonNull(socialPosition, "socialPosition");
            obligations = tags(obligations, "obligations");
            relationships = nonEmptyCopy(relationships, "relationships");
            conditions = nonEmptyCopy(conditions, "conditions");
            knowledge = nonEmptyCopy(knowledge, "knowledge");
            pressures = nonEmptyCopy(pressures, "pressures");
            evidenceAffinities = tags(evidenceAffinities, "evidenceAffinities");
            startingConstraint = text(startingConstraint, "startingConstraint");
            hiddenOpportunity = text(hiddenOpportunity, "hiddenOpportunity");
            presentationCue = text(presentationCue, "presentationCue");
        }
    }

    public record RoleMatch(RoleProfile role, long seed, int designWeight, List<String> matchedEvidence) {
        public RoleMatch {
            role = Objects.requireNonNull(role, "role");
            matchedEvidence = List.copyOf(Objects.requireNonNull(matchedEvidence, "matchedEvidence"));
            if (designWeight <= 0) {
                throw new IllegalArgumentException("designWeight must be positive");
            }
        }
    }

    /** First authored role wave. Exact roles, constraints and opportunities are DESIGN. */
    public static List<RoleProfile> waveOne() {
        List<RoleProfile> roles = List.of(
                role("watch_apprentice", "Watch Apprentice", "warning-tower apprentice", SocialPosition.DEPENDENT,
                        Set.of("keep_the_watch", "carry_the_warning"), Set.of(Relationship.OWES_DUTY, Relationship.PROTECTS_SOMEONE),
                        Set.of(Condition.EXHAUSTED), Set.of(Knowledge.SIGNALS, Knowledge.ROUTES),
                        Set.of(AlignmentPressure.AUTHORITY, AlignmentPressure.CIVILIANS), Set.of("warning", "rescue", "signals", "duty"),
                        "The senior watchkeeper is missing and the apprentice has incomplete authority.",
                        "The apprentice knows an older signal code that can bypass the official chain of command.",
                        "Cold wind rattles the signal frame above an understaffed watch."),
                role("wounded_courier", "Wounded Courier", "road courier", SocialPosition.OUTSIDER,
                        Set.of("deliver_message", "protect_dispatch"), Set.of(Relationship.IS_BEING_WATCHED, Relationship.DEPENDS_ON_SOMEONE),
                        Set.of(Condition.WOUNDED, Condition.EXHAUSTED), Set.of(Knowledge.ROUTES, Knowledge.SIGNALS),
                        Set.of(AlignmentPressure.TRUTH, AlignmentPressure.SELF_PRESERVATION), Set.of("message", "route", "escape", "truth"),
                        "A leg wound makes the obvious road impossible to traverse at full speed.",
                        "The sealed dispatch can be interpreted from its routing marks without breaking it.",
                        "A blood-marked satchel hangs from a courier who has already missed one relay."),
                role("caravan_scout", "Caravan Scout", "advance scout", SocialPosition.RETAINER,
                        Set.of("find_safe_route", "return_with_warning"), Set.of(Relationship.OWES_DUTY, Relationship.HAS_LOCAL_TIES),
                        Set.of(Condition.ISOLATED), Set.of(Knowledge.ROUTES, Knowledge.CREATURE_HABITS),
                        Set.of(AlignmentPressure.SURVIVAL_GROUP, AlignmentPressure.SELF_PRESERVATION), Set.of("scouting", "creature", "route", "warning"),
                        "The scout begins separated from the people depending on the report.",
                        "Old spoor reveals a route the caravan leaders have dismissed as unusable.",
                        "Distant bells fade behind a scout standing alone beyond the last marker."),
                role("cistern_keeper", "Cistern Keeper", "waterworks keeper", SocialPosition.LABOURER,
                        Set.of("keep_water_clean", "maintain_flow"), Set.of(Relationship.PROTECTS_SOMEONE, Relationship.HAS_LOCAL_TIES),
                        Set.of(Condition.UNDER_OBSERVATION), Set.of(Knowledge.WATERWORKS, Knowledge.ROUTES),
                        Set.of(AlignmentPressure.CIVILIANS, AlignmentPressure.AUTHORITY), Set.of("water", "sabotage", "civilians", "repair"),
                        "Officials suspect contamination and restrict access to the lower valves.",
                        "A maintenance bypass can reroute flow without opening the guarded main gate.",
                        "Stone channels hum beneath a keeper carrying too few keys."),
                role("healers_aide", "Healer's Aide", "field healer's aide", SocialPosition.AUXILIARY,
                        Set.of("triage_wounded", "protect_supplies"), Set.of(Relationship.PROTECTS_SOMEONE, Relationship.DEPENDS_ON_SOMEONE),
                        Set.of(Condition.HUNGRY, Condition.EXHAUSTED), Set.of(Knowledge.MEDICINE, Knowledge.CREATURE_HABITS),
                        Set.of(AlignmentPressure.CIVILIANS, AlignmentPressure.SURVIVAL_GROUP), Set.of("healing", "rescue", "supplies", "sacrifice"),
                        "There are fewer remedies than patients and no safe place to treat everyone.",
                        "The aide recognizes that one dangerous plant can substitute for a missing antiseptic.",
                        "A torn medical roll lies open beside more wounded than it can serve."),
                role("quarry_runner", "Quarry Runner", "quarry message-runner", SocialPosition.LABOURER,
                        Set.of("carry_shift_orders", "mark_unsafe_tunnels"), Set.of(Relationship.RIVALS_SOMEONE, Relationship.HAS_LOCAL_TIES),
                        Set.of(Condition.DISTRUSTED), Set.of(Knowledge.ROUTES, Knowledge.SIGNALS),
                        Set.of(AlignmentPressure.FACTION, AlignmentPressure.SURVIVAL_GROUP), Set.of("collapse", "tunnel", "route", "labour"),
                        "Supervisors distrust the runner after an earlier false alarm.",
                        "The runner alone remembers an abandoned ventilation gallery connecting two worksites.",
                        "Dusty tally marks end where a fresh crack crosses the quarry wall."),
                role("border_levy", "Border Levy", "conscripted border guard", SocialPosition.AUXILIARY,
                        Set.of("hold_checkpoint", "identify_threat"), Set.of(Relationship.FEARS_AUTHORITY, Relationship.PROTECTS_SOMEONE),
                        Set.of(Condition.HUNGRY, Condition.UNDER_OBSERVATION), Set.of(Knowledge.ROUTES, Knowledge.LANGUAGES),
                        Set.of(AlignmentPressure.AUTHORITY, AlignmentPressure.FAMILY), Set.of("guard", "checkpoint", "choice", "authority"),
                        "The levy has orders to stop everyone, including people known personally.",
                        "A second dialect exposes forged travel papers the officers cannot read.",
                        "A borrowed spear rests against a gate crowded by people who cannot wait."),
                role("archive_novice", "Archive Novice", "record-keeper novice", SocialPosition.INITIATE,
                        Set.of("preserve_records", "obey_custodian"), Set.of(Relationship.OWES_DUTY, Relationship.KNOWS_A_SECRET),
                        Set.of(Condition.ISOLATED), Set.of(Knowledge.LEDGERS, Knowledge.RITUALS),
                        Set.of(AlignmentPressure.TRUTH, AlignmentPressure.AUTHORITY), Set.of("records", "secret", "history", "truth"),
                        "The archive is being evacuated and the novice is ordered to abandon restricted ledgers.",
                        "Cross-referenced dates reveal which official account has been altered.",
                        "Ink, smoke, and hurried footsteps gather around a locked cabinet."),
                role("hostage_interpreter", "Hostage Interpreter", "interpreter under guard", SocialPosition.BOUND,
                        Set.of("translate_demands", "avoid_escalation"), Set.of(Relationship.FEARS_AUTHORITY, Relationship.IS_BEING_WATCHED),
                        Set.of(Condition.RESTRAINED, Condition.DISTRUSTED), Set.of(Knowledge.LANGUAGES, Knowledge.ROUTES),
                        Set.of(AlignmentPressure.FACTION, AlignmentPressure.SELF_PRESERVATION), Set.of("negotiation", "language", "hostage", "deception"),
                        "Both sides assume the interpreter is loyal to the other.",
                        "A deliberate ambiguity in one dialect can create time for another plan.",
                        "Every sentence is repeated twice while two armed groups watch for betrayal."),
                role("pilgrim_guide", "Pilgrim Guide", "local pilgrimage guide", SocialPosition.OUTSIDER,
                        Set.of("guide_group", "respect_taboo"), Set.of(Relationship.HAS_LOCAL_TIES, Relationship.PROTECTS_SOMEONE),
                        Set.of(Condition.UNDER_OBSERVATION), Set.of(Knowledge.ROUTES, Knowledge.RITUALS),
                        Set.of(AlignmentPressure.FAITH, AlignmentPressure.CIVILIANS), Set.of("ritual", "route", "faith", "shelter"),
                        "The safest physical path violates a local prohibition the group expects the guide to uphold.",
                        "A forgotten service path reaches the shrine without crossing the exposed ridge.",
                        "Prayer ribbons snap in a wind that has erased the familiar trail."),
                role("temple_attendant", "Temple Attendant", "junior temple attendant", SocialPosition.INITIATE,
                        Set.of("maintain_rite", "guard_reliquary"), Set.of(Relationship.OWES_DUTY, Relationship.KNOWS_A_SECRET),
                        Set.of(Condition.DISTRUSTED), Set.of(Knowledge.RITUALS, Knowledge.LEDGERS),
                        Set.of(AlignmentPressure.FAITH, AlignmentPressure.TRUTH), Set.of("ritual", "secret", "relic", "duty"),
                        "The attendant's superior has given an order that contradicts a written rite.",
                        "Marginal notes reveal the ritual has a safe interruption point.",
                        "Lamp smoke curls around a reliquary whose seal has already been disturbed."),
                role("ferry_deckhand", "Ferry Deckhand", "river ferry deckhand", SocialPosition.DEPENDENT,
                        Set.of("keep_passengers_safe", "keep_ferry_moving"), Set.of(Relationship.DEPENDS_ON_SOMEONE, Relationship.PROTECTS_SOMEONE),
                        Set.of(Condition.EXHAUSTED), Set.of(Knowledge.WATERWORKS, Knowledge.CREATURE_HABITS),
                        Set.of(AlignmentPressure.CIVILIANS, AlignmentPressure.SELF_PRESERVATION), Set.of("crossing", "water", "rescue", "creature"),
                        "The ferrymaster is incapacitated while the crossing is already underway.",
                        "The deckhand knows where the current can beach the vessel without using the main landing.",
                        "A half-loaded ferry turns broadside as something moves beneath the water." )
        );
        validateUniqueIds(roles);
        return roles;
    }

    /**
     * Deterministic DESIGN matcher. Evidence changes relative authored weights, then
     * a stable seed chooses among weighted candidates. This is not a canonical Spell formula.
     */
    public static RoleMatch match(long seed, Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        List<Map.Entry<String, Integer>> normalized = evidence.entrySet().stream()
                .map(entry -> Map.entry(stableId(entry.getKey()), entry.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        normalized.forEach(entry -> {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException("evidence values cannot be negative");
            }
        });

        List<WeightedRole> weighted = new ArrayList<>();
        int total = 0;
        for (RoleProfile role : waveOne().stream().sorted(Comparator.comparing(RoleProfile::id)).toList()) {
            int weight = 100;
            List<String> matched = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : normalized) {
                if (role.evidenceAffinities().contains(entry.getKey()) && entry.getValue() > 0) {
                    weight = Math.addExact(weight, Math.multiplyExact(entry.getValue(), 25));
                    matched.add(entry.getKey());
                }
            }
            total = Math.addExact(total, weight);
            weighted.add(new WeightedRole(role, weight, List.copyOf(matched)));
        }

        int pick = Math.floorMod(mix(seed, normalized), total);
        for (WeightedRole candidate : weighted) {
            if (pick < candidate.weight()) {
                return new RoleMatch(candidate.role(), seed, candidate.weight(), candidate.matchedEvidence());
            }
            pick -= candidate.weight();
        }
        throw new IllegalStateException("weighted role selection exhausted unexpectedly");
    }

    private record WeightedRole(RoleProfile role, int weight, List<String> matchedEvidence) {
    }

    private static RoleProfile role(String id, String displayName, String occupation, SocialPosition socialPosition,
                                    Set<String> obligations, Set<Relationship> relationships, Set<Condition> conditions,
                                    Set<Knowledge> knowledge, Set<AlignmentPressure> pressures, Set<String> evidenceAffinities,
                                    String startingConstraint, String hiddenOpportunity, String presentationCue) {
        return new RoleProfile(id, displayName, occupation, socialPosition, obligations, relationships, conditions,
                knowledge, pressures, evidenceAffinities, startingConstraint, hiddenOpportunity, presentationCue);
    }

    private static long mix(long seed, List<Map.Entry<String, Integer>> evidence) {
        long value = seed ^ 0x9E3779B97F4A7C15L;
        for (Map.Entry<String, Integer> entry : evidence) {
            value ^= entry.getKey().hashCode();
            value = Long.rotateLeft(value * 0xBF58476D1CE4E5B9L, 27);
            value ^= entry.getValue();
            value *= 0x94D049BB133111EBL;
        }
        return value ^ (value >>> 31);
    }

    private static void validateUniqueIds(List<RoleProfile> roles) {
        HashSet<String> ids = new HashSet<>();
        for (RoleProfile role : roles) {
            if (!ids.add(role.id())) {
                throw new IllegalArgumentException("Duplicate role id: " + role.id());
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
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
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
