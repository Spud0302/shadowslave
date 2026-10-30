package dev.spud.shadowslave.spell.truename;

import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** The loaded True Name settings and deeds; replaced whole on each data reload. */
public record TrueNameContent(TrueNameSettings settings, Map<ResourceLocation, TrueNameDeed> deeds) {
    public static final TrueNameContent EMPTY = new TrueNameContent(TrueNameSettings.DEFAULT, Map.of());
    private static volatile TrueNameContent current = EMPTY;

    public TrueNameContent {
        deeds = Map.copyOf(deeds);
    }

    public static TrueNameContent get() {
        return current;
    }

    public static void set(TrueNameContent content) {
        current = content;
    }

    /** The deed that answers best: the highest priority among those that match, ties by id so it is stable. */
    public Optional<Map.Entry<ResourceLocation, TrueNameDeed>> best(Predicate<TrueNameDeed> matches) {
        return deeds.entrySet().stream()
                .filter(entry -> matches.test(entry.getValue()))
                .max(Comparator.<Map.Entry<ResourceLocation, TrueNameDeed>>comparingInt(entry -> entry.getValue().priority())
                        .thenComparing(entry -> entry.getKey().toString(), Comparator.reverseOrder()));
    }

    public Optional<TrueNameDeed> deed(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id.contains(":") ? id : "shadowslave:" + id);
        return location == null ? Optional.empty() : Optional.ofNullable(deeds.get(location));
    }

    public List<String> ids() {
        return deeds.keySet().stream().map(ResourceLocation::getPath).sorted().toList();
    }
}
