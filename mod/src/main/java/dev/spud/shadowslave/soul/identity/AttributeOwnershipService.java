package dev.spud.shadowslave.soul.identity;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Mutation boundary for persistent Java-owned Attribute identities. */
public final class AttributeOwnershipService {
    private AttributeOwnershipService() {
    }

    public static AttributeOwnershipData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.ATTRIBUTES);
    }

    public static AttributeOwnershipData replace(ServerPlayer player, AttributeOwnershipData next) {
        ServerPlayer checkedPlayer = Objects.requireNonNull(player, "player");
        AttributeOwnershipData checkedNext = Objects.requireNonNull(next, "next");
        checkedPlayer.setData(ModAttachments.ATTRIBUTES, checkedNext);
        return checkedNext;
    }

    public static AttributeOwnershipData award(ServerPlayer player, AttributeInstanceData attribute) {
        return replace(player, get(player).award(attribute));
    }
}
