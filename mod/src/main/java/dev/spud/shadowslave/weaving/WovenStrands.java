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
     */
    public record Laid(ResourceLocation id, int tier, int potency, Optional<StrandRestriction> restriction, String weaver) {
        public static final Codec<Laid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Laid::id),
                Codec.intRange(1, StrandRules.MAX_TIER).fieldOf("tier").forGetter(Laid::tier),
                Codec.intRange(1, 100).fieldOf("potency").forGetter(Laid::potency),
                StrandRestriction.CODEC.optionalFieldOf("restriction").forGetter(Laid::restriction),
                Codec.STRING.optionalFieldOf("weaver", "").forGetter(Laid::weaver)
        ).apply(instance, Laid::new));

        public Laid withPotency(int potency) {
            return new Laid(id, tier, Math.max(1, Math.min(100, potency)), restriction, weaver);
        }
    }
}
