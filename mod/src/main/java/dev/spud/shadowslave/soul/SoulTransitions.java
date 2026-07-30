package dev.spud.shadowslave.soul;

import net.minecraft.resources.ResourceLocation;

/**
 * Pure transition rules for permanent Soul state.
 *
 * <p>Nightmare scenario state, assigned historical roles and temporary bodies
 * belong to Nightmare instances. These rules only mark the canonical stage
 * reached at each lifecycle boundary.</p>
 */
public final class SoulTransitions {
    private SoulTransitions() {
    }

    public static SoulData infect(SoulData current) {
        if (current.spellState() != SpellState.UNINFECTED) {
            return current;
        }
        return current.asCarrier();
    }

    public static SoulData beginFirstNightmare(SoulData current) {
        if (current.spellState() != SpellState.CARRIER) {
            throw new IllegalStateException("Only a Carrier can become an Aspirant");
        }
        return current.asAspirant();
    }

    public static SoulData completeFirstNightmare(
            SoulData current,
            ResourceLocation aspectId,
            SoulRank aspectRank,
            ResourceLocation flawId
    ) {
        if (current.spellState() != SpellState.ASPIRANT) {
            throw new IllegalStateException("Only an Aspirant can complete the First Nightmare");
        }
        return current.asDreamer(aspectId, aspectRank, flawId);
    }

    public static SoulData reset() {
        return SoulData.uninfected();
    }
}
