package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Traced path cells of one dimension for the Field Guide map: each cell's key
 * (see {@code DangerRating.key}) and how it shows, {@link #FAINT} for a route
 * in the guide or {@link #ESTABLISHED} for an established path. {@code cell}
 * is the width of a cell in blocks; {@code reset} means this is every path
 * cell for that dimension rather than an update.
 */
public record FieldGuidePathPayload(String dimension, boolean reset, int cell, long[] cells, byte[] strength)
        implements CustomPacketPayload {
    public static final byte FAINT = 1;
    public static final byte ESTABLISHED = 2;
    public static final int MAX_CELLS_PER_PACKET = 32768;

    public static final Type<FieldGuidePathPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_paths")
    );
    public static final StreamCodec<ByteBuf, FieldGuidePathPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                FriendlyByteBuf out = new FriendlyByteBuf(buffer);
                out.writeUtf(payload.dimension, 256);
                out.writeBoolean(payload.reset);
                out.writeVarInt(payload.cell);
                out.writeLongArray(payload.cells);
                out.writeByteArray(payload.strength);
            },
            buffer -> {
                FriendlyByteBuf in = new FriendlyByteBuf(buffer);
                String dimension = in.readUtf(256);
                boolean reset = in.readBoolean();
                int cell = in.readVarInt();
                long[] cells = in.readLongArray(null, MAX_CELLS_PER_PACKET);
                byte[] strength = in.readByteArray(MAX_CELLS_PER_PACKET);
                return new FieldGuidePathPayload(dimension, reset, cell, cells, strength);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
