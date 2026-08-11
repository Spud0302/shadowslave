package dev.spud.shadowslave.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/** Java-owned persistent terrain anchor for the authored Blackwater Hook undertow line. */
public record BlackwaterHookAnchorData(Optional<Anchor> anchor) {
    private static final MapCodec<BlackwaterHookAnchorData> RAW_CODEC = Anchor.CODEC.optionalFieldOf("anchor")
            .xmap(BlackwaterHookAnchorData::new, BlackwaterHookAnchorData::anchor);

    public static final MapCodec<BlackwaterHookAnchorData> CODEC = RAW_CODEC.flatXmap(
            value -> {
                try {
                    return DataResult.success(new BlackwaterHookAnchorData(value.anchor()));
                } catch (IllegalArgumentException | NullPointerException exception) {
                    return DataResult.error(() -> "Invalid BlackwaterHookAnchorData: " + exception.getMessage());
                }
            },
            DataResult::success
    );

    public BlackwaterHookAnchorData {
        anchor = Objects.requireNonNull(anchor, "anchor");
    }

    public static BlackwaterHookAnchorData empty() {
        return new BlackwaterHookAnchorData(Optional.empty());
    }

    public BlackwaterHookAnchorData anchored(String dimension, long blockPos) {
        return new BlackwaterHookAnchorData(Optional.of(new Anchor(dimension, blockPos)));
    }

    public BlackwaterHookAnchorData clear() {
        return empty();
    }

    public record Anchor(String dimension, long blockPos) {
        public static final Codec<Anchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("dimension").forGetter(Anchor::dimension),
                Codec.LONG.fieldOf("block_pos").forGetter(Anchor::blockPos)
        ).apply(instance, Anchor::new));

        public Anchor {
            dimension = Objects.requireNonNull(dimension, "dimension").trim();
            if (dimension.isEmpty()) {
                throw new IllegalArgumentException("dimension cannot be blank");
            }
        }
    }
}
