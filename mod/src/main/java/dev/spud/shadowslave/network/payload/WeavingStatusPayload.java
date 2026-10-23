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
 * Server-to-client reading of the player's Weaving: points, each branch's
 * level and next step, the patterns they know, the hints failed attempts
 * have left, and what the last press of Weave did. Text is worded on the
 * server; item ids are named on the client.
 */
public record WeavingStatusPayload(int points, int xpTowardPoint, int xpPerPoint, boolean canRespec,
                                   List<Branch> branches, List<Pattern> patterns, List<Hint> hints,
                                   String lastResult) implements CustomPacketPayload {
    public static final WeavingStatusPayload NONE = new WeavingStatusPayload(0, 0, 100, false, List.of(), List.of(), List.of(), "");

    public record Branch(String name, int level, int maxLevel, int xp, String next, boolean canRaise) {
        static final Codec<Branch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Branch::name),
                Codec.INT.fieldOf("level").forGetter(Branch::level),
                Codec.INT.fieldOf("max").forGetter(Branch::maxLevel),
                Codec.INT.fieldOf("xp").forGetter(Branch::xp),
                Codec.STRING.fieldOf("next").forGetter(Branch::next),
                Codec.BOOL.fieldOf("can_raise").forGetter(Branch::canRaise)
        ).apply(instance, Branch::new));
    }

    public record Material(String item, int count) {
        static final Codec<Material> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(Material::item),
                Codec.INT.fieldOf("count").forGetter(Material::count)
        ).apply(instance, Material::new));
    }

    /** A known pattern. {@code lock} says why it can't be woven yet, or is empty. */
    public record Pattern(String name, String result, String branch, int tier, String rank, List<Material> materials,
                          int woven, String lock) {
        static final Codec<Pattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Pattern::name),
                Codec.STRING.fieldOf("result").forGetter(Pattern::result),
                Codec.STRING.fieldOf("branch").forGetter(Pattern::branch),
                Codec.INT.fieldOf("tier").forGetter(Pattern::tier),
                Codec.STRING.fieldOf("rank").forGetter(Pattern::rank),
                Material.CODEC.listOf().fieldOf("materials").forGetter(Pattern::materials),
                Codec.INT.fieldOf("woven").forGetter(Pattern::woven),
                Codec.STRING.fieldOf("lock").forGetter(Pattern::lock)
        ).apply(instance, Pattern::new));
    }

    /** An unfound pattern a failed attempt brushed against: the materials found so far out of {@code total}. */
    public record Hint(String branch, int tier, int total, List<String> revealed, String hint) {
        static final Codec<Hint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("branch").forGetter(Hint::branch),
                Codec.INT.fieldOf("tier").forGetter(Hint::tier),
                Codec.INT.fieldOf("total").forGetter(Hint::total),
                Codec.STRING.listOf().fieldOf("revealed").forGetter(Hint::revealed),
                Codec.STRING.fieldOf("hint").forGetter(Hint::hint)
        ).apply(instance, Hint::new));
    }

    private static final Codec<WeavingStatusPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("points").forGetter(WeavingStatusPayload::points),
            Codec.INT.fieldOf("xp_toward").forGetter(WeavingStatusPayload::xpTowardPoint),
            Codec.INT.fieldOf("xp_per_point").forGetter(WeavingStatusPayload::xpPerPoint),
            Codec.BOOL.fieldOf("can_respec").forGetter(WeavingStatusPayload::canRespec),
            Branch.CODEC.listOf().fieldOf("branches").forGetter(WeavingStatusPayload::branches),
            Pattern.CODEC.listOf().fieldOf("patterns").forGetter(WeavingStatusPayload::patterns),
            Hint.CODEC.listOf().fieldOf("hints").forGetter(WeavingStatusPayload::hints),
            Codec.STRING.fieldOf("last").forGetter(WeavingStatusPayload::lastResult)
    ).apply(instance, WeavingStatusPayload::new));

    public static final Type<WeavingStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "weaving_status")
    );
    public static final StreamCodec<ByteBuf, WeavingStatusPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public WeavingStatusPayload {
        branches = List.copyOf(branches);
        patterns = List.copyOf(patterns);
        hints = List.copyOf(hints);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
