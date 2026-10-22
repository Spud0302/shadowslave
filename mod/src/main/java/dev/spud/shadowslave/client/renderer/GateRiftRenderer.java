package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.CreatureModel;
import dev.spud.shadowslave.gate.GateRiftEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer for a Gate's rift: drawn translucent and at full brightness, so the
 * tear reads as light coming through rather than an object standing there.
 */
public final class GateRiftRenderer extends GeoEntityRenderer<GateRiftEntity> {
    public GateRiftRenderer(EntityRendererProvider.Context context) {
        super(context, new CreatureModel<>("gate_rift"));
        this.shadowRadius = 0F;
    }

    @Override
    public RenderType getRenderType(GateRiftEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentEmissive(texture);
    }

    @Override
    protected int getBlockLightLevel(GateRiftEntity entity, BlockPos pos) {
        return 15;
    }
}
