package dev.spud.shadowslave.weaving;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * Who a Bound Memory answers to. A piece that holds the Bound strand but no
 * {@code BoundTo} has not bonded yet: it bonds to the next player who holds
 * it in hand or wears it, so a Weaver can bind a piece for someone else.
 */
public record BoundTo(UUID owner, String name) {
    public static final Codec<BoundTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(BoundTo::owner),
            Codec.STRING.fieldOf("name").forGetter(BoundTo::name)
    ).apply(instance, BoundTo::new));
}
