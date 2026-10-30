package dev.spud.shadowslave.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * A Gateway's hunger (DESIGN, Run C, Jack 2026-10-30: scaled by member count),
 * read from {@code data/<ns>/citadel/upkeep.json}. A claimed Citadel's
 * provisions drain every in-game day by {@code base_per_day} plus
 * {@code per_member_per_day} for each member of its clan. Members feed it soul
 * shards and materials at the Gateway. Once the provisions run out, neglect
 * deepens in stages; it never costs the clan the Citadel.
 *
 * @param basePerDay       provisions eaten each day whatever the clan's size
 * @param perMemberPerDay  provisions eaten each day for every member
 * @param capacityDays     how many days of provisions the Gateway can hold
 * @param claimDays        how many days of provisions a freshly claimed Citadel holds
 * @param starvingDays     days at no provisions before Hungry becomes Starving
 * @param forsakenDays     days at no provisions before Starving becomes Forsaken
 * @param feed             what feeds the Gateway, and how much each item is worth
 */
public record CitadelUpkeep(double basePerDay, double perMemberPerDay, double capacityDays, double claimDays,
                            double starvingDays, double forsakenDays, List<Cost> feed) {
    public static final long DAY_TICKS = 24000L;

    public static final CitadelUpkeep DEFAULT = new CitadelUpkeep(40, 20, 14, 3, 2, 5, List.of());

    public static final Codec<CitadelUpkeep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.doubleRange(0, 100_000).optionalFieldOf("base_per_day", DEFAULT.basePerDay).forGetter(CitadelUpkeep::basePerDay),
            Codec.doubleRange(0, 100_000).optionalFieldOf("per_member_per_day", DEFAULT.perMemberPerDay).forGetter(CitadelUpkeep::perMemberPerDay),
            Codec.doubleRange(0.5, 1000).optionalFieldOf("capacity_days", DEFAULT.capacityDays).forGetter(CitadelUpkeep::capacityDays),
            Codec.doubleRange(0, 1000).optionalFieldOf("claim_days", DEFAULT.claimDays).forGetter(CitadelUpkeep::claimDays),
            Codec.doubleRange(0, 1000).optionalFieldOf("starving_days", DEFAULT.starvingDays).forGetter(CitadelUpkeep::starvingDays),
            Codec.doubleRange(0, 1000).optionalFieldOf("forsaken_days", DEFAULT.forsakenDays).forGetter(CitadelUpkeep::forsakenDays),
            Cost.CODEC.listOf().optionalFieldOf("feed", DEFAULT.feed).forGetter(CitadelUpkeep::feed)
    ).apply(instance, CitadelUpkeep::new));

    public CitadelUpkeep {
        feed = List.copyOf(feed);
    }

    /** Provisions a clan of {@code members} eats in a day. */
    public double perDay(int members) {
        return basePerDay + perMemberPerDay * Math.max(1, members);
    }

    public double capacity(int members) {
        return perDay(members) * capacityDays;
    }

    /** How neglected a Gateway with these provisions is, after {@code hungryTicks} at none. */
    public Neglect stage(double provisions, long hungryTicks) {
        if (provisions > 0) {
            return Neglect.FED;
        }
        if (hungryTicks >= forsakenDays * DAY_TICKS) {
            return Neglect.FORSAKEN;
        }
        if (hungryTicks >= starvingDays * DAY_TICKS) {
            return Neglect.STARVING;
        }
        return Neglect.HUNGRY;
    }

    /** What one of this item feeds the Gateway; empty if it feeds nothing. The first entry that matches wins. */
    public Optional<Double> worth(ItemStack stack) {
        return feed.stream().filter(entry -> entry.matches(stack)).findFirst().map(Cost::points).filter(points -> points > 0);
    }
}
