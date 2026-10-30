package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;

import java.util.Collection;
import java.util.Optional;

/** The pure rules of Echoes, kept free of the game so they can be tested alone. */
public final class EchoRules {
    private EchoRules() {
    }

    /** Echoes a soul can hold: none before the Spell names a Rank. */
    public static int slots(EchoSettings settings, Optional<SoulRank> rank) {
        return rank.map(settings::slots).orElse(0);
    }

    /** The weakest soul shard that can anchor a bond to a creature of {@code rank}. */
    public static ShardRank anchorFor(CreatureRank rank) {
        return rank.shard().orElse(ShardRank.CORRUPTED);
    }

    /**
     * Why a remnant of {@code rank} can't be bound yet, or empty if it can.
     * {@code anchor} is the soul shard in the table's anchor slot, if any.
     */
    public static Optional<String> bindLock(EchoSettings settings, CreatureRank rank, int bindingLevel,
                                            Optional<ShardRank> anchor, float essence) {
        int needed = settings.bindingLevel(rank);
        if (bindingLevel < needed) {
            return Optional.of("Needs Binding level " + needed + " (yours is " + bindingLevel + ")");
        }
        ShardRank wanted = anchorFor(rank);
        if (anchor.isEmpty() || anchor.get().tier() < wanted.tier()) {
            return Optional.of("Anchor it with a " + CreatureRank.of(wanted).displayName() + " soul shard or better");
        }
        int cost = settings.bindEssence(rank);
        if (essence < cost) {
            return Optional.of("Binding takes " + cost + " essence");
        }
        return Optional.empty();
    }

    /** Essence a second the summoned Echoes cost together. */
    public static float upkeep(EchoSettings settings, Collection<EchoRecord> summoned) {
        float total = 0.0F;
        for (EchoRecord echo : summoned) {
            total += settings.upkeep(echo.body().rank());
        }
        return total;
    }

    /** An Echo after resting {@code seconds} in the soul: it heals a share of its health each second. */
    public static EchoRecord rested(EchoSettings settings, EchoRecord echo, int seconds) {
        if (echo.health() >= echo.body().maxHealth()) {
            return echo;
        }
        return echo.withHealth(echo.health() + echo.body().maxHealth() * settings.restPerSecond() * seconds);
    }

    /** The remnant chance for a creature, after any per-creature override. */
    public static float dropChance(EchoSettings settings, CreatureRank rank, Optional<EchoCreature> override) {
        return override.flatMap(EchoCreature::dropChance).orElseGet(() -> settings.dropChance(rank));
    }
}
