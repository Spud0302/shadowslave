package dev.spud.shadowslave.client.model;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.world.entity.ChainbackEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** GeckoLib presentation binding for the Chainback physical executor. */
public final class ChainbackModel extends GeoModel<ChainbackEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "geo/chainback.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "animations/chainback.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "textures/entity/chainback.png");

    @Override
    public ResourceLocation getModelResource(ChainbackEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChainbackEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChainbackEntity animatable) {
        return ANIMATIONS;
    }
}
