package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import dev.spud.shadowslave.soul.identity.SoulIdentityService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Server-side lookups for the always-on parts of a kit: innate rules and Flaws. */
public final class KitRules {
    private KitRules() {
    }

    public static boolean hasInnate(ServerPlayer player, ResourceLocation innateId) {
        return SoulIdentityService.get(player).aspect()
                .map(aspect -> aspect.abilitySet().contains(innateId))
                .orElse(false);
    }

    public static boolean hasFlaw(ServerPlayer player, ResourceLocation flawEffectId) {
        return hasFlaw(SoulIdentityService.get(player), flawEffectId);
    }

    static boolean hasFlaw(SoulIdentityData identity, ResourceLocation flawEffectId) {
        return identity.flaw().map(flaw -> flaw.effectId().equals(flawEffectId)).orElse(false);
    }
}
