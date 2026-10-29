package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** The one piece of a generated Citadel; it lays {@link CitadelLayout} a chunk at a time. */
public final class CitadelPiece extends StructurePiece {
    private final BlockPos centre;
    private final int half;
    private final CitadelPalette palette;

    public CitadelPiece(BlockPos centre, int half, CitadelPalette palette) {
        super(CitadelRegistry.CITADEL_PIECE.get(), 0, CitadelLayout.bounds(centre, half));
        this.centre = centre.immutable();
        this.half = half;
        this.palette = palette;
    }

    public CitadelPiece(CompoundTag tag) {
        super(CitadelRegistry.CITADEL_PIECE.get(), tag);
        this.centre = NbtUtils.readBlockPos(tag, "centre").orElse(BlockPos.ZERO);
        this.half = tag.getInt("half");
        this.palette = CitadelPalette.CODEC.parse(NbtOps.INSTANCE, tag.get("palette"))
                .resultOrPartial(error -> ShadowSlaveMod.LOGGER.warn("Bad saved Citadel palette: {}", error))
                .orElse(CitadelPalette.DEFAULT);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put("centre", NbtUtils.writeBlockPos(centre));
        tag.putInt("half", half);
        CitadelPalette.CODEC.encodeStart(NbtOps.INSTANCE, palette).result().ifPresent(saved -> tag.put("palette", saved));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        CitadelLayout.build(new CitadelLayout.Canvas() {
            @Override
            public BlockState get(BlockPos pos) {
                return level.getBlockState(pos);
            }

            @Override
            public void set(BlockPos pos, BlockState state) {
                if (box.isInside(pos)) {
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                }
            }

            @Override
            public boolean covers(BlockPos pos) {
                return box.isInside(pos);
            }
        }, centre, half, palette);
    }
}
