package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client option: how often the player wants the Spell to call (a {@code CallingFrequency} ordinal). */
public record CallingFrequencyPayload(int frequency) implements CustomPacketPayload {
    public static final Type<CallingFrequencyPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "calling_frequency"));
    public static final StreamCodec<ByteBuf, CallingFrequencyPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(CallingFrequencyPayload::new, CallingFrequencyPayload::frequency);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
