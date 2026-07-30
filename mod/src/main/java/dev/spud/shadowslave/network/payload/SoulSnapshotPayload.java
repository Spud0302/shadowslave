package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client Soul snapshot, optionally requesting the Soul screen open. */
public record SoulSnapshotPayload(SoulSnapshot snapshot, boolean openScreen) implements CustomPacketPayload {
    public static final Type<SoulSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "soul_snapshot")
    );

    public static final StreamCodec<ByteBuf, SoulSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            SoulSnapshot.STREAM_CODEC,
            SoulSnapshotPayload::snapshot,
            ByteBufCodecs.BOOL,
            SoulSnapshotPayload::openScreen,
            SoulSnapshotPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
