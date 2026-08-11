package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Server-authoritative access boundary for Borrowed Dawn's stored-light payload. */
public final class BorrowedDawnChargeService {
    private BorrowedDawnChargeService() {}

    public static BorrowedDawnChargeData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.BORROWED_DAWN_CHARGE);
    }

    public static void storeLight(ServerPlayer player) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.BORROWED_DAWN_CHARGE, BorrowedDawnChargeData.storedLight());
    }

    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.BORROWED_DAWN_CHARGE, BorrowedDawnChargeData.empty());
    }
}
