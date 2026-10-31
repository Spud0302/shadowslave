package dev.spud.shadowslave.citadel;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import dev.spud.shadowslave.ShadowSlaveMod;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads {@code citadel/settings.json}, {@code citadel/upkeep.json},
 * {@code citadel/raid.json}, {@code citadel/war.json}, {@code citadel/npc.json} and every
 * {@code citadel/upgrade/<id>.json}; a data pack can retune Citadels, clans,
 * upkeep, upgrades, raids and wars without code.
 */
public final class CitadelContentLoader extends SimplePreparableReloadListener<CitadelContentLoader.Content> {
    private static final ResourceLocation SETTINGS = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/settings.json");
    private static final ResourceLocation UPKEEP = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/upkeep.json");
    private static final ResourceLocation RAID = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/raid.json");
    private static final ResourceLocation WAR = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/war.json");
    private static final ResourceLocation NPC = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "citadel/npc.json");
    private static final String UPGRADES = "citadel/upgrade";

    /** Everything Citadels read from data. Upgrades are keyed by their file's path ("wards") and kept in Gateway order. */
    public record Content(CitadelSettings settings, CitadelUpkeep upkeep, CitadelRaidSettings raids, CitadelWarSettings wars,
                          NpcCitadelSettings npc, Map<String, CitadelUpgrade> upgrades) {
        static final Content DEFAULT = new Content(CitadelSettings.DEFAULT, CitadelUpkeep.DEFAULT, CitadelRaidSettings.DEFAULT,
                CitadelWarSettings.DEFAULT, NpcCitadelSettings.DEFAULT, Map.of());

        public Optional<CitadelUpgrade> upgrade(String id) {
            return Optional.ofNullable(upgrades.get(id));
        }
    }

    private static volatile Content current = Content.DEFAULT;

    public static CitadelSettings settings() {
        return current.settings();
    }

    public static Content content() {
        return current;
    }

    /** Swaps the content in place, for game tests. */
    static void set(Content content) {
        current = content;
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CitadelContentLoader());
    }

    @Override
    protected Content prepare(ResourceManager manager, ProfilerFiller profiler) {
        CitadelSettings settings = read(manager, SETTINGS, CitadelSettings.CODEC).orElse(CitadelSettings.DEFAULT);
        CitadelUpkeep upkeep = read(manager, UPKEEP, CitadelUpkeep.CODEC).orElse(CitadelUpkeep.DEFAULT);
        CitadelRaidSettings raids = read(manager, RAID, CitadelRaidSettings.CODEC).orElse(CitadelRaidSettings.DEFAULT);
        CitadelWarSettings wars = read(manager, WAR, CitadelWarSettings.CODEC).orElse(CitadelWarSettings.DEFAULT);
        NpcCitadelSettings npc = read(manager, NPC, NpcCitadelSettings.CODEC).orElse(NpcCitadelSettings.DEFAULT);
        Map<String, CitadelUpgrade> found = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(UPGRADES, path -> path.getPath().endsWith(".json")).entrySet()) {
            String path = entry.getKey().getPath();
            String id = path.substring(UPGRADES.length() + 1, path.length() - ".json".length());
            read(entry.getKey(), entry.getValue(), CitadelUpgrade.CODEC).ifPresent(upgrade -> found.put(id, upgrade));
        }
        Map<String, CitadelUpgrade> upgrades = new LinkedHashMap<>();
        found.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<String, CitadelUpgrade> entry) -> entry.getValue().order())
                .thenComparing(Map.Entry::getKey)).forEach(entry -> upgrades.put(entry.getKey(), entry.getValue()));
        return new Content(settings, upkeep, raids, wars, npc, java.util.Collections.unmodifiableMap(upgrades));
    }

    private static <T> Optional<T> read(ResourceManager manager, ResourceLocation id, Codec<T> codec) {
        return manager.getResource(id).flatMap(resource -> read(id, resource, codec));
    }

    private static <T> Optional<T> read(ResourceLocation id, Resource resource, Codec<T> codec) {
        try (Reader reader = resource.openAsReader()) {
            return codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .resultOrPartial(error -> ShadowSlaveMod.LOGGER.error("Bad Citadel data {}: {}", id, error));
        } catch (Exception exception) {
            ShadowSlaveMod.LOGGER.error("Could not read Citadel data {}", id, exception);
            return Optional.empty();
        }
    }

    @Override
    protected void apply(Content content, ResourceManager manager, ProfilerFiller profiler) {
        current = content;
    }
}
