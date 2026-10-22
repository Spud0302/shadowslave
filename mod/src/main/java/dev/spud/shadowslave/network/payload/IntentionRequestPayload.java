package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent from the Soul Sea before the roll: name a longing and offer what is in hand. */
public record IntentionRequestPayload(String longing) implements CustomPacketPayload {
    public static final Type<IntentionRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "intention")
    );
    public static final StreamCodec<ByteBuf, IntentionRequestPayload> STREAM_CODEC = ByteBufCodecs.stringUtf8(16)
            .map(IntentionRequestPayload::new, IntentionRequestPayload::longing);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
