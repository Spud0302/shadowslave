package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AspectRevealPayloadTest {
    @Test
    void revealCarriesTheRolledKitWords() {
        AspectRoller.Roll roll = AspectRoller.roll(7L,
                PlaystyleEvidence.fromTotals(new PlaystyleEvidence.Totals(0, 0, 0, 0)));
        AspectRevealPayload payload = AspectRollService.revealPayload(roll);
        AspectKit kit = roll.kit();

        assertEquals(roll.aspectName(), payload.aspectName());
        assertEquals("Dormant", payload.rankName());
        assertEquals(kit.natureName(), payload.natureName());
        assertEquals(kit.innateName(), payload.innateName());
        assertEquals(kit.abilityDescription(), payload.abilityDescription());
        assertEquals(kit.flawName(), payload.flawName());
        assertEquals(kit.flawDescription(), payload.flawDescription());
    }

    @Test
    void payloadRoundTripsThroughItsCodec() {
        AspectRevealPayload payload = new AspectRevealPayload("Lantern of Ash", "Dormant", "Ember",
                "Ember's Warmth", "a", "Kindle", "b", "Cold Ash", "c");
        ByteBuf buffer = Unpooled.buffer();
        AspectRevealPayload.STREAM_CODEC.encode(buffer, payload);

        assertEquals(payload, AspectRevealPayload.STREAM_CODEC.decode(buffer));
    }
}
