package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The blocks a Citadel is built from, set per structure in
 * {@code worldgen/structure/*.json}, so a data pack can raise Citadels of
 * another stone without code. Missing keys keep the ashen defaults.
 */
public record CitadelPalette(BlockState wall, BlockState wallWorn, BlockState floor, BlockState trim, BlockState pillar,
                             BlockState accent, BlockState glow, BlockState light) {
    public static final CitadelPalette DEFAULT = new CitadelPalette(
            Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),
            Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE_TILES.defaultBlockState(),
            Blocks.POLISHED_BASALT.defaultBlockState(),
            Blocks.CHISELED_DEEPSLATE.defaultBlockState(),
            Blocks.CRYING_OBSIDIAN.defaultBlockState(),
            Blocks.SOUL_LANTERN.defaultBlockState());

    public static final Codec<CitadelPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.optionalFieldOf("wall", DEFAULT.wall).forGetter(CitadelPalette::wall),
            BlockState.CODEC.optionalFieldOf("wall_worn", DEFAULT.wallWorn).forGetter(CitadelPalette::wallWorn),
            BlockState.CODEC.optionalFieldOf("floor", DEFAULT.floor).forGetter(CitadelPalette::floor),
            BlockState.CODEC.optionalFieldOf("trim", DEFAULT.trim).forGetter(CitadelPalette::trim),
            BlockState.CODEC.optionalFieldOf("pillar", DEFAULT.pillar).forGetter(CitadelPalette::pillar),
            BlockState.CODEC.optionalFieldOf("accent", DEFAULT.accent).forGetter(CitadelPalette::accent),
            BlockState.CODEC.optionalFieldOf("glow", DEFAULT.glow).forGetter(CitadelPalette::glow),
            BlockState.CODEC.optionalFieldOf("light", DEFAULT.light).forGetter(CitadelPalette::light)
    ).apply(instance, CitadelPalette::new));
}
