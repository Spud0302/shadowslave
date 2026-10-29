package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server-to-client words of a Nightmare's appraisal (after ch. 15-16): the deeds
 * the Spell counted, the grade and its comment, and the boons. Display text only;
 * the boons themselves are granted on the server.
 */
public record NightmareAppraisalPayload(String nightmare, String setting, String ending, List<String> deeds,
                                        String grade, String comment, List<String> boons, String lore)
        implements CustomPacketPayload {
    public static final Type<NightmareAppraisalPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "nightmare_appraisal"));

    private static final StreamCodec<ByteBuf, String> TEXT = ByteBufCodecs.stringUtf8(1024);
    private static final StreamCodec<ByteBuf, List<String>> LINES = TEXT.apply(ByteBufCodecs.list(64));

    public static final StreamCodec<ByteBuf, NightmareAppraisalPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NightmareAppraisalPayload decode(ByteBuf buffer) {
            return new NightmareAppraisalPayload(TEXT.decode(buffer), TEXT.decode(buffer), TEXT.decode(buffer), LINES.decode(buffer),
                    TEXT.decode(buffer), TEXT.decode(buffer), LINES.decode(buffer), TEXT.decode(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, NightmareAppraisalPayload value) {
            TEXT.encode(buffer, value.nightmare);
            TEXT.encode(buffer, value.setting);
            TEXT.encode(buffer, value.ending);
            LINES.encode(buffer, value.deeds);
            TEXT.encode(buffer, value.grade);
            TEXT.encode(buffer, value.comment);
            LINES.encode(buffer, value.boons);
            TEXT.encode(buffer, value.lore);
        }
    };

    public NightmareAppraisalPayload {
        deeds = List.copyOf(deeds.subList(0, Math.min(64, deeds.size())));
        boons = List.copyOf(boons.subList(0, Math.min(64, boons.size())));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
