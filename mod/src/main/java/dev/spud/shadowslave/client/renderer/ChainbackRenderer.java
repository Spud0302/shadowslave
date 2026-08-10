package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.ChainbackModel;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Client-only GeckoLib renderer for the Chainback execution adapter. */
public final class ChainbackRenderer extends GeoEntityRenderer<ChainbackEntity> {
    public ChainbackRenderer(EntityRendererProvider.Context context) {
        super(context, new ChainbackModel());
        this.shadowRadius = 0.65F;
    }
}
