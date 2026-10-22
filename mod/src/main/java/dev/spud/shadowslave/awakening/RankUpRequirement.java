package dev.spud.shadowslave.awakening;

import net.minecraft.server.level.ServerPlayer;

/**
 * What a player must have done before they can rise to the next rank. Kept
 * behind this interface so the trigger can change (a challenge, a Nightmare)
 * without touching the rank-up itself.
 */
public interface RankUpRequirement {
    boolean met(ServerPlayer player);

    /** One line for the Soul Sea: what is needed, or that it is done. */
    String describe(ServerPlayer player);
}
