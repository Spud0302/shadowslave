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

/**
 * Server-to-client reading of a claimed Citadel's Gateway, for the Gateway
 * screen: its hunger, its upgrades and what the next tier or a repair costs,
 * any raid under way, and where its Gateway Links reach. Text is worded on
 * the server.
 *
 * @param stage      the stage of neglect, 0 (Fed) to 3 (Forsaken)
 * @param stageLine  what the clan has lost to neglect, or how long the provisions last
 * @param feedHint   what feeds the Gateway
 * @param raid       one line on the raid under way, or empty
 * @param travelCost essence per trip through the Gateway Links, or -1 while none are built
 * @param message    the result of the player's last action here, or empty
 */
public record GatewayStatusPayload(String citadel, String name, String clan, int members, String rank,
                                   float provisions, float capacity, float perDay, int stage, String stageLine,
                                   String feedHint, List<Upgrade> upgrades, int anchors, int anchorSlots,
                                   String raid, int travelCost, List<Link> links, String message) implements CustomPacketPayload {
    public static final Type<GatewayStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "gateway_status"));

    /** One upgrade track and its next step. Costs come with whether the player carries them. */
    public record Upgrade(String id, String name, String description, int tier, int maxTier, boolean damaged, boolean works,
                          String now, String next, List<Need> cost, List<Need> repair) {
        static final Codec<Upgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Upgrade::id),
                Codec.STRING.fieldOf("name").forGetter(Upgrade::name),
                Codec.STRING.fieldOf("description").forGetter(Upgrade::description),
                Codec.INT.fieldOf("tier").forGetter(Upgrade::tier),
                Codec.INT.fieldOf("max_tier").forGetter(Upgrade::maxTier),
                Codec.BOOL.fieldOf("damaged").forGetter(Upgrade::damaged),
                Codec.BOOL.fieldOf("works").forGetter(Upgrade::works),
                Codec.STRING.fieldOf("now").forGetter(Upgrade::now),
                Codec.STRING.fieldOf("next").forGetter(Upgrade::next),
                Need.CODEC.listOf().fieldOf("cost").forGetter(Upgrade::cost),
                Need.CODEC.listOf().fieldOf("repair").forGetter(Upgrade::repair)
        ).apply(instance, Upgrade::new));

        public boolean canPay() {
            return cost.stream().allMatch(Need::have);
        }

        public boolean canRepair() {
            return repair.stream().allMatch(Need::have);
        }
    }

    /** Some items asked for, and whether the player carries them. */
    public record Need(String text, boolean have) {
        static final Codec<Need> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("text").forGetter(Need::text),
                Codec.BOOL.fieldOf("have").forGetter(Need::have)
        ).apply(instance, Need::new));
    }

    /** Another Citadel of the clan the Gateway Links can reach. */
    public record Link(String citadel, String name, int distance) {
        static final Codec<Link> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("citadel").forGetter(Link::citadel),
                Codec.STRING.fieldOf("name").forGetter(Link::name),
                Codec.INT.fieldOf("distance").forGetter(Link::distance)
        ).apply(instance, Link::new));
    }

    private record Head(String citadel, String name, String clan, int members, String rank,
                        float provisions, float capacity, float perDay, int stage, String stageLine, String feedHint) {
        static final Codec<Head> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("citadel").forGetter(Head::citadel),
                Codec.STRING.fieldOf("name").forGetter(Head::name),
                Codec.STRING.fieldOf("clan").forGetter(Head::clan),
                Codec.INT.fieldOf("members").forGetter(Head::members),
                Codec.STRING.fieldOf("rank").forGetter(Head::rank),
                Codec.FLOAT.fieldOf("provisions").forGetter(Head::provisions),
                Codec.FLOAT.fieldOf("capacity").forGetter(Head::capacity),
                Codec.FLOAT.fieldOf("per_day").forGetter(Head::perDay),
                Codec.INT.fieldOf("stage").forGetter(Head::stage),
                Codec.STRING.fieldOf("stage_line").forGetter(Head::stageLine),
                Codec.STRING.fieldOf("feed_hint").forGetter(Head::feedHint)
        ).apply(instance, Head::new));
    }

    private static final Codec<GatewayStatusPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Head.CODEC.fieldOf("head").forGetter(payload -> new Head(payload.citadel, payload.name, payload.clan, payload.members,
                    payload.rank, payload.provisions, payload.capacity, payload.perDay, payload.stage, payload.stageLine, payload.feedHint)),
            Upgrade.CODEC.listOf().fieldOf("upgrades").forGetter(GatewayStatusPayload::upgrades),
            Codec.INT.fieldOf("anchors").forGetter(GatewayStatusPayload::anchors),
            Codec.INT.fieldOf("anchor_slots").forGetter(GatewayStatusPayload::anchorSlots),
            Codec.STRING.fieldOf("raid").forGetter(GatewayStatusPayload::raid),
            Codec.INT.fieldOf("travel_cost").forGetter(GatewayStatusPayload::travelCost),
            Link.CODEC.listOf().fieldOf("links").forGetter(GatewayStatusPayload::links),
            Codec.STRING.fieldOf("message").forGetter(GatewayStatusPayload::message)
    ).apply(instance, (head, upgrades, anchors, slots, raid, travel, links, message) -> new GatewayStatusPayload(head.citadel,
            head.name, head.clan, head.members, head.rank, head.provisions, head.capacity, head.perDay, head.stage, head.stageLine,
            head.feedHint, upgrades, anchors, slots, raid, travel, links, message)));

    public static final StreamCodec<ByteBuf, GatewayStatusPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public GatewayStatusPayload {
        upgrades = List.copyOf(upgrades);
        links = List.copyOf(links);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
