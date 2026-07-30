package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Deliberately limited client view of server-owned Soul state.
 *
 * <p>Progression history, migration internals, future resources and mutable
 * ability state are not exposed until a client feature actually needs them.</p>
 */
public record SoulSnapshot(
        int schemaVersion,
        String spellState,
        String soulRank,
        String aspectId,
        String flawId,
        boolean importedFromDatapack
) {
    private static final int STATE_LENGTH = 32;
    private static final int ID_LENGTH = 256;

    public static final StreamCodec<ByteBuf, SoulSnapshot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SoulSnapshot::schemaVersion,
            ByteBufCodecs.stringUtf8(STATE_LENGTH),
            SoulSnapshot::spellState,
            ByteBufCodecs.stringUtf8(STATE_LENGTH),
            SoulSnapshot::soulRank,
            ByteBufCodecs.stringUtf8(ID_LENGTH),
            SoulSnapshot::aspectId,
            ByteBufCodecs.stringUtf8(ID_LENGTH),
            SoulSnapshot::flawId,
            ByteBufCodecs.BOOL,
            SoulSnapshot::importedFromDatapack,
            SoulSnapshot::new
    );

    public static SoulSnapshot from(SoulData soul) {
        return new SoulSnapshot(
                soul.schemaVersion(),
                soul.spellState().serializedName(),
                soul.soulRank().serializedName(),
                soul.aspectId().map(ResourceLocation::toString).orElse(""),
                soul.flawId().map(ResourceLocation::toString).orElse(""),
                soul.importedFromDatapack()
        );
    }

    public String displayedAspect() {
        return aspectId.isBlank() ? "—" : aspectId;
    }

    public String displayedFlaw() {
        return flawId.isBlank() ? "—" : flawId;
    }
}
