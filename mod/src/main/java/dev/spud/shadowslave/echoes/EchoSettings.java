package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.soul.SoulRank;

import java.util.List;

/**
 * Echo tuning, read from {@code data/<ns>/echo/settings.json} (DESIGN). Every
 * list is indexed by rank, Dormant first; a list shorter than the ranks
 * repeats its last value.
 *
 * <ul>
 *   <li>{@code slots}: Echoes a soul can hold, by Soul Rank (Jack: Awakened 1,
 *       Master 2, Saint 3).</li>
 *   <li>{@code dropChance}: the chance a slain creature leaves an echo remnant,
 *       by creature rank.</li>
 *   <li>{@code bindingLevel}, {@code bindEssence}, {@code bindXp}: what binding a
 *       remnant of that creature rank needs and earns at the Weaving Table.</li>
 *   <li>{@code summonEssence} and {@code upkeep} (essence a second) for an Echo
 *       of that creature rank.</li>
 *   <li>{@code passiveCreatures}: whether animals and other creatures that are
 *       no one's enemy can leave remnants too, as Dormant.</li>
 *   <li>{@code restPerSecond}: the share of its health an Echo resting in the
 *       soul regains each second.</li>
 * </ul>
 */
public record EchoSettings(
        List<Integer> slots,
        List<Float> dropChance,
        List<Integer> bindingLevel,
        List<Integer> bindEssence,
        List<Integer> bindXp,
        List<Integer> summonEssence,
        List<Float> upkeep,
        boolean passiveCreatures,
        float restPerSecond,
        double followDistance,
        double teleportDistance,
        double guardRadius,
        double attackReach
) {
    public static final EchoSettings DEFAULT = new EchoSettings(
            List.of(0, 1, 2, 3),
            List.of(0.004F, 0.008F, 0.015F, 0.03F),
            List.of(1, 3, 5, 7),
            List.of(40, 80, 140, 200),
            List.of(30, 60, 120, 200),
            List.of(10, 20, 35, 50),
            List.of(0.5F, 1.0F, 1.75F, 2.5F),
            true, 0.02F, 4.0, 24.0, 16.0, 32.0);

    public static final Codec<EchoSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 64).listOf(1, 16).optionalFieldOf("slots", DEFAULT.slots).forGetter(EchoSettings::slots),
            Codec.floatRange(0.0F, 1.0F).listOf(1, 16).optionalFieldOf("drop_chance", DEFAULT.dropChance).forGetter(EchoSettings::dropChance),
            Codec.intRange(0, 7).listOf(1, 16).optionalFieldOf("binding_level", DEFAULT.bindingLevel).forGetter(EchoSettings::bindingLevel),
            Codec.intRange(0, 10_000).listOf(1, 16).optionalFieldOf("bind_essence", DEFAULT.bindEssence).forGetter(EchoSettings::bindEssence),
            Codec.intRange(0, 10_000).listOf(1, 16).optionalFieldOf("bind_xp", DEFAULT.bindXp).forGetter(EchoSettings::bindXp),
            Codec.intRange(0, 10_000).listOf(1, 16).optionalFieldOf("summon_essence", DEFAULT.summonEssence).forGetter(EchoSettings::summonEssence),
            Codec.floatRange(0.0F, 1000.0F).listOf(1, 16).optionalFieldOf("upkeep", DEFAULT.upkeep).forGetter(EchoSettings::upkeep),
            Codec.BOOL.optionalFieldOf("passive_creatures", DEFAULT.passiveCreatures).forGetter(EchoSettings::passiveCreatures),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("rest_per_second", DEFAULT.restPerSecond).forGetter(EchoSettings::restPerSecond),
            Codec.doubleRange(1.0, 64.0).optionalFieldOf("follow_distance", DEFAULT.followDistance).forGetter(EchoSettings::followDistance),
            Codec.doubleRange(4.0, 128.0).optionalFieldOf("teleport_distance", DEFAULT.teleportDistance).forGetter(EchoSettings::teleportDistance),
            Codec.doubleRange(1.0, 64.0).optionalFieldOf("guard_radius", DEFAULT.guardRadius).forGetter(EchoSettings::guardRadius),
            Codec.doubleRange(1.0, 128.0).optionalFieldOf("attack_reach", DEFAULT.attackReach).forGetter(EchoSettings::attackReach)
    ).apply(instance, EchoSettings::new));

    public EchoSettings {
        slots = List.copyOf(slots);
        dropChance = List.copyOf(dropChance);
        bindingLevel = List.copyOf(bindingLevel);
        bindEssence = List.copyOf(bindEssence);
        bindXp = List.copyOf(bindXp);
        summonEssence = List.copyOf(summonEssence);
        upkeep = List.copyOf(upkeep);
    }

    static <T> T at(List<T> values, int index) {
        return values.get(Math.max(0, Math.min(values.size() - 1, index)));
    }

    public int slots(SoulRank rank) {
        return at(slots, rank.ordinal());
    }

    public float dropChance(CreatureRank rank) {
        return at(dropChance, rank.ordinal());
    }

    public int bindingLevel(CreatureRank rank) {
        return at(bindingLevel, rank.ordinal());
    }

    public int bindEssence(CreatureRank rank) {
        return at(bindEssence, rank.ordinal());
    }

    public int bindXp(CreatureRank rank) {
        return at(bindXp, rank.ordinal());
    }

    public int summonEssence(CreatureRank rank) {
        return at(summonEssence, rank.ordinal());
    }

    public float upkeep(CreatureRank rank) {
        return at(upkeep, rank.ordinal());
    }
}
