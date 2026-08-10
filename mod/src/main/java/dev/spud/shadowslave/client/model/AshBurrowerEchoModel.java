package dev.spud.shadowslave.client.model;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.echo.AshBurrowerEchoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** GeckoLib model binding for the player-owned Ash Burrower Echo executor. */
public final class AshBurrowerEchoModel extends GeoModel<AshBurrowerEchoEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "geo/ash_burrower.geo.json");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "animations/ash_burrower.animation.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ShadowSlaveMod.MOD_ID, "textures/entity/ash_burrower.png");

    @Override
    public ResourceLocation getModelResource(AshBurrowerEchoEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AshBurrowerEchoEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AshBurrowerEchoEntity animatable) {
        return ANIMATIONS;
    }
}
