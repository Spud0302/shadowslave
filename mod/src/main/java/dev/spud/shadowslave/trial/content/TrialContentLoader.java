package dev.spud.shadowslave.trial.content;

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
 * Loads Nightmares ({@code nightmare_trial/nightmare/*.json}), their tuning
 * ({@code nightmare_trial/settings.json}) and echo sites
 * ({@code dream_realm/echo_site/*.json}). A data pack can add a Nightmare and
 * the place it grows from without code. The settings file also holds the
 * Nightmare Scar ({@code scar}), Long Nightmare ({@code long_nightmares}) and First
 * Nightmare ({@code first_nightmare}) tuning.
 */
public final class TrialContentLoader extends SimplePreparableReloadListener<TrialContent> {
    private static final FileToIdConverter NIGHTMARES = FileToIdConverter.json("nightmare_trial/nightmare");
    private static final FileToIdConverter SITES = FileToIdConverter.json("dream_realm/echo_site");
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "nightmare_trial/settings.json");

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new TrialContentLoader());
    }

    @Override
    protected TrialContent prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, NightmarePack> nightmares = new HashMap<>();
        NIGHTMARES.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, NightmarePack.CODEC).ifPresent(pack -> nightmares.put(NIGHTMARES.fileToId(file), pack)));
        Map<ResourceLocation, EchoSite> sites = new HashMap<>();
        SITES.listMatchingResources(manager).forEach((file, resource) ->
                parse(file, resource, EchoSite.CODEC).ifPresent(site -> sites.put(SITES.fileToId(file), site)));
        sites.forEach((id, site) -> {
            if (!nightmares.containsKey(site.nightmare())) {
                ShadowSlaveMod.LOGGER.error("Echo site {} names a missing Nightmare {}", id, site.nightmare());
            }
        });
        Optional<Resource> settingsFile = manager.getResource(SETTINGS);
        TrialSettings settings = settingsFile.flatMap(resource -> parse(SETTINGS, resource, TrialSettings.CODEC))
                .orElse(TrialSettings.DEFAULT);
        ScarSettings scar = settingsFile.flatMap(resource -> parse(SETTINGS, resource, ScarSettings.IN_SETTINGS))
                .orElse(ScarSettings.DEFAULT);
        LongNightmareSettings longNightmares = settingsFile.flatMap(resource -> parse(SETTINGS, resource, LongNightmareSettings.IN_SETTINGS))
                .orElse(LongNightmareSettings.DEFAULT);
        FirstNightmareSettings firstNightmare = settingsFile.flatMap(resource -> parse(SETTINGS, resource, FirstNightmareSettings.IN_SETTINGS))
                .orElse(FirstNightmareSettings.DEFAULT);
        AppraisalSettings appraisal = settingsFile.flatMap(resource -> parse(SETTINGS, resource, AppraisalSettings.IN_SETTINGS))
                .orElse(AppraisalSettings.DEFAULT);
        firstNightmare.nightmares().stream().filter(id -> !nightmares.containsKey(id)).forEach(id ->
                ShadowSlaveMod.LOGGER.error("The First Nightmare settings name a missing Nightmare {}", id));
        return new TrialContent(settings, scar, longNightmares, firstNightmare, appraisal, nightmares, sites);
    }

    @Override
    protected void apply(TrialContent content, ResourceManager manager, ProfilerFiller profiler) {
        TrialContent.set(content);
        ShadowSlaveMod.LOGGER.info("Loaded {} Nightmares and {} echo sites", content.nightmares().size(), content.sites().size());
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Nightmare file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Nightmare file {}", file, exception);
            return Optional.empty();
        }
    }
}
