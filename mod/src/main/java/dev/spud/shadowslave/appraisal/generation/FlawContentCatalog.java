package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Player-facing content for the Flaw identities already authored by
 * {@link ExpandedIdentityContentCatalog}.
 *
 * <p>Everything in this catalogue is Minecraft DESIGN. Canon establishes that
 * Flaws can impose highly personal conditions and that the Nightmare Spell can
 * reveal them, but canon does not provide a universal Flaw-generation formula,
 * mechanical taxonomy, trigger model, or coping system. This class therefore
 * never selects which Flaw a player owns. It only describes an already-resolved
 * Java-owned Flaw identity.</p>
 */
public final class FlawContentCatalog {
    private static final String NAMESPACE = "shadowslave";
    private static final String GENERATOR_VERSION = "flaw-presentation-v1";

    private static final List<FlawProfile> WAVE_ONE = canonicalProfiles(List.of(
            profile(
                    "bell_without_silence",
                    FlawFamily.SENSORY,
                    "Silence never fully arrives; faint meaningful sounds keep pressing at the edge of attention.",
                    "Quiet surroundings or attempts to rest without other sound.",
                    "Persistent phantom chimes make concentration and sleep harder until the bearer grounds themself in a real sound.",
                    List.of(
                            "Create a deliberate rhythm to separate real sound from the lingering chime.",
                            "Move somewhere with steady ambient noise before attempting careful work."
                    ),
                    Set.of("sound", "warning", "rest", "attention")
            ),
            profile(
                    "borrowed_breath",
                    FlawFamily.RESOURCE,
                    "Concealment is paid for with breath rather than offered freely.",
                    "Remaining hidden, muffled, or supernaturally indistinct for too long.",
                    "Breath shortens in stages, forcing the bearer to surface, speak, or otherwise break concealment before exhaustion sets in.",
                    List.of(
                            "Use concealment in short intervals instead of treating it as a permanent stance.",
                            "Plan a visible fallback route before spending the last safe breath."
                    ),
                    Set.of("mist", "concealment", "breath", "resource")
            ),
            profile(
                    "brittle_oath",
                    FlawFamily.COMPULSION,
                    "A promise made deliberately becomes difficult to abandon, even when circumstances change.",
                    "Giving an explicit promise while understanding what is being promised.",
                    "Breaking the promise builds mounting physical discomfort and disrupts precise actions until the obligation is honestly resolved or fulfilled.",
                    List.of(
                            "Avoid casual promises; state intentions and uncertainties precisely.",
                            "Negotiate an honest release from an impossible promise instead of silently abandoning it."
                    ),
                    Set.of("glass", "oath", "precision", "compulsion")
            ),
            profile(
                    "burden_of_the_last",
                    FlawFamily.RESOURCE,
                    "Being the final active protector of a group turns responsibility into a literal drain.",
                    "Remaining as the last capable defender while allies are incapacitated, absent, or retreating.",
                    "Sustained solitary responsibility steadily increases fatigue and makes recovery slower until another capable ally shares the burden.",
                    List.of(
                            "Preserve at least one ally's ability to assist rather than spending everyone at once.",
                            "Create a defensible pause where another person can explicitly take over part of the watch."
                    ),
                    Set.of("sacrifice", "aftermath", "duty", "fatigue")
            ),
            profile(
                    "cold_ash",
                    FlawFamily.ENVIRONMENTAL,
                    "Cold water and prolonged soaking smother the bearer's supernatural warmth.",
                    "Extended contact with cold water, heavy rain, or deep damp.",
                    "Warmth-based effects weaken and the bearer becomes progressively sluggish until they dry out and rekindle heat.",
                    List.of(
                            "Carry a dry ignition source and treat deep water as a planned hazard.",
                            "Break long wet crossings into places where heat can be restored."
                    ),
                    Set.of("ember", "water", "environment", "warmth")
            ),
            profile(
                    "echoing_pain",
                    FlawFamily.SENSORY,
                    "A severe injury does not end when the wound stops hurting; part of the pain returns in delayed echoes.",
                    "Receiving a sharp spike of physical pain.",
                    "Several weaker aftershocks recur unpredictably for a short period, distracting movement and timing without recreating the original injury.",
                    List.of(
                            "Do not assume a quiet second means the pain cycle has ended.",
                            "Use simple, committed actions while an aftershock window is still active."
                    ),
                    Set.of("resonance", "pain", "timing", "sensory")
            ),
            profile(
                    "empty_seat",
                    FlawFamily.ATTACHMENT,
                    "Leaving a trusted companion behind creates a persistent sense of absence that is hard to ignore.",
                    "Separating from someone the bearer has recently relied upon under pressure.",
                    "Focus drifts toward the missing companion and solitary rest becomes less effective until the separation is resolved or consciously accepted.",
                    List.of(
                            "Set a clear reunion point before splitting up.",
                            "If reunion is impossible, mark the separation deliberately instead of leaving it unresolved."
                    ),
                    Set.of("absence", "attachment", "witness", "rest")
            ),
            profile(
                    "glass_heart",
                    FlawFamily.EMOTIONAL,
                    "Strong emotion makes careful self-control fragile rather than impossible.",
                    "A sudden surge of fear, grief, anger, or joy during a precision task.",
                    "Fine control degrades until the bearer slows down enough to regain composure; forcing speed makes mistakes more likely.",
                    List.of(
                            "Pause before precision work when emotions spike.",
                            "Choose robust actions over delicate ones until composure returns."
                    ),
                    Set.of("reflection", "emotion", "precision", "control")
            ),
            profile(
                    "narrow_path",
                    FlawFamily.COMPULSION,
                    "Once the bearer publicly names a route or plan, changing course becomes increasingly difficult.",
                    "Declaring a specific path, destination, or course of action as the chosen way forward.",
                    "Departing from the declared path creates mounting hesitation and disorientation until the bearer openly acknowledges the change.",
                    List.of(
                            "Describe uncertain routes as options instead of commitments.",
                            "When new information matters, explicitly name the new course before taking it."
                    ),
                    Set.of("path", "guidance", "choice", "compulsion")
            ),
            profile(
                    "open_flame",
                    FlawFamily.SOCIAL,
                    "Direct questions about immediate danger are difficult to evade without visibly betraying the evasion.",
                    "Being directly asked about a danger the bearer currently believes is relevant to the questioner.",
                    "Attempts to conceal the danger produce obvious tells and mounting discomfort; careful truthful framing remains possible.",
                    List.of(
                            "Warn allies before entering situations where secrecy and safety will conflict.",
                            "Answer narrowly and truthfully rather than inventing a false reassurance."
                    ),
                    Set.of("warning", "disclosure", "social", "truth")
            ),
            profile(
                    "rooted_step",
                    FlawFamily.PHYSICAL,
                    "Holding ground becomes easier than leaving it.",
                    "Remaining nearly stationary while resisting danger for a sustained period.",
                    "The bearer gains a growing sense of physical heaviness when trying to disengage suddenly, making abrupt retreats slower than planned.",
                    List.of(
                            "Reposition before committing to a prolonged hold.",
                            "Withdraw in stages instead of waiting until an instant retreat is necessary."
                    ),
                    Set.of("duty", "endurance", "movement", "physical")
            ),
            profile(
                    "stone_sleep",
                    FlawFamily.PHYSICAL,
                    "Deep rest is genuinely deep; waking quickly carries a cost.",
                    "Being forced awake soon after entering restorative sleep.",
                    "The bearer suffers heavy limbs and dulled reactions for a short period after abrupt waking.",
                    List.of(
                            "Use watches and alarms that allow a gradual wake-up when possible.",
                            "Keep simple defensive options within reach before sleeping."
                    ),
                    Set.of("stone", "sleep", "sluggishness", "physical")
            ),
            profile(
                    "thorned_mercy",
                    FlawFamily.EMOTIONAL,
                    "Choosing mercy while expecting retaliation leaves a painful supernatural sting.",
                    "Deliberately sparing someone the bearer sincerely expects will remain a threat.",
                    "A brief wave of pain follows the decision, punishing neither mercy nor violence universally but making dangerous mercy costly.",
                    List.of(
                            "Create restraints, distance, or conditions that reduce the expected retaliation before sparing an enemy.",
                            "Do not mistake the pain for proof that mercy was the wrong decision."
                    ),
                    Set.of("growth", "retaliation", "mercy", "pain")
            ),
            profile(
                    "tidal_debt",
                    FlawFamily.RESOURCE,
                    "Borrowed bursts of effort create a predictable period of weakness afterward.",
                    "Pushing beyond the bearer's ordinary sustainable pace for a decisive burst.",
                    "The excess effort returns as a temporary trough in stamina; repeated borrowing deepens the next trough instead of erasing the debt.",
                    List.of(
                            "Spend bursts where a recovery window actually exists afterward.",
                            "Keep one reserve plan that does not depend on another immediate burst."
                    ),
                    Set.of("water", "rhythm", "stamina", "cyclical")
            ),
            profile(
                    "unanswered_call",
                    FlawFamily.COMPULSION,
                    "A sincere request for aid is difficult to ignore once clearly heard.",
                    "Recognizing that a nearby person has deliberately asked the bearer for help.",
                    "Walking away without responding creates mounting unease and loss of focus until the bearer answers, refuses openly, or attempts meaningful aid.",
                    List.of(
                            "Answer impossible requests explicitly instead of pretending not to hear them.",
                            "Define what aid can actually be given before committing to more."
                    ),
                    Set.of("signal", "duty", "aid", "compulsion")
            ),
            profile(
                    "uncut_thread",
                    FlawFamily.ATTACHMENT,
                    "Meaningful bonds linger after separation and can pull attention across distance.",
                    "Parting from someone after a shared crisis, promise, or strong mutual dependence.",
                    "Memories and concern surface at inconvenient moments, making complete emotional detachment difficult until the relationship changes honestly.",
                    List.of(
                            "Leave relationships in a named state rather than relying on ambiguity.",
                            "Use the recurring concern as information, but verify danger instead of assuming it."
                    ),
                    Set.of("thread", "connection", "attachment", "long_horizon")
            ),
            profile(
                    "witness_burden",
                    FlawFamily.SOCIAL,
                    "Knowing a consequential truth creates pressure to make sure it is not lost with the bearer.",
                    "Personally witnessing information the bearer believes could materially affect another person's survival or major decision.",
                    "Keeping the information completely unrecorded and unshared creates mounting mental pressure until it is preserved or responsibly communicated.",
                    List.of(
                            "Record dangerous knowledge in a secure form when immediate disclosure would be reckless.",
                            "Choose a trustworthy recipient before entering situations likely to produce important evidence."
                    ),
                    Set.of("perception", "witness", "disclosure", "psychological")
            )
    ));

