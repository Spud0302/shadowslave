package dev.spud.shadowslave.gate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** What a Gate Lure holds, carried on its item when it is broken so no fed shard is lost. */
public record LureCharge(int pullQuarters, int shards) {
    public static final LureCharge EMPTY = new LureCharge(0, 0);
    public static final Codec<LureCharge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("pull_quarters").forGetter(LureCharge::pullQuarters),
            Codec.INT.fieldOf("shards").forGetter(LureCharge::shards)
    ).apply(instance, LureCharge::new));
    public static final StreamCodec<ByteBuf, LureCharge> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LureCharge::pullQuarters,
            ByteBufCodecs.VAR_INT, LureCharge::shards,
            LureCharge::new);

    /** Pull in whole points, as the forecast reads it. */
    public int pull() {
        return pullQuarters / 4;
    }
}
