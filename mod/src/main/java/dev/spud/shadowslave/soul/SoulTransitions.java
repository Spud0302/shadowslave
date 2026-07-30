package dev.spud.shadowslave.soul;

import net.minecraft.resources.ResourceLocation;

/**
 * Pure transition rules for Soul state. Keeping these independent of Minecraft
 * entities makes them unit-testable and reusable by commands, migration and
 * Nightmare completion.
 */
public final class SoulTransitions {
    private SoulTransitions() {
    }

    public static SoulData infect(SoulData current) {
        if (current.spellState() != SpellState.MUNDANE) {
            return current;
        }
        return current.asCarrier();
    }

    public static SoulData completeFirstNightmare(
            SoulData current,
            ResourceLocation aspectId,
            ResourceLocation flawId
    ) {
        if (current.spellState() != SpellState.CARRIER) {
            throw new IllegalStateException("Only a Carrier can complete the First Nightmare");
        }
        return current.asSleeper(aspectId, flawId);
    }

    public static SoulData reset() {
        return SoulData.mundane();
    }
}
