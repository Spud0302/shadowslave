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
 * One Smithing pattern, read from {@code data/<ns>/smithing/pattern/<name>.json}
 * (DESIGN). The materials are shapeless and exact: the Forge Hearth's crucible
 * must hold these items in these amounts and nothing else. {@code memory}, if
 * given, names the profile in the Memory catalogue, whose Rank and Tier set
 * what the pattern needs; a plain pattern (an iron sword) gives its own
 * {@code name} and {@code tier} instead. {@code result} is the item it makes
 * and {@code forging} how the billet is worked.
 */
public record WeavingRecipe(
        ResourceLocation branch,
        Optional<ResourceLocation> memory,
        Optional<String> name,
        int tier,
        ResourceLocation result,
        List<Material> materials,
        int xp,
        Unlock unlock,
        String hint,
        Forging forging
) {
    public static final Codec<WeavingRecipe> CODEC = RecordCodecBuilder.<WeavingRecipe>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("branch").forGetter(WeavingRecipe::branch),
            ResourceLocation.CODEC.optionalFieldOf("memory").forGetter(WeavingRecipe::memory),
            Codec.STRING.optionalFieldOf("name").forGetter(WeavingRecipe::name),
            Codec.intRange(1, 7).optionalFieldOf("tier", 1).forGetter(WeavingRecipe::tier),
            ResourceLocation.CODEC.fieldOf("result").forGetter(WeavingRecipe::result),
            Material.CODEC.listOf().fieldOf("materials").forGetter(WeavingRecipe::materials),
            Codec.intRange(0, 10_000).optionalFieldOf("xp", 10).forGetter(WeavingRecipe::xp),
            Unlock.CODEC.fieldOf("unlock").forGetter(WeavingRecipe::unlock),
            Codec.STRING.optionalFieldOf("hint", "").forGetter(WeavingRecipe::hint),
            Forging.CODEC.optionalFieldOf("forging", Forging.DEFAULT).forGetter(WeavingRecipe::forging)
    ).apply(instance, WeavingRecipe::new)).validate(WeavingRecipe::validate);

    public WeavingRecipe {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(result, "result");
        materials = List.copyOf(materials);
        Objects.requireNonNull(unlock, "unlock");
        hint = Objects.requireNonNull(hint, "hint");
        Objects.requireNonNull(forging, "forging");
    }

    private static DataResult<WeavingRecipe> validate(WeavingRecipe recipe) {
        if (recipe.memory.isEmpty() && recipe.name.isEmpty()) {
            return DataResult.error(() -> "A pattern names its Memory or gives its own name");
        }
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

    /**
     * How a billet of this pattern is worked. {@code heatMin} to
     * {@code heatMax} is the working band in degrees; a band of 0 to 0 is
     * cold work (hide and cloth) that needs no hearth heat. {@code blows} is
     * how many landed hammer blows shape it; a {@code quench} pattern is then
     * dipped in a trough and held for {@code quenchTicks}.
     */
    public record Forging(int heatMin, int heatMax, int blows, boolean quench, int quenchTicks) {
        public static final Forging DEFAULT = new Forging(900, 1250, 8, true, 30);
        public static final Codec<Forging> CODEC = RecordCodecBuilder.<Forging>create(instance -> instance.group(
                Codec.intRange(0, 2000).optionalFieldOf("heat_min", DEFAULT.heatMin()).forGetter(Forging::heatMin),
                Codec.intRange(0, 2000).optionalFieldOf("heat_max", DEFAULT.heatMax()).forGetter(Forging::heatMax),
                Codec.intRange(1, 40).optionalFieldOf("blows", DEFAULT.blows()).forGetter(Forging::blows),
                Codec.BOOL.optionalFieldOf("quench", DEFAULT.quench()).forGetter(Forging::quench),
                Codec.intRange(5, 200).optionalFieldOf("quench_ticks", DEFAULT.quenchTicks()).forGetter(Forging::quenchTicks)
        ).apply(instance, Forging::new)).validate(forging -> forging.heatMax < forging.heatMin
                ? DataResult.error(() -> "heat_max is below heat_min")
                : DataResult.success(forging));

        /** Cold work: no heat band at all. */
        public boolean cold() {
            return heatMax == 0;
        }
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
