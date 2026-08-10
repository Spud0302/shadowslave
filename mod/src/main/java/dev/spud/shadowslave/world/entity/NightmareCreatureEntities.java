package dev.spud.shadowslave.world.entity;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** NeoForge execution bindings for Java-owned Nightmare Creature identities. */
public final class NightmareCreatureEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(ShadowSlaveMod.MOD_ID);

    public static final Supplier<EntityType<ChainbackEntity>> CHAINBACK = ENTITY_TYPES.registerEntityType(
            ChainbackEntity.CONTENT_ID,
            ChainbackEntity::new,
            MobCategory.MONSTER,
            builder -> builder.sized(1.4F, 0.9F).clientTrackingRange(8)
    );

    private NightmareCreatureEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(NightmareCreatureEntities::createDefaultAttributes);
    }

    public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(CHAINBACK.get(), Spider.createAttributes().build());
    }
}
