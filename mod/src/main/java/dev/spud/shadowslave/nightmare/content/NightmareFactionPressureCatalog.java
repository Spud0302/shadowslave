package dev.spud.shadowslave.nightmare.content;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** DESIGN-only player-facing pressure primitives for already-resolved Java-owned Nightmare factions. */
public final class NightmareFactionPressureCatalog {
    public static final String GENERATOR_VERSION = "nightmare-faction-pressure-v1";

    private NightmareFactionPressureCatalog() {}

    public enum PressureFamily { DUTY, RESOURCE, SECRECY, TERRITORIAL, RESCUE, SURVIVAL }

    public record Primitive(String id, PressureFamily family, String title, String pressureRead,
                            String negotiationQuestion, List<String> playerLevers, Set<String> affinityTags,
                            List<String> presentationCues, String antiOverclaimBoundary) {
        public Primitive {
            id = stableId(id);
            family = Objects.requireNonNull(family, "family");
            title = text(title, "title");
            pressureRead = text(pressureRead, "pressureRead");
            negotiationQuestion = text(negotiationQuestion, "negotiationQuestion");
            playerLevers = exactTextList(playerLevers, 3, "playerLevers");
            affinityTags = nonEmptyTags(affinityTags);
            presentationCues = exactTextList(presentationCues, 2, "presentationCues");
            antiOverclaimBoundary = text(antiOverclaimBoundary, "antiOverclaimBoundary");
        }
    }

    public record Selection(String generatorVersion, long seed, String scenarioId, String factionId,
                            Set<PressureFamily> allowedFamilies, Primitive primitive, String presentationCue,
                            Set<String> matchedEvidenceTags) {
        public Selection {
            generatorVersion = text(generatorVersion, "generatorVersion");
            scenarioId = opaqueId(scenarioId, "scenarioId");
            factionId = opaqueId(factionId, "factionId");
            allowedFamilies = nonEmptyFamilies(allowedFamilies);
            primitive = Objects.requireNonNull(primitive, "primitive");
            if (!allowedFamilies.contains(primitive.family())) {
                throw new IllegalArgumentException("primitive family must be allowed by caller-owned faction context");
            }
            presentationCue = text(presentationCue, "presentationCue");
            matchedEvidenceTags = Set.copyOf(Objects.requireNonNull(matchedEvidenceTags, "matchedEvidenceTags"));
        }
    }

