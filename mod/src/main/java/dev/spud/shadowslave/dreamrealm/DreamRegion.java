package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.gate.GateCategory;
import dev.spud.shadowslave.gate.GateForecast;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One region of the Dream Realm, read from {@code data/<ns>/dream_realm/region/*.json}
 * (DESIGN, Jack 2026-10-23: one dimension with rank regions). A region is a
 * ring around the Dream Realm's centre, from {@code min_distance} up to (not
 * including) {@code max_distance} blocks out. Harder regions sit further out
 * and hold harder Seeds: a Seed takes its Category forecast from the region it
 * grows in, and cleansing it pays out {@code cleanse_loot}. {@code danger}
 * is the rank the Field Guide rates the region at before anything is seen there.
 */
public record DreamRegion(String displayName, int minDistance, int maxDistance, GateForecast seedForecast,
                          ResourceLocation cleanseLoot, String arrival, Optional<CreatureRank> danger) {
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
            Codec.STRING.optionalFieldOf("arrival", "").forGetter(DreamRegion::arrival),
            CreatureRank.CODEC.optionalFieldOf("danger").forGetter(DreamRegion::danger)
    ).apply(instance, DreamRegion::new)).validate(region -> region.minDistance < region.maxDistance
            ? DataResult.success(region)
            : DataResult.error(() -> "min_distance must be below max_distance"));

    public DreamRegion(String displayName, int minDistance, int maxDistance, GateForecast seedForecast,
                       ResourceLocation cleanseLoot, String arrival) {
        this(displayName, minDistance, maxDistance, seedForecast, cleanseLoot, arrival, Optional.empty());
    }

    /**
     * The region's baseline danger rating for the Field Guide: its
     * {@code danger} rank, or else the Guardian rank of the weakest Category
     * its Seeds can grow into.
     */
    public CreatureRank dangerRank() {
        return danger.orElseGet(() -> GateCategory.guardianRank(
                seedForecast.odds().keySet().stream().mapToInt(Integer::intValue).min().orElse(GateCategory.MIN)));
    }

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
