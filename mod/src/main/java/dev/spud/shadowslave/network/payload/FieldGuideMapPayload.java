package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Explored chunks of one dimension for the Field Guide map: each chunk's
 * position and its sampled vanilla map colours, {@code bytesPerChunk} bytes
 * each. {@code reset} means this is the whole map for that dimension rather
 * than an update.
 */
public record FieldGuideMapPayload(String dimension, boolean reset, long[] chunks, byte[] colours)
        implements CustomPacketPayload {
    public static final int MAX_CHUNKS_PER_PACKET = 16384;

    public static final Type<FieldGuideMapPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_map")
    );
    public static final StreamCodec<ByteBuf, FieldGuideMapPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                FriendlyByteBuf out = new FriendlyByteBuf(buffer);
                out.writeUtf(payload.dimension, 256);
                out.writeBoolean(payload.reset);
                out.writeLongArray(payload.chunks);
                out.writeByteArray(payload.colours);
            },
            buffer -> {
                FriendlyByteBuf in = new FriendlyByteBuf(buffer);
                String dimension = in.readUtf(256);
                boolean reset = in.readBoolean();
                long[] chunks = in.readLongArray(null, MAX_CHUNKS_PER_PACKET);
                byte[] colours = in.readByteArray(MAX_CHUNKS_PER_PACKET * 16);
                return new FieldGuideMapPayload(dimension, reset, chunks, colours);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
