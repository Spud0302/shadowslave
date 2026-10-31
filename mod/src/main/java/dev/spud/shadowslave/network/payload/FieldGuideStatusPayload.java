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
 * Server-to-client reading of the player's Field Guide, apart from the map
 * itself (see {@link FieldGuideMapPayload}): creature pages with only their
 * revealed sections, pins and places, danger ratings and the Dream Realm's
 * rank regions, whether Trace is on, and what copying onto a Field Note
 * costs and covers. Text is worded on the server. {@code open} asks the
 * client to open the guide.
 */
public record FieldGuideStatusPayload(List<Creature> creatures, List<Pin> pins, List<Danger> danger, List<Region> regions,
                                      String realm, int ratio, int dangerCell, boolean open, boolean tracing, int chartRadius,
                                      String copyCost) implements CustomPacketPayload {
    public static final FieldGuideStatusPayload NONE = new FieldGuideStatusPayload(List.of(), List.of(), List.of(), List.of(),
            "", 8, 64, false, false, 6, "");

    public record Section(String title, String text) {
        static final Codec<Section> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("title").forGetter(Section::title),
                Codec.STRING.fieldOf("text").forGetter(Section::text)
        ).apply(instance, Section::new));
    }

    /** One creature page. {@code locked} sections are still hidden; {@code hint} says how to open the next. */
    public record Creature(String id, String name, String summary, int sightings, int kills, List<String> ranks,
                           List<Section> sections, int locked, String hint, String lastDimension, int lastX, int lastZ) {
        static final Codec<Creature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Creature::id),
                Codec.STRING.fieldOf("name").forGetter(Creature::name),
                Codec.STRING.fieldOf("summary").forGetter(Creature::summary),
                Codec.INT.fieldOf("sightings").forGetter(Creature::sightings),
                Codec.INT.fieldOf("kills").forGetter(Creature::kills),
                Codec.STRING.listOf().fieldOf("ranks").forGetter(Creature::ranks),
                Section.CODEC.listOf().fieldOf("sections").forGetter(Creature::sections),
                Codec.INT.fieldOf("locked").forGetter(Creature::locked),
                Codec.STRING.fieldOf("hint").forGetter(Creature::hint),
                Codec.STRING.fieldOf("last_dim").forGetter(Creature::lastDimension),
                Codec.INT.fieldOf("last_x").forGetter(Creature::lastX),
                Codec.INT.fieldOf("last_z").forGetter(Creature::lastZ)
        ).apply(instance, Creature::new));
    }

    /**
     * A pin on a map. {@code kind} is place, warning, gate, seed or anchor;
     * {@code detail} is an extra line (a Seed's Overworld Gate spot);
     * {@code active} is false for a Gate that has closed.
     */
    public record Pin(String kind, String dimension, int x, int z, String label, String detail, boolean active,
                      int gateX, int gateZ) {
        static final Codec<Pin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("kind").forGetter(Pin::kind),
                Codec.STRING.fieldOf("dim").forGetter(Pin::dimension),
                Codec.INT.fieldOf("x").forGetter(Pin::x),
                Codec.INT.fieldOf("z").forGetter(Pin::z),
                Codec.STRING.fieldOf("label").forGetter(Pin::label),
                Codec.STRING.fieldOf("detail").forGetter(Pin::detail),
                Codec.BOOL.fieldOf("active").forGetter(Pin::active),
                Codec.INT.fieldOf("gate_x").forGetter(Pin::gateX),
                Codec.INT.fieldOf("gate_z").forGetter(Pin::gateZ)
        ).apply(instance, Pin::new));

        public boolean named() {
            return kind.equals("place") || kind.equals("warning");
        }
    }

    /** A rated danger cell, already including its baseline and deaths; {@code rating} is a creature rank ordinal. */
    public record Danger(String dimension, int cellX, int cellZ, int rating) {
        static final Codec<Danger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("dim").forGetter(Danger::dimension),
                Codec.INT.fieldOf("x").forGetter(Danger::cellX),
                Codec.INT.fieldOf("z").forGetter(Danger::cellZ),
                Codec.INT.fieldOf("rating").forGetter(Danger::rating)
        ).apply(instance, Danger::new));
    }

    /** A Dream Realm rank region: a ring from {@code min} to {@code max} blocks out, rated at {@code rank}. */
    public record Region(String name, int min, int max, int rank) {
        static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Region::name),
                Codec.INT.fieldOf("min").forGetter(Region::min),
                Codec.INT.fieldOf("max").forGetter(Region::max),
                Codec.INT.fieldOf("rank").forGetter(Region::rank)
        ).apply(instance, Region::new));
    }

    private static final Codec<FieldGuideStatusPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Creature.CODEC.listOf().fieldOf("creatures").forGetter(FieldGuideStatusPayload::creatures),
            Pin.CODEC.listOf().fieldOf("pins").forGetter(FieldGuideStatusPayload::pins),
            Danger.CODEC.listOf().fieldOf("danger").forGetter(FieldGuideStatusPayload::danger),
            Region.CODEC.listOf().fieldOf("regions").forGetter(FieldGuideStatusPayload::regions),
            Codec.STRING.fieldOf("realm").forGetter(FieldGuideStatusPayload::realm),
            Codec.INT.fieldOf("ratio").forGetter(FieldGuideStatusPayload::ratio),
            Codec.INT.fieldOf("cell").forGetter(FieldGuideStatusPayload::dangerCell),
            Codec.BOOL.fieldOf("open").forGetter(FieldGuideStatusPayload::open),
            Codec.BOOL.fieldOf("tracing").forGetter(FieldGuideStatusPayload::tracing),
            Codec.INT.fieldOf("chart_radius").forGetter(FieldGuideStatusPayload::chartRadius),
            Codec.STRING.fieldOf("copy_cost").forGetter(FieldGuideStatusPayload::copyCost)
    ).apply(instance, FieldGuideStatusPayload::new));

    public static final Type<FieldGuideStatusPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide_status")
    );
    public static final StreamCodec<ByteBuf, FieldGuideStatusPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public FieldGuideStatusPayload {
        creatures = List.copyOf(creatures);
        pins = List.copyOf(pins);
        danger = List.copyOf(danger);
        regions = List.copyOf(regions);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
