package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Deliberately limited client view of server-owned Soul state.
 *
 * <p>Nightmare history, migration internals, evidence, resources and mutable
 * ability state are not exposed until a client feature actually needs them.</p>
 */
public record SoulSnapshot(
        int schemaVersion,
        String spellState,
        String awakeningPath,
        String soulRank,
        String aspectId,
        String aspectRank,
        String flawId,
        boolean importedFromDatapack
) {
    private static final int STATE_LENGTH = 32;
    private static final int ID_LENGTH = 256;
    private static final StreamCodec<ByteBuf, String> STATE_CODEC = ByteBufCodecs.stringUtf8(STATE_LENGTH);
    private static final StreamCodec<ByteBuf, String> ID_CODEC = ByteBufCodecs.stringUtf8(ID_LENGTH);

    public static final StreamCodec<ByteBuf, SoulSnapshot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SoulSnapshot decode(ByteBuf buffer) {
            return new SoulSnapshot(
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    STATE_CODEC.decode(buffer),
                    STATE_CODEC.decode(buffer),
                    STATE_CODEC.decode(buffer),
                    ID_CODEC.decode(buffer),
                    STATE_CODEC.decode(buffer),
                    ID_CODEC.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer)
            );
        }

        @Override
        public void encode(ByteBuf buffer, SoulSnapshot value) {
            ByteBufCodecs.VAR_INT.encode(buffer, value.schemaVersion());
            STATE_CODEC.encode(buffer, value.spellState());
            STATE_CODEC.encode(buffer, value.awakeningPath());
            STATE_CODEC.encode(buffer, value.soulRank());
            ID_CODEC.encode(buffer, value.aspectId());
            STATE_CODEC.encode(buffer, value.aspectRank());
            ID_CODEC.encode(buffer, value.flawId());
            ByteBufCodecs.BOOL.encode(buffer, value.importedFromDatapack());
        }
    };

    public static SoulSnapshot from(SoulData soul) {
        return new SoulSnapshot(
                soul.schemaVersion(),
                soul.spellState().serializedName(),
                soul.awakeningPath().serializedName(),
                soul.soulRank().map(SoulRank::serializedName).orElse(""),
                soul.aspectId().map(ResourceLocation::toString).orElse(""),
                soul.aspectRank().map(SoulRank::serializedName).orElse(""),
                soul.flawId().map(ResourceLocation::toString).orElse(""),
                soul.importedFromDatapack()
        );
    }

    public String displayedSoulRank() {
        return soulRank.isBlank() ? "—" : soulRank;
    }

    public String displayedAspect() {
        return aspectId.isBlank() ? "—" : aspectId;
    }

    public String displayedAspectRank() {
        return aspectRank.isBlank() ? "—" : aspectRank;
    }

    public String displayedFlaw() {
        return flawId.isBlank() ? "—" : flawId;
    }
}
