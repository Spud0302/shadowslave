package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client intent to use the Aspect ability on one key: slot 0 is the Dormant ability, slot 1 the Awakened one. */
public record UseAbilityRequestPayload(int slot) implements CustomPacketPayload {
    public static final Type<UseAbilityRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "use_ability")
    );
    public static final StreamCodec<ByteBuf, UseAbilityRequestPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(UseAbilityRequestPayload::new, UseAbilityRequestPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
