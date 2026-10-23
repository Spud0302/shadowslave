package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.gate.GateCategory;
import dev.spud.shadowslave.gate.GateForecast;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One region of the Dream Realm, read from {@code data/<ns>/dream_realm/region/*.json}
 * (DESIGN, Jack 2026-10-23: one dimension with rank regions). A region is a
 * ring around the Dream Realm's centre, from {@code min_distance} up to (not
 * including) {@code max_distance} blocks out. Harder regions sit further out
 * and hold harder Seeds: a Seed takes its Category forecast from the region it
 * grows in, and cleansing it pays out {@code cleanse_loot}.
 */
public record DreamRegion(String displayName, int minDistance, int maxDistance, GateForecast seedForecast,
                          ResourceLocation cleanseLoot, String arrival) {
    private static final Codec<GateForecast> FORECAST_CODEC = Codec.unboundedMap(Codec.STRING, Codec.intRange(1, 100))
            .comapFlatMap(DreamRegion::forecast, forecast -> {
                Map<String, Integer> odds = new LinkedHashMap<>();
                forecast.odds().forEach((category, percent) -> odds.put(Integer.toString(category), percent));
                return odds;
            });

    public static final Codec<DreamRegion> CODEC = RecordCodecBuilder.<DreamRegion>create(instance -> instance.group(
            Codec.STRING.fieldOf("display_name").forGetter(DreamRegion::displayName),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("min_distance", 0).forGetter(DreamRegion::minDistance),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("max_distance", Integer.MAX_VALUE).forGetter(DreamRegion::maxDistance),
            FORECAST_CODEC.fieldOf("seed_forecast").forGetter(DreamRegion::seedForecast),
            ResourceLocation.CODEC.fieldOf("cleanse_loot").forGetter(DreamRegion::cleanseLoot),
            Codec.STRING.optionalFieldOf("arrival", "").forGetter(DreamRegion::arrival)
    ).apply(instance, DreamRegion::new)).validate(region -> region.minDistance < region.maxDistance
            ? DataResult.success(region)
            : DataResult.error(() -> "min_distance must be below max_distance"));

    public boolean contains(double distance) {
        return distance >= minDistance && distance < maxDistance;
    }

    private static DataResult<GateForecast> forecast(Map<String, Integer> raw) {
        Map<Integer, Integer> odds = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            int category;
            try {
                category = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException exception) {
                return DataResult.error(() -> "Seed forecast key is not a Category: " + entry.getKey());
            }
            if (category < GateCategory.MIN || category > GateCategory.MAX) {
                return DataResult.error(() -> "Seed forecast Category out of range: " + category);
            }
            odds.put(category, entry.getValue());
        }
        int total = odds.values().stream().mapToInt(Integer::intValue).sum();
        if (odds.isEmpty() || total != 100) {
            return DataResult.error(() -> "Seed forecast odds must add up to 100, got " + total);
        }
        return DataResult.success(new GateForecast(odds));
    }
}
