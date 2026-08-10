package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.AshBurrowerModel;
import dev.spud.shadowslave.world.entity.AshBurrowerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Client-only GeckoLib renderer for the hostile Ash Burrower executor. */
public final class AshBurrowerRenderer extends GeoEntityRenderer<AshBurrowerEntity> {
    public AshBurrowerRenderer(EntityRendererProvider.Context context) {
        super(context, new AshBurrowerModel());
        this.shadowRadius = 0.45F;
    }
}
