package dev.spud.shadowslave.fieldguide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Tunables for the Field Guide, read from {@code data/<ns>/field_guide/rules.json}
 * (DESIGN, Jack 2026-10-23: every number is "for now"). Missing keys keep
 * their defaults.
 *
 * @param giveOnFirstJoin   hand every player a guide the first time they join
 * @param exploreRadius     chunks around the player the map uncovers
 * @param maxChunks         explored chunks remembered per dimension; past this the oldest fall back into fog
 * @param sightingRange     how far a spyglass can make out a creature
 * @param sightingHoldTicks how long a creature must stay in the spyglass to count
 * @param sightingCooldown  ticks before the same kind of creature counts as a new sighting
 * @param seerBonus         extra sightings a Seer gets from each look
 * @param placeRange        how close you must come to a Gate to log it
 * @param seedRange         how close you must come to a Seed to log it
 * @param dangerCell        width in blocks of one danger-rated area
 * @param deathStep         ranks a creature-caused death adds to an area's rating
 * @param paths             path tracing and established paths
 * @param notes             copying pages onto Field Notes, the public board and lost notes
 */
public record FieldGuideSettings(boolean giveOnFirstJoin, int exploreRadius, int maxChunks, int sightingRange,
                                 int sightingHoldTicks, int sightingCooldown, int seerBonus, int placeRange,
                                 int seedRange, int dangerCell, int deathStep, PathSettings paths, NoteSettings notes) {
    public static final FieldGuideSettings DEFAULT = new FieldGuideSettings(true, 2, 16384, 64, 16, 600, 1, 48, 24, 64, 1,
            PathSettings.DEFAULT, NoteSettings.DEFAULT);

    public static final Codec<FieldGuideSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("give_on_first_join", DEFAULT.giveOnFirstJoin).forGetter(FieldGuideSettings::giveOnFirstJoin),
            Codec.intRange(0, 8).optionalFieldOf("explore_radius", DEFAULT.exploreRadius).forGetter(FieldGuideSettings::exploreRadius),
            Codec.intRange(64, 1 << 20).optionalFieldOf("max_chunks", DEFAULT.maxChunks).forGetter(FieldGuideSettings::maxChunks),
            Codec.intRange(4, 256).optionalFieldOf("sighting_range", DEFAULT.sightingRange).forGetter(FieldGuideSettings::sightingRange),
            Codec.intRange(0, 200).optionalFieldOf("sighting_hold_ticks", DEFAULT.sightingHoldTicks).forGetter(FieldGuideSettings::sightingHoldTicks),
            Codec.intRange(0, 72000).optionalFieldOf("sighting_cooldown_ticks", DEFAULT.sightingCooldown).forGetter(FieldGuideSettings::sightingCooldown),
            Codec.intRange(0, 10).optionalFieldOf("seer_bonus", DEFAULT.seerBonus).forGetter(FieldGuideSettings::seerBonus),
            Codec.intRange(0, 512).optionalFieldOf("place_range", DEFAULT.placeRange).forGetter(FieldGuideSettings::placeRange),
            Codec.intRange(0, 512).optionalFieldOf("seed_range", DEFAULT.seedRange).forGetter(FieldGuideSettings::seedRange),
            Codec.intRange(16, 1024).optionalFieldOf("danger_cell", DEFAULT.dangerCell).forGetter(FieldGuideSettings::dangerCell),
            Codec.intRange(0, 6).optionalFieldOf("death_step", DEFAULT.deathStep).forGetter(FieldGuideSettings::deathStep),
            PathSettings.CODEC.optionalFieldOf("paths", PathSettings.DEFAULT).forGetter(FieldGuideSettings::paths),
            NoteSettings.CODEC.optionalFieldOf("notes", NoteSettings.DEFAULT).forGetter(FieldGuideSettings::notes)
    ).apply(instance, FieldGuideSettings::new));

    /**
     * Path tracing (DESIGN, the Shared Field Guide plan): with Trace on, the
     * guide marks each path cell the player walks through. A cell walked
     * {@code establishedWalks} times becomes an established path; one player
     * counts for at most {@code walksPerPlayer} of those walks, so a server
     * can ask for many travellers while a lone player can still wear a road.
     *
     * @param cell             width in blocks of one path cell
     * @param maxCells         path cells a guide remembers per dimension; past this the oldest fade
     * @param walkAgainTicks   ticks before walking the same cell again counts as another walk
     * @param establishedWalks walks that make a cell an established path
     * @param walksPerPlayer   most walks one player adds to a cell's count
     * @param needsGuide       trace only while a Field Guide is carried
     */
    public record PathSettings(int cell, int maxCells, int walkAgainTicks, int establishedWalks, int walksPerPlayer,
                               boolean needsGuide) {
        public static final PathSettings DEFAULT = new PathSettings(4, 8192, 1200, 8, 8, true);

        public static final Codec<PathSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 64).optionalFieldOf("cell", DEFAULT.cell).forGetter(PathSettings::cell),
                Codec.intRange(64, 1 << 18).optionalFieldOf("max_cells", DEFAULT.maxCells).forGetter(PathSettings::maxCells),
                Codec.intRange(0, 72000).optionalFieldOf("walk_again_ticks", DEFAULT.walkAgainTicks).forGetter(PathSettings::walkAgainTicks),
                Codec.intRange(1, 1000).optionalFieldOf("established_walks", DEFAULT.establishedWalks).forGetter(PathSettings::establishedWalks),
                Codec.intRange(1, 1000).optionalFieldOf("walks_per_player", DEFAULT.walksPerPlayer).forGetter(PathSettings::walksPerPlayer),
                Codec.BOOL.optionalFieldOf("needs_guide", DEFAULT.needsGuide).forGetter(PathSettings::needsGuide)
        ).apply(instance, PathSettings::new));
    }

    /** One item a copy costs, by id so the rules load before the item registry does. */
    public record Cost(ResourceLocation id, int count) {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Cost::id),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Cost::count)
        ).apply(instance, Cost::new));

        public Item item() {
            return BuiltInRegistries.ITEM.get(id);
        }

        public String describe() {
            return count + " " + new ItemStack(item()).getHoverName().getString();
        }
    }

    /**
     * Field Notes (DESIGN, the Shared Field Guide plan): copying a page onto a
     * note costs paper and ink, and a chart copies the map within
     * {@code chartRadius} chunks of where it was taken.
     *
     * @param cost          what one copy costs (creative players copy for free)
     * @param chartRadius   chunks around the chosen spot a chart, path or danger note covers
     * @param boardCapacity notes one public board holds
     */
    public record NoteSettings(List<Cost> cost, int chartRadius, int boardCapacity, LostNotes lost) {
        public static final NoteSettings DEFAULT = new NoteSettings(
                List.of(new Cost(ResourceLocation.withDefaultNamespace("paper"), 1),
                        new Cost(ResourceLocation.withDefaultNamespace("ink_sac"), 1)), 6, 12, LostNotes.DEFAULT);

        public static final Codec<NoteSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Cost.CODEC.listOf().optionalFieldOf("cost", DEFAULT.cost).forGetter(NoteSettings::cost),
                Codec.intRange(1, 16).optionalFieldOf("chart_radius", DEFAULT.chartRadius).forGetter(NoteSettings::chartRadius),
                Codec.intRange(1, 54).optionalFieldOf("board_capacity", DEFAULT.boardCapacity).forGetter(NoteSettings::boardCapacity),
                LostNotes.CODEC.optionalFieldOf("lost", DEFAULT.lost).forGetter(NoteSettings::lost)
        ).apply(instance, NoteSettings::new));

        public String describeCost() {
            return cost.isEmpty() ? "nothing" : String.join(", ", cost.stream().map(Cost::describe).toList());
        }
    }

    /**
     * Notes left by Sleepers who did not make it, found in chests (which
     * chests is the {@code shadowslave:lost_field_notes} loot modifier). A
     * lost note is a part-filled creature page or a chart of the ground
     * around where it was found, with a warning pinned and the death marked.
     *
     * @param creatureWeight chance weight of a creature page
     * @param chartWeight    chance weight of a chart
     * @param maxSightings   most sightings a lost creature page records
     * @param maxKills       most kills a lost creature page records
     * @param chartRadius    chunks around the chest a lost chart covers, as far as is loaded
     * @param author         who the note says copied it
     * @param warnings       labels for the warning pinned on a lost chart
     * @param endings        a last line, one picked at random
     */
    public record LostNotes(int creatureWeight, int chartWeight, int maxSightings, int maxKills, int chartRadius,
                            String author, List<String> warnings, List<String> endings) {
        public static final LostNotes DEFAULT = new LostNotes(3, 2, 5, 2, 3, "a Sleeper who did not make it",
                List.of("Do not stay the night", "Something under the ash", "They came from here", "Turn back",
                        "Not alone here", "Do not follow the voices"),
                List.of("The last line trails off.", "The ink is smeared, as if written running.", "A corner is torn away.",
                        "The hand gets shakier near the end.", "Someone has underlined the warning twice."));

        public static final Codec<LostNotes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 100).optionalFieldOf("creature_weight", DEFAULT.creatureWeight).forGetter(LostNotes::creatureWeight),
                Codec.intRange(0, 100).optionalFieldOf("chart_weight", DEFAULT.chartWeight).forGetter(LostNotes::chartWeight),
                Codec.intRange(1, 100).optionalFieldOf("max_sightings", DEFAULT.maxSightings).forGetter(LostNotes::maxSightings),
                Codec.intRange(0, 100).optionalFieldOf("max_kills", DEFAULT.maxKills).forGetter(LostNotes::maxKills),
                Codec.intRange(0, 8).optionalFieldOf("chart_radius", DEFAULT.chartRadius).forGetter(LostNotes::chartRadius),
                Codec.STRING.optionalFieldOf("author", DEFAULT.author).forGetter(LostNotes::author),
                Codec.STRING.listOf().optionalFieldOf("warnings", DEFAULT.warnings).forGetter(LostNotes::warnings),
                Codec.STRING.listOf().optionalFieldOf("endings", DEFAULT.endings).forGetter(LostNotes::endings)
        ).apply(instance, LostNotes::new));
    }
}
