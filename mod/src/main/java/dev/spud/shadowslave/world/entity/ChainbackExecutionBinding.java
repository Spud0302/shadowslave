package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.nightmare.content.NightmareCreatureContentCatalog;

/** Pure-Java binding from the physical Chainback adapter to its authoritative content profile. */
public final class ChainbackExecutionBinding {
    public static final String CONTENT_ID = "chainback";

    private ChainbackExecutionBinding() {
    }

    /** Returns the existing Java-owned content profile rather than duplicating identity here. */
    public static NightmareCreatureContentCatalog.CreatureProfile contentProfile() {
        return NightmareCreatureContentCatalog.waveOne().stream()
                .filter(profile -> profile.id().equals(CONTENT_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Nightmare Creature profile: " + CONTENT_ID));
    }
}
