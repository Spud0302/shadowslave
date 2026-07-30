package dev.spud.shadowslave.soul;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoulDataTest {
    private static final ResourceLocation ASPECT =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/veiled_witness");
    private static final ResourceLocation FLAW =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/heavy_step");

    @Test
    void defaultsToMundane() {
        SoulData soul = SoulData.mundane();

        assertEquals(SoulData.CURRENT_SCHEMA, soul.schemaVersion());
        assertEquals(SpellState.MUNDANE, soul.spellState());
        assertEquals(SoulRank.MUNDANE, soul.soulRank());
        assertTrue(soul.aspectId().isEmpty());
        assertTrue(soul.flawId().isEmpty());
        assertFalse(soul.importedFromDatapack());
    }

    @Test
    void firstNightmareRequiresCarrierAndProducesDormantSleeper() {
        SoulData carrier = SoulTransitions.infect(SoulData.mundane());
        SoulData sleeper = SoulTransitions.completeFirstNightmare(carrier, ASPECT, FLAW);

        assertEquals(SpellState.SLEEPER, sleeper.spellState());
        assertEquals(SoulRank.DORMANT, sleeper.soulRank());
        assertEquals(ASPECT, sleeper.aspectId().orElseThrow());
        assertEquals(FLAW, sleeper.flawId().orElseThrow());

        assertThrows(
                IllegalStateException.class,
                () -> SoulTransitions.completeFirstNightmare(SoulData.mundane(), ASPECT, FLAW)
        );
    }

    @Test
    void codecRoundTripsImportedIdentity() {
        SoulData original = SoulTransitions.completeFirstNightmare(
                SoulTransitions.infect(SoulData.mundane()),
                ASPECT,
                FLAW
        ).markImported(1);

        JsonElement encoded = SoulData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, original)
                .getOrThrow();
        SoulData decoded = SoulData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded)
                .getOrThrow();

        assertEquals(original, decoded);
    }
}
