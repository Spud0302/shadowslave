package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent from the Soul Sea: place a point in a stat ("body", "soul", "aspect"), or "respec". */
public record RefineRequestPayload(String action) implements CustomPacketPayload {
    public static final String RESPEC = "respec";
    public static final Type<RefineRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "refine")
    );
    public static final StreamCodec<ByteBuf, RefineRequestPayload> STREAM_CODEC = ByteBufCodecs.stringUtf8(16)
            .map(RefineRequestPayload::new, RefineRequestPayload::action);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
