package dev.spud.shadowslave.weaving;

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
 * Loads Weaving from data packs: {@code weaving/branch/*.json},
 * {@code smithing/pattern/*.json} and {@code weaving/rules.json}. A data pack
 * can add, change or remove branches and patterns without code.
 */
public final class WeavingContentLoader extends SimplePreparableReloadListener<WeavingContent> {
    private static final FileToIdConverter BRANCHES = FileToIdConverter.json("weaving/branch");
    private static final FileToIdConverter RECIPES = FileToIdConverter.json("smithing/pattern");
    private static final ResourceLocation RULES = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "weaving/rules.json");

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WeavingContentLoader());
    }

    @Override
    protected WeavingContent prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, WeavingBranch> branches = read(manager, BRANCHES, WeavingBranch.CODEC);
        Map<ResourceLocation, WeavingRecipe> recipes = read(manager, RECIPES, WeavingRecipe.CODEC);
        WeavingSettings settings = manager.getResource(RULES)
                .flatMap(resource -> parse(RULES, resource, WeavingSettings.CODEC))
                .orElse(WeavingSettings.DEFAULT);
        return WeavingContent.build(settings, branches, recipes, ShadowSlaveMod.LOGGER::warn);
    }

    @Override
    protected void apply(WeavingContent content, ResourceManager manager, ProfilerFiller profiler) {
        WeavingContent.set(content);
        ShadowSlaveMod.LOGGER.info("Loaded {} Weaving branches and {} patterns", content.branches().size(), content.recipes().size());
    }

    private static <T> Map<ResourceLocation, T> read(ResourceManager manager, FileToIdConverter files, Codec<T> codec) {
        Map<ResourceLocation, T> values = new HashMap<>();
        files.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, codec).ifPresent(value -> values.put(files.fileToId(file), value)));
        return values;
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Weaving file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Weaving file {}", file, exception);
            return Optional.empty();
        }
    }
}
