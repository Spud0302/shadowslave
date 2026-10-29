package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The Memories a player has dismissed into their soul, most recent last
 * (lore: a Memory is stored in the soul as essence, ch. 3024). Saved with
 * the player and kept through death.
 */
public record SoulMemories(List<ItemStack> held) {
    public static final Codec<SoulMemories> CODEC = ItemStack.CODEC.listOf().xmap(SoulMemories::new, SoulMemories::held);
    public static final SoulMemories EMPTY = new SoulMemories(List.of());

    public SoulMemories {
        held = held.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    public int size() {
        return held.size();
    }

    public boolean isEmpty() {
        return held.isEmpty();
    }

    /** The Memory that comes when summoned: the one dismissed last. */
    public ItemStack next() {
        return held.isEmpty() ? ItemStack.EMPTY : held.getLast();
    }

    public SoulMemories with(ItemStack stack) {
        List<ItemStack> next = new ArrayList<>(held);
        next.add(stack);
        return new SoulMemories(next);
    }

    public SoulMemories withoutNext() {
        return held.isEmpty() ? this : new SoulMemories(held.subList(0, held.size() - 1));
    }

    /** Every Memory but the one at {@code index}. */
    public SoulMemories without(int index) {
        if (index < 0 || index >= held.size()) {
            return this;
        }
        List<ItemStack> next = new ArrayList<>(held);
        next.remove(index);
        return new SoulMemories(next);
    }

    /** Moves the oldest Memory to the front of the line, so a summon brings it next. */
    public SoulMemories cycled() {
        if (held.size() < 2) {
            return this;
        }
        List<ItemStack> next = new ArrayList<>(held.subList(1, held.size()));
        next.add(held.getFirst());
        return new SoulMemories(next);
    }
}
