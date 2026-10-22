package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to use the player's Dormant Aspect ability, if they have one. */
public record UseAbilityRequestPayload() implements CustomPacketPayload {
    public static final UseAbilityRequestPayload INSTANCE = new UseAbilityRequestPayload();
    public static final Type<UseAbilityRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "use_ability")
    );
    public static final StreamCodec<ByteBuf, UseAbilityRequestPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
