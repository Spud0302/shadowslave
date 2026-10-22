package dev.spud.shadowslave.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Persistent per-ability cooldowns, stored as the game time each ability is ready again. */
public record AbilityCooldownData(Map<ResourceLocation, Long> readyAt) {
    public static final MapCodec<AbilityCooldownData> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG)
            .optionalFieldOf("ready_at", Map.of())
            .xmap(AbilityCooldownData::new, AbilityCooldownData::readyAt);

    public AbilityCooldownData {
        readyAt = Map.copyOf(Objects.requireNonNull(readyAt, "readyAt"));
    }

    public static AbilityCooldownData empty() {
        return new AbilityCooldownData(Map.of());
    }

    public long remainingTicks(ResourceLocation ability, long now) {
        return Math.max(0L, readyAt.getOrDefault(ability, 0L) - now);
    }

    public AbilityCooldownData start(ResourceLocation ability, long now, int cooldownTicks) {
        Map<ResourceLocation, Long> next = new HashMap<>(readyAt);
        next.put(ability, now + cooldownTicks);
        return new AbilityCooldownData(next);
    }
}
