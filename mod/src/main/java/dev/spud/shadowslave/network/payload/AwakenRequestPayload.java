package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to Awaken, choosing one ability from the Soul Sea. The server checks everything. */
public record AwakenRequestPayload(String abilityId) implements CustomPacketPayload {
    public static final Type<AwakenRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "awaken")
    );
    public static final StreamCodec<ByteBuf, AwakenRequestPayload> STREAM_CODEC = ByteBufCodecs.stringUtf8(256)
            .map(AwakenRequestPayload::new, AwakenRequestPayload::abilityId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
