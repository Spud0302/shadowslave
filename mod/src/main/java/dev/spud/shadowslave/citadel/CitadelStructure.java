package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * A Citadel as world generation sees it (DESIGN, Jack 2026-10-29: Citadels
 * are found, not built). Where they appear is the structure set
 * {@code worldgen/structure_set/citadels.json}; their size, palette and how
 * rough the ground may be are in {@code worldgen/structure/citadel.json}. A
 * Citadel only rises on ground flat enough to hold it.
 */
public final class CitadelStructure extends Structure {
    public static final MapCodec<CitadelStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.intRange(8, 20).optionalFieldOf("half_size", 12).forGetter(structure -> structure.half),
            Codec.intRange(0, 32).optionalFieldOf("max_slope", 10).forGetter(structure -> structure.maxSlope),
            CitadelPalette.CODEC.optionalFieldOf("palette", CitadelPalette.DEFAULT).forGetter(structure -> structure.palette)
    ).apply(instance, CitadelStructure::new));

    private final int half;
    private final int maxSlope;
    private final CitadelPalette palette;

    public CitadelStructure(StructureSettings settings, int half, int maxSlope, CitadelPalette palette) {
        super(settings);
        this.half = half;
        this.maxSlope = maxSlope;
        this.palette = palette;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        int centre = 0;
        for (int[] offset : new int[][]{{0, 0}, {-half, -half}, {half, -half}, {-half, half}, {half, half}}) {
            int height = context.chunkGenerator().getFirstOccupiedHeight(x + offset[0], z + offset[1],
                    Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            if (offset[0] == 0 && offset[1] == 0) {
                centre = height;
            }
            lowest = Math.min(lowest, height);
            highest = Math.max(highest, height);
        }
        if (highest - lowest > maxSlope || lowest <= context.heightAccessor().getMinBuildHeight() + CitadelLayout.FOUNDATION) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, centre + 1, z);
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new CitadelPiece(origin, half, palette))));
    }

    @Override
    public StructureType<?> type() {
        return CitadelRegistry.CITADEL_STRUCTURE.get();
    }
}
