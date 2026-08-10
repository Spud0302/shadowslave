package dev.spud.shadowslave.client.model;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.world.entity.DrownedListenerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** GeckoLib presentation binding for the Drowned Listener physical executor. */
public final class DrownedListenerModel extends GeoModel<DrownedListenerEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "geo/drowned_listener.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "animations/drowned_listener.animation.json");
    private static final ResourceLocation PLACEHOLDER_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/zombie/drowned.png");

    @Override
    public ResourceLocation getModelResource(DrownedListenerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DrownedListenerEntity animatable) {
        return PLACEHOLDER_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DrownedListenerEntity animatable) {
        return ANIMATIONS;
    }
}
