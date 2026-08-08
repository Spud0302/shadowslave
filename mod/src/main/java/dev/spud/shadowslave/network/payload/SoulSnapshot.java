package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Deliberately limited client view of server-owned Soul and revealed identity state. */
public record SoulSnapshot(
        int schemaVersion,
        String spellState,
        String awakeningPath,
        String soulRank,
        String aspectId,
        String aspectName,
        String aspectRank,
        String abilityId,
        String flawId,
        String flawName,
        String flawEffectId,
        boolean importedFromDatapack
) {
    private static final int STATE_LENGTH = 32;
    private static final int NAME_LENGTH = 128;
    private static final int ID_LENGTH = 256;
    private static final StreamCodec<ByteBuf, String> STATE_CODEC = ByteBufCodecs.stringUtf8(STATE_LENGTH);
    private static final StreamCodec<ByteBuf, String> NAME_CODEC = ByteBufCodecs.stringUtf8(NAME_LENGTH);
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
                    NAME_CODEC.decode(buffer),
                    STATE_CODEC.decode(buffer),
                    ID_CODEC.decode(buffer),
                    ID_CODEC.decode(buffer),
                    NAME_CODEC.decode(buffer),
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
            NAME_CODEC.encode(buffer, value.aspectName());
            STATE_CODEC.encode(buffer, value.aspectRank());
            ID_CODEC.encode(buffer, value.abilityId());
            ID_CODEC.encode(buffer, value.flawId());
            NAME_CODEC.encode(buffer, value.flawName());
            ID_CODEC.encode(buffer, value.flawEffectId());
            ByteBufCodecs.BOOL.encode(buffer, value.importedFromDatapack());
        }
    };

    public static SoulSnapshot from(SoulData soul, SoulIdentityData identity) {
        return new SoulSnapshot(
                soul.schemaVersion(),
                soul.spellState().serializedName(),
                soul.awakeningPath().serializedName(),
                soul.soulRank().map(SoulRank::serializedName).orElse(""),
                soul.aspectId().map(ResourceLocation::toString).orElse(""),
                identity.aspect().flatMap(value -> value.formalName()).orElse(""),
                soul.aspectRank().map(SoulRank::serializedName).orElse(""),
                identity.aspect().map(value -> value.abilityId().toString()).orElse(""),
                soul.flawId().map(ResourceLocation::toString).orElse(""),
                identity.flaw().flatMap(value -> value.formalName()).orElse(""),
                identity.flaw().map(value -> value.effectId().toString()).orElse(""),
                soul.importedFromDatapack()
        );
    }

    public String displayedSoulRank() {
        return display(soulRank);
    }

    public String displayedAspect() {
        return aspectName.isBlank() ? display(aspectId) : aspectName;
    }

    public String displayedAspectRank() {
        return display(aspectRank);
    }

    public String displayedAbility() {
        return display(abilityId);
    }

    public String displayedFlaw() {
        return flawName.isBlank() ? display(flawId) : flawName;
    }

    public String displayedFlawEffect() {
        return display(flawEffectId);
    }

    private static String display(String value) {
        return value.isBlank() ? "—" : value;
    }
}
