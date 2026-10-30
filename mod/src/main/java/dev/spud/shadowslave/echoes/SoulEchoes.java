package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Echoes resting in a player's soul (lore: Echoes are summoned from and
 * dismissed into the soul, ch. 47) and the standing order they keep. A
 * summoned Echo stays on this list; only its death or being drawn out to
 * hand over takes it off. Saved with the player and kept through death.
 */
public record SoulEchoes(List<EchoRecord> held, EchoCommand order) {
    public static final Codec<SoulEchoes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EchoRecord.CODEC.listOf().optionalFieldOf("held", List.of()).forGetter(SoulEchoes::held),
            EchoCommand.CODEC.optionalFieldOf("order", EchoCommand.GUARD).forGetter(SoulEchoes::order)
    ).apply(instance, SoulEchoes::new));
    public static final SoulEchoes EMPTY = new SoulEchoes(List.of(), EchoCommand.GUARD);

    public SoulEchoes {
        held = List.copyOf(held);
        order = order.standing() ? order : EchoCommand.GUARD;
    }

    public int size() {
        return held.size();
    }

    public boolean isEmpty() {
        return held.isEmpty();
    }

    public Optional<EchoRecord> find(UUID id) {
        return held.stream().filter(echo -> echo.id().equals(id)).findFirst();
    }

    public int indexOf(UUID id) {
        for (int i = 0; i < held.size(); i++) {
            if (held.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public SoulEchoes with(EchoRecord echo) {
        List<EchoRecord> next = new ArrayList<>(held);
        next.removeIf(other -> other.id().equals(echo.id()));
        next.add(echo);
        return new SoulEchoes(next, order);
    }

    /** The same list with {@code echo} replacing the one of the same id, in place. */
    public SoulEchoes updated(EchoRecord echo) {
        List<EchoRecord> next = new ArrayList<>(held);
        next.replaceAll(other -> other.id().equals(echo.id()) ? echo : other);
        return new SoulEchoes(next, order);
    }

    public SoulEchoes without(UUID id) {
        List<EchoRecord> next = new ArrayList<>(held);
        next.removeIf(other -> other.id().equals(id));
        return new SoulEchoes(next, order);
    }

    public SoulEchoes ordered(EchoCommand next) {
        return new SoulEchoes(held, next);
    }
}
