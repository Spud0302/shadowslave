package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client notice that a player just used an ability, so every nearby client plays its casting pose. */
public record AbilityCastPayload(int entityId, String abilityId) implements CustomPacketPayload {
    public static final Type<AbilityCastPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_cast")
    );

    public static final StreamCodec<ByteBuf, AbilityCastPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbilityCastPayload::entityId,
            ByteBufCodecs.stringUtf8(256), AbilityCastPayload::abilityId,
            AbilityCastPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
