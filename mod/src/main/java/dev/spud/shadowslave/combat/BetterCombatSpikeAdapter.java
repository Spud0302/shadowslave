package dev.spud.shadowslave.combat;

import net.bettercombat.api.CombatFlags;
import net.minecraft.server.level.ServerPlayer;

/**
 * Development-spike boundary around the one Better Combat Java API read needed to avoid
 * judging a vanilla attack when another mod has disabled Better Combat for this player.
 *
 * <p>This class must only be touched after NeoForge confirms Better Combat is loaded. It
 * intentionally exposes no attack timing, targeting, hitbox, damage, animation, or state
 * mutation API to Shadow Slave.</p>
 */
final class BetterCombatSpikeAdapter {
    private BetterCombatSpikeAdapter() {
    }

    static boolean isAttackDisabled(ServerPlayer player) {
        return CombatFlags.isAttackDisabled(player);
    }
}
