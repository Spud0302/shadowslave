package dev.spud.shadowslave.network.payload;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Server-to-client reading of the player's Echoes: how many the soul holds,
 * each one and whether it is out, the standing order, what the summoned ones
 * cost a second, and, while a Weaving Table holds an echo remnant, what
 * binding it needs. Text is worded on the server.
 */
public record EchoStatusPayload(int slots, String order, float upkeep, List<Echo> held, Optional<Binding> binding)
        implements CustomPacketPayload {
    public static final EchoStatusPayload NONE = new EchoStatusPayload(0, "guard", 0.0F, List.of(), Optional.empty());

    /** One Echo in the soul; {@code entity} is its creature's entity id. */
    public record Echo(String name, String entity, String rank, float health, float maxHealth, boolean summoned, float upkeep, String binder) {
        static final Codec<Echo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Echo::name),
                Codec.STRING.fieldOf("entity").forGetter(Echo::entity),
                Codec.STRING.fieldOf("rank").forGetter(Echo::rank),
                Codec.FLOAT.fieldOf("health").forGetter(Echo::health),
                Codec.FLOAT.fieldOf("max_health").forGetter(Echo::maxHealth),
                Codec.BOOL.fieldOf("summoned").forGetter(Echo::summoned),
                Codec.FLOAT.fieldOf("upkeep").forGetter(Echo::upkeep),
                Codec.STRING.fieldOf("binder").forGetter(Echo::binder)
        ).apply(instance, Echo::new));
    }

    /**
     * The remnant on an open Weaving Table: what it would become, the Binding
     * level and essence it takes, the anchor it wants, and why it can't be
     * bound yet ({@code lock}, empty when it can).
     */
    public record Binding(String name, String rank, int level, int held, int essence, String anchor, String lock) {
        static final Codec<Binding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Binding::name),
                Codec.STRING.fieldOf("rank").forGetter(Binding::rank),
                Codec.INT.fieldOf("level").forGetter(Binding::level),
                Codec.INT.fieldOf("held").forGetter(Binding::held),
                Codec.INT.fieldOf("essence").forGetter(Binding::essence),
                Codec.STRING.fieldOf("anchor").forGetter(Binding::anchor),
                Codec.STRING.fieldOf("lock").forGetter(Binding::lock)
        ).apply(instance, Binding::new));
    }

    static final Codec<EchoStatusPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slots").forGetter(EchoStatusPayload::slots),
            Codec.STRING.fieldOf("order").forGetter(EchoStatusPayload::order),
            Codec.FLOAT.fieldOf("upkeep").forGetter(EchoStatusPayload::upkeep),
            Echo.CODEC.listOf().fieldOf("held").forGetter(EchoStatusPayload::held),
            Binding.CODEC.optionalFieldOf("binding").forGetter(EchoStatusPayload::binding)
    ).apply(instance, EchoStatusPayload::new));

    public static final Type<EchoStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo_status"));
    public static final StreamCodec<ByteBuf, EchoStatusPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public int summoned() {
        return (int) held.stream().filter(Echo::summoned).count();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
