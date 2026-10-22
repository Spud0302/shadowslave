package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server-to-client words of a fresh Aspect roll, for the typed-out Spell
 * reveal. Display text only: the authoritative identity arrives separately in
 * the Soul snapshot.
 */
public record AspectRevealPayload(
        String aspectName,
        String rankName,
        String natureName,
        String innateName,
        String innateDescription,
        String abilityName,
        String abilityDescription,
        String flawName,
        String flawDescription
) implements CustomPacketPayload {
    public static final Type<AspectRevealPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "aspect_reveal")
    );

    private static final StreamCodec<ByteBuf, List<String>> FIELDS =
            ByteBufCodecs.stringUtf8(512).apply(ByteBufCodecs.list(9));

    public static final StreamCodec<ByteBuf, AspectRevealPayload> STREAM_CODEC = FIELDS.map(
            AspectRevealPayload::fromFields,
            AspectRevealPayload::fields
    );

    private static AspectRevealPayload fromFields(List<String> fields) {
        if (fields.size() != 9) {
            throw new IllegalArgumentException("Aspect reveal needs 9 fields, got " + fields.size());
        }
        return new AspectRevealPayload(fields.get(0), fields.get(1), fields.get(2), fields.get(3),
                fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8));
    }

    private List<String> fields() {
        return List.of(aspectName, rankName, natureName, innateName, innateDescription,
                abilityName, abilityDescription, flawName, flawDescription);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
