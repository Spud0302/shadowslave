package dev.spud.shadowslave.memory;

import dev.spud.shadowslave.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Server-authoritative access boundary for Bellglass Token's one stored note. */
public final class BellglassHeldNoteService {
    private BellglassHeldNoteService() {}

    public static BellglassHeldNoteData get(ServerPlayer player) {
        return Objects.requireNonNull(player, "player").getData(ModAttachments.BELLGLASS_HELD_NOTE);
    }

    public static BellglassHeldNoteData capture(ServerPlayer player, String instrument, int note) {
        Objects.requireNonNull(player, "player");
        BellglassHeldNoteData data = BellglassHeldNoteData.captured(instrument, note);
        player.setData(ModAttachments.BELLGLASS_HELD_NOTE, data);
        return data;
    }

    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player").setData(ModAttachments.BELLGLASS_HELD_NOTE, BellglassHeldNoteData.empty());
    }
}
