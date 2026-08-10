package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.DrownedListenerModel;
import dev.spud.shadowslave.world.entity.DrownedListenerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Client-only GeckoLib renderer for the Drowned Listener execution adapter. */
public final class DrownedListenerRenderer extends GeoEntityRenderer<DrownedListenerEntity> {
    public DrownedListenerRenderer(EntityRendererProvider.Context context) {
        super(context, new DrownedListenerModel());
        this.shadowRadius = 0.45F;
    }
}
