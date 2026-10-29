package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.weaving.StrandRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Client intent from the Weaving Table: lay {@code strand} at {@code tier},
 * tuned to {@code restriction} (empty for none), along this trace of the
 * anchor pattern ({@code trace} holds x, y pairs in anchor radii). The server
 * judges the trace and decides everything else.
 */
public record WeaveTracePayload(String strand, int tier, String restriction, List<Float> trace) implements CustomPacketPayload {
    public static final Type<WeaveTracePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "weave_trace")
    );
    public static final StreamCodec<ByteBuf, WeaveTracePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), WeaveTracePayload::strand,
            ByteBufCodecs.VAR_INT, WeaveTracePayload::tier,
            ByteBufCodecs.stringUtf8(32), WeaveTracePayload::restriction,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list(StrandRules.TRACE_LIMIT * 2)), WeaveTracePayload::trace,
            WeaveTracePayload::new);

    public WeaveTracePayload {
        trace = List.copyOf(trace);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
