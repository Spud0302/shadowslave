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
 * makes them opt-in, and, while they dream, the bed they will wake in, the
 * sleeping body they left on it, when they arrived, and whether they are Lost.
 */
public record DreamData(Optional<BlockPos> anchor, boolean seedsOptIn, Optional<Dreaming> dreaming) {
    /**
     * Where a dreamer wakes, the body that waits there if the server keeps one,
     * the game time they arrived (a Candle or Gateway only lets them through
     * after a wait, ch. 398), and whether their body died under them and left
     * them Lost (ch. 360, 435).
     */
    public record Dreaming(GlobalPos bed, Optional<UUID> body, long since, boolean lost) {
        public static final Codec<Dreaming> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.fieldOf("bed").forGetter(Dreaming::bed),
                UUIDUtil.CODEC.optionalFieldOf("body").forGetter(Dreaming::body),
                Codec.LONG.optionalFieldOf("since", 0L).forGetter(Dreaming::since),
                Codec.BOOL.optionalFieldOf("lost", false).forGetter(Dreaming::lost)
        ).apply(instance, Dreaming::new));

        /** The body is gone: the dreamer is Lost, with nothing in the waking world to return to. */
        public Dreaming asLost() {
            return new Dreaming(bed, Optional.empty(), since, true);
        }

        /** The body is remade (Ascension): the dreamer is no longer Lost, and carries their body from now on. */
        public Dreaming found() {
            return new Dreaming(bed, Optional.empty(), since, false);
        }
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

    public DreamData dreamingFrom(GlobalPos bed, Optional<UUID> body, long since) {
        return new DreamData(anchor, seedsOptIn, Optional.of(new Dreaming(bed, body, since, false)));
    }

    public DreamData withDreaming(Dreaming next) {
        return new DreamData(anchor, seedsOptIn, Optional.of(next));
    }

    public DreamData awake() {
        return new DreamData(anchor, seedsOptIn, Optional.empty());
    }

    public boolean isDreaming() {
        return dreaming.isPresent();
    }

    public boolean isLost() {
        return dreaming.filter(Dreaming::lost).isPresent();
    }
}
