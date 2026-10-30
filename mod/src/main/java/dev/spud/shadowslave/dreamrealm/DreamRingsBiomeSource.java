package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Paints the Dream Realm's biomes in rings around its centre (DESIGN, Jack
 * 2026-10-23: one dimension with rank regions further out), so each region
 * of {@code dream_realm/region/*.json} looks like itself: the Ashen Expanse at
 * the heart, deeper and stranger lands beyond. Set in the dimension file:
 *
 * <pre>{"type": "shadowslave:dream_rings", "biome": "...", "rings": [{"min_distance": 1200, "biome": "..."}]}</pre>
 *
 * <p>A column takes the biome of the ring that starts furthest out within its
 * {@link DreamRealm#regionDistance}, so keep each ring's {@code min_distance}
 * equal to its region's; a game test checks they agree.</p>
 */
public final class DreamRingsBiomeSource extends BiomeSource {
    public record Ring(int minDistance, Holder<Biome> biome) {
        public static final Codec<Ring> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min_distance").forGetter(Ring::minDistance),
                Biome.CODEC.fieldOf("biome").forGetter(Ring::biome)
        ).apply(instance, Ring::new));
    }

    public static final MapCodec<DreamRingsBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(source -> source.centre),
            Ring.CODEC.listOf().optionalFieldOf("rings", List.of()).forGetter(source -> source.rings)
    ).apply(instance, DreamRingsBiomeSource::new));

    private final Holder<Biome> centre;
    /** Furthest ring first. */
    private final List<Ring> rings;

    public DreamRingsBiomeSource(Holder<Biome> centre, List<Ring> rings) {
        this.centre = centre;
        this.rings = rings.stream().sorted(Comparator.comparingInt(Ring::minDistance).reversed()).toList();
    }

    public List<Ring> rings() {
        return rings;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(Stream.of(centre), rings.stream().map(Ring::biome));
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return biomeAt(QuartPos.toBlock(x) + 2, QuartPos.toBlock(z) + 2);
    }

    /** The biome over a block column. */
    public Holder<Biome> biomeAt(int blockX, int blockZ) {
        double distance = DreamRealm.regionDistance(blockX, blockZ);
        for (Ring ring : rings) {
            if (distance >= ring.minDistance()) {
                return ring.biome();
            }
        }
        return centre;
    }
}
