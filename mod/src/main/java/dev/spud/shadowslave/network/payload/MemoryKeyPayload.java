package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent from the Memory key: dismiss the held Memory or summon one,
 * or with {@code cycle} (sneaking) turn which dismissed Memory comes next.
 * The server decides what happens.
 */
public record MemoryKeyPayload(boolean cycle) implements CustomPacketPayload {
    public static final Type<MemoryKeyPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "memory_key")
    );
    public static final StreamCodec<ByteBuf, MemoryKeyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MemoryKeyPayload::cycle,
            MemoryKeyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
