package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to toggle the implemented Ash Burrower Echo manifestation. */
public record ToggleAshBurrowerEchoRequestPayload() implements CustomPacketPayload {
    public static final ToggleAshBurrowerEchoRequestPayload INSTANCE = new ToggleAshBurrowerEchoRequestPayload();
    public static final Type<ToggleAshBurrowerEchoRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "toggle_ash_burrower_echo")
    );
    public static final StreamCodec<ByteBuf, ToggleAshBurrowerEchoRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
