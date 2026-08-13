package dev.spud.combatcore.api;

public record CombatActionDefinition(
        String id,
        int windupTicks,
        int activeTicks,
        int recoveryTicks,
        double reach
) {
    public CombatActionDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must be non-blank");
        if (windupTicks < 0 || activeTicks <= 0 || recoveryTicks < 0) {
            throw new IllegalArgumentException("invalid phase duration");
        }
        if (!Double.isFinite(reach) || reach <= 0.0) {
            throw new IllegalArgumentException("reach must be finite and positive");
        }
    }

    public int totalTicks() {
        return windupTicks + activeTicks + recoveryTicks;
    }
}
