package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server-to-client view of a player's True Name and the names they know, for
 * the Soul screen's Name page; with {@code reveal}, the client opens the
 * Spell's reveal screen. Display only: every effect is decided on the server.
 *
 * @param name         the player's True Name, or empty
 * @param deedLine     what the Spell granted it for
 * @param spellLine    the Spell's words for the reveal screen
 * @param shown        whether the name shows as a title
 * @param drainResist  how much of a drain an Anchored Soul shrugs off, in percent
 * @param damageBonus  the extra damage from anyone who knows the name, in percent
 * @param bindEssence  essence it takes to weave the name into a Memory
 * @param known        the True Names this player knows
 */
public record TrueNamePayload(String name, String deedLine, String spellLine, boolean shown, int drainResist, int damageBonus,
                              int bindEssence, List<Known> known, boolean reveal) implements CustomPacketPayload {
    public static final Type<TrueNamePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "true_name"));
    public static final TrueNamePayload NONE = new TrueNamePayload("", "", "", false, 0, 0, 0, List.of(), false);
    private static final int MAX_KNOWN = 128;
    private static final StreamCodec<ByteBuf, String> TEXT = ByteBufCodecs.stringUtf8(512);

    /** A known name: the name, whose it is, and the game time it can be sensed again. */
    public record Known(String name, String bearer, long readyAt) {
    }

    private static final StreamCodec<ByteBuf, List<Known>> KNOWN = StreamCodec.composite(
            TEXT, Known::name,
            TEXT, Known::bearer,
            ByteBufCodecs.VAR_LONG, Known::readyAt,
            Known::new).apply(ByteBufCodecs.list(MAX_KNOWN));

    public static final StreamCodec<ByteBuf, TrueNamePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TrueNamePayload decode(ByteBuf buffer) {
            return new TrueNamePayload(TEXT.decode(buffer), TEXT.decode(buffer), TEXT.decode(buffer), ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer),
                    KNOWN.decode(buffer), ByteBufCodecs.BOOL.decode(buffer));
        }

        @Override
        public void encode(ByteBuf buffer, TrueNamePayload value) {
            TEXT.encode(buffer, value.name);
            TEXT.encode(buffer, value.deedLine);
            TEXT.encode(buffer, value.spellLine);
            ByteBufCodecs.BOOL.encode(buffer, value.shown);
            ByteBufCodecs.VAR_INT.encode(buffer, value.drainResist);
            ByteBufCodecs.VAR_INT.encode(buffer, value.damageBonus);
            ByteBufCodecs.VAR_INT.encode(buffer, value.bindEssence);
            KNOWN.encode(buffer, value.known);
            ByteBufCodecs.BOOL.encode(buffer, value.reveal);
        }
    };

    public TrueNamePayload {
        known = List.copyOf(known.subList(0, Math.min(MAX_KNOWN, known.size())));
    }

    public boolean named() {
        return !name.isEmpty();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