    public static List<Primitive> waveOne() {
        return List.of(
                p("duty_hold_watch", PressureFamily.DUTY, "Hold the Watch",
                        "The faction is under pressure to keep a watch, post, or warning duty staffed despite competing demands.",
                        "What can you offer that lets them meet the duty without treating it as automatically correct?",
                        "Offer to cover a bounded watch interval.", "Bring information that changes what must be watched.", "Propose a handoff that preserves the duty.",
                        Set.of("duty", "watch", "warning"),
                        "A marked post or watch route stays visible during the exchange.", "The duty is framed as an obligation, not a victory condition.",
                        "Duty pressure does not prove the order is wise, legitimate, truthful, or required for Nightmare completion."),
                p("duty_carry_order", PressureFamily.DUTY, "Carry the Order",
                        "The faction is trying to deliver an instruction, warning, or summons before circumstances make delivery harder.",
                        "Can the message be delivered, redirected, or questioned without the catalogue deciding its authority?",
                        "Carry the message to a named destination.", "Ask who issued it and what remains unknown.", "Offer another route or messenger.",
                        Set.of("duty", "message", "authority"),
                        "The sender, destination, and unresolved authority are shown separately.", "A sealed-message motif emphasizes delivery rather than truth.",
                        "Presentation cannot make an order lawful, authentic, binding, or accepted by the ResolutionGraph."),
                p("duty_keep_oath", PressureFamily.DUTY, "Keep the Promise",
                        "The faction is constrained by a prior promise, compact, or stated obligation that now competes with immediate advantage.",
                        "What part of the obligation can be honored, renegotiated, or left unresolved?",
                        "Ask what was actually promised.", "Offer terms that preserve the narrow obligation.", "Name the cost of keeping it unchanged.",
                        Set.of("duty", "obligation", "bargain"),
                        "The stated promise is shown beside its present cost.", "No moral approval marker is attached to compliance or refusal.",
                        "The catalogue does not establish whether an oath is magically binding, morally correct, enforceable, or appraisal-relevant."),
                p("duty_protect_charge", PressureFamily.DUTY, "Protect the Charge",
                        "The faction is responsible for a person, object, place, or task it has been told or chosen to protect.",
                        "How can that responsibility be met or narrowed without inventing a canonical protection rule?",
                        "Ask what must actually be protected.", "Offer a safer transfer or temporary shelter.", "Separate the charge from a wider faction objective.",
                        Set.of("duty", "protection", "obligation"),
                        "The protected charge is highlighted without implying invulnerability.", "Competing responsibilities remain visible around it.",
                        "Protection pressure cannot determine ownership, innocence, survival, allegiance, or scenario success."),

                p("resource_ration_stores", PressureFamily.RESOURCE, "Ration the Last Stores",
                        "The faction has limited supplies and must decide what to spend now, reserve, trade, or move.",
                        "What bounded exchange changes access to the stores without the catalogue deciding scarcity math?",
                        "Offer a substitute resource.", "Ask what the stores are reserved for.", "Trade information or labor for a bounded share.",
                        Set.of("resource", "supply", "ration"),
                        "Visible stock is described qualitatively rather than as a canonical shortage percentage.", "Reserved and immediately usable supplies are presented separately.",
                        "Resource pressure does not calculate inventory truth, scarcity probability, fair allocation, or appraisal value."),
                p("resource_hold_water", PressureFamily.RESOURCE, "Hold the Water Source",
                        "The faction depends on access to a well, spring, cistern, or other bounded source that others may also need.",
                        "Can access be shared, traded, guarded, or investigated without declaring who owns the source?",
                        "Negotiate a time-bounded access window.", "Offer help checking an alternate source.", "Ask what threat or shortage caused the restriction.",
                        Set.of("resource", "water", "access"),
                        "The source and access boundary are emphasized rather than a faction ownership badge.", "Competing needs are shown without a moral ranking.",
                        "The catalogue cannot establish ownership, purity, future supply, territorial rights, or successful access."),
                p("resource_protect_tools", PressureFamily.RESOURCE, "Protect the Tools",
                        "A small set of tools, components, mounts, or specialist equipment is carrying more value than the faction can easily replace.",
                        "What can the player offer that reduces the pressure without making the equipment a scripted objective?",
                        "Offer repair materials or replacement labor.", "Ask which tool is actually indispensable.", "Propose moving the equipment before the next risk.",
                        Set.of("resource", "tool", "repair"),
                        "Critical equipment is foregrounded while replacement uncertainty remains explicit.", "Wear and damage are cues, not authoritative durability state.",
                        "A tool pressure primitive cannot damage, repair, consume, duplicate, or assign canonical value to an item."),
                p("resource_contest_shelter", PressureFamily.RESOURCE, "Contest the Shelter",
                        "The faction wants use of a defensible or survivable shelter whose capacity or access is under pressure.",
                        "Can space, timing, or responsibility be negotiated without making shelter control a universal win condition?",
                        "Offer a shared occupancy arrangement.", "Ask what makes the shelter necessary now.", "Propose another defensible location to inspect.",
                        Set.of("resource", "shelter", "access"),
                        "Entrances and capacity constraints are presented as local facts supplied by the caller.", "The shelter is not styled as a conquest objective.",
                        "Presentation cannot establish capacity, safety, ownership, exclusion rights, or terminal scenario control."),

                p("secrecy_hide_route", PressureFamily.SECRECY, "Keep the Route Hidden",
                        "The faction believes disclosure of a route, refuge, or approach would expose something it values.",
                        "What information can be exchanged without presentation deciding whether secrecy is justified?",
                        "Ask for a partial route that protects the hidden destination.", "Offer another way to verify your purpose.", "Agree to a bounded escort instead of receiving the route.",
                        Set.of("secrecy", "route", "access"),
                        "Only the disclosed segment is rendered as known.", "The hidden portion remains absent rather than replaced by a speculative map.",
                        "Secrecy pressure does not prove the hidden place exists, is safe, belongs to the faction, or must stay secret."),
                p("secrecy_protect_witness", PressureFamily.SECRECY, "Protect a Witness",
                        "The faction is withholding a person's identity or location because disclosure is believed to carry risk.",
                        "Can the player gain useful testimony or access while leaving identity protection intact?",
                        "Ask for an anonymized account.", "Offer a protected meeting condition.", "Seek corroboration that does not expose the witness.",
                        Set.of("secrecy", "witness", "testimony"),
                        "The account can appear without revealing a hidden identity.", "Risk is described as a faction concern, not an adjudicated forecast.",
                        "The catalogue cannot prove witness truthfulness, danger, innocence, guilt, identity, or future safety."),
                p("secrecy_conceal_failure", PressureFamily.SECRECY, "Conceal the Failure",
                        "The faction is under pressure to keep a mistake, loss, breach, or failed obligation from becoming widely known.",
                        "What does the player need to know or verify before accepting the faction's preferred silence?",
                        "Ask what concrete consequence disclosure would cause.", "Separate the failure from unrelated accusations.", "Offer a correction that does not require public blame.",
                        Set.of("secrecy", "failure", "reputation"),
                        "Known facts and withheld details are visually separated.", "No guilt marker is attached to the faction portrait.",
                        "Concealment pressure does not establish deception, culpability, reputation loss, or the truth of an accusation."),
                p("secrecy_withhold_detail", PressureFamily.SECRECY, "Withhold a Dangerous Detail",
                        "The faction believes one piece of information would become harmful if repeated too broadly or too soon.",
                        "Can a narrower fact or condition satisfy the player's need without forcing full disclosure?",
                        "Ask for the decision-relevant fragment only.", "Offer to verify the claim through another source.", "Accept a temporary boundary and record the missing detail.",
                        Set.of("secrecy", "information", "uncertainty"),
                        "The missing detail is explicitly marked rather than silently inferred.", "A bounded disclosure cue avoids theatrical proof of danger.",
                        "Withholding does not prove that information is dangerous, true, false, magical, or scenario-critical."),

                p("territorial_hold_crossing", PressureFamily.TERRITORIAL, "Hold the Crossing",
                        "The faction wants to control passage through a bridge, gate, ford, tunnel, or other chokepoint for its present purposes.",
                        "What terms could change passage without the catalogue deciding territorial ownership or military success?",
                        "Negotiate passage for a bounded group or time.", "Offer information about another approach.", "Ask what event would make the crossing negotiable.",
                        Set.of("territory", "crossing", "access"),
                        "The chokepoint and current restriction are visible without a permanent ownership flag.", "Possible terms are presented beside the restriction.",
                        "The catalogue cannot grant passage, establish ownership, resolve combat, or accept a crossing event for the scenario."),
                p("territorial_defend_boundary", PressureFamily.TERRITORIAL, "Defend the Boundary",
                        "The faction is treating a local boundary as something that must be watched, defended, or respected under current conditions.",
                        "Can the boundary be clarified or negotiated without treating it as a canonical border?",
                        "Ask where the claimed boundary actually lies.", "Offer a neutral meeting point.", "Ask what crossing would change for the faction.",
                        Set.of("territory", "boundary", "warning"),
                        "Claimed and physically observed boundaries are visually distinct.", "No map coloring implies legal or historical ownership.",
                        "A territorial claim is not proof of sovereignty, legitimacy, historical right, hostility, or future control."),
                p("territorial_keep_high_ground", PressureFamily.TERRITORIAL, "Keep the Vantage",
                        "The faction values a ridge, tower, wall, or other position because it currently supports observation, signaling, or defense.",
                        "What alternative preserves the function they care about if occupation itself is negotiable?",
                        "Offer another observation point.", "Ask whether signaling or defense is the real concern.", "Propose shared use under a bounded condition.",
                        Set.of("territory", "vantage", "watch"),
                        "The position's function is emphasized over ownership.", "Sightlines are presentation cues, not authoritative detection mechanics.",
                        "The catalogue cannot establish tactical superiority, detection, ownership, or combat outcome."),
                p("territorial_keep_refuge", PressureFamily.TERRITORIAL, "Keep the Refuge",
                        "The faction is resisting access to or displacement from a place it currently uses as refuge.",
                        "Can the player's need be met without presentation deciding whose claim is rightful?",
                        "Ask for temporary entry under stated conditions.", "Offer another refuge to inspect.", "Separate immediate shelter needs from long-term possession.",
                        Set.of("territory", "refuge", "shelter"),
                        "Immediate refuge needs and long-term claims are shown separately.", "Occupancy is not rendered as permanent control.",
                        "Refuge pressure cannot establish ownership, innocence, safe capacity, eviction rights, or scenario victory."),

                p("rescue_recover_missing", PressureFamily.RESCUE, "Recover the Missing",
                        "The faction wants to locate or recover people whose current condition or location is not fully known.",
                        "What bounded help can the player offer without the catalogue deciding whether the missing can be saved?",
                        "Ask for the last verified location.", "Offer to check one route or landmark.", "Seek another witness before committing the group.",
                        Set.of("rescue", "missing", "route"),
                        "Last-known information is separated from speculation.", "Missing people are not marked alive, dead, captive, or safe without Java-owned state.",
                        "Rescue pressure does not determine survival, location, captor, success probability, or accepted scenario events."),
                p("rescue_open_passage", PressureFamily.RESCUE, "Open a Passage",
                        "The faction needs a route, door, bridge, or corridor made usable long enough to move people away from immediate pressure.",
                        "What can be checked or negotiated before anyone treats the passage as usable?",
                        "Inspect one obstruction or access condition.", "Seek help from whoever controls the approach.", "Offer a staged crossing instead of a full commitment.",
                        Set.of("rescue", "crossing", "access"),
                        "The intended movement is shown as a plan rather than completed state.", "Unverified hazards remain visibly unresolved.",
                        "The catalogue cannot open terrain, guarantee route safety, move NPCs, or complete a rescue."),
                p("rescue_trade_time", PressureFamily.RESCUE, "Trade Time for Lives",
                        "The faction is weighing delay, diversion, or another costly holding action so others can move or prepare.",
                        "What part of that tradeoff can be clarified without assigning a canonical sacrifice formula?",
                        "Ask who gains time and for what action.", "Offer a different delaying action.", "Set a bounded withdrawal condition before agreeing.",
                        Set.of("rescue", "delay", "sacrifice"),
                        "The intended beneficiaries and cost are shown without a heroism score.", "Withdrawal conditions remain visible beside the plan.",
                        "The catalogue does not calculate required sacrifice, moral worth, survival odds, or appraisal credit."),
                p("rescue_extract_injured", PressureFamily.RESCUE, "Move the Injured",
                        "The faction wants to move wounded, exhausted, trapped, or otherwise vulnerable people before conditions worsen.",
                        "What support or route can be arranged without the catalogue deciding medical state or success?",
                        "Offer transport or escort for a bounded group.", "Ask which route is currently supported by evidence.", "Seek a safer staging point first.",
                        Set.of("rescue", "injured", "shelter"),
                        "Vulnerability is described without diagnostic claims.", "Movement remains pending until authoritative game state accepts it.",
                        "This primitive cannot diagnose, heal, move, save, or assign survival chances to any actor."),

                p("survival_leave_before_collapse", PressureFamily.SURVIVAL, "Leave Before Conditions Worsen",
                        "The faction wants to withdraw before an environmental, structural, military, or social situation becomes harder to survive.",
                        "What evidence or threshold would justify leaving now without presentation forecasting the future?",
                        "Ask which present condition triggered the withdrawal plan.", "Offer a safer destination to verify.", "Record what would make the group stop or turn back.",
                        Set.of("survival", "withdrawal", "environment"),
                        "Present hazards and future fears are visually separated.", "The destination is shown as proposed until Java-owned state verifies it.",
                        "Survival pressure does not forecast collapse, attack, weather, route safety, or whether withdrawal is correct."),
                p("survival_split_group", PressureFamily.SURVIVAL, "Split the Burden",
                        "The faction is considering dividing people, supplies, duties, or routes because one group cannot comfortably carry every concern at once.",
                        "What division can be made legible without the catalogue deciding who should survive or succeed?",
                        "Ask what each group would be responsible for.", "Offer a rendezvous condition.", "Keep one critical resource or witness from being silently duplicated.",
                        Set.of("survival", "group", "resource"),
                        "Proposed groups and responsibilities are shown as a plan.", "Shared resources stay explicitly shared or assigned by caller-owned state.",
                        "The catalogue cannot split authoritative parties, duplicate resources, assign survival odds, or determine the optimal plan."),
                p("survival_bargain_passage", PressureFamily.SURVIVAL, "Bargain for Passage",
                        "The faction believes continued movement depends on terms with another actor, group, or gatekeeper.",
                        "What can be offered or verified without the catalogue enforcing a bargain?",
                        "Offer a bounded trade or service.", "Ask for the exact condition of passage.", "Seek another route while keeping negotiations open.",
                        Set.of("survival", "bargain", "crossing"),
                        "Terms are shown as proposed until authoritative state accepts them.", "Alternate routes remain visibly unresolved.",
                        "A displayed bargain does not bind either faction, spend resources, grant passage, or alter allegiance."),
                p("survival_preserve_remnant", PressureFamily.SURVIVAL, "Preserve What Remains",
                        "The faction is concentrating on keeping a reduced group, store, archive, refuge, or capability intact after losses or pressure.",
                        "What must actually be preserved, and what can be surrendered or deferred?",
                        "Ask the faction to name the minimum they are protecting.", "Offer to preserve information instead of a position.", "Separate immediate survival from later recovery.",
                        Set.of("survival", "preservation", "loss"),
                        "The retained core concern is foregrounded without implying total defeat.", "Losses are not expanded beyond facts supplied by Java-owned state.",
                        "Preservation pressure cannot establish casualty counts, permanent loss, faction defeat, moral priority, or Nightmare resolution.")
        );
    }

