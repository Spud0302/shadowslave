package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to activate the player's server-owned Kindle ability, if available. */
public record ActivateKindleRequestPayload() implements CustomPacketPayload {
    public static final ActivateKindleRequestPayload INSTANCE = new ActivateKindleRequestPayload();
    public static final Type<ActivateKindleRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "activate_kindle")
    );
    public static final StreamCodec<ByteBuf, ActivateKindleRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
