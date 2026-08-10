package dev.spud.shadowslave.world.entity;

import java.util.Objects;
import java.util.Set;

/** Pure-Java selector for replacing the Drowned Bell's legacy vanilla execution placeholder. */
public final class DrownedBellListenerExecutionBinding {
    public static final String VANILLA_DROWNED_ID = "minecraft:drowned";
    public static final String PLACEHOLDER_TAG = "shadowslave_drowned_listener_placeholder";

    private DrownedBellListenerExecutionBinding() {
    }

    public static boolean shouldReplace(String entityTypeId, Set<String> tags) {
        Objects.requireNonNull(entityTypeId, "entityTypeId");
        Objects.requireNonNull(tags, "tags");
        return VANILLA_DROWNED_ID.equals(entityTypeId) && tags.contains(PLACEHOLDER_TAG);
    }
}
