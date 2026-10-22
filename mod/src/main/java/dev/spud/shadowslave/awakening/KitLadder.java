package dev.spud.shadowslave.awakening;

import dev.spud.shadowslave.soul.SoulRank;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Each Nature's ladder past Dormant (DESIGN, from the lore-to-gameplay doc):
 * at Awakening the player chooses one of two abilities that deepen the same
 * nature, the way Sunny's Shadow Step grew out of Shadow Control.
 */
public final class KitLadder {
    private static final String NAMESPACE = "shadowslave";

    public static final AbilityVariant CARRY_THE_FLAME = variant("carry_the_flame", "Carry the Flame",
            "Pass your fire to an ally you look at (or yourself): regeneration and fire resistance.", "Support");
    public static final AbilityVariant EMBER_WARD = variant("ember_ward", "Ember Ward",
            "A ring of fire around you for a few seconds; hostile mobs that touch it are thrown back burning.", "Control");
    public static final AbilityVariant THROUGH_THE_HAZE = variant("through_the_haze", "Through the Haze",
            "See every creature and player nearby through walls for a few seconds.", "Sight");
    public static final AbilityVariant VEIL = variant("veil", "Veil",
            "A dome of mist: hostile mobs inside are blinded and lose track of their prey.", "Control");
    public static final AbilityVariant ROOT_IN_STONE = variant("root_in_stone", "Root in Stone",
            "Raise a stone wall in front of you; it crumbles after a while.", "Shelter");
    public static final AbilityVariant QUAKE = variant("quake", "Quake",
            "Stomp: nearby hostile mobs are hurt, thrown up and slowed.", "Force");
    public static final AbilityVariant LOW_TIDE = variant("low_tide", "Low Tide",
            "Surge forward on a wave that shoves mobs aside.", "Movement");
    public static final AbilityVariant UNDERTOW = variant("undertow", "Undertow",
            "Drag nearby mobs toward the spot you look at and slow them.", "Control");

    private static final Map<ResourceLocation, List<AbilityVariant>> AWAKENED = Map.of(
            AspectKitCatalog.EMBER.natureId(), List.of(CARRY_THE_FLAME, EMBER_WARD),
            AspectKitCatalog.MIST.natureId(), List.of(THROUGH_THE_HAZE, VEIL),
            AspectKitCatalog.STONE.natureId(), List.of(ROOT_IN_STONE, QUAKE),
            AspectKitCatalog.TIDE.natureId(), List.of(LOW_TIDE, UNDERTOW)
    );

    private KitLadder() {
    }

    /** The two abilities a Nature can grow on reaching {@code rank}; empty for ranks not built yet. */
    public static List<AbilityVariant> variants(ResourceLocation natureId, SoulRank rank) {
        if (rank != SoulRank.AWAKENED) {
            return List.of();
        }
        return AWAKENED.getOrDefault(natureId, List.of());
    }

    public static List<AbilityVariant> all() {
        return AWAKENED.values().stream().flatMap(List::stream).toList();
    }

    public static Optional<AbilityVariant> byAbility(ResourceLocation abilityId) {
        return all().stream().filter(variant -> variant.abilityId().equals(abilityId)).findFirst();
    }

    private static AbilityVariant variant(String path, String name, String description, String role) {
        return new AbilityVariant(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "generation/ability/" + path),
                name, description, role, SoulRank.AWAKENED);
    }
}
