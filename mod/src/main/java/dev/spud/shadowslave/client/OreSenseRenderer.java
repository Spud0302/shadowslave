package dev.spud.shadowslave.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;

import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

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
        // Draw in our own frame (camera turn, then offset) so the stage's model-view state doesn't matter.
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        RenderSystem.applyModelViewMatrix();
        PoseStack pose = new PoseStack();
        pose.mulPose(event.getModelViewMatrix());
        Vec3 camera = event.getCamera().getPosition();
        pose.translate(-camera.x, -camera.y, -camera.z);
        // A buffer of our own: the shared one may hold another batch that must not be flushed under our matrices.
        BufferBuilder lines = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (Seen ore : seen) {
            float r = (ore.rgb() >> 16 & 0xFF) / 255.0F;
            float g = (ore.rgb() >> 8 & 0xFF) / 255.0F;
            float b = (ore.rgb() & 0xFF) / 255.0F;
            LevelRenderer.renderLineBox(pose, lines, new AABB(ore.pos()).deflate(0.02), r, g, b, 0.9F);
        }
        // The "always" depth state leaves the test as it found it, so switch it off here.
        MeshData mesh = lines.build();
        if (mesh != null) {
            RenderSystem.disableDepthTest();
            OreLines.TYPE.draw(mesh);
            RenderSystem.enableDepthTest();
        }
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
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

    /** Lines drawn on top of everything, so ores show through the stone. */
    private static final class OreLines extends RenderType {
        static final RenderType TYPE = create("shadowslave_ore_sense", DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES, 1536, false, false,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new LineStateShard(OptionalDouble.of(2.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .createCompositeState(false));

        private OreLines(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean affectsCrumbling,
                         boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, size, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
