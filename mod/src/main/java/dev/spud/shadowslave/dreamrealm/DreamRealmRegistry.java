package dev.spud.shadowslave.dreamrealm;

import com.google.common.collect.ImmutableSet;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.block.ModBlocks;
import dev.spud.shadowslave.dreamrealm.seed.NightmareSeedBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The Dream Realm's own registrations: the Nightmare Seed block, the sleeping
 * body, the point of interest that finds lit Dream Candles for warding, and
 * the dreamer attachment. Kept here to stay out of shared registries.
 */
public final class DreamRealmRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.biome.BiomeSource>>
            BIOME_SOURCES = DeferredRegister.create(Registries.BIOME_SOURCE, ShadowSlaveMod.MOD_ID);

    /** The Dream Realm's ring biome source, so each region has its own ground and sky. */
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.biome.BiomeSource>,
            com.mojang.serialization.MapCodec<DreamRingsBiomeSource>> DREAM_RINGS =
            BIOME_SOURCES.register("dream_rings", () -> DreamRingsBiomeSource.CODEC);

    /** Dying with your sleeping body: "Steve's dream was severed". */
    public static final ResourceKey<DamageType> SEVERED_DREAM = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "severed_dream"));

    public static final DeferredBlock<NightmareSeedBlock> NIGHTMARE_SEED = BLOCKS.registerBlock("nightmare_seed",
            NightmareSeedBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> 4 + 3 * state.getValue(NightmareSeedBlock.STAGE)));

    public static final DeferredHolder<EntityType<?>, EntityType<SleepingBodyEntity>> SLEEPING_BODY = ENTITY_TYPES.register(
            "sleeping_body",
            registryName -> EntityType.Builder.<SleepingBodyEntity>of(SleepingBodyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build(registryName.toString()));

    /** Every lit Dream Candle, so a blooming Seed can find one near its Gate without scanning blocks. */
    public static final DeferredHolder<PoiType, PoiType> LIT_DREAM_CANDLE = POI_TYPES.register("lit_dream_candle",
            () -> new PoiType(litCandleStates(), 0, 1));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DreamData>> DREAM = ATTACHMENTS.register("dream",
            () -> AttachmentType.builder(DreamData::empty).serialize(DreamData.CODEC).copyOnDeath().build());

    private DreamRealmRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        POI_TYPES.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        modEventBus.addListener(DreamRealmRegistry::createAttributes);
    }

    private static ImmutableSet<BlockState> litCandleStates() {
        return ModBlocks.DREAM_CANDLE.get().getStateDefinition().getPossibleStates().stream()
                .filter(state -> state.getValue(CandleBlock.LIT))
                .collect(ImmutableSet.toImmutableSet());
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(SLEEPING_BODY.get(), LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0).build());
    }
}
