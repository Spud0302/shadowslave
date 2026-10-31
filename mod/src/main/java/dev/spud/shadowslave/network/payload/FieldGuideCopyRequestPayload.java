package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent from the Field Guide: copy a page onto a Field Note.
 * {@code kind} is creature (with the creature's id as {@code target}), or
 * place, chart, path or danger at a spot in a dimension.
 */
public record FieldGuideCopyRequestPayload(String kind, String target, String dimension, int x, int z)
        implements CustomPacketPayload {
    public static final Type<FieldGuideCopyRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_copy")
    );
    public static final StreamCodec<ByteBuf, FieldGuideCopyRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16), FieldGuideCopyRequestPayload::kind,
            ByteBufCodecs.stringUtf8(256), FieldGuideCopyRequestPayload::target,
            ByteBufCodecs.stringUtf8(256), FieldGuideCopyRequestPayload::dimension,
            ByteBufCodecs.VAR_INT, FieldGuideCopyRequestPayload::x,
            ByteBufCodecs.VAR_INT, FieldGuideCopyRequestPayload::z,
            FieldGuideCopyRequestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
