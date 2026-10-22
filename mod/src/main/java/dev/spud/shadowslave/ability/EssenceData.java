package dev.spud.shadowslave.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/** The essence currently held in a player's core. The pool's size comes from {@link EssenceService}. */
public record EssenceData(float current) {
    public static final MapCodec<EssenceData> CODEC = Codec.FLOAT
            .optionalFieldOf("current", 0.0F)
            .xmap(EssenceData::new, EssenceData::current);

    public EssenceData {
        current = Float.isFinite(current) ? Math.max(0.0F, current) : 0.0F;
    }

    public static EssenceData empty() {
        return new EssenceData(0.0F);
    }

    public EssenceData refill(float amount, int capacity) {
        return new EssenceData(Math.min(capacity, current + amount));
    }

    public EssenceData spend(float amount) {
        return new EssenceData(current - amount);
    }

    public boolean covers(int cost) {
        return current + 1.0e-3F >= cost;
    }
}
