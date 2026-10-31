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

    /** A player on the server by id, mock players in game tests included, or null. */
    public static ServerPlayer online(net.minecraft.server.MinecraftServer server, java.util.UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            return player;
        }
        for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
            if (level.getPlayerByUUID(id) instanceof ServerPlayer found) {
                return found;
            }
        }
        return null;
    }

    static boolean hasFlaw(SoulIdentityData identity, ResourceLocation flawEffectId) {
        return identity.flaw().map(flaw -> flaw.effectId().equals(flawEffectId)).orElse(false);
    }
}
