package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulTransitions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoulSnapshotTest {
    private static final ResourceLocation ASPECT =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/veiled_witness");
    private static final ResourceLocation FLAW =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "prototype/heavy_step");

    @Test
    void snapshotContainsOnlyTheClientFacingIdentity() {
        SoulData imported = SoulTransitions.completeFirstNightmare(
                SoulTransitions.infect(SoulData.mundane()),
                ASPECT,
                FLAW
        ).markImported(1);

        SoulSnapshot snapshot = SoulSnapshot.from(imported);

        assertEquals(SoulData.CURRENT_SCHEMA, snapshot.schemaVersion());
        assertEquals("sleeper", snapshot.spellState());
        assertEquals("dormant", snapshot.soulRank());
        assertEquals(ASPECT.toString(), snapshot.aspectId());
        assertEquals(FLAW.toString(), snapshot.flawId());
        assertTrue(snapshot.importedFromDatapack());
    }

    @Test
    void payloadRoundTripsWithoutClientSuppliedSoulData() {
        SoulSnapshot original = SoulSnapshot.from(SoulData.mundane());
        SoulSnapshotPayload payload = new SoulSnapshotPayload(original, true);
        ByteBuf buffer = Unpooled.buffer();

        try {
            SoulSnapshotPayload.STREAM_CODEC.encode(buffer, payload);
            SoulSnapshotPayload decoded = SoulSnapshotPayload.STREAM_CODEC.decode(buffer);

            assertEquals(payload, decoded);
            assertTrue(decoded.openScreen());
            assertEquals("—", decoded.snapshot().displayedAspect());
            assertEquals("—", decoded.snapshot().displayedFlaw());
            assertFalse(decoded.snapshot().importedFromDatapack());
        } finally {
            buffer.release();
        }
    }
}
