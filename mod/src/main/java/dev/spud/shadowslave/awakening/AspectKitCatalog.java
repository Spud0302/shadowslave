package dev.spud.shadowslave.awakening;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * The four Natures the Dream Candle ritual can roll in the first slice:
 * Ember, Mist, Stone and Tide. Ability and Flaw ids match the procedural
 * catalogue so existing mechanics (Kindle, Cold Ash) keep working.
 */
public final class AspectKitCatalog {
    private static final String NAMESPACE = "shadowslave";

    public static final AspectKit EMBER = new AspectKit(
            id("generation/nature/ember"), "Ember",
            id("kit/innate/ember_warmth"), "Ember's Warmth",
            "Fire and lava hurt you half as much.",
            id("generation/ability/kindle"), "Kindle",
            "A burst of light: night vision and speed, and nearby hostile mobs catch fire.",
            id("generation/flaw/cold_ash"), id("generation/flaw_effect/cold_ash"), "Cold Ash",
            "Weakness while you stand in water or rain."
    );

    public static final AspectKit MIST = new AspectKit(
            id("generation/nature/mist"), "Mist",
            id("kit/innate/mist_hush"), "Mist's Hush",
            "Sneaking makes no sound.",
            id("generation/ability/mist_passage"), "Mist Passage",
            "Blink a short way where you look and turn briefly invisible.",
            id("generation/flaw/borrowed_breath"), id("generation/flaw_effect/borrowed_breath"), "Borrowed Breath",
            "Every Mist Passage costs hunger, and you run out of air faster."
    );

    public static final AspectKit STONE = new AspectKit(
            id("generation/nature/stone"), "Stone",
            id("kit/innate/stone_footing"), "Stone's Footing",
            "No knockback while you stand still.",
            id("generation/ability/holdfast"), "Holdfast",
            "Brace: heavy damage resistance and no knockback for a few seconds, but no sprinting.",
            id("generation/flaw/stone_sleep"), id("generation/flaw_effect/stone_sleep"), "Stone Sleep",
            "You wake slow and heavy, and you sink in water."
    );

    public static final AspectKit TIDE = new AspectKit(
            id("generation/nature/tide"), "Tide",
            id("kit/innate/tide_gills"), "Tide's Breath",
            "You breathe underwater.",
            id("generation/ability/returning_tide"), "Returning Tide",
            "Mark where you stand, then be pulled back there.",
            id("generation/flaw/tidal_debt"), id("generation/flaw_effect/tidal_debt"), "Tidal Debt",
            "Touch water once each day, or grow weak until you do."
    );

    private static final List<AspectKit> ALL = List.of(EMBER, MIST, STONE, TIDE);

    private AspectKitCatalog() {
    }

    public static List<AspectKit> all() {
        return ALL;
    }

    public static Optional<AspectKit> byNature(ResourceLocation natureId) {
        return ALL.stream().filter(kit -> kit.natureId().equals(natureId)).findFirst();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
}
