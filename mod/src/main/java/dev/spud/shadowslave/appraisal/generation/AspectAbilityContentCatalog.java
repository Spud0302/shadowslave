package dev.spud.shadowslave.appraisal.generation;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Player-facing descriptions for the already-authored procedural Aspect nature and
 * Dormant ability primitives in {@link ExpandedIdentityContentCatalog}.
 *
 * <p>All exact effects, categories, wording and gameplay hooks in this class are
 * Minecraft DESIGN. The class consumes already-resolved primitive identities and
 * never chooses, awards, rerolls, persists or executes an Aspect or ability.</p>
 */
public final class AspectAbilityContentCatalog {
    public static final String PROVENANCE = "DESIGN";
    public static final String PRESENTATION_VERSION = "aspect-ability-presentation-v1";
    private static final String NAMESPACE = "shadowslave";

    private final Map<ResourceLocation, NatureProfile> natures;
    private final Map<ResourceLocation, AbilityProfile> abilities;
    private final IdentityPrimitiveCatalog identityCatalog;

    private AspectAbilityContentCatalog(
            IdentityPrimitiveCatalog identityCatalog,
            List<NatureProfile> natures,
            List<AbilityProfile> abilities
    ) {
        this.identityCatalog = Objects.requireNonNull(identityCatalog, "identityCatalog");
        this.natures = indexed(natures, NatureProfile::id, "nature profile");
        this.abilities = indexed(abilities, AbilityProfile::id, "ability profile");
        validateCoverage();
    }

