package dev.spud.shadowslave.fieldguide;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The Field Guide's loaded data: its rules and its creature pages, keyed by entity type. */
public record FieldGuideContent(FieldGuideSettings settings, Map<ResourceLocation, CreaturePage> pages) {
    private static volatile FieldGuideContent current = new FieldGuideContent(FieldGuideSettings.DEFAULT, Map.of());

    public FieldGuideContent {
        pages = Map.copyOf(pages);
    }

    public static FieldGuideContent get() {
        return current;
    }

    public static void set(FieldGuideContent content) {
        current = content;
    }

    /** Builds the content from page files, keying each page by the entity it describes. */
    public static FieldGuideContent build(FieldGuideSettings settings, Map<ResourceLocation, CreaturePage> files) {
        Map<ResourceLocation, CreaturePage> byEntity = new LinkedHashMap<>();
        files.values().forEach(page -> byEntity.put(page.entity(), page));
        return new FieldGuideContent(settings, byEntity);
    }

    public Optional<CreaturePage> page(ResourceLocation entity) {
        return Optional.ofNullable(pages.get(entity));
    }
}
