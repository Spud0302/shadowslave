package dev.spud.shadowslave.item;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item data components owned by the mod. */
public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShardContents>> SHARD_CONTENTS =
            COMPONENTS.registerComponentType("shard_contents", builder -> builder
                    .persistent(ShardContents.CODEC)
                    .networkSynchronized(ShardContents.STREAM_CODEC));

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