    public static AspectAbilityContentCatalog waveOne() {
        return new AspectAbilityContentCatalog(
                ExpandedIdentityContentCatalog.waveOne(),
                List.of(
                        nature("ash", "Endurance shaped by ruin, residue and what remains after loss.",
                                "Excels when the player must keep functioning after damage, depletion or collapse.",
                                Set.of("aftermath", "endurance", "survival")),
                        nature("bell", "Warning, sound and resonance made into a coherent supernatural theme.",
                                "Turns attention, signals and resonant traces into useful information or pressure.",
                                Set.of("resonance", "sound", "warning")),
                        nature("ember", "A small surviving light centered on preservation, resolve and controlled heat.",
                                "Rewards keeping something alive, lit, protected or recoverable under pressure.",
                                Set.of("light", "preservation", "resolve")),
                        nature("glass", "Reflection and brittle precision expressed through exact perception and retaliation.",
                                "Favors careful timing, reflected information and narrow high-precision openings.",
                                Set.of("precision", "reflection", "retaliation")),
                        nature("hollow", "Absence, quiet and concealment expressed as controlled reduction of presence.",
                                "Creates space to avoid notice, cross danger or act through what is missing rather than force.",
                                Set.of("absence", "concealment", "movement")),
                        nature("mist", "Obscurity and perception bound together in uncertain terrain and hidden movement.",
                                "Helps the player navigate, perceive or reposition where ordinary sight is unreliable.",
                                Set.of("concealment", "guidance", "perception")),
                        nature("road", "Paths, distance and direction expressed as supernatural movement and guidance.",
                                "Makes routes, destinations and escape lines meaningful parts of an encounter.",
                                Set.of("guidance", "movement", "path")),
                        nature("signal", "Warning, communication and distant perception concentrated into one theme.",
                                "Lets the player notice, carry or transmit consequential information across danger.",
                                Set.of("perception", "signal", "warning")),
                        nature("stone", "Stability, endurance and deliberate immovability shaped into defensive utility.",
                                "Excels at holding position, preserving footing and surviving pressure that would displace others.",
                                Set.of("endurance", "stability", "support")),
                        nature("thorn", "Growth and retaliation expressed through dangerous boundaries and living resistance.",
                                "Turns contact, pursuit or sustained pressure into opportunities for counteraction or protection.",
                                Set.of("growth", "preservation", "retaliation")),
                        nature("thread", "Connection and precision expressed through bonds, links and deliberate severance.",
                                "Makes relationships between targets, routes or effects into manipulable tactical information.",
                                Set.of("connection", "guidance", "precision")),
                        nature("tide", "Rhythm and adaptation expressed through cycles of pressure, withdrawal and return.",
                                "Rewards reading changing conditions and acting at the right phase rather than forcing one pace.",
                                Set.of("adaptation", "rhythm", "water"))
                ),
                List.of(
                        ability("ashen_guard", AbilityFamily.DEFENSE,
                                "Brace against a brief surge of harm or exhaustion by drawing on the Aspect's affinity for aftermath.",
                                "A short defensive expression, not immunity or automatic healing.",
                                List.of("Hold a chokepoint through one dangerous exchange.", "Buy time to finish a rescue or retreat."),
                                Set.of("defense", "endurance")),
                        ability("carry_the_flame", AbilityFamily.SUPPORT,
                                "Sustain a small protected source of warmth or light while carrying it through hostile conditions.",
                                "The expression preserves a modest existing flame-like effect; it is not unrestricted fire creation.",
                                List.of("Keep an authored beacon component viable during travel.", "Protect a fragile light source while moving allies."),
                                Set.of("light", "preservation", "support")),
                        ability("chime_warning", AbilityFamily.PERCEPTION,
                                "Produce or perceive a brief resonant warning when an authored nearby danger crosses a defined threshold.",
                                "It gives a warning cue, not omniscience, exact identity or universal hostile detection.",
                                List.of("Notice an approaching threat before line of sight.", "Give nearby allies a concise danger cue."),
                                Set.of("perception", "warning")),
                        ability("cut_the_thread", AbilityFamily.CONTROL,
                                "Sever one currently established DESIGN link, tether or tracking relationship represented by compatible content.",
                                "It only affects explicitly compatible links and does not erase arbitrary bonds, fate or ownership.",
                                List.of("Break a hostile tracking tether.", "Disconnect a temporary linked hazard before it propagates."),
                                Set.of("control", "precision", "severance")),
                        ability("endure_the_ruin", AbilityFamily.ENDURANCE,
                                "Temporarily keep functioning through an authored aftermath penalty that would otherwise interrupt an action.",
                                "The penalty is deferred or endured, not deleted, healed or converted into permanent resistance.",
                                List.of("Finish crossing a collapsing route while exhausted.", "Complete an interaction before an aftermath penalty takes hold."),
                                Set.of("aftermath", "endurance")),
                        ability("glass_edge", AbilityFamily.OFFENSE,
                                "Focus a precise retaliatory edge against a target or weak point already exposed by play.",
                                "The expression requires an authored opening and does not grant universal armor penetration or guaranteed damage.",
                                List.of("Exploit a creature weakness learned during investigation.", "Punish an exposed attacker after a committed strike."),
                                Set.of("offense", "precision", "retaliation")),
                        ability("hollow_step", AbilityFamily.MOVEMENT,
                                "Dampen the user's presence for one short reposition through a compatible concealment window.",
                                "It is not true invisibility, teleportation or immunity to perception.",
                                List.of("Cross a watched gap while attention is elsewhere.", "Reposition around a creature without starting a direct contest."),
                                Set.of("concealment", "movement")),
                        ability("hear_the_call", AbilityFamily.PERCEPTION,
                                "Sense the direction of a known or authored signal that the player has already been given reason to recognize.",
                                "It does not reveal arbitrary people, hidden objectives or unknown signals.",
                                List.of("Follow a distant warning signal through poor visibility.", "Recover a route after losing ordinary landmarks."),
                                Set.of("guidance", "perception", "warning")),
                        ability("holdfast", AbilityFamily.DEFENSE,
                                "Anchor the user against forced movement and loss of footing for a brief committed hold.",
                                "It improves stability rather than making the user immovable or invulnerable.",
                                List.of("Resist a shove near a ledge or flood channel.", "Maintain an interaction while environmental force builds."),
                                Set.of("defense", "stability")),
                        ability("kindle", AbilityFamily.SUPPORT,
                                "Encourage a small failing ember, lamp or similar authored source to catch and remain usable.",
                                "It supports compatible ignition/preservation content and is not unrestricted pyrokinesis.",
                                List.of("Restore a weakened signal fire after gathering fuel.", "Relight a prepared shelter source in poor conditions."),
                                Set.of("preservation", "resolve", "support")),
                        ability("low_tide", AbilityFamily.CONCEALMENT,
                                "Create a brief lull in a compatible water- or rhythm-driven pressure pattern, opening a safer movement window.",
                                "It manipulates an authored local pressure cycle and does not command arbitrary bodies of water.",
                                List.of("Cross a flooded passage during a temporary lull.", "Reduce environmental pressure long enough to extract someone."),
                                Set.of("adaptation", "concealment", "water")),
                        ability("mirror_glimpse", AbilityFamily.PERCEPTION,
                                "Catch a short reflected impression of an already-present nearby scene, angle or motion that ordinary sight misses.",
                                "The glimpse is partial and local; it is not prophecy, remote viewing or memory reading.",
                                List.of("Check around a blind corner using a reflective surface.", "Notice motion behind cover without exposing the whole body."),
                                Set.of("perception", "reflection")),
                        ability("mist_passage", AbilityFamily.MOVEMENT,
                                "Move through a short stretch of mist or obscuring conditions with reduced navigation penalty.",
                                "It helps traverse compatible obscurity and does not create unrestricted mist or phase through matter.",
                                List.of("Cross a hazardous fog bank without losing the route.", "Guide a short reposition while visibility collapses."),
                                Set.of("concealment", "movement")),
                        ability("resonant_mark", AbilityFamily.GUIDANCE,
                                "Place a temporary resonant marker on a compatible location or object so it can be recognized again nearby.",
                                "The mark is local navigation support, not permanent tracking or a cross-world locator.",
                                List.of("Mark the safe fork in a confusing ruin.", "Leave a recoverable reference point before scouting ahead."),
                                Set.of("guidance", "resonance")),
                        ability("returning_tide", AbilityFamily.MOBILITY_SUPPORT,
                                "Use the return phase of an authored pressure cycle to recover position, momentum or route access.",
                                "It depends on a compatible cycle and does not provide unconditional speed or teleportation.",
                                List.of("Retreat on the safe phase of a repeating hazard.", "Re-enter a route after pressure recedes."),
                                Set.of("adaptation", "movement", "rhythm")),
                        ability("root_in_stone", AbilityFamily.ENDURANCE,
                                "Settle into a stable stance that reduces fatigue from holding a fixed position under sustained pressure.",
                                "The benefit depends on remaining committed to the hold and does not grant permanent toughness.",
                                List.of("Maintain a defensive position during a long environmental hazard.", "Keep a mechanism engaged while allies move."),
                                Set.of("endurance", "stability")),
                        ability("shorten_the_road", AbilityFamily.MOVEMENT,
                                "Compress the effort of a short, already-visible route so traversing it costs less time or exertion.",
                                "It does not teleport, ignore barriers or create paths that do not exist.",
                                List.of("Reach cover before a closing hazard.", "Move a short evacuation leg more efficiently."),
                                Set.of("escape", "movement", "path")),
                        ability("still_presence", AbilityFamily.CONCEALMENT,
                                "Reduce incidental signs of presence while the user remains mostly still and deliberate.",
                                "It suppresses authored detection cues rather than making the user invisible or nonexistent.",
                                List.of("Wait out a searching creature without unnecessary movement.", "Observe a meeting from concealment when already hidden."),
                                Set.of("absence", "concealment")),
                        ability("thorn_lash", AbilityFamily.OFFENSE,
                                "Answer close hostile pressure with a short retaliatory lash represented by the Aspect's thorn theme.",
                                "It is bounded counterpressure, not an unlimited summoned weapon or automatic retaliation.",
                                List.of("Create space after an enemy commits to close range.", "Punish a pursuer that presses through an authored hazard line."),
                                Set.of("offense", "retaliation")),
                        ability("through_the_haze", AbilityFamily.PERCEPTION,
                                "Pick out useful silhouettes, movement or route cues through compatible visual obscurity.",
                                "It improves perception through authored haze but does not reveal invisible, conceptual or infinitely distant targets.",
                                List.of("Track movement through dense mist.", "Keep sight of a marked route while visibility degrades."),
                                Set.of("guidance", "perception")),
                        ability("warning_pulse", AbilityFamily.WARNING,
                                "Emit a brief directional warning pulse tied to a detected or authored immediate danger.",
                                "It communicates danger but does not identify every threat or compel allies to react.",
                                List.of("Alert nearby allies to an ambush direction.", "Signal that a known hazard has entered its dangerous phase."),
                                Set.of("signal", "warning")),
                        ability("waymark", AbilityFamily.GUIDANCE,
                                "Establish a short-lived directional reference toward a known destination or previously visited point.",
                                "It requires an authored known destination and does not discover unknown objectives.",
                                List.of("Keep a group oriented during a retreat.", "Return to a safe landmark after scouting a branch route."),
                                Set.of("guidance", "movement", "path")),
                        ability("weave_link", AbilityFamily.SUPPORT,
                                "Create one temporary explicit DESIGN link between compatible targets so a guidance or coordination cue can pass between them.",
                                "It does not merge minds, transfer ownership, bind fate or create permanent supernatural bonds.",
                                List.of("Keep two separated route markers coordinated.", "Share a simple movement cue between linked participants."),
                                Set.of("connection", "guidance", "support")),
                        ability("wicked_bloom", AbilityFamily.DEFENSE,
                                "Encourage a prepared thorn-like defensive growth or hazard to expand briefly when pressure reaches it.",
                                "It operates only on compatible authored content and does not create arbitrary living matter from nothing.",
                                List.of("Reinforce a prepared perimeter when pursued.", "Turn a planted defensive line into temporary cover or deterrence."),
                                Set.of("growth", "preservation", "retaliation"))
                )
        );
    }

