package dev.spud.shadowslave.echoes;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/** Everything Echoes read from data: the settings and the per-creature overrides, keyed by entity id. */
public record EchoContent(EchoSettings settings, Map<ResourceLocation, EchoCreature> creatures) {
    public static final EchoContent DEFAULT = new EchoContent(EchoSettings.DEFAULT, Map.of());
    private static volatile EchoContent current = DEFAULT;

    public EchoContent {
        creatures = Map.copyOf(creatures);
    }

    public static EchoContent current() {
        return current;
    }

    static void set(EchoContent content) {
        current = content;
    }

    public Optional<EchoCreature> creature(ResourceLocation entity) {
        return Optional.ofNullable(creatures.get(entity));
    }
}
