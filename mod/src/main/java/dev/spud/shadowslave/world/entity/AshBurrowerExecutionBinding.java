package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;

/** Pure-Java binding from the physical executor to the existing authored creature profile. */
public final class AshBurrowerExecutionBinding {
    public static final String CONTENT_ID = "ash_burrower";

    private AshBurrowerExecutionBinding() {
    }

    public static NightmareCreatureContentCatalog.CreatureProfile profile() {
        return NightmareCreatureContentCatalog.waveOne().stream()
                .filter(candidate -> candidate.id().equals(CONTENT_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Java-owned Nightmare Creature " + CONTENT_ID));
    }
}
