package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

/**
 * Delve's Ore Sense: while a Delve sneaks, the ores within {@link #RANGE}
 * blocks are outlined through the stone, tinted by what they are. The scan
 * runs on the client from blocks it already knows, a few times a second.
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class OreSenseRenderer {
    static final int RANGE = 8;
    static final int SCAN_TICKS = 5;
    private static final String INNATE = AspectKitCatalog.DELVE.innateId().toString();

    record Seen(BlockPos pos, int rgb) {
    }

    private static List<Seen> seen = List.of();
    private static long lastScan = -SCAN_TICKS;

    private OreSenseRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.isShiftKeyDown() || !hasOreSense()) {
            seen = List.of();
            lastScan = -SCAN_TICKS;
            return;
        }
        long now = minecraft.level.getGameTime();
        if (now - lastScan >= SCAN_TICKS || now < lastScan) {
            seen = scan(minecraft.level, player.blockPosition());
            lastScan = now;
        }
        if (seen.isEmpty()) {
            return;
        }
        XrayLines.draw(event, (pose, lines) -> {
            for (Seen ore : seen) {
                float r = (ore.rgb() >> 16 & 0xFF) / 255.0F;
                float g = (ore.rgb() >> 8 & 0xFF) / 255.0F;
                float b = (ore.rgb() & 0xFF) / 255.0F;
                LevelRenderer.renderLineBox(pose, lines, new AABB(ore.pos()).deflate(0.02), r, g, b, 0.9F);
            }
        });
    }

    static boolean hasOreSense() {
        return ClientSoulState.latest().map(soul -> soul.abilityIds().contains(INNATE)).orElse(false);
    }

    private static List<Seen> scan(Level level, BlockPos center) {
        List<Seen> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, -RANGE, -RANGE), center.offset(RANGE, RANGE, RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Tags.Blocks.ORES)) {
                found.add(new Seen(pos.immutable(), colour(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath())));
            }
        }
        return List.copyOf(found);
    }

    /** A tint per ore, from its id. */
    static int colour(String path) {
        if (path.contains("diamond")) {
            return 0x5CF2E8;
        } else if (path.contains("emerald")) {
            return 0x3BE36B;
        } else if (path.contains("gold")) {
            return 0xFFD23F;
        } else if (path.contains("iron")) {
            return 0xE0B48C;
        } else if (path.contains("copper")) {
            return 0xE07A3F;
        } else if (path.contains("redstone")) {
            return 0xFF3B30;
        } else if (path.contains("lapis")) {
            return 0x3F6BFF;
        } else if (path.contains("coal")) {
            return 0x5A5A5A;
        } else if (path.contains("quartz")) {
            return 0xF2EDE4;
        } else if (path.contains("debris")) {
            return 0x9A5A3C;
        }
        return 0xFFFFFF;
    }
}
