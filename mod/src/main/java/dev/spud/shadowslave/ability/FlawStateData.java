package dev.spud.shadowslave.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/** Saved bookkeeping for Flaws that track time, currently Tidal Debt's last touch of water. */
public record FlawStateData(long lastWaterTouch) {
    public static final long NEVER = -1L;
    public static final MapCodec<FlawStateData> CODEC = Codec.LONG
            .optionalFieldOf("last_water_touch", NEVER)
            .xmap(FlawStateData::new, FlawStateData::lastWaterTouch);

    public static FlawStateData empty() {
        return new FlawStateData(NEVER);
    }

    public FlawStateData withWaterTouch(long gameTime) {
        return new FlawStateData(gameTime);
    }
}
