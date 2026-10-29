package dev.spud.shadowslave.spell;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.spell.voice.SpellVoices;
import dev.spud.shadowslave.spell.voice.VoiceLines;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the Spell's tunables ({@code spell/settings.json}) and its written
 * lines ({@code spell_voice/<topic>.json}). A data pack can add or replace
 * lines without code.
 */
public final class SpellContentLoader extends SimplePreparableReloadListener<SpellContentLoader.Loaded> {
    private static final FileToIdConverter VOICE = FileToIdConverter.json("spell_voice");
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "spell/settings.json");
    private static volatile SpellSettings settings = SpellSettings.DEFAULT;

    record Loaded(SpellSettings settings, VoiceLines lines) {
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SpellContentLoader());
    }

    public static SpellSettings settings() {
        return settings;
    }

    @Override
    protected Loaded prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, List<Map<String, List<VoiceLines.Line>>>> files = new HashMap<>();
        VOICE.listMatchingResourceStacks(manager).forEach((file, stack) -> {
            String topic = VOICE.fileToId(file).getPath();
            for (Resource resource : stack) {
                parse(file, resource, VoiceLines.FILE_CODEC).ifPresent(lines ->
                        files.computeIfAbsent(topic, ignored -> new ArrayList<>()).add(lines));
            }
        });
        SpellSettings loaded = manager.getResource(SETTINGS)
                .flatMap(resource -> parse(SETTINGS, resource, SpellSettings.CODEC))
                .orElse(SpellSettings.DEFAULT);
        return new Loaded(loaded, VoiceLines.merge(files));
    }

    @Override
    protected void apply(Loaded loaded, ResourceManager manager, ProfilerFiller profiler) {
        settings = loaded.settings();
        SpellVoices.setLines(loaded.lines());
        ShadowSlaveMod.LOGGER.info("Loaded {} Spell lines in {} topics", loaded.lines().count(), loaded.lines().topics().size());
    }

    private static <T> Optional<T> parse(ResourceLocation file, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Spell file {}: {}", file, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Spell file {}", file, exception);
            return Optional.empty();
        }
    }
}
