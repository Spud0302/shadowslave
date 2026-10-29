package dev.spud.shadowslave.network.payload;

import dev.spud.shadowslave.ShadowSlaveMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Server-to-client Callings: either every Calling the player has heard (on
 * login, {@code fresh} false, replacing what the client holds), or one new
 * Calling to show over the Spell's thread-web ({@code fresh} true, appended).
 */
public record SpellCallingsPayload(List<Entry> entries, boolean fresh) implements CustomPacketPayload {
    public static final int MAX = 256;

    public record Entry(String calling, String title, String text, long day) {
        private static final StreamCodec<ByteBuf, Entry> CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(64), Entry::calling,
                ByteBufCodecs.stringUtf8(64), Entry::title,
                ByteBufCodecs.stringUtf8(512), Entry::text,
                ByteBufCodecs.VAR_LONG, Entry::day,
                Entry::new);
    }

    public static final Type<SpellCallingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "spell_callings"));

    public static final StreamCodec<ByteBuf, SpellCallingsPayload> STREAM_CODEC = StreamCodec.composite(
            Entry.CODEC.apply(ByteBufCodecs.list(MAX)), SpellCallingsPayload::entries,
            ByteBufCodecs.BOOL, SpellCallingsPayload::fresh,
            SpellCallingsPayload::new);

    public SpellCallingsPayload {
        entries = List.copyOf(entries.subList(Math.max(0, entries.size() - MAX), entries.size()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