    private static final Map<ResourceLocation, FlawProfile> BY_ID = index(WAVE_ONE);

    private FlawContentCatalog() {
    }

    public static List<FlawProfile> waveOne() {
        return WAVE_ONE;
    }

    public static FlawProfile require(ResourceLocation flawId) {
        FlawProfile profile = BY_ID.get(Objects.requireNonNull(flawId, "flawId"));
        if (profile == null) {
            throw new IllegalArgumentException("Unknown authored Flaw content: " + flawId);
        }
        return profile;
    }

    /**
     * Builds immutable presentation for an already-resolved Flaw. The seed only
     * varies which authored coping hook is surfaced first; it never chooses or
     * changes the Flaw identity itself.
     */
    public static FlawPresentation compose(long seed, ResourceLocation flawId) {
        FlawProfile profile = require(flawId);
        int index = Math.floorMod(mix(seed, flawId), profile.copingHooks().size());
        return new FlawPresentation(
                profile.flawId(),
                profile.formalName(),
                profile.family(),
                profile.playerSummary(),
                profile.trigger(),
                profile.consequence(),
                profile.copingHooks().get(index),
                profile.presentationTags(),
                Classification.DESIGN,
                GENERATOR_VERSION,
                seed
        );
    }

    private static FlawProfile profile(
            String path,
            FlawFamily family,
            String playerSummary,
            String trigger,
            String consequence,
            List<String> copingHooks,
            Set<String> presentationTags
    ) {
        ResourceLocation flawId = id("generation/flaw/" + path);
        String formalName = ExpandedIdentityContentCatalog.waveOne().flaws().stream()
                .filter(flaw -> flaw.id().equals(flawId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No generated Flaw primitive for " + flawId))
                .formalName();
        return new FlawProfile(
                flawId,
                formalName,
                family,
                playerSummary,
                trigger,
                consequence,
                copingHooks,
                presentationTags,
                Classification.DESIGN
        );
    }

    private static List<FlawProfile> canonicalProfiles(List<FlawProfile> source) {
        ArrayList<FlawProfile> copy = new ArrayList<>(Objects.requireNonNull(source, "source"));
        copy.sort(Comparator.comparing(profile -> profile.flawId().toString()));

        Set<ResourceLocation> generatedIds = new HashSet<>();
        for (IdentityPrimitiveCatalog.Flaw flaw : ExpandedIdentityContentCatalog.waveOne().flaws()) {
            generatedIds.add(flaw.id());
        }

        HashSet<ResourceLocation> seen = new HashSet<>();
        for (FlawProfile profile : copy) {
            Objects.requireNonNull(profile, "profile");
            if (!seen.add(profile.flawId())) {
                throw new IllegalArgumentException("Duplicate Flaw content id: " + profile.flawId());
            }
            if (!generatedIds.contains(profile.flawId())) {
                throw new IllegalArgumentException("Flaw content does not map to generated identity: " + profile.flawId());
            }
        }
        if (!seen.equals(generatedIds)) {
            HashSet<ResourceLocation> missing = new HashSet<>(generatedIds);
            missing.removeAll(seen);
            throw new IllegalArgumentException("Missing player-facing Flaw content for: " + missing);
        }
        return List.copyOf(copy);
    }

    private static Map<ResourceLocation, FlawProfile> index(List<FlawProfile> profiles) {
        HashMap<ResourceLocation, FlawProfile> indexed = new HashMap<>();
        for (FlawProfile profile : profiles) {
            indexed.put(profile.flawId(), profile);
        }
        return Map.copyOf(indexed);
    }

    private static int mix(long seed, ResourceLocation id) {
        long mixed = seed ^ (long) id.toString().hashCode() * 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        return (int) (mixed ^ (mixed >>> 32));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    private static List<String> textList(List<String> values, String name) {
        ArrayList<String> checked = new ArrayList<>();
        for (String value : Objects.requireNonNull(values, name)) {
            checked.add(text(value, name));
        }
        if (checked.size() < 2) {
            throw new IllegalArgumentException(name + " must contain at least two authored options");
        }
        return List.copyOf(checked);
    }

    private static Set<String> tags(Set<String> values) {
        HashSet<String> checked = new HashSet<>();
        for (String value : Objects.requireNonNull(values, "presentationTags")) {
            checked.add(text(value, "presentationTag").toLowerCase(Locale.ROOT).replace(' ', '_'));
        }
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("presentationTags cannot be empty");
        }
        return Set.copyOf(checked);
    }

    public enum FlawFamily {
        COMPULSION,
        SENSORY,
        PHYSICAL,
        RESOURCE,
        SOCIAL,
        EMOTIONAL,
        ENVIRONMENTAL,
        ATTACHMENT
    }

    public enum Classification {
        CANON,
        INFERRED,
        DESIGN,
        UNKNOWN,
        COMPATIBILITY
    }

    public record FlawProfile(
            ResourceLocation flawId,
            String formalName,
            FlawFamily family,
            String playerSummary,
            String trigger,
            String consequence,
            List<String> copingHooks,
            Set<String> presentationTags,
            Classification classification
    ) {
        public FlawProfile {
            flawId = Objects.requireNonNull(flawId, "flawId");
            formalName = text(formalName, "formalName");
            family = Objects.requireNonNull(family, "family");
            playerSummary = text(playerSummary, "playerSummary");
            trigger = text(trigger, "trigger");
            consequence = text(consequence, "consequence");
            copingHooks = textList(copingHooks, "copingHooks");
            presentationTags = tags(presentationTags);
            classification = Objects.requireNonNull(classification, "classification");
            if (classification != Classification.DESIGN) {
                throw new IllegalArgumentException("Authored Flaw profiles must remain explicit DESIGN");
            }
        }
    }

    public record FlawPresentation(
            ResourceLocation flawId,
            String formalName,
            FlawFamily family,
            String playerSummary,
            String trigger,
            String consequence,
            String copingHook,
            Set<String> presentationTags,
            Classification classification,
            String generatorVersion,
            long seed
    ) {
        public FlawPresentation {
            flawId = Objects.requireNonNull(flawId, "flawId");
            formalName = text(formalName, "formalName");
            family = Objects.requireNonNull(family, "family");
            playerSummary = text(playerSummary, "playerSummary");
            trigger = text(trigger, "trigger");
            consequence = text(consequence, "consequence");
            copingHook = text(copingHook, "copingHook");
            presentationTags = tags(presentationTags);
            classification = Objects.requireNonNull(classification, "classification");
            generatorVersion = text(generatorVersion, "generatorVersion");
        }
    }
}
