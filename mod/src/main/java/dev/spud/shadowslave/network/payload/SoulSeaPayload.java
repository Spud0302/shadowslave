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
 * rank and what it needs, the abilities they can choose on rising, and their
 * refinement points. {@code open} asks the client to open the Soul Sea;
 * otherwise it only refreshes a Soul Sea that is already open.
 */
public record SoulSeaPayload(CoreStatusPayload core, String nextRank, String requirement, boolean ready,
                             List<Choice> choices, Refinement refinement, boolean open) implements CustomPacketPayload {
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

    /** Points placed in Body, Soul and Aspect, points left to place, and whether a respec is possible. */
    public record Refinement(int unspent, int body, int soul, int aspect, boolean canRespec) {
        public static final Refinement NONE = new Refinement(0, 0, 0, 0, false);
        public static final StreamCodec<ByteBuf, Refinement> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Refinement::unspent,
                ByteBufCodecs.VAR_INT, Refinement::body,
                ByteBufCodecs.VAR_INT, Refinement::soul,
                ByteBufCodecs.VAR_INT, Refinement::aspect,
                ByteBufCodecs.BOOL, Refinement::canRespec,
                Refinement::new
        );

        public int placed() {
            return body + soul + aspect;
        }
    }

    private static final StreamCodec<ByteBuf, List<Choice>> CHOICES = Choice.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CHOICES));

    public static final StreamCodec<ByteBuf, SoulSeaPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                CoreStatusPayload.STREAM_CODEC.encode(buffer, value.core());
                ByteBufCodecs.stringUtf8(32).encode(buffer, value.nextRank());
                ByteBufCodecs.stringUtf8(256).encode(buffer, value.requirement());
                ByteBufCodecs.BOOL.encode(buffer, value.ready());
                CHOICES.encode(buffer, value.choices());
                Refinement.STREAM_CODEC.encode(buffer, value.refinement());
                ByteBufCodecs.BOOL.encode(buffer, value.open());
            },
            buffer -> new SoulSeaPayload(
                    CoreStatusPayload.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.stringUtf8(32).decode(buffer),
                    ByteBufCodecs.stringUtf8(256).decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    CHOICES.decode(buffer),
                    Refinement.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer)
            )
    );

    public SoulSeaPayload {
        choices = List.copyOf(choices.subList(0, Math.min(MAX_CHOICES, choices.size())));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
