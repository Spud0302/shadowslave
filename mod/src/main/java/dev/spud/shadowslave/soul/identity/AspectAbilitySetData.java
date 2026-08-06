package dev.spud.shadowslave.soul.identity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Ordered, provider-independent abilities belonging to one persistent Aspect identity. */
public record AspectAbilitySetData(List<AspectAbilityData> abilities) {
    public static final Codec<AspectAbilitySetData> CODEC = AspectAbilityData.CODEC.codec()
            .listOf()
            .fieldOf("abilities")
            .flatXmap(AspectAbilitySetData::construct, data -> DataResult.success(data.abilities()))
            .codec();

    public AspectAbilitySetData {
        abilities = List.copyOf(Objects.requireNonNull(abilities, "abilities"));
        Set<ResourceLocation> seen = new LinkedHashSet<>();
        for (AspectAbilityData ability : abilities) {
            AspectAbilityData checked = Objects.requireNonNull(ability, "ability");
            if (!seen.add(checked.abilityId())) {
                throw new IllegalArgumentException("Duplicate Aspect ability ID: " + checked.abilityId());
            }
        }
    }

    public static AspectAbilitySetData empty() {
        return new AspectAbilitySetData(List.of());
    }

    public boolean contains(ResourceLocation abilityId) {
        ResourceLocation checked = Objects.requireNonNull(abilityId, "abilityId");
        return abilities.stream().anyMatch(ability -> ability.abilityId().equals(checked));
    }

    private static DataResult<AspectAbilitySetData> construct(List<AspectAbilityData> abilities) {
        try {
            return DataResult.success(new AspectAbilitySetData(abilities));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return DataResult.error(() -> "Invalid AspectAbilitySetData: " + exception.getMessage());
        }
    }
}
