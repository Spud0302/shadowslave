package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client request from the Soul screen's Name page: show or hide your True Name
 * as a title, or speak a name you know to sense its bearer. The server checks
 * everything again.
 */
public record TrueNameActionPayload(int action, String name) implements CustomPacketPayload {
    public static final int TOGGLE_SHOWN = 0;
    public static final int SENSE = 1;
    public static final Type<TrueNameActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "true_name_action"));
    public static final StreamCodec<ByteBuf, TrueNameActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrueNameActionPayload::action,
            ByteBufCodecs.stringUtf8(512), TrueNameActionPayload::name,
            TrueNameActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
