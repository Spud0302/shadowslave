package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.ability.AbilityService;
import dev.spud.shadowslave.network.SoulSyncService;
import dev.spud.shadowslave.soul.identity.AspectAbilityData;
import dev.spud.shadowslave.soul.identity.AspectInstanceData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

/** Adds a rank-up ability variant to a player's Aspect. */
public final class AbilityGrants {
    private AbilityGrants() {
    }

    /**
     * Grants {@code variant} to the player's Aspect. False if they have no
     * Aspect, the variant belongs to another Nature, or they already hold it.
     */
    public static boolean grant(ServerPlayer player, AbilityVariant variant) {
        Objects.requireNonNull(variant, "variant");
        SoulIdentityData identity = SoulIdentityService.get(player);
        Optional<AspectInstanceData> aspect = identity.aspect();
        if (aspect.isEmpty()
                || !KitLadder.variants(aspect.get().natureId(), variant.rank()).contains(variant)
                || aspect.get().abilitySet().contains(variant.abilityId())) {
            return false;
        }
        AspectInstanceData next = aspect.get().withAbilitySet(aspect.get().abilitySet().with(
                AspectAbilityData.rankGranted(variant.abilityId(), variant.rank(), "rank-up choice: " + variant.name())));
        SoulIdentityService.replace(player, new SoulIdentityData(Optional.of(next), identity.flaw()));
        SoulSyncService.sync(player);
        AbilityService.syncStatus(player);
        return true;
    }
}
