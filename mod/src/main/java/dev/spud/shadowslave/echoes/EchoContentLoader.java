package dev.spud.shadowslave.echoes;

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

/** Loads {@code echo/settings.json} and {@code echo/creature/*.json} from data packs. */
public final class EchoContentLoader extends SimplePreparableReloadListener<EchoContent> {
    private static final FileToIdConverter CREATURES = FileToIdConverter.json("echo/creature");
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo/settings.json");

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EchoContentLoader());
    }

    @Override
    protected EchoContent prepare(ResourceManager manager, ProfilerFiller profiler) {
        EchoSettings settings = manager.getResource(SETTINGS)
                .flatMap(resource -> parse(SETTINGS, resource, EchoSettings.CODEC))
                .orElse(EchoSettings.DEFAULT);
        Map<ResourceLocation, EchoCreature> creatures = new HashMap<>();
        CREATURES.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, EchoCreature.CODEC).ifPresent(creature -> creatures.put(creature.entity(), creature)));
        return new EchoContent(settings, creatures);
    }

    @Override
    protected void apply(EchoContent content, ResourceManager manager, ProfilerFiller profiler) {
        EchoContent.set(content);
        ShadowSlaveMod.LOGGER.info("Loaded Echo settings and {} creature overrides", content.creatures().size());
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Echo file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Echo file {}", file, exception);
            return Optional.empty();
        }
    }
}
