package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client notice that a player wears a borrowed face (an entity type id), or none ("") any more. */
public record DisguisePayload(int entityId, String entityType) implements CustomPacketPayload {
    public static final Type<DisguisePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "disguise")
    );

    public static final StreamCodec<ByteBuf, DisguisePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DisguisePayload::entityId,
            ByteBufCodecs.stringUtf8(256), DisguisePayload::entityType,
            DisguisePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
