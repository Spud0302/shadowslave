package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to toggle the implemented Ash Compass Memory manifestation. */
public record ToggleAshCompassRequestPayload() implements CustomPacketPayload {
    public static final ToggleAshCompassRequestPayload INSTANCE = new ToggleAshCompassRequestPayload();
    public static final Type<ToggleAshCompassRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "toggle_ash_compass")
    );
    public static final StreamCodec<ByteBuf, ToggleAshCompassRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
