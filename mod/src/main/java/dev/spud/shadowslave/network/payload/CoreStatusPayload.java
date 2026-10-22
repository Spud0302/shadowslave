package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client reading of the player's soul core: its Soul Rank, how many
 * saturation units it holds and its limit. A limit of 0 means no ranked core.
 */
public record CoreStatusPayload(String rank, int saturation, int limit) implements CustomPacketPayload {
    public static final CoreStatusPayload NONE = new CoreStatusPayload("", 0, 0);
    public static final Type<CoreStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "core_status")
    );
    public static final StreamCodec<ByteBuf, CoreStatusPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), CoreStatusPayload::rank,
            ByteBufCodecs.VAR_INT, CoreStatusPayload::saturation,
            ByteBufCodecs.VAR_INT, CoreStatusPayload::limit,
            CoreStatusPayload::new
    );

    public CoreStatusPayload {
        limit = Math.max(0, limit);
        saturation = Math.max(0, Math.min(limit, saturation));
    }

    public boolean hasCore() {
        return limit > 0;
    }

    public float fraction() {
        return limit <= 0 ? 0.0F : (float) saturation / limit;
    }

    public boolean saturated() {
        return limit > 0 && saturation >= limit;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
