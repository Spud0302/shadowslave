package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * Marks a living creature in the world as a summoned Echo: whose it is and
 * which of their Echoes it is. A {@code lesser} body is a copy an Echo Tyrant
 * spawned; it fights for the same owner but is no one's Echo and leaves with
 * the Echo that made it.
 */
public record EchoBond(UUID owner, UUID echo, boolean lesser) {
    public static final Codec<EchoBond> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(EchoBond::owner),
            UUIDUtil.CODEC.fieldOf("echo").forGetter(EchoBond::echo),
            Codec.BOOL.optionalFieldOf("lesser", false).forGetter(EchoBond::lesser)
    ).apply(instance, EchoBond::new));
}
