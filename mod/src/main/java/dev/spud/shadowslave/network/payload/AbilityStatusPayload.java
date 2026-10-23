package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * Server-to-client state of the player's Aspect abilities for the HUD: one
 * slot per ability key, in key order, and the essence pool they share. The
 * client predicts the pool refilling between updates from {@code regenPerTick}.
 * No slots means the player has no ability yet. {@code strikePrimed} is set
 * while channeled essence waits in the player's next melee blow. {@code rage}
 * is Fury's meter, {@link Rage#NONE} for every other Nature.
 */
public record AbilityStatusPayload(
        List<Slot> slots,
        float essence,
        int capacity,
        float regenPerTick,
        boolean strikePrimed,
        Rage rage
) implements CustomPacketPayload {
    public static final int MAX_SLOTS = 8;
    public static final AbilityStatusPayload NONE = new AbilityStatusPayload(List.of(), 0.0F, 0, 0.0F, false, Rage.NONE);
    public static final Type<AbilityStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_status")
    );

    /** One ability key: what it fires, its essence cost, and whether a press has primed it. */
    public record Slot(String abilityId, String abilityName, int cost, boolean primed) {
        public static final StreamCodec<ByteBuf, Slot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(256), Slot::abilityId,
                ByteBufCodecs.stringUtf8(128), Slot::abilityName,
                ByteBufCodecs.VAR_INT, Slot::cost,
                ByteBufCodecs.BOOL, Slot::primed,
                Slot::new
        );

        public Slot {
            Objects.requireNonNull(abilityId, "abilityId");
            Objects.requireNonNull(abilityName, "abilityName");
            cost = Math.max(0, cost);
        }
    }

    /** Fury's rage, 0 to 100, and whether Red Haze has taken hold; a negative value means no meter. */
    public record Rage(float value, boolean hazed) {
        public static final Rage NONE = new Rage(-1.0F, false);
        public static final StreamCodec<ByteBuf, Rage> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Rage::value,
                ByteBufCodecs.BOOL, Rage::hazed,
                Rage::new
        );

        public boolean shown() {
            return value >= 0.0F;
        }
    }

    public static final StreamCodec<ByteBuf, AbilityStatusPayload> STREAM_CODEC = StreamCodec.composite(
            Slot.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_SLOTS)), AbilityStatusPayload::slots,
            ByteBufCodecs.FLOAT, AbilityStatusPayload::essence,
            ByteBufCodecs.VAR_INT, AbilityStatusPayload::capacity,
            ByteBufCodecs.FLOAT, AbilityStatusPayload::regenPerTick,
            ByteBufCodecs.BOOL, AbilityStatusPayload::strikePrimed,
            Rage.STREAM_CODEC, AbilityStatusPayload::rage,
            AbilityStatusPayload::new
    );

    public AbilityStatusPayload {
        slots = List.copyOf(Objects.requireNonNull(slots, "slots"));
        if (slots.size() > MAX_SLOTS) {
            throw new IllegalArgumentException("Too many ability slots: " + slots.size());
        }
        capacity = Math.max(0, capacity);
        essence = Float.isFinite(essence) ? Math.max(0.0F, Math.min(capacity, essence)) : 0.0F;
        regenPerTick = Float.isFinite(regenPerTick) ? Math.max(0.0F, regenPerTick) : 0.0F;
        rage = Objects.requireNonNull(rage, "rage");
    }

    /** A status with no rage meter. */
    public AbilityStatusPayload(List<Slot> slots, float essence, int capacity, float regenPerTick, boolean strikePrimed) {
        this(slots, essence, capacity, regenPerTick, strikePrimed, Rage.NONE);
    }

    /** A status with no channeled strike waiting. */
    public AbilityStatusPayload(List<Slot> slots, float essence, int capacity, float regenPerTick) {
        this(slots, essence, capacity, regenPerTick, false);
    }

    public boolean hasAbility() {
        return !slots.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
