package dev.spud.shadowslave.fieldguide;

import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.network.payload.FieldGuideCopyRequestPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Field Notes (DESIGN, the Shared Field Guide plan): any page of a guide can
 * be copied onto a note with paper and ink, and reading a note adds what it
 * carries to the reader's own guide. A creature page carries its counts, so
 * a fuller page teaches more; a chart, path or danger note carries the area
 * within {@code chart_radius} chunks of where it was taken.
 */
public final class FieldNotes {
    private FieldNotes() {
    }

    private static FieldGuideSettings settings() {
        return FieldGuideContent.get().settings();
    }

    // Copying

    /** Copies a page of the player's guide onto a new note, if they have the page and can pay for it. */
    public static Optional<ItemStack> copy(ServerPlayer player, FieldGuideCopyRequestPayload request) {
        FieldNotePage.Kind kind = FieldNotePage.Kind.byName(request.kind());
        if (kind == null || kind == FieldNotePage.Kind.LOST) {
            return Optional.empty();
        }
        Optional<FieldNotePage> page = page(player, kind, request);
        if (page.isEmpty()) {
            player.displayClientMessage(Component.literal(nothingToCopy(kind)).withStyle(ChatFormatting.GRAY), true);
            return Optional.empty();
        }
        if (!pay(player)) {
            player.displayClientMessage(Component.literal("Copying a page needs " + settings().notes().describeCost() + ".")
                    .withStyle(ChatFormatting.GRAY), true);
            return Optional.empty();
        }
        ItemStack note = note(page.get());
        if (!player.getInventory().add(note.copy())) {
            player.drop(note.copy(), false);
        }
        player.displayClientMessage(Component.literal("Copied onto a Field Note: " + page.get().title()).withStyle(ChatFormatting.GOLD), true);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.2F);
        return Optional.of(note);
    }

    public static ItemStack note(FieldNotePage page) {
        ItemStack note = new ItemStack(FieldGuideRegistry.FIELD_NOTE_ITEM.get());
        note.set(FieldGuideRegistry.NOTE_PAGE.get(), page);
        return note;
    }

    private static String nothingToCopy(FieldNotePage.Kind kind) {
        return switch (kind) {
            case CREATURE -> "Your guide has no page for that creature.";
            case PLACE -> "There is no marked place there to copy.";
            case CHART -> "Your guide has not mapped anything there.";
            case PATH -> "Your guide has no traced paths there.";
            case DANGER -> "Your guide knows of no danger there.";
            case LOST -> "";
        };
    }

    /** Takes the copy's cost from the player's inventory, unless they are in creative. Returns false, taking nothing, if they cannot pay. */
    static boolean pay(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        List<FieldGuideSettings.Cost> cost = settings().notes().cost();
        for (FieldGuideSettings.Cost each : cost) {
            if (player.getInventory().countItem(each.item()) < each.count()) {
                return false;
            }
        }
        for (FieldGuideSettings.Cost each : cost) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(each.item()), each.count(), player.inventoryMenu.getCraftSlots());
        }
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    static Optional<FieldNotePage> page(ServerPlayer player, FieldNotePage.Kind kind, FieldGuideCopyRequestPayload request) {
        FieldGuideData mine = FieldGuideService.get(player);
        FieldGuideData slice = FieldGuideData.empty();
        String author = player.getGameProfile().getName();
        String dimension = request.dimension();
        String where = request.x() + ", " + request.z();
        int radius = settings().notes().chartRadius();
        List<String> lines = new ArrayList<>();
        String title;
        switch (kind) {
            case CREATURE -> {
                ResourceLocation type = ResourceLocation.tryParse(request.target());
                Optional<FieldGuideData.Creature> creature = type == null ? Optional.empty() : mine.creature(type);
                if (creature.isEmpty() || creature.get().sightings() + creature.get().kills() == 0) {
                    return Optional.empty();
                }
                slice.copyCreature(type, creature.get());
                title = FieldGuideService.creatureName(type);
                lines.add(completeness(type, creature.get()));
                lines.add("Seen " + creature.get().sightings() + ", slain " + creature.get().kills());
            }
            case PLACE -> {
                Optional<FieldGuideData.Pin> pin = mine.pins().stream()
                        .filter(each -> each.kind() != FieldGuideData.PinKind.ANCHOR && each.dimension().equals(dimension)
                                && Math.abs(each.x() - request.x()) <= 2 && Math.abs(each.z() - request.z()) <= 2)
                        .findFirst();
                if (pin.isEmpty()) {
                    return Optional.empty();
                }
                slice.addPin(pin.get());
                title = pin.get().label().isEmpty() ? "A marked place" : pin.get().label();
                lines.add("At " + pin.get().x() + ", " + pin.get().z() + " in the " + dimensionName(dimension));
            }
            case CHART -> {
                int chunks = copyChart(mine, slice, dimension, request.x(), request.z(), radius);
                if (chunks == 0) {
                    return Optional.empty();
                }
                title = "Chart near " + where;
                lines.add(chunks + " stretches of map in the " + dimensionName(dimension));
            }
            case PATH -> {
                int cells = copyPaths(mine, slice, dimension, request.x(), request.z(), radius);
                if (cells == 0) {
                    return Optional.empty();
                }
                title = "Paths near " + where;
                lines.add(cells + " stretches of path in the " + dimensionName(dimension));
            }
            case DANGER -> {
                int worst = copyDanger(mine, slice, dimension, request.x(), request.z(), radius);
                if (worst == Integer.MIN_VALUE) {
                    return Optional.empty();
                }
                title = "Danger near " + where;
                lines.add(worst >= 0 ? "Worst marked: " + CreatureRank.byOrdinal(worst).displayName() : "Warnings only");
            }
            default -> {
                return Optional.empty();
            }
        }
        return Optional.of(new FieldNotePage(kind, title, author, lines, slice, UUID.randomUUID()));
    }

    /** How much of a creature's page a record opens, e.g. "3 of 4 entries". */
    static String completeness(ResourceLocation type, FieldGuideData.Creature creature) {
        return FieldGuideContent.get().page(type)
                .map(page -> page.revealed(creature.sightings(), creature.kills()).size() + " of " + page.sections().size() + " entries")
                .orElse("A page with no entries");
    }

    private static boolean inArea(int blockX, int blockZ, int x, int z, int radius) {
        return within(blockX, blockZ, x, z, radius * 16 + 8);
    }

    private static boolean within(int blockX, int blockZ, int x, int z, int reach) {
        return Math.abs(blockX - x) <= reach && Math.abs(blockZ - z) <= reach;
    }

    static int copyChart(FieldGuideData from, FieldGuideData into, String dimension, int x, int z, int radius) {
        ChunkPos centre = new ChunkPos(x >> 4, z >> 4);
        int copied = 0;
        for (Map.Entry<Long, byte[]> chunk : from.exploredChunks(dimension).entrySet()) {
            int cx = ChunkPos.getX(chunk.getKey());
            int cz = ChunkPos.getZ(chunk.getKey());
            if (Math.abs(cx - centre.x) <= radius && Math.abs(cz - centre.z) <= radius) {
                into.copyChunk(dimension, chunk.getKey(), chunk.getValue());
                copied++;
            }
        }
        if (copied > 0) {
            // A chart shows the Gates, Seeds and Citadels drawn on it.
            for (FieldGuideData.Pin pin : from.pins()) {
                if (pin.kind().shared() && pin.kind() != FieldGuideData.PinKind.WARNING && pin.dimension().equals(dimension)
                        && inArea(pin.x(), pin.z(), x, z, radius)) {
                    into.addPin(pin);
                }
            }
        }
        return copied;
    }

    static int copyPaths(FieldGuideData from, FieldGuideData into, String dimension, int x, int z, int radius) {
        int cell = settings().paths().cell();
        int copied = 0;
        for (Long key : from.pathCells(dimension).keySet()) {
            if (inArea(DangerRating.keyX(key) * cell, DangerRating.keyZ(key) * cell, x, z, radius)
                    && into.learnPath(dimension, key, Integer.MAX_VALUE)) {
                copied++;
            }
        }
        return copied;
    }

    /** Copies the danger marks and warnings of an area. Returns the worst rank marked, -1 for warnings only, or MIN_VALUE for nothing. */
    static int copyDanger(FieldGuideData from, FieldGuideData into, String dimension, int x, int z, int radius) {
        int cell = settings().dangerCell();
        int worst = Integer.MIN_VALUE;
        for (Map.Entry<Long, Integer> marks : from.dangerMarks(dimension).entrySet()) {
            int middleX = DangerRating.keyX(marks.getKey()) * cell + cell / 2;
            int middleZ = DangerRating.keyZ(marks.getKey()) * cell + cell / 2;
            if (within(middleX, middleZ, x, z, radius * 16 + cell / 2)) {
                into.copyDanger(dimension, marks.getKey(), marks.getValue());
                int rating = DangerRating.rate(DangerRating.NONE, FieldGuideData.strongest(marks.getValue()),
                        FieldGuideData.deaths(marks.getValue()), settings().deathStep());
                worst = Math.max(worst, rating);
            }
        }
        for (FieldGuideData.Pin pin : from.pins()) {
            if (pin.kind() == FieldGuideData.PinKind.WARNING && pin.dimension().equals(dimension) && inArea(pin.x(), pin.z(), x, z, radius)) {
                into.addPin(pin);
                worst = Math.max(worst, -1);
            }
        }
        return worst;
    }

    static String dimensionName(String dimension) {
        if (dimension.equals(Level.OVERWORLD.location().toString())) {
            return "waking world";
        }
        if (dimension.equals(dev.spud.shadowslave.dreamrealm.DreamRealm.LEVEL.location().toString())) {
            return "Dream Realm";
        }
        if (dimension.equals(Level.NETHER.location().toString())) {
            return "Nether";
        }
        return dimension;
    }

    // Reading

    /** Reads a note into the player's guide. Returns what they learned. */
    public static FieldGuideData.Learned read(ServerPlayer player, ItemStack stack, FieldNotePage page) {
        FieldGuideData.Learned learned = learn(player, page.knowledge());
        if (!learned.any()) {
            player.displayClientMessage(Component.literal("Your Field Guide already knows everything on this note.")
                    .withStyle(ChatFormatting.GRAY), true);
            return learned;
        }
        player.displayClientMessage(Component.literal("Field Guide: from the note you learn " + String.join(", ", learned.describe()) + ".")
                .withStyle(ChatFormatting.GOLD), true);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (FieldGuideRules.notesWearOut(player.server) && !player.getAbilities().instabuild) {
            stack.shrink(1);
            player.sendSystemMessage(Component.literal("The note crumbles as you finish reading it.").withStyle(ChatFormatting.DARK_GRAY));
        }
        return learned;
    }

    /** Adds a slice of a guide to the player's guide and sends them what changed. */
    public static FieldGuideData.Learned learn(ServerPlayer player, FieldGuideData knowledge) {
        return learn(player, List.of(knowledge));
    }

    /** Adds several slices of guides to the player's guide and sends them what changed, once. */
    public static FieldGuideData.Learned learn(ServerPlayer player, List<FieldGuideData> knowledge) {
        // Gathered into one slice first, so two notes about the same creature count as one page learned.
        FieldGuideData gathered = FieldGuideData.empty();
        for (FieldGuideData each : knowledge) {
            gathered.learnFrom(each, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
        }
        FieldGuideData.Learned learned = FieldGuideService.get(player)
                .learnFrom(gathered, FieldGuideService.maxChunks(), FieldGuideService.maxPathCells(), true);
        if (learned.chunks() > 0) {
            FieldGuideService.sendWholeMap(player);
        }
        if (learned.paths() > 0) {
            FieldGuideService.sendWholePaths(player);
        }
        if (learned.any()) {
            FieldGuideService.sendStatus(player, false);
        }
        return learned;
    }

    // Lost notes

    /**
     * A note left by a Sleeper who did not make it (DESIGN, the Shared Field
     * Guide plan's single-player answer to trading): a part-filled creature
     * page, or a chart of the ground around where it was found with a
     * warning and the death that ended it marked.
     */
    public static FieldNotePage lost(ServerLevel level, Vec3 origin, RandomSource random) {
        FieldGuideSettings.LostNotes rules = settings().notes().lost();
        FieldGuideData slice = FieldGuideData.empty();
        String dimension = FieldGuideService.dimension(level);
        int x = (int) Math.floor(origin.x);
        int z = (int) Math.floor(origin.z);
        List<String> lines = new ArrayList<>();
        String title;
        List<ResourceLocation> creatures = new ArrayList<>(FieldGuideContent.get().pages().keySet());
        int roll = random.nextInt(Math.max(1, rules.creatureWeight() + rules.chartWeight()));
        if (!creatures.isEmpty() && roll < rules.creatureWeight()) {
            creatures.sort(java.util.Comparator.comparing(ResourceLocation::toString));
            ResourceLocation type = creatures.get(random.nextInt(creatures.size()));
            int sightings = 1 + random.nextInt(Math.max(1, rules.maxSightings()));
            int kills = random.nextInt(rules.maxKills() + 1);
            slice.sight(type, CreatureRank.DORMANT.ordinal(), dimension, x, z, 0, 0, sightings);
            for (int i = 0; i < kills; i++) {
                slice.kill(type, CreatureRank.DORMANT.ordinal(), dimension, x, z);
            }
            title = FieldGuideService.creatureName(type);
            lines.add(completeness(type, slice.creature(type).orElseThrow()));
        } else {
            ChunkPos centre = new ChunkPos(x >> 4, z >> 4);
            int radius = rules.chartRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (level.hasChunk(centre.x + dx, centre.z + dz)) {
                        LevelChunk chunk = level.getChunk(centre.x + dx, centre.z + dz);
                        slice.copyChunk(dimension, ChunkPos.asLong(centre.x + dx, centre.z + dz), MapSampler.sample(level, chunk));
                    }
                }
            }
            if (!rules.warnings().isEmpty()) {
                int reach = radius * 16;
                int wx = x + random.nextInt(reach * 2 + 1) - reach;
                int wz = z + random.nextInt(reach * 2 + 1) - reach;
                slice.addPin(new FieldGuideData.Pin(FieldGuideData.PinKind.WARNING, dimension, wx, wz,
                        rules.warnings().get(random.nextInt(rules.warnings().size())), Optional.empty()));
            }
            int cell = settings().dangerCell();
            slice.markDeath(dimension, DangerRating.key(DangerRating.cell(x, cell), DangerRating.cell(z, cell)));
            title = "A chart of this place";
            lines.add("Someone died near here");
        }
        if (!rules.endings().isEmpty()) {
            lines.add(rules.endings().get(random.nextInt(rules.endings().size())));
        }
        return new FieldNotePage(FieldNotePage.Kind.LOST, title, rules.author(), lines, slice, UUID.randomUUID());
    }
}
