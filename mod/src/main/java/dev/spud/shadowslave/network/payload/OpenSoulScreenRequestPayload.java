package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent only. It carries no Soul values; the server responds from its
 * own authoritative attachment.
 */
public record OpenSoulScreenRequestPayload() implements CustomPacketPayload {
    public static final OpenSoulScreenRequestPayload INSTANCE = new OpenSoulScreenRequestPayload();
    public static final Type<OpenSoulScreenRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "open_soul_screen")
    );
    public static final StreamCodec<ByteBuf, OpenSoulScreenRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
