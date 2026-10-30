package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureRank;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * A per-creature override, read from {@code data/<ns>/echo/creature/*.json}:
 * the creature rank its Echo counts as, its own remnant chance, or
 * {@code remnant: false} to leave no remnant at all. Creatures without a file
 * use their shard rank and the settings' chance; the entity type tag
 * {@code shadowslave:echo/no_remnant} also leaves no remnant.
 */
public record EchoCreature(ResourceLocation entity, Optional<CreatureRank> rank, Optional<Float> dropChance, boolean remnant) {
    public static final Codec<EchoCreature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(EchoCreature::entity),
            CreatureRank.CODEC.optionalFieldOf("rank").forGetter(EchoCreature::rank),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("drop_chance").forGetter(EchoCreature::dropChance),
            Codec.BOOL.optionalFieldOf("remnant", true).forGetter(EchoCreature::remnant)
    ).apply(instance, EchoCreature::new));
}
