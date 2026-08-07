package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Deliberately limited client view of server-owned Soul and revealed identity state. */
public record SoulSnapshot(
        int schemaVersion,
        String spellState,
        String awakeningPath,
        String soulRank,
        String aspectId,
        String aspectName,
        String aspectRank,
        List<String> abilityIds,
        String flawId,
        String flawName,
        String flawEffectId,
        boolean importedFromDatapack
) {
    private static final int STATE_LENGTH = 32;
    private static final int NAME_LENGTH = 128;
    private static final int ID_LENGTH = 256;
    private static final int MAX_ABILITIES = 64;
    private static final StreamCodec<ByteBuf, String> STATE_CODEC = ByteBufCodecs.stringUtf8(STATE_LENGTH);
    private static final StreamCodec<ByteBuf, String> NAME_CODEC = ByteBufCodecs.stringUtf8(NAME_LENGTH);
    private static final StreamCodec<ByteBuf, String> ID_CODEC = ByteBufCodecs.stringUtf8(ID_LENGTH);

    public SoulSnapshot {
        abilityIds = validateAbilityIds(abilityIds);
    }

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
                    decodeAbilityIds(buffer),
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
            encodeAbilityIds(buffer, value.abilityIds());
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
                identity.aspect().map(value -> value.formalName()).orElse(""),
                soul.aspectRank().map(SoulRank::serializedName).orElse(""),
                identity.aspect()
                        .map(value -> value.abilitySet().abilities().stream()
                                .map(ability -> ability.abilityId().toString())
                                .toList())
                        .orElseGet(List::of),
                soul.flawId().map(ResourceLocation::toString).orElse(""),
                identity.flaw().map(value -> value.formalName()).orElse(""),
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

    public String displayedAbilities() {
        return abilityIds.isEmpty() ? "—" : String.join(", ", abilityIds);
    }

    /** Compatibility name for the current Soul screen; renders the complete set rather than one privileged entry. */
    public String displayedAbility() {
        return displayedAbilities();
    }

    public String displayedFlaw() {
        return flawName.isBlank() ? display(flawId) : flawName;
    }

    public String displayedFlawEffect() {
        return display(flawEffectId);
    }

    private static List<String> decodeAbilityIds(ByteBuf buffer) {
        int count = ByteBufCodecs.VAR_INT.decode(buffer);
        if (count < 0 || count > MAX_ABILITIES) {
            throw new IllegalArgumentException("Invalid Aspect ability count in SoulSnapshot: " + count);
        }
        List<String> ids = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            ids.add(ID_CODEC.decode(buffer));
        }
        return ids;
    }

    private static void encodeAbilityIds(ByteBuf buffer, List<String> ids) {
        List<String> checked = validateAbilityIds(ids);
        ByteBufCodecs.VAR_INT.encode(buffer, checked.size());
        checked.forEach(id -> ID_CODEC.encode(buffer, id));
    }

    private static List<String> validateAbilityIds(List<String> ids) {
        List<String> checked = List.copyOf(Objects.requireNonNull(ids, "abilityIds"));
        if (checked.size() > MAX_ABILITIES) {
            throw new IllegalArgumentException("SoulSnapshot cannot contain more than " + MAX_ABILITIES + " Aspect abilities");
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String id : checked) {
            String value = Objects.requireNonNull(id, "abilityId");
            if (value.isBlank()) {
                throw new IllegalArgumentException("SoulSnapshot Aspect ability IDs cannot be blank");
            }
            if (!seen.add(value)) {
                throw new IllegalArgumentException("Duplicate SoulSnapshot Aspect ability ID: " + value);
            }
        }
        return checked;
    }

    private static String display(String value) {
        return value.isBlank() ? "—" : value;
    }
}
