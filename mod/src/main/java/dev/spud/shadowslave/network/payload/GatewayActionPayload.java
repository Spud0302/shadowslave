package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client request from the Gateway screen: build an upgrade's next tier,
 * repair a damaged one, or travel through the Gateway Links to another of the
 * clan's Citadels. {@code target} is the upgrade's id or the Citadel's id. The
 * server checks everything again, including that the player still stands at
 * the Gateway.
 */
public record GatewayActionPayload(String citadel, int action, String target) implements CustomPacketPayload {
    public static final int UPGRADE = 0;
    public static final int REPAIR = 1;
    public static final int TRAVEL = 2;
    public static final Type<GatewayActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "gateway_action"));
    public static final StreamCodec<ByteBuf, GatewayActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), GatewayActionPayload::citadel,
            ByteBufCodecs.VAR_INT, GatewayActionPayload::action,
            ByteBufCodecs.stringUtf8(128), GatewayActionPayload::target,
            GatewayActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
