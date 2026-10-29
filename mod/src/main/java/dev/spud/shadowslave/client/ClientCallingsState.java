package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SpellCallingsPayload;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The Callings this player has heard, for the Field Guide, and the one showing now. */
public final class ClientCallingsState {
    private static final List<SpellCallingsPayload.Entry> HEARD = new ArrayList<>();
    private static SpellCallingsPayload.Entry showing;
    private static long shownAt;

    private ClientCallingsState() {
    }

    public static void update(SpellCallingsPayload payload) {
        if (!payload.fresh()) {
            HEARD.clear();
        }
        HEARD.addAll(payload.entries());
        while (HEARD.size() > SpellCallingsPayload.MAX) {
            HEARD.remove(0);
        }
        if (payload.fresh() && !payload.entries().isEmpty()) {
            show(payload.entries().get(payload.entries().size() - 1));
        }
    }

    /** Shows a Calling over the HUD from now. */
    public static void show(SpellCallingsPayload.Entry entry) {
        showing = entry;
        shownAt = Util.getMillis();
    }

    /** Newest first. */
    public static List<SpellCallingsPayload.Entry> heard() {
        List<SpellCallingsPayload.Entry> out = new ArrayList<>(HEARD);
        java.util.Collections.reverse(out);
        return out;
    }

    public static Optional<SpellCallingsPayload.Entry> showing() {
        return Optional.ofNullable(showing);
    }

    public static long shownAt() {
        return shownAt;
    }

    public static void clearShowing() {
        showing = null;
    }

    public static void reset() {
        HEARD.clear();
        showing = null;
    }
}