    public List<NatureProfile> natures() {
        return List.copyOf(natures.values());
    }

    public List<AbilityProfile> abilities() {
        return List.copyOf(abilities.values());
    }

    public NatureProfile nature(ResourceLocation id) {
        NatureProfile profile = natures.get(Objects.requireNonNull(id, "id"));
        if (profile == null) {
            throw new IllegalArgumentException("Unknown Aspect nature content id: " + id);
        }
        return profile;
    }

    public AbilityProfile ability(ResourceLocation id) {
        AbilityProfile profile = abilities.get(Objects.requireNonNull(id, "id"));
        if (profile == null) {
            throw new IllegalArgumentException("Unknown Aspect ability content id: " + id);
        }
        return profile;
    }

    /**
     * Compose presentation only after the generator/core has resolved both IDs.
     */
    public AspectAbilityPresentation compose(ResourceLocation natureId, ResourceLocation abilityId) {
        NatureProfile natureProfile = nature(natureId);
        AbilityProfile abilityProfile = ability(abilityId);
        IdentityPrimitiveCatalog.Nature naturePrimitive = primitiveNature(natureId);
        IdentityPrimitiveCatalog.Ability abilityPrimitive = primitiveAbility(abilityId);
        if (!abilityPrimitive.supports(naturePrimitive)) {
            throw new IllegalArgumentException("Ability " + abilityId + " is not compatible with nature " + natureId);
        }
        return new AspectAbilityPresentation(
                PRESENTATION_VERSION,
                PROVENANCE,
                natureId,
                naturePrimitive.nameToken(),
                natureProfile.summary(),
                natureProfile.playerPromise(),
                abilityId,
                abilityPrimitive.displayName(),
                abilityProfile.family(),
                abilityProfile.summary(),
                abilityProfile.boundary(),
                abilityProfile.gameplayHooks(),
                abilityProfile.presentationTags()
        );
    }

