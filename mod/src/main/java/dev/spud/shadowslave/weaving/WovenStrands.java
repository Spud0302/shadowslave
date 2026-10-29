package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The strands laid into one piece, in the order they were woven. Each takes
 * one of the piece's threads.
 */
public record WovenStrands(List<Laid> strands) {
    public static final WovenStrands EMPTY = new WovenStrands(List.of());

    public static final Codec<WovenStrands> CODEC = Laid.CODEC.listOf().xmap(WovenStrands::new, WovenStrands::strands);

    public WovenStrands {
        strands = List.copyOf(strands);
    }

    public int size() {
        return strands.size();
    }

    public boolean isEmpty() {
        return strands.isEmpty();
    }

    public WovenStrands with(Laid strand) {
        ArrayList<Laid> next = new ArrayList<>(strands);
        next.add(strand);
        return new WovenStrands(next);
    }

    public WovenStrands without(int index) {
        ArrayList<Laid> next = new ArrayList<>(strands);
        next.remove(index);
        return new WovenStrands(next);
    }

    /**
     * One strand in a piece.
     *
     * @param potency     how cleanly it was traced, 1 to 100: the share of its Tier's strength it gives
     * @param restriction the condition it was tuned to, if any
     * @param weaver      who laid it
     * @param fused       for a strand of a Weaver's own making, its name and the strands it fuses
     */
    public record Laid(ResourceLocation id, int tier, int potency, Optional<StrandRestriction> restriction, String weaver,
                       Optional<Fused> fused) {
        public static final Codec<Laid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Laid::id),
                Codec.intRange(1, StrandRules.MAX_TIER).fieldOf("tier").forGetter(Laid::tier),
                Codec.intRange(1, 100).fieldOf("potency").forGetter(Laid::potency),
                StrandRestriction.CODEC.optionalFieldOf("restriction").forGetter(Laid::restriction),
                Codec.STRING.optionalFieldOf("weaver", "").forGetter(Laid::weaver),
                Fused.CODEC.optionalFieldOf("fused").forGetter(Laid::fused)
        ).apply(instance, Laid::new));

        public Laid(ResourceLocation id, int tier, int potency, Optional<StrandRestriction> restriction, String weaver) {
            this(id, tier, potency, restriction, weaver, Optional.empty());
        }

        public Laid withPotency(int potency) {
            return new Laid(id, tier, Math.max(1, Math.min(100, potency)), restriction, weaver, fused);
        }
    }

    /**
     * A strand a Weaver made by fusing two learned ones (DESIGN, plan 2b:
     * copy, tune, create). It carries both parts so it works, reads and
     * teaches anywhere, and each part works at {@code share} percent.
     */
    public record Fused(String name, List<ResourceLocation> parts, int share) {
        public static final Codec<Fused> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Fused::name),
                ResourceLocation.CODEC.listOf().fieldOf("parts").forGetter(Fused::parts),
                Codec.intRange(1, 100).fieldOf("share").forGetter(Fused::share)
        ).apply(instance, Fused::new));

        public Fused {
            parts = List.copyOf(parts);
        }
    }
}
