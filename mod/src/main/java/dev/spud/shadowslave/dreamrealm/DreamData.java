package dev.spud.shadowslave.dreamrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * A player's tie to the Dream Realm: their Dream Anchor (the last Dream
 * Candle they lit there), whether they chose natural Seeds when the server
 * makes them opt-in, and, while they dream, the bed they will wake in and the
 * sleeping body they left on it.
 */
public record DreamData(Optional<BlockPos> anchor, boolean seedsOptIn, Optional<Dreaming> dreaming) {
    /** Where a dreamer wakes, and the body that waits there if the server keeps one. */
    public record Dreaming(GlobalPos bed, Optional<UUID> body) {
        public static final Codec<Dreaming> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.fieldOf("bed").forGetter(Dreaming::bed),
                UUIDUtil.CODEC.optionalFieldOf("body").forGetter(Dreaming::body)
        ).apply(instance, Dreaming::new));
    }

    public static final Codec<DreamData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf("anchor").forGetter(DreamData::anchor),
            Codec.BOOL.optionalFieldOf("seeds_opt_in", false).forGetter(DreamData::seedsOptIn),
            Dreaming.CODEC.optionalFieldOf("dreaming").forGetter(DreamData::dreaming)
    ).apply(instance, DreamData::new));

    public static DreamData empty() {
        return new DreamData(Optional.empty(), false, Optional.empty());
    }

    public DreamData withAnchor(BlockPos pos) {
        return new DreamData(Optional.of(pos.immutable()), seedsOptIn, dreaming);
    }

    public DreamData withSeedsOptIn(boolean optIn) {
        return new DreamData(anchor, optIn, dreaming);
    }

    public DreamData dreamingFrom(GlobalPos bed, Optional<UUID> body) {
        return new DreamData(anchor, seedsOptIn, Optional.of(new Dreaming(bed, body)));
    }

    public DreamData awake() {
        return new DreamData(anchor, seedsOptIn, Optional.empty());
    }

    public boolean isDreaming() {
        return dreaming.isPresent();
    }
}