    public static Selection compose(long seed, String scenarioId, String factionId,
                                    Set<PressureFamily> allowedFamilies, Map<String, Integer> evidence) {
        String scenario = opaqueId(scenarioId, "scenarioId");
        String faction = opaqueId(factionId, "factionId");
        Set<PressureFamily> allowed = nonEmptyFamilies(allowedFamilies);
        Set<String> positive = positiveEvidenceTags(evidence);
        List<Primitive> eligible = waveOne().stream().filter(p -> allowed.contains(p.family())).toList();
        List<Primitive> preferred = eligible.stream()
                .filter(p -> p.affinityTags().stream().anyMatch(positive::contains)).toList();
        List<Primitive> pool = preferred.isEmpty() ? eligible : preferred;
        String familyKey = allowed.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        Primitive primitive = pool.get(index(seed, scenario, faction, familyKey, "primitive", pool.size()));
        String cue = primitive.presentationCues().get(index(seed, scenario, faction, primitive.id(), "cue", 2));
        Set<String> matched = primitive.affinityTags().stream().filter(positive::contains)
                .collect(Collectors.toUnmodifiableSet());
        return new Selection(GENERATOR_VERSION, seed, scenario, faction, allowed, primitive, cue, matched);
    }

    public static Primitive byId(String id) {
        String stable = stableId(id);
        return waveOne().stream().filter(p -> p.id().equals(stable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Nightmare faction pressure primitive: " + stable));
    }

    private static Primitive p(String id, PressureFamily family, String title, String read, String question,
                               String a, String b, String c, Set<String> tags, String cue1, String cue2, String boundary) {
        return new Primitive(id, family, title, read, question, List.of(a, b, c), tags, List.of(cue1, cue2), boundary);
    }

    private static Set<PressureFamily> nonEmptyFamilies(Set<PressureFamily> families) {
        Objects.requireNonNull(families, "allowedFamilies");
        if (families.isEmpty()) throw new IllegalArgumentException("allowedFamilies must not be empty");
        if (families.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("allowedFamilies must not contain null");
        return Set.copyOf(families);
    }

    private static Set<String> positiveEvidenceTags(Map<String, Integer> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        return evidence.entrySet().stream().map(entry -> {
            String key = stableId(entry.getKey());
            Integer value = Objects.requireNonNull(entry.getValue(), "evidence value");
            if (value < 0) throw new IllegalArgumentException("evidence values must not be negative");
            return Map.entry(key, value);
        }).filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static int index(long seed, String a, String b, String c, String discriminator, int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        String material = GENERATOR_VERSION + "|" + seed + "|" + a + "|" + b + "|" + c + "|" + discriminator;
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
        long value = 0L;
        for (int i = 0; i < 8; i++) value = (value << 8) | Byte.toUnsignedLong(digest[i]);
        return (int) Long.remainderUnsigned(value, bound);
    }

    private static String opaqueId(String value, String field) {
        return text(value, field);
    }

    private static String stableId(String value) {
        String normalized = text(value, "id").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_-]*")) throw new IllegalArgumentException("invalid stable id: " + value);
        return normalized;
    }

    private static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return trimmed;
    }

    private static List<String> exactTextList(List<String> values, int size, String field) {
        Objects.requireNonNull(values, field);
        if (values.size() != size) throw new IllegalArgumentException(field + " must contain exactly " + size + " entries");
        return List.copyOf(values.stream().map(value -> text(value, field)).toList());
    }

    private static Set<String> nonEmptyTags(Set<String> tags) {
        Objects.requireNonNull(tags, "affinityTags");
        if (tags.isEmpty()) throw new IllegalArgumentException("affinityTags must not be empty");
        return tags.stream().map(NightmareFactionPressureCatalog::stableId)
                .sorted(Comparator.naturalOrder()).collect(Collectors.toUnmodifiableSet());
    }
}