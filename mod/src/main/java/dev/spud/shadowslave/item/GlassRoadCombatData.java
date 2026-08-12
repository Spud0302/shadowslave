package dev.spud.shadowslave.item;

/** Transient server-authoritative commitment/recovery state for Glass Road's clean-edge action. */
public record GlassRoadCombatData(long strikeAt, long recoveryUntil) {
    public GlassRoadCombatData {
        if (strikeAt < 0 || recoveryUntil < 0) throw new IllegalArgumentException("combat timestamps cannot be negative");
    }

    public static GlassRoadCombatData empty() {
        return new GlassRoadCombatData(0L, 0L);
    }

    public boolean committed(long now) {
        return strikeAt > now;
    }

    public boolean readyToResolve(long now) {
        return strikeAt > 0L && strikeAt <= now;
    }

    public boolean recovering(long now) {
        return recoveryUntil > now;
    }
}
