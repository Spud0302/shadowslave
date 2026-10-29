package dev.spud.shadowslave.citadel;

import com.google.gson.JsonParser;
import dev.spud.shadowslave.ShadowSlaveMod;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.Optional;

/** Loads {@code citadel/settings.json}; a data pack can retune Citadels and clans without code. */
public final class CitadelContentLoader extends SimplePreparableReloadListener<CitadelSettings> {
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/settings.json");
    private static volatile CitadelSettings current = CitadelSettings.DEFAULT;

    public static CitadelSettings settings() {
        return current;
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CitadelContentLoader());
    }

    @Override
    protected CitadelSettings prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.getResource(SETTINGS).flatMap(resource -> {
            try (Reader reader = resource.openAsReader()) {
                return CitadelSettings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Citadel settings {}: {}", SETTINGS, error));
            } catch (Exception exception) {
                ShadowSlaveMod.LOGGER.error("Could not read Citadel settings {}", SETTINGS, exception);
                return Optional.<CitadelSettings>empty();
            }
        }).orElse(CitadelSettings.DEFAULT);
    }

    @Override
    protected void apply(CitadelSettings settings, ResourceManager manager, ProfilerFiller profiler) {
        current = settings;
    }
}
