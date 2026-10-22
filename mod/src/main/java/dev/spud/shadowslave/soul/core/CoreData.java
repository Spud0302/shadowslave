package dev.spud.shadowslave.soul.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/** How saturated a player's soul core is, in saturation units. Its limit comes from {@link CoreRules}. */
public record CoreData(int saturation) {
    public static final MapCodec<CoreData> CODEC = Codec.INT
            .optionalFieldOf("saturation", 0)
            .xmap(CoreData::new, CoreData::saturation);

    public CoreData {
        saturation = Math.max(0, saturation);
    }

    public static CoreData empty() {
        return new CoreData(0);
    }
}
