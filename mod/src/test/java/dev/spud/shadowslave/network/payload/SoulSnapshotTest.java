package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
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
    void snapshotSeparatesStatusPathSoulRankAndAspectRank() {
        SoulData imported = SoulTransitions.completeFirstNightmare(
                SoulTransitions.beginFirstNightmare(
                        SoulTransitions.infect(SoulData.uninfected())
                ),
                ASPECT,
                SoulRank.DIVINE,
                FLAW
        ).markImported(1);

        SoulSnapshot snapshot = SoulSnapshot.from(imported);

        assertEquals(SoulData.CURRENT_SCHEMA, snapshot.schemaVersion());
        assertEquals("dreamer", snapshot.spellState());
        assertEquals("nightmare_spell", snapshot.awakeningPath());
        assertEquals("dormant", snapshot.soulRank());
        assertEquals(ASPECT.toString(), snapshot.aspectId());
        assertEquals("divine", snapshot.aspectRank());
        assertEquals(FLAW.toString(), snapshot.flawId());
        assertTrue(snapshot.importedFromDatapack());
    }

    @Test
    void payloadRoundTripsWithoutClientSuppliedSoulData() {
        SoulSnapshot original = SoulSnapshot.from(SoulData.uninfected());
        SoulSnapshotPayload payload = new SoulSnapshotPayload(original, true);
        ByteBuf buffer = Unpooled.buffer();

        try {
            SoulSnapshotPayload.STREAM_CODEC.encode(buffer, payload);
            SoulSnapshotPayload decoded = SoulSnapshotPayload.STREAM_CODEC.decode(buffer);

            assertEquals(payload, decoded);
            assertTrue(decoded.openScreen());
            assertEquals("—", decoded.snapshot().displayedSoulRank());
            assertEquals("—", decoded.snapshot().displayedAspect());
            assertEquals("—", decoded.snapshot().displayedAspectRank());
            assertEquals("—", decoded.snapshot().displayedFlaw());
            assertFalse(decoded.snapshot().importedFromDatapack());
        } finally {
            buffer.release();
        }
    }
}
