package dev.spud.shadowslave.fieldguide;

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
 * Loads the Field Guide from data packs: {@code field_guide/creature/*.json}
 * and {@code field_guide/rules.json}. A data pack can add pages for new
 * creatures or retune the rules without code.
 */
public final class FieldGuideContentLoader extends SimplePreparableReloadListener<FieldGuideContent> {
    private static final FileToIdConverter PAGES = FileToIdConverter.json("field_guide/creature");
    private static final ResourceLocation RULES = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide/rules.json");

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new FieldGuideContentLoader());
    }

    @Override
    protected FieldGuideContent prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, CreaturePage> pages = new HashMap<>();
        PAGES.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, CreaturePage.CODEC).ifPresent(page -> pages.put(PAGES.fileToId(file), page)));
        FieldGuideSettings settings = manager.getResource(RULES)
                .flatMap(resource -> parse(RULES, resource, FieldGuideSettings.CODEC))
                .orElse(FieldGuideSettings.DEFAULT);
        return FieldGuideContent.build(settings, pages);
    }

    @Override
    protected void apply(FieldGuideContent content, ResourceManager manager, ProfilerFiller profiler) {
        FieldGuideContent.set(content);
        ShadowSlaveMod.LOGGER.info("Loaded {} Field Guide creature pages", content.pages().size());
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            return codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Field Guide file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Field Guide file {}", file, exception);
            return Optional.empty();
        }
    }
}
