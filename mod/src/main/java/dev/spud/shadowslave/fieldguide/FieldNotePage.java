package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.UUID;

/**
 * A page of the Field Guide copied onto a Field Note (DESIGN, the Shared Field
 * Guide plan): what kind of page it is, what its face says, who copied it,
 * and the slice of a guide it carries. Reading the note teaches that slice to
 * the reader's own guide. Notes compare by {@code id}, since the slice inside
 * is a plain guide and never changes once written.
 */
public record FieldNotePage(Kind kind, String title, String author, List<String> lines, FieldGuideData knowledge, UUID id) {
    public enum Kind implements StringRepresentable {
        CREATURE("creature", "Creature page"),
        PLACE("place", "Marked place"),
        CHART("chart", "Chart"),
        PATH("path", "Paths"),
        DANGER("danger", "Danger marks"),
        LOST("lost", "Lost note");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;
        private final String label;

        Kind(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Kind byName(String name) {
            for (Kind kind : values()) {
                if (kind.name.equals(name)) {
                    return kind;
                }
            }
            return null;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public FieldNotePage {
        lines = List.copyOf(lines);
    }

    public static final Codec<FieldNotePage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kind.CODEC.fieldOf("kind").forGetter(FieldNotePage::kind),
            Codec.STRING.fieldOf("title").forGetter(FieldNotePage::title),
            Codec.STRING.optionalFieldOf("author", "").forGetter(FieldNotePage::author),
            Codec.STRING.listOf().optionalFieldOf("lines", List.of()).forGetter(FieldNotePage::lines),
            FieldGuideData.CODEC.fieldOf("knowledge").forGetter(FieldNotePage::knowledge),
            UUIDUtil.CODEC.fieldOf("id").forGetter(FieldNotePage::id)
    ).apply(instance, FieldNotePage::new));

    @Override
    public boolean equals(Object other) {
        return other instanceof FieldNotePage page && page.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
