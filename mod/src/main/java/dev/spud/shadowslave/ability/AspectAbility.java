package dev.spud.shadowslave.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** One activatable Aspect ability, run on the server when the player presses the ability key. */
public interface AspectAbility {
    ResourceLocation id();

    String displayName();

    int cooldownTicks();

    /**
     * Performs the ability. Returns false when it could not be used right now
     * (for example nothing to target); the cooldown only starts on true.
     */
    boolean activate(ServerPlayer player);
}
