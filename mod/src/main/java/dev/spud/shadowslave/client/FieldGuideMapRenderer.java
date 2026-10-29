package dev.spud.shadowslave.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.fieldguide.FieldGuideData;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Draws the Field Guide map for the guide screen and the off-hand minimap.
 * Explored chunks are baked into 128-pixel tiles of 32 by 32 chunks (four
 * pixels per chunk side, one per sampled spot) in a faded parchment palette;
 * everything unexplored stays under fog. Layers (danger tint, pins, Gates,
 * Seeds, creatures) are drawn on top.
 */
public final class FieldGuideMapRenderer {
    public static final int TILE_CHUNKS = 32;
    private static final int TILE_PIXELS = TILE_CHUNKS * FieldGuideData.SAMPLES;
    private static final int TILE_BLOCKS = TILE_CHUNKS * 16;

    public static final int FOG = 0xFFC4B694;
    private static final int FOG_HATCH = 0xFFB8A985;
    public static final int INK = 0xFF3B2A1A;

    private static final class Tile {
        final DynamicTexture texture;
        final ResourceLocation id;
        boolean dirty = true;

        Tile(DynamicTexture texture, ResourceLocation id) {
            this.texture = texture;
            this.id = id;
        }
    }

    private static final Map<String, Map<Long, Tile>> TILES = new HashMap<>();
    private static int nextId;

    private FieldGuideMapRenderer() {
    }

    /** Where a map's blocks land on screen. */
    public record View(int left, int top, int width, int height, double centreX, double centreZ, float pixelsPerBlock) {
        int originX() {
            return left + width / 2 - (int) Math.floor(centreX * pixelsPerBlock);
        }

        int originZ() {
            return top + height / 2 - (int) Math.floor(centreZ * pixelsPerBlock);
        }

        public int screenX(double blockX) {
            return originX() + (int) Math.floor(blockX * pixelsPerBlock);
        }

        public int screenY(double blockZ) {
            return originZ() + (int) Math.floor(blockZ * pixelsPerBlock);
        }

        public double blockX(double screenX) {
            return (screenX - originX()) / pixelsPerBlock;
        }

        public double blockZ(double screenY) {
            return (screenY - originZ()) / pixelsPerBlock;
        }

        public boolean contains(double x, double y) {
            return x >= left && x < left + width && y >= top && y < top + height;
        }
    }

    public static long tileOf(int chunkX, int chunkZ) {
        return ChunkPos.asLong(Math.floorDiv(chunkX, TILE_CHUNKS), Math.floorDiv(chunkZ, TILE_CHUNKS));
    }

    static void markDirty(String dimension, long tile) {
        Map<Long, Tile> tiles = TILES.get(dimension);
        if (tiles != null && tiles.containsKey(tile)) {
            tiles.get(tile).dirty = true;
        }
    }

    static void forget(String dimension) {
        Map<Long, Tile> tiles = TILES.remove(dimension);
        if (tiles != null) {
            tiles.values().forEach(FieldGuideMapRenderer::release);
        }
    }

    static void forgetAll() {
        for (Iterator<Map<Long, Tile>> it = TILES.values().iterator(); it.hasNext(); ) {
            it.next().values().forEach(FieldGuideMapRenderer::release);
            it.remove();
        }
    }

    private static void release(Tile tile) {
        Minecraft.getInstance().getTextureManager().release(tile.id);
    }

    // Terrain

