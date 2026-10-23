package dev.spud.shadowslave.dreamrealm;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the Dream Realm from data packs: {@code dream_realm/region/*.json}
 * and {@code dream_realm/seeds.json}. A data pack can add rings of harder
 * regions or retune Seeds without code.
 */
public final class DreamRealmContentLoader extends SimplePreparableReloadListener<DreamRealmContent> {
    private static final FileToIdConverter REGIONS = FileToIdConverter.json("dream_realm/region");
    private static final ResourceLocation SEEDS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "dream_realm/seeds.json");

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DreamRealmContentLoader());
    }

    @Override
    protected DreamRealmContent prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, DreamRegion> regions = new HashMap<>();
        REGIONS.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, DreamRegion.CODEC).ifPresent(region -> regions.put(REGIONS.fileToId(file), region)));
        DreamSeedSettings seeds = manager.getResource(SEEDS)
                .flatMap(resource -> parse(SEEDS, resource, DreamSeedSettings.CODEC))
                .orElse(DreamSeedSettings.DEFAULT);
        return new DreamRealmContent(seeds, regions);
    }

    @Override
    protected void apply(DreamRealmContent content, ResourceManager manager, ProfilerFiller profiler) {
        DreamRealmContent.set(content);
        ShadowSlaveMod.LOGGER.info("Loaded {} Dream Realm regions", content.regions().size());
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Dream Realm file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Dream Realm file {}", file, exception);
            return Optional.empty();
        }
    }
}
