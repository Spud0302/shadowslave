package dev.spud.shadowslave.trial;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Nightmares' own registrations: the Nightmare Scar and the figure a sleeping challenger leaves in a Long Nightmare. */
public final class TrialRegistry {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<MobEffect, NightmareScar> NIGHTMARE_SCAR = EFFECTS.register("nightmare_scar", NightmareScar::new);

    public static final DeferredHolder<EntityType<?>, EntityType<LingeringChallenger>> LINGERING_CHALLENGER = ENTITY_TYPES.register(
            "lingering_challenger",
            registryName -> EntityType.Builder.<LingeringChallenger>of(LingeringChallenger::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(registryName.toString()));

    private TrialRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(TrialRegistry::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(LINGERING_CHALLENGER.get(), LingeringChallenger.attributes().build());
    }
}
