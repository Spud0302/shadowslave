package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.echo.AshBurrowerEchoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** NeoForge execution bindings for Java-owned creature and Echo identities. */
public final class NightmareCreatureEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ShadowSlaveMod.MOD_ID);

    public static final Supplier<EntityType<AshBurrowerEntity>> ASH_BURROWER = ENTITY_TYPES.register(
            AshBurrowerExecutionBinding.CONTENT_ID,
            registryName -> EntityType.Builder.of(AshBurrowerEntity::new, MobCategory.MONSTER)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(8)
                    .build(registryName.toString())
    );

    /** Presentation executor only; Echo ownership and commands remain in EchoOwnershipData. */
    public static final Supplier<EntityType<AshBurrowerEchoEntity>> ASH_BURROWER_ECHO = ENTITY_TYPES.register(
            "ash_burrower_echo",
            registryName -> EntityType.Builder.of(AshBurrowerEchoEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(8)
                    .build(registryName.toString())
    );

    public static final Supplier<EntityType<ChainbackEntity>> CHAINBACK = ENTITY_TYPES.register(
            ChainbackExecutionBinding.CONTENT_ID,
            registryName -> EntityType.Builder.of(ChainbackEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(8)
                    .build(registryName.toString())
    );

    public static final Supplier<EntityType<DrownedListenerEntity>> DROWNED_LISTENER = ENTITY_TYPES.register(
            DrownedListenerExecutionBinding.CONTENT_ID,
            registryName -> EntityType.Builder.of(DrownedListenerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(registryName.toString())
    );

    private NightmareCreatureEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(NightmareCreatureEntities::createDefaultAttributes);
    }

    public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(ASH_BURROWER.get(), Silverfish.createAttributes().build());
        event.put(ASH_BURROWER_ECHO.get(), Armadillo.createAttributes().build());
        event.put(CHAINBACK.get(), Spider.createAttributes().build());
        event.put(DROWNED_LISTENER.get(), Drowned.createAttributes().build());
    }
}
