package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.AshBurrowerEchoModel;
import dev.spud.shadowslave.echo.AshBurrowerEchoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Client-only GeckoLib renderer for the player-owned Ash Burrower Echo executor. */
public final class AshBurrowerEchoRenderer extends GeoEntityRenderer<AshBurrowerEchoEntity> {
    public AshBurrowerEchoRenderer(EntityRendererProvider.Context context) {
        super(context, new AshBurrowerEchoModel());
        this.shadowRadius = 0.45F;
    }
}
