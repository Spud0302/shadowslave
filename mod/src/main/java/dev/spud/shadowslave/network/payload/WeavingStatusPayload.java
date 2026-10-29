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
 * level and next step, the Smithing patterns they know, the hints failed
 * attempts have left, the strands they have studied, what the piece on the
 * Weaving Table can teach, and what the last action did. Text is worded on
 * the server; item ids are named on the client.
 *
 * @param tierCap the highest strand Tier the soul and the Patterns branch allow, before the anchor's own cap
 */
public record WeavingStatusPayload(int points, int xpTowardPoint, int xpPerPoint, boolean canRespec,
                                   List<Branch> branches, List<Pattern> patterns, List<Hint> hints,
                                   String lastResult, List<Strand> strands, List<Lesson> table,
                                   int tierCap, int patternsLevel, int forgeryLevel) implements CustomPacketPayload {
    public static final WeavingStatusPayload NONE = new WeavingStatusPayload(0, 0, 100, false, List.of(), List.of(), List.of(), "",
            List.of(), List.of(), 1, 1, 1);

    /**
     * A strand the player has studied. {@code points} holds the anchor
     * pattern as x, y pairs; {@code essence} and {@code counts} hold one
     * value per Tier.
     */
    public record Strand(String id, String name, String description, String fits, boolean tunable, String material,
                         List<Integer> counts, List<Integer> essence, List<Float> points, int laid, String lock) {
        static final Codec<Strand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Strand::id),
                Codec.STRING.fieldOf("name").forGetter(Strand::name),
                Codec.STRING.fieldOf("description").forGetter(Strand::description),
                Codec.STRING.fieldOf("fits").forGetter(Strand::fits),
                Codec.BOOL.fieldOf("tunable").forGetter(Strand::tunable),
                Codec.STRING.fieldOf("material").forGetter(Strand::material),
                Codec.INT.listOf().fieldOf("counts").forGetter(Strand::counts),
                Codec.INT.listOf().fieldOf("essence").forGetter(Strand::essence),
                Codec.FLOAT.listOf().fieldOf("points").forGetter(Strand::points),
                Codec.INT.fieldOf("laid").forGetter(Strand::laid),
                Codec.STRING.fieldOf("lock").forGetter(Strand::lock)
        ).apply(instance, Strand::new));

        public int maxTier() {
            return essence.size();
        }
    }

    /** A strand the piece in the table's centre holds or teaches; {@code known} once studied. */
    public record Lesson(String id, String name, boolean known) {
        static final Codec<Lesson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Lesson::id),
                Codec.STRING.fieldOf("name").forGetter(Lesson::name),
                Codec.BOOL.fieldOf("known").forGetter(Lesson::known)
        ).apply(instance, Lesson::new));
    }

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
            Codec.STRING.fieldOf("last").forGetter(WeavingStatusPayload::lastResult),
            Strand.CODEC.listOf().fieldOf("strands").forGetter(WeavingStatusPayload::strands),
            Lesson.CODEC.listOf().fieldOf("table").forGetter(WeavingStatusPayload::table),
            Codec.INT.fieldOf("tier_cap").forGetter(WeavingStatusPayload::tierCap),
            Codec.INT.fieldOf("patterns_level").forGetter(WeavingStatusPayload::patternsLevel),
            Codec.INT.fieldOf("forgery_level").forGetter(WeavingStatusPayload::forgeryLevel)
    ).apply(instance, WeavingStatusPayload::new));

    public static final Type<WeavingStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "weaving_status")
    );
    public static final StreamCodec<ByteBuf, WeavingStatusPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public WeavingStatusPayload {
        branches = List.copyOf(branches);
        patterns = List.copyOf(patterns);
        hints = List.copyOf(hints);
        strands = List.copyOf(strands);
        table = List.copyOf(table);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
