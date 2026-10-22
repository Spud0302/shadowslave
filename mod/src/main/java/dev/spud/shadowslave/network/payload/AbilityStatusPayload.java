package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client state of the player's Aspect ability for the HUD slot:
 * which ability the key fires, how long until it is ready, and whether a
 * press has primed it. An empty id means the player has no ability yet.
 */
public record AbilityStatusPayload(
        String abilityId,
        String abilityName,
        int remainingTicks,
        int cooldownTicks,
        boolean primed
) implements CustomPacketPayload {
    public static final AbilityStatusPayload NONE = new AbilityStatusPayload("", "", 0, 0, false);
    public static final Type<AbilityStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_status")
    );

    public static final StreamCodec<ByteBuf, AbilityStatusPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), AbilityStatusPayload::abilityId,
            ByteBufCodecs.stringUtf8(128), AbilityStatusPayload::abilityName,
            ByteBufCodecs.VAR_INT, AbilityStatusPayload::remainingTicks,
            ByteBufCodecs.VAR_INT, AbilityStatusPayload::cooldownTicks,
            ByteBufCodecs.BOOL, AbilityStatusPayload::primed,
            AbilityStatusPayload::new
    );

    public AbilityStatusPayload {
        remainingTicks = Math.max(0, remainingTicks);
        cooldownTicks = Math.max(0, cooldownTicks);
    }

    public boolean hasAbility() {
        return !abilityId.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
