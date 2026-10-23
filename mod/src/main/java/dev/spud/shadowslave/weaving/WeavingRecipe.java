package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * One Weaving pattern, read from {@code data/<ns>/weaving/recipe/<name>.json}
 * (DESIGN). The materials are shapeless and exact: the table must hold these
 * items in these amounts and nothing else. {@code memory} names the profile in
 * the Memory catalogue, whose Rank and Tier set what the pattern needs;
 * {@code result} is the item it weaves.
 */
public record WeavingRecipe(
        ResourceLocation branch,
        ResourceLocation memory,
        ResourceLocation result,
        List<Material> materials,
        int xp,
        Unlock unlock,
        String hint
) {
    public static final Codec<WeavingRecipe> CODEC = RecordCodecBuilder.<WeavingRecipe>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("branch").forGetter(WeavingRecipe::branch),
            ResourceLocation.CODEC.fieldOf("memory").forGetter(WeavingRecipe::memory),
            ResourceLocation.CODEC.fieldOf("result").forGetter(WeavingRecipe::result),
            Material.CODEC.listOf().fieldOf("materials").forGetter(WeavingRecipe::materials),
            Codec.intRange(0, 10_000).optionalFieldOf("xp", 10).forGetter(WeavingRecipe::xp),
            Unlock.CODEC.fieldOf("unlock").forGetter(WeavingRecipe::unlock),
            Codec.STRING.optionalFieldOf("hint", "").forGetter(WeavingRecipe::hint)
    ).apply(instance, WeavingRecipe::new)).validate(WeavingRecipe::validate);

    public WeavingRecipe {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(result, "result");
        materials = List.copyOf(materials);
        Objects.requireNonNull(unlock, "unlock");
        hint = Objects.requireNonNull(hint, "hint");
    }

    private static DataResult<WeavingRecipe> validate(WeavingRecipe recipe) {
        if (recipe.materials.isEmpty() || recipe.materials.size() > WeavingRules.MATERIAL_SLOTS) {
            return DataResult.error(() -> "A pattern needs 1 to " + WeavingRules.MATERIAL_SLOTS + " materials");
        }
        HashSet<ResourceLocation> seen = new HashSet<>();
        for (Material material : recipe.materials) {
            if (!seen.add(material.item())) {
                return DataResult.error(() -> "Material listed twice: " + material.item());
            }
        }
        return DataResult.success(recipe);
    }

    /** Item id to amount, in the order the file lists them. */
    public Map<ResourceLocation, Integer> amounts() {
        LinkedHashMap<ResourceLocation, Integer> amounts = new LinkedHashMap<>();
        materials.forEach(material -> amounts.put(material.item(), material.count()));
        return amounts;
    }

    public record Material(ResourceLocation item, int count) {
        public static final Codec<Material> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Material::item),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Material::count)
        ).apply(instance, Material::new));
    }

    /**
     * How a player comes to know the pattern: everyone starts with
     * {@code starter} patterns, {@code discover} patterns are found by trial at
     * the table, and {@code first_kill} patterns are granted by the Spell the
     * first time the player slays {@code entity}.
     */
    public record Unlock(Kind kind, Optional<ResourceLocation> entity) {
        public static final Codec<Unlock> CODEC = RecordCodecBuilder.<Unlock>create(instance -> instance.group(
                Kind.CODEC.fieldOf("type").forGetter(Unlock::kind),
                ResourceLocation.CODEC.optionalFieldOf("entity").forGetter(Unlock::entity)
        ).apply(instance, Unlock::new)).validate(unlock -> unlock.kind == Kind.FIRST_KILL && unlock.entity.isEmpty()
                ? DataResult.error(() -> "A first_kill unlock names its entity")
                : DataResult.success(unlock));

        public static Unlock starter() {
            return new Unlock(Kind.STARTER, Optional.empty());
        }

        public static Unlock discover() {
            return new Unlock(Kind.DISCOVER, Optional.empty());
        }

        public static Unlock firstKill(ResourceLocation entity) {
            return new Unlock(Kind.FIRST_KILL, Optional.of(entity));
        }

        public enum Kind {
            STARTER, DISCOVER, FIRST_KILL;

            public static final Codec<Kind> CODEC = Codec.STRING.comapFlatMap(
                    value -> {
                        for (Kind kind : values()) {
                            if (kind.name().toLowerCase(Locale.ROOT).equals(value)) {
                                return DataResult.success(kind);
                            }
                        }
                        return DataResult.error(() -> "Unknown unlock type: " + value);
                    },
                    kind -> kind.name().toLowerCase(Locale.ROOT));
        }
    }
}
