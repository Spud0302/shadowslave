package dev.spud.shadowslave.citadel;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Citadels' registrations: the Gateway block and its block entity, the Link
 * Anchor that holds a Citadel's supply line, the residents of NPC-held
 * Citadels, and the
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
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ShadowSlaveMod.MOD_ID);

    public static final DeferredBlock<GatewayBlock> GATEWAY = BLOCKS.registerBlock("citadel_gateway", GatewayBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.LODESTONE)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(GatewayBlock.STATE).light()));

    public static final DeferredBlock<LinkAnchorBlock> LINK_ANCHOR = BLOCKS.registerBlock("link_anchor", LinkAnchorBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.LODESTONE)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> state.getValue(LinkAnchorBlock.CUT) ? 3 : 12));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GatewayBlockEntity>> GATEWAY_ENTITY =
            BLOCK_ENTITIES.register("citadel_gateway", () -> BlockEntityType.Builder.of(GatewayBlockEntity::new, GATEWAY.get()).build(null));

    public static final DeferredHolder<StructureType<?>, StructureType<CitadelStructure>> CITADEL_STRUCTURE =
            STRUCTURE_TYPES.register("citadel", () -> () -> CitadelStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> CITADEL_PIECE =
            STRUCTURE_PIECES.register("citadel", () -> (StructurePieceType.ContextlessType) CitadelPiece::new);

    public static final DeferredHolder<EntityType<?>, EntityType<CitadelResident>> CITADEL_RESIDENT = ENTITY_TYPES.register(
            "citadel_resident",
            registryName -> EntityType.Builder.<CitadelResident>of(CitadelResident::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(registryName.toString()));

    private CitadelRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(CitadelRegistry::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(CITADEL_RESIDENT.get(), CitadelResident.attributes().build());
    }
}
