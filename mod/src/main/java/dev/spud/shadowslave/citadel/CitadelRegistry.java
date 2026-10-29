package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Citadels' registrations: the Gateway block and its block entity, and the
 * Citadel structure type and piece. Where Citadels generate, how they look
 * and what holds them all live in data.
 */
public final class CitadelRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, ShadowSlaveMod.MOD_ID);

    public static final DeferredBlock<GatewayBlock> GATEWAY = BLOCKS.registerBlock("citadel_gateway", GatewayBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.LODESTONE)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(GatewayBlock.STATE).light()));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GatewayBlockEntity>> GATEWAY_ENTITY =
            BLOCK_ENTITIES.register("citadel_gateway", () -> BlockEntityType.Builder.of(GatewayBlockEntity::new, GATEWAY.get()).build(null));

    public static final DeferredHolder<StructureType<?>, StructureType<CitadelStructure>> CITADEL_STRUCTURE =
            STRUCTURE_TYPES.register("citadel", () -> () -> CitadelStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CITADEL_PIECE =
            STRUCTURE_PIECES.register("citadel", () -> (StructurePieceType.ContextlessType) CitadelPiece::new);

    private CitadelRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECES.register(modEventBus);
    }
}
