package dev.spud.shadowslave.dreamrealm;

import java.util.List;
import java.util.Objects;

/** Pure-Java presentation binding for Cinder Rest's authored hooded-lamp arrival cue. */
public final class CinderRestLanternRingBinding {
    private CinderRestLanternRingBinding() {}

    public record Lamp(int x, int y, int z) {}

    public record Binding(
            String moduleId,
            String settlementName,
            String factionName,
            List<String> serviceLabels,
            String standingRule,
            List<Lamp> lamps
    ) {
        public Binding {
            moduleId = nonBlank(moduleId, "moduleId");
            settlementName = nonBlank(settlementName, "settlementName");
            factionName = nonBlank(factionName, "factionName");
            serviceLabels = List.copyOf(Objects.requireNonNull(serviceLabels, "serviceLabels"));
            standingRule = nonBlank(standingRule, "standingRule");
            lamps = List.copyOf(Objects.requireNonNull(lamps, "lamps"));
            if (serviceLabels.isEmpty()) throw new IllegalArgumentException("serviceLabels cannot be empty");
            if (lamps.isEmpty()) throw new IllegalArgumentException("lamps cannot be empty");
        }
    }

    public static Binding cinderRest() {
        var story = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        return new Binding(
                story.moduleId(),
                story.settlementName(),
                story.factionName(),
                story.serviceLabels(),
                story.standingRule(),
                List.of(
                        new Lamp(-4, 2, 2),
                        new Lamp(0, 2, 2),
                        new Lamp(4, 2, 2),
                        new Lamp(-4, 2, 0),
                        new Lamp(4, 2, 0)
                ));
    }

    public static boolean isLamp(Binding binding, int x, int y, int z) {
        Objects.requireNonNull(binding, "binding");
        return binding.lamps().stream().anyMatch(lamp -> lamp.x() == x && lamp.y() == y && lamp.z() == z);
    }

    private static String nonBlank(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) throw new IllegalArgumentException(name + " cannot be blank");
        return checked;
    }
}
