package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to channel essence: a leap, or with {@code strike} a heavy next blow. */
public record ChannelRequestPayload(boolean strike) implements CustomPacketPayload {
    public static final Type<ChannelRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "channel")
    );
    public static final StreamCodec<ByteBuf, ChannelRequestPayload> STREAM_CODEC = ByteBufCodecs.BOOL
            .map(ChannelRequestPayload::new, ChannelRequestPayload::strike);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
