package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent from the Field Guide map: {@code action} is place, warning or
 * remove, at a spot in a dimension, or trace, which turns path tracing on or
 * off (the spot is then unused).
 */
public record FieldGuidePinRequestPayload(String action, String dimension, int x, int z, String label)
        implements CustomPacketPayload {
    public static final String PLACE = "place";
    public static final String WARNING = "warning";
    public static final String REMOVE = "remove";
    public static final String TRACE = "trace";
    public static final int MAX_LABEL = 32;

    public static final Type<FieldGuidePinRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_pin")
    );
    public static final StreamCodec<ByteBuf, FieldGuidePinRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16), FieldGuidePinRequestPayload::action,
            ByteBufCodecs.stringUtf8(256), FieldGuidePinRequestPayload::dimension,
            ByteBufCodecs.VAR_INT, FieldGuidePinRequestPayload::x,
            ByteBufCodecs.VAR_INT, FieldGuidePinRequestPayload::z,
            ByteBufCodecs.stringUtf8(MAX_LABEL * 4), FieldGuidePinRequestPayload::label,
            FieldGuidePinRequestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
