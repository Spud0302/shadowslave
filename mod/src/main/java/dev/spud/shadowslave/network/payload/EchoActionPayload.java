package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client intent for Echoes: an order from the command wheel ({@code follow},
 * {@code stay}, {@code guard}, {@code attack}), the Echo key's {@code call},
 * or a Soul screen action on the Echo at {@code index} ({@code summon},
 * {@code dismiss}, {@code draw}). The server decides what happens.
 */
public record EchoActionPayload(String action, int index) implements CustomPacketPayload {
    public static final String CALL = "call";
    public static final String SUMMON = "summon";
    public static final String DISMISS = "dismiss";
    public static final String DRAW = "draw";

    public static final Type<EchoActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo_action"));
    public static final StreamCodec<ByteBuf, EchoActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EchoActionPayload::action,
            ByteBufCodecs.VAR_INT, EchoActionPayload::index,
            EchoActionPayload::new);

    public static EchoActionPayload of(String action) {
        return new EchoActionPayload(action, -1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
