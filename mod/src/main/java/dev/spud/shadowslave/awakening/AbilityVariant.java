package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** One ability a Nature can grow at a rank-up; the player picks one of two at each rank (DESIGN). */
public record AbilityVariant(ResourceLocation abilityId, String name, String description, String role, SoulRank rank) {
    public AbilityVariant {
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(rank, "rank");
    }
}
