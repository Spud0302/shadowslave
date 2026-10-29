package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent from the Soul screen's Memories page: summon the Memory at
 * {@code index} in the soul's line. The server decides whether it comes.
 */
public record MemorySummonPayload(int index) implements CustomPacketPayload {
    public static final Type<MemorySummonPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory_summon")
    );
    public static final StreamCodec<ByteBuf, MemorySummonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MemorySummonPayload::index,
            MemorySummonPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
