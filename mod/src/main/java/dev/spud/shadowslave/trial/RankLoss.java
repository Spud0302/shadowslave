package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.ability.EssenceService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import dev.spud.shadowslave.soul.SpellState;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.identity.AspectAbilitySetData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * The harsher costs of dying in a Nightmare (server options, Jack 2026-10-29):
 * True Death costs a rank, Hardcore throws you back to Dormant. Abilities a
 * rank-up granted go with the rank, and the core empties.
 *
 * <p>Today the only rank above Dormant a player can hold is Awakened, so both
 * land on Dormant. Ranks above that are not built yet and are left alone.</p>
 */
public final class RankLoss {
    private RankLoss() {
    }

    /** Drops an Awakened player back to Dormant. Returns false if there was nothing to lose. */
    public static boolean toDormant(ServerPlayer player) {
        SoulData soul = SoulService.get(player);
        if (soul.spellState() != SpellState.AWAKENED || soul.aspectId().isEmpty() || soul.flawId().isEmpty()) {
            if (soul.soulRank().filter(rank -> rank.compareTo(SoulRank.AWAKENED) > 0).isPresent()) {
                ShadowSlaveMod.LOGGER.warn("{} would lose a rank above Awakened, which is not built yet", player.getScoreboardName());
            }
            return false;
        }
        SoulService.replace(player, soul.asDreamer(soul.aspectId().get(), SoulRank.DORMANT, soul.flawId().get()));
        SoulIdentityData identity = SoulIdentityService.get(player);
        Optional<AspectInstanceData> aspect = identity.aspect();
        if (aspect.isPresent()) {
            AspectAbilitySetData kept = new AspectAbilitySetData(aspect.get().abilitySet().abilities().stream()
                    .filter(ability -> ability.acquisitionRank().filter(rank -> rank.compareTo(SoulRank.DORMANT) > 0).isEmpty())
                    .toList());
            SoulIdentityService.replace(player, new SoulIdentityData(
                    Optional.of(aspect.get().withAspectRank(SoulRank.DORMANT).withAbilitySet(kept)), identity.flaw()));
        }
        CoreService.set(player, 0);
        CoreService.refresh(player);
        EssenceService.fill(player);
        SoulSyncService.sync(player);
        AbilityService.syncStatus(player);
        return true;
    }
}
