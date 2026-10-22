package dev.spud.shadowslave.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** One activatable Aspect ability, run on the server when the player presses the ability key. */
public interface AspectAbility {
    ResourceLocation id();

    String displayName();

    /** Essence one use takes from the shared pool. */
    int essenceCost();

    /** Performs the ability for one key press. */
    Activation activate(ServerPlayer player);

    /** Called every server tick for players who hold this ability, for abilities with lingering state. */
    default void tick(ServerPlayer player) {
    }

    /** Drops any lingering state (a wall, a ward) when the player leaves or loses the ability. */
    default void clear(ServerPlayer player) {
    }

    /** True while a press has set something up that the next press will finish. */
    default boolean primed(ServerPlayer player) {
        return false;
    }

    enum Activation {
        /** The ability fired; its essence is spent. */
        USED,
        /** The press set something up (a mark, a stance); the essence is spent when it finishes. */
        PRIMED,
        /** Nothing happened, for example no room to move. */
        FAILED
    }
}
