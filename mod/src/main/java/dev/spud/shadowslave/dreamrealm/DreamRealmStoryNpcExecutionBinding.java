package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmStoryContentCatalog;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Pure-Java binding between authored Dream Realm story identity and a removable Minecraft NPC body.
 * This class resolves existing content only; it owns no relationship, progression, quest or world state.
 */
public final class DreamRealmStoryNpcExecutionBinding {
    public static final String ASHEN_WATCH_MODULE_ID = "ashen_watch";
    public static final String WATCH_CAPTAIN_ARCHETYPE_ID = "watch_captain";

    private DreamRealmStoryNpcExecutionBinding() {
    }

    public record Binding(
            String moduleId,
            String moduleDisplayName,
            String regionId,
            String settlementName,
            String factionName,
            String archetypeId,
            String archetypeDisplayName,
            List<String> serviceLabels,
            String arrivalCue,
            String standingRule
    ) {
        public Binding {
            moduleId = nonBlank(moduleId, "moduleId");
            moduleDisplayName = nonBlank(moduleDisplayName, "moduleDisplayName");
            regionId = nonBlank(regionId, "regionId");
            settlementName = nonBlank(settlementName, "settlementName");
            factionName = nonBlank(factionName, "factionName");
            archetypeId = nonBlank(archetypeId, "archetypeId");
            archetypeDisplayName = nonBlank(archetypeDisplayName, "archetypeDisplayName");
            serviceLabels = List.copyOf(Objects.requireNonNull(serviceLabels, "serviceLabels"));
            if (serviceLabels.isEmpty()) {
                throw new IllegalArgumentException("serviceLabels cannot be empty");
            }
            arrivalCue = nonBlank(arrivalCue, "arrivalCue");
            standingRule = nonBlank(standingRule, "standingRule");
        }
    }

    public static Binding ashenWatchCaptain() {
        return resolve(ASHEN_WATCH_MODULE_ID, WATCH_CAPTAIN_ARCHETYPE_ID);
    }

    public static Binding resolve(String moduleId, String archetypeId) {
        String checkedModuleId = nonBlank(moduleId, "moduleId");
        String checkedArchetypeId = nonBlank(archetypeId, "archetypeId");

        DreamRealmStoryContentCatalog.StoryModule module = DreamRealmStoryContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals(checkedModuleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Dream Realm story module: " + checkedModuleId));

        if (!module.npcArchetypes().contains(checkedArchetypeId)) {
            throw new IllegalArgumentException(
                    "NPC archetype " + checkedArchetypeId + " is not authored for story module " + checkedModuleId);
        }

        List<String> services = module.services().stream()
                .map(service -> service.name().toLowerCase(Locale.ROOT))
                .sorted(Comparator.naturalOrder())
                .toList();

        return new Binding(
                module.id(),
                module.displayName(),
                module.regionId(),
                module.settlementName(),
                module.factionName(),
                checkedArchetypeId,
                humanize(checkedArchetypeId),
                services,
                module.arrivalCue(),
                module.standingRule()
        );
    }

    private static String humanize(String stableId) {
        StringBuilder result = new StringBuilder();
        for (String part : stableId.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static String nonBlank(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
