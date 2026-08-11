package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to switch the implemented Ash Burrower Echo between Follow and Hold. */
public record ToggleAshBurrowerEchoModeRequestPayload() implements CustomPacketPayload {
    public static final ToggleAshBurrowerEchoModeRequestPayload INSTANCE = new ToggleAshBurrowerEchoModeRequestPayload();
    public static final Type<ToggleAshBurrowerEchoModeRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "toggle_ash_burrower_echo_mode")
    );
    public static final StreamCodec<ByteBuf, ToggleAshBurrowerEchoModeRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
