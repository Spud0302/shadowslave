package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One track of upgrades a clan builds at its Citadel's Gateway (DESIGN, Run C),
 * read from {@code data/<ns>/citadel/upgrade/<id>.json}. Each tier is paid for
 * in items and sets named values the game reads by the upgrade's id: Wards read
 * {@code radius}, the Hall reads {@code anchors}, and so on. Tier 0 (nothing
 * built) uses {@code base}. A damaged upgrade works one tier lower until it is
 * repaired, and a neglected Citadel loses the upgrade entirely from the
 * {@code fails_at} stage of neglect on.
 *
 * <pre>{"name": "Wards", "description": "...", "order": 0, "fails_at": "hungry",
 *  "base": {"radius": 0},
 *  "tiers": [{"cost": [{"item": "minecraft:stone_bricks", "count": 32}], "values": {"radius": 24}}]}</pre>
 *
 * @param name        what the Gateway calls it
 * @param description one line on what it does
 * @param order       where it stands in the Gateway's list
 * @param failsAt     the stage of neglect from which it stops working; empty if neglect never touches it
 * @param base        its values with nothing built
 * @param tiers       what each tier costs and gives, from the first
 */
public record CitadelUpgrade(String name, String description, int order, Optional<Neglect> failsAt,
                             Map<String, Double> base, List<Tier> tiers) {

    /** One tier: what it costs, what it gives, and how it reads on the Gateway. */
    public record Tier(List<Cost> cost, Map<String, Double> values, String summary) {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Cost.CODEC.listOf().optionalFieldOf("cost", List.of()).forGetter(Tier::cost),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("values", Map.of()).forGetter(Tier::values),
                Codec.STRING.optionalFieldOf("summary", "").forGetter(Tier::summary)
        ).apply(instance, Tier::new));

        public Tier {
            cost = List.copyOf(cost);
            values = Map.copyOf(values);
        }
    }

    public static final Codec<CitadelUpgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CitadelUpgrade::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(CitadelUpgrade::description),
            Codec.INT.optionalFieldOf("order", 0).forGetter(CitadelUpgrade::order),
            Neglect.CODEC.optionalFieldOf("fails_at").forGetter(CitadelUpgrade::failsAt),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("base", Map.of()).forGetter(CitadelUpgrade::base),
            Tier.CODEC.listOf(1, 8).fieldOf("tiers").forGetter(CitadelUpgrade::tiers)
    ).apply(instance, CitadelUpgrade::new));

    public CitadelUpgrade {
        base = Map.copyOf(base);
        tiers = List.copyOf(tiers);
    }

    public int maxTier() {
        return tiers.size();
    }

    /** A value at a tier (0 = nothing built); a tier that does not name it keeps the last tier's, then the base. */
    public double value(int tier, String key) {
        double value = base.getOrDefault(key, 0.0);
        for (int i = 0; i < Math.min(tier, tiers.size()); i++) {
            value = tiers.get(i).values().getOrDefault(key, value);
        }
        return value;
    }

    /** Whether a Citadel at this stage of neglect keeps the upgrade working. */
    public boolean worksAt(Neglect stage) {
        return failsAt.map(fails -> stage.ordinal() < fails.ordinal()).orElse(true);
    }

    /** "Ward I", "Ward II": a tier's name as the Gateway shows it. */
    public static String numeral(int tier) {
        return switch (tier) {
            case 0 -> "-";
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(tier);
        };
    }
}
