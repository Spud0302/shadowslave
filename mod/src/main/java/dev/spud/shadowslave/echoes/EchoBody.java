package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.creature.CreatureRank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * What a slain creature leaves for an Echo to be made of (lore: an Echo is a
 * soulless copy of the creature, ch. 47): its kind, the name and rank it had,
 * and its saved body, so the Echo comes back with the same gear, variant and
 * moves. {@code kind} is a Nightmare Creature's Class ("Monster"), or empty.
 */
public record EchoBody(ResourceLocation entity, String name, CreatureRank rank, String kind, CompoundTag tag, float maxHealth) {
    public static final Codec<EchoBody> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(EchoBody::entity),
            Codec.STRING.fieldOf("name").forGetter(EchoBody::name),
            CreatureRank.CODEC.optionalFieldOf("rank", CreatureRank.DORMANT).forGetter(EchoBody::rank),
            Codec.STRING.optionalFieldOf("kind", "").forGetter(EchoBody::kind),
            CompoundTag.CODEC.optionalFieldOf("body", new CompoundTag()).forGetter(EchoBody::tag),
            Codec.floatRange(0.0F, 100_000.0F).optionalFieldOf("max_health", 20.0F).forGetter(EchoBody::maxHealth)
    ).apply(instance, EchoBody::new));

    public EchoBody {
        Objects.requireNonNull(entity, "entity");
        name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(rank, "rank");
        kind = Objects.requireNonNull(kind, "kind");
        tag = tag.copy();
    }

    /** "Awakened Monster", or just "Dormant" for a creature without a Class. */
    public String rankText() {
        return kind.isEmpty() ? rank.displayName() : rank.displayName() + " " + kind;
    }

    @Override
    public CompoundTag tag() {
        return tag.copy();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EchoBody body && entity.equals(body.entity) && name.equals(body.name) && rank == body.rank
                && kind.equals(body.kind) && tag.equals(body.tag) && Float.compare(maxHealth, body.maxHealth) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, name, rank, kind, tag, maxHealth);
    }
}
