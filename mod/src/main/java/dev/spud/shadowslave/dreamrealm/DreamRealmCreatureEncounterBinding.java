package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.dreamrealm.content.DreamRealmRegionContentCatalog;
import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;
import dev.spud.shadowslave.world.entity.AshBurrowerExecutionBinding;

/** Pure-Java validation for one physical region encounter using existing authored identities. */
public final class DreamRealmCreatureEncounterBinding {
    private DreamRealmCreatureEncounterBinding() {
    }

    public record Encounter(
            DreamRealmRegionContentCatalog.RegionProfile region,
            NightmareCreatureContentCatalog.CreatureProfile creature,
            int x,
            int y,
            int z
    ) {
    }

    public static Encounter ashenExpanseAshBurrower() {
        DreamRealmRegionContentCatalog.RegionProfile region = DreamRealmVerticalSliceDefinition.ashenExpanse().region();
        NightmareCreatureContentCatalog.CreatureProfile creature = AshBurrowerExecutionBinding.profile();
        if (!region.creatureAffinityIds().contains(creature.id())) {
            throw new IllegalStateException("Ash Burrower is not authored for the Ashen Expanse");
        }
        return new Encounter(region, creature, 12, 1, 6);
    }
}
