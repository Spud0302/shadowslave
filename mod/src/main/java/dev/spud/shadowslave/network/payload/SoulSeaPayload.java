package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * What a player sees on meditating at the Dream Candle: their core, the next
 * rank and what it needs, and the abilities they can choose on rising.
 * {@code open} asks the client to open the Soul Sea; otherwise it only
 * refreshes a Soul Sea that is already open.
 */
public record SoulSeaPayload(CoreStatusPayload core, String nextRank, String requirement, boolean ready,
                             List<Choice> choices, boolean open) implements CustomPacketPayload {
    public static final int MAX_CHOICES = 4;
    public static final Type<SoulSeaPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "soul_sea")
    );

    public record Choice(String abilityId, String name, String role, String description) {
        public static final StreamCodec<ByteBuf, Choice> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(256), Choice::abilityId,
                ByteBufCodecs.stringUtf8(64), Choice::name,
                ByteBufCodecs.stringUtf8(32), Choice::role,
                ByteBufCodecs.stringUtf8(512), Choice::description,
                Choice::new
        );
    }

    public static final StreamCodec<ByteBuf, SoulSeaPayload> STREAM_CODEC = StreamCodec.composite(
            CoreStatusPayload.STREAM_CODEC, SoulSeaPayload::core,
            ByteBufCodecs.stringUtf8(32), SoulSeaPayload::nextRank,
            ByteBufCodecs.stringUtf8(256), SoulSeaPayload::requirement,
            ByteBufCodecs.BOOL, SoulSeaPayload::ready,
            Choice.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CHOICES)), SoulSeaPayload::choices,
            ByteBufCodecs.BOOL, SoulSeaPayload::open,
            SoulSeaPayload::new
    );

    public SoulSeaPayload {
        choices = List.copyOf(choices.subList(0, Math.min(MAX_CHOICES, choices.size())));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
