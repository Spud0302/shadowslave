package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * What the Field Guide can learn about one kind of creature, read from
 * {@code data/<ns>/field_guide/creature/*.json}. A page opens with the first
 * spyglass sighting (or kill) and each section reveals itself once the
 * player has seen the creature {@code sightings} times or killed it
 * {@code kills} times, whichever comes first. A section with neither never
 * opens on its own (a hook for later sources such as Field Notes).
 */
public record CreaturePage(ResourceLocation entity, String name, String summary, List<Section> sections) {
    public record Section(String title, String text, Optional<Integer> sightings, Optional<Integer> kills) {
        public static final Codec<Section> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("title").forGetter(Section::title),
                Codec.STRING.fieldOf("text").forGetter(Section::text),
                Codec.intRange(0, 10_000).optionalFieldOf("sightings").forGetter(Section::sightings),
                Codec.intRange(0, 10_000).optionalFieldOf("kills").forGetter(Section::kills)
        ).apply(instance, Section::new));

        public boolean unlocked(int seen, int killed) {
            return sightings.filter(needed -> seen >= needed).isPresent() || kills.filter(needed -> killed >= needed).isPresent();
        }

        /** "Sight it 2 more times or slay 1", or empty once open or when neither path opens it. */
        public String hint(int seen, int killed) {
            if (unlocked(seen, killed)) {
                return "";
            }
            String look = sightings.map(needed -> "sight it " + times(needed - seen)).orElse("");
            String kill = kills.map(needed -> "slay " + (needed - killed) + " more").orElse("");
            if (look.isEmpty()) {
                return capitalise(kill);
            }
            return capitalise(kill.isEmpty() ? look : look + " or " + kill);
        }

        private static String times(int count) {
            return count == 1 ? "once more" : count + " more times";
        }

        private static String capitalise(String text) {
            return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
    }

    public static final Codec<CreaturePage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("entity").forGetter(CreaturePage::entity),
            Codec.STRING.fieldOf("name").forGetter(CreaturePage::name),
            Codec.STRING.optionalFieldOf("summary", "").forGetter(CreaturePage::summary),
            Section.CODEC.listOf().fieldOf("sections").forGetter(CreaturePage::sections)
    ).apply(instance, CreaturePage::new));

    public CreaturePage {
        sections = List.copyOf(sections);
    }

    public List<Section> revealed(int seen, int killed) {
        return sections.stream().filter(section -> section.unlocked(seen, killed)).toList();
    }

    /** How to open the next closed section, or empty when the page is complete. */
    public String nextHint(int seen, int killed) {
        return sections.stream().map(section -> section.hint(seen, killed)).filter(hint -> !hint.isEmpty()).findFirst().orElse("");
    }
}
