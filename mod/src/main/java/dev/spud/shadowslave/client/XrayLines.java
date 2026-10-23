package dev.spud.shadowslave.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

import java.util.OptionalDouble;
import java.util.function.BiConsumer;

/**
 * Lines drawn on top of everything, so what they outline shows through
 * stone: Ore Sense's ores and the Wayfarer's Waymarks. Call from
 * {@link RenderLevelStageEvent.Stage#AFTER_PARTICLES}; the lines are given in
 * world coordinates.
 */
final class XrayLines {
    private XrayLines() {
    }

    static void draw(RenderLevelStageEvent event, BiConsumer<PoseStack, BufferBuilder> lines) {
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
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        lines.accept(pose, builder);
        // The "always" depth state leaves the test as it found it, so switch it off here.
        MeshData mesh = builder.build();
        if (mesh != null) {
            RenderSystem.disableDepthTest();
            Type.LINES.draw(mesh);
            RenderSystem.enableDepthTest();
        }
        modelView.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private static final class Type extends RenderType {
        static final RenderType LINES = create("shadowslave_xray_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL,
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

        private Type(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean affectsCrumbling,
                     boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, size, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