    /** Fog, then every explored tile in view. */
    public static void drawTerrain(GuiGraphics graphics, String dimension, View view) {
        graphics.fill(view.left(), view.top(), view.left() + view.width(), view.top() + view.height(), FOG);
        int hatch = Math.max(6, (int) (16 * Math.max(1.0F, view.pixelsPerBlock())));
        for (int x = view.left() - view.height(); x < view.left() + view.width(); x += hatch) {
            for (int i = 0; i < view.height(); i += 2) {
                int px = x + i;
                if (px >= view.left() && px < view.left() + view.width()) {
                    graphics.fill(px, view.top() + i, px + 1, view.top() + i + 1, FOG_HATCH);
                }
            }
        }
        int tilePixels = Math.round(TILE_BLOCKS * view.pixelsPerBlock());
        int minTileX = Math.floorDiv((int) Math.floor(view.blockX(view.left())), TILE_BLOCKS);
        int maxTileX = Math.floorDiv((int) Math.floor(view.blockX(view.left() + view.width())), TILE_BLOCKS);
        int minTileZ = Math.floorDiv((int) Math.floor(view.blockZ(view.top())), TILE_BLOCKS);
        int maxTileZ = Math.floorDiv((int) Math.floor(view.blockZ(view.top() + view.height())), TILE_BLOCKS);
        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int tz = minTileZ; tz <= maxTileZ; tz++) {
                long key = ChunkPos.asLong(tx, tz);
                if (!ClientFieldGuideState.hasTile(dimension, key)) {
                    continue;
                }
                Tile tile = tile(dimension, key, tx, tz);
                graphics.blit(tile.id, view.screenX(tx * TILE_BLOCKS), view.screenY(tz * TILE_BLOCKS), tilePixels, tilePixels,
                        0.0F, 0.0F, TILE_PIXELS, TILE_PIXELS, TILE_PIXELS, TILE_PIXELS);
            }
        }
    }

    private static Tile tile(String dimension, long key, int tileX, int tileZ) {
        Map<Long, Tile> tiles = TILES.computeIfAbsent(dimension, k -> new HashMap<>());
        Tile tile = tiles.get(key);
        if (tile == null) {
            DynamicTexture texture = new DynamicTexture(TILE_PIXELS, TILE_PIXELS, true);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "field_guide/tile_" + nextId++);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            tile = new Tile(texture, id);
            tiles.put(key, tile);
        }
        if (tile.dirty) {
            bake(tile.texture.getPixels(), dimension, tileX, tileZ);
            tile.texture.upload();
            tile.dirty = false;
        }
        return tile;
    }

    private static void bake(NativeImage image, String dimension, int tileX, int tileZ) {
        image.fillRect(0, 0, TILE_PIXELS, TILE_PIXELS, 0);
        Map<Long, byte[]> chunks = ClientFieldGuideState.chunks(dimension);
        int samples = FieldGuideData.SAMPLES;
        for (int cx = 0; cx < TILE_CHUNKS; cx++) {
            for (int cz = 0; cz < TILE_CHUNKS; cz++) {
                byte[] colours = chunks.get(ChunkPos.asLong(tileX * TILE_CHUNKS + cx, tileZ * TILE_CHUNKS + cz));
                if (colours == null) {
                    continue;
                }
                for (int sx = 0; sx < samples; sx++) {
                    for (int sz = 0; sz < samples; sz++) {
                        int abgr = MapColor.getColorFromPackedId(colours[sz * samples + sx] & 0xFF);
                        // Unmapped spots (air, the void) read as faded paper rather than fog.
                        image.setPixelRGBA(cx * samples + sx, cz * samples + sz, abgr == 0 ? 0xFFB5CBDB : parchment(abgr));
                    }
                }
            }
        }
    }

    /** Fades a vanilla map colour (ABGR) toward sepia, so the map reads as ink on paper. */
    static int parchment(int abgr) {
        int r = abgr & 0xFF;
        int g = (abgr >> 8) & 0xFF;
        int b = (abgr >> 16) & 0xFF;
        float grey = 0.3F * r + 0.59F * g + 0.11F * b;
        float mix = 0.35F;
        int nr = clamp(r * (1 - mix) + (grey * 1.05F + 22) * mix);
        int ng = clamp(g * (1 - mix) + (grey * 0.95F + 12) * mix);
        int nb = clamp(b * (1 - mix) + (grey * 0.78F) * mix);
        return 0xFF000000 | (nb << 16) | (ng << 8) | nr;
    }

    private static int clamp(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    // Danger

    /** Tint colour for a creature rank ordinal. */
    public static int dangerColour(int rating) {
        return switch (CreatureRank.byOrdinal(rating)) {
            case DORMANT -> 0xE0C84A;
            case AWAKENED -> 0xE8862E;
            case FALLEN -> 0xCF3A26;
            case CORRUPTED -> 0x9A1F55;
            case GREAT -> 0x5E2491;
            case CURSED -> 0x2E145A;
            case UNHOLY -> 0x0E0A10;
        };
    }

    /** The rating of a danger cell: the reading's own mark, or the Dream Realm region it lies in. */
    public static int rating(FieldGuideStatusPayload status, String dimension, int cellX, int cellZ) {
        int rating = -1;
        for (FieldGuideStatusPayload.Danger danger : status.danger()) {
            if (danger.cellX() == cellX && danger.cellZ() == cellZ && danger.dimension().equals(dimension)) {
                rating = Math.max(rating, danger.rating());
            }
        }
        if (dimension.equals(status.realm())) {
            int cell = status.dangerCell();
            double mx = cellX * cell + cell / 2.0;
            double mz = cellZ * cell + cell / 2.0;
            double distance = Math.sqrt(mx * mx + mz * mz);
            for (FieldGuideStatusPayload.Region region : status.regions()) {
                if (distance >= region.min() && distance < region.max()) {
                    rating = Math.max(rating, region.rank());
                }
            }
        }
        return rating;
    }

    /**
     * Tints the explored ground in view by its danger rating. Each explored
     * chunk takes its cell's colour, so the tint follows the fog's edge;
     * cells rated from what was seen there are drawn stronger than a bare
     * region baseline.
     */
    public static void drawDanger(GuiGraphics graphics, String dimension, View view, FieldGuideStatusPayload status) {
        int cell = Math.max(16, status.dangerCell());
        Map<Long, Integer> marked = new HashMap<>();
        for (FieldGuideStatusPayload.Danger danger : status.danger()) {
            if (danger.dimension().equals(dimension)) {
                marked.merge(ChunkPos.asLong(danger.cellX(), danger.cellZ()), danger.rating(), Math::max);
            }
        }
        boolean realm = dimension.equals(status.realm());
        if (!realm && marked.isEmpty()) {
            return;
        }
        int minX = Math.floorDiv((int) Math.floor(view.blockX(view.left())), 16);
        int maxX = Math.floorDiv((int) Math.floor(view.blockX(view.left() + view.width())), 16);
        int minZ = Math.floorDiv((int) Math.floor(view.blockZ(view.top())), 16);
        int maxZ = Math.floorDiv((int) Math.floor(view.blockZ(view.top() + view.height())), 16);
        if ((long) (maxX - minX + 1) * (maxZ - minZ + 1) > 60_000) {
            return;
        }
        Map<Long, Integer> ratings = new HashMap<>();
        graphics.enableScissor(view.left(), view.top(), view.left() + view.width(), view.top() + view.height());
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                if (!ClientFieldGuideState.explored(dimension, cx, cz)) {
                    continue;
                }
                long cellKey = ChunkPos.asLong(Math.floorDiv(cx * 16, cell), Math.floorDiv(cz * 16, cell));
                int rating = ratings.computeIfAbsent(cellKey, key -> {
                    int value = marked.getOrDefault(key, -1);
                    return realm ? Math.max(value, rating(status, dimension, ChunkPos.getX(key), ChunkPos.getZ(key))) : value;
                });
                if (rating < 0) {
                    continue;
                }
                int alpha = marked.containsKey(cellKey) ? 0x70000000 : 0x38000000;
                graphics.fill(view.screenX(cx * 16), view.screenY(cz * 16), view.screenX((cx + 1) * 16), view.screenY((cz + 1) * 16),
                        alpha | dangerColour(rating));
            }
        }
        graphics.disableScissor();
    }

    // Markers

    public static int pinColour(String kind, boolean active) {
        if (!active) {
            return 0xFF8A8076;
        }
        return switch (kind) {
            case "place" -> 0xFF2D5FA8;
            case "warning" -> 0xFFB3261E;
            case "gate" -> 0xFF7B3FC4;
            case "seed" -> 0xFF7A1020;
            case "anchor" -> 0xFF1F8FA3;
            case "citadel" -> 0xFF5B4A7A;
            default -> INK;
        };
    }

    /** A pin's marker, centred on (x, y). */
    public static void drawPin(GuiGraphics graphics, String kind, int x, int y, boolean active) {
        int colour = pinColour(kind, active);
        switch (kind) {
            case "gate" -> {
                ring(graphics, x, y, 4, 0xFF1A1020);
                ring(graphics, x, y, 3, colour);
                graphics.fill(x - 1, y - 1, x + 2, y + 2, active ? 0xFFE9D7FF : 0xFFCFC6BB);
            }
            case "seed" -> {
                diamond(graphics, x, y, 4, 0xFF1A0A0A);
                diamond(graphics, x, y, 3, colour);
                graphics.fill(x, y - 1, x + 1, y + 2, 0xFFF0A0A0);
            }
            case "citadel" -> {
                // A keep: a squat block with two towers either side of a lit gate.
                graphics.fill(x - 5, y - 2, x + 6, y + 5, 0xFF140E1C);
                graphics.fill(x - 5, y - 5, x - 2, y - 1, 0xFF140E1C);
                graphics.fill(x + 3, y - 5, x + 6, y - 1, 0xFF140E1C);
                graphics.fill(x - 4, y - 1, x + 5, y + 4, colour);
                graphics.fill(x - 4, y - 4, x - 2, y - 1, colour);
                graphics.fill(x + 3, y - 4, x + 5, y - 1, colour);
                graphics.fill(x, y + 1, x + 1, y + 4, active ? 0xFFD9B8FF : 0xFFCFC6BB);
            }
            case "anchor" -> {
                graphics.fill(x - 4, y - 1, x + 5, y + 2, 0xFF0C2A30);
                graphics.fill(x - 1, y - 4, x + 2, y + 5, 0xFF0C2A30);
                graphics.fill(x - 3, y, x + 4, y + 1, colour);
                graphics.fill(x, y - 3, x + 1, y + 4, colour);
            }
            case "warning" -> {
                graphics.fill(x - 3, y - 4, x + 4, y + 4, 0xFF2A0A08);
                graphics.fill(x - 2, y - 3, x + 3, y + 3, colour);
                graphics.fill(x, y - 2, x + 1, y + 1, 0xFFFFE8E0);
                graphics.fill(x, y + 2, x + 1, y + 3, 0xFFFFE8E0);
            }
            default -> {
                diamond(graphics, x, y, 4, 0xFF0E1A2E);
                diamond(graphics, x, y, 3, colour);
            }
        }
    }

    /** A Seed's Gate spot in the waking world: a dashed red square. */
    public static void drawThreat(GuiGraphics graphics, int x, int y) {
        int colour = 0xFFB3261E;
        for (int i = -4; i <= 4; i += 2) {
            graphics.fill(x + i, y - 4, x + i + 1, y - 3, colour);
            graphics.fill(x + i, y + 4, x + i + 1, y + 5, colour);
            graphics.fill(x - 4, y + i, x - 3, y + i + 1, colour);
            graphics.fill(x + 4, y + i, x + 5, y + i + 1, colour);
        }
        graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xC0B3261E);
    }

    /** Where a creature was last seen: a small dark claw mark. */
    public static void drawCreature(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + 3, y + 3, 0xFFEADFC2);
        graphics.fill(x - 2, y - 2, x - 1, y + 2, 0xFF3B2A1A);
        graphics.fill(x, y - 2, x + 1, y + 3, 0xFF3B2A1A);
        graphics.fill(x + 2, y - 1, x + 3, y + 2, 0xFF3B2A1A);
    }

    /** The player: a white arrow facing their yaw. */
    public static void drawPlayer(GuiGraphics graphics, int x, int y, float yaw) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 0.5F, y + 0.5F, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(yaw + 180.0F));
        for (int row = 0; row < 7; row++) {
            int half = row / 2;
            graphics.fill(-half - 1, row - 4, half + 2, row - 3, 0xFF1A120A);
        }
        for (int row = 1; row < 6; row++) {
            int half = (row - 1) / 2;
            graphics.fill(-half, row - 4, half + 1, row - 3, 0xFFFFFFFF);
        }
        graphics.pose().popPose();
    }

    private static void ring(GuiGraphics graphics, int x, int y, int radius, int colour) {
        graphics.fill(x - radius, y - radius, x + radius + 1, y + radius + 1, colour);
    }

    private static void diamond(GuiGraphics graphics, int x, int y, int radius, int colour) {
        for (int dy = -radius; dy <= radius; dy++) {
            int half = radius - Math.abs(dy);
            graphics.fill(x - half, y + dy, x + half + 1, y + dy + 1, colour);
        }
    }
}
