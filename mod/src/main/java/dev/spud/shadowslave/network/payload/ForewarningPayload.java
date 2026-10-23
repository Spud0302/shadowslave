package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client flash of a Seer's Forewarning: the creature with this
 * entity id is winding up a big attack that lands in about {@code ticks}.
 * {@code shared} is true when it reached this player through Shared Sight.
 */
public record ForewarningPayload(int attackerId, int ticks, boolean shared) implements CustomPacketPayload {
    public static final Type<ForewarningPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "forewarning")
    );

    public static final StreamCodec<ByteBuf, ForewarningPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ForewarningPayload::attackerId,
            ByteBufCodecs.VAR_INT, ForewarningPayload::ticks,
            ByteBufCodecs.BOOL, ForewarningPayload::shared,
            ForewarningPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
