package dev.spud.shadowslave.echoes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * One bound Echo (DESIGN, after ch. 47): who it is ({@code id} never
 * changes, so a traded Echo is still the same Echo), what it was made from,
 * how hurt it is, and who bound it. It never levels; a stronger Echo is a
 * different Echo.
 */
public record EchoRecord(UUID id, EchoBody body, float health, String binder) {
    public static final Codec<EchoRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(EchoRecord::id),
            EchoBody.CODEC.fieldOf("of").forGetter(EchoRecord::body),
            Codec.floatRange(0.0F, 100_000.0F).fieldOf("health").forGetter(EchoRecord::health),
            Codec.STRING.optionalFieldOf("binder", "").forGetter(EchoRecord::binder)
    ).apply(instance, EchoRecord::new));

    public EchoRecord {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(body, "body");
        health = Math.max(0.0F, Math.min(body.maxHealth(), health));
        binder = Objects.requireNonNull(binder, "binder");
    }

    public static EchoRecord bind(EchoBody body, String binder) {
        return new EchoRecord(UUID.randomUUID(), body, body.maxHealth(), binder);
    }

    public EchoRecord withHealth(float next) {
        return new EchoRecord(id, body, next, binder);
    }

    /** "Zombie Echo". */
    public String name() {
        return body.name() + " Echo";
    }

    public float healthShare() {
        return body.maxHealth() <= 0 ? 1.0F : health / body.maxHealth();
    }
}
