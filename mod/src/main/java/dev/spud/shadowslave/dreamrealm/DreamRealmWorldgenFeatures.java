package dev.spud.shadowslave.dreamrealm;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Native Minecraft world-generation executors backed by Java-owned Dream Realm plans. */
public final class DreamRealmWorldgenFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ShadowSlaveMod.MOD_ID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> DROWNED_BELL_LATER_ANCHOR = FEATURES.register(
            "drowned_bell_later_anchor_feature",
            () -> new DrownedBellLaterAnchorFeature(NoneFeatureConfiguration.CODEC)
    );

    private DreamRealmWorldgenFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
