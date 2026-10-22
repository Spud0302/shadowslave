package dev.spud.shadowslave.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

/**
 * What a soul shard torn from a player carries: the saturation it took from
 * their core and whose it was. Mob shards have no contents and fill by rank.
 */
public record ShardContents(int units, String origin) {
    public static final Codec<ShardContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("units").forGetter(ShardContents::units),
            Codec.STRING.fieldOf("origin").forGetter(ShardContents::origin)
    ).apply(instance, ShardContents::new));

    public static final StreamCodec<ByteBuf, ShardContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ShardContents::units,
            ByteBufCodecs.stringUtf8(64), ShardContents::origin,
            ShardContents::new
    );

    public ShardContents {
        units = Math.max(1, units);
        origin = Objects.requireNonNull(origin, "origin");
    }
}
