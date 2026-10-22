package dev.spud.shadowslave.ability;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Activatable abilities by id. Ability ids without an entry here are passive or not built yet. */
public final class AbilityRegistry {
    private static final Map<ResourceLocation, AspectAbility> ABILITIES = new LinkedHashMap<>();

    static {
        register(new KindleAbility());
        register(new MistPassageAbility());
        register(new HoldfastAbility());
        register(new ReturningTideAbility());
    }

    private AbilityRegistry() {
    }

    private static void register(AspectAbility ability) {
        if (ABILITIES.putIfAbsent(ability.id(), ability) != null) {
            throw new IllegalStateException("Duplicate ability " + ability.id());
        }
    }

    public static Optional<AspectAbility> get(ResourceLocation id) {
        return Optional.ofNullable(ABILITIES.get(id));
    }

    public static boolean isActivatable(ResourceLocation id) {
        return ABILITIES.containsKey(id);
    }
}
