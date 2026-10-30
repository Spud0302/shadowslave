package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/** A True Name written on a Field Note: whose it is and the name itself. */
public record WrittenName(UUID bearer, String bearerName, String name) {
    public static final Codec<WrittenName> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("bearer").forGetter(WrittenName::bearer),
            Codec.STRING.fieldOf("bearer_name").forGetter(WrittenName::bearerName),
            Codec.STRING.fieldOf("name").forGetter(WrittenName::name)
    ).apply(instance, WrittenName::new));
}
