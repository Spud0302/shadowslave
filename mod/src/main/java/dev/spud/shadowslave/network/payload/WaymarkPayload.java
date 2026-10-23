package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server-to-client: the Waymarks a Wayfarer (entity {@code ownerId}) has set
 * in the viewer's dimension, so the viewer can see them through walls. Sent
 * about once a second while the viewer is near; an empty list clears them.
 */
public record WaymarkPayload(int ownerId, List<BlockPos> marks) implements CustomPacketPayload {
    public static final Type<WaymarkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "waymarks")
    );

    public static final StreamCodec<ByteBuf, WaymarkPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WaymarkPayload::ownerId,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(8)), WaymarkPayload::marks,
            WaymarkPayload::new
    );

    public WaymarkPayload {
        marks = List.copyOf(marks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
