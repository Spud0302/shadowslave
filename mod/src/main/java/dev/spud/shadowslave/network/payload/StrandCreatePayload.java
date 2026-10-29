package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.weaving.StrandFusion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent from the Weaving Table: fuse learned strands {@code first}
 * and {@code second} into one of the Weaver's own, called {@code name}
 * (empty for the default). The server decides whether it can be made.
 */
public record StrandCreatePayload(String first, String second, String name) implements CustomPacketPayload {
    public static final Type<StrandCreatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "strand_create")
    );
    public static final StreamCodec<ByteBuf, StrandCreatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), StrandCreatePayload::first,
            ByteBufCodecs.stringUtf8(256), StrandCreatePayload::second,
            ByteBufCodecs.stringUtf8(StrandFusion.NAME_LENGTH * 4), StrandCreatePayload::name,
            StrandCreatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