    private IdentityPrimitiveCatalog.Nature primitiveNature(ResourceLocation id) {
        return identityCatalog.natures().stream()
                .filter(value -> value.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown generated nature id: " + id));
    }

    private IdentityPrimitiveCatalog.Ability primitiveAbility(ResourceLocation id) {
        return identityCatalog.abilities().stream()
                .filter(value -> value.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown generated ability id: " + id));
    }

    private void validateCoverage() {
        Set<ResourceLocation> generatedNatures = identityCatalog.natures().stream()
                .map(IdentityPrimitiveCatalog.Nature::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<ResourceLocation> generatedAbilities = identityCatalog.abilities().stream()
                .map(IdentityPrimitiveCatalog.Ability::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!generatedNatures.equals(natures.keySet())) {
            throw new IllegalArgumentException("Aspect nature presentation must exactly cover generated nature identities");
        }
        if (!generatedAbilities.equals(abilities.keySet())) {
            throw new IllegalArgumentException("Aspect ability presentation must exactly cover generated ability identities");
        }
    }

    private static NatureProfile nature(String path, String summary, String playerPromise, Set<String> presentationTags) {
        return new NatureProfile(id("generation/nature/" + path), summary, playerPromise, presentationTags);
    }

    private static AbilityProfile ability(
            String path,
            AbilityFamily family,
            String summary,
            String boundary,
            List<String> gameplayHooks,
            Set<String> presentationTags
    ) {
        return new AbilityProfile(
                id("generation/ability/" + path),
                family,
                summary,
                boundary,
                gameplayHooks,
                presentationTags
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static <T> Map<ResourceLocation, T> indexed(
            List<T> values,
            java.util.function.Function<T, ResourceLocation> idExtractor,
            String family
    ) {
        LinkedHashMap<ResourceLocation, T> result = new LinkedHashMap<>();
        values.stream()
                .sorted(java.util.Comparator.comparing(value -> idExtractor.apply(value).toString()))
                .forEach(value -> {
                    ResourceLocation id = idExtractor.apply(Objects.requireNonNull(value, family));
                    if (result.put(id, value) != null) {
                        throw new IllegalArgumentException("Duplicate " + family + " id: " + id);
                    }
                });
        return Map.copyOf(result);
    }

    public enum AbilityFamily {
        CONCEALMENT,
        CONTROL,
        DEFENSE,
        ENDURANCE,
        GUIDANCE,
        MOBILITY_SUPPORT,
        MOVEMENT,
        OFFENSE,
        PERCEPTION,
        SUPPORT,
        WARNING
    }

    public record NatureProfile(
            ResourceLocation id,
            String summary,
            String playerPromise,
            Set<String> presentationTags
    ) {
        public NatureProfile {
            id = Objects.requireNonNull(id, "id");
            summary = text(summary, "summary");
            playerPromise = text(playerPromise, "playerPromise");
            presentationTags = Set.copyOf(Objects.requireNonNull(presentationTags, "presentationTags"));
            if (presentationTags.isEmpty()) {
                throw new IllegalArgumentException("presentationTags cannot be empty");
            }
        }
    }

    public record AbilityProfile(
            ResourceLocation id,
            AbilityFamily family,
            String summary,
            String boundary,
            List<String> gameplayHooks,
            Set<String> presentationTags
    ) {
        public AbilityProfile {
            id = Objects.requireNonNull(id, "id");
            family = Objects.requireNonNull(family, "family");
            summary = text(summary, "summary");
            boundary = text(boundary, "boundary");
            gameplayHooks = List.copyOf(Objects.requireNonNull(gameplayHooks, "gameplayHooks"));
            if (gameplayHooks.size() < 2 || gameplayHooks.stream().anyMatch(value -> value == null || value.isBlank())) {
                throw new IllegalArgumentException("Every ability requires at least two non-blank gameplay hooks");
            }
            presentationTags = Set.copyOf(Objects.requireNonNull(presentationTags, "presentationTags"));
            if (presentationTags.isEmpty()) {
                throw new IllegalArgumentException("presentationTags cannot be empty");
            }
        }
    }

    public record AspectAbilityPresentation(
            String version,
            String provenance,
            ResourceLocation natureId,
            String natureName,
            String natureSummary,
            String playerPromise,
            ResourceLocation abilityId,
            String abilityName,
            AbilityFamily family,
            String abilitySummary,
            String boundary,
            List<String> gameplayHooks,
            Set<String> presentationTags
    ) {
        public AspectAbilityPresentation {
            version = text(version, "version");
            provenance = text(provenance, "provenance");
            natureId = Objects.requireNonNull(natureId, "natureId");
            natureName = text(natureName, "natureName");
            natureSummary = text(natureSummary, "natureSummary");
            playerPromise = text(playerPromise, "playerPromise");
            abilityId = Objects.requireNonNull(abilityId, "abilityId");
            abilityName = text(abilityName, "abilityName");
            family = Objects.requireNonNull(family, "family");
            abilitySummary = text(abilitySummary, "abilitySummary");
            boundary = text(boundary, "boundary");
            gameplayHooks = List.copyOf(gameplayHooks);
            presentationTags = Set.copyOf(presentationTags);
        }
    }

    private static String text(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
