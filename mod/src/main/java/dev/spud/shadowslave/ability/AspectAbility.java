package dev.spud.shadowslave.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** One activatable Aspect ability, run on the server when the player presses the ability key. */
public interface AspectAbility {
    ResourceLocation id();

    String displayName();

    int cooldownTicks();

    /** Performs the ability for one key press. */
    Activation activate(ServerPlayer player);

    /** Called every server tick for players who hold this ability, for abilities with lingering state. */
    default void tick(ServerPlayer player) {
    }

    /** True while a press has set something up that the next press will finish. */
    default boolean primed(ServerPlayer player) {
        return false;
    }

    enum Activation {
        /** The ability fired; its cooldown starts. */
        USED,
        /** The press set something up (a mark, a stance) without starting the cooldown yet. */
        PRIMED,
        /** Nothing happened, for example no room to move. */
        FAILED
    }
}
