package dev.spud.shadowslave.dreamrealm;

/** Pure-Java integration contract tying one authored settlement/NPC to the bounded Ashen Expanse slice. */
public final class DreamRealmWorldStoryIntegration {
    private DreamRealmWorldStoryIntegration() {
    }

    public record CinderRest(
            DreamRealmVerticalSliceDefinition.Slice slice,
            DreamRealmStoryNpcExecutionBinding.Binding watchCaptain,
            int x,
            int y,
            int z
    ) {
    }

    public static CinderRest cinderRest() {
        var slice = DreamRealmVerticalSliceDefinition.ashenExpanse();
        var captain = DreamRealmStoryNpcExecutionBinding.ashenWatchCaptain();
        if (!captain.regionId().equals(slice.region().id())) {
            throw new IllegalStateException("Cinder Rest story module does not belong to the Ashen Expanse slice");
        }
        return new CinderRest(slice, captain, 0, 1, -1);
    }
}
