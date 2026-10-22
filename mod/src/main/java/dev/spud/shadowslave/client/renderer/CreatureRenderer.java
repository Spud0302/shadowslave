package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.client.model.CreatureModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renderer for a Nightmare Creature's GeckoLib model. A creature with glowing
 * parts (embers, eyes) also gets its {@code <name>_glowmask.png} drawn at
 * full brightness.
 */
public class CreatureRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {
    public CreatureRenderer(EntityRendererProvider.Context context, String name, float shadowRadius, boolean glows) {
        super(context, new CreatureModel<>(name));
        this.shadowRadius = shadowRadius;
        if (glows) {
            addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }
}
